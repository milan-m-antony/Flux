package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.outlined.Timer
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
import androidx.compose.ui.draw.scale
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
    val durationMillis: Long
) {
    LAST_1_HOUR("Last 1 Hour", "Live temperature over the last 1 hour", 60 * 60 * 1000L),
    LAST_6_HOURS("Last 6 Hours", "Temperature over the last 6 hours", 6 * 60 * 60 * 1000L),
    LAST_24_HOURS("Last 24 Hours", "Hourly temperature over the last 24 hours", 24 * 60 * 60 * 1000L),
    LAST_7_DAYS("Last 7 Days", "Daily temperature trend over the last 7 days", 7 * 24 * 60 * 60 * 1000L)
}

@Composable
fun FluxHomeScreen(
    viewModel: MainViewModel,
    onNavigateToHistory: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onNavigateToDeviceSetup: () -> Unit = {},
    onNavigateToStatsTimer: () -> Unit = {}
) {
    val latestReading by viewModel.latestReading.collectAsState()
    val allReadings by viewModel.allReadings.collectAsState()
    val preferredUnit by viewModel.preferredUnit.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val connectivityState by viewModel.connectivityState.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()
    val lowThreshold by viewModel.lowTempThreshold.collectAsState()
    val highThreshold by viewModel.highTempThreshold.collectAsState()

    val hasRealData = latestReading != null || allReadings.isNotEmpty() || connectionState == com.example.ble.ConnectionState.CONNECTED
    val currentTempC = latestReading?.temp ?: allReadings.firstOrNull()?.temperature ?: 0.0
    var selectedUnit by remember { mutableStateOf(preferredUnit) }

    val displayTemp = if (selectedUnit == "F") (currentTempC * 9 / 5) + 32 else currentTempC
    val unitSymbol = if (selectedUnit == "F") "°F" else "°C"

    val animatedDisplayTemp by animateFloatAsState(
        targetValue = displayTemp.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "animatedDisplayTemp"
    )

    val colors = rememberFluxColors(darkTheme = false, activeProfile = activeProfile)

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

            val isDeviceOnline = connectivityState.isOnline || (viewModel.bleManager.isBluetoothEnabled() && connectionState == com.example.ble.ConnectionState.CONNECTED)
            // 2. Large Circular Temperature Gauge (Slightly reduced from 290.dp to 265.dp to preserve generous size)
            TemperatureGauge(
                temperature = currentTempC.toFloat(),
                minTemp = 16f,
                maxTemp = 40f,
                unitSymbol = unitSymbol,
                displayTempStr = if (hasRealData && (isDeviceOnline || allReadings.isNotEmpty())) String.format(java.util.Locale.US, "%.1f", animatedDisplayTemp) else "--.-",
                hasData = hasRealData && (isDeviceOnline || allReadings.isNotEmpty()),
                isLive = isDeviceOnline,
                darkTheme = false,
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
                            .background(if (selectedUnit == "C") colors.primary else Color.Transparent)
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
                            .background(if (selectedUnit == "F") colors.primary else Color.Transparent)
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

                // Action Buttons Row: Session Stats/Timer + History/Trend
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Session Timer & Stats Shortcut
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(3.dp, CircleShape, spotColor = Color(0x10000000))
                            .clip(CircleShape)
                            .background(colors.cardBg)
                            .border(0.5.dp, colors.cardBorder, CircleShape)
                            .clickable { onNavigateToStatsTimer() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = "Session Stats & Timer",
                            tint = FluxOrange,
                            modifier = Modifier.size(19.dp)
                        )
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
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active Profile Kinetic Display Card
            ActiveProfileDisplayBanner(
                activeProfile = activeProfile,
                lowThreshold = lowThreshold,
                highThreshold = highThreshold,
                unitSymbol = unitSymbol,
                isFahrenheit = selectedUnit == "F",
                colors = colors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Temperature Trend Card (Interactive Graph, Filter, Min/Max/Avg Stat Cards)
            TemperatureTrendCard(
                unitSymbol = unitSymbol,
                isFahrenheit = selectedUnit == "F",
                readings = allReadings,
                darkTheme = false,
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
    isLive: Boolean = true,
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

    // Animated breathing radio wave aura on thumb knob when live
    val infiniteTransition = rememberInfiniteTransition(label = "gaugePulse")
    val knobPulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "knobPulse"
    )
    val knobPulseProgress2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 750, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "knobPulse2"
    )
    val liveDotPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveDotPulse"
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

            // C. Active Dynamic Gradient Arc
            val activeSweep = sweepTotalAngle * animatedProgress
            if (activeSweep > 0) {
                // Subtle ambient glow behind active arc
                drawArc(
                    color = colors.primary.copy(alpha = 0.20f),
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Round)
                )

                // Main vibrant profile gradient stroke
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            colors.primaryGradientStart,
                            colors.primaryGradientEnd,
                            colors.primary
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
            // Animated breathing radio aura rings when live
            if (isLive) {
                drawCircle(
                    color = colors.primary.copy(alpha = (1f - knobPulseProgress) * 0.40f),
                    radius = (10.dp.toPx() + knobPulseProgress * 14.dp.toPx()),
                    center = thumbPos
                )
                drawCircle(
                    color = colors.primary.copy(alpha = (1f - knobPulseProgress2) * 0.25f),
                    radius = (10.dp.toPx() + knobPulseProgress2 * 22.dp.toPx()),
                    center = thumbPos
                )
            }
            // Outer white thumb with crisp profile border
            drawCircle(
                color = Color.White,
                radius = 10.dp.toPx(),
                center = thumbPos
            )
            drawCircle(
                color = colors.primary,
                radius = 10.dp.toPx(),
                center = thumbPos,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // Ambient Thermal Aura Glow (Warm amber-red when hot, cool cyan when cold, profile-themed when optimal)
        val thermalAuraColor = when {
            temperature > 28f -> Color(0xFFEF4444)
            temperature < 18f -> Color(0xFF38BDF8)
            else -> colors.primary
        }
        Box(
            modifier = Modifier
                .size(164.dp)
                .offset(y = 5.dp)
                .clip(CircleShape)
                .background(thermalAuraColor.copy(alpha = if (hasData) 0.12f else 0f))
        )

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
                // Thermometer icon tinted with active profile primary color
                Icon(
                    imageVector = Icons.Outlined.Thermostat,
                    contentDescription = null,
                    tint = colors.primary,
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
                            .size(7.dp)
                            .scale(if (isLive) liveDotPulse else 1f)
                            .clip(CircleShape)
                            .background(if (isLive) FluxGreenOnline else Color(0xFF94A3B8))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLive) "Live Stream" else "Offline",
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

    val filteredReadings = remember(readings, selectedFilter) {
        val cutoff = System.currentTimeMillis() - selectedFilter.durationMillis
        val list = readings.filter { it.timestamp >= cutoff }
        if (list.isNotEmpty()) list else readings
    }

    val hasRealTelemetry = filteredReadings.isNotEmpty()
    val realMinTemp = if (hasRealTelemetry) filteredReadings.minOf { it.temperature }.toFloat() else null
    val realMaxTemp = if (hasRealTelemetry) filteredReadings.maxOf { it.temperature }.toFloat() else null
    val realAvgTemp = if (hasRealTelemetry) filteredReadings.map { it.temperature }.average().toFloat() else null

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

                // Interactive Dropdown capsule "Last 1 Hour"
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
                    readings = filteredReadings,
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
                    value = convertTemp(realMinTemp),
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                // Max Card: Light peach container + orange up arrow
                SummaryStatCard(
                    iconBgColor = if (darkTheme) Color(0xFF332014) else Color(0xFFFFEDE3),
                    iconTint = Color(0xFFEA580C),
                    icon = Icons.Default.North,
                    label = "Max",
                    value = convertTemp(realMaxTemp),
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                // Average Card: Light slate container + slate bar chart
                SummaryStatCard(
                    iconBgColor = if (darkTheme) Color(0xFF262F3E) else Color(0xFFF0F3FA),
                    iconTint = Color(0xFF64748B),
                    icon = Icons.Outlined.BarChart,
                    label = "Average",
                    value = convertTemp(realAvgTemp),
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "statCardScale"
    )

    Row(
        modifier = modifier
            .scale(cardScale)
            .clickable(interactionSource = interactionSource, indication = null) {}
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
    readings: List<com.example.data.Reading> = emptyList(),
    unitSymbol: String,
    isFahrenheit: Boolean = false,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = false,
    colors: FluxColors = rememberFluxColors(darkTheme)
) {
    val hasRealData = readings.isNotEmpty()
    val realTemps = remember(readings) {
        if (readings.isEmpty()) emptyList()
        else {
            val count = readings.size
            if (count > 20) {
                val step = count / 15
                readings.filterIndexed { i, _ -> i % step == 0 }.takeLast(15).map { it.temperature.toFloat() }
            } else {
                readings.map { it.temperature.toFloat() }
            }
        }
    }

    // Animated progress to draw in line and fill when filter changes or on load
    val animProgress = remember { Animatable(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "homeBeacon")
    val beaconPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.2f,
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
    LaunchedEffect(filter, readings.size) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }
    val chartAnimProgress = animProgress.value

    var isDragging by remember { mutableStateOf(false) }
    var touchFraction by remember { mutableStateOf(1f) }

    val activeFraction = if (isDragging) touchFraction else 1f

    val rawTempC = if (!hasRealData) {
        0f
    } else if (realTemps.size == 1) {
        realTemps.first()
    } else {
        val activeIndexFloat = activeFraction * (realTemps.size - 1)
        val lowerIndex = activeIndexFloat.toInt().coerceIn(0, realTemps.size - 1)
        val upperIndex = (lowerIndex + 1).coerceAtMost(realTemps.size - 1)
        val fractionBetween = activeIndexFloat - lowerIndex
        if (lowerIndex == upperIndex) {
            realTemps[lowerIndex]
        } else {
            realTemps[lowerIndex] + fractionBetween * (realTemps[upperIndex] - realTemps[lowerIndex])
        }
    }

    val displayTempStr = if (isFahrenheit) {
        String.format("%.1f%s", (rawTempC * 9 / 5) + 32, unitSymbol)
    } else {
        String.format("%.1f%s", rawTempC, unitSymbol)
    }

    Box(
        modifier = modifier
            .then(
                if (hasRealData && realTemps.size > 1) {
                    Modifier
                        .pointerInput(filter, realTemps) {
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
                        .pointerInput(filter, realTemps) {
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
                } else Modifier
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val leftPadding = 26.dp.toPx()
            val rightPadding = 8.dp.toPx()
            val bottomPadding = 18.dp.toPx()
            val topPadding = 24.dp.toPx()

            val chartWidth = width - leftPadding - rightPadding
            val chartHeight = height - topPadding - bottomPadding

            // 1. Horizontal dashed grid lines
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

            if (hasRealData) {
                val minRange = 10f
                val maxRange = 40f
                val points = if (realTemps.size == 1) {
                    val normalizedY = 1f - ((realTemps[0] - minRange) / (maxRange - minRange)).coerceIn(0f, 1f)
                    val y = topPadding + (chartHeight * normalizedY)
                    listOf(Offset(leftPadding, y), Offset(leftPadding + chartWidth, y))
                } else {
                    realTemps.mapIndexed { idx, temp ->
                        val x = leftPadding + (chartWidth * idx / (realTemps.size - 1))
                        val normalizedY = 1f - ((temp - minRange) / (maxRange - minRange)).coerceIn(0f, 1f)
                        val y = topPadding + (chartHeight * normalizedY)
                        Offset(x, y)
                    }
                }

                val fullSplinePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val cx = (p0.x + p1.x) / 2f
                        cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }
                }

                val clipRight = leftPadding + (chartWidth * chartAnimProgress)
                val fillPath = Path().apply {
                    addPath(fullSplinePath)
                    lineTo(points.last().x, topPadding + chartHeight)
                    lineTo(points.first().x, topPadding + chartHeight)
                    close()
                }

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
                                colors.primary.copy(alpha = 0.28f),
                                colors.primary.copy(alpha = 0.02f)
                            ),
                            startY = topPadding,
                            endY = topPadding + chartHeight
                        )
                    )

                    drawPath(
                        path = fullSplinePath,
                        color = colors.primary,
                        style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Highlighted point
                val activeX = leftPadding + (chartWidth * activeFraction)
                val normalizedActiveY = 1f - ((rawTempC - minRange) / (maxRange - minRange)).coerceIn(0f, 1f)
                val activeY = topPadding + (chartHeight * normalizedActiveY)
                val pointPos = Offset(activeX, activeY)

                drawLine(
                    color = if (darkTheme) Color(0xFF64748B) else Color(0xFFCBD5E1),
                    start = Offset(pointPos.x, pointPos.y),
                    end = Offset(pointPos.x, topPadding + chartHeight),
                    strokeWidth = 1.3.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f), 0f)
                )

                // Outer glowing pulse ring
                if (chartAnimProgress > 0.8f) {
                    drawCircle(
                        color = colors.primary.copy(alpha = beaconPulseAlpha),
                        radius = 5.5.dp.toPx() * beaconPulseScale,
                        center = pointPos
                    )
                }

                drawCircle(color = Color.White, radius = 5.5.dp.toPx(), center = pointPos)
                drawCircle(
                    color = colors.primary,
                    radius = 5.5.dp.toPx(),
                    center = pointPos,
                    style = Stroke(width = 2.2.dp.toPx())
                )

                val badgeW = 46.dp.toPx()
                val badgeH = 20.dp.toPx()
                val badgeLeft = (activeX - badgeW / 2f).coerceIn(leftPadding, width - rightPadding - badgeW)
                val badgeY = activeY - badgeH - 6.dp.toPx()

                drawRoundRect(
                    color = colors.primary,
                    topLeft = Offset(badgeLeft, badgeY),
                    size = Size(badgeW, badgeH),
                    cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                )

                val trianglePath = Path().apply {
                    moveTo(pointPos.x - 3.dp.toPx(), badgeY + badgeH)
                    lineTo(pointPos.x + 3.dp.toPx(), badgeY + badgeH)
                    lineTo(pointPos.x, badgeY + badgeH + 3.5.dp.toPx())
                    close()
                }
                drawPath(path = trianglePath, color = colors.primary)
            }
        }

        if (!hasRealData) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Awaiting Telemetry Readings",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Data will plot here automatically when connected",
                    color = colors.textSecondary.copy(alpha = 0.65f),
                    fontSize = 10.5.sp
                )
            }
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

        if (hasRealData) {
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
}

@Composable
private fun ActiveProfileDisplayBanner(
    activeProfile: String,
    lowThreshold: Double,
    highThreshold: Double,
    unitSymbol: String,
    isFahrenheit: Boolean,
    colors: FluxColors,
    modifier: Modifier = Modifier
) {
    val lowDisp = if (isFahrenheit) (lowThreshold * 9 / 5) + 32 else lowThreshold
    val highDisp = if (isFahrenheit) (highThreshold * 9 / 5) + 32 else highThreshold
    val presetLabel = when {
        activeProfile.contains("Environment", ignoreCase = true) -> "Preset Safety: 18°C – 26°C"
        activeProfile.contains("Liquid", ignoreCase = true) -> "Preset Safety: 10°C – 35°C"
        activeProfile.contains("Fermentation", ignoreCase = true) -> "Preset Safety: 18°C – 24°C"
        else -> "Safety Limits: %.1f%s – %.1f%s".format(lowDisp, unitSymbol, highDisp, unitSymbol)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bannerAnim")
    val bannerWavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * kotlin.math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bannerWavePhase"
    )
    val sporeFloat by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sporeFloat"
    )

    Box(
        modifier = modifier
            .shadow(3.dp, RoundedCornerShape(18.dp), spotColor = Color(0x10000000))
            .clip(RoundedCornerShape(18.dp))
            .background(colors.cardBg)
            .border(0.5.dp, colors.primaryBorder.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Profile Animation Visual Canvas Mini-Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.primarySoft),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(24.dp)) {
                        val w = size.width
                        val h = size.height
                        when {
                            activeProfile.contains("Environment", ignoreCase = true) -> {
                                drawCircle(
                                    color = colors.primary,
                                    radius = 2.8.dp.toPx(),
                                    center = Offset(w * 0.35f, h * 0.5f + sporeFloat)
                                )
                                drawCircle(
                                    color = colors.primary.copy(alpha = 0.7f),
                                    radius = 2.0.dp.toPx(),
                                    center = Offset(w * 0.65f, h * 0.35f - sporeFloat)
                                )
                                drawCircle(
                                    color = colors.primary.copy(alpha = 0.5f),
                                    radius = 1.6.dp.toPx(),
                                    center = Offset(w * 0.7f, h * 0.7f + (sporeFloat * 0.6f))
                                )
                            }
                            activeProfile.contains("Liquid", ignoreCase = true) -> {
                                val wavePath = Path().apply {
                                    moveTo(0f, h * 0.5f)
                                    for (x in 0..w.toInt() step 2) {
                                        val y = h * 0.5f + sin(x * 0.35f + bannerWavePhase) * 4f
                                        lineTo(x.toFloat(), y)
                                    }
                                }
                                drawPath(
                                    path = wavePath,
                                    color = colors.primary,
                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            activeProfile.contains("Fermentation", ignoreCase = true) -> {
                                drawCircle(
                                    color = colors.primary,
                                    radius = 3.dp.toPx(),
                                    center = Offset(w * 0.5f, h * 0.4f + (sporeFloat * 1.2f))
                                )
                                drawCircle(
                                    color = colors.primary.copy(alpha = 0.6f),
                                    radius = 2.dp.toPx(),
                                    center = Offset(w * 0.3f, h * 0.6f - (sporeFloat * 0.8f))
                                )
                                drawCircle(
                                    color = colors.primary.copy(alpha = 0.4f),
                                    radius = 1.5.dp.toPx(),
                                    center = Offset(w * 0.75f, h * 0.55f + sporeFloat)
                                )
                            }
                            else -> {
                                drawCircle(
                                    color = colors.primary.copy(alpha = 0.35f),
                                    radius = 8.dp.toPx() * (1f + (sporeFloat + 3f) / 10f),
                                    style = Stroke(width = 1.2.dp.toPx())
                                )
                                drawCircle(
                                    color = colors.primary,
                                    radius = 3.dp.toPx(),
                                    center = Offset(w / 2f, h / 2f)
                                )
                            }
                        }
                    }
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = activeProfile,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.primarySoft)
                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = colors.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                    Text(
                        text = presetLabel,
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.pillBg)
                    .border(0.5.dp, colors.pillBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "Theme Active",
                    color = colors.primary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
