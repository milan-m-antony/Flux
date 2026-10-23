package com.example.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@SuppressLint("MissingPermission")
class BLEScanner(private val context: Context) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter
    private val scanner: BluetoothLeScanner? = adapter?.bluetoothLeScanner

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = device.name ?: result.scanRecord?.deviceName ?: "Unknown"
            
            // The ESP32 broadcasts this specific Service UUID
            val targetServiceUuid1 = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"
            val targetServiceUuid2 = "6E400011-B5A3-F393-E0A9-E50E24DCCA9E"
            val hasTargetService = result.scanRecord?.serviceUuids?.any { 
                val uuidStr = it.uuid.toString()
                uuidStr.equals(targetServiceUuid1, ignoreCase = true) || uuidStr.equals(targetServiceUuid2, ignoreCase = true)
            } == true
            
            if (name.contains("Flux", ignoreCase = true) || name.contains("NaTrack", ignoreCase = true) || hasTargetService) {
                val displayName = if (name == "Unknown") "Flux Telemetry" else name
                val newDevice = DiscoveredDevice(device, displayName, device.address, result.rssi)
                val currentList = _discoveredDevices.value.toMutableList()
                val existingIndex = currentList.indexOfFirst { it.address == device.address }
                if (existingIndex >= 0) {
                    currentList[existingIndex] = newDevice
                } else {
                    currentList.add(newDevice)
                }
                _discoveredDevices.value = currentList
            }
        }
    }

    fun startScan() {
        if (scanner == null || adapter?.isEnabled != true || _isScanning.value) return
        _discoveredDevices.value = emptyList()
        _isScanning.value = true
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        scanner.startScan(null, settings, scanCallback)
    }

    fun stopScan() {
        if (!_isScanning.value) return
        _isScanning.value = false
        try {
            scanner?.stopScan(scanCallback)
        } catch (e: Exception) {}
    }
}
