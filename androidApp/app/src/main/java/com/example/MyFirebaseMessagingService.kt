package com.example

import android.util.Log
import com.example.data.AppDatabase
import com.example.data.NotificationEntity
import com.example.util.FluxNotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FCM_DEBUG"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "onMessageReceived called. From: ${remoteMessage.from}")

        // 1. Resolve notification title and body from notification payload OR data payload
        val rawTitle = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Flux Telemetry Alert"

        val rawBody = remoteMessage.notification?.body
            ?: remoteMessage.data["message"]
            ?: remoteMessage.data["body"]
            ?: "New notification from your Flux thermal monitoring node."

        // Sanitize incoming strings to purge any legacy project identifiers (e.g. NaTrack)
        val title = FluxNotificationHelper.sanitizeBrandText(rawTitle).let {
            if (it.isBlank() || it.equals("Flux", ignoreCase = true)) "Flux Telemetry Alert" else it
        }

        val body = FluxNotificationHelper.sanitizeBrandText(rawBody).let {
            if (it.isBlank()) "New notification from your Flux thermal monitoring node." else it
        }

        val type = remoteMessage.data["type"]
            ?: if (title.contains("Alert", ignoreCase = true) || body.contains("Alert", ignoreCase = true) || body.contains("exceeded", ignoreCase = true) || body.contains("breach", ignoreCase = true)) "ALERT"
               else if (title.contains("Warning", ignoreCase = true)) "WARNING"
               else "INFO"

        Log.d(TAG, "FCM Payload extracted: title='$title', body='$body', type='$type'")

        // 2. Persist real notification into Room Database so it appears in FluxAlertsScreen
        val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val entity = NotificationEntity(
            title = title,
            message = body,
            time = timeStr,
            type = type,
            isRead = false
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDatabase.getDatabase(applicationContext).notificationDao().insertNotification(entity)
                Log.d(TAG, "FCM Alert successfully recorded into Room Database.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to insert FCM notification into Room Database", e)
            }
        }

        // 3. Dispatch system heads-up notification with audible alert sound & vibration
        FluxNotificationHelper.showSystemNotification(
            context = this,
            title = title,
            message = body,
            isAlert = type == "ALERT" || type == "WARNING"
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken called. New token generated: $token")
        sendRegistrationToServer(token)
        subscribeToDefaultTopics()
    }

    private fun subscribeToDefaultTopics() {
        FirebaseMessaging.getInstance().subscribeToTopic("alerts")
            .addOnSuccessListener { Log.d(TAG, "Subscribed to FCM topic: alerts") }
        FirebaseMessaging.getInstance().subscribeToTopic("flux_alerts")
            .addOnSuccessListener { Log.d(TAG, "Subscribed to FCM topic: flux_alerts") }
        FirebaseMessaging.getInstance().subscribeToTopic("all")
            .addOnSuccessListener { Log.d(TAG, "Subscribed to FCM topic: all") }
    }

    private fun sendRegistrationToServer(token: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            Log.d(TAG, "Saving FCM token to Firestore for user: ${user.uid}")
            FirebaseFirestore.getInstance().collection("users").document(user.uid)
                .update("fcmToken", token)
                .addOnSuccessListener { Log.d(TAG, "Token successfully saved to Firestore") }
                .addOnFailureListener { e -> Log.e(TAG, "Failed to save token to Firestore", e) }
        } else {
            Log.d(TAG, "No authenticated user. Storing device token in Firestore device_tokens.")
            val data = hashMapOf(
                "token" to token,
                "updatedAt" to com.google.firebase.Timestamp.now(),
                "device" to (android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL)
            )
            FirebaseFirestore.getInstance().collection("device_tokens").document("flux_default_device")
                .set(data)
                .addOnSuccessListener { Log.d(TAG, "Device token successfully saved to Firestore device_tokens") }
                .addOnFailureListener { e -> Log.w(TAG, "Could not save device token to Firestore: ${e.message}") }
        }
    }
}
