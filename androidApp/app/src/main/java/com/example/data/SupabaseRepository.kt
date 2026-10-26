package com.example.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.math.min
import kotlin.random.Random

@Serializable
data class TelemetryUploadPayload(
    val device_id: String,
    val temperature_celsius: Double,
    val active_profile: String,
    val is_alert: Boolean,
    val source: String = "BLE"
)

@Serializable
data class DeviceStatePayload(
    val device_id: String,
    val latest_temperature_celsius: Double,
    val active_profile: String,
    val preferred_unit: String,
    val battery_percentage: Int,
    val battery_voltage: Double,
    val charging: Boolean,
    val sensor_connected: Boolean,
    val is_alert: Boolean,
    val status_text: String
)

@Serializable
data class TelemetryHistoricalPoint(
    val id: Long = 0,
    val recorded_at: String = "",
    val temperature_celsius: Double = 0.0,
    val active_profile: String = "Live Monitor",
    val is_alert: Boolean = false
)

// DTOs for Render Backend API
@Serializable
data class BackendTelemetryRequest(
    val deviceId: String,
    val temperatureCelsius: Double,
    val recordedAt: String,
    val activeProfile: String,
    val isAlert: Boolean,
    val source: String = "BLE",
    val batteryPercentage: Int? = null,
    val batteryVoltage: Double? = null,
    val sensorConnected: Boolean = true,
    val idempotencyKey: String
)

@Serializable
data class BackendTelemetryResponse(
    val success: Boolean = false,
    val message: String? = null,
    val code: String? = null,
    val error: String? = null
)

@Serializable
data class BackendHistoricalItem(
    val id: Long = 0,
    val deviceId: String = "",
    val temperatureCelsius: Double = 0.0,
    val activeProfile: String = "Live Monitor",
    val isAlert: Boolean = false,
    val source: String = "BLE",
    val recordedAt: String = "",
    val createdAt: String = ""
)

@Serializable
data class BackendHistoricalResponse(
    val success: Boolean = false,
    val data: List<BackendHistoricalItem> = emptyList()
)

class SupabaseRepository(
    private val scope: CoroutineScope,
    private val queuedTelemetryDao: QueuedTelemetryDao? = null,
    private val firebaseAuth: FirebaseAuth? = null
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val _syncStatus = MutableStateFlow("Ready")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _pendingQueueCount = MutableStateFlow(0)
    val pendingQueueCount: StateFlow<Int> = _pendingQueueCount.asStateFlow()

    private val _historicalTelemetry = MutableStateFlow<List<TelemetryHistoricalPoint>>(emptyList())
    val historicalTelemetry: StateFlow<List<TelemetryHistoricalPoint>> = _historicalTelemetry.asStateFlow()

    private var lastUploadTimeMs = 0L
    private var isFlushing = false

    init {
        // Collect live queue count from durable Room database
        if (queuedTelemetryDao != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    queuedTelemetryDao.getQueueCountFlow().collect { count ->
                        _pendingQueueCount.value = count
                    }
                } catch (e: Exception) {
                    Log.w("BackendRepo", "Error observing queue count: ${e.message}")
                }
            }
        }
    }

    private suspend fun getFirebaseIdToken(forceRefresh: Boolean = false): String? {
        val user = firebaseAuth?.currentUser ?: return null
        return suspendCancellableCoroutine { cont ->
            user.getIdToken(forceRefresh).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    cont.resume(task.result?.token)
                } else {
                    Log.w("BackendRepo", "Failed to get Firebase token: ${task.exception?.message}")
                    cont.resume(null)
                }
            }
        }
    }

    private fun formatIsoTimestamp(ms: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(ms))
    }

    fun uploadTelemetry(
        supabaseUrl: String,
        anonKey: String,
        deviceId: String,
        temperatureC: Double,
        profile: String,
        isAlert: Boolean,
        unit: String,
        batteryPercent: Int,
        batteryVolts: Double,
        charging: Boolean,
        sensorConnected: Boolean,
        uploadIntervalSec: Int,
        backendUrl: String = SettingsRepository.DEFAULT_BACKEND_URL
    ) {
        val now = System.currentTimeMillis()
        val intervalMs = uploadIntervalSec * 1000L

        // Enforce quota rate-limiting unless urgent alert
        if (!isAlert && (now - lastUploadTimeMs) < intervalMs) {
            return
        }
        lastUploadTimeMs = now

        val idempotencyKey = UUID.randomUUID().toString()

        scope.launch(Dispatchers.IO) {
            try {
                // 1. Persist unsent reading into durable Room database first
                if (queuedTelemetryDao != null) {
                    val queuedItem = QueuedTelemetryEntity(
                        idempotencyKey = idempotencyKey,
                        deviceId = deviceId,
                        temperatureCelsius = temperatureC,
                        recordedAtMs = now,
                        activeProfile = profile,
                        isAlert = isAlert,
                        source = "BLE",
                        batteryPercentage = batteryPercent,
                        batteryVoltage = batteryVolts,
                        sensorConnected = sensorConnected,
                        retryCount = 0,
                        lastAttemptMs = 0L,
                        createdAtMs = now
                    )
                    queuedTelemetryDao.insert(queuedItem)
                    // Bounded queue protection: prune oldest beyond 500
                    queuedTelemetryDao.pruneOldest(500)
                }

                // 2. Trigger asynchronous queue flush to Render Backend API
                flushOfflineQueue(backendUrl)
            } catch (e: Exception) {
                Log.w("BackendRepo", "Error queuing telemetry locally: ${e.message}")
                _syncStatus.value = "Offline (Buffered)"
            }
        }
    }

    private suspend fun flushOfflineQueue(backendUrl: String) = withContext(Dispatchers.IO) {
        if (isFlushing || queuedTelemetryDao == null) return@withContext
        isFlushing = true

        try {
            val token = getFirebaseIdToken(forceRefresh = false)
            if (token == null) {
                _syncStatus.value = "Awaiting Authentication"
                isFlushing = false
                return@withContext
            }

            // Fetch oldest batch of up to 10 entries from durable Room queue
            val batch = queuedTelemetryDao.getOldestBatch(10)
            if (batch.isEmpty()) {
                isFlushing = false
                return@withContext
            }

            val targetUrl = "${backendUrl.trimEnd('/')}/api/v1/telemetry"
            val successfullySentIds = mutableListOf<Long>()

            for (record in batch) {
                val reqPayload = BackendTelemetryRequest(
                    deviceId = record.deviceId,
                    temperatureCelsius = record.temperatureCelsius,
                    recordedAt = formatIsoTimestamp(record.recordedAtMs),
                    activeProfile = record.activeProfile,
                    isAlert = record.isAlert,
                    source = record.source,
                    batteryPercentage = record.batteryPercentage,
                    batteryVoltage = record.batteryVoltage,
                    sensorConnected = record.sensorConnected,
                    idempotencyKey = record.idempotencyKey
                )

                val body = json.encodeToString(reqPayload).toRequestBody(jsonMediaType)
                val request = Request.Builder()
                    .url(targetUrl)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()

                try {
                    client.newCall(request).execute().use { resp ->
                        if (resp.isSuccessful) {
                            successfullySentIds.add(record.id)
                        } else if (resp.code == 401) {
                            // Token might be expired, force refresh on next run
                            _syncStatus.value = "Token Expired (Refreshing)"
                            getFirebaseIdToken(forceRefresh = true)
                            return@use
                        } else if (resp.code == 403) {
                            _syncStatus.value = "Device Access Denied"
                            return@use
                        } else {
                            _syncStatus.value = "Sync Error (${resp.code})"
                            // Exponential backoff
                            val retryCount = record.retryCount + 1
                            val backoffMs = min(30000L, (1000L * (1 shl min(retryCount, 5))) + Random.nextLong(0, 1000L))
                            queuedTelemetryDao.update(record.copy(retryCount = retryCount, lastAttemptMs = System.currentTimeMillis()))
                            delay(backoffMs)
                        }
                    }
                } catch (e: Exception) {
                    Log.w("BackendRepo", "Network call failed during queue flush: ${e.message}")
                    _syncStatus.value = "Offline (Buffered)"
                    val retryCount = record.retryCount + 1
                    val backoffMs = min(30000L, (1000L * (1 shl min(retryCount, 5))) + Random.nextLong(0, 1000L))
                    queuedTelemetryDao.update(record.copy(retryCount = retryCount, lastAttemptMs = System.currentTimeMillis()))
                    delay(backoffMs)
                    break
                }
            }

            // Remove confirmed accepted entries only
            if (successfullySentIds.isNotEmpty()) {
                queuedTelemetryDao.deleteBatch(successfullySentIds)
                _syncStatus.value = "Synced"
            }
        } finally {
            isFlushing = false
        }
    }

    suspend fun fetchHistoricalTelemetry(
        supabaseUrl: String,
        anonKey: String,
        deviceId: String,
        backendUrl: String = SettingsRepository.DEFAULT_BACKEND_URL
    ) = withContext(Dispatchers.IO) {
        val targetUrl = "${backendUrl.trimEnd('/')}/api/v1/devices/$deviceId/telemetry?limit=50"
        try {
            val token = getFirebaseIdToken(forceRefresh = false)
            val reqBuilder = Request.Builder().url(targetUrl).get()
            if (!token.isNullOrBlank()) {
                reqBuilder.addHeader("Authorization", "Bearer $token")
            }

            client.newCall(reqBuilder.build()).execute().use { resp ->
                if (resp.isSuccessful) {
                    val rawJson = resp.body?.string() ?: ""
                    val parsed = json.decodeFromString<BackendHistoricalResponse>(rawJson)
                    val points = parsed.data.map { item ->
                        TelemetryHistoricalPoint(
                            id = item.id,
                            recorded_at = item.recordedAt,
                            temperature_celsius = item.temperatureCelsius,
                            active_profile = item.activeProfile,
                            is_alert = item.isAlert
                        )
                    }
                    _historicalTelemetry.value = points
                } else {
                    Log.w("BackendRepo", "Historical fetch failed with status: ${resp.code}")
                }
            }
        } catch (e: Exception) {
            Log.e("BackendRepo", "Failed to fetch telemetry history from backend: ${e.message}")
        }
    }

    suspend fun flushOfflineQueueNow(
        supabaseUrl: String,
        anonKey: String,
        backendUrl: String = SettingsRepository.DEFAULT_BACKEND_URL
    ) {
        flushOfflineQueue(backendUrl)
    }
}
