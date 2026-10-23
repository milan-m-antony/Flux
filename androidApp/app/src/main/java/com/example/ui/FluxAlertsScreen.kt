package com.example.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.MainViewModel
import com.example.R
import com.example.ui.theme.*

@Composable
fun FluxAlertsScreen(
    viewModel: MainViewModel,
    onNavigateToHome: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToProfiles: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val darkTheme by viewModel.isDarkTheme.collectAsState()
    val colors = rememberFluxColors(darkTheme)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            // Spacer to clear the fixed top header cleanly
            Spacer(modifier = Modifier.height(68.dp))

            // Title & Subtitle
            Text(
                        text = "Alerts",
                        color = colors.textPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Stay informed about your device",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Test FCM Push Notification Action Card
                    TestFcmNotificationCard(
                        darkTheme = darkTheme,
                        onTestTriggered = {
                            // 1. Android Toast
                            Toast.makeText(
                                context,
                                "FCM Alert Triggered! Check your notification shade.",
                                Toast.LENGTH_SHORT
                            ).show()

                            // 2. Real System Heads-up Notification
                            sendRealFcmAlertNotification(
                                context = context,
                                title = "🔥 Flux FCM Alert: High Temperature",
                                message = "Threshold breach: DS18B20 reading 41.8°C exceeds high limit (38.0°C)"
                            )

                            // 3. Add to ViewModel internal notification list
                            viewModel.createNotification(
                                title = "FCM High Temperature Alert",
                                message = "Simulated high temperature alert (41.8°C)",
                                type = "HIGH"
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // "2 Active Alerts" Summary Banner Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 5.dp,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = if (darkTheme) Color(0x30000000) else Color(0x14000000)
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = if (darkTheme) {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF2A2420), Color(0xFF1E1E28))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFFF9F5), Color(0xFFFFE8DC))
                                    )
                                }
                            )
                            .border(
                                width = 0.5.dp,
                                color = if (darkTheme) FluxOrange.copy(alpha = 0.35f) else Color(0xFFFFD9C7),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Orange Bell Icon
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .shadow(4.dp, CircleShape, spotColor = FluxOrange.copy(alpha = 0.4f))
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(FluxOrangeGradientStart, FluxOrangeGradientEnd)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "2 Active Alerts",
                                    color = colors.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Your device needs attention",
                                    color = colors.textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // FCM Push Notification Simulator Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(20.dp),
                                spotColor = if (darkTheme) Color(0x30000000) else Color(0x10000000)
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.cardBg)
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (darkTheme) Color(0xFF1E293B) else Color(0xFFFFF7ED)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.NotificationsActive,
                                    contentDescription = null,
                                    tint = FluxOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "FCM Push Simulator",
                                    color = colors.textPrimary,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Test how push alerts appear in-app & tray",
                                    color = colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .shadow(3.dp, RoundedCornerShape(12.dp), spotColor = FluxOrange.copy(alpha = 0.4f))
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            listOf(FluxOrangeGradientStart, FluxOrangeGradientEnd)
                                        )
                                    )
                                    .clickable {
                                        Toast.makeText(context, "FCM Alert Received! Check notification shade.", Toast.LENGTH_SHORT).show()
                                        sendRealFcmAlertNotification(
                                            context,
                                            "Critical Alert: Flux ESP32",
                                            "Telemetry threshold exceeded: 35.8°C at ${java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())}"
                                        )
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Test FCM",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Group: Today
                    Text(
                        text = "Today",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Alert 1: High Temperature
                    AlertRecordItem(
                        icon = Icons.Filled.Thermostat,
                        iconTint = Color(0xFFEF4444),
                        iconBg = if (darkTheme) Color(0xFF451A1A) else Color(0xFFFEE2E2),
                        title = "High Temperature",
                        description = "Temperature reached 34.2°C",
                        time = "11:00 AM",
                        pillText = "34.2°C",
                        pillBg = if (darkTheme) Color(0xFF451A1A) else Color(0xFFFEE2E2),
                        pillColor = Color(0xFFEF4444),
                        darkTheme = darkTheme,
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Alert 2: Temperature Back to Normal
                    AlertRecordItem(
                        icon = Icons.Filled.Thermostat,
                        iconTint = FluxOrange,
                        iconBg = if (darkTheme) Color(0xFF332014) else Color(0xFFFFF2EB),
                        title = "Temperature Back to Normal",
                        description = "Temperature is now within range",
                        time = "11:25 AM",
                        pillText = "28.1°C",
                        pillBg = if (darkTheme) Color(0xFF332014) else Color(0xFFFFF2EB),
                        pillColor = FluxOrange,
                        darkTheme = darkTheme,
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Group: Yesterday
                    Text(
                        text = "Yesterday",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Alert 3: Low Temperature
                    AlertRecordItem(
                        icon = Icons.Filled.Thermostat,
                        iconTint = Color(0xFF3B82F6),
                        iconBg = if (darkTheme) Color(0xFF172554) else Color(0xFFDBEAFE),
                        title = "Low Temperature",
                        description = "Temperature dropped to 15.8°C",
                        time = "08:24 AM",
                        pillText = "15.8°C",
                        pillBg = if (darkTheme) Color(0xFF172554) else Color(0xFFDBEAFE),
                        pillColor = Color(0xFF3B82F6),
                        darkTheme = darkTheme,
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Alert 4: Device Offline
                    AlertRecordItem(
                        icon = Icons.Outlined.WifiOff,
                        iconTint = colors.textSecondary,
                        iconBg = if (darkTheme) Color(0xFF334155) else Color(0xFFF1F5F9),
                        title = "Device Offline",
                        description = "No signal received for 10 minutes",
                        time = "04:12 PM",
                        pillText = "Offline",
                        pillBg = if (darkTheme) Color(0xFF334155) else Color(0xFFF1F5F9),
                        pillColor = colors.textSecondary,
                        darkTheme = darkTheme,
                        colors = colors
                    )

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
private fun TestFcmNotificationCard(
    darkTheme: Boolean,
    onTestTriggered: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "FcmBtnScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Color(0x14000000)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(if (darkTheme) Color(0xFF1E293B) else Color.White)
            .border(
                width = 1.dp,
                color = if (darkTheme) Color(0xFF334155) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable {
                isPressed = true
                onTestTriggered()
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        LaunchedEffect(isPressed) {
            if (isPressed) {
                kotlinx.coroutines.delay(120)
                isPressed = false
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(FluxOrangeGradientStart, FluxOrangeGradientEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Test FCM Push Notification",
                    color = if (darkTheme) Color(0xFFF8FAFC) else FluxTextDark,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Trigger real Android heads-up banner & toast",
                    color = if (darkTheme) Color(0xFF94A3B8) else FluxTextMuted,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Test Button Capsule
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(FluxOrangeGradientStart, FluxOrangeGradientEnd)
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Test",
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AlertRecordItem(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    description: String,
    time: String,
    pillText: String,
    pillBg: Color,
    pillColor: Color,
    darkTheme: Boolean,
    colors: FluxColors
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (darkTheme) Color(0x30000000) else Color(0x0C000000)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(colors.cardBg)
            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = colors.textPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = colors.textSecondary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = time,
                        color = colors.textSecondary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(pillBg)
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pillText,
                    color = pillColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun sendRealFcmAlertNotification(context: Context, title: String, message: String) {
    val channelId = "default_channel_id"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Flux Default Channel",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Flux Telemetry and Alert notifications"
            enableLights(true)
            lightColor = android.graphics.Color.RED
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(title)
        .setContentText(message)
        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

    notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
}
