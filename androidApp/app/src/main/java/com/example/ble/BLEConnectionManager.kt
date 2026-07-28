package com.example.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class BLEState {
    DISCONNECTED,
    SCANNING,
    CONNECTING,
    CONNECTED,
    FAILED,
    RECONNECTING
}

@SuppressLint("MissingPermission")
class BLEConnectionManager(
    private val context: Context,
    private val onDataReceived: (String) -> Unit,
    private val onTemperatureReceived: (Float) -> Unit = {}
) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter
    private var gatt: BluetoothGatt? = null
    private val handler = Handler(Looper.getMainLooper())

    private val _connectionState = MutableStateFlow(BLEState.DISCONNECTED)
    val connectionState: StateFlow<BLEState> = _connectionState.asStateFlow()

    private val _deviceName = MutableStateFlow("")
    val deviceName: StateFlow<String> = _deviceName.asStateFlow()

    private val _rssi = MutableStateFlow<Int?>(null)
    val rssi: StateFlow<Int?> = _rssi.asStateFlow()

    private val sharedPrefs = context.getSharedPreferences("NaTrackBLEPrefs", Context.MODE_PRIVATE)

    private val SERVICE_UUID = UUID.fromString("6E400011-B5A3-F393-E0A9-E50E24DCCA9E")
    private val JSON_CHAR_UUID = UUID.fromString("6E400009-B5A3-F393-E0A9-E50E24DCCA9E")
    private val TEMP_CHAR_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
    private val CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private var lastAddress: String? = null
    private var isAutoReconnecting = false
    private var discoveryStarted = false

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value = BLEState.FAILED
                disconnect()
                retryOrScheduleReconnect()
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                _connectionState.value = BLEState.CONNECTED
                _deviceName.value = gatt.device.name ?: "NaTrack"
                discoveryStarted = false
                // Save last successful connected device
                sharedPrefs.edit().putString("last_address", gatt.device.address).apply()
                
                // CRITICAL FIX: Clear Android's aggressive GATT cache so it sees the newly flashed JSON characteristic!
                // Without this, Android will reuse the old cached GATT table from before you flashed the new ESP32 code.
                try {
                    val refreshMethod = gatt.javaClass.getMethod("refresh")
                    refreshMethod.invoke(gatt)
                } catch (e: Exception) {
                    // Ignore, reflection might fail on some OEMs but usually works
                }
                
                // Bluetooth operations must often be on the main thread to avoid silent failures
                handler.post {
                    val mtuRequested = gatt.requestMtu(256)
                    if (!mtuRequested) {
                        // If MTU request fails or is unsupported, discover services immediately
                        discoveryStarted = true
                        gatt.discoverServices()
                    } else {
                        // Fallback: If onMtuChanged is swallowed by the OS, force discovery after 1.5s
                        handler.postDelayed({
                            if (!discoveryStarted) {
                                discoveryStarted = true
                                gatt.discoverServices()
                            }
                        }, 1500)
                    }
                }
                
                readRssiRepeatedly()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _connectionState.value = BLEState.DISCONNECTED
                this@BLEConnectionManager.gatt = null
                retryOrScheduleReconnect()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(SERVICE_UUID)
                if (service == null) {
                    onDataReceived("Error: Service not found in GATT table")
                    return
                }
                
                val jsonChar = service.getCharacteristic(JSON_CHAR_UUID)
                val tempChar = service.getCharacteristic(TEMP_CHAR_UUID)
                
                if (jsonChar != null) {
                    gatt.setCharacteristicNotification(jsonChar, true)
                    val descriptor = jsonChar.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)
                    if (descriptor != null) {
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(descriptor)
                    }
                }

                // Delay the second descriptor write to avoid GATT BUSY errors
                handler.postDelayed({
                    if (tempChar != null) {
                        gatt.setCharacteristicNotification(tempChar, true)
                        val descriptor = tempChar.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)
                        if (descriptor != null) {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                        }
                    }
                    onDataReceived("Subscribed. Waiting for ESP32...")
                }, 500)
            } else {
                onDataReceived("Error: Service discovery failed with status $status")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val data = characteristic.value ?: return
            if (characteristic.uuid == JSON_CHAR_UUID) {
                val payloadString = String(data, Charsets.UTF_8).trim('\u0000', ' ', '\n', '\r')
                onDataReceived(payloadString)
            } else if (characteristic.uuid == TEMP_CHAR_UUID) {
                if (data.size == 4) {
                    val temp = java.nio.ByteBuffer.wrap(data).order(java.nio.ByteOrder.LITTLE_ENDIAN).float
                    onTemperatureReceived(temp)
                }
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _rssi.value = rssi
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            handler.post {
                // MTU negotiation completed, now we can safely discover services
                if (!discoveryStarted) {
                    discoveryStarted = true
                    gatt.discoverServices()
                }
            }
        }
    }

    private fun readRssiRepeatedly() {
        if (_connectionState.value == BLEState.CONNECTED) {
            gatt?.readRemoteRssi()
            handler.postDelayed({ readRssiRepeatedly() }, 5000)
        }
    }

    fun connect(address: String) {
        disconnect()
        lastAddress = address
        _connectionState.value = BLEState.CONNECTING
        val device = adapter?.getRemoteDevice(address)
        if (device != null) {
            gatt = device.connectGatt(context, false, gattCallback)
        } else {
            _connectionState.value = BLEState.FAILED
        }
    }

    fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        _connectionState.value = BLEState.DISCONNECTED
        _deviceName.value = ""
        _rssi.value = null
    }

    private fun retryOrScheduleReconnect() {
        val savedAddr = sharedPrefs.getString("last_address", null) ?: lastAddress
        if (savedAddr != null && !isAutoReconnecting) {
            isAutoReconnecting = true
            _connectionState.value = BLEState.RECONNECTING
            handler.postDelayed({
                isAutoReconnecting = false
                connect(savedAddr)
            }, 5000)
        }
    }

    fun autoReconnectIfPossible() {
        val savedAddr = sharedPrefs.getString("last_address", null)
        if (savedAddr != null && _connectionState.value == BLEState.DISCONNECTED) {
            connect(savedAddr)
        }
    }
}
