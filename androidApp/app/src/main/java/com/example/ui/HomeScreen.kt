package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ble.ConnectionState
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.StatusHigh
import com.example.ui.theme.StatusLow
import com.example.ui.theme.StatusNormal
import com.example.ui.theme.TextGrey
import com.example.ui.theme.neoCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel, onNavigateToScan: () -> Unit, onNavigateToProfile: () -> Unit, onNavigateToNotification: () -> Unit) {
    val connectionState by viewModel.connectionState.collectAsState()
    val reading by viewModel.latestReading.collectAsState()
    
    val lowThreshold by viewModel.lowAlertThreshold.collectAsState()
    val highThreshold by viewModel.highAlertThreshold.collectAsState()
    
    val userName by viewModel.userName.collectAsState()
    val userPhotoUrl by viewModel.userPhotoUrl.collectAsState()
    
    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val activeAccent = if (darkTheme) AccentCyan else Color(0xFF059669)
    
    var showProfileDialog by remember { mutableStateOf(false) }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Profile") },
            text = { Text("User: $userName\nDevice: ${if(connectionState == ConnectionState.CONNECTED) "Connected" else "Disconnected"}") },
            confirmButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            // Top Bar
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
                    .height(80.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Badge
                    Row(
                        modifier = Modifier
                            .clickable { onNavigateToProfile() }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (darkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (userPhotoUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = userPhotoUrl,
                                    contentDescription = "Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize().padding(8.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                userName, 
                                color = MaterialTheme.colorScheme.onSurface, 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 18.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (connectionState == ConnectionState.CONNECTED) Icons.Default.Bluetooth else Icons.Default.BluetoothDisabled,
                                    contentDescription = null,
                                    tint = if (connectionState == ConnectionState.CONNECTED) StatusNormal else TextGrey,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (connectionState == ConnectionState.CONNECTED) StatusNormal else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (connectionState == ConnectionState.CONNECTED) "Device Connected" else "Device Disconnected", 
                                    color = if (connectionState == ConnectionState.CONNECTED) StatusNormal else Color.Red, 
                                    fontSize = 12.sp, 
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
    
                    val notificationsList by viewModel.allNotifications.collectAsState()
                    val unreadCount = notificationsList.filter { !it.isRead }.size
                    
                    val notificationContent = @Composable {
                        IconButton(
                            onClick = { onNavigateToNotification() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Filled.Notifications, 
                                contentDescription = "Alerts", 
                                tint = MaterialTheme.colorScheme.onSurface, 
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    if (unreadCount > 0) {
                        BadgedBox(
                            badge = { Badge { Text(unreadCount.toString()) } },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            notificationContent()
                        }
                    } else {
                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            notificationContent()
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .padding(horizontal = 24.dp)
            ) {
                // Spacer for Top Bar
                Spacer(modifier = Modifier.statusBarsPadding().height(96.dp))

            if (connectionState != ConnectionState.CONNECTED) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .neoCard(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BluetoothDisabled,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Sensor Disconnected", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Connect your Na+ device to start tracking.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(32.dp))
                        com.example.ui.theme.NeoButton(
                            onClick = onNavigateToScan,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Connect Device", fontSize = MaterialTheme.typography.titleMedium.fontSize, color = activeAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                val naValue = reading?.na ?: 0.0
                val healthSummary by viewModel.healthSummary.collectAsState()
                
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    item {
                        // Header Section
                        Text(
                            text = SimpleDateFormat("EEE, dd MMMM", Locale.getDefault()).format(Date()),
                            color = activeAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daily Status",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Value Display
                        Box(modifier = Modifier.fillMaxWidth().neoCard().padding(24.dp)) {
                            Column {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = if (naValue < 0) "---" else String.format(Locale.getDefault(), "%.1f", naValue),
                                        fontSize = 64.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 64.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "mmol/L",
                                        fontSize = 18.sp,
                                        color = TextGrey,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                }
                                Text(
                                    text = if (naValue < 0) "Waiting for sensor" else if (naValue in lowThreshold..highThreshold) "Status: Normal" else if (naValue < lowThreshold) "Status: Low" else "Status: High",
                                    color = if (naValue < 0) TextGrey else if (naValue in lowThreshold..highThreshold) StatusNormal else if (naValue < lowThreshold) StatusLow else StatusHigh,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Last updated: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}", color = TextGrey, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Chart Area
                        Text("Weekly Trend", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .neoCard()
                                .padding(16.dp)
                        ) {
                            val historyReadings by viewModel.historyReadings.collectAsState()
                            val last10 = historyReadings.take(10).reversed()
                            
                            if (last10.size > 1) {
                                // Existing canvas logic
                                androidx.compose.foundation.Canvas(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    val maxNa = (last10.maxOfOrNull { it.naValue } ?: 150.0).coerceAtLeast(highThreshold)
                                    val minNa = (last10.minOfOrNull { it.naValue } ?: 130.0).coerceAtMost(lowThreshold)
                                    val range = (maxNa - minNa).coerceAtLeast(10.0)
                                    
                                    val stepX = size.width / (last10.size - 1).coerceAtLeast(1)
                                    val path = androidx.compose.ui.graphics.Path()
                                    
                                    val points = mutableListOf<androidx.compose.ui.geometry.Offset>()
                                    
                                    last10.forEachIndexed { index, r ->
                                        val x = index * stepX
                                        val y = size.height - ((r.naValue - minNa) / range * size.height).toFloat()
                                        points.add(androidx.compose.ui.geometry.Offset(x, y))
                                    }
                                    
                                    if (points.isNotEmpty()) {
                                        path.moveTo(points.first().x, points.first().y)
                                        for (i in 0 until points.size - 1) {
                                            val p0 = points[i]
                                            val p1 = points[i + 1]
                                            val cx = (p0.x + p1.x) / 2
                                            path.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                                        }
                                    }
                                    
                                    drawPath(
                                        path = path,
                                        color = activeAccent,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                                            width = 4.dp.toPx(), 
                                            cap = StrokeCap.Round, 
                                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                                        )
                                    )
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Not enough data", color = TextGrey)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Health Insight Card
                        Box(modifier = Modifier.fillMaxWidth().neoCard().padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(48.dp).background(activeAccent.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.GridView, contentDescription = null, tint = activeAccent)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Health Insight", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                    Text("Your sodium levels are stable. Maintain your water intake for optimal performance.", color = TextGrey, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Stats Grid
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            StatCard(
                                icon = Icons.Default.WaterDrop,
                                label = "Water",
                                value = "${healthSummary.waterIntake}/${healthSummary.waterGoal}L",
                                progress = if (healthSummary.waterGoal > 0) (healthSummary.waterIntake / healthSummary.waterGoal).toFloat() else 0f,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                icon = Icons.Default.Thermostat,
                                label = "Body Temp",
                                value = "${reading?.temp ?: 0.0}°C",
                                progress = ((reading?.temp?.toFloat() ?: 0f) / 50f).coerceIn(0f, 1f),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            StatCard(
                                icon = Icons.Default.BatteryFull,
                                label = "Device Battery",
                                value = "${reading?.battery ?: 0}%",
                                progress = (reading?.battery?.toFloat() ?: 0f) / 100f,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                icon = if (connectionState == ConnectionState.CONNECTED) Icons.Default.Bluetooth else Icons.Default.BluetoothDisabled,
                                label = "Bluetooth",
                                value = if (connectionState == ConnectionState.CONNECTED) "Connected" else "Disconnected",
                                progress = if (connectionState == ConnectionState.CONNECTED) 1f else 0.3f,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun StatCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, progress: Float, modifier: Modifier = Modifier) {
    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val activeAccent = if (darkTheme) AccentCyan else Color(0xFF059669)
    Box(modifier = modifier.neoCard().padding(16.dp)) {
        Column {
            Icon(icon, contentDescription = null, tint = activeAccent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(label, color = TextGrey, fontSize = 12.sp)
            Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = activeAccent,
                trackColor = if (darkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)
            )
        }
    }
}
