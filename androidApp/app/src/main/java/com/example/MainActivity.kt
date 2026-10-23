package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.FluxApp
import com.example.ui.theme.MyApplicationTheme
import com.example.util.OneSignalManager

class MainActivity : FragmentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val showOneSignalDialog by OneSignalManager.showVerificationDialog.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FluxApp(viewModel)

                    // OneSignal Push Subscription Verification Dialog (Mandatory AI Prompt Specification)
                    if (showOneSignalDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                OneSignalManager.onVerificationDismissed()
                            },
                            title = {
                                Text(text = "Your OneSignal SDK integration is complete!")
                            },
                            text = {
                                Text(
                                    text = "You can now send Push Notifications & In-App Messages through OneSignal. Tap below to enable push notifications."
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        OneSignalManager.onVerificationDismissed()
                                    }
                                ) {
                                    Text("Got it")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
