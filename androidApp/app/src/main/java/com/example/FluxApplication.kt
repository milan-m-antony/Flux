package com.example

import android.app.Application
import android.util.Log
import com.example.util.FluxNotificationHelper
import com.example.util.OneSignalManager
import com.google.firebase.messaging.FirebaseMessaging

class FluxApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i("FluxApplication", "Initializing Flux Application...")
        
        // Ensure high-priority alert notification channel is created with sound & vibration
        FluxNotificationHelper.ensureNotificationChannel(this)

        // Initialize OneSignal with provided App ID
        OneSignalManager.init(this, OneSignalManager.DEFAULT_APP_ID)

        // Auto-subscribe to FCM alert topics
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("alerts")
                .addOnSuccessListener { Log.i("FluxApplication", "Subscribed to FCM topic: alerts") }
            FirebaseMessaging.getInstance().subscribeToTopic("flux_alerts")
                .addOnSuccessListener { Log.i("FluxApplication", "Subscribed to FCM topic: flux_alerts") }
            FirebaseMessaging.getInstance().subscribeToTopic("all")
                .addOnSuccessListener { Log.i("FluxApplication", "Subscribed to FCM topic: all") }

            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.i("FluxApplication", "FCM Device Token: $token")
                }
            }
        } catch (e: Exception) {
            Log.e("FluxApplication", "Error during FCM topic subscription", e)
        }
    }
}
