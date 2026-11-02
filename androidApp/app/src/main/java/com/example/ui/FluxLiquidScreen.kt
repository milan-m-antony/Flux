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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ble.ConnectionState
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FluxLiquidScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val darkTheme by viewModel.isDarkTheme.collectAsState()
    val colors = rememberFluxColors(darkTheme)
    val connectionState by viewModel.connectionState.collectAsState()
    val reading by viewModel.latestReading.collectAsState()
    val preferredUnit by viewModel.preferredUnit.collectAsState()
    val isConnected = connectionState == ConnectionState.CONNECTED && viewModel.bleManager.isBluetoothEnabled()

    val rawTemp = reading?.temp ?: 0.0
    val displayTemp = if (preferredUnit == "F") {
        if (rawTemp > -50.0) String.format(Locale.US, "%.1f", (rawTemp * 9.0 / 5.0) + 32.0) else "--.-"
    } else {
        if (rawTemp > -50.0) String.format(Locale.US, "%.1f", rawTemp) else "--.-"
    }
    val unitSymbol = if (preferredUnit == "F") "°F" else "°C"

    // Continuous Sinusoidal Wave Animation
    val infiniteTransition = rememberInfiniteTransition(label = "liquidWaveAnim")
    val wavePhase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase1"
    )
    val wavePhase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase2"
    )

    // Rising Bubbles Animation
    val bubbleProgress1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble1"
    )
    val bubbleProgress2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3100, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble2"
    )
    val bubbleProgress3 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, delayMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble3"
    )

    // Water Drop Pulsing Indicator
    val dropPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isConnected) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dropPulse"
    )

    FluxBackground(darkTheme = darkTheme) {
        Scaffold(
            containerColor = Color.Transparent
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 640.dp)
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Top Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .shadow(2.dp, CircleShape, spotColor = Color(0x10000000))
                                .clip(CircleShape)
                                .background(colors.cardBg)
                                .border(0.5.dp, colors.cardBorder, CircleShape)
                                .clickable { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Liquid Profile",
                                color = colors.textPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            )
                            Text(
                                text = "Submerged probe liquid equilibrium",
                                color = colors.textSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. Hero Dynamic Liquid Tank Card with Sinusoidal Wave & Bubbles
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .shadow(6.dp, RoundedCornerShape(26.dp), spotColor = Color(0x14000000))
                            .clip(RoundedCornerShape(26.dp))
                            .background(colors.cardBg)
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f), RoundedCornerShape(26.dp))
                    ) {
                        // Canvas: Realistic Animated Dual Sine Waves + Rising Carbonation / Air Bubbles
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val baseWaterY = h * 0.48f

                            // Primary Wave Path
                            val path1 = Path().apply {
                                moveTo(0f, baseWaterY)
                                val wavelength1 = w * 0.85f
                                var x = 0f
                                while (x <= w) {
                                    val y = baseWaterY + 7.dp.toPx() * sin((2 * PI * (x / wavelength1) + wavePhase1).toFloat())
                                    lineTo(x, y)
                                    x += 5f
                                }
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            }

                            // Secondary Back Wave Path (Translucent Depth Layer)
                            val path2 = Path().apply {
                                moveTo(0f, baseWaterY)
                                val wavelength2 = w * 0.55f
                                var x = 0f
                                while (x <= w) {
                                    val y = baseWaterY + 5.dp.toPx() * cos((2 * PI * (x / wavelength2) + wavePhase2).toFloat())
                                    lineTo(x, y)
                                    x += 5f
                                }
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            }

                            // Draw Secondary Wave First (Background)
                            drawPath(
                                path = path2,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFBAE6FD).copy(alpha = 0.35f),
                                        Color(0xFF38BDF8).copy(alpha = 0.40f)
                                    ),
                                    startY = baseWaterY - 10f,
                                    endY = h
                                )
                            )

                            // Draw Primary Wave (Foreground)
                            drawPath(
                                path = path1,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF38BDF8).copy(alpha = 0.55f),
                                        Color(0xFF0284C7).copy(alpha = 0.78f)
                                    ),
                                    startY = baseWaterY,
                                    endY = h
                                )
                            )

                            // Draw Animated Rising Micro-Bubbles
                            if (isConnected) {
                                val bubble1Y = baseWaterY + (h - baseWaterY) * bubbleProgress1
                                val bubble1X = w * 0.22f + 3.dp.toPx() * sin(bubbleProgress1 * 6f)
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.55f * bubbleProgress1),
                                    radius = 3.5.dp.toPx(),
                                    center = Offset(bubble1X, bubble1Y)
                                )

                                val bubble2Y = baseWaterY + (h - baseWaterY) * bubbleProgress2
                                val bubble2X = w * 0.76f + 4.dp.toPx() * cos(bubbleProgress2 * 5f)
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.60f * bubbleProgress2),
                                    radius = 4.5.dp.toPx(),
                                    center = Offset(bubble2X, bubble2Y)
                                )

                                val bubble3Y = baseWaterY + (h - baseWaterY) * bubbleProgress3
                                val bubble3X = w * 0.52f + 2.dp.toPx() * sin(bubbleProgress3 * 7f)
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.50f * bubbleProgress3),
                                    radius = 2.8.dp.toPx(),
                                    center = Offset(bubble3X, bubble3Y)
                                )
                            }
                        }

                        // Content Overlay: Header Status + Submerged Temperature Readout
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top Row: Drop Icon + Submerged Status Pill
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .scale(dropPulse)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0284C7).copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WaterDrop,
                                            contentDescription = null,
                                            tint = Color(0xFF0284C7),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Submerged Probe",
                                        color = colors.textPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isConnected) Color(0xFF10B981).copy(alpha = 0.14f) else Color(0xFFEF4444).copy(alpha = 0.14f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isConnected) "SUBMERGED" else "DISCONNECTED",
                                        color = if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Submerged Readout (Floating comfortably in the liquid wave)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isConnected) "$displayTemp $unitSymbol" else "--.-",
                                    fontSize = 46.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = (-0.5).sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (!isConnected) "Sensor offline • Pair Bluetooth"
                                    else "Active hydrodynamic equilibrium",
                                    color = Color.White.copy(alpha = 0.90f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 2. Liquid Thermal Metrics Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Thermal Drift Rate Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(2.dp, RoundedCornerShape(18.dp), spotColor = Color(0x0C000000))
                                .clip(RoundedCornerShape(18.dp))
                                .background(colors.cardBg)
                                .border(0.5.dp, colors.cardBorder, RoundedCornerShape(18.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Waves,
                                        contentDescription = null,
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Stability Index",
                                        color = colors.textSecondary,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isConnected) "Optimal (±0.1$unitSymbol)" else "--",
                                    color = colors.textPrimary,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Equilibrium Mode Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(2.dp, RoundedCornerShape(18.dp), spotColor = Color(0x0C000000))
                                .clip(RoundedCornerShape(18.dp))
                                .background(colors.cardBg)
                                .border(0.5.dp, colors.cardBorder, RoundedCornerShape(18.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Probe Health",
                                        color = colors.textSecondary,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isConnected) "Active Probe" else "Standby",
                                    color = if (isConnected) Color(0xFF10B981) else colors.textPrimary,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3. Safety Notice Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.pillBg)
                            .border(0.5.dp, colors.pillBorder, RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Only the stainless steel probe tube is waterproof. Ensure the electronics enclosure and cable joints remain dry during liquid immersion monitoring.",
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
