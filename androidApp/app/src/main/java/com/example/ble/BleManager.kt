package com.example.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*

enum class ConnectionState {
    DISCONNECTED,
    SCANNING,
    CONNECTING,
    CONNECTED
}

data class DiscoveredDevice(
    val device: BluetoothDevice?,
    val name: String,
    val address: String,
    val rssi: Int
)

@SuppressLint("MissingPermission")
class BleManager(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val repository = BLERepository.getInstance(context)
    private val scanner = BLEScanner(context)

    // Bridge connection state
    val connectionState: StateFlow<ConnectionState> = repository.connectionState.map { state ->
        when (state) {
            BLEState.DISCONNECTED -> ConnectionState.DISCONNECTED
            BLEState.SCANNING -> ConnectionState.SCANNING
            BLEState.CONNECTING -> ConnectionState.CONNECTING
            BLEState.CONNECTED -> ConnectionState.CONNECTED
            BLEState.FAILED -> ConnectionState.DISCONNECTED
            BLEState.RECONNECTING -> ConnectionState.CONNECTING
        }
    }.stateIn(scope, SharingStarted.Eagerly, ConnectionState.DISCONNECTED)

    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = scanner.discoveredDevices
    val isScanning: StateFlow<Boolean> = scanner.isScanning

    val rawPayload: StateFlow<String> = repository.rawPayload

    // Bridge BleReading using BLEStateData from parser
    val latestReading: StateFlow<BleReading?> = repository.bleStateData.map { data ->
        if (data == null) {
            null
        } else {
            val naValue = try {
                if (data.sodium == "---") -1.0 else data.sodium.toDouble()
            } catch (e: Exception) {
                -1.0
            }
            BleReading(
                na = naValue,
                temp = data.temperature,
                mv = data.batteryVoltage * 1000.0,
                status = data.status,
                e0 = 150.0,
                slope = 59.2,
                battery = data.battery,
                batteryVoltage = data.batteryVoltage,
                charging = data.charging,
                firmware = data.firmware,
                deviceName = data.device,
                mode = data.mode,
                unit = data.unit,
                minTemp = data.minTemp,
                maxTemp = data.maxTemp,
                avgTemp = data.avgTemp,
                elapsedSec = data.elapsedSec,
                isAlert = data.alert,
                buzzer = data.buzzer,
                wifiConnected = data.wifiConnected,
                wifiRssi = data.wifiRssi,
                wifiSsid = data.wifiSsid
            )
        }
    }.stateIn(scope, SharingStarted.Eagerly, null)

    val rssi: StateFlow<Int?> = repository.rssi
    val deviceName: StateFlow<String> = repository.deviceName
    val latestMv: StateFlow<Double?> = latestReading.map { it?.mv }.stateIn(scope, SharingStarted.Eagerly, null)

    fun startScan() {
        scanner.startScan()
    }

    fun stopScan() {
        scanner.stopScan()
    }

    fun isBluetoothEnabled(): Boolean {
        return repository.isBluetoothEnabled()
    }

    fun connect(address: String) {
        if (!repository.isBluetoothEnabled()) return
        repository.connect(address)
    }

    fun disconnect() {
        repository.disconnect()
    }

    fun requestNewReading() {
        // Future UART request if needed
    }

    fun applyCalibration(e0: Double, slope: Double) {
        // Future calibration application if needed
    }

    fun autoReconnectIfPossible() {
        repository.autoReconnectIfPossible()
    }

    fun sendCommand(command: String): Boolean {
        return repository.sendCommand(command)
    }

    fun setBuzzerEnabled(enabled: Boolean): Boolean {
        return sendCommand(if (enabled) "SET_BUZZER:1" else "SET_BUZZER:0")
    }
}
