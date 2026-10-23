package com.example.ui

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
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.theme.*

@Composable
fun FluxAlertsScreen(
    viewModel: MainViewModel,
    onNotificationClick: () -> Unit = {}
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val isOnline = connectionState == com.example.ble.ConnectionState.CONNECTED || true

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Shared Flux Header
            FluxHeader(
                isOnline = isOnline,
                hasUnreadAlerts = true,
                onNotificationClick = onNotificationClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Title & Subtitle
            Text(
                text = "Alerts",
                color = FluxTextDark,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Stay informed about your device",
                color = FluxTextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. "2 Active Alerts" Summary Banner Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(26.dp),
                        spotColor = Color(0x14000000)
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFFF9F5), Color(0xFFFFE8DC))
                        )
                    )
                    .border(0.5.dp, Color(0xFFFFD9C7), RoundedCornerShape(26.dp))
                    .clickable { /* expand or view active alerts */ }
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Orange Bell Icon
                    Box(
                        modifier = Modifier
                            .size(52.dp)
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
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "2 Active Alerts",
                            color = FluxTextDark,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Your device needs attention",
                            color = FluxTextMuted,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = FluxTextDark,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 4. Group: Today
            Text(
                text = "Today",
                color = FluxTextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Alert 1: High Temperature
            AlertRecordItem(
                icon = Icons.Filled.Thermostat,
                iconTint = Color(0xFFEF4444),
                iconBg = Color(0xFFFEE2E2),
                title = "High Temperature",
                description = "Temperature reached 34.2°C",
                time = "11:00 AM",
                pillText = "34.2°C",
                pillBg = Color(0xFFFEE2E2),
                pillColor = Color(0xFFDC2626)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Alert 2: Temperature Back to Normal
            AlertRecordItem(
                icon = Icons.Filled.Thermostat,
                iconTint = FluxOrange,
                iconBg = Color(0xFFFFF2EB),
                title = "Temperature Back to Normal",
                description = "Temperature is now within range",
                time = "11:25 AM",
                pillText = "28.1°C",
                pillBg = Color(0xFFFFF2EB),
                pillColor = Color(0xFFEA580C)
            )

            Spacer(modifier = Modifier.height(22.dp))

            // 5. Group: Yesterday
            Text(
                text = "Yesterday",
                color = FluxTextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Alert 3: Low Temperature
            AlertRecordItem(
                icon = Icons.Filled.Thermostat,
                iconTint = Color(0xFF3B82F6),
                iconBg = Color(0xFFDBEAFE),
                title = "Low Temperature",
                description = "Temperature dropped to 15.8°C",
                time = "08:24 AM",
                pillText = "15.8°C",
                pillBg = Color(0xFFDBEAFE),
                pillColor = Color(0xFF2563EB)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Alert 4: Device Offline
            AlertRecordItem(
                icon = Icons.Outlined.WifiOff,
                iconTint = Color(0xFF475569),
                iconBg = Color(0xFFF1F5F9),
                title = "Device Offline",
                description = "ESP32 lost connection",
                time = "09:15 PM",
                pillText = "Offline",
                pillBg = Color(0xFFF1F5F9),
                pillColor = Color(0xFF475569)
            )

            Spacer(modifier = Modifier.height(22.dp))

            // 6. Group: 20 Oct 2026
            Text(
                text = "20 Oct 2026",
                color = FluxTextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Alert 5: Temperature Back to Normal
            AlertRecordItem(
                icon = Icons.Filled.Thermostat,
                iconTint = FluxOrange,
                iconBg = Color(0xFFFFF2EB),
                title = "Temperature Back to Normal",
                description = "Temperature is now within range",
                time = "10:10 PM",
                pillText = "26.7°C",
                pillBg = Color(0xFFFFF2EB),
                pillColor = Color(0xFFEA580C)
            )

            Spacer(modifier = Modifier.height(110.dp))
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
    pillColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(20.dp), spotColor = Color(0x0E000000))
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(0.5.dp, Color(0xFFF1F5F9), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular status icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Title & description + timestamp
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = FluxTextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = FluxTextMuted,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = time,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right Value / Status Pill
            Box(
                modifier = Modifier
                    .background(pillBg, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pillText,
                    color = pillColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
