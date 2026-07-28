package com.example.ble

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TemperatureRecord(val temperature: Float, val timestamp: Long)

class BleTemperatureViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BLERepository.getInstance(application)

    // Expose connection states
    val connectionState: StateFlow<BLEState> = repository.connectionState
    val deviceName: StateFlow<String> = repository.deviceName
    val rssi: StateFlow<Int?> = repository.rssi

    // Expose live temperature
    val liveTemperature: StateFlow<Float?> = repository.liveTemperature
    val lastUpdateTime: StateFlow<Long?> = repository.lastTemperatureUpdateTime

    // History of last 20 readings
    private val _temperatureHistory = MutableStateFlow<List<TemperatureRecord>>(emptyList())
    val temperatureHistory: StateFlow<List<TemperatureRecord>> = _temperatureHistory.asStateFlow()

    init {
        viewModelScope.launch {
            repository.liveTemperature.collect { temp ->
                if (temp != null) {
                    val record = TemperatureRecord(temp, System.currentTimeMillis())
                    _temperatureHistory.value = (listOf(record) + _temperatureHistory.value).take(20)
                }
            }
        }
    }

    fun connect(address: String) {
        repository.connect(address)
    }

    fun disconnect() {
        repository.disconnect()
    }

    fun reconnect() {
        repository.autoReconnectIfPossible()
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BleTemperatureViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return BleTemperatureViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
