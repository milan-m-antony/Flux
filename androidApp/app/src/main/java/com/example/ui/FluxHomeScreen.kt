package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FluxHomeScreen(
    viewModel: MainViewModel,
    onNavigateToHistory: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    val latestReading by viewModel.latestReading.collectAsState()
    val preferredUnit by viewModel.preferredUnit.collectAsState()
    val lowThreshold by viewModel.lowTempThreshold.collectAsState()
    val highThreshold by viewModel.highTempThreshold.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()

    val currentTempC = latestReading?.temp ?: 28.4
    var selectedUnit by remember { mutableStateOf(preferredUnit) }

    val displayTemp = if (selectedUnit == "F") (currentTempC * 9 / 5) + 32 else currentTempC
    val unitSymbol = if (selectedUnit == "F") "°F" else "°C"

    val isOnline = connectionState == com.example.ble.ConnectionState.CONNECTED || true

    val darkTheme by viewModel.isDarkTheme.collectAsState()
    val colors = rememberFluxColors(darkTheme)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Spacer to clear the fixed top header
            Spacer(modifier = Modifier.height(72.dp))

            // 2. Large Circular Temperature Gauge
            TemperatureGauge(
                temperature = currentTempC.toFloat(),
                minTemp = 16f,
                maxTemp = 40f,
                unitSymbol = unitSymbol,
                displayTempStr = String.format("%.1f", displayTemp),
                darkTheme = darkTheme,
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Segmented Unit Selector (°C / °F) and Trend Button Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // °C / °F Segmented Capsule
                Row(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color(0x10000000))
                        .background(colors.cardBg, RoundedCornerShape(24.dp))
                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(24.dp))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selectedUnit == "C") (if (darkTheme) FluxOrange else Color(0xFF111827)) else Color.Transparent)
                            .clickable {
                                selectedUnit = "C"
                                viewModel.setPreferredUnit("C")
                            }
                            .padding(horizontal = 22.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "°C",
                            color = if (selectedUnit == "C") Color.White else colors.textSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selectedUnit == "F") (if (darkTheme) FluxOrange else Color(0xFF111827)) else Color.Transparent)
                            .clickable {
                                selectedUnit = "F"
                                viewModel.setPreferredUnit("F")
                            }
                            .padding(horizontal = 22.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "°F",
                            color = if (selectedUnit == "F") Color.White else colors.textSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Trend/Chart Action Button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(4.dp, CircleShape, spotColor = Color(0x12000000))
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
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Temperature Trend Card
            TemperatureTrendCard(
                darkTheme = darkTheme,
                colors = colors,
                modifier = Modifier.fillMaxWidth()
            )

            // Extra padding at bottom to clear floating navigation bar
            Spacer(modifier = Modifier.height(110.dp))
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

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Top "Medium" needle marker label
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Medium",
                color = colors.textPrimary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Canvas(modifier = Modifier.size(8.dp, 6.dp)) {
                val p = Path().apply {
                    moveTo(size.width / 2f, size.height)
                    lineTo(0f, 0f)
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(p, color = colors.textPrimary)
            }
        }

        // Circular Gauge Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f + 8f)
            val radius = (size.width.coerceAtMost(size.height) / 2f) - 24f

            val startAngle = 145f
            val sweepTotalAngle = 250f

            // 1. Tick marks around outer ring
            val numTicks = 42
            val tickRadiusOuter = radius + 22f
            val tickRadiusInner = radius + 15f
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
                    color = Color(0xFFCBD5E1),
                    start = p1,
                    end = p2,
                    strokeWidth = 1.5f,
                    cap = StrokeCap.Round
                )
            }

            // 2. Inactive Gray Arc Track
            drawArc(
                color = Color(0xFFE2E8F0),
                startAngle = startAngle,
                sweepAngle = sweepTotalAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 24f, cap = StrokeCap.Round)
            )

            // 3. Active Orange Gradient Arc
            val activeSweep = sweepTotalAngle * animatedProgress
            if (activeSweep > 0) {
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
                    style = Stroke(width = 24f, cap = StrokeCap.Round)
                )
            }

            // 4. Indicator Thumb Knob on Arc
            val thumbAngleRad = Math.toRadians((startAngle + activeSweep).toDouble())
            val thumbPos = Offset(
                (center.x + radius * cos(thumbAngleRad)).toFloat(),
                (center.y + radius * sin(thumbAngleRad)).toFloat()
            )
            // Outer white thumb with orange border
            drawCircle(
                color = Color.White,
                radius = 16f,
                center = thumbPos
            )
            drawCircle(
                color = FluxOrange,
                radius = 16f,
                center = thumbPos,
                style = Stroke(width = 4f)
            )
        }

        // Inner Elevated White Card
        Box(
            modifier = Modifier
                .size(175.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    spotColor = Color(0x16000000),
                    ambientColor = Color(0x0A000000)
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
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Big bold reading
                Text(
                    text = "$displayTempStr$unitSymbol",
                    color = colors.textPrimary,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Stable status with orange dot
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(FluxOrange)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Stable",
                        color = FluxTextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Bottom Range Labels: 16°C and 40°C
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 46.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (-6).dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${minTemp.toInt()}$unitSymbol",
                color = FluxTextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${maxTemp.toInt()}$unitSymbol",
                color = FluxTextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TemperatureTrendCard(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = false,
    colors: FluxColors = rememberFluxColors(darkTheme)
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0x12000000)
            )
            .background(colors.cardBg, RoundedCornerShape(24.dp))
            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(24.dp))
            .padding(18.dp)
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
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Live temperature over the last 1 hour",
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Dropdown capsule "Last 1 Hour ⌵"
                Row(
                    modifier = Modifier
                        .background(colors.pillBg, RoundedCornerShape(16.dp))
                        .border(0.5.dp, colors.pillBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Last 1 Hour",
                        color = colors.textPrimary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Smooth Wavy Spline Chart with Selected Peak Indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                TrendChartCanvas(
                    modifier = Modifier.fillMaxSize(),
                    darkTheme = darkTheme
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary row: Min, Max, Avg pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryStatPill(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Thermostat,
                            contentDescription = null,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    value = "26.1°C",
                    label = "Min",
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                SummaryStatPill(
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (darkTheme) Color(0xFF332014) else Color(0xFFFFF2EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Thermostat,
                                contentDescription = null,
                                tint = FluxOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    value = "30.8°C",
                    label = "Max",
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                SummaryStatPill(
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (darkTheme) Color(0xFF334155) else Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BarChart,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    value = "28.3°C",
                    label = "Avg",
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryStatPill(
    icon: @Composable () -> Unit,
    value: String,
    label: String,
    colors: FluxColors,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(colors.pillBg, RoundedCornerShape(16.dp))
            .border(0.5.dp, colors.pillBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = value,
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = colors.textSecondary,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TrendChartCanvas(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = false
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val leftPadding = 32f
        val bottomPadding = 24f
        val chartWidth = width - leftPadding
        val chartHeight = height - bottomPadding

        // 1. Horizontal dashed grid lines & Y labels (10, 20, 30, 40)
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

        // 2. Data Points for the smooth spline (16 to 34°C range)
        val points = listOf(
            Offset(leftPadding + chartWidth * 0.00f, chartHeight * 0.70f),
            Offset(leftPadding + chartWidth * 0.12f, chartHeight * 0.62f),
            Offset(leftPadding + chartWidth * 0.22f, chartHeight * 0.68f),
            Offset(leftPadding + chartWidth * 0.35f, chartHeight * 0.52f),
            Offset(leftPadding + chartWidth * 0.48f, chartHeight * 0.58f),
            Offset(leftPadding + chartWidth * 0.62f, chartHeight * 0.40f),
            Offset(leftPadding + chartWidth * 0.75f, chartHeight * 0.48f),
            Offset(leftPadding + chartWidth * 0.88f, chartHeight * 0.22f), // Peak around 11:00
            Offset(leftPadding + chartWidth * 1.00f, chartHeight * 0.38f)
        )

        // Cubic Bezier Spline Path
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val cx = (p0.x + p1.x) / 2f
                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
            }
        }

        // Fill Path under spline
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
                    FluxOrange.copy(alpha = 0.32f),
                    FluxOrange.copy(alpha = 0.03f)
                ),
                startY = 0f,
                endY = chartHeight
            )
        )

        // Draw orange line curve
        drawPath(
            path = path,
            color = FluxOrange,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // 3. Peak Indicator at 11:00 (points[7])
        val peak = points[7]
        // Vertical dashed line down to bottom
        drawLine(
            color = Color(0xFFCBD5E1),
            start = Offset(peak.x, peak.y),
            end = Offset(peak.x, chartHeight),
            strokeWidth = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
        )

        // Outer white circle + orange border
        drawCircle(color = Color.White, radius = 6.dp.toPx(), center = peak)
        drawCircle(color = FluxOrange, radius = 6.dp.toPx(), center = peak, style = Stroke(width = 2.5.dp.toPx()))

        // Floating tooltip badge: "28.4°C"
        val badgeW = 44.dp.toPx()
        val badgeH = 20.dp.toPx()
        val badgeX = peak.x - (badgeW / 2f)
        val badgeY = peak.y - badgeH - 6.dp.toPx()
        drawRoundRect(
            color = Color(0xFF1E232A),
            topLeft = Offset(badgeX, badgeY),
            size = Size(badgeW, badgeH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )
    }

    // Static overlay for X & Y axis text labels
    Box(modifier = modifier) {
        // Y Axis Labels
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

        // X Axis Timestamps
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth()
                .padding(start = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("10:20", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            Text("10:30", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            Text("10:40", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            Text("10:50", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            Text("11:00", color = FluxTextLight, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }

        // Tooltip text overlay
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-38).dp, y = 14.dp)
        ) {
            Text(
                text = "28.4°C",
                color = Color.White,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
