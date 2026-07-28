package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeoTopBar
import com.example.ui.theme.neoCard
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

@Composable
fun FcmTokenScreen(onBack: () -> Unit) {
    var token by remember { mutableStateOf("Fetching token...") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                token = fcmToken
            } catch (e: Exception) {
                error = e.localizedMessage ?: "Failed to get token"
                token = "Failed"
            }
        }
    }

    Scaffold(
        topBar = {
            NeoTopBar(
                title = "FCM Token",
                onBackClick = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Your Device FCM Token",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoCard()
                    .padding(16.dp)
            ) {
                if (error != null) {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = token,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (token != "Fetching token..." && token != "Failed") {
                        clipboardManager.setText(AnnotatedString(token))
                        Toast.makeText(context, "Token copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = token != "Fetching token..." && token != "Failed"
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy Token")
            }
        }
    }
}
