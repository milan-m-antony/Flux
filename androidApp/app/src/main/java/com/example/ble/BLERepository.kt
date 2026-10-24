package com.example.ble

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BLERepository private constructor(context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _rawPayload = MutableStateFlow<String>("No data received")
    val rawPayload: StateFlow<String> = _rawPayload.asStateFlow()

    private val connectionManager = BLEConnectionManager(context, onDataReceived = { payload ->
        _rawPayload.value = payload
        val data = BLEParser.parse(payload)
        if (data != null) {
            _bleStateData.value = data
        }
    }, onTemperatureReceived = { temp ->
        _liveTemperature.value = temp
        _lastTemperatureUpdateTime.value = System.currentTimeMillis()
    })

    private val _bleStateData = MutableStateFlow<BLEStateData?>(null)
    val bleStateData: StateFlow<BLEStateData?> = _bleStateData.asStateFlow()

    init {
        scope.launch {
            connectionManager.connectionState.collect { state ->
                if (state == BLEState.DISCONNECTED || state == BLEState.FAILED) {
                    _bleStateData.value = null
                    _liveTemperature.value = null
                    _lastTemperatureUpdateTime.value = null
                }
            }
        }
    }

    private val _liveTemperature = MutableStateFlow<Float?>(null)
    val liveTemperature: StateFlow<Float?> = _liveTemperature.asStateFlow()

    private val _lastTemperatureUpdateTime = MutableStateFlow<Long?>(null)
    val lastTemperatureUpdateTime: StateFlow<Long?> = _lastTemperatureUpdateTime.asStateFlow()

    val connectionState: StateFlow<BLEState> = connectionManager.connectionState
    val deviceName: StateFlow<String> = connectionManager.deviceName
    val rssi: StateFlow<Int?> = connectionManager.rssi

    fun connect(address: String) {
        connectionManager.connect(address)
    }

    fun disconnect() {
        connectionManager.disconnect()
        _bleStateData.value = null
        _liveTemperature.value = null
        _lastTemperatureUpdateTime.value = null
    }

    fun autoReconnectIfPossible() {
        connectionManager.autoReconnectIfPossible()
    }

    fun sendCommand(command: String): Boolean {
        return connectionManager.sendCommand(command)
    }

    companion object {
        @Volatile
        private var INSTANCE: BLERepository? = null

        fun getInstance(context: Context): BLERepository {
            return INSTANCE ?: synchronized(this) {
                val instance = BLERepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
