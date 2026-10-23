package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ble.ConnectionState
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToScan: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToLiveTemp: () -> Unit = {},
    onNavigateToEnvironment: () -> Unit = {},
    onNavigateToLiquid: () -> Unit = {},
    onNavigateToFermentation: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    onNavigateToDiagnostics: () -> Unit = {}
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val reading by viewModel.latestReading.collectAsState()
    val preferredUnit by viewModel.preferredUnit.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()
    val lowThreshold by viewModel.lowTempThreshold.collectAsState()
    val highThreshold by viewModel.highTempThreshold.collectAsState()
    val syncStatus by viewModel.supabaseSyncStatus.collectAsState()
    val pendingRecords by viewModel.pendingOfflineRecords.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val notificationsList by viewModel.allNotifications.collectAsState()
    val unreadCount = notificationsList.count { !it.isRead }

    // Temperature display calculation
    val rawTempC = reading?.temp ?: 0.0
    val isConnected = connectionState == ConnectionState.CONNECTED
    val isAlert = reading?.isAlert == true || (isConnected && (rawTempC < lowThreshold || rawTempC > highThreshold))

    val displayTemp = if (preferredUnit == "F") {
        if (rawTempC > -50.0) String.format(Locale.US, "%.1f", (rawTempC * 9.0 / 5.0) + 32.0) else "--.-"
    } else {
        if (rawTempC > -50.0) String.format(Locale.US, "%.1f", rawTempC) else "--.-"
    }
    val unitSymbol = if (preferredUnit == "F") "°F" else "°C"

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .statusBarsPadding(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Header: Branding, Connection status, Cloud sync, Notification Bell
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FLUX",
                                color = FluxPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(FluxSurfaceElevated)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "TELEMETRY",
                                    color = FluxSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault()).format(Date()),
                            color = FluxTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Connection Indicator Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isConnected) StatusNormal.copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isConnected) StatusNormal else DangerRed
                            ),
                            modifier = Modifier.clickable { if (!isConnected) onNavigateToScan() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isConnected) StatusNormal else DangerRed)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isConnected) "BLE Live" else "Connect",
                                    color = if (isConnected) StatusNormal else DangerRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Notification Icon
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(containerColor = DangerRed) { Text(unreadCount.toString()) }
                                }
                            }
                        ) {
                            IconButton(onClick = onNavigateToNotification) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Alerts",
                                    tint = FluxTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 2. Cloud Sync Banner
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FluxSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, FluxBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = if (syncStatus == "Synced") FluxPrimary else FluxSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Supabase: $syncStatus",
                                color = FluxTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (pendingRecords > 0) {
                            Text(
                                text = "$pendingRecords buffered",
                                color = StatusLow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "Auto (5s)",
                                color = FluxTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // 3. Primary Temperature Hero Card
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = FluxSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isAlert) DangerRed else FluxPrimary.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp)
                    ) {
                        // Card Top Row: Active profile chip + C/F toggle pill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FluxPrimary.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = activeProfile.uppercase(),
                                        color = FluxPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (isAlert) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DangerRed.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "ALERT",
                                            color = DangerRed,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Quick Unit Switcher
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = FluxSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, FluxBorder)
                            ) {
                                Row(modifier = Modifier.padding(2.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (preferredUnit == "C") FluxPrimary else Color.Transparent)
                                            .clickable { viewModel.setUnit("C") }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "°C",
                                            color = if (preferredUnit == "C") Color.Black else FluxTextSecondary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (preferredUnit == "F") FluxPrimary else Color.Transparent)
                                            .clickable { viewModel.setUnit("F") }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "°F",
                                            color = if (preferredUnit == "F") Color.Black else FluxTextSecondary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Temperature Readout
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = if (isConnected) displayTemp else "--.-",
                                color = if (isAlert) DangerRed else FluxTextPrimary,
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-1).sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = unitSymbol,
                                color = if (isAlert) DangerRed else FluxPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Status subtitle
                        Text(
                            text = if (!isConnected) {
                                "Sensor disconnected • Connect BLE device"
                            } else if (isAlert) {
                                "⚠️ Outside configured limits ($lowThreshold - $highThreshold °C)"
                            } else {
                                "Optimal thermal state • DS18B20 Valid"
                            },
                            color = if (isAlert) DangerRed else FluxTextSecondary,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Session Min / Max / Avg stats bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(FluxSurfaceElevated)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val minVal = reading?.minTemp ?: 0.0
                            val maxVal = reading?.maxTemp ?: 0.0
                            val avgVal = reading?.avgTemp ?: 0.0

                            Column {
                                Text("SESSION MIN", color = FluxTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (isConnected && minVal > -50.0) String.format(Locale.US, "%.1f %s", minVal, unitSymbol) else "--",
                                    color = FluxSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text("AVERAGE", color = FluxTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (isConnected && avgVal > -50.0) String.format(Locale.US, "%.1f %s", avgVal, unitSymbol) else "--",
                                    color = FluxTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text("SESSION MAX", color = FluxTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (isConnected && maxVal > -50.0) String.format(Locale.US, "%.1f %s", maxVal, unitSymbol) else "--",
                                    color = DangerRed,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 4. Monitoring Profile Switcher Chips
            item {
                Column {
                    Text(
                        text = "MONITORING MODES",
                        color = FluxTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val profiles = listOf("Live Monitor", "Environment", "Liquid Probe", "Fermentation")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(profiles) { profile ->
                            val isSelected = activeProfile.equals(profile, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) FluxPrimary else FluxSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) FluxPrimary else FluxBorder
                                ),
                                modifier = Modifier.clickable {
                                    viewModel.setProfile(profile)
                                    when (profile) {
                                        "Live Monitor" -> onNavigateToLiveTemp()
                                        "Environment" -> onNavigateToEnvironment()
                                        "Liquid Probe" -> onNavigateToLiquid()
                                        "Fermentation" -> onNavigateToFermentation()
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (profile) {
                                            "Environment" -> Icons.Default.Eco
                                            "Liquid Probe" -> Icons.Default.WaterDrop
                                            "Fermentation" -> Icons.Default.Science
                                            else -> Icons.Default.Speed
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) Color.Black else FluxPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = profile,
                                        color = if (isSelected) Color.Black else FluxTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Historical Trend Bezier Chart
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = FluxSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, FluxBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TEMPERATURE TREND",
                                color = FluxTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Live Stream",
                                color = FluxPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom Bezier curve drawing
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            val w = size.width
                            val h = size.height

                            // Draw subtle grid lines
                            drawLine(
                                color = FluxBorder.copy(alpha = 0.5f),
                                start = androidx.compose.ui.geometry.Offset(0f, h * 0.25f),
                                end = androidx.compose.ui.geometry.Offset(w, h * 0.25f),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = FluxBorder.copy(alpha = 0.5f),
                                start = androidx.compose.ui.geometry.Offset(0f, h * 0.5f),
                                end = androidx.compose.ui.geometry.Offset(w, h * 0.5f),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = FluxBorder.copy(alpha = 0.5f),
                                start = androidx.compose.ui.geometry.Offset(0f, h * 0.75f),
                                end = androidx.compose.ui.geometry.Offset(w, h * 0.75f),
                                strokeWidth = 1f
                            )

                            // Draw smooth trend wave
                            val path = Path().apply {
                                moveTo(0f, h * 0.65f)
                                cubicTo(w * 0.25f, h * 0.40f, w * 0.45f, h * 0.75f, w * 0.70f, h * 0.35f)
                                cubicTo(w * 0.85f, h * 0.15f, w * 0.95f, h * 0.30f, w, h * 0.25f)
                            }

                            drawPath(
                                path = path,
                                color = FluxPrimary,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("10m ago", color = FluxTextSecondary, fontSize = 10.sp)
                            Text("5m ago", color = FluxTextSecondary, fontSize = 10.sp)
                            Text("Now", color = FluxPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 6. Quick Telemetry Actions Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Session Timer Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = FluxSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, FluxBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToStats() }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = FluxSecondary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Session Timer", color = FluxTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            val elapsedSec = reading?.elapsedSec ?: 0L
                            val h = elapsedSec / 3600
                            val m = (elapsedSec % 3600) / 60
                            val s = elapsedSec % 60
                            Text(
                                text = String.format(Locale.US, "%02d:%02d:%02d", h, m, s),
                                color = FluxSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Alerts Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = FluxSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, FluxBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToAlerts() }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = FluxPrimary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Alert Rules", color = FluxTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$lowThreshold - $highThreshold °C",
                                color = FluxPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Diagnostics Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = FluxSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, FluxBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToDiagnostics() }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(Icons.Default.Dns, contentDescription = null, tint = FluxTextSecondary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Diagnostics", color = FluxTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = reading?.firmware ?: "v2.0.0",
                                color = FluxTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
