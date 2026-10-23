package com.example

import android.app.Application
import android.util.Log
import com.example.util.OneSignalManager

class FluxApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i("FluxApplication", "Initializing Flux Application...")
        
        // Initialize OneSignal with provided App ID
        OneSignalManager.init(this, OneSignalManager.DEFAULT_APP_ID)
    }
}
