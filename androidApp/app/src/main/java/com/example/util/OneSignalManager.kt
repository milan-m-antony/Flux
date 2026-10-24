package com.example.util

import android.content.Context
import android.util.Log
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.onesignal.user.subscriptions.IPushSubscriptionObserver
import com.onesignal.user.subscriptions.PushSubscriptionChangedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object OneSignalManager {
    private const val TAG = "OneSignalManager"
    const val DEFAULT_APP_ID = "50847ad7-fc7f-4c89-85a9-c5d7f91cc2c8"
    private const val PREFS_NAME = "onesignal_verification_prefs"
    private const val KEY_VERIFICATION_SHOWN = "has_shown_verification_dialog"

    private var initialized = false
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _showVerificationDialog = MutableStateFlow(false)
    val showVerificationDialog: StateFlow<Boolean> = _showVerificationDialog.asStateFlow()

    private val _subscriptionId = MutableStateFlow<String?>(null)
    val subscriptionId: StateFlow<String?> = _subscriptionId.asStateFlow()

    // Retained strongly at object scope so OneSignal's weak reference does not deallocate it
    private var retainedPushObserver: IPushSubscriptionObserver? = null

    /**
     * Initializes OneSignal notifications.
     */
    fun init(context: Context, appId: String = DEFAULT_APP_ID) {
        if (appId.isBlank()) {
            Log.d(TAG, "OneSignal App ID not provided. Awaiting user configuration.")
            return
        }

        try {
            // Set log level for debugging
            OneSignal.Debug.logLevel = LogLevel.VERBOSE

            // Initialize OneSignal
            OneSignal.initWithContext(context, appId)
            initialized = true
            Log.i(TAG, "OneSignal successfully initialized with App ID: $appId")

            // Register and retain push subscription observer
            setupPushSubscriptionObserver(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing OneSignal: ${e.message}", e)
        }
    }

    /**
     * Helper to determine if a push subscription ID is a real server-assigned value
     * and NOT a local placeholder (must not be empty and must not start with 'local-').
     */
    fun isRealSubscriptionId(id: String?): Boolean {
        return !id.isNullOrBlank() && !id.startsWith("local-")
    }

    private fun setupPushSubscriptionObserver(context: Context) {
        val observer = object : IPushSubscriptionObserver {
            override fun onPushSubscriptionChange(state: PushSubscriptionChangedState) {
                val currentId = state.current.id
                Log.d(TAG, "Push subscription changed. Current ID: $currentId")
                _subscriptionId.value = currentId
                if (isRealSubscriptionId(currentId)) {
                    evaluateAndTriggerVerificationDialog(context)
                }
            }
        }

        // Retain strongly
        retainedPushObserver = observer
        OneSignal.User.pushSubscription.addObserver(observer)

        // Evaluate immediately at observer-registration time in case already server-assigned
        val initialId = OneSignal.User.pushSubscription.id
        Log.d(TAG, "Initial push subscription ID: $initialId")
        _subscriptionId.value = initialId
        if (isRealSubscriptionId(initialId)) {
            evaluateAndTriggerVerificationDialog(context)
        }
    }

    private fun evaluateAndTriggerVerificationDialog(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val alreadyShown = prefs.getBoolean(KEY_VERIFICATION_SHOWN, false)
        if (!alreadyShown) {
            Log.i(TAG, "Server-assigned push subscription detected. Triggering verification dialog.")
            prefs.edit().putBoolean(KEY_VERIFICATION_SHOWN, true).apply()
            _showVerificationDialog.value = true
        }
    }

    /**
     * Called when the user clicks 'Got it' on the verification dialog.
     */
    fun onVerificationDismissed() {
        _showVerificationDialog.value = false
        requestNotificationPermission()
    }

    /**
     * Request push permission from OneSignal.
     */
    fun requestNotificationPermission() {
        scope.launch {
            try {
                Log.i(TAG, "Requesting OneSignal notification permission...")
                OneSignal.Notifications.requestPermission(true)
            } catch (e: Exception) {
                Log.e(TAG, "Error requesting notification permission: ${e.message}", e)
            }
        }
    }

    /**
     * Authenticated-user association using OneSignal v5 user identity.
     */
    fun login(userId: String) {
        if (userId.isNotBlank()) {
            try {
                Log.i(TAG, "Logging in OneSignal user: $userId")
                OneSignal.login(userId)
            } catch (e: Exception) {
                Log.e(TAG, "Error logging into OneSignal user identity", e)
            }
        }
    }

    /**
     * Securely disconnect user identity on logout.
     */
    fun logout() {
        try {
            Log.i(TAG, "Logging out OneSignal user identity")
            OneSignal.logout()
        } catch (e: Exception) {
            Log.e(TAG, "Error logging out of OneSignal user identity", e)
        }
    }

    fun isConfigured(): Boolean = initialized
}
