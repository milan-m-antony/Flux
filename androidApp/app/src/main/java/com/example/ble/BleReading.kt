package com.example.ble

import kotlinx.serialization.Serializable

@Serializable
data class BleReading(
    val na: Double = -1.0,
    val temp: Double,
    val mv: Double = 0.0,
    val status: String = "Normal",
    val e0: Double = 150.0,
    val slope: Double = 59.2,
    val battery: Int = 100,
    val batteryVoltage: Double = 4.20,
    val firmware: String = "v2.0.0",
    val deviceName: String = "Flux1",
    val mode: String = "Live Monitor",
    val unit: String = "C",
    val minTemp: Double = 0.0,
    val maxTemp: Double = 0.0,
    val avgTemp: Double = 0.0,
    val elapsedSec: Long = 0L,
    val isAlert: Boolean = false,
    val buzzer: Boolean = true,
    val wifiConnected: Boolean = false,
    val wifiRssi: Int = 0,
    val wifiSsid: String = ""
)
