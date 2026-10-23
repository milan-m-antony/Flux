package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.connectivity.FluxConnectivityState
import com.example.ui.components.FluxSignalMeter
import com.example.ui.theme.*

@Composable
fun FluxHeader(
    modifier: Modifier = Modifier,
    connectivityState: FluxConnectivityState = FluxConnectivityState(),
    isOnline: Boolean = true,
    isBleConnected: Boolean = true,
    isWifiConnected: Boolean = true,
    darkTheme: Boolean = false,
    showNotificationBell: Boolean = false,
    hasUnreadAlerts: Boolean = false,
    onNotificationClick: () -> Unit = {},
    onDeviceClick: () -> Unit = {}
) {
    // Pulse animation for online indicator & flame
    val infiniteTransition = rememberInfiniteTransition(label = "HeaderAnimations")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FlameScale"
    )

    // Glowing halo animation for the status dot
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloScale"
    )
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloAlpha"
    )
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "DotScale"
    )

    val effectiveOnline = connectivityState.isOnline && isOnline
    val statusColor = if (effectiveOnline) {
        if (darkTheme) Color(0xFF00E676) else Color(0xFF00C853)
    } else {
        if (darkTheme) Color(0xFFEF4444) else Color(0xFFDC2626)
    }

    // Reduced height & curved capsule header container matching reference design exactly
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (darkTheme) 3.5.dp else 4.5.dp,
                shape = RoundedCornerShape(percent = 50),
                spotColor = if (darkTheme) Color(0x30000000) else Color(0x14000000),
                ambientColor = Color(0x0A000000)
            )
            .background(
                color = if (darkTheme) Color(0xFF1E293B).copy(alpha = 0.98f) else Color.White.copy(alpha = 0.98f),
                shape = RoundedCornerShape(percent = 50)
            )
            .border(
                width = 1.dp,
                color = if (darkTheme) Color(0xFF334155) else Color(0xFFF1F5F9),
                shape = RoundedCornerShape(percent = 50)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Left Section: Flux Flame Logo + Title + Subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_flux_flame),
                    contentDescription = "Flux Logo",
                    modifier = Modifier
                        .size(28.dp)
                        .scale(if (effectiveOnline) flameScale else 1f)
                )

                Spacer(modifier = Modifier.width(7.dp))

                Column {
                    Text(
                        text = "Flux",
                        color = if (darkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        text = "Smart Telemetry System",
                        color = if (darkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                        fontSize = 8.8.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.1).sp
                    )
                }
            }

            // Balanced spacing before center divider
            Spacer(modifier = Modifier.weight(1f))

            // 2. Center Divider Bar: |
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(23.dp)
                    .background(if (darkTheme) Color(0xFF334155) else Color(0xFFE2E8F0))
            )

            // Balanced spacing after center divider
            Spacer(modifier = Modifier.weight(1f))

            // 3. Right Section: Clickable Interactive Device Info ("Flux1" + ● Offline/Online) + 4-Bar Signal Meter
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDeviceClick
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Device name + Status (with minimized gap between Flux1 and Offline)
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = connectivityState.deviceName.ifBlank { "Flux1" },
                        color = if (darkTheme) Color.White else Color(0xFF0F172A),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(0.5.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Animated pulsing & glowing status dot
                        Box(
                            modifier = Modifier.size(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Outer glowing halo
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .scale(haloScale)
                                    .alpha(haloAlpha)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            // Core solid dot
                            Box(
                                modifier = Modifier
                                    .size(5.5.dp)
                                    .scale(dotScale)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                        }

                        Spacer(modifier = Modifier.width(3.5.dp))

                        Text(
                            text = if (effectiveOnline) "Online" else "Offline",
                            color = statusColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(11.dp))

                // Compact 4-Bar Signal Meter
                FluxSignalMeter(
                    state = connectivityState,
                    darkTheme = darkTheme,
                    isOnline = effectiveOnline,
                    barHeights = listOf(6.5.dp, 10.5.dp, 14.5.dp, 18.5.dp),
                    barWidth = 3.8.dp,
                    barSpacing = 2.dp,
                    fontSize = 8.5.sp
                )
            }

            // Optional Notification Bell Button (hidden by default to match reference design)
            if (showNotificationBell) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
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
                        modifier = Modifier.size(16.dp)
                    )

                    if (hasUnreadAlerts) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 5.dp, end = 5.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(FluxOrange)
                        )
                    }
                }
            }
        }
    }
}

