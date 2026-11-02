package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.theme.*

@Composable
fun FluxAlertsScreen(
    viewModel: MainViewModel,
    onNavigateToHome: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToProfiles: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onNavigateToAlertSettings: () -> Unit = {}
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val colors = rememberFluxColors(isDarkTheme)
    val notifications by viewModel.allNotifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }

    val infiniteTransition = rememberInfiniteTransition(label = "alertsPulse")
    val bellPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (unreadCount > 0) 1.08f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bellPulse"
    )

    // Animated ringing bell wobble when unread notifications exist
    val bellWobbleAngle by infiniteTransition.animateFloat(
        initialValue = if (unreadCount > 0) -8f else 0f,
        targetValue = if (unreadCount > 0) 8f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(260, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bellWobble"
    )

    // Dynamic emergency breach pulse glow for unread alert cards
    val alertPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alertPulseAlpha"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            // Spacer to clear the fixed top header cleanly
            Spacer(modifier = Modifier.height(72.dp))

            // Responsive Title & Action Buttons Header Row
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isNarrow = maxWidth < 380.dp

                if (isNarrow) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Alerts",
                                color = colors.textPrimary,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Real-time hardware breaches & notifications",
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AlertHeaderActions(
                                unreadCount = unreadCount,
                                hasNotifications = notifications.isNotEmpty(),
                                colors = colors,
                                onConfig = onNavigateToAlertSettings,
                                onMarkAllRead = { viewModel.markAllNotificationsAsRead() },
                                onClearAll = { viewModel.clearAllNotifications() }
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Alerts",
                                color = colors.textPrimary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Stay informed about your device",
                                color = colors.textSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AlertHeaderActions(
                                unreadCount = unreadCount,
                                hasNotifications = notifications.isNotEmpty(),
                                colors = colors,
                                onConfig = onNavigateToAlertSettings,
                                onMarkAllRead = { viewModel.markAllNotificationsAsRead() },
                                onClearAll = { viewModel.clearAllNotifications() }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active Alerts Summary Banner Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(22.dp),
                        spotColor = Color(0x14000000)
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFFF9F5), Color(0xFFFFE8DC))
                        )
                    )
                    .border(
                        width = 0.5.dp,
                        color = Color(0xFFFFD9C7),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Orange Bell Icon with live breathing scale & ringing wobble
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .scale(bellPulseScale)
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
                            modifier = Modifier
                                .size(22.dp)
                                .graphicsLayer(rotationZ = bellWobbleAngle)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = unreadCount,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220)) togetherWith
                                fadeOut(animationSpec = tween(220))
                            },
                            label = "alertCount"
                        ) { count ->
                            Text(
                                text = if (count > 0) "$count Active Alert${if (count > 1) "s" else ""}"
                                else "All Systems Normal",
                                color = colors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (unreadCount > 0) "Hardware telemetry limits breached" else "No active warnings or threshold breaches",
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Dynamic alerts from Room DB
            if (notifications.isEmpty()) {
                // Clean empty-state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.cardBg)
                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = null,
                                tint = colors.textSecondary.copy(alpha = 0.45f),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No active alerts",
                            color = colors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "All systems normal • Alerts will appear here when telemetry breaches limits or hardware warnings occur.",
                            color = colors.textSecondary,
                            fontSize = 12.5.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 17.sp
                        )
                    }
                }
            } else {
                val sorted = remember(notifications) { notifications.sortedByDescending { it.id } }

                sorted.forEach { notif ->
                    val isAlert = notif.type.equals("ALERT", ignoreCase = true)
                    val isWarning = notif.type.equals("WARNING", ignoreCase = true)
                    val iconTint = when {
                        isAlert -> Color(0xFFEF4444)
                        isWarning -> FluxOrange
                        else -> Color(0xFF10B981)
                    }
                    val iconBg = when {
                        isAlert -> Color(0xFFFEE2E2)
                        isWarning -> Color(0xFFFFF2EB)
                        else -> Color(0xFFD1FAE5)
                    }
                    val pillBg = iconBg
                    val pillColor = iconTint
                    val pillText = when {
                        isAlert -> "Alert"
                        isWarning -> "Warning"
                        else -> "Info"
                    }

                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(250)) + expandVertically(),
                        exit = fadeOut(tween(200)) + shrinkVertically()
                    ) {
                        AlertRecordItem(
                            icon = if (isAlert) Icons.Filled.Thermostat
                            else if (isWarning) Icons.Outlined.AccessTime
                            else Icons.Filled.Notifications,
                            iconTint = iconTint,
                            iconBg = iconBg,
                            title = notif.title,
                            description = notif.message,
                            time = notif.time,
                            pillText = pillText,
                            pillBg = pillBg,
                            pillColor = pillColor,
                            isRead = notif.isRead,
                            isAlert = isAlert,
                            pulseAlpha = alertPulseAlpha,
                            colors = colors,
                            onDelete = { viewModel.dismissNotification(notif.id) }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
private fun AlertHeaderActions(
    unreadCount: Int,
    hasNotifications: Boolean,
    colors: FluxColors,
    onConfig: () -> Unit,
    onMarkAllRead: () -> Unit,
    onClearAll: () -> Unit
) {
    val configInteraction = remember { MutableInteractionSource() }
    val isConfigPressed by configInteraction.collectIsPressedAsState()
    val configScale by animateFloatAsState(
        targetValue = if (isConfigPressed) 0.94f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "configScale"
    )

    // Config Button
    Box(
        modifier = Modifier
            .scale(configScale)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF1F5F9))
            .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .clickable(interactionSource = configInteraction, indication = null) { onConfig() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Tune,
                contentDescription = "Alert Config",
                tint = FluxOrange,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Config",
                color = colors.textPrimary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    if (hasNotifications) {
        if (unreadCount > 0) {
            val readInteraction = remember { MutableInteractionSource() }
            val isReadPressed by readInteraction.collectIsPressedAsState()
            val readScale by animateFloatAsState(
                targetValue = if (isReadPressed) 0.94f else 1.0f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "readScale"
            )

            Box(
                modifier = Modifier
                    .scale(readScale)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9))
                    .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .clickable(interactionSource = readInteraction, indication = null) { onMarkAllRead() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.DoneAll,
                        contentDescription = "Mark all read",
                        tint = FluxOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Read All",
                        color = colors.textPrimary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        val clearInteraction = remember { MutableInteractionSource() }
        val isClearPressed by clearInteraction.collectIsPressedAsState()
        val clearScale by animateFloatAsState(
            targetValue = if (isClearPressed) 0.94f else 1.0f,
            animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "clearScale"
        )

        Box(
            modifier = Modifier
                .scale(clearScale)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFEE2E2))
                .border(0.5.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
                .clickable(interactionSource = clearInteraction, indication = null) { onClearAll() }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = "Clear all alerts",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(15.dp)
            )
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
    isRead: Boolean,
    isAlert: Boolean,
    pulseAlpha: Float,
    colors: FluxColors,
    onDelete: () -> Unit
) {
    val borderColor = if (!isRead) {
        if (isAlert) Color(0xFFEF4444).copy(alpha = pulseAlpha)
        else FluxOrange.copy(alpha = pulseAlpha)
    } else {
        colors.cardBorder
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (!isRead) 4.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (!isRead) borderColor.copy(alpha = 0.25f) else Color(0x0C000000)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(colors.cardBg)
            .border(
                width = if (!isRead) 1.4.dp else 0.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = colors.textSecondary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
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

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(pillBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pillText,
                    color = pillColor,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = colors.textSecondary.copy(alpha = 0.55f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
