package com.example.data

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

object RemoteConfigManager {
    private val remoteConfig = FirebaseRemoteConfig.getInstance()
    
    init {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600) // 1 hour
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(mapOf(
            "CLOUDINARY_API_KEY" to "",
            "CLOUDINARY_CLOUD_NAME" to "",
            "CLOUDINARY_PRESET" to ""
        ))
    }
    
    fun fetchAndActivate(onComplete: (Boolean) -> Unit) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }
    
    fun getString(key: String): String = remoteConfig.getString(key)
}
