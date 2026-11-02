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
            // Spacer to clear the fixed top header cleanly
            Spacer(modifier = Modifier.height(68.dp))

            // 2. Title & Subtitle
            Text(
                text = "History",
                color = colors.textPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = when (selectedTab) {
                    "Alerts" -> "Historical alarm breaches and system notices"
                    "Device Activity" -> "Hardware telemetry pipeline and sync logs"
                    else -> "View past data and device activity"
                },
                color = colors.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                                HistoryChartCanvas(
                                    modifier = Modifier.fillMaxSize(),
                                    darkTheme = false,
                                    readings = filteredReadings,
                                    colors = colors
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

@Composable
private fun HistoryChartCanvas(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = false,
    readings: List<com.example.data.Reading> = emptyList(),
    colors: FluxColors = rememberFluxColors(false)
) {
    // 1. Smooth animated line sweep progress
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(readings.size) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 750,
                easing = FastOutSlowInEasing
            )
        )
    }
    val chartAnimProgress = animProgress.value

    // 2. Continuous Live Pulse Beacon on latest telemetry point
    val infiniteTransition = rememberInfiniteTransition(label = "historyBeacon")
    val beaconPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beaconPulseScale"
    )
    val beaconPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beaconPulseAlpha"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val chartLeft = 28f.dp.toPx()
        val chartRight = w - 8f.dp.toPx()
        val chartTop = 10f.dp.toPx()
        val chartBottom = h - 22f.dp.toPx()

        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        val gridLineColor = Color(0xFFF1F5F9)

        // Draw 4 horizontal dashed grid lines
        val ySteps = 4
        for (i in 0..ySteps) {
            val y = chartTop + (chartHeight / ySteps) * i
            drawLine(
                color = gridLineColor,
                start = Offset(chartLeft, y),
                end = Offset(chartRight, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
        }

        if (readings.size >= 2) {
            // Plot real data points
            val displayList = readings.takeLast(24) // up to 24 real telemetry records
            val minTemp = (displayList.minOfOrNull { it.temperature } ?: 10.0).coerceAtMost(10.0)
            val maxTemp = (displayList.maxOfOrNull { it.temperature } ?: 40.0).coerceAtLeast(40.0)
            val tempRange = (maxTemp - minTemp).coerceAtLeast(1.0)

            val xStep = chartWidth / (displayList.size - 1).coerceAtLeast(1)

            val points = displayList.mapIndexed { idx, r ->
                val x = chartLeft + idx * xStep
                val normalizedY = ((r.temperature - minTemp) / tempRange).toFloat()
                val y = chartBottom - (normalizedY * chartHeight)
                Offset(x, y.coerceIn(chartTop, chartBottom))
            }

            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val cx = (p0.x + p1.x) / 2f
                    cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                }
            }

            val fillPath = Path().apply {
                addPath(path)
                lineTo(points.last().x, chartBottom)
                lineTo(points.first().x, chartBottom)
                close()
            }

            val clipRight = chartLeft + (chartWidth * chartAnimProgress)

            clipRect(
                left = 0f,
                top = 0f,
                right = clipRight,
                bottom = h
            ) {
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colors.primary.copy(alpha = 0.32f),
                            colors.primary.copy(alpha = 0.02f)
                        ),
                        startY = chartTop,
                        endY = chartBottom
                    )
                )

                drawPath(
                    path = path,
                    color = colors.primary,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Live Glowing Beacon Ring on newest reading
                if (chartAnimProgress > 0.85f) {
                    val lastP = points.last()
                    drawCircle(
                        color = colors.primary.copy(alpha = beaconPulseAlpha),
                        radius = 5.dp.toPx() * beaconPulseScale,
                        center = lastP
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.5.dp.toPx(),
                        center = lastP
                    )
                    drawCircle(
                        color = colors.primary,
                        radius = 4.5.dp.toPx(),
                        center = lastP,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
    }

    // Dynamic Axis Labels Overlay (Computed from genuine Room DB timestamps)
    Box(modifier = modifier) {
        if (readings.isEmpty()) {
            androidx.compose.material3.Text(
                text = "Awaiting Telemetry Readings",
                color = FluxTextLight,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxHeight()
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("40°", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                Text("30°", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                Text("20°", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                Text("10°", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            }

            // Real dynamic timestamps from actual data records
            val displayList = remember(readings) { readings.takeLast(24) }
            val timeLabels = remember(displayList) {
                val timeFmt = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                val startT = displayList.first().timestamp
                val endT = displayList.last().timestamp
                if (displayList.size < 4 || (endT - startT) < 120_000L) {
                    displayList.map { timeFmt.format(java.util.Date(it.timestamp)) }.distinct().takeLast(4)
                } else {
                    val step = (endT - startT) / 3
                    listOf(
                        timeFmt.format(java.util.Date(startT)),
                        timeFmt.format(java.util.Date(startT + step)),
                        timeFmt.format(java.util.Date(startT + step * 2)),
                        timeFmt.format(java.util.Date(endT))
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth()
                    .padding(start = 28.dp, end = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                timeLabels.forEach { label ->
                    Text(label, color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
