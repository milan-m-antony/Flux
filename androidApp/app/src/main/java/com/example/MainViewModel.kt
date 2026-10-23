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
    private val supabaseRepository = com.example.data.SupabaseRepository(viewModelScope)

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

    private val firebaseAuth = try {
        FirebaseAuth.getInstance()
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

    val supabaseSyncStatus = supabaseRepository.syncStatus
    val pendingOfflineRecords = supabaseRepository.pendingQueueCount
    val cloudHistoricalTelemetry = supabaseRepository.historicalTelemetry

    val lowAlertThreshold = lowTempThreshold
    val highAlertThreshold = highTempThreshold
    val isDarkTheme = settingsRepository.isDarkTheme.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    val userName = settingsRepository.userName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val userAge = settingsRepository.userAge.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val userWeight = settingsRepository.userWeight.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val userHeight = settingsRepository.userHeight.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val userPhotoUrl = settingsRepository.userPhotoUrl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _lastTelemetryTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastTelemetryTimestamp: StateFlow<Long> = _lastTelemetryTimestamp.asStateFlow()

    private val _espWifiConnected = MutableStateFlow(true)
    val espWifiConnected: StateFlow<Boolean> = _espWifiConnected.asStateFlow()

    private val _espWifiRssi = MutableStateFlow<Int?>(-57)
    val espWifiRssi: StateFlow<Int?> = _espWifiRssi.asStateFlow()

    val connectivityCoordinator = FluxConnectivityCoordinator(
        context = application,
        scope = viewModelScope,
        bleManager = bleManager,
        espWifiConnected = _espWifiConnected,
        espWifiRssi = _espWifiRssi,
        lastTelemetryTimestamp = _lastTelemetryTimestamp,
        cloudSyncStatus = supabaseSyncStatus
    )
    val connectivityState: StateFlow<FluxConnectivityState> = connectivityCoordinator.connectivityState
    
    init {
        firebaseAuth?.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser
        }
        viewModelScope.launch {
            bleManager.latestReading.collect { reading ->
                if (reading != null) {
                    _lastTelemetryTimestamp.value = System.currentTimeMillis()
                    if (reading.wifiConnected) {
                        _espWifiConnected.value = true
                        if (reading.wifiRssi != 0) {
                            _espWifiRssi.value = reading.wifiRssi
                        }
                    }
                    readingRepository.insert(
                        naValue = reading.na,
                        temperature = reading.temp,
                        millivolts = reading.mv,
                        status = reading.status,
                        calibrationE0 = reading.e0,
                        calibrationSlope = reading.slope
                    )

                    // Threshold alert check with in-app notification
                    if (reading.temp < lowTempThreshold.value || reading.temp > highTempThreshold.value) {
                        createNotification(
                            title = "Threshold Alert",
                            message = "Temperature ${reading.temp}°C is outside configured limits (${lowTempThreshold.value}-${highTempThreshold.value}°C).",
                            type = "ALERT"
                        )
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
                        uploadIntervalSec = cloudUploadIntervalSec.value
                    )
                }
            }
        }
        
        // Auto connect if saved
        viewModelScope.launch {
            settingsRepository.lastDeviceAddress.collect { addr: String? ->
                if (addr != null && bleManager.connectionState.value == ConnectionState.DISCONNECTED) {
                    bleManager.connect(addr)
                }
            }
        }

        // Track connection state for notifications
        viewModelScope.launch {
            var wasConnected = false
            bleManager.connectionState.collect { state ->
                if (state == ConnectionState.CONNECTED && !wasConnected) {
                    wasConnected = true
                    createNotification("Device Connected", "Successfully connected to your Flux sensor.", "INFO")
                } else if (state == ConnectionState.DISCONNECTED && wasConnected) {
                    wasConnected = false
                    createNotification("Device Disconnected", "Connection to Flux sensor was lost.", "WARNING")
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
            supabaseRepository.fetchHistoricalTelemetry(supabaseUrl.value, supabaseAnonKey.value, addr)
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
        }
    }

    fun setPreferredUnit(unit: String) {
        viewModelScope.launch {
            settingsRepository.saveUnit(unit)
        }
    }

    fun saveActiveProfile(profile: String) {
        viewModelScope.launch {
            settingsRepository.saveProfile(profile)
        }
    }

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveTheme(isDark)
        }
    }

    fun flushOfflineQueue() {
        viewModelScope.launch {
            supabaseRepository.flushOfflineQueueNow(supabaseUrl.value, supabaseAnonKey.value)
        }
    }

    override fun onCleared() {
        super.onCleared()
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
