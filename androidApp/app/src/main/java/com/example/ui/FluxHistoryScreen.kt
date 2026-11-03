package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.components.FluxTrendGraph
import com.example.ui.theme.*

@Composable
fun FluxHistoryScreen(
    viewModel: MainViewModel,
    onNotificationClick: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedTab by remember { mutableStateOf("Temperature") }
    var selectedRange by remember { mutableStateOf("Day") }
    val selectedCalendar = remember { mutableStateOf(java.util.Calendar.getInstance()) }
    val formattedCurrentDate = remember(selectedCalendar.value) {
        java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(selectedCalendar.value.time)
    }

    val connectionState by viewModel.connectionState.collectAsState()
    val allReadings by viewModel.allReadings.collectAsState()
    val preferredUnit by viewModel.preferredUnit.collectAsState()
    val isOnline = connectionState == com.example.ble.ConnectionState.CONNECTED && viewModel.bleManager.isBluetoothEnabled()

    val activeProfile by viewModel.activeProfile.collectAsState()
    val colors = rememberFluxColors(darkTheme = false, activeProfile = activeProfile)

    val allNotifications by viewModel.allNotifications.collectAsState()
    val connectivityState by viewModel.connectivityState.collectAsState()
    val lastCalibration by viewModel.lastCalibration.collectAsState()
    val pendingRecords by viewModel.pendingOfflineRecords.collectAsState()
    val supabaseSyncStatus by viewModel.supabaseSyncStatus.collectAsState()

    // Dynamic Date & Range Filtering
    val filteredReadings = remember(allReadings, selectedCalendar.value, selectedRange) {
        val cal = selectedCalendar.value
        val startCal = java.util.Calendar.getInstance().apply {
            time = cal.time
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val endCal = java.util.Calendar.getInstance().apply {
            time = cal.time
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 59)
            set(java.util.Calendar.MILLISECOND, 999)
        }

        when (selectedRange) {
            "Day" -> {
                val s = startCal.timeInMillis
                val e = endCal.timeInMillis
                val list = allReadings.filter { it.timestamp in s..e }
                if (list.isNotEmpty()) list else allReadings
            }
            "Week" -> {
                val e = endCal.timeInMillis
                val s = e - (7L * 24 * 60 * 60 * 1000)
                val list = allReadings.filter { it.timestamp in s..e }
                if (list.isNotEmpty()) list else allReadings
            }
            "Month" -> {
                startCal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                endCal.set(java.util.Calendar.DAY_OF_MONTH, endCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
                val s = startCal.timeInMillis
                val e = endCal.timeInMillis
                val list = allReadings.filter { it.timestamp in s..e }
                if (list.isNotEmpty()) list else allReadings
            }
            else -> allReadings
        }
    }

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

            // 3. Segmented Navigation Tabs (Temperature / Device Activity / Alerts)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(26.dp), spotColor = Color(0x10000000))
                    .background(colors.cardBg, RoundedCornerShape(26.dp))
                    .border(0.5.dp, colors.cardBorder, RoundedCornerShape(26.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Temperature Tab
                Row(
                    modifier = Modifier
                        .weight(1.3f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (selectedTab == "Temperature") colors.primary else Color.Transparent)
                        .clickable { selectedTab = "Temperature" }
                        .padding(vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Thermostat,
                        contentDescription = null,
                        tint = if (selectedTab == "Temperature") Color.White else colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Temperature",
                        color = if (selectedTab == "Temperature") Color.White else colors.textPrimary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Device Activity Tab
                Row(
                    modifier = Modifier
                        .weight(1.3f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (selectedTab == "Device Activity") colors.primary else Color.Transparent)
                        .clickable { selectedTab = "Device Activity" }
                        .padding(vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BarChart,
                        contentDescription = null,
                        tint = if (selectedTab == "Device Activity") Color.White else colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Device Activity",
                        color = if (selectedTab == "Device Activity") Color.White else colors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Alerts Tab
                Row(
                    modifier = Modifier
                        .weight(1.1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (selectedTab == "Alerts") colors.primary else Color.Transparent)
                        .clickable { selectedTab = "Alerts" }
                        .padding(vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = if (selectedTab == "Alerts") Color.White else colors.textSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Alerts",
                        color = if (selectedTab == "Alerts") Color.White else colors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                "Temperature" -> {
                    // Date Picker & Day/Week/Month Range Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Date Selector Pill with Interactive DatePickerDialog
                        Row(
                            modifier = Modifier
                                .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color(0x0E000000))
                                .background(colors.pillBg, RoundedCornerShape(20.dp))
                                .border(0.5.dp, colors.pillBorder, RoundedCornerShape(20.dp))
                                .clickable {
                                    val c = selectedCalendar.value
                                    android.app.DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            val newCal = java.util.Calendar.getInstance().apply {
                                                set(year, month, dayOfMonth, 0, 0, 0)
                                                set(java.util.Calendar.MILLISECOND, 0)
                                            }
                                            selectedCalendar.value = newCal
                                        },
                                        c.get(java.util.Calendar.YEAR),
                                        c.get(java.util.Calendar.MONTH),
                                        c.get(java.util.Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                                .padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = "Select Date",
                                tint = colors.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formattedCurrentDate,
                                color = colors.textPrimary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Day / Week / Month Segmented Pill
                        Row(
                            modifier = Modifier
                                .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color(0x0E000000))
                                .background(colors.pillBg, RoundedCornerShape(20.dp))
                                .border(0.5.dp, colors.pillBorder, RoundedCornerShape(20.dp))
                                .padding(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selectedRange == "Day") colors.primary else Color.Transparent)
                                    .clickable { selectedRange = "Day" }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Day",
                                    color = if (selectedRange == "Day") Color.White else colors.textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selectedRange == "Week") colors.primary else Color.Transparent)
                                    .clickable { selectedRange = "Week" }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Week",
                                    color = if (selectedRange == "Week") Color.White else colors.textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selectedRange == "Month") colors.primary else Color.Transparent)
                                    .clickable { selectedRange = "Month" }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Month",
                                    color = if (selectedRange == "Month") Color.White else colors.textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Temperature Trend Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = Color(0x10000000))
                            .background(colors.cardBg, RoundedCornerShape(24.dp))
                            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(24.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(colors.primarySoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Thermostat,
                                            contentDescription = null,
                                            tint = colors.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Temperature Trend",
                                            color = colors.textPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "$formattedCurrentDate • $selectedRange",
                                            color = colors.textSecondary,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                val isFahrBadge = preferredUnit.equals("F", ignoreCase = true)
                                val maxTemp = remember(filteredReadings) { filteredReadings.maxOfOrNull { it.temperature } }
                                val minTemp = remember(filteredReadings) { filteredReadings.minOfOrNull { it.temperature } }
                                fun fmtBadge(t: Double?) = if (t == null) "--" else {
                                    val v = if (isFahrBadge) (t * 9 / 5) + 32 else t
                                    String.format("%.1f°%s", v, if (isFahrBadge) "F" else "C")
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(
                                        modifier = Modifier
                                            .background(Color(0xFFFEE2E2), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = fmtBadge(maxTemp),
                                            color = Color(0xFFDC2626),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Max",
                                            color = Color(0xFFEF4444),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Column(
                                        modifier = Modifier
                                            .background(Color(0xFFDBEAFE), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = fmtBadge(minTemp),
                                            color = Color(0xFF2563EB),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Min",
                                            color = Color(0xFF3B82F6),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            ) {
                                FluxTrendGraph(
                                    readings = filteredReadings,
                                    unitSymbol = if (preferredUnit == "F") "°F" else "°C",
                                    isFahrenheit = preferredUnit == "F",
                                    colors = colors,
                                    chartHeight = 140.dp,
                                    showTimeAxis = true,
                                    showYAxis = true,
                                    interactive = true,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Data Log Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Data Log",
                                color = colors.textPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Recorded temperature readings",
                                color = colors.textSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        val todayFormatted = remember {
                            java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                        }
                        Text(
                            text = todayFormatted,
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val lowThresh by viewModel.lowTempThreshold.collectAsState()
                    val highThresh by viewModel.highTempThreshold.collectAsState()

                    if (allReadings.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.cardBg, RoundedCornerShape(18.dp))
                                .border(0.5.dp, colors.cardBorder, RoundedCornerShape(18.dp))
                                .padding(horizontal = 20.dp, vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No recorded readings yet.\nTelemetry will log here in real-time when connected.",
                                color = colors.textSecondary,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    } else {
                        val timeFormatter = remember {
                            java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                        }
                        val recentReadings = remember(allReadings) {
                            allReadings.takeLast(10).reversed()
                        }

                        recentReadings.forEach { reading ->
                            val readingTime = timeFormatter.format(java.util.Date(reading.timestamp))
                            val isFahr = preferredUnit.equals("F", ignoreCase = true)
                            val dispTemp = if (isFahr) (reading.temperature * 9 / 5) + 32 else reading.temperature
                            val valStr = String.format("%.1f°%s", dispTemp, if (isFahr) "F" else "C")

                            val isHigh = reading.temperature > highThresh
                            val isLow = reading.temperature < lowThresh

                            val statusLabel = when {
                                isHigh -> "High"
                                isLow -> "Low"
                                else -> "Normal"
                            }

                            val iconTint = when {
                                isHigh -> Color(0xFFEF4444)
                                isLow -> Color(0xFF3B82F6)
                                else -> FluxOrange
                            }

                            val iconBg = when {
                                isHigh -> Color(0xFFFEE2E2)
                                isLow -> Color(0xFFDBEAFE)
                                else -> colors.primarySoft
                            }

                            val statusBg = when {
                                isHigh -> Color(0xFFFEE2E2)
                                isLow -> Color(0xFFDBEAFE)
                                else -> Color(0xFFDCFCE7)
                            }

                            val statusColor = when {
                                isHigh -> Color(0xFFDC2626)
                                isLow -> Color(0xFF2563EB)
                                else -> Color(0xFF16A34A)
                            }

                            DataLogCard(
                                iconTint = iconTint,
                                iconBg = iconBg,
                                time = readingTime,
                                value = valStr,
                                statusLabel = statusLabel,
                                statusBg = statusBg,
                                statusColor = statusColor,
                                colors = colors
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }

                "Alerts" -> {
                    // Alerts Tab Content
                    val alertCount = allNotifications.size
                    val unreadCount = allNotifications.count { !it.isRead }

                    // Alerts Summary Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.cardBg)
                            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Alert Archive Summary",
                                    color = colors.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$alertCount logged event${if (alertCount != 1) "s" else ""} • $unreadCount unacknowledged",
                                    color = colors.textSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (unreadCount > 0) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (unreadCount > 0) "BREACH" else "CLEAR",
                                    color = if (unreadCount > 0) Color(0xFFEF4444) else Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (allNotifications.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.cardBg, RoundedCornerShape(18.dp))
                                .border(0.5.dp, colors.cardBorder, RoundedCornerShape(18.dp))
                                .padding(horizontal = 20.dp, vertical = 28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No historical alerts recorded in database.",
                                color = colors.textSecondary,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        allNotifications.sortedByDescending { it.id }.forEach { notif ->
                            val isBreach = notif.type.equals("ALERT", ignoreCase = true)
                            val isWarning = notif.type.equals("WARNING", ignoreCase = true)
                            val badgeColor = when {
                                isBreach -> Color(0xFFEF4444)
                                isWarning -> FluxOrange
                                else -> Color(0xFF10B981)
                            }
                            val badgeBg = badgeColor.copy(alpha = 0.14f)

                            DataLogCard(
                                iconTint = badgeColor,
                                iconBg = badgeBg,
                                time = notif.time,
                                value = notif.title,
                                statusLabel = if (isBreach) "Critical" else if (isWarning) "Warning" else "Info",
                                statusBg = badgeBg,
                                statusColor = badgeColor,
                                colors = colors
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }

                "Device Activity" -> {
                    // Device Activity Tab Content
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.cardBg)
                            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Telemetry & Hardware Pipeline",
                                color = colors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Peripheral Bluetooth Link", color = colors.textSecondary, fontSize = 12.5.sp)
                                Text(
                                    text = if (isOnline) "ONLINE (BLE CONNECTED)" else connectionState.name,
                                    color = if (isOnline) Color(0xFF10B981) else Color(0xFFEF4444),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Transport Mode", color = colors.textSecondary, fontSize = 12.5.sp)
                                Text(
                                    text = connectivityState.type.name,
                                    color = colors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Local Room Ingestion", color = colors.textSecondary, fontSize = 12.5.sp)
                                Text(
                                    text = "${allReadings.size} records saved",
                                    color = colors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Cloud Synchronizer", color = colors.textSecondary, fontSize = 12.5.sp)
                                Text(
                                    text = if (pendingRecords > 0) "$pendingRecords pending" else supabaseSyncStatus,
                                    color = if (pendingRecords > 0) FluxOrange else Color(0xFF10B981),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Recent Connection & Sync Events",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Real Hardware Event 1: Calibration
                    DataLogCard(
                        iconTint = Color(0xFF3B82F6),
                        iconBg = Color(0xFF3B82F6).copy(alpha = 0.15f),
                        time = lastCalibration?.let {
                            java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(it.timestamp))
                        } ?: "Recent",
                        value = lastCalibration?.let { "Sensor Calibrated (E0=%.2f)".format(it.e0) } ?: "Factory Calibration DS18B20",
                        statusLabel = "Calibrated",
                        statusBg = Color(0xFF3B82F6).copy(alpha = 0.15f),
                        statusColor = Color(0xFF3B82F6),
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Real Hardware Event 2: Cloud Sync Queue
                    DataLogCard(
                        iconTint = FluxOrange,
                        iconBg = FluxOrange.copy(alpha = 0.15f),
                        time = "Active",
                        value = "Cloud Sync Pipeline: $supabaseSyncStatus",
                        statusLabel = if (pendingRecords == 0) "Synced" else "Queued",
                        statusBg = if (pendingRecords == 0) Color(0xFF10B981).copy(alpha = 0.15f) else FluxOrange.copy(alpha = 0.15f),
                        statusColor = if (pendingRecords == 0) Color(0xFF10B981) else FluxOrange,
                        colors = colors
                    )
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
private fun DataLogCard(
    iconTint: Color,
    iconBg: Color,
    time: String,
    value: String,
    statusLabel: String,
    statusBg: Color,
    statusColor: Color,
    colors: FluxColors = rememberFluxColors(false)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp), spotColor = Color(0x0C000000))
            .background(colors.cardBg, RoundedCornerShape(18.dp))
            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular Thermometer Icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Thermostat,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Time and Value Column
        Column {
            Text(
                text = time,
                color = colors.textSecondary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = colors.textPrimary,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Status Badge (High / Low / Normal)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(statusBg)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = statusLabel,
                color = statusColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
