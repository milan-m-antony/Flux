package com.example.ble

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class BLEStateData(
    val temperature: Double,
    val battery: Int,
    val batteryVoltage: Double,
    val charging: Boolean,
    val sodium: String = "---",
    val status: String = "Normal",
    val firmware: String = "v2.0.0",
    val device: String = "Flux1",
    val mode: String = "Live Monitor",
    val unit: String = "C",
    val minTemp: Double = 0.0,
    val maxTemp: Double = 0.0,
    val avgTemp: Double = 0.0,
    val elapsedSec: Long = 0L,
    val alert: Boolean = false,
    val buzzer: Boolean = true,
    val wifiConnected: Boolean = false,
    val wifiRssi: Int = 0,
    val wifiSsid: String = ""
)

object BLEParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(payload: String): BLEStateData? {
        return try {
            json.decodeFromString<BLEStateData>(payload)
        } catch (e: Exception) {
            android.util.Log.e("BLEParser", "Failed to parse JSON payload: $payload", e)
            null
        }
    }
}
