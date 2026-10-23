package com.example.data

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import java.util.concurrent.ConcurrentLinkedQueue

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

class SupabaseRepository(
    private val scope: CoroutineScope
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

    private val offlineQueue = ConcurrentLinkedQueue<TelemetryUploadPayload>()
    private var lastUploadTimeMs = 0L

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
        uploadIntervalSec: Int
    ) {
        if (supabaseUrl.isBlank() || anonKey.isBlank()) {
            _syncStatus.value = "Awaiting Credentials"
            return
        }

        val now = System.currentTimeMillis()
        val intervalMs = uploadIntervalSec * 1000L

        // Enforce quota rate-limiting (default 5s) unless urgent alert
        if (!isAlert && (now - lastUploadTimeMs) < intervalMs) {
            return
        }
        lastUploadTimeMs = now

        val payload = TelemetryUploadPayload(
            device_id = deviceId,
            temperature_celsius = temperatureC,
            active_profile = profile,
            is_alert = isAlert
        )

        val statePayload = DeviceStatePayload(
            device_id = deviceId,
            latest_temperature_celsius = temperatureC,
            active_profile = profile,
            preferred_unit = unit,
            battery_percentage = batteryPercent,
            battery_voltage = batteryVolts,
            charging = charging,
            sensor_connected = sensorConnected,
            is_alert = isAlert,
            status_text = if (isAlert) "Threshold Alert" else "Normal"
        )

        scope.launch(Dispatchers.IO) {
            try {
                // 1. Post to Telemetry time-series table
                val telemetryBody = json.encodeToString(listOf(payload)).toRequestBody(jsonMediaType)
                val telemetryReq = Request.Builder()
                    .url("${supabaseUrl.trimEnd('/')}/rest/v1/telemetry")
                    .addHeader("apikey", anonKey)
                    .addHeader("Authorization", "Bearer $anonKey")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=minimal")
                    .post(telemetryBody)
                    .build()

                client.newCall(telemetryReq).execute().use { resp ->
                    if (!resp.isSuccessful) {
                        enqueueOffline(payload)
                    }
                }

                // 2. Upsert into Device State single-row table (for instant UI & Realtime reads)
                val stateBody = json.encodeToString(statePayload).toRequestBody(jsonMediaType)
                val stateReq = Request.Builder()
                    .url("${supabaseUrl.trimEnd('/')}/rest/v1/device_state")
                    .addHeader("apikey", anonKey)
                    .addHeader("Authorization", "Bearer $anonKey")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "resolution=merge-duplicates")
                    .post(stateBody)
                    .build()

                client.newCall(stateReq).execute().use { resp ->
                    if (resp.isSuccessful) {
                        _syncStatus.value = "Synced"
                        flushOfflineQueue(supabaseUrl, anonKey)
                    } else {
                        _syncStatus.value = "Sync Error (${resp.code})"
                    }
                }
            } catch (e: Exception) {
                Log.w("SupabaseRepo", "Network error uploading telemetry, buffering offline: ${e.message}")
                enqueueOffline(payload)
                _syncStatus.value = "Offline (Buffered)"
            }
        }
    }

    private fun enqueueOffline(payload: TelemetryUploadPayload) {
        if (offlineQueue.size < 500) { // Bound offline queue to 500 to prevent memory exhaustion
            offlineQueue.offer(payload)
        }
        _pendingQueueCount.value = offlineQueue.size
    }

    private suspend fun flushOfflineQueue(supabaseUrl: String, anonKey: String) = withContext(Dispatchers.IO) {
        if (offlineQueue.isEmpty()) return@withContext
        val batch = mutableListOf<TelemetryUploadPayload>()
        while (!offlineQueue.isEmpty() && batch.size < 50) {
            offlineQueue.poll()?.let { batch.add(it) }
        }
        if (batch.isEmpty()) return@withContext

        try {
            val body = json.encodeToString(batch).toRequestBody(jsonMediaType)
            val req = Request.Builder()
                .url("${supabaseUrl.trimEnd('/')}/rest/v1/telemetry")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(body)
                .build()

            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    _pendingQueueCount.value = offlineQueue.size
                    _syncStatus.value = "Synced"
                } else {
                    // Re-enqueue batch
                    batch.forEach { offlineQueue.offer(it) }
                    _pendingQueueCount.value = offlineQueue.size
                }
            }
        } catch (e: Exception) {
            batch.forEach { offlineQueue.offer(it) }
            _pendingQueueCount.value = offlineQueue.size
        }
    }

    suspend fun fetchHistoricalTelemetry(supabaseUrl: String, anonKey: String, deviceId: String) = withContext(Dispatchers.IO) {
        if (supabaseUrl.isBlank() || anonKey.isBlank()) return@withContext
        try {
            val req = Request.Builder()
                .url("${supabaseUrl.trimEnd('/')}/rest/v1/telemetry?device_id=eq.$deviceId&order=recorded_at.desc&limit=50")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .get()
                .build()

            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val rawJson = resp.body?.string() ?: "[]"
                    val points = json.decodeFromString<List<TelemetryHistoricalPoint>>(rawJson)
                    _historicalTelemetry.value = points
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseRepo", "Failed to fetch telemetry history: ${e.message}")
        }
    }
}
