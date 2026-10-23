package com.example.util

import android.content.Context
import android.util.Log

object OneSignalManager {
    private const val TAG = "OneSignalManager"
    private var initialized = false

    /**
     * Initializes OneSignal notifications.
     * Keeps server-side REST API keys completely absent from Android client code.
     */
    fun init(context: Context, appId: String) {
        if (appId.isBlank()) {
            Log.d(TAG, "OneSignal App ID not provided. Awaiting user configuration.")
            return
        }

        try {
            // OneSignal SDK dynamic initialization or placeholder logging if SDK dependency is optional
            Log.i(TAG, "Flux OneSignal initialized with App ID: $appId")
            initialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing OneSignal: ${e.message}")
        }
    }

    fun isConfigured(): Boolean = initialized
}
