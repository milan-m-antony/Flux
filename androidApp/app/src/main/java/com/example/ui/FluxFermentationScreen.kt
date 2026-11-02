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
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Grain
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
fun FluxFermentationScreen(
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
    val inRange = rawTemp in 18.0..24.0
    val displayTemp = if (preferredUnit == "F") {
        if (rawTemp > -50.0) String.format(Locale.US, "%.1f", (rawTemp * 9.0 / 5.0) + 32.0) else "--.-"
    } else {
        if (rawTemp > -50.0) String.format(Locale.US, "%.1f", rawTemp) else "--.-"
    }
    val unitSymbol = if (preferredUnit == "F") "°F" else "°C"

    // Continuous Effervescent Bubbling Animation
    val infiniteTransition = rememberInfiniteTransition(label = "fermentBubbleAnim")
    val b1 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1900, easing = LinearEasing), RepeatMode.Restart),
        label = "fb1"
    )
    val b2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2400, delayMillis = 300, easing = LinearEasing), RepeatMode.Restart),
        label = "fb2"
    )
    val b3 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1600, delayMillis = 600, easing = LinearEasing), RepeatMode.Restart),
        label = "fb3"
    )
    val b4 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2900, delayMillis = 150, easing = LinearEasing), RepeatMode.Restart),
        label = "fb4"
    )

    val frothWavePhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "frothWave"
    )

    // Animated Fermentation Beaker Pulse
    val beakerScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (inRange && isConnected) 1.12f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beakerScale"
    )

    val timerPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timerPulse"
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
                                text = "Fermentation Profile",
                                color = colors.textPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            )
                            Text(
                                text = "Precision sourdough, beer & kombucha",
                                color = colors.textSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. Hero Animated Fermentation Vessel Card with Rising Effervescent Bubbles
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .shadow(6.dp, RoundedCornerShape(26.dp), spotColor = Color(0x14000000))
                            .clip(RoundedCornerShape(26.dp))
                            .background(colors.cardBg)
                            .border(
                                width = if (inRange) 1.dp else 1.5.dp,
                                color = if (inRange) Color(0xFFF59E0B).copy(alpha = 0.5f) else Color(0xFFEF4444).copy(alpha = 0.6f),
                                shape = RoundedCornerShape(26.dp)
                            )
                    ) {
                        // Canvas: Frothy Fermentation Surface + Continuous Rising Micro-Bubbles
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val foamY = h * 0.46f

                            // Gentle frothy surface wave
                            val foamPath = Path().apply {
                                moveTo(0f, foamY)
                                val wavelength = w * 0.6f
                                var x = 0f
                                while (x <= w) {
                                    val y = foamY + 5.dp.toPx() * sin((2 * PI * (x / wavelength) + frothWavePhase).toFloat())
                                    lineTo(x, y)
                                    x += 5f
                                }
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            }

                            // Fermentation broth warm golden/amber gradient
                            drawPath(
                                path = foamPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFDE68A).copy(alpha = 0.40f),
                                        Color(0xFFD97706).copy(alpha = 0.85f)
                                    ),
                                    startY = foamY,
                                    endY = h
                                )
                            )

                            // Rising effervescent carbonation bubbles
                            if (isConnected) {
                                val y1 = foamY + (h - foamY) * b1
                                val x1 = w * 0.18f + 4.dp.toPx() * sin(b1 * 8f)
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.65f * b1),
                                    radius = 3.2.dp.toPx(),
                                    center = Offset(x1, y1)
                                )

                                val y2 = foamY + (h - foamY) * b2
                                val x2 = w * 0.44f + 3.dp.toPx() * cos(b2 * 6f)
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.70f * b2),
                                    radius = 4.2.dp.toPx(),
                                    center = Offset(x2, y2)
                                )

                                val y3 = foamY + (h - foamY) * b3
                                val x3 = w * 0.78f + 5.dp.toPx() * sin(b3 * 7f)
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.60f * b3),
                                    radius = 3.6.dp.toPx(),
                                    center = Offset(x3, y3)
                                )

                                val y4 = foamY + (h - foamY) * b4
                                val x4 = w * 0.62f + 2.dp.toPx() * cos(b4 * 9f)
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.55f * b4),
                                    radius = 2.4.dp.toPx(),
                                    center = Offset(x4, y4)
                                )
                            }
                        }

                        // Content Overlay
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top Row: Science Icon + Optimal Band Pill
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .scale(beakerScale)
                                            .clip(CircleShape)
                                            .background(FluxOrange.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Science,
                                            contentDescription = null,
                                            tint = FluxOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Vessel Thermics",
                                        color = colors.textPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (inRange) Color(0xFF10B981).copy(alpha = 0.14f) else Color(0xFFEF4444).copy(alpha = 0.14f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (inRange) "IN BAND (18–24°C)" else "OUT OF BAND!",
                                        color = if (inRange) Color(0xFF10B981) else Color(0xFFEF4444),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Temperature Readout in Golden Broth
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
                                    text = if (!isConnected) "Sensor Disconnected"
                                    else if (inRange) "Yeast kinetics optimal • Stable active fermentation"
                                    else "Warning: Thermal limit breached! Regulate ambient temp.",
                                    color = Color.White.copy(alpha = 0.92f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 2. Active Batch Duration Timer Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(22.dp), spotColor = Color(0x0C000000))
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.cardBg)
                            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
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
                                        .background(FluxOrange.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = FluxOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Active Fermentation Batch",
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Monotonic elapsed hardware duration",
                                        color = colors.textSecondary,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }

                            val elapsedSec = reading?.elapsedSec ?: 0L
                            val h = elapsedSec / 3600
                            val m = (elapsedSec % 3600) / 60
                            val batchDurationStr = if (elapsedSec > 0) String.format(Locale.US, "%02dh %02dm", h, m) else if (isConnected) "00h 00m" else "--h --m"

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.5.dp)
                                        .scale(timerPulseAlpha)
                                        .clip(CircleShape)
                                        .background(FluxOrange)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = batchDurationStr,
                                    color = FluxOrange,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3. Fermentation Phase Tracker Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(22.dp), spotColor = Color(0x0C000000))
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.cardBg)
                            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Grain,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Kinetic Phase",
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    )
                                }
                                Text(
                                    text = if (isConnected && inRange) "Active High Kräusen" else "Awaiting Inoculation",
                                    color = if (isConnected && inRange) Color(0xFF10B981) else colors.textSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            // 3-step progress bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (isConnected) FluxOrange else Color(0xFFE2E8F0))
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (isConnected && inRange) FluxOrange else Color(0xFFE2E8F0))
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFFE2E8F0))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 4. Notice Card
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
                                tint = FluxOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Maintaining 18°C–24°C prevents off-flavors and sluggish yeast activity. Real-time temperature alerts safeguard active brews against thermal runaway.",
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
