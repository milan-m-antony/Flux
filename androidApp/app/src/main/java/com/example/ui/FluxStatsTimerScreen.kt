package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Timer
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
fun FluxStatsTimerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val darkTheme by viewModel.isDarkTheme.collectAsState()
    val colors = rememberFluxColors(darkTheme)
    val connectionState by viewModel.connectionState.collectAsState()
    val reading by viewModel.latestReading.collectAsState()
    val allReadings by viewModel.allReadings.collectAsState()
    val preferredUnit by viewModel.preferredUnit.collectAsState()
    val isConnected = connectionState == ConnectionState.CONNECTED && viewModel.bleManager.isBluetoothEnabled()

    val unitSymbol = if (preferredUnit == "F") "°F" else "°C"
    val isFahr = preferredUnit.equals("F", ignoreCase = true)
    fun toDisp(c: Double) = if (isFahr) (c * 9.0 / 5.0) + 32.0 else c

    val minVal = reading?.minTemp ?: allReadings.minOfOrNull { it.temperature } ?: 0.0
    val maxVal = reading?.maxTemp ?: allReadings.maxOfOrNull { it.temperature } ?: 0.0
    val avgVal = reading?.avgTemp ?: (if (allReadings.isNotEmpty()) allReadings.map { it.temperature }.average() else 0.0)

    val infiniteTransition = rememberInfiniteTransition(label = "timerPulse")
    val timerIconScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isConnected) 1.09f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timerIconScale"
    )

    val timerBorderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = if (isConnected) 0.85f else 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timerBorderAlpha"
    )

    val colonAlpha by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isConnected) 0.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colonAlpha"
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
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Top Header Row
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

                        Text(
                            text = "Session Statistics & Timer",
                            color = colors.textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Elapsed Monotonic Duration Hero Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color(0x10000000))
                            .clip(RoundedCornerShape(24.dp))
                            .background(colors.cardBg)
                            .border(
                                width = if (isConnected) 1.2.dp else 0.5.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        FluxOrange.copy(alpha = timerBorderAlpha),
                                        FluxOrangeGradientEnd.copy(alpha = timerBorderAlpha * 0.6f)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(22.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .scale(timerIconScale)
                                    .clip(CircleShape)
                                    .background(FluxOrange.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = FluxOrange,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "ACTIVE MONITORING DURATION",
                                color = colors.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val elapsedSec = reading?.elapsedSec ?: 0L
                            val h = elapsedSec / 3600
                            val m = (elapsedSec % 3600) / 60
                            val s = elapsedSec % 60

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format(Locale.US, "%02d", h),
                                    color = FluxOrange,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = ":",
                                    color = FluxOrange.copy(alpha = colonAlpha),
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = String.format(Locale.US, "%02d", m),
                                    color = FluxOrange,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = ":",
                                    color = FluxOrange.copy(alpha = colonAlpha),
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = String.format(Locale.US, "%02d", s),
                                    color = FluxOrange,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isConnected) "Hardware Uptime Synced" else "Session Paused (Offline)",
                                color = if (isConnected) Color(0xFF10B981) else colors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2x2 Stat Cards Grid with interactive spring bounce
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "SESSION MIN",
                            value = if (minVal > -50.0) String.format(Locale.US, "%.1f %s", toDisp(minVal), unitSymbol) else "--",
                            valueColor = Color(0xFF3B82F6),
                            colors = colors,
                            modifier = Modifier.weight(1f)
                        )

                        StatCard(
                            title = "SESSION MAX",
                            value = if (maxVal > -50.0) String.format(Locale.US, "%.1f %s", toDisp(maxVal), unitSymbol) else "--",
                            valueColor = Color(0xFFEF4444),
                            colors = colors,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "SESSION AVERAGE",
                            value = if (avgVal > -50.0) String.format(Locale.US, "%.1f %s", toDisp(avgVal), unitSymbol) else "--",
                            valueColor = FluxOrange,
                            colors = colors,
                            modifier = Modifier.weight(1f)
                        )

                        StatCard(
                            title = "TOTAL PACKETS",
                            value = "${allReadings.size}",
                            valueColor = colors.textPrimary,
                            colors = colors,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    valueColor: Color,
    colors: FluxColors,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "statCardScale"
    )

    Box(
        modifier = modifier
            .scale(cardScale)
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0x10000000))
            .clip(RoundedCornerShape(20.dp))
            .background(colors.cardBg)
            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(20.dp))
            .clickable(interactionSource = interactionSource, indication = null) {}
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                color = colors.textSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = valueColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
