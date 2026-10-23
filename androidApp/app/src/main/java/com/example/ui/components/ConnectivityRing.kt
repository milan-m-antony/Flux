package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connectivity.ConnectionType
import com.example.connectivity.FluxConnectivityState

/**
 * Dynamic Connectivity Ring component exactly reproducing the new reference design:
 * Single dynamic icon with animated arc, next to device name "Flux1" and "● Online/Offline" status.
 */
@Composable
fun ConnectivityRing(
    state: FluxConnectivityState,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    ringSize: Dp = 40.dp,
    strokeWidth: Dp = 3.2.dp
) {
    // Dynamic ring color matching signal level
    val ringColor = if (state.isOnline) {
        state.signalLevel.color
    } else {
        if (darkTheme) Color(0xFF64748B) else Color(0xFF94A3B8)
    }

    // Status label color (vivid green when online, matching screenshot)
    val statusColor = if (state.isOnline) {
        if (darkTheme) Color(0xFF00E676) else Color(0xFF00C853)
    } else {
        if (darkTheme) Color(0xFFEF4444) else Color(0xFFDC2626)
    }

    // Subtle track color
    val trackColor = if (darkTheme) Color(0xFF334155).copy(alpha = 0.85f) else Color(0xFFE2E8F0)

    // Animated progress sweep angle (smooth arc animation)
    val sweepAngleTarget = if (state.isOnline && state.signalPercent != null && state.signalPercent > 0) {
        (state.signalPercent / 100f) * 360f
    } else {
        0f
    }
    val animatedSweepAngle by animateFloatAsState(
        targetValue = sweepAngleTarget,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "SignalRingSweep"
    )

    // Determine correct icon for detected connection
    val iconVector = when (state.type) {
        ConnectionType.WIFI -> Icons.Filled.Wifi
        ConnectionType.BLUETOOTH -> Icons.Filled.Bluetooth
        ConnectionType.MOBILE_DATA -> Icons.Filled.SignalCellularAlt
        ConnectionType.HOTSPOT -> Icons.Filled.WifiTethering
        ConnectionType.NONE -> Icons.Outlined.WifiOff
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Single Circular Ring with dynamic animated icon
        Box(
            modifier = Modifier.size(ringSize),
            contentAlignment = Alignment.Center
        ) {
            // Background track + dynamic progress arc
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePx = strokeWidth.toPx()
                val diameter = size.minDimension - strokePx
                val arcTopLeft = Offset(strokePx / 2f, strokePx / 2f)
                val arcSize = Size(diameter, diameter)

                // Circular background track
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // Animated glowing progress arc (starts at 270 deg - 12 o'clock)
                if (animatedSweepAngle > 0f) {
                    drawArc(
                        color = ringColor,
                        startAngle = 270f,
                        sweepAngle = animatedSweepAngle,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }
            }

            // Dynamically animated single icon inside ring with smooth fade & scale animation
            AnimatedContent(
                targetState = iconVector,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220, delayMillis = 50)) + scaleIn(initialScale = 0.80f, animationSpec = tween(220)))
                        .togetherWith(fadeOut(animationSpec = tween(160)) + scaleOut(targetScale = 0.80f, animationSpec = tween(160)))
                },
                label = "TransportIconAnimation"
            ) { targetIcon ->
                Icon(
                    imageVector = targetIcon,
                    contentDescription = state.type.label,
                    tint = if (darkTheme) Color.White else Color(0xFF0F172A),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 2. Device Name & Status Column: Flux1 + ● Online
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            // Device name: "Flux1"
            Text(
                text = state.deviceName.ifBlank { "Flux1" },
                color = if (darkTheme) Color.White else Color(0xFF0F172A),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(1.5.dp))

            // Status: ● Online / Offline
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = if (state.isOnline) "Online" else "Offline",
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}
