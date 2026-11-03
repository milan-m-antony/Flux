package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Reading
import com.example.ui.theme.FluxColors
import com.example.ui.theme.FluxTextLight
import com.example.ui.theme.rememberFluxColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

private data class ChartPoint(
    val timestamp: Long,
    val rawTemp: Double,
    val dispTemp: Float,
    val timeFormatted: String
)

@Composable
fun FluxTrendGraph(
    readings: List<Reading>,
    modifier: Modifier = Modifier,
    unitSymbol: String = "°C",
    isFahrenheit: Boolean = false,
    colors: FluxColors = rememberFluxColors(),
    chartHeight: Dp = 84.dp,
    showTimeAxis: Boolean = false,
    showYAxis: Boolean = true,
    interactive: Boolean = true
) {
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val axisTimeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    // 1. Process and downsample readings for silky smooth 60fps canvas performance
    val processedPoints = remember(readings, isFahrenheit) {
        if (readings.isEmpty()) {
            emptyList()
        } else {
            val converted = readings.map { r ->
                val disp = if (isFahrenheit) (r.temperature * 9 / 5) + 32 else r.temperature
                ChartPoint(
                    timestamp = r.timestamp,
                    rawTemp = r.temperature,
                    dispTemp = disp.toFloat(),
                    timeFormatted = timeFormatter.format(Date(r.timestamp))
                )
            }
            // Decimate down to max 28 points to preserve battery and GPU rendering
            if (converted.size > 28) {
                val step = converted.size / 24
                val sampled = converted.filterIndexed { i, _ -> i % step == 0 }.takeLast(24).toMutableList()
                if (sampled.last().timestamp != converted.last().timestamp) {
                    sampled.add(converted.last())
                }
                sampled
            } else {
                converted
            }
        }
    }

    val hasData = processedPoints.isNotEmpty()

    // 2. Animated Entrance & Transitions
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(processedPoints.size, isFahrenheit) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }
    val chartAnimProgress = animProgress.value

    // 3. Real-Time Beacon Pulse Animation
    val infiniteTransition = rememberInfiniteTransition(label = "trendGraphAnim")
    val beaconPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beaconScale"
    )
    val beaconPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beaconAlpha"
    )

    // Breathing wave phase for subtle life in the telemetry curve
    val waveGlowPhase by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveGlow"
    )

    // 4. Interactive Touch Scrubbing State
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubFraction by remember { mutableStateOf(1f) }

    val activeFraction = if (isScrubbing) scrubFraction else 1f

    // Active point calculation via smooth interpolation
    val activePoint = remember(processedPoints, activeFraction) {
        if (!hasData) null
        else if (processedPoints.size == 1) processedPoints.first()
        else {
            val floatIdx = activeFraction * (processedPoints.size - 1)
            val lower = floatIdx.toInt().coerceIn(0, processedPoints.size - 1)
            val upper = (lower + 1).coerceAtMost(processedPoints.size - 1)
            val frac = floatIdx - lower
            if (lower == upper) {
                processedPoints[lower]
            } else {
                val p0 = processedPoints[lower]
                val p1 = processedPoints[upper]
                val interpolatedTemp = p0.dispTemp + frac * (p1.dispTemp - p0.dispTemp)
                val interpolatedTs = (p0.timestamp + frac * (p1.timestamp - p0.timestamp)).toLong()
                ChartPoint(
                    timestamp = interpolatedTs,
                    rawTemp = (p0.rawTemp + frac * (p1.rawTemp - p0.rawTemp)),
                    dispTemp = interpolatedTemp,
                    timeFormatted = timeFormatter.format(Date(interpolatedTs))
                )
            }
        }
    }

    // 5. Adaptive Dynamic Y-Axis Bounds (Guarantees visible waves even with small 0.4°C fluctuations)
    val (yMin, yMax) = remember(processedPoints, isFahrenheit) {
        if (!hasData) {
            if (isFahrenheit) Pair(60f, 90f) else Pair(16f, 32f)
        } else {
            val minT = processedPoints.minOf { it.dispTemp }
            val maxT = processedPoints.maxOf { it.dispTemp }
            val delta = maxT - minT
            val minSpan = if (isFahrenheit) 5.5f else 3.2f
            val span = max(minSpan, delta * 1.35f)
            val mid = (maxT + minT) / 2f
            Pair(mid - span / 2f, mid + span / 2f)
        }
    }
    val yRange = (yMax - yMin).coerceAtLeast(1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
            .then(
                if (interactive && hasData && processedPoints.size > 1) {
                    Modifier
                        .pointerInput(processedPoints) {
                            detectTapGestures(
                                onPress = { offset ->
                                    isScrubbing = true
                                    val leftPad = if (showYAxis) 26.dp.toPx() else 8.dp.toPx()
                                    val rightPad = 8.dp.toPx()
                                    val cWidth = size.width - leftPad - rightPad
                                    if (cWidth > 0) {
                                        scrubFraction = ((offset.x - leftPad) / cWidth).coerceIn(0f, 1f)
                                    }
                                    tryAwaitRelease()
                                    isScrubbing = false
                                }
                            )
                        }
                        .pointerInput(processedPoints) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    isScrubbing = true
                                    val leftPad = if (showYAxis) 26.dp.toPx() else 8.dp.toPx()
                                    val rightPad = 8.dp.toPx()
                                    val cWidth = size.width - leftPad - rightPad
                                    if (cWidth > 0) {
                                        scrubFraction = ((offset.x - leftPad) / cWidth).coerceIn(0f, 1f)
                                    }
                                },
                                onDragEnd = { isScrubbing = false },
                                onDragCancel = { isScrubbing = false }
                            ) { change, _ ->
                                val leftPad = if (showYAxis) 26.dp.toPx() else 8.dp.toPx()
                                val rightPad = 8.dp.toPx()
                                val cWidth = size.width - leftPad - rightPad
                                if (cWidth > 0) {
                                    scrubFraction = ((change.position.x - leftPad) / cWidth).coerceIn(0f, 1f)
                                }
                            }
                        }
                } else Modifier
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val leftPad = if (showYAxis) 26.dp.toPx() else 8.dp.toPx()
            val rightPad = 8.dp.toPx()
            val topPad = 12.dp.toPx()
            val bottomPad = if (showTimeAxis) 22.dp.toPx() else 10.dp.toPx()

            val chartWidth = width - leftPad - rightPad
            val chartHeightPx = height - topPad - bottomPad

            // 1. Horizontal Guidelines (4 dashed lines)
            val gridColor = Color(0xFFF1F5F9)
            val steps = 3
            for (i in 0..steps) {
                val y = topPad + (chartHeightPx * i / steps)
                drawLine(
                    color = gridColor,
                    start = Offset(leftPad, y),
                    end = Offset(width - rightPad, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                )
            }

            if (hasData) {
                // Map points to canvas coordinates
                val coords = if (processedPoints.size == 1) {
                    val normY = 1f - ((processedPoints[0].dispTemp - yMin) / yRange).coerceIn(0f, 1f)
                    val y = topPad + (chartHeightPx * normY)
                    listOf(Offset(leftPad, y), Offset(leftPad + chartWidth, y))
                } else {
                    processedPoints.mapIndexed { i, p ->
                        val x = leftPad + (chartWidth * i / (processedPoints.size - 1))
                        val normY = 1f - ((p.dispTemp - yMin) / yRange).coerceIn(0f, 1f)
                        val y = topPad + (chartHeightPx * normY)
                        Offset(x, y.coerceIn(topPad, topPad + chartHeightPx))
                    }
                }

                // 2. Build Smooth Catmull-Rom Bezier Spline
                val splinePath = Path().apply {
                    moveTo(coords.first().x, coords.first().y)
                    val tension = 0.35f
                    for (i in 0 until coords.size - 1) {
                        val p0 = if (i > 0) coords[i - 1] else coords[i]
                        val p1 = coords[i]
                        val p2 = coords[i + 1]
                        val p3 = if (i + 2 < coords.size) coords[i + 2] else p2

                        val cp1x = p1.x + (p2.x - p0.x) * tension
                        val cp1y = p1.y + (p2.y - p0.y) * tension
                        val cp2x = p2.x - (p3.x - p1.x) * tension
                        val cp2y = p2.y - (p3.y - p1.y) * tension

                        cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
                    }
                }

                val clipRight = leftPad + (chartWidth * chartAnimProgress)

                // 3. Ambient Translucent Gradient Fill
                val fillPath = Path().apply {
                    addPath(splinePath)
                    lineTo(coords.last().x, topPad + chartHeightPx)
                    lineTo(coords.first().x, topPad + chartHeightPx)
                    close()
                }

                clipRect(left = 0f, top = 0f, right = clipRight, bottom = height) {
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colors.primary.copy(alpha = 0.30f * waveGlowPhase),
                                colors.primary.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            startY = topPad,
                            endY = topPad + chartHeightPx
                        )
                    )

                    // Ambient Neon Glow Stroke (wide soft layer)
                    drawPath(
                        path = splinePath,
                        color = colors.primary.copy(alpha = 0.18f * waveGlowPhase),
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Core Vibrant Gradient Line
                    drawPath(
                        path = splinePath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(colors.primaryGradientStart, colors.primaryGradientEnd),
                            startX = leftPad,
                            endX = leftPad + chartWidth
                        ),
                        style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // 4. Live Beacon / Scrubbed Point Highlighting
                val activeX = leftPad + (chartWidth * activeFraction)
                val activeNormY = 1f - (((activePoint?.dispTemp ?: yMin) - yMin) / yRange).coerceIn(0f, 1f)
                val activeY = topPad + (chartHeightPx * activeNormY)
                val pt = Offset(activeX, activeY)

                if (chartAnimProgress > 0.7f) {
                    // Vertical scrub guide line
                    if (isScrubbing) {
                        drawLine(
                            color = colors.primary.copy(alpha = 0.6f),
                            start = Offset(pt.x, topPad),
                            end = Offset(pt.x, topPad + chartHeightPx),
                            strokeWidth = 1.2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                        )
                    }

                    // Concentric Radar Beacon Rings
                    drawCircle(
                        color = colors.primary.copy(alpha = beaconPulseAlpha),
                        radius = 6.dp.toPx() * beaconPulseScale,
                        center = pt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 5.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = colors.primary,
                        radius = 5.dp.toPx(),
                        center = pt,
                        style = Stroke(width = 2.4.dp.toPx())
                    )
                }
            }
        }

        // Empty state
        if (!hasData) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Awaiting Telemetry Readings",
                    color = colors.textSecondary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Real-time thermal trend plots here automatically",
                    color = colors.textSecondary.copy(alpha = 0.65f),
                    fontSize = 10.sp
                )
            }
        }

        // 6. Dynamic Y-Axis Labels (adaptive to actual readings)
        if (showYAxis && hasData) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxHeight()
                    .padding(top = 10.dp, bottom = if (showTimeAxis) 24.dp else 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val step = yRange / 3f
                for (i in 0..3) {
                    val mark = yMax - (step * i)
                    Text(
                        text = "%.0f°".format(mark),
                        color = FluxTextLight,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 7. Time Axis Labels (for History screen or expanded cards)
        if (showTimeAxis && hasData && processedPoints.size >= 2) {
            val startTs = processedPoints.first().timestamp
            val endTs = processedPoints.last().timestamp
            val timeStep = (endTs - startTs) / 3

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth()
                    .padding(start = if (showYAxis) 26.dp else 10.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0..3) {
                    val t = startTs + (timeStep * i)
                    Text(
                        text = axisTimeFormatter.format(Date(t)),
                        color = FluxTextLight,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 8. Floating Scrub Tooltip
        if (isScrubbing && activePoint != null) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val leftPad = if (showYAxis) 26.dp else 8.dp
                val rightPad = 8.dp
                val topPad = 12.dp
                val bottomPad = if (showTimeAxis) 22.dp else 10.dp

                val cWidth = maxWidth - leftPad - rightPad
                val cHeight = maxHeight - topPad - bottomPad

                val activeX = leftPad + (cWidth * activeFraction)
                val normY = 1f - ((activePoint.dispTemp - yMin) / yRange).coerceIn(0f, 1f)
                val activeY = topPad + (cHeight * normY)

                val tooltipW = 74.dp
                val tooltipH = 28.dp
                val tipLeft = (activeX - tooltipW / 2).coerceIn(leftPad, maxWidth - rightPad - tooltipW)
                val tipTop = (activeY - tooltipH - 8.dp).coerceAtLeast(0.dp)

                Box(
                    modifier = Modifier
                        .offset(x = tipLeft, y = tipTop)
                        .shadow(4.dp, RoundedCornerShape(8.dp), spotColor = Color(0x25000000))
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.primary)
                        .padding(horizontal = 6.dp, vertical = 2.5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%.1f%s", activePoint.dispTemp, unitSymbol),
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = activePoint.timeFormatted,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

