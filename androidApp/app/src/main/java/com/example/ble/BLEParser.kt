package com.example.ble

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class BLEStateData(
    val temperature: Double,
    val battery: Int,
    val batteryVoltage: Double,
    val charging: Boolean,
    val sodium: String,
    val status: String,
    val firmware: String,
    val device: String
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
