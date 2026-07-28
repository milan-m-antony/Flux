package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FCM_DEBUG"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "onMessageReceived called. From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            // Handle data payload (e.g. background data refresh)
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            showNotification(it.title, it.body)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken called. New token generated: $token")
        // Send the token to the server
        sendRegistrationToServer(token)
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
            Log.d(TAG, "No authenticated user. Token will not be saved to Firestore.")
        }
    }

    private fun showNotification(title: String?, message: String?) {
        Log.d(TAG, "Creating notification builder for title: $title, message: $message")
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "default_channel_id"
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title ?: "NaTrack Notification")
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Creating notification channel (API 26+)")
            val channel = NotificationChannel(
                channelId,
                "NaTrack Default Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            // Configure additional channel settings here if needed
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = Random.nextInt()
        Log.d(TAG, "Dispatching notification with ID: $notificationId")
        notificationManager.notify(notificationId, builder.build())
    }
}
