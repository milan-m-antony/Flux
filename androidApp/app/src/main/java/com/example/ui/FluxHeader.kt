package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun FluxHeader(
    modifier: Modifier = Modifier,
    isOnline: Boolean = true,
    isBleConnected: Boolean = true,
    isWifiConnected: Boolean = true,
    darkTheme: Boolean = false,
    hasUnreadAlerts: Boolean = true,
    onNotificationClick: () -> Unit = {}
) {
    // Pulse animation for online indicator & flame
    val infiniteTransition = rememberInfiniteTransition(label = "HeaderPulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "DotAlpha"
    )
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FlameScale"
    )

    // Reduced height & more curved (full capsule) header container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (darkTheme) 4.dp else 6.dp,
                shape = RoundedCornerShape(percent = 50),
                spotColor = if (darkTheme) Color(0x30000000) else Color(0x14000000),
                ambientColor = Color(0x0A000000)
            )
            .background(
                color = if (darkTheme) Color(0xFF1E293B).copy(alpha = 0.96f) else Color.White.copy(alpha = 0.96f),
                shape = RoundedCornerShape(percent = 50)
            )
            .border(
                width = 1.dp,
                color = if (darkTheme) Color(0xFF334155) else Color(0xFFF1F5F9),
                shape = RoundedCornerShape(percent = 50)
            )
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Flux Flame Logo with subtle breathing animation
            Image(
                painter = painterResource(id = R.drawable.ic_flux_flame),
                contentDescription = "Flux Logo",
                modifier = Modifier
                    .size(30.dp)
                    .scale(if (isOnline) flameScale else 1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 2. Title & Subtitle
            Column {
                Text(
                    text = "Flux",
                    color = if (darkTheme) Color(0xFFF8FAFC) else FluxTextDark,
                    fontSize = 17.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Smart Telemetry System",
                    color = if (darkTheme) Color(0xFF94A3B8) else FluxTextMuted,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 3. Device Indicator with device image, BLE & WiFi icons, and Online/Offline status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular device image
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (darkTheme) Color(0xFF334155) else Color(0xFFF8FAFC))
                        .border(0.5.dp, if (darkTheme) Color(0xFF475569) else Color(0xFFE2E8F0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.device_image),
                        contentDescription = "Flux Device",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(7.dp))

                Column {
                    // Small connection mode icons: Bluetooth and WiFi
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bluetooth,
                            contentDescription = "Bluetooth Status",
                            tint = if (isBleConnected) Color(0xFF38BDF8) else (if (darkTheme) Color(0xFF475569) else Color(0xFFCBD5E1)),
                            modifier = Modifier.size(11.dp)
                        )
                        Icon(
                            imageVector = Icons.Filled.Wifi,
                            contentDescription = "WiFi Status",
                            tint = if (isWifiConnected) Color(0xFF22C55E) else (if (darkTheme) Color(0xFF475569) else Color(0xFFCBD5E1)),
                            modifier = Modifier.size(11.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(1.dp))

                    // Status Indicator: Online or Offline (No ESP32 text)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isOnline) FluxGreenOnline.copy(alpha = dotAlpha) else Color(0xFF94A3B8)
                                )
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isOnline) "Online" else "Offline",
                            color = if (isOnline) {
                                if (darkTheme) Color(0xFF4ADE80) else Color(0xFF16A34A)
                            } else {
                                if (darkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 4. Circular Notification Bell Button with Badge
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .shadow(
                        elevation = 2.dp,
                        shape = CircleShape,
                        spotColor = Color(0x12000000)
                    )
                    .clip(CircleShape)
                    .background(if (darkTheme) Color(0xFF334155) else Color.White)
                    .border(0.5.dp, if (darkTheme) Color(0xFF475569) else Color(0xFFE2E8F0), CircleShape)
                    .clickable { onNotificationClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Alerts",
                    tint = if (darkTheme) Color(0xFFF8FAFC) else FluxTextDark,
                    modifier = Modifier.size(17.dp)
                )

                if (hasUnreadAlerts) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 6.dp, end = 6.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(FluxOrange)
                    )
                }
            }
        }
    }
}
