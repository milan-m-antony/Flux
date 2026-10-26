package com.example.connectivity

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import com.example.ble.BleManager
import com.example.ble.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Domain-level coordinator managing automatic connection switching, signal strength calibration,
 * telemetry freshness tracking, and lifecycle-aware network state monitoring.
 */
@SuppressLint("MissingPermission")
class FluxConnectivityCoordinator(
    private val context: Context,
    private val scope: CoroutineScope,
    private val bleManager: BleManager,
    private val espWifiConnected: StateFlow<Boolean>,
    private val espWifiRssi: StateFlow<Int?>,
    private val lastTelemetryTimestamp: StateFlow<Long>,
    private val cloudSyncStatus: StateFlow<String>,
    private val smartSwitchMode: StateFlow<String> = MutableStateFlow("automatic"),
    private val manualPreferredTransport: StateFlow<String> = MutableStateFlow("bluetooth")
) {
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    // Internal state tracking Android phone network transport
    private val _phoneHasWifi = MutableStateFlow(false)
    private val _phoneHasCellular = MutableStateFlow(false)
    private val _phoneIsMetered = MutableStateFlow(false)
    private val _phoneNetworkSignalDbm = MutableStateFlow<Int?>(null)
    private val _isInternetValidated = MutableStateFlow(false)
    private val _phoneWifiSsid = MutableStateFlow<String?>(null)

    // Primary output StateFlow
    private val _connectivityState = MutableStateFlow(FluxConnectivityState())
    val connectivityState: StateFlow<FluxConnectivityState> = _connectivityState.asStateFlow()
    val phoneWifiSsid: StateFlow<String?> = _phoneWifiSsid.asStateFlow()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var periodicFreshnessJob: Job? = null

    init {
        registerNetworkCallback()
        startFreshnessMonitor()
        observeInputs()
    }

    private fun registerNetworkCallback() {
        if (cm == null) return
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
                override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                    handleNetworkCapabilities(capabilities)
                }

                override fun onLost(network: Network) {
                    evaluateActiveNetwork()
                }
            }
        } else {
            object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                    handleNetworkCapabilities(capabilities)
                }

                override fun onLost(network: Network) {
                    evaluateActiveNetwork()
                }
            }
        }

        try {
            cm.registerNetworkCallback(request, networkCallback!!)
            evaluateActiveNetwork()
        } catch (e: Exception) {
            Log.w("ConnectivityCoord", "Could not register network callback: ${e.message}")
        }
    }

    private fun handleNetworkCapabilities(capabilities: NetworkCapabilities) {
        val hasWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val hasCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        val isMetered = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        val validated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        var signalDbm: Int? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val strength = capabilities.signalStrength
            if (strength != NetworkCapabilities.SIGNAL_STRENGTH_UNSPECIFIED) {
                signalDbm = strength
            }
        }

        _phoneHasWifi.value = hasWifi
        _phoneHasCellular.value = hasCellular
        _phoneIsMetered.value = isMetered
        _isInternetValidated.value = validated
        if (signalDbm != null) {
            _phoneNetworkSignalDbm.value = signalDbm
        }

        if (hasWifi) {
            updatePhoneWifiSsid(capabilities)
        } else {
            _phoneWifiSsid.value = null
        }
    }

    private fun updatePhoneWifiSsid(capabilities: NetworkCapabilities?) {
        try {
            if (capabilities != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val wifiInfo = capabilities.transportInfo as? android.net.wifi.WifiInfo
                val name = wifiInfo?.ssid
                if (!name.isNullOrBlank() && name != "<unknown ssid>" && name != "0x" && name != "\"\"") {
                    _phoneWifiSsid.value = name.removeSurrounding("\"")
                    return
                }
            }
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? android.net.wifi.WifiManager
            val raw = wifiManager?.connectionInfo?.ssid
            if (!raw.isNullOrBlank() && raw != "<unknown ssid>" && raw != "0x" && raw != "\"\"") {
                _phoneWifiSsid.value = raw.removeSurrounding("\"")
            }
        } catch (_: Exception) {}
    }

    private fun evaluateActiveNetwork() {
        if (cm == null) return
        val activeNetwork = cm.activeNetwork
        val capabilities = cm.getNetworkCapabilities(activeNetwork)
        if (capabilities != null) {
            val hasWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            _phoneHasWifi.value = hasWifi
            _phoneHasCellular.value = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            _phoneIsMetered.value = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            _isInternetValidated.value = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            if (hasWifi) {
                updatePhoneWifiSsid(capabilities)
            } else {
                _phoneWifiSsid.value = null
            }
        } else {
            _phoneHasWifi.value = false
            _phoneHasCellular.value = false
            _phoneIsMetered.value = false
            _isInternetValidated.value = false
            _phoneWifiSsid.value = null
        }
    }

    /**
     * Periodic background check ensuring stale telemetry (> 60s without update)
     * transitions to the appropriate stale/offline state without requiring external events.
     */
    private fun startFreshnessMonitor() {
        periodicFreshnessJob = scope.launch(Dispatchers.Default) {
            while (true) {
                delay(5000L)
                recalculateConnectivity()
            }
        }
    }

    private fun observeInputs() {
        scope.launch {
            combine(
                bleManager.connectionState,
                bleManager.rssi,
                espWifiConnected,
                espWifiRssi,
                lastTelemetryTimestamp,
                cloudSyncStatus,
                _phoneHasWifi,
                _phoneHasCellular,
                _phoneIsMetered,
                smartSwitchMode,
                manualPreferredTransport
            ) { _ ->
                // Recalculate whenever any dependency updates
                recalculateConnectivity()
            }.collect()
        }
    }

    /**
     * Smart Switch Connectivity Logic:
     * - Automatic Mode:
     *   1. BLE setup or active local BLE session -> Bluetooth (Nearby connection)
     *   2. Registered ESP32 device online through Wi-Fi -> Wi-Fi (Device online)
     *   3. Hotspot detected -> Hotspot
     *   4. Phone mobile data -> Mobile data (Internet available)
     *   5. ESP32 unreachable or telemetry stale -> Device offline / Stale data
     * - Manual Mode:
     *   Strictly respects user-preferred transport (Bluetooth or Wi-Fi) without unwanted auto-switching.
     */
    fun recalculateConnectivity() {
        val now = System.currentTimeMillis()
        val lastTelemetry = lastTelemetryTimestamp.value
        val isStale = (lastTelemetry > 0 && (now - lastTelemetry) > 60_000L)
        val bleState = bleManager.connectionState.value
        val isBleConnected = bleState == ConnectionState.CONNECTED
        val isBleScanning = bleState == ConnectionState.SCANNING || bleState == ConnectionState.CONNECTING
        val bleRssiVal = bleManager.rssi.value

        val isEspWifiOnline = espWifiConnected.value
        val espWifiRssiVal = espWifiRssi.value

        val cloudSync = cloudSyncStatus.value
        val isCloudReachable = cloudSync == "Synced" || cloudSync == "Ready"

        val hasCellular = _phoneHasCellular.value
        val hasWifi = _phoneHasWifi.value
        val isMeteredWifi = hasWifi && _phoneIsMetered.value

        val mode = if (smartSwitchMode.value.equals("manual", ignoreCase = true)) {
            SmartSwitchMode.MANUAL
        } else {
            SmartSwitchMode.AUTOMATIC
        }

        val manualPref = if (manualPreferredTransport.value.equals("wifi", ignoreCase = true)) {
            ManualTransportPreference.WIFI
        } else {
            ManualTransportPreference.BLUETOOTH
        }

        val newState = if (mode == SmartSwitchMode.MANUAL) {
            when (manualPref) {
                ManualTransportPreference.BLUETOOTH -> {
                    when {
                        isBleConnected -> {
                            val effectiveDbm = bleRssiVal ?: -55
                            val percent = SignalCalibrator.rssiToBlePercent(effectiveDbm)
                            val liveName = bleManager.latestReading.value?.deviceName?.ifBlank {
                                bleManager.deviceName.value.ifBlank { "Flux1" }
                            } ?: bleManager.deviceName.value.ifBlank { "Flux1" }
                            FluxConnectivityState(
                                type = ConnectionType.BLUETOOTH,
                                deviceName = liveName,
                                title = "Nearby connection",
                                statusText = "$percent% signal",
                                signalPercent = percent,
                                signalDbm = effectiveDbm,
                                signalLevel = SignalCalibrator.evaluateSignalLevel(percent),
                                isOnline = true,
                                isCloudReachable = isCloudReachable,
                                isStale = false,
                                lastTelemetryTimestamp = lastTelemetry,
                                switchMode = mode,
                                manualPreference = manualPref,
                                espWifiConnected = isEspWifiOnline,
                                bleConnected = true,
                                phoneWifiAvailable = hasWifi,
                                phoneCellularAvailable = hasCellular
                            )
                        }
                        isBleScanning -> {
                            FluxConnectivityState(
                                type = ConnectionType.BLUETOOTH,
                                deviceName = "Searching...",
                                title = "Connecting...",
                                statusText = "Searching",
                                signalPercent = null,
                                signalDbm = null,
                                signalLevel = SignalLevel.UNAVAILABLE,
                                isOnline = false,
                                isCloudReachable = isCloudReachable,
                                isStale = false,
                                lastTelemetryTimestamp = lastTelemetry,
                                switchMode = mode,
                                manualPreference = manualPref,
                                espWifiConnected = isEspWifiOnline,
                                bleConnected = false,
                                phoneWifiAvailable = hasWifi,
                                phoneCellularAvailable = hasCellular
                            )
                        }
                        else -> {
                            FluxConnectivityState(
                                type = ConnectionType.BLUETOOTH,
                                deviceName = "Flux1",
                                title = "Bluetooth disconnected",
                                statusText = "Manual mode: BLE",
                                signalPercent = null,
                                signalDbm = null,
                                signalLevel = SignalLevel.UNAVAILABLE,
                                isOnline = false,
                                isCloudReachable = isCloudReachable,
                                isStale = false,
                                lastTelemetryTimestamp = lastTelemetry,
                                switchMode = mode,
                                manualPreference = manualPref,
                                espWifiConnected = isEspWifiOnline,
                                bleConnected = false,
                                phoneWifiAvailable = hasWifi,
                                phoneCellularAvailable = hasCellular
                            )
                        }
                    }
                }
                ManualTransportPreference.WIFI -> {
                    if (isEspWifiOnline && lastTelemetry > 0 && !isStale) {
                        val effectiveDbm = espWifiRssiVal ?: -57
                        val percent = SignalCalibrator.rssiToWifiPercent(effectiveDbm)
                        FluxConnectivityState(
                            type = ConnectionType.WIFI,
                            deviceName = "Flux1",
                            title = "Device online",
                            statusText = "$percent% signal",
                            signalPercent = percent,
                            signalDbm = effectiveDbm,
                            signalLevel = SignalCalibrator.evaluateSignalLevel(percent),
                            isOnline = true,
                            isCloudReachable = isCloudReachable,
                            isStale = false,
                            lastTelemetryTimestamp = lastTelemetry,
                            switchMode = mode,
                            manualPreference = manualPref,
                            espWifiConnected = true,
                            bleConnected = isBleConnected,
                            phoneWifiAvailable = hasWifi,
                            phoneCellularAvailable = hasCellular
                        )
                    } else {
                        FluxConnectivityState(
                            type = ConnectionType.WIFI,
                            deviceName = "Flux1",
                            title = if (isStale) "Stale data" else "Device Wi-Fi offline",
                            statusText = "Manual mode: Wi-Fi",
                            signalPercent = null,
                            signalDbm = null,
                            signalLevel = SignalLevel.UNAVAILABLE,
                            isOnline = false,
                            isCloudReachable = isCloudReachable,
                            isStale = isStale,
                            lastTelemetryTimestamp = lastTelemetry,
                            switchMode = mode,
                            manualPreference = manualPref,
                            espWifiConnected = isEspWifiOnline,
                            bleConnected = isBleConnected,
                            phoneWifiAvailable = hasWifi,
                            phoneCellularAvailable = hasCellular
                        )
                    }
                }
            }
        } else {
            // AUTOMATIC MODE
            when {
                // Priority 1: BLE setup or active local BLE session
                isBleConnected -> {
                    val effectiveDbm = bleRssiVal ?: -55
                    val percent = SignalCalibrator.rssiToBlePercent(effectiveDbm)
                    val liveName = bleManager.latestReading.value?.deviceName?.ifBlank {
                        bleManager.deviceName.value.ifBlank { "Flux1" }
                    } ?: bleManager.deviceName.value.ifBlank { "Flux1" }
                    FluxConnectivityState(
                        type = ConnectionType.BLUETOOTH,
                        deviceName = liveName,
                        title = "Nearby connection",
                        statusText = "$percent% signal",
                        signalPercent = percent,
                        signalDbm = effectiveDbm,
                        signalLevel = SignalCalibrator.evaluateSignalLevel(percent),
                        isOnline = true,
                        isCloudReachable = isCloudReachable,
                        isStale = false,
                        lastTelemetryTimestamp = lastTelemetry,
                        switchMode = mode,
                        manualPreference = manualPref,
                        espWifiConnected = isEspWifiOnline,
                        bleConnected = true,
                        phoneWifiAvailable = hasWifi,
                        phoneCellularAvailable = hasCellular
                    )
                }

                isBleScanning -> {
                    FluxConnectivityState(
                        type = ConnectionType.BLUETOOTH,
                        deviceName = "Searching...",
                        title = "Connecting...",
                        statusText = "Searching",
                        signalPercent = null,
                        signalDbm = null,
                        signalLevel = SignalLevel.UNAVAILABLE,
                        isOnline = false,
                        isCloudReachable = isCloudReachable,
                        isStale = false,
                        lastTelemetryTimestamp = lastTelemetry,
                        switchMode = mode,
                        manualPreference = manualPref,
                        espWifiConnected = isEspWifiOnline,
                        bleConnected = false,
                        phoneWifiAvailable = hasWifi,
                        phoneCellularAvailable = hasCellular
                    )
                }

                // Priority 2: Registered ESP32 device online through Wi-Fi (fresh telemetry received within 60s)
                isEspWifiOnline && lastTelemetry > 0 && !isStale -> {
                    val effectiveDbm = espWifiRssiVal ?: -57
                    val percent = SignalCalibrator.rssiToWifiPercent(effectiveDbm)
                    FluxConnectivityState(
                        type = ConnectionType.WIFI,
                        deviceName = "Flux1",
                        title = "Device online",
                        statusText = "$percent% signal",
                        signalPercent = percent,
                        signalDbm = effectiveDbm,
                        signalLevel = SignalCalibrator.evaluateSignalLevel(percent),
                        isOnline = true,
                        isCloudReachable = isCloudReachable,
                        isStale = false,
                        lastTelemetryTimestamp = lastTelemetry,
                        switchMode = mode,
                        manualPreference = manualPref,
                        espWifiConnected = true,
                        bleConnected = isBleConnected,
                        phoneWifiAvailable = hasWifi,
                        phoneCellularAvailable = hasCellular
                    )
                }

                // Priority 5: Personal Hotspot detected on phone with active telemetry
                isMeteredWifi && lastTelemetry > 0 && !isStale -> {
                    val effectiveDbm = _phoneNetworkSignalDbm.value ?: -84
                    val percent = SignalCalibrator.rssiToHotspotPercent(effectiveDbm)
                    FluxConnectivityState(
                        type = ConnectionType.HOTSPOT,
                        deviceName = "Flux1",
                        title = "Device online",
                        statusText = "$percent% signal",
                        signalPercent = percent,
                        signalDbm = effectiveDbm,
                        signalLevel = SignalCalibrator.evaluateSignalLevel(percent),
                        isOnline = true,
                        isCloudReachable = isCloudReachable,
                        isStale = false,
                        lastTelemetryTimestamp = lastTelemetry,
                        switchMode = mode,
                        manualPreference = manualPref,
                        espWifiConnected = isEspWifiOnline,
                        bleConnected = isBleConnected,
                        phoneWifiAvailable = hasWifi,
                        phoneCellularAvailable = hasCellular
                    )
                }

                // Priority 4: Phone mobile data active fallback with active telemetry
                hasCellular && !hasWifi && lastTelemetry > 0 && !isStale -> {
                    val effectiveDbm = _phoneNetworkSignalDbm.value ?: -86
                    val percent = SignalCalibrator.rssiToCellularPercent(effectiveDbm)
                    FluxConnectivityState(
                        type = ConnectionType.MOBILE_DATA,
                        deviceName = "Flux1",
                        title = "Cloud telemetry",
                        statusText = "$percent% signal",
                        signalPercent = percent,
                        signalDbm = effectiveDbm,
                        signalLevel = SignalCalibrator.evaluateSignalLevel(percent),
                        isOnline = true,
                        isCloudReachable = isCloudReachable,
                        isStale = false,
                        lastTelemetryTimestamp = lastTelemetry,
                        switchMode = mode,
                        manualPreference = manualPref,
                        espWifiConnected = isEspWifiOnline,
                        bleConnected = isBleConnected,
                        phoneWifiAvailable = hasWifi,
                        phoneCellularAvailable = hasCellular
                    )
                }

                // Telemetry stale
                isStale && lastTelemetry > 0 -> {
                    FluxConnectivityState(
                        type = ConnectionType.NONE,
                        deviceName = "Flux1",
                        title = "Stale data",
                        statusText = "Offline",
                        signalPercent = null,
                        signalDbm = null,
                        signalLevel = SignalLevel.UNAVAILABLE,
                        isOnline = false,
                        isCloudReachable = false,
                        isStale = true,
                        lastTelemetryTimestamp = lastTelemetry,
                        switchMode = mode,
                        manualPreference = manualPref,
                        espWifiConnected = isEspWifiOnline,
                        bleConnected = isBleConnected,
                        phoneWifiAvailable = hasWifi,
                        phoneCellularAvailable = hasCellular
                    )
                }

                else -> {
                    FluxConnectivityState(
                        type = ConnectionType.NONE,
                        deviceName = "",
                        title = "Device offline",
                        statusText = "Offline",
                        signalPercent = null,
                        signalDbm = null,
                        signalLevel = SignalLevel.UNAVAILABLE,
                        isOnline = false,
                        isCloudReachable = false,
                        isStale = false,
                        lastTelemetryTimestamp = 0L,
                        switchMode = mode,
                        manualPreference = manualPref,
                        espWifiConnected = isEspWifiOnline,
                        bleConnected = isBleConnected,
                        phoneWifiAvailable = hasWifi,
                        phoneCellularAvailable = hasCellular
                    )
                }
            }
        }

        _connectivityState.value = newState
    }

    fun cleanup() {
        periodicFreshnessJob?.cancel()
        networkCallback?.let {
            try {
                cm?.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
