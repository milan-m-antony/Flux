package com.example.ui

import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ble.ConnectionState
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun FluxLiveMonitorScreen(
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

    // Smooth rolling number spring interpolation
    val animatedTemp by animateFloatAsState(
        targetValue = if (rawTemp > -50.0) rawTemp.toFloat() else 0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "liveTempSpring"
    )

    val displayTemp = if (preferredUnit == "F") {
        if (rawTemp > -50.0) String.format(Locale.US, "%.1f", (animatedTemp * 9.0f / 5.0f) + 32.0f) else "--.-"
    } else {
        if (rawTemp > -50.0) String.format(Locale.US, "%.1f", animatedTemp) else "--.-"
    }
    val unitSymbol = if (preferredUnit == "F") "°F" else "°C"

    // Live Telemetry Breathing & Multi-Tier Sonar Radar Animations
    val infiniteTransition = rememberInfiniteTransition(label = "liveMonitorPulse")

    // Concentric Sonar Wave 1 (Expands 1.0f -> 1.36f, fades alpha 0.45f -> 0.0f)
    val sonar1Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sonar1Progress"
    )

    // Concentric Sonar Wave 2 (Phase offset by 1200ms)
    val sonar2Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, delayMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sonar2Progress"
    )

    val coreGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "coreGlowAlpha"
    )

    val ledAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ledAlpha"
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
                                text = "Live Telemetry Monitor",
                                color = colors.textPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            )
                            Text(
                                text = "Active probe continuous stream",
                                color = colors.textSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Hero Circular Gauge with Animated Multi-Tier Sonar Rings
                    BoxWithConstraints(contentAlignment = Alignment.Center) {
                        val gaugeSize = minOf(maxWidth * 0.68f, 240.dp)

                        // Multi-tier Concentric Sonar Waves
                        if (isConnected) {
                            val scale1 = 1.0f + (sonar1Progress * 0.36f)
                            val alpha1 = (1.0f - sonar1Progress) * 0.45f
                            Box(
                                modifier = Modifier
                                    .size(gaugeSize)
                                    .scale(scale1)
                                    .clip(CircleShape)
                                    .border(1.6.dp, FluxOrange.copy(alpha = alpha1), CircleShape)
                            )

                            val scale2 = 1.0f + (sonar2Progress * 0.36f)
                            val alpha2 = (1.0f - sonar2Progress) * 0.45f
                            Box(
                                modifier = Modifier
                                    .size(gaugeSize)
                                    .scale(scale2)
                                    .clip(CircleShape)
                                    .border(1.6.dp, FluxOrange.copy(alpha = alpha2), CircleShape)
                            )
                        }

                        // Main Hero Gauge with dynamic glowing sweep border
                        Box(
                            modifier = Modifier
                                .size(gaugeSize)
                                .shadow(8.dp, CircleShape, spotColor = FluxOrange.copy(alpha = if (isConnected) 0.20f else 0.08f))
                                .clip(CircleShape)
                                .background(colors.cardBg)
                                .border(
                                    width = 3.dp,
                                    brush = Brush.sweepGradient(
                                        listOf(
                                            FluxOrange.copy(alpha = coreGlowAlpha),
                                            FluxOrangeGradientEnd.copy(alpha = coreGlowAlpha * 0.7f),
                                            FluxOrange.copy(alpha = coreGlowAlpha)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(FluxOrange.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Thermostat,
                                        contentDescription = null,
                                        tint = FluxOrange,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = if (isConnected) displayTemp else "--.-",
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.textPrimary
                                )

                                Text(
                                    text = unitSymbol,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FluxOrange
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isConnected) Color(0xFF10B981).copy(alpha = 0.14f)
                                             else Color(0xFFEF4444).copy(alpha = 0.14f)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isConnected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .scale(ledAlpha)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF10B981))
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                        }
                                        Text(
                                            text = if (isConnected) "DS18B20 Live Stream" else "Sensor Disconnected",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Live Sensor Metrics Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color(0x10000000))
                            .clip(RoundedCornerShape(24.dp))
                            .background(colors.cardBg)
                            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(24.dp))
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                text = "Telemetry Metrics",
                                color = colors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            MetricRow(
                                icon = Icons.Outlined.CheckCircle,
                                label = "Hardware Status",
                                value = reading?.status ?: if (isConnected) "Active" else "Offline",
                                valueColor = if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444),
                                colors = colors,
                                showPulseDot = isConnected
                            )

                            HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.6f), thickness = 0.5.dp)

                            MetricRow(
                                icon = Icons.Outlined.BatteryChargingFull,
                                label = "Battery Voltage",
                                value = String.format(Locale.US, "%.2f V (%d%%)", reading?.batteryVoltage ?: 0.0, reading?.battery ?: 0),
                                valueColor = colors.textPrimary,
                                colors = colors
                            )

                            HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.6f), thickness = 0.5.dp)

                            MetricRow(
                                icon = Icons.Outlined.Speed,
                                label = "Sampling Interval",
                                value = "1000 ms (Real-time)",
                                valueColor = FluxOrange,
                                colors = colors,
                                showPulseDot = isConnected,
                                dotColor = FluxOrange
                            )

                            HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.6f), thickness = 0.5.dp)

                            MetricRow(
                                icon = Icons.Outlined.Bluetooth,
                                label = "BLE Telemetry Link",
                                value = if (isConnected) "Connected (GATT)" else "Disconnected",
                                valueColor = if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444),
                                colors = colors,
                                showPulseDot = isConnected
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color,
    colors: FluxColors,
    showPulseDot: Boolean = false,
    dotColor: Color = Color(0xFF10B981)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "metricPulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "metricDotAlpha"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                color = colors.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showPulseDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .scale(dotAlpha)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = value,
                color = valueColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
