package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.data.NotificationEntity
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeoIconButton
import com.example.ui.theme.NeoTopBar
import com.example.ui.theme.StatusHigh
import com.example.ui.theme.TextGrey
import com.example.ui.theme.neoCard

@Composable
fun NotificationScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val notificationsList by viewModel.allNotifications.collectAsStateWithLifecycle()

    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val activeAccent = if (darkTheme) AccentCyan else Color(0xFF059669)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            NeoTopBar(
                title = "Notifications",
                onBackClick = onBack,
                actions = {
                    if (notificationsList.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.markAllNotificationsAsRead()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Mark all read",
                                tint = activeAccent
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.clearAllNotifications()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ClearAll,
                                contentDescription = "Clear all",
                                tint = TextGrey
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (notificationsList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .neoCard(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = "No notifications",
                            tint = activeAccent.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "All Caught Up!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You have no new alerts or notifications at this time.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGrey,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(notificationsList, key = { it.id }) { notification ->
                        NotificationCard(
                            notification = notification,
                            onMarkRead = {
                                viewModel.markNotificationAsRead(notification.id)
                            },
                            onDismiss = {
                                viewModel.dismissNotification(notification.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: NotificationEntity,
    onMarkRead: () -> Unit,
    onDismiss: () -> Unit
) {
    val cardAlpha = if (notification.isRead) 0.6f else 1.0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neoCard()
            .clickable { onMarkRead() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notification.type) {
                        "WARNING" -> Icons.Default.Warning
                        "ALERT" -> Icons.Default.Info
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = null,
                    tint = (when (notification.type) {
                        "WARNING" -> Color(0xFFFF9500)
                        "ALERT" -> StatusHigh
                        else -> AccentCyan
                    }).copy(alpha = cardAlpha)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = cardAlpha),
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = notification.time,
                        fontSize = 11.sp,
                        color = TextGrey.copy(alpha = cardAlpha),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = TextGrey.copy(alpha = cardAlpha)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = TextGrey.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
