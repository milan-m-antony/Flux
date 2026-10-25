package com.example.ui

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
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.BarChart
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
    var selectedTab by remember { mutableStateOf("Temperature") }
    var selectedRange by remember { mutableStateOf("Day") }
    val connectionState by viewModel.connectionState.collectAsState()
    val allReadings by viewModel.allReadings.collectAsState()
    val preferredUnit by viewModel.preferredUnit.collectAsState()
    val isOnline = connectionState == com.example.ble.ConnectionState.CONNECTED

    val darkTheme by viewModel.isDarkTheme.collectAsState()
    val colors = rememberFluxColors(darkTheme)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                text = "View past data and device activity",
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
                // Temperature Tab (Active in Screenshot)
                Row(
                    modifier = Modifier
                        .weight(1.3f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (selectedTab == "Temperature") FluxOrange else Color.Transparent)
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
                        .background(if (selectedTab == "Device Activity") FluxOrange else Color.Transparent)
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
                        .background(if (selectedTab == "Alerts") FluxOrange else Color.Transparent)
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

            // 4. Date Picker & Day/Week/Month Range Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date Selector Pill
                Row(
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color(0x0E000000))
                        .background(colors.pillBg, RoundedCornerShape(20.dp))
                        .border(0.5.dp, colors.pillBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = colors.textPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val formattedCurrentDate = remember {
                        java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                    }
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
                    // Day (Selected Orange Circle)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selectedRange == "Day") FluxOrange else Color.Transparent)
                            .clickable { selectedRange = "Day" }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Day",
                            color = if (selectedRange == "Day") Color.White else colors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Week
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selectedRange == "Week") FluxOrange else Color.Transparent)
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

                    // Month
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selectedRange == "Month") FluxOrange else Color.Transparent)
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

            // 5. Temperature Trend Card (With 30.8°C Max and 20.1°C Min Badges)
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
                                    .background(if (darkTheme) Color(0xFF332014) else Color(0xFFFFF2EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Thermostat,
                                    contentDescription = null,
                                    tint = FluxOrange,
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
                                val formattedCurrentDate = remember {
                                    java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                                }
                                Text(
                                    text = "$formattedCurrentDate • $selectedRange",
                                    color = colors.textSecondary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Max & Min Badges — computed from real readings
                        val isFahrBadge = preferredUnit.equals("F", ignoreCase = true)
                        val maxTemp = remember(allReadings) { allReadings.maxOfOrNull { it.temperature } }
                        val minTemp = remember(allReadings) { allReadings.minOfOrNull { it.temperature } }
                        fun fmtBadge(t: Double?) = if (t == null) "--" else {
                            val v = if (isFahrBadge) (t * 9 / 5) + 32 else t
                            String.format("%.1f°%s", v, if (isFahrBadge) "F" else "C")
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Max badge
                            Column(
                                modifier = Modifier
                                    .background(if (darkTheme) Color(0xFF451A1A) else Color(0xFFFEE2E2), RoundedCornerShape(12.dp))
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

                            // Min badge
                            Column(
                                modifier = Modifier
                                    .background(if (darkTheme) Color(0xFF172554) else Color(0xFFDBEAFE), RoundedCornerShape(12.dp))
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

                    // Hourly Area Chart — real data
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        HistoryChartCanvas(
                            modifier = Modifier.fillMaxSize(),
                            darkTheme = darkTheme,
                            readings = allReadings
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 6. Data Log Section
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
                        isHigh -> if (darkTheme) Color(0xFF451A1A) else Color(0xFFFEE2E2)
                        isLow -> if (darkTheme) Color(0xFF172554) else Color(0xFFDBEAFE)
                        else -> if (darkTheme) Color(0xFF332014) else Color(0xFFFFF2EB)
                    }

                    val statusBg = when {
                        isHigh -> if (darkTheme) Color(0xFF451A1A) else Color(0xFFFEE2E2)
                        isLow -> if (darkTheme) Color(0xFF172554) else Color(0xFFDBEAFE)
                        else -> if (darkTheme) Color(0xFF14532D) else Color(0xFFDCFCE7)
                    }

                    val statusColor = when {
                        isHigh -> Color(0xFFDC2626)
                        isLow -> Color(0xFF2563EB)
                        else -> if (darkTheme) Color(0xFF4ADE80) else Color(0xFF16A34A)
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

        Spacer(modifier = Modifier.width(14.dp))

        // Time
        Text(
            text = time,
            color = colors.textSecondary,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.weight(1f))

        // Value in bold
        Text(
            text = value,
            color = colors.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Status pill
        Box(
            modifier = Modifier
                .background(statusBg, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = statusLabel,
                color = statusColor,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right chevron
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun HistoryChartCanvas(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = false,
    readings: List<com.example.data.Reading> = emptyList()
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val leftPadding = 30f
        val bottomPadding = 24f
        val chartWidth = width - leftPadding
        val chartHeight = height - bottomPadding

        // 1. Horizontal dashed grid lines (40, 30, 20, 10)
        val ySteps = listOf(40f, 30f, 20f, 10f)
        for (i in ySteps.indices) {
            val y = (chartHeight * i / (ySteps.size - 1))
            drawLine(
                color = if (darkTheme) Color(0xFF334155) else Color(0xFFEDF2F7),
                start = Offset(leftPadding, y),
                end = Offset(width, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
        }

        // 2. Data points from real readings (last 24 readings max)
        if (readings.isNotEmpty()) {
            val slice = readings.takeLast(24)
            val minT = slice.minOf { it.temperature }.toFloat()
            val maxT = slice.maxOf { it.temperature }.toFloat()
            val range = (maxT - minT).coerceAtLeast(1f)
            val points = slice.mapIndexed { i, r ->
                val x = leftPadding + chartWidth * (i.toFloat() / (slice.size - 1).coerceAtLeast(1))
                val y = chartHeight * (1f - (r.temperature.toFloat() - minT) / range)
                Offset(x, y)
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
                lineTo(points.last().x, chartHeight)
                lineTo(points.first().x, chartHeight)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        FluxOrange.copy(alpha = 0.35f),
                        FluxOrange.copy(alpha = 0.03f)
                    ),
                    startY = 0f,
                    endY = chartHeight
                )
            )

            drawPath(
                path = path,
                color = FluxOrange,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        // If readings is empty, just show the grid lines — label overlay handles empty state text
    }

    // Overlay: axis labels or empty-state message
    Box(modifier = modifier) {
        if (readings.isEmpty()) {
            // Empty state — centred message
            androidx.compose.material3.Text(
                text = "Awaiting Telemetry Readings",
                color = FluxTextLight,
                fontSize = 11.sp,
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
                Text("40", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                Text("30", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                Text("20", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                Text("10", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth()
                    .padding(start = 24f.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("00:00", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                Text("04:00", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                Text("08:00", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                Text("12:00", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                Text("16:00", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                Text("20:00", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
