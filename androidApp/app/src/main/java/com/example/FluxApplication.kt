package com.example

import android.app.Application
import android.util.Log
import com.example.util.OneSignalManager

class FluxApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i("FluxApplication", "Initializing Flux Application...")
        
        // Ensure high-priority alert notification channel is created
        com.example.util.FluxNotificationHelper.ensureNotificationChannel(this)

        // Initialize OneSignal with provided App ID
        OneSignalManager.init(this, OneSignalManager.DEFAULT_APP_ID)
    }
}
