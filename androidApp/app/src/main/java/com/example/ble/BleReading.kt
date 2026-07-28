package com.example.ble

import kotlinx.serialization.Serializable

@Serializable
data class BleReading(
    val na: Double,
    val temp: Double,
    val mv: Double,
    val status: String,
    val e0: Double,
    val slope: Double,
    val battery: Int = 0,
    val batteryVoltage: Double = 0.0,
    val firmware: String = "1.0.0",
    val deviceName: String = "NaTrack"
)
