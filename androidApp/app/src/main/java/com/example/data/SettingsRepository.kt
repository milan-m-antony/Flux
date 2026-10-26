package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        val LAST_DEVICE_ADDRESS = stringPreferencesKey("last_device_address")
        val LAST_DEVICE_NAME = stringPreferencesKey("last_device_name")
        val PREFERRED_UNIT = stringPreferencesKey("preferred_unit") // "C" or "F"
        val ACTIVE_PROFILE = stringPreferencesKey("active_profile") // "Live Monitor", "Environment", "Liquid Probe", "Fermentation"
        val LOW_TEMP_THRESHOLD = doublePreferencesKey("low_temp_threshold")
        val HIGH_TEMP_THRESHOLD = doublePreferencesKey("high_temp_threshold")
        val CLOUD_UPLOAD_INTERVAL_SEC = intPreferencesKey("cloud_upload_interval_sec")
        val SUPABASE_URL = stringPreferencesKey("supabase_url")
        val SUPABASE_ANON_KEY = stringPreferencesKey("supabase_anon_key")
        val ONESIGNAL_APP_ID = stringPreferencesKey("onesignal_app_id")
        val BACKEND_URL = stringPreferencesKey("backend_url")

        const val DEFAULT_SUPABASE_URL = "https://pivtrxrhxdqxzactcrrb.supabase.co"
        const val DEFAULT_SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InBpdnRyeHJoeGRxeHphY3RjcnJiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTI3NjAxMjEsImV4cCI6MjEwODMzNjEyMX0.LGOfzD6MVDQQ725kJEC8JEcfwKUQyxpt3V8RBB7HNxg"
        const val DEFAULT_ONESIGNAL_APP_ID = "50847ad7-fc7f-4c89-85a9-c5d7f91cc2c8"
        const val DEFAULT_BACKEND_URL = "https://flux-backend.onrender.com"
        
        val PUSH_ALERTS = booleanPreferencesKey("push_alerts")
        val SOUND_ALERTS = booleanPreferencesKey("sound_alerts")
        val VIBRATION_ALERTS = booleanPreferencesKey("vibration_alerts")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_AGE = stringPreferencesKey("user_age")
        val USER_WEIGHT = stringPreferencesKey("user_weight")
        val USER_HEIGHT = stringPreferencesKey("user_height")
        val USER_PHOTO_URL = stringPreferencesKey("user_photo_url")
        val USER_NOTES = stringPreferencesKey("user_notes")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val WATER_INTAKE_ML = intPreferencesKey("water_intake_ml")
        val WATER_GOAL_ML = intPreferencesKey("water_goal_ml")
        val SAVED_WIFI_SSID = stringPreferencesKey("saved_wifi_ssid")
        val SAVED_WIFI_PASS = stringPreferencesKey("saved_wifi_pass")
    }

    val lastDeviceAddress: Flow<String?> = dataStore.data.map { it[LAST_DEVICE_ADDRESS] }
    val lastDeviceName: Flow<String?> = dataStore.data.map { it[LAST_DEVICE_NAME] }
    val preferredUnit: Flow<String> = dataStore.data.map { it[PREFERRED_UNIT] ?: "C" }
    val activeProfile: Flow<String> = dataStore.data.map { it[ACTIVE_PROFILE] ?: "Live Monitor" }
    val lowTempThreshold: Flow<Double> = dataStore.data.map { it[LOW_TEMP_THRESHOLD] ?: 18.0 }
    val highTempThreshold: Flow<Double> = dataStore.data.map { it[HIGH_TEMP_THRESHOLD] ?: 28.0 }
    val cloudUploadIntervalSec: Flow<Int> = dataStore.data.map { it[CLOUD_UPLOAD_INTERVAL_SEC] ?: 5 }
    val supabaseUrl: Flow<String> = dataStore.data.map { it[SUPABASE_URL] ?: DEFAULT_SUPABASE_URL }
    val supabaseAnonKey: Flow<String> = dataStore.data.map { it[SUPABASE_ANON_KEY] ?: DEFAULT_SUPABASE_ANON_KEY }
    val onesignalAppId: Flow<String> = dataStore.data.map { it[ONESIGNAL_APP_ID] ?: DEFAULT_ONESIGNAL_APP_ID }
    val backendUrl: Flow<String> = dataStore.data.map { it[BACKEND_URL] ?: DEFAULT_BACKEND_URL }

    val savedWifiSsid: Flow<String> = dataStore.data.map { it[SAVED_WIFI_SSID] ?: "" }
    val savedWifiPass: Flow<String> = dataStore.data.map { it[SAVED_WIFI_PASS] ?: "" }

    val pushAlerts: Flow<Boolean> = dataStore.data.map { it[PUSH_ALERTS] ?: true }
    val soundAlerts: Flow<Boolean> = dataStore.data.map { it[SOUND_ALERTS] ?: true }
    val vibrationAlerts: Flow<Boolean> = dataStore.data.map { it[VIBRATION_ALERTS] ?: true }

    val userName: Flow<String> = dataStore.data.map { it[USER_NAME] ?: "" }
    val userAge: Flow<String> = dataStore.data.map { it[USER_AGE] ?: "" }
    val userWeight: Flow<String> = dataStore.data.map { it[USER_WEIGHT] ?: "" }
    val userHeight: Flow<String> = dataStore.data.map { it[USER_HEIGHT] ?: "" }
    val userPhotoUrl: Flow<String> = dataStore.data.map { it[USER_PHOTO_URL] ?: "" }
    val userNotes: Flow<String> = dataStore.data.map { it[USER_NOTES] ?: "" }
    val isDarkTheme: Flow<Boolean> = dataStore.data.map { it[IS_DARK_THEME] ?: true } // Flux defaults to sleek dark theme
    val waterIntakeMl: Flow<Int> = dataStore.data.map { it[WATER_INTAKE_ML] ?: 1250 }
    val waterGoalMl: Flow<Int> = dataStore.data.map { it[WATER_GOAL_ML] ?: 2500 }

    suspend fun saveDevice(address: String, name: String?) {
        dataStore.edit { prefs ->
            prefs[LAST_DEVICE_ADDRESS] = address
            if (name != null) prefs[LAST_DEVICE_NAME] = name
        }
    }

    suspend fun clearDevice() {
        dataStore.edit { prefs ->
            prefs.remove(LAST_DEVICE_ADDRESS)
            prefs.remove(LAST_DEVICE_NAME)
        }
    }

    suspend fun saveUnit(unit: String) {
        dataStore.edit { prefs ->
            prefs[PREFERRED_UNIT] = unit
        }
    }

    suspend fun saveProfile(profile: String) {
        dataStore.edit { prefs ->
            prefs[ACTIVE_PROFILE] = profile
        }
    }

    suspend fun saveThresholds(low: Double, high: Double) {
        dataStore.edit { prefs ->
            prefs[LOW_TEMP_THRESHOLD] = low
            prefs[HIGH_TEMP_THRESHOLD] = high
        }
    }

    suspend fun saveUploadInterval(seconds: Int) {
        dataStore.edit { prefs ->
            prefs[CLOUD_UPLOAD_INTERVAL_SEC] = seconds
        }
    }

    suspend fun saveCloudCredentials(url: String, key: String, onesignalId: String) {
        dataStore.edit { prefs ->
            prefs[SUPABASE_URL] = url
            prefs[SUPABASE_ANON_KEY] = key
            prefs[ONESIGNAL_APP_ID] = onesignalId
        }
    }

    suspend fun saveBackendUrl(url: String) {
        dataStore.edit { prefs ->
            prefs[BACKEND_URL] = url
        }
    }

    suspend fun saveNotificationSettings(push: Boolean, sound: Boolean, vibration: Boolean) {
        dataStore.edit { prefs ->
            prefs[PUSH_ALERTS] = push
            prefs[SOUND_ALERTS] = sound
            prefs[VIBRATION_ALERTS] = vibration
        }
    }

    suspend fun saveUserProfile(name: String, age: String, weight: String, height: String, photoUrl: String) {
        dataStore.edit { prefs ->
            prefs[USER_NAME] = name
            prefs[USER_AGE] = age
            prefs[USER_WEIGHT] = weight
            prefs[USER_HEIGHT] = height
            prefs[USER_PHOTO_URL] = photoUrl
        }
    }

    suspend fun saveTheme(isDark: Boolean) {
        dataStore.edit { prefs ->
            prefs[IS_DARK_THEME] = isDark
        }
    }

    suspend fun saveWaterIntake(intakeMl: Int) {
        dataStore.edit { prefs ->
            prefs[WATER_INTAKE_ML] = intakeMl
        }
    }

    suspend fun saveWaterGoal(goalMl: Int) {
        dataStore.edit { prefs ->
            prefs[WATER_GOAL_ML] = goalMl
        }
    }

    suspend fun saveWifiCredentials(ssid: String, pass: String) {
        dataStore.edit { prefs ->
            prefs[SAVED_WIFI_SSID] = ssid
            prefs[SAVED_WIFI_PASS] = pass
        }
    }
}
