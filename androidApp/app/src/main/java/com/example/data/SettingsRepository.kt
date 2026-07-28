package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        val LAST_DEVICE_ADDRESS = stringPreferencesKey("last_device_address")
        val LAST_DEVICE_NAME = stringPreferencesKey("last_device_name")
        val LOW_ALERT_THRESHOLD = doublePreferencesKey("low_alert_threshold")
        val HIGH_ALERT_THRESHOLD = doublePreferencesKey("high_alert_threshold")
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
    }

    val lastDeviceAddress: Flow<String?> = dataStore.data.map { it[LAST_DEVICE_ADDRESS] }
    val lastDeviceName: Flow<String?> = dataStore.data.map { it[LAST_DEVICE_NAME] }
    
    val lowAlertThreshold: Flow<Double> = dataStore.data.map { it[LOW_ALERT_THRESHOLD] ?: 135.0 }
    val highAlertThreshold: Flow<Double> = dataStore.data.map { it[HIGH_ALERT_THRESHOLD] ?: 145.0 }
    
    val pushAlerts: Flow<Boolean> = dataStore.data.map { it[PUSH_ALERTS] ?: true }
    val soundAlerts: Flow<Boolean> = dataStore.data.map { it[SOUND_ALERTS] ?: true }
    val vibrationAlerts: Flow<Boolean> = dataStore.data.map { it[VIBRATION_ALERTS] ?: true }
    
    val userName: Flow<String> = dataStore.data.map { it[USER_NAME] ?: "" }
    val userAge: Flow<String> = dataStore.data.map { it[USER_AGE] ?: "" }
    val userWeight: Flow<String> = dataStore.data.map { it[USER_WEIGHT] ?: "" }
    val userHeight: Flow<String> = dataStore.data.map { it[USER_HEIGHT] ?: "" }
    val userPhotoUrl: Flow<String> = dataStore.data.map { it[USER_PHOTO_URL] ?: "" }
    val userNotes: Flow<String> = dataStore.data.map { it[USER_NOTES] ?: "" }
    val isDarkTheme: Flow<Boolean> = dataStore.data.map { it[IS_DARK_THEME] ?: false }
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

    suspend fun saveThresholds(low: Double, high: Double) {
        dataStore.edit { prefs ->
            prefs[LOW_ALERT_THRESHOLD] = low
            prefs[HIGH_ALERT_THRESHOLD] = high
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
}
