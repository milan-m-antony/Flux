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
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ble.ConnectionState
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.sin

@Composable
fun FluxEnvironmentScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val darkTheme by viewModel.isDarkTheme.collectAsState()
    val colors = rememberFluxColors(darkTheme)
    val connectionState by viewModel.connectionState.collectAsState()
    val reading by viewModel.latestReading.collectAsState()
    val preferredUnit by viewModel.preferredUnit.collectAsState()
    val lowThreshold by viewModel.lowTempThreshold.collectAsState()
    val highThreshold by viewModel.highTempThreshold.collectAsState()
    val isConnected = connectionState == ConnectionState.CONNECTED && viewModel.bleManager.isBluetoothEnabled()

    val rawTemp = reading?.temp ?: 0.0
    val inRange = rawTemp in lowThreshold..highThreshold
    val displayTemp = if (preferredUnit == "F") {
        if (rawTemp > -50.0) String.format(Locale.US, "%.1f", (rawTemp * 9.0 / 5.0) + 32.0) else "--.-"
    } else {
        if (rawTemp > -50.0) String.format(Locale.US, "%.1f", rawTemp) else "--.-"
    }
    val unitSymbol = if (preferredUnit == "F") "°F" else "°C"

    // Animated Breathing Eco Pulse & Gentle Breeze Sway
    val infiniteTransition = rememberInfiniteTransition(label = "ecoPulse")
    val leafScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (inRange && isConnected) 1.12f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leafScale"
    )

    val leafSwayAngle by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leafSway"
    )

    // Animated Floating Breeze Spores
    val spore1Y by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing), RepeatMode.Restart),
        label = "spore1"
    )
    val spore2Y by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(3600, delayMillis = 600, easing = LinearEasing), RepeatMode.Restart),
        label = "spore2"
    )
    val spore3Y by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2400, delayMillis = 1100, easing = LinearEasing), RepeatMode.Restart),
        label = "spore3"
    )

    // Animated progress indicator fraction (mapped from 10°C to 35°C)
    val targetFraction = remember(rawTemp) {
        if (rawTemp <= 10.0) 0.05f
        else if (rawTemp >= 35.0) 0.95f
        else ((rawTemp - 10.0) / (35.0 - 10.0)).toFloat().coerceIn(0.05f, 0.95f)
    }
    val animatedProgress by animateFloatAsState(
        targetValue = if (isConnected) targetFraction else 0.5f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "ambientProgress"
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
                                text = "Environment Profile",
                                color = colors.textPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            )
                            Text(
                                text = "Ambient thermal tracking & grow rooms",
                                color = colors.textSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. Hero Ambient Temperature Card with Breeze Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .shadow(6.dp, RoundedCornerShape(26.dp), spotColor = Color(0x14000000))
                            .clip(RoundedCornerShape(26.dp))
                            .background(colors.cardBg)
                            .border(
                                width = if (inRange) 1.dp else 1.5.dp,
                                color = if (inRange) Color(0xFF10B981).copy(alpha = 0.55f) else Color(0xFFEF4444).copy(alpha = 0.6f),
                                shape = RoundedCornerShape(26.dp)
                            )
                    ) {
                        // Ambient Floating Spores Canvas (Gentle Eco Breeze)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw subtle environmental aura gradient at bottom
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        if (inRange) Color(0xFFD1FAE5).copy(alpha = 0.40f) else Color(0xFFFEE2E2).copy(alpha = 0.40f)
                                    ),
                                    startY = h * 0.45f,
                                    endY = h
                                )
                            )

                            if (isConnected && inRange) {
                                // Spore 1
                                val s1Y = h * spore1Y
                                val s1X = w * 0.25f + 8.dp.toPx() * sin(spore1Y * 4f)
                                drawCircle(
                                    color = Color(0xFF10B981).copy(alpha = 0.35f * spore1Y),
                                    radius = 3.dp.toPx(),
                                    center = Offset(s1X, s1Y)
                                )

                                // Spore 2
                                val s2Y = h * spore2Y
                                val s2X = w * 0.72f + 10.dp.toPx() * sin(spore2Y * 5f)
                                drawCircle(
                                    color = Color(0xFF34D399).copy(alpha = 0.30f * spore2Y),
                                    radius = 4.dp.toPx(),
                                    center = Offset(s2X, s2Y)
                                )

                                // Spore 3
                                val s3Y = h * spore3Y
                                val s3X = w * 0.48f + 6.dp.toPx() * sin(spore3Y * 6f)
                                drawCircle(
                                    color = Color(0xFF059669).copy(alpha = 0.25f * spore3Y),
                                    radius = 2.5.dp.toPx(),
                                    center = Offset(s3X, s3Y)
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .scale(leafScale)
                                            .rotate(leafSwayAngle)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981).copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Eco,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Ambient Thermal Band",
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
                                        text = if (inRange) "IN RANGE" else "OUT OF BAND",
                                        color = if (inRange) Color(0xFF10B981) else Color(0xFFEF4444),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = if (isConnected) "$displayTemp $unitSymbol" else "--.-",
                                    fontSize = 46.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (inRange) colors.textPrimary else Color(0xFFEF4444),
                                    letterSpacing = (-0.5).sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (!isConnected) "Sensor Disconnected • Connect Bluetooth"
                                    else if (inRange) "Ambient comfort range optimal (${lowThreshold.toInt()}°C – ${highThreshold.toInt()}°C)"
                                    else "Thermal limits breached! Regulate ventilation.",
                                    color = if (inRange) colors.textSecondary else Color(0xFFEF4444),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Dynamic Ambient Range Bar with Spring Animation
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Color(0xFFE2E8F0))
                            ) {
                                // Optimal zone green band
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(0.35f)
                                        .offset(x = 75.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color(0xFF10B981).copy(alpha = 0.35f))
                                )

                                // Animated Live Temp Marker
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(animatedProgress)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFF10B981),
                                                    if (inRange) Color(0xFF34D399) else Color(0xFFEF4444)
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 2. Target Band Configuration Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(22.dp), spotColor = Color(0x0C000000))
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.cardBg)
                            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Park,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Target Thermal Limits",
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    )
                                }
                                Text(
                                    text = "Auto-Synced",
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Lower Threshold", color = colors.textSecondary, fontSize = 13.sp)
                                Text("${lowThreshold.toInt()}°C", color = FluxOrange, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            }

                            HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.6f), thickness = 0.5.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Upper Threshold", color = colors.textSecondary, fontSize = 13.sp)
                                Text("${highThreshold.toInt()}°C", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3. Notice Card
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
                                text = "Environment profile targets ambient growth rooms, terrariums, and storage units using direct DS18B20 hardware telemetry.",
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
