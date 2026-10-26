package com.example.connectivity

import androidx.compose.ui.graphics.Color

/**
 * Supported connection transport types for Flux Telemetry
 */
enum class ConnectionType(val label: String) {
    WIFI("Wi-Fi"),
    BLUETOOTH("Bluetooth"),
    MOBILE_DATA("Mobile data"),
    HOTSPOT("Hotspot"),
    NONE("Offline")
}

/**
 * Signal strength thresholds matching the UI specification:
 * - Green: 70–100% (Strong)
 * - Orange: 35–69% (Moderate)
 * - Red: 1–34% (Weak)
 * - Grey: Disconnected, stale, or unavailable
 */
enum class SignalLevel(val color: Color) {
    STRONG(Color(0xFF22C55E)),     // 70–100%
    MODERATE(Color(0xFFF97316)),   // 35–69% (Flux Orange)
    WEAK(Color(0xFFEF4444)),       // 1–34%
    UNAVAILABLE(Color(0xFF94A3B8)) // Offline / Stale / Unknown
}

/**
 * Smart Switch Operating Modes
 */
enum class SmartSwitchMode(val label: String, val description: String) {
    AUTOMATIC(
        "Automatic",
        "Flux automatically routes connections via Bluetooth when nearby, ESP32 Wi-Fi for direct device telemetry, and phone network for cloud access."
    ),
    MANUAL(
        "Manual",
        "You control your preferred connection link. Flux respects your selection without unexpected auto-switching."
    )
}

/**
 * Transport preference when in Manual Smart Switch mode
 */
enum class ManualTransportPreference(val label: String) {
    BLUETOOTH("Bluetooth"),
    WIFI("Wi-Fi")
}

/**
 * Production-ready immutable state representing dynamic connection status and signal strength.
 */
data class FluxConnectivityState(
    val type: ConnectionType = ConnectionType.NONE,
    val deviceName: String = "",
    val title: String = "Device offline",
    val statusText: String = "Offline",
    val signalPercent: Int? = null,
    val signalDbm: Int? = null,
    val signalLevel: SignalLevel = SignalLevel.UNAVAILABLE,
    val isOnline: Boolean = false,
    val isCloudReachable: Boolean = false,
    val isStale: Boolean = false,
    val lastTelemetryTimestamp: Long = 0L,
    val switchMode: SmartSwitchMode = SmartSwitchMode.AUTOMATIC,
    val manualPreference: ManualTransportPreference = ManualTransportPreference.BLUETOOTH,
    val espWifiConnected: Boolean = false,
    val bleConnected: Boolean = false,
    val phoneWifiAvailable: Boolean = false,
    val phoneCellularAvailable: Boolean = false
)

/**
 * Signal strength calibrator mapping radio dBm to normalized display percentages.
 */
object SignalCalibrator {
    /**
     * Wi-Fi RSSI Calibration:
     * -50 dBm or higher = 100% (Strong)
     * -90 dBm or lower = 1% (Weak)
     * Formula: ((rssi - (-90)) / 40.0) * 100
     * Example: -57 dBm -> (( -57 + 90 ) / 40.0) * 100 = 82.5% -> 82%
     */
    fun rssiToWifiPercent(rssi: Int): Int {
        if (rssi >= -50) return 100
        if (rssi <= -89) return 1
        return kotlin.math.round((((rssi - (-89)).toFloat() / (-50 - (-89)).toFloat()) * 100)).toInt().coerceIn(1, 100)
    }

    /**
     * BLE RSSI Calibration:
     * -40 dBm or higher = 100% (Strong)
     * -95 dBm or lower = 1% (Weak)
     * Formula: round(((rssi - (-95)) / 55.0) * 100)
     * Example: -44 dBm -> round(51.0 / 55.0 * 100) = 93%
     */
    fun rssiToBlePercent(rssi: Int): Int {
        if (rssi >= -40) return 100
        if (rssi <= -95) return 1
        return kotlin.math.round((((rssi - (-95)).toFloat() / (-40 - (-95)).toFloat()) * 100)).toInt().coerceIn(1, 100)
    }

    /**
     * Cellular RSSI Calibration:
     * -65 dBm or higher = 100%
     * -115 dBm or lower = 1%
     * Example: -86 dBm -> round((29.0 / 50.0) * 100) = 58%
     */
    fun rssiToCellularPercent(rssi: Int): Int {
        if (rssi >= -65) return 100
        if (rssi <= -115) return 1
        return kotlin.math.round((((rssi - (-115)).toFloat() / (-65 - (-115)).toFloat()) * 100)).toInt().coerceIn(1, 100)
    }

    /**
     * Hotspot RSSI Calibration:
     * -50 dBm or higher = 100%
     * -90 dBm or lower = 1%
     * Example: -84 dBm -> round((6.0 / 40.0) * 100) = 15%
     */
    fun rssiToHotspotPercent(rssi: Int): Int {
        if (rssi >= -50) return 100
        if (rssi <= -90) return 1
        return kotlin.math.round((((rssi - (-90)).toFloat() / (-50 - (-90)).toFloat()) * 100)).toInt().coerceIn(1, 100)
    }

    /**
     * Evaluate normalized percentage against UI display thresholds.
     */
    fun evaluateSignalLevel(percent: Int?): SignalLevel {
        return when {
            percent == null -> SignalLevel.UNAVAILABLE
            percent >= 70 -> SignalLevel.STRONG
            percent >= 35 -> SignalLevel.MODERATE
            percent > 0 -> SignalLevel.WEAK
            else -> SignalLevel.UNAVAILABLE
        }
    }
}
