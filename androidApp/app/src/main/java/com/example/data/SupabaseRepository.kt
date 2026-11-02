package com.example.data

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.random.Random

@Serializable
data class TelemetryHistoricalPoint(
    val id: Long? = null,
    val recorded_at: String? = null,
    val temperature_celsius: Double = 0.0,
    val active_profile: String? = null,
    val is_alert: Boolean = false
)

@Serializable
data class BackendTelemetryRequest(
    @SerialName("device_id") val deviceId: String,
    @SerialName("temperature_celsius") val temperatureCelsius: Double,
    @SerialName("recorded_at") val recordedAt: String,
    @SerialName("active_profile") val activeProfile: String,
    @SerialName("is_alert") val isAlert: Boolean,
    @SerialName("source") val source: String,
    @SerialName("battery_percentage") val batteryPercentage: Int? = null,
    @SerialName("battery_voltage") val batteryVoltage: Double? = null,
    @SerialName("sensor_connected") val sensorConnected: Boolean = true,
    @SerialName("idempotency_key") val idempotencyKey: String
)

@Serializable
data class BackendHistoricalItem(
    val id: Long = 0L,
    @SerialName("device_id") val deviceId: String = "",
    @SerialName("temperature_celsius") val temperatureCelsius: Double = 0.0,
    @SerialName("recorded_at") val recordedAt: String = "",
    @SerialName("active_profile") val activeProfile: String = "",
    @SerialName("is_alert") val isAlert: Boolean = false
)

@Serializable
data class BackendHistoricalResponse(
    val success: Boolean = false,
    val data: List<BackendHistoricalItem> = emptyList()
)

class SupabaseRepository(
    private val scope: CoroutineScope,
    private val queuedTelemetryDao: QueuedTelemetryDao? = null,
    private val firebaseAuth: Any? = null
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val _syncStatus = MutableStateFlow("Ready")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _pendingQueueCount = MutableStateFlow(0)
    val pendingQueueCount: StateFlow<Int> = _pendingQueueCount.asStateFlow()

    private val _historicalTelemetry = MutableStateFlow<List<TelemetryHistoricalPoint>>(emptyList())
    val historicalTelemetry: StateFlow<List<TelemetryHistoricalPoint>> = _historicalTelemetry.asStateFlow()

    private var isFlushing = false

    init {
        if (queuedTelemetryDao != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    queuedTelemetryDao.getQueueCountFlow().collect { count ->
                        _pendingQueueCount.value = count
                    }
                } catch (e: Exception) {
                    Log.w("SupabaseRepo", "Error observing queue count: ${e.message}")
                }
            }
        }
    }

    private fun formatIsoTimestamp(ms: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return sdf.format(java.util.Date(ms))
    }

    fun uploadTelemetry(
        supabaseUrl: String,
        anonKey: String,
        deviceId: String,
        temp: Double,
        profile: String,
        isAlert: Boolean,
        batteryPercent: Int? = null,
        batteryVolts: Double? = null,
        sensorConnected: Boolean = true,
        uploadIntervalSec: Int = 5,
        backendUrl: String = SettingsRepository.DEFAULT_BACKEND_URL
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()
                val idempotencyKey = "$deviceId-$now-${Random.nextInt(1000, 9999)}"

                if (queuedTelemetryDao != null) {
                    val queuedItem = QueuedTelemetryEntity(
                        idempotencyKey = idempotencyKey,
                        deviceId = deviceId,
                        temperatureCelsius = temp,
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
                    queuedTelemetryDao.pruneOldest(500)
                }

                flushOfflineQueue(supabaseUrl, anonKey)
            } catch (e: Exception) {
                Log.w("SupabaseRepo", "Error queuing telemetry locally: ${e.message}")
                _syncStatus.value = "Offline (Buffered)"
            }
        }
    }

    private suspend fun flushOfflineQueue(supabaseUrl: String, anonKey: String) = withContext(Dispatchers.IO) {
        if (isFlushing || queuedTelemetryDao == null) return@withContext
        isFlushing = true

        try {
            val batch = queuedTelemetryDao.getOldestBatch(10)
            if (batch.isEmpty()) {
                isFlushing = false
                return@withContext
            }

            val targetUrl = "${supabaseUrl.trimEnd('/')}/rest/v1/telemetry"
            val successfullySentIds = mutableListOf<Long>()

            for (record in batch) {
                val payloadMap = mapOf(
                    "device_id" to record.deviceId,
                    "temperature_celsius" to record.temperatureCelsius,
                    "recorded_at" to formatIsoTimestamp(record.recordedAtMs),
                    "active_profile" to record.activeProfile,
                    "is_alert" to record.isAlert,
                    "source" to record.source
                )

                val body = json.encodeToString(payloadMap).toRequestBody(jsonMediaType)
                val request = Request.Builder()
                    .url(targetUrl)
                    .addHeader("apikey", anonKey)
                    .addHeader("Authorization", "Bearer $anonKey")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=minimal")
                    .post(body)
                    .build()

                try {
                    client.newCall(request).execute().use { resp ->
                        if (resp.isSuccessful) {
                            successfullySentIds.add(record.id)
                        } else {
                            _syncStatus.value = "Sync Error (${resp.code})"
                            val retryCount = record.retryCount + 1
                            val backoffMs = min(30000L, (1000L * (1 shl min(retryCount, 5))) + Random.nextLong(0, 1000L))
                            queuedTelemetryDao.update(record.copy(retryCount = retryCount, lastAttemptMs = System.currentTimeMillis()))
                            delay(backoffMs)
                        }
                    }
                } catch (e: Exception) {
                    Log.w("SupabaseRepo", "Network call failed during queue flush: ${e.message}")
                    _syncStatus.value = "Offline (Buffered)"
                    break
                }
            }

            if (successfullySentIds.isNotEmpty()) {
                queuedTelemetryDao.deleteBatch(successfullySentIds)
                _syncStatus.value = "Synced"
            }
        } finally {
            isFlushing = false
        }
    }

    suspend fun fetchLatestTelemetry(
        supabaseUrl: String,
        anonKey: String,
        deviceId: String = "Flux1"
    ): TelemetryHistoricalPoint? = withContext(Dispatchers.IO) {
        val targetUrl = "${supabaseUrl.trimEnd('/')}/rest/v1/telemetry?device_id=eq.$deviceId&order=created_at.desc&limit=1"
        try {
            val req = Request.Builder()
                .url(targetUrl)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .get()
                .build()

            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val rawJson = resp.body?.string() ?: ""
                    val list = json.decodeFromString<List<TelemetryHistoricalPoint>>(rawJson)
                    return@withContext list.firstOrNull()
                }
            }
        } catch (e: Exception) {
            Log.d("SupabaseRepo", "Fetch latest telemetry failed: ${e.message}")
        }
        null
    }

    suspend fun fetchHistoricalTelemetry(
        supabaseUrl: String,
        anonKey: String,
        deviceId: String = "Flux1",
        backendUrl: String = SettingsRepository.DEFAULT_BACKEND_URL
    ) = withContext(Dispatchers.IO) {
        val targetUrl = "${supabaseUrl.trimEnd('/')}/rest/v1/telemetry?device_id=eq.$deviceId&order=created_at.desc&limit=50"
        try {
            val req = Request.Builder()
                .url(targetUrl)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .get()
                .build()

            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val rawJson = resp.body?.string() ?: ""
                    val list = json.decodeFromString<List<TelemetryHistoricalPoint>>(rawJson)
                    _historicalTelemetry.value = list
                }
            }
        } catch (e: Exception) {
            Log.w("SupabaseRepo", "Failed to fetch telemetry history from Supabase: ${e.message}")
        }
    }

    suspend fun flushOfflineQueueNow(
        supabaseUrl: String,
        anonKey: String,
        backendUrl: String = SettingsRepository.DEFAULT_BACKEND_URL
    ) {
        flushOfflineQueue(supabaseUrl, anonKey)
    }
}
