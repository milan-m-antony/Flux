package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

enum class TrendTimeFilter(
    val label: String,
    val subtitle: String,
    val timestamps: List<String>,
    val temps: List<Float>,
    val peakIndex: Int,
    val minTemp: Float,
    val maxTemp: Float,
    val avgTemp: Float
) {
    LAST_1_HOUR(
        label = "Last 1 Hour",
        subtitle = "Live temperature over the last 1 hour",
        timestamps = listOf("10:20", "10:30", "10:40", "10:50", "11:00"),
        temps = listOf(18.2f, 21.0f, 19.5f, 24.8f, 23.0f, 28.5f, 26.4f, 34.2f, 30.1f),
        peakIndex = 7, // 34.2°C at points[7]
        minTemp = 26.1f,
        maxTemp = 30.8f,
        avgTemp = 28.3f
    ),
    LAST_6_HOURS(
        label = "Last 6 Hours",
        subtitle = "Temperature over the last 6 hours",
        timestamps = listOf("05:00", "06:30", "08:00", "09:30", "11:00"),
        temps = listOf(19.0f, 22.4f, 21.1f, 26.0f, 25.2f, 29.8f, 28.0f, 32.6f, 29.4f),
        peakIndex = 7,
        minTemp = 24.5f,
        maxTemp = 32.6f,
        avgTemp = 27.9f
    ),
    LAST_24_HOURS(
        label = "Last 24 Hours",
        subtitle = "Hourly temperature over the last 24 hours",
        timestamps = listOf("12:00", "18:00", "00:00", "06:00", "11:00"),
        temps = listOf(22.0f, 25.5f, 23.8f, 28.0f, 22.0f, 20.4f, 24.0f, 31.5f, 28.0f),
        peakIndex = 7,
        minTemp = 20.4f,
        maxTemp = 31.5f,
        avgTemp = 26.8f
    ),
    LAST_7_DAYS(
        label = "Last 7 Days",
        subtitle = "Daily temperature trend over the last 7 days",
        timestamps = listOf("Mon", "Wed", "Thu", "Sat", "Today"),
        temps = listOf(23.5f, 26.0f, 24.2f, 27.5f, 26.0f, 30.2f, 27.8f, 33.0f, 28.4f),
        peakIndex = 7,
        minTemp = 23.5f,
        maxTemp = 33.0f,
        avgTemp = 27.5f
    )
}

@Composable
fun FluxHomeScreen(
    viewModel: MainViewModel,
    onNavigateToHistory: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onNavigateToDeviceSetup: () -> Unit = {}
) {
    val latestReading by viewModel.latestReading.collectAsState()
    val allReadings by viewModel.allReadings.collectAsState()
    val preferredUnit by viewModel.preferredUnit.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val connectivityState by viewModel.connectivityState.collectAsState()

    val hasRealData = latestReading != null || allReadings.isNotEmpty() || connectionState == com.example.ble.ConnectionState.CONNECTED
    val currentTempC = latestReading?.temp ?: allReadings.firstOrNull()?.temperature ?: 0.0
    var selectedUnit by remember { mutableStateOf(preferredUnit) }

    val displayTemp = if (selectedUnit == "F") (currentTempC * 9 / 5) + 32 else currentTempC
    val unitSymbol = if (selectedUnit == "F") "°F" else "°C"

    val darkTheme by viewModel.isDarkTheme.collectAsState()
    val colors = rememberFluxColors(darkTheme)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Spacer to cleanly clear the fixed top header with generous breathing room
            Spacer(modifier = Modifier.height(86.dp))

            // First-Device Empty State / Add Device Banner
            if (!hasRealData) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.cardBg)
                        .border(1.dp, FluxOrange.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(FluxOrange.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.North, // Generic arrow or search
                                    contentDescription = null,
                                    tint = FluxOrange,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "No Device Connected",
                                    color = colors.textPrimary,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Pair your Flux ESP32 sensor",
                                    color = colors.textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Button(
                            onClick = onNavigateToDeviceSetup,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FluxOrange,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text("Add Device", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 2. Large Circular Temperature Gauge (Slightly reduced from 290.dp to 265.dp to preserve generous size)
            TemperatureGauge(
                temperature = currentTempC.toFloat(),
                minTemp = 16f,
                maxTemp = 40f,
                unitSymbol = unitSymbol,
                displayTempStr = if (hasRealData) String.format("%.1f", displayTemp) else "--.-",
                hasData = hasRealData,
                darkTheme = darkTheme,
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(265.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Segmented Unit Selector (°C / °F) and History/Trend Button Row (Comfortable height ~ 40.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // °C / °F Segmented Capsule
                Row(
                    modifier = Modifier
                        .shadow(3.dp, RoundedCornerShape(20.dp), spotColor = Color(0x10000000))
                        .background(colors.cardBg, RoundedCornerShape(20.dp))
                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(20.dp))
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selectedUnit == "C") (if (darkTheme) FluxOrange else Color(0xFF0F172A)) else Color.Transparent)
                            .clickable {
                                selectedUnit = "C"
                                viewModel.setPreferredUnit("C")
                            }
                            .padding(horizontal = 20.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "°C",
                            color = if (selectedUnit == "C") Color.White else colors.textSecondary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selectedUnit == "F") (if (darkTheme) FluxOrange else Color(0xFF0F172A)) else Color.Transparent)
                            .clickable {
                                selectedUnit = "F"
                                viewModel.setPreferredUnit("F")
                            }
                            .padding(horizontal = 20.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "°F",
                            color = if (selectedUnit == "F") Color.White else colors.textSecondary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Trend/Chart Action Button (Circular button with vertical bar chart icon)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(3.dp, CircleShape, spotColor = Color(0x10000000))
                        .clip(CircleShape)
                        .background(colors.cardBg)
                        .border(0.5.dp, colors.cardBorder, CircleShape)
                        .clickable { onNavigateToHistory() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BarChart,
                        contentDescription = "View Trend",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Temperature Trend Card (Interactive Graph, Filter, Min/Max/Avg Stat Cards)
            TemperatureTrendCard(
                unitSymbol = unitSymbol,
                isFahrenheit = selectedUnit == "F",
                readings = allReadings,
                darkTheme = darkTheme,
                colors = colors,
                modifier = Modifier.fillMaxWidth()
            )

            // 5. Bottom clearance padding to keep content above floating navigation bar
            Spacer(modifier = Modifier.height(78.dp))
        }
    }
}

@Composable
private fun TemperatureGauge(
    temperature: Float,
    minTemp: Float,
    maxTemp: Float,
    unitSymbol: String,
    displayTempStr: String,
    hasData: Boolean = true,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = false,
    colors: FluxColors = rememberFluxColors(darkTheme)
) {
    val progress = ((temperature - minTemp) / (maxTemp - minTemp)).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "gauge_progress"
    )

    // Accurate min/max labels converting properly for Fahrenheit
    val minDisplay = if (unitSymbol == "°F") "${((minTemp * 9 / 5) + 32).toInt()}°F" else "${minTemp.toInt()}°C"
    val maxDisplay = if (unitSymbol == "°F") "${((maxTemp * 9 / 5) + 32).toInt()}°F" else "${maxTemp.toInt()}°C"

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val totalWidth = maxWidth
        val totalHeight = maxHeight
        val centerX = totalWidth / 2
        val centerY = totalHeight / 2 + 5.dp

        val radiusDp = 104.dp
        // 135° (bottom left) and 45° (bottom right)
        // cos(45°) = 0.7071f, sin(45°) = 0.7071f
        val arcOffsetX = radiusDp * 0.7071f
        val arcOffsetY = radiusDp * 0.7071f
        val labelYDp = centerY + arcOffsetY + 10.dp

        // 1. Top "Medium" needle marker label moved higher to top for clear visibility
        Text(
            text = "Medium",
            color = colors.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-14).dp)
        )

        // 2. Circular Gauge Canvas (Outer Radial Ticks, Track, Active Arc, Knob)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f + 5.dp.toPx())
            val radius = radiusDp.toPx()

            val startAngle = 135f
            val sweepTotalAngle = 270f

            // A. Outer Concentric Ring of Fine Radial Ticks
            val numTicks = 48
            val tickRadiusOuter = radius + 18.dp.toPx()
            val tickRadiusInner = radius + 12.dp.toPx()
            val tickColor = if (darkTheme) Color(0xFF475569) else Color(0xFFCBD5E1)

            for (i in 0..numTicks) {
                val angleDeg = startAngle + (sweepTotalAngle * i / numTicks)
                val rad = Math.toRadians(angleDeg.toDouble())
                val p1 = Offset(
                    (center.x + tickRadiusInner * cos(rad)).toFloat(),
                    (center.y + tickRadiusInner * sin(rad)).toFloat()
                )
                val p2 = Offset(
                    (center.x + tickRadiusOuter * cos(rad)).toFloat(),
                    (center.y + tickRadiusOuter * sin(rad)).toFloat()
                )
                drawLine(
                    color = tickColor,
                    start = p1,
                    end = p2,
                    strokeWidth = 1.3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // B. Inactive Gray Arc Track
            val trackColor = if (darkTheme) Color(0xFF1E293B) else Color(0xFFE2E8F0)
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepTotalAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
            )

            // C. Active Orange Gradient Arc
            val activeSweep = sweepTotalAngle * animatedProgress
            if (activeSweep > 0) {
                // Subtle ambient glow behind active arc
                drawArc(
                    color = FluxOrange.copy(alpha = 0.18f),
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Round)
                )

                // Main vibrant orange gradient stroke
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            FluxOrangeGradientStart,
                            FluxOrangeGradientEnd,
                            Color(0xFFFF3D00)
                        ),
                        center = center
                    ),
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // D. Thumb Knob Indicator on Arc at current temperature
            val thumbAngleRad = Math.toRadians((startAngle + activeSweep).toDouble())
            val thumbPos = Offset(
                (center.x + radius * cos(thumbAngleRad)).toFloat(),
                (center.y + radius * sin(thumbAngleRad)).toFloat()
            )
            // Outer white thumb with crisp orange border
            drawCircle(
                color = Color.White,
                radius = 10.dp.toPx(),
                center = thumbPos
            )
            drawCircle(
                color = FluxOrange,
                radius = 10.dp.toPx(),
                center = thumbPos,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // 3. Inner Elevated White/Card Circle (Comfortably sized at 158.dp)
        Box(
            modifier = Modifier
                .size(158.dp)
                .offset(y = 5.dp)
                .shadow(
                    elevation = 9.dp,
                    shape = CircleShape,
                    spotColor = Color(0x16000000),
                    ambientColor = Color(0x08000000)
                )
                .clip(CircleShape)
                .background(colors.cardBg)
                .border(1.dp, colors.cardBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Thermometer icon
                Icon(
                    imageVector = Icons.Outlined.Thermostat,
                    contentDescription = null,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Big bold reading
                Text(
                    text = if (hasData) "$displayTempStr$unitSymbol" else "--.-",
                    color = colors.textPrimary,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Stable status with orange/green dot
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.5.dp)
                            .clip(CircleShape)
                            .background(if (hasData) FluxGreenOnline else Color(0xFF94A3B8))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasData) "Live" else "Offline",
                        color = FluxTextMuted,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 4. Exact "16°C" Position: Directly beneath the bottom-left arc tip at 135°
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(
                    x = (centerX - arcOffsetX - 16.dp).coerceAtLeast(0.dp),
                    y = labelYDp
                )
        ) {
            Text(
                text = minDisplay,
                color = FluxTextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // 5. Exact "40°C" Position: Directly beneath the bottom-right arc tip at 45°
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(
                    x = (centerX + arcOffsetX - 16.dp).coerceAtMost(totalWidth - 36.dp),
                    y = labelYDp
                )
        ) {
            Text(
                text = maxDisplay,
                color = FluxTextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TemperatureTrendCard(
    unitSymbol: String,
    isFahrenheit: Boolean = false,
    readings: List<com.example.data.Reading> = emptyList(),
    modifier: Modifier = Modifier,
    darkTheme: Boolean = false,
    colors: FluxColors = rememberFluxColors(darkTheme)
) {
    var selectedFilter by remember { mutableStateOf(TrendTimeFilter.LAST_1_HOUR) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val hasRealTelemetry = readings.isNotEmpty()
    val realMinTemp = if (hasRealTelemetry) readings.minOf { it.temperature }.toFloat() else null
    val realMaxTemp = if (hasRealTelemetry) readings.maxOf { it.temperature }.toFloat() else null
    val realAvgTemp = if (hasRealTelemetry) readings.map { it.temperature }.average().toFloat() else null

    fun convertTemp(tempC: Float?): String {
        if (tempC == null) return "--"
        val converted = if (isFahrenheit) (tempC * 9 / 5) + 32 else tempC
        return String.format("%.1f%s", converted, unitSymbol)
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0x10000000)
            )
            .background(colors.cardBg, RoundedCornerShape(20.dp))
            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column {
            // Header Row: Title + Dropdown Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Temperature Trend",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = selectedFilter.subtitle,
                        color = colors.textSecondary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Interactive Dropdown capsule "Last 1 Hour ⌵"
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.pillBg)
                            .border(0.5.dp, colors.pillBorder, RoundedCornerShape(14.dp))
                            .clickable { dropdownExpanded = true }
                            .padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedFilter.label,
                            color = colors.textPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Select Time Range",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(colors.cardBg)
                    ) {
                        TrendTimeFilter.values().forEach { filter ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = filter.label,
                                        fontSize = 12.sp,
                                        color = if (filter == selectedFilter) FluxOrange else colors.textPrimary,
                                        fontWeight = if (filter == selectedFilter) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedFilter = filter
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Smooth Wavy Spline Chart with Interactive Touch Scrubbing & Tooltip (Height ~ 125.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
            ) {
                InteractiveTrendChart(
                    filter = selectedFilter,
                    unitSymbol = unitSymbol,
                    isFahrenheit = isFahrenheit,
                    darkTheme = darkTheme,
                    colors = colors,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Summary row: Min, Max, Average pills (Equal width weight 1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Min Card: Light blue container + blue thermometer
                SummaryStatCard(
                    iconBgColor = if (darkTheme) Color(0xFF1E293B) else Color(0xFFEBF3FF),
                    iconTint = Color(0xFF2563EB),
                    icon = Icons.Outlined.Thermostat,
                    label = "Min",
                    value = convertTemp(if (hasRealTelemetry) realMinTemp else selectedFilter.minTemp),
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                // Max Card: Light peach container + orange up arrow
                SummaryStatCard(
                    iconBgColor = if (darkTheme) Color(0xFF332014) else Color(0xFFFFEDE3),
                    iconTint = Color(0xFFEA580C),
                    icon = Icons.Default.North,
                    label = "Max",
                    value = convertTemp(if (hasRealTelemetry) realMaxTemp else selectedFilter.maxTemp),
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                // Average Card: Light slate container + slate bar chart
                SummaryStatCard(
                    iconBgColor = if (darkTheme) Color(0xFF262F3E) else Color(0xFFF0F3FA),
                    iconTint = Color(0xFF64748B),
                    icon = Icons.Outlined.BarChart,
                    label = "Average",
                    value = convertTemp(if (hasRealTelemetry) realAvgTemp else selectedFilter.avgTemp),
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryStatCard(
    iconBgColor: Color,
    iconTint: Color,
    icon: ImageVector,
    label: String,
    value: String,
    colors: FluxColors,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(colors.pillBg, RoundedCornerShape(12.dp))
            .border(0.5.dp, colors.pillBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = label,
                color = colors.textSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = value,
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun InteractiveTrendChart(
    filter: TrendTimeFilter,
    unitSymbol: String,
    isFahrenheit: Boolean = false,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = false,
    colors: FluxColors = rememberFluxColors(darkTheme)
) {
    // Animated progress to draw in line and fill when filter changes or on load
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(filter) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }
    val chartAnimProgress = animProgress.value

    // Interactive Touch / Drag Scrubbing State
    var isDragging by remember { mutableStateOf(false) }
    var touchFraction by remember { mutableStateOf(0.88f) } // Defaults to peak point at ~0.88f (11:00)

    val currentPeakIndex = filter.peakIndex
    val defaultPeakFraction = (currentPeakIndex.toFloat() / (filter.temps.size - 1)).coerceIn(0f, 1f)

    // Active scrubber fraction: defaults to peak point when not dragging
    val activeFraction = if (isDragging) touchFraction else defaultPeakFraction

    // Interpolate temperature reading at activeFraction
    val activeIndexFloat = activeFraction * (filter.temps.size - 1)
    val lowerIndex = activeIndexFloat.toInt().coerceIn(0, filter.temps.size - 1)
    val upperIndex = (lowerIndex + 1).coerceAtMost(filter.temps.size - 1)
    val fractionBetween = activeIndexFloat - lowerIndex
    val rawTempC = if (lowerIndex == upperIndex) {
        filter.temps[lowerIndex]
    } else {
        filter.temps[lowerIndex] + fractionBetween * (filter.temps[upperIndex] - filter.temps[lowerIndex])
    }

    val displayTempStr = if (isFahrenheit) {
        String.format("%.1f%s", (rawTempC * 9 / 5) + 32, unitSymbol)
    } else {
        String.format("%.1f%s", rawTempC, unitSymbol)
    }

    Box(
        modifier = modifier
            .pointerInput(filter) {
                detectTapGestures(
                    onPress = { offset ->
                        isDragging = true
                        val leftPaddingPx = 28.dp.toPx()
                        val rightPaddingPx = 10.dp.toPx()
                        val chartW = size.width - leftPaddingPx - rightPaddingPx
                        if (chartW > 0) {
                            touchFraction = ((offset.x - leftPaddingPx) / chartW).coerceIn(0f, 1f)
                        }
                        tryAwaitRelease()
                        isDragging = false
                    }
                )
            }
            .pointerInput(filter) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val leftPaddingPx = 28.dp.toPx()
                        val rightPaddingPx = 10.dp.toPx()
                        val chartW = size.width - leftPaddingPx - rightPaddingPx
                        if (chartW > 0) {
                            touchFraction = ((offset.x - leftPaddingPx) / chartW).coerceIn(0f, 1f)
                        }
                    },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false }
                ) { change, _ ->
                    val leftPaddingPx = 28.dp.toPx()
                    val rightPaddingPx = 10.dp.toPx()
                    val chartW = size.width - leftPaddingPx - rightPaddingPx
                    if (chartW > 0) {
                        touchFraction = ((change.position.x - leftPaddingPx) / chartW).coerceIn(0f, 1f)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val leftPadding = 26.dp.toPx()
            val rightPadding = 8.dp.toPx()
            val bottomPadding = 18.dp.toPx()
            val topPadding = 24.dp.toPx() // space for floating tooltip badge

            val chartWidth = width - leftPadding - rightPadding
            val chartHeight = height - topPadding - bottomPadding

            // 1. Horizontal dashed grid lines for Y levels
            val gridLineColor = if (darkTheme) Color(0xFF334155) else Color(0xFFEDF2F7)
            val yLevels = listOf(40f, 30f, 20f, 10f)
            for (i in yLevels.indices) {
                val y = topPadding + (chartHeight * i / (yLevels.size - 1))
                drawLine(
                    color = gridLineColor,
                    start = Offset(leftPadding, y),
                    end = Offset(width - rightPadding, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )
            }

            // 2. Compute Points from filter.temps (mapped across 10°C to 40°C range)
            val minRange = 10f
            val maxRange = 40f
            val points = filter.temps.mapIndexed { idx, temp ->
                val x = leftPadding + (chartWidth * idx / (filter.temps.size - 1))
                val normalizedY = 1f - ((temp - minRange) / (maxRange - minRange)).coerceIn(0f, 1f)
                val y = topPadding + (chartHeight * normalizedY)
                Offset(x, y)
            }

            // 3. Smooth Cubic Bezier Spline Path
            val fullSplinePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val cx = (p0.x + p1.x) / 2f
                    cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                }
            }

            // Animate line clipping from left to right
            val clipRight = leftPadding + (chartWidth * chartAnimProgress)

            // Fill Path under spline
            val fillPath = Path().apply {
                addPath(fullSplinePath)
                lineTo(points.last().x, topPadding + chartHeight)
                lineTo(points.first().x, topPadding + chartHeight)
                close()
            }

            // Draw Area Fill under curve with clipping for entry animation
            clipRect(
                left = 0f,
                top = 0f,
                right = clipRight,
                bottom = height
            ) {
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            FluxOrange.copy(alpha = 0.28f),
                            FluxOrange.copy(alpha = 0.02f)
                        ),
                        startY = topPadding,
                        endY = topPadding + chartHeight
                    )
                )

                // Draw orange spline curve
                drawPath(
                    path = fullSplinePath,
                    color = FluxOrange,
                    style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 4. Highlighted Point & Tooltip at activeFraction
            val activeX = leftPadding + (chartWidth * activeFraction)
            val normalizedActiveY = 1f - ((rawTempC - minRange) / (maxRange - minRange)).coerceIn(0f, 1f)
            val activeY = topPadding + (chartHeight * normalizedActiveY)
            val pointPos = Offset(activeX, activeY)

            // Vertical dashed line down to chart bottom
            drawLine(
                color = if (darkTheme) Color(0xFF64748B) else Color(0xFFCBD5E1),
                start = Offset(pointPos.x, pointPos.y),
                end = Offset(pointPos.x, topPadding + chartHeight),
                strokeWidth = 1.3.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f), 0f)
            )

            // Outer white circle + orange border knob
            drawCircle(color = Color.White, radius = 5.5.dp.toPx(), center = pointPos)
            drawCircle(
                color = FluxOrange,
                radius = 5.5.dp.toPx(),
                center = pointPos,
                style = Stroke(width = 2.2.dp.toPx())
            )

            // Floating dark tooltip badge with downward triangle pointer
            val badgeW = 46.dp.toPx()
            val badgeH = 20.dp.toPx()
            val badgeX = (pointPos.x - badgeW / 2f).coerceIn(leftPadding, width - rightPadding - badgeW)
            val badgeY = pointPos.y - badgeH - 6.dp.toPx()

            // Badge capsule
            drawRoundRect(
                color = Color(0xFF1E232A),
                topLeft = Offset(badgeX, badgeY),
                size = Size(badgeW, badgeH),
                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
            )

            // Downward pointer triangle
            val trianglePath = Path().apply {
                moveTo(pointPos.x - 3.dp.toPx(), badgeY + badgeH)
                lineTo(pointPos.x + 3.dp.toPx(), badgeY + badgeH)
                lineTo(pointPos.x, badgeY + badgeH + 3.5.dp.toPx())
                close()
            }
            drawPath(path = trianglePath, color = Color(0xFF1E232A))
        }

        // Static Overlay for Y-Axis labels (40, 30, 20, 10 or Fahrenheit equivalent)
        val yLabels = if (isFahrenheit) listOf("104", "86", "68", "50") else listOf("40", "30", "20", "10")
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxHeight()
                .padding(top = 22.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            yLabels.forEach { label ->
                Text(label, color = FluxTextLight, fontSize = 8.5.sp, fontWeight = FontWeight.Medium)
            }
        }

        // X-Axis Timestamps (e.g. 10:20, 10:30, 10:40, 10:50, 11:00)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth()
                .padding(start = 28.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            filter.timestamps.forEach { timeStr ->
                Text(
                    text = timeStr,
                    color = FluxTextLight,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Tooltip text overlay aligned with the active marker
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val leftPaddingPx = 26.dp
            val rightPaddingPx = 8.dp
            val topPaddingPx = 24.dp
            val bottomPaddingPx = 18.dp

            val chartW = maxWidth - leftPaddingPx - rightPaddingPx
            val chartH = maxHeight - topPaddingPx - bottomPaddingPx

            val activeXPx = leftPaddingPx + (chartW * activeFraction)
            val normalizedActiveY = 1f - ((rawTempC - 10f) / 30f).coerceIn(0f, 1f)
            val activeYPx = topPaddingPx + (chartH * normalizedActiveY)

            val badgeWPx = 46.dp
            val badgeHPx = 20.dp
            val badgeLeft = (activeXPx - badgeWPx / 2).coerceIn(leftPaddingPx, maxWidth - rightPaddingPx - badgeWPx)
            val badgeTop = activeYPx - badgeHPx - 6.dp

            Box(
                modifier = Modifier
                    .offset(x = badgeLeft, y = badgeTop)
                    .size(badgeWPx, badgeHPx),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayTempStr,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
