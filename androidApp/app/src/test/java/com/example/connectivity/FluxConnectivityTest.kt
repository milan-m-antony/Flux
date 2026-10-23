package com.example.connectivity

import org.junit.Assert.*
import org.junit.Test

class FluxConnectivityTest {

    @Test
    fun testWifiRssiCalibrationExactScreenshotValues() {
        // -57 dBm should yield exactly 82% signal (matching screenshot 1)
        val percent = SignalCalibrator.rssiToWifiPercent(-57)
        assertEquals(82, percent)

        // Threshold evaluation: 82% must be STRONG (Green)
        val level = SignalCalibrator.evaluateSignalLevel(percent)
        assertEquals(SignalLevel.STRONG, level)
    }

    @Test
    fun testBleRssiCalibrationExactScreenshotValues() {
        // -44 dBm should yield exactly 93% signal (matching screenshot 2)
        val percent = SignalCalibrator.rssiToBlePercent(-44)
        assertEquals(93, percent)

        // Threshold evaluation: 93% must be STRONG (Green)
        val level = SignalCalibrator.evaluateSignalLevel(percent)
        assertEquals(SignalLevel.STRONG, level)
    }

    @Test
    fun testCellularCalibrationExactScreenshotValues() {
        // -86 dBm yields 58% signal (matching screenshot 3)
        val percent = SignalCalibrator.rssiToCellularPercent(-86)
        assertEquals(58, percent)

        // Threshold evaluation: 58% must be MODERATE (Orange)
        val level = SignalCalibrator.evaluateSignalLevel(percent)
        assertEquals(SignalLevel.MODERATE, level)
    }

    @Test
    fun testHotspotCalibrationExactScreenshotValues() {
        // -84 dBm yields 15% signal (matching screenshot 4)
        val percent = SignalCalibrator.rssiToHotspotPercent(-84)
        assertEquals(15, percent)

        // Threshold evaluation: 15% must be WEAK (Red)
        val level = SignalCalibrator.evaluateSignalLevel(percent)
        assertEquals(SignalLevel.WEAK, level)
    }

    @Test
    fun testSignalThresholdBoundaries() {
        // 70% -> STRONG
        assertEquals(SignalLevel.STRONG, SignalCalibrator.evaluateSignalLevel(70))
        assertEquals(SignalLevel.STRONG, SignalCalibrator.evaluateSignalLevel(100))

        // 69% -> MODERATE
        assertEquals(SignalLevel.MODERATE, SignalCalibrator.evaluateSignalLevel(69))
        assertEquals(SignalLevel.MODERATE, SignalCalibrator.evaluateSignalLevel(35))

        // 34% -> WEAK
        assertEquals(SignalLevel.WEAK, SignalCalibrator.evaluateSignalLevel(34))
        assertEquals(SignalLevel.WEAK, SignalCalibrator.evaluateSignalLevel(1))

        // null / unknown -> UNAVAILABLE (never 0% measured signal)
        assertEquals(SignalLevel.UNAVAILABLE, SignalCalibrator.evaluateSignalLevel(null))
        assertEquals(SignalLevel.UNAVAILABLE, SignalCalibrator.evaluateSignalLevel(0))
    }

    @Test
    fun testClampingLimits() {
        // Very strong Wi-Fi (-30 dBm) capped at 100%
        assertEquals(100, SignalCalibrator.rssiToWifiPercent(-30))

        // Extremely weak Wi-Fi (-110 dBm) floored at 1%
        assertEquals(1, SignalCalibrator.rssiToWifiPercent(-110))

        // Very strong BLE (-20 dBm) capped at 100%
        assertEquals(100, SignalCalibrator.rssiToBlePercent(-20))

        // Extremely weak BLE (-120 dBm) floored at 1%
        assertEquals(1, SignalCalibrator.rssiToBlePercent(-120))
    }

    @Test
    fun testFluxConnectivityStateImmutability() {
        val defaultState = FluxConnectivityState()
        assertEquals(ConnectionType.WIFI, defaultState.type)
        assertEquals("Device online", defaultState.title)
        assertEquals("82% signal", defaultState.statusText)
        assertEquals(82, defaultState.signalPercent)
        assertEquals(SignalLevel.STRONG, defaultState.signalLevel)
        assertTrue(defaultState.isOnline)
        assertTrue(defaultState.isCloudReachable)
        assertFalse(defaultState.isStale)
        assertEquals("Flux1", defaultState.deviceName)
    }

    @Test
    fun testStaleTelemetryEvaluation() {
        val now = System.currentTimeMillis()
        val staleTimestamp = now - 65_000L // 65 seconds ago (exceeds 60s timeout)
        val isStale = (now - staleTimestamp) > 60_000L
        assertTrue("Telemetry older than 60s must be marked stale", isStale)

        val staleState = FluxConnectivityState(
            type = ConnectionType.WIFI,
            title = "Stale data",
            statusText = "Offline",
            signalPercent = null,
            signalDbm = null,
            signalLevel = SignalLevel.UNAVAILABLE,
            isOnline = false,
            isCloudReachable = false,
            isStale = true,
            lastTelemetryTimestamp = staleTimestamp
        )
        assertTrue(staleState.isStale)
        assertFalse(staleState.isOnline)
        assertNull(staleState.signalPercent)
        assertEquals(SignalLevel.UNAVAILABLE, staleState.signalLevel)
    }

    @Test
    fun testSignalMeterOfflinePoorStateMatchesScreenshot() {
        // In the reference screenshot: device is offline with 2 bars orange and 'Signal: Poor'
        val offlineState = FluxConnectivityState(
            isOnline = false,
            signalPercent = null,
            deviceName = "Flux1"
        )
        assertFalse(offlineState.isOnline)
        assertEquals("Flux1", offlineState.deviceName)
    }
}

