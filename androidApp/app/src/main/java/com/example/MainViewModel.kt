package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ble.BleManager
import com.example.ble.ConnectionState
import com.example.ble.DiscoveredDevice
import com.example.data.AppDatabase
import com.example.data.CalibrationHistory
import com.example.data.CalibrationRepository
import com.example.data.Reading
import com.example.data.ReadingRepository
import com.example.data.SettingsRepository
import com.example.data.dataStore
import com.example.data.NotificationEntity
import com.example.data.NotificationRepository
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import android.os.Bundle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.connectivity.FluxConnectivityCoordinator
import com.example.connectivity.FluxConnectivityState

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    val bleManager = BleManager(application)
    
    private val database = AppDatabase.getDatabase(application)
    private val readingRepository = ReadingRepository(database.readingDao())
    private val calibrationRepository = CalibrationRepository(database.calibrationDao())
    private val settingsRepository = SettingsRepository(application.dataStore)
    private val notificationRepository = NotificationRepository(database.notificationDao())

    private val firebaseAuth = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        null
    }

    private val supabaseRepository = com.example.data.SupabaseRepository(
        scope = viewModelScope,
        queuedTelemetryDao = database.queuedTelemetryDao(),
        firebaseAuth = firebaseAuth
    )

    private val firebaseAnalytics = try {
        FirebaseAnalytics.getInstance(application)
    } catch (e: Exception) {
        null
    }

    private val firebaseFirestore = try {
        FirebaseFirestore.getInstance().apply {
            firestoreSettings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        }
    } catch (e: Exception) {
        null
    }

    init {
        // Fetch RemoteConfig credentials
        com.example.data.RemoteConfigManager.fetchAndActivate { }
    }
    
    private val _currentUser = MutableStateFlow<FirebaseUser?>(firebaseAuth?.currentUser)
    val currentUser = _currentUser.asStateFlow()

    private fun logFirebaseEvent(name: String, bundle: Bundle? = null) {
        try {
            firebaseAnalytics?.logEvent(name, bundle)
        } catch (e: Exception) {
            // Silently ignore
        }
    }

    private fun syncToFirebase(path: String, value: Any) {
        val uid = firebaseAuth?.currentUser?.uid ?: "anonymous"
        viewModelScope.launch {
            try {
                firebaseFirestore?.collection("users")?.document(uid)?.set(mapOf(path to value), SetOptions.merge())?.addOnFailureListener { /* Handle failure */ }
            } catch (e: Exception) {
                // Silently ignore
            }
        }
    }

    val connectionState = bleManager.connectionState
    val discoveredDevices = bleManager.discoveredDevices
    val isScanning = bleManager.isScanning
    val latestReading = bleManager.latestReading
    val latestMv = bleManager.latestMv
    val rawPayload = bleManager.rawPayload
    
    val allNotifications = notificationRepository.allNotifications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val waterIntakeMl = settingsRepository.waterIntakeMl.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1250
    )

    val waterGoalMl = settingsRepository.waterGoalMl.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 2500
    )
    
    val allReadings = readingRepository.allReadings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList<Reading>()
    )
    
    val historyReadings = readingRepository.allReadings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList<Reading>()
    )
    
    val lastCalibration = calibrationRepository.lastCalibration.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    
    val preferredUnit = settingsRepository.preferredUnit.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "C")
    val activeProfile = settingsRepository.activeProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Live Monitor")
    val lowTempThreshold = settingsRepository.lowTempThreshold.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 18.0)
    val highTempThreshold = settingsRepository.highTempThreshold.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 28.0)
    val cloudUploadIntervalSec = settingsRepository.cloudUploadIntervalSec.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)
    val supabaseUrl = settingsRepository.supabaseUrl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.SettingsRepository.DEFAULT_SUPABASE_URL)
    val supabaseAnonKey = settingsRepository.supabaseAnonKey.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.SettingsRepository.DEFAULT_SUPABASE_ANON_KEY)
    val onesignalAppId = settingsRepository.onesignalAppId.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.SettingsRepository.DEFAULT_ONESIGNAL_APP_ID)
    val backendUrl = settingsRepository.backendUrl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.SettingsRepository.DEFAULT_BACKEND_URL)

    val supabaseSyncStatus = supabaseRepository.syncStatus
    val pendingOfflineRecords = supabaseRepository.pendingQueueCount
    val cloudHistoricalTelemetry = supabaseRepository.historicalTelemetry

    val lowAlertThreshold = lowTempThreshold
    val highAlertThreshold = highTempThreshold
    val isDarkTheme = settingsRepository.isDarkTheme.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    val userName = settingsRepository.userName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val userAge = settingsRepository.userAge.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val userWeight = settingsRepository.userWeight.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val userHeight = settingsRepository.userHeight.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val userPhotoUrl = settingsRepository.userPhotoUrl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val savedWifiSsid = settingsRepository.savedWifiSsid.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val savedWifiPass = settingsRepository.savedWifiPass.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val pushAlerts = settingsRepository.pushAlerts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val lastDeviceAddress: StateFlow<String?> = settingsRepository.lastDeviceAddress.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var wasInBreach = false
    private var lastBreachAlertTime = 0L
    private var wasSensorDisconnected = false
    private var lastSensorAlertTime = 0L
    private val BREACH_COOLDOWN_MS = 5 * 60 * 1000L // 5 minutes anti-flap cooldown
    private val HYSTERESIS_MARGIN = 0.3

    private val _lastTelemetryTimestamp = MutableStateFlow(0L)
    val lastTelemetryTimestamp: StateFlow<Long> = _lastTelemetryTimestamp.asStateFlow()

    private val _espWifiConnected = MutableStateFlow(false)
    val espWifiConnected: StateFlow<Boolean> = _espWifiConnected.asStateFlow()

    private val _espWifiRssi = MutableStateFlow<Int?>(null)
    val espWifiRssi: StateFlow<Int?> = _espWifiRssi.asStateFlow()

    val smartSwitchMode = settingsRepository.smartSwitchMode.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "automatic"
    )
    val manualPreferredTransport = settingsRepository.manualPreferredTransport.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "bluetooth"
    )

    val connectivityCoordinator = FluxConnectivityCoordinator(
        context = application,
        scope = viewModelScope,
        bleManager = bleManager,
        espWifiConnected = _espWifiConnected,
        espWifiRssi = _espWifiRssi,
        lastTelemetryTimestamp = _lastTelemetryTimestamp,
        cloudSyncStatus = supabaseSyncStatus,
        smartSwitchMode = smartSwitchMode,
        manualPreferredTransport = manualPreferredTransport
    )
    val connectivityState: StateFlow<FluxConnectivityState> = connectivityCoordinator.connectivityState
    val phoneWifiSsid: StateFlow<String?> = connectivityCoordinator.phoneWifiSsid
    
    private val bluetoothReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(ctx: android.content.Context?, intent: android.content.Intent?) {
            if (intent?.action == android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(
                    android.bluetooth.BluetoothAdapter.EXTRA_STATE,
                    android.bluetooth.BluetoothAdapter.ERROR
                )
                if (state == android.bluetooth.BluetoothAdapter.STATE_OFF || state == android.bluetooth.BluetoothAdapter.STATE_TURNING_OFF) {
                    bleManager.disconnect()
                    _lastTelemetryTimestamp.value = 0L
                    _espWifiConnected.value = false
                    _espWifiRssi.value = null
                    connectivityCoordinator.recalculateConnectivity()
                } else if (state == android.bluetooth.BluetoothAdapter.STATE_ON) {
                    bleManager.autoReconnectIfPossible()
                    connectivityCoordinator.recalculateConnectivity()
                }
            }
        }
    }

    init {
        try {
            val filter = android.content.IntentFilter(android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED)
            application.registerReceiver(bluetoothReceiver, filter)
        } catch (_: Exception) {}

        // Ensure offline state on startup if Bluetooth is not enabled
        val btMgr = application.getSystemService(android.content.Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager
        if (btMgr?.adapter?.isEnabled != true) {
            bleManager.disconnect()
            _lastTelemetryTimestamp.value = 0L
            _espWifiConnected.value = false
            _espWifiRssi.value = null
            connectivityCoordinator.recalculateConnectivity()
        }

        firebaseAuth?.addAuthStateListener { auth ->
            val user = auth.currentUser
            _currentUser.value = user
            if (user != null) {
                com.example.util.OneSignalManager.login(user.uid)
            } else {
                com.example.util.OneSignalManager.logout()
            }
        }
        viewModelScope.launch {
            bleManager.latestReading.collect { reading ->
                if (reading != null) {
                    _lastTelemetryTimestamp.value = System.currentTimeMillis()
                    if (reading.wifiConnected) {
                        _espWifiConnected.value = true
                        _espWifiRssi.value = if (reading.wifiRssi != 0) reading.wifiRssi else null
                    } else {
                        _espWifiConnected.value = false
                        _espWifiRssi.value = null
                    }

                    // Bidirectional sync: sync profile and unit received from ESP32
                    if (reading.mode.isNotEmpty() && reading.mode != activeProfile.value) {
                        settingsRepository.saveProfile(reading.mode)
                    }
                    if (reading.unit.isNotEmpty() && reading.unit != preferredUnit.value) {
                        settingsRepository.saveUnit(reading.unit)
                    }
                    readingRepository.insert(
                        naValue = reading.na,
                        temperature = reading.temp,
                        millivolts = reading.mv,
                        status = reading.status,
                        calibrationE0 = reading.e0,
                        calibrationSlope = reading.slope
                    )

                    // 1. Sensor Disconnect Detection with restoration tracking
                    val isSensorDown = reading.status.equals("Disconnected", ignoreCase = true) || reading.temp <= -100.0
                    val now = System.currentTimeMillis()
                    if (isSensorDown) {
                        if (!wasSensorDisconnected || (now - lastSensorAlertTime > BREACH_COOLDOWN_MS)) {
                            wasSensorDisconnected = true
                            lastSensorAlertTime = now
                            val title = "Sensor Disconnected"
                            val message = "Thermal probe DS18B20 disconnected or reading invalid. Please check hardware connection."
                            createNotification(title, message, "ALERT")
                            if (pushAlerts.value) {
                                com.example.util.FluxNotificationHelper.showSystemNotification(getApplication(), title, message, isAlert = true)
                            }
                        }
                    } else if (wasSensorDisconnected) {
                        wasSensorDisconnected = false
                        val title = "Sensor Restored"
                        val message = "Thermal probe reconnected. Telemetry reading ${String.format(java.util.Locale.US, "%.1f", reading.temp)}°C."
                        createNotification(title, message, "INFO")
                        if (pushAlerts.value) {
                            com.example.util.FluxNotificationHelper.showSystemNotification(getApplication(), title, message, isAlert = false)
                        }
                    }

                    // 2. Threshold Alert Check with anti-flap hysteresis (0.3°C margin) & 5-min cooldown
                    if (!isSensorDown) {
                        val isCurrentlyBreached = reading.temp < (lowTempThreshold.value - HYSTERESIS_MARGIN) ||
                                                  reading.temp > (highTempThreshold.value + HYSTERESIS_MARGIN)
                        if (isCurrentlyBreached) {
                            val shouldAlert = !wasInBreach || (now - lastBreachAlertTime > BREACH_COOLDOWN_MS)
                            if (shouldAlert) {
                                wasInBreach = true
                                lastBreachAlertTime = now
                                val isHigh = reading.temp > highTempThreshold.value
                                val title = if (isHigh) "High Temperature Alert" else "Low Temperature Alert"
                                val message = if (isHigh) {
                                    "Temperature ${String.format(java.util.Locale.US, "%.1f", reading.temp)}°C exceeded maximum limit (${highTempThreshold.value}°C)."
                                } else {
                                    "Temperature ${String.format(java.util.Locale.US, "%.1f", reading.temp)}°C dropped below minimum limit (${lowTempThreshold.value}°C)."
                                }
                                createNotification(title, message, "ALERT")
                                if (pushAlerts.value) {
                                    com.example.util.FluxNotificationHelper.showSystemNotification(getApplication(), title, message, isAlert = true)
                                }
                            }
                        } else if (wasInBreach && reading.temp >= lowTempThreshold.value && reading.temp <= highTempThreshold.value) {
                            // Temperature safely normalized
                            wasInBreach = false
                            val title = "Temperature Normalized"
                            val message = "Thermal reading returned to safe range (${String.format(java.util.Locale.US, "%.1f", reading.temp)}°C)."
                            createNotification(title, message, "INFO")
                            if (pushAlerts.value) {
                                com.example.util.FluxNotificationHelper.showSystemNotification(getApplication(), title, message, isAlert = false)
                            }
                        }
                    }

                    // Direct telemetry upload exclusively to Supabase (Zero writes to Firebase database)
                    val addr = settingsRepository.lastDeviceAddress.first() ?: "Flux1"
                    supabaseRepository.uploadTelemetry(
                        supabaseUrl = supabaseUrl.value,
                        anonKey = supabaseAnonKey.value,
                        deviceId = addr,
                        temperatureC = reading.temp,
                        profile = reading.mode.ifEmpty { activeProfile.value },
                        isAlert = reading.isAlert || (reading.temp < lowTempThreshold.value || reading.temp > highTempThreshold.value),
                        unit = preferredUnit.value,
                        batteryPercent = reading.battery,
                        batteryVolts = reading.batteryVoltage,
                        charging = false,
                        sensorConnected = reading.status != "Disconnected",
                        uploadIntervalSec = cloudUploadIntervalSec.value,
                        backendUrl = backendUrl.value
                    )
                }
            }
        }
        
        // Auto connect if saved (gated by manual transport preference)
        viewModelScope.launch {
            combine(
                settingsRepository.lastDeviceAddress,
                smartSwitchMode,
                manualPreferredTransport
            ) { addr, mode, pref ->
                Triple(addr, mode, pref)
            }.collect { (addr, mode, pref) ->
                val isManualWifi = mode.equals("manual", ignoreCase = true) && pref.equals("wifi", ignoreCase = true)
                val isBtEnabled = bleManager.isBluetoothEnabled()
                if (addr != null && !isManualWifi && isBtEnabled && bleManager.connectionState.value == ConnectionState.DISCONNECTED) {
                    bleManager.connect(addr)
                }
            }
        }

        // Track connection state for notifications and offline resets
        viewModelScope.launch {
            var wasConnected = false
            bleManager.connectionState.collect { state ->
                if (state == ConnectionState.CONNECTED && !wasConnected) {
                    wasConnected = true
                    createNotification("Device Connected", "Successfully connected to your Flux sensor.", "INFO")
                } else if (state == ConnectionState.DISCONNECTED) {
                    if (wasConnected) {
                        wasConnected = false
                        createNotification("Device Disconnected", "Connection to Flux sensor was lost.", "WARNING")
                    }
                    _lastTelemetryTimestamp.value = 0L
                    _espWifiConnected.value = false
                    _espWifiRssi.value = null
                    connectivityCoordinator.recalculateConnectivity()
                }
            }
        }
    }

    fun createNotification(title: String, message: String, type: String = "INFO") {
        viewModelScope.launch {
            val formatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
            val time = formatter.format(java.util.Date())
            notificationRepository.insert(
                NotificationEntity(
                    title = title,
                    message = message,
                    time = time,
                    type = type,
                    isRead = false
                )
            )
        }
    }

    private fun initializeFirestore() {
        val collections = listOf("users", "sensors", "measurements", "notifications")
        collections.forEach { collection ->
            firebaseFirestore?.collection(collection)?.document("init")?.set(mapOf("initialized" to true), SetOptions.merge())
        }
    }

    fun startScan() {
        bleManager.startScan()
    }
    
    fun stopScan() {
        bleManager.stopScan()
    }

    fun connectDevice(device: DiscoveredDevice) {
        viewModelScope.launch {
            settingsRepository.saveDevice(device.address, device.name)
            bleManager.connect(device.address)
        }
    }
    
    fun disconnectDevice() {
        viewModelScope.launch {
            settingsRepository.clearDevice()
            bleManager.disconnect()
        }
    }

    fun takeNewReading() {
        bleManager.requestNewReading()
    }

    fun setUnit(unit: String) {
        viewModelScope.launch {
            settingsRepository.saveUnit(unit)
        }
    }

    fun setProfile(profile: String) {
        viewModelScope.launch {
            settingsRepository.saveProfile(profile)
        }
    }

    fun setTemperatureThresholds(low: Double, high: Double) {
        viewModelScope.launch {
            settingsRepository.saveThresholds(low, high)
        }
    }

    fun setUploadInterval(sec: Int) {
        viewModelScope.launch {
            settingsRepository.saveUploadInterval(sec)
        }
    }

    fun saveCloudCredentials(url: String, key: String, onesignalId: String) {
        viewModelScope.launch {
            settingsRepository.saveCloudCredentials(url, key, onesignalId)
            com.example.util.OneSignalManager.init(getApplication(), onesignalId)
        }
    }

    fun fetchCloudHistory() {
        viewModelScope.launch {
            val addr = settingsRepository.lastDeviceAddress.first() ?: "Flux1"
            supabaseRepository.fetchHistoricalTelemetry(supabaseUrl.value, supabaseAnonKey.value, addr, backendUrl.value)
        }
    }
    
    fun applyCalibration(e0: Double, slope: Double) {
        bleManager.applyCalibration(e0, slope)
        viewModelScope.launch {
            calibrationRepository.insert(e0, slope)
        }
        val bundle = Bundle().apply {
            putDouble("e0", e0)
            putDouble("slope", slope)
        }
        logFirebaseEvent("sensor_calibrated", bundle)
        syncToFirebase("telemetry/calibration", mapOf("e0" to e0, "slope" to slope, "timestamp" to System.currentTimeMillis()))
    }
    
    fun saveSettings(low: Double, high: Double) {
        viewModelScope.launch {
            settingsRepository.saveThresholds(low, high)
        }
    }

    fun saveTheme(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveTheme(isDark)
        }
    }

    fun uploadProfilePicture(uri: android.net.Uri, context: android.content.Context, callback: (String?) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                
                if (bytes != null) {
                    var url: String? = null
                    try {
                        val fallbackFile = java.io.File(context.filesDir, "profile_pic_${System.currentTimeMillis()}.jpg")
                        fallbackFile.writeBytes(bytes)
                        url = android.net.Uri.fromFile(fallbackFile).toString()
                    } catch (e: Exception) {
                        android.util.Log.e("MainViewModel", "Local profile pic save failed", e)
                    }
                    
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        callback(url)
                    }
                } else {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        callback(null)
                    }
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }

    fun saveUserProfile(name: String, age: String, weight: String, height: String, photoUrl: String) {
        viewModelScope.launch {
            settingsRepository.saveUserProfile(name, age, weight, height, photoUrl)
            syncToFirebase("profile", mapOf(
                "name" to name,
                "age" to age,
                "weight" to weight,
                "height" to height,
                "photoUrl" to photoUrl
            ))
        }
    }

    fun updateWaterIntake(intakeMl: Int) {
        viewModelScope.launch {
            settingsRepository.saveWaterIntake(intakeMl)
        }
        val bundle = Bundle().apply {
            putInt("intake_ml", intakeMl)
        }
        logFirebaseEvent("water_intake_logged", bundle)
        syncToFirebase("telemetry/water_intake", mapOf("amount_ml" to intakeMl, "timestamp" to System.currentTimeMillis()))
    }

    fun updateWaterGoal(goalMl: Int) {
        viewModelScope.launch {
            settingsRepository.saveWaterGoal(goalMl)
        }
        val bundle = Bundle().apply {
            putInt("goal_ml", goalMl)
        }
        logFirebaseEvent("water_goal_updated", bundle)
        syncToFirebase("telemetry/water_goal", mapOf("goal_ml" to goalMl, "timestamp" to System.currentTimeMillis()))
    }

    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }

    fun dismissNotification(id: Int) {
        viewModelScope.launch {
            notificationRepository.delete(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            notificationRepository.deleteAll()
        }
    }

    fun sendPasswordResetEmail(emailStr: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (firebaseAuth == null) {
            onFailure("Firebase Auth is not initialized")
            return
        }
        firebaseAuth.sendPasswordResetEmail(emailStr)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    logFirebaseEvent("password_reset_success")
                    onSuccess()
                } else {
                    val error = task.exception?.localizedMessage ?: "Failed to send reset email"
                    logFirebaseEvent("password_reset_failed", Bundle().apply { putString("error", error) })
                    onFailure(error)
                }
            }
    }

    fun loginWithEmail(emailStr: String, passwordStr: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (firebaseAuth == null) {
            onFailure("Firebase Auth is not initialized")
            return
        }
        firebaseAuth.signInWithEmailAndPassword(emailStr, passwordStr)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    logFirebaseEvent("login_success")
                    createNotification("Login Successful", "Welcome back!", "INFO")
                    onSuccess()
                } else {
                    val error = task.exception?.localizedMessage ?: "Login failed"
                    logFirebaseEvent("login_failed", Bundle().apply { putString("error", error) })
                    onFailure(error)
                }
            }
    }

    fun signUpWithEmail(emailStr: String, passwordStr: String, nameStr: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (firebaseAuth == null) {
            onFailure("Firebase Auth is not initialized")
            return
        }
        firebaseAuth.createUserWithEmailAndPassword(emailStr, passwordStr)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                        displayName = nameStr
                    }
                    user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                        logFirebaseEvent("signup_success")
                        syncToFirebase("profile", mapOf("name" to nameStr, "email" to emailStr))
                        createNotification("Account Created", "Welcome to Flux, $nameStr!", "INFO")
                        onSuccess()
                    } ?: run {
                        logFirebaseEvent("signup_success")
                        createNotification("Account Created", "Welcome to Flux!", "INFO")
                        onSuccess()
                    }
                } else {
                    val error = task.exception?.localizedMessage ?: "Sign up failed"
                    logFirebaseEvent("signup_failed", Bundle().apply { putString("error", error) })
                    onFailure(error)
                }
            }
    }

    fun loginWithGoogleToken(idToken: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (firebaseAuth == null) {
            onFailure("Firebase Auth is not initialized")
            return
        }
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    logFirebaseEvent("google_login_success")
                    if (user != null) {
                        val profileData = mutableMapOf<String, Any>(
                            "name" to (user.displayName ?: ""),
                            "email" to (user.email ?: "")
                        )
                        user.photoUrl?.toString()?.takeIf { it.isNotEmpty() }?.let {
                            profileData["photoUrl"] = it
                        }
                        syncToFirebase("profile", profileData)
                        createNotification("Login Successful", "Welcome back, ${user.displayName ?: "User"}!", "INFO")
                    }
                    onSuccess()
                } else {
                    val error = task.exception?.localizedMessage ?: "Google Sign-In failed"
                    logFirebaseEvent("google_login_failed", Bundle().apply { putString("error", error) })
                    onFailure(error)
                }
            }
    }

    fun logout() {
        firebaseAuth?.signOut()
        com.example.util.OneSignalManager.logout()
        
        // Force account selection on next login by signing out of Google
        val context = getApplication<android.app.Application>()
        val webClientIdResId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (webClientIdResId != 0) {
            val webClientId = context.getString(webClientIdResId)
            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
            com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso).signOut()
        }
        
        logFirebaseEvent("user_logout")
    }

    fun updateThresholds(low: Double, high: Double) {
        viewModelScope.launch {
            settingsRepository.saveThresholds(low, high)
            bleManager.sendCommand("SET_THRESH:${low.toInt()},${high.toInt()}")
        }
    }

    fun setPreferredUnit(unit: String) {
        viewModelScope.launch {
            settingsRepository.saveUnit(unit)
            val cmd = if (unit.equals("F", ignoreCase = true)) "SET_UNIT_F" else "SET_UNIT_C"
            bleManager.sendCommand(cmd)
        }
    }

    fun setBuzzerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            bleManager.setBuzzerEnabled(enabled)
        }
    }

    fun saveActiveProfile(profile: String) {
        viewModelScope.launch {
            settingsRepository.saveProfile(profile)
            val cmd = when (profile.lowercase()) {
                "live monitor", "live" -> "MODE_LIVE"
                "environment", "env" -> "MODE_ENV"
                "liquid probe", "liquid" -> "MODE_LIQ"
                "fermentation", "ferm" -> "MODE_FERM"
                else -> "MODE_LIVE"
            }
            bleManager.sendCommand(cmd)
        }
    }

    fun sendWifiCredentials(ssid: String, pass: String) {
        viewModelScope.launch {
            settingsRepository.saveWifiCredentials(ssid, pass)
            bleManager.sendCommand("SET_WIFI:$ssid,$pass")
        }
    }

    fun turnOffBle() {
        viewModelScope.launch {
            try {
                bleManager.sendCommand("BLE_OFF")
            } catch (_: Exception) {
            }
            bleManager.disconnect()
        }
    }

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveTheme(isDark)
        }
    }

    fun flushOfflineQueue() {
        viewModelScope.launch {
            supabaseRepository.flushOfflineQueueNow(supabaseUrl.value, supabaseAnonKey.value, backendUrl.value)
        }
    }

    fun saveBackendUrl(url: String) {
        viewModelScope.launch {
            settingsRepository.saveBackendUrl(url)
        }
    }

    fun setSmartSwitchMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.saveSmartSwitchMode(mode)
            connectivityCoordinator.recalculateConnectivity()
        }
    }

    fun setManualPreferredTransport(transport: String) {
        viewModelScope.launch {
            settingsRepository.saveManualPreferredTransport(transport)
            connectivityCoordinator.recalculateConnectivity()
        }
    }

    suspend fun provisionWifiOverAp(ssid: String, pass: String): Result<Boolean> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                settingsRepository.saveWifiCredentials(ssid, pass)
                val url = java.net.URL("http://192.168.4.1/wifi")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 6000
                conn.readTimeout = 8000
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                val jsonPayload = org.json.JSONObject().apply {
                    put("ssid", ssid)
                    put("pass", pass)
                }.toString()
                conn.outputStream.use { os ->
                    os.write(jsonPayload.toByteArray(Charsets.UTF_8))
                }
                val code = conn.responseCode
                conn.disconnect()
                if (code in 200..299) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("ESP32 SoftAP returned HTTP $code"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(bluetoothReceiver)
        } catch (_: Exception) {}
        connectivityCoordinator.cleanup()
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
