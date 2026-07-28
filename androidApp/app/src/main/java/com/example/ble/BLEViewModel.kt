package com.example.ble

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BLEViewModel(application: Application) : AndroidViewModel(application) {
    private val bleManager = BleManager(application)

    val connectionState: StateFlow<ConnectionState> = bleManager.connectionState
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = bleManager.discoveredDevices
    val latestReading: StateFlow<BleReading?> = bleManager.latestReading
    val latestMv: StateFlow<Double?> = bleManager.latestMv

    init {
        // Automatically try to reconnect to saved device when ViewModel is initialized
        viewModelScope.launch {
            bleManager.autoReconnectIfPossible()
        }
    }

    fun startScan() {
        bleManager.startScan()
    }

    fun stopScan() {
        bleManager.stopScan()
    }

    fun connect(address: String) {
        bleManager.connect(address)
    }

    fun disconnect() {
        bleManager.disconnect()
    }
}
