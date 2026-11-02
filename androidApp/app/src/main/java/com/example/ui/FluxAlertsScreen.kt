package com.example.ui

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val activeProfile by viewModel.activeProfile.collectAsState()
    val colors = rememberFluxColors(darkTheme = false, activeProfile = activeProfile)
    val notifications by viewModel.allNotifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }

    val infiniteTransition = rememberInfiniteTransition(label = "alertsPulse")
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
            // Clearance below fixed header
            Spacer(modifier = Modifier.height(86.dp))

            // Top Segmented Curved Action Bar (Config / Read All / Delete)
            AlertCurvedActionBar(
                unreadCount = unreadCount,
                notificationsCount = notifications.size,
                colors = colors,
                onConfig = onNavigateToAlertSettings,
                onMarkAllRead = { viewModel.markAllNotificationsAsRead() },
                onClearAll = { viewModel.clearAllNotifications() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic alerts from Room DB
            if (notifications.isEmpty()) {
                // Clean empty-state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(22.dp), spotColor = Color(0x0C000000))
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.cardBg)
                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                        .padding(horizontal = 20.dp, vertical = 34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(colors.pillBg),
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
                            text = "Alerts will appear here when telemetry breaches limits or hardware warnings occur.",
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
                            onMarkRead = { viewModel.markNotificationAsRead(notif.id) },
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
private fun AlertCurvedActionBar(
    unreadCount: Int,
    notificationsCount: Int,
    colors: FluxColors,
    onConfig: () -> Unit,
    onMarkAllRead: () -> Unit,
    onClearAll: () -> Unit
) {
    val context = LocalContext.current
    val hasUnread = unreadCount > 0
    val hasNotifications = notificationsCount > 0

    val configInteraction = remember { MutableInteractionSource() }
    val isConfigPressed by configInteraction.collectIsPressedAsState()
    val configScale by animateFloatAsState(
        targetValue = if (isConfigPressed) 0.94f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "configScale"
    )

    val readInteraction = remember { MutableInteractionSource() }
    val isReadPressed by readInteraction.collectIsPressedAsState()
    val readScale by animateFloatAsState(
        targetValue = if (isReadPressed) 0.94f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "readScale"
    )

    val deleteInteraction = remember { MutableInteractionSource() }
    val isDeletePressed by deleteInteraction.collectIsPressedAsState()
    val deleteScale by animateFloatAsState(
        targetValue = if (isDeletePressed) 0.94f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "deleteScale"
    )

    // Segmented Curved Container (matches FluxHistoryScreen curved top tabs)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(26.dp), spotColor = Color(0x10000000))
            .background(colors.cardBg, RoundedCornerShape(26.dp))
            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(26.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Config Action Segment
        Row(
            modifier = Modifier
                .weight(1f)
                .scale(configScale)
                .clip(RoundedCornerShape(22.dp))
                .background(colors.pillBg)
                .clickable(interactionSource = configInteraction, indication = null) {
                    onConfig()
                }
                .padding(vertical = 9.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Tune,
                contentDescription = "Alert Config",
                tint = colors.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Config",
                color = colors.textPrimary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // 2. Read All Action Segment
        Row(
            modifier = Modifier
                .weight(1.2f)
                .scale(readScale)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    if (hasUnread) colors.primary.copy(alpha = 0.12f)
                    else Color.Transparent
                )
                .clickable(interactionSource = readInteraction, indication = null) {
                    if (hasUnread) {
                        onMarkAllRead()
                        Toast.makeText(context, "All alerts marked as read", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "No unread alerts", Toast.LENGTH_SHORT).show()
                    }
                }
                .padding(vertical = 9.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.DoneAll,
                contentDescription = "Mark all read",
                tint = if (hasUnread) colors.primary else colors.textSecondary.copy(alpha = 0.45f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = if (hasUnread) "Read All ($unreadCount)" else "Read All",
                color = if (hasUnread) colors.primary else colors.textSecondary.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // 3. Delete Action Segment
        Row(
            modifier = Modifier
                .weight(1f)
                .scale(deleteScale)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    if (hasNotifications) Color(0xFFFEE2E2).copy(alpha = 0.75f)
                    else Color.Transparent
                )
                .clickable(interactionSource = deleteInteraction, indication = null) {
                    if (hasNotifications) {
                        onClearAll()
                        Toast.makeText(context, "Alert history cleared", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "No alerts to delete", Toast.LENGTH_SHORT).show()
                    }
                }
                .padding(vertical = 9.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = "Clear all alerts",
                tint = if (hasNotifications) Color(0xFFEF4444) else colors.textSecondary.copy(alpha = 0.45f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "Delete",
                color = if (hasNotifications) Color(0xFFDC2626) else colors.textSecondary.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontWeight = if (hasNotifications) FontWeight.Bold else FontWeight.SemiBold
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
    onMarkRead: () -> Unit,
    onDelete: () -> Unit
) {
    val borderColor = if (!isRead) {
        if (isAlert) Color(0xFFEF4444).copy(alpha = pulseAlpha)
        else colors.primary.copy(alpha = pulseAlpha)
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
                width = if (!isRead) 1.2.dp else 0.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable {
                if (!isRead) {
                    onMarkRead()
                }
            }
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
