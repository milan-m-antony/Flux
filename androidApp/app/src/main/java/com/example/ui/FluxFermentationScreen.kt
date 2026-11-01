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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ble.ConnectionState
import com.example.ui.theme.*
import java.util.Locale

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
    val lowThreshold by viewModel.lowTempThreshold.collectAsState()
    val highThreshold by viewModel.highTempThreshold.collectAsState()
    val isConnected = connectionState == ConnectionState.CONNECTED

    val rawTemp = reading?.temp ?: 0.0
    val inRange = rawTemp in 18.0..24.0
    val displayTemp = if (preferredUnit == "F") {
        if (rawTemp > -50.0) String.format(Locale.US, "%.1f", (rawTemp * 9.0 / 5.0) + 32.0) else "--.-"
    } else {
        if (rawTemp > -50.0) String.format(Locale.US, "%.1f", rawTemp) else "--.-"
    }
    val unitSymbol = if (preferredUnit == "F") "°F" else "°C"

    // Animated Fermentation Beaker Pulse
    val infiniteTransition = rememberInfiniteTransition(label = "fermentPulse")
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

                    // Hero Fermentation Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color(0x10000000))
                            .clip(RoundedCornerShape(24.dp))
                            .background(colors.cardBg)
                            .border(
                                width = if (inRange) 1.dp else 1.5.dp,
                                color = if (inRange) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFFEF4444).copy(alpha = 0.6f),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(20.dp)
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
                                        text = "Vessel Temperature",
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

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (isConnected) "$displayTemp $unitSymbol" else "--.-",
                                fontSize = 44.sp,
                                fontWeight = FontWeight.Black,
                                color = if (inRange) colors.textPrimary else Color(0xFFEF4444)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (!isConnected) "Sensor Disconnected"
                                else if (inRange) "Yeast kinetics optimal • Stable fermentation zone"
                                else "Warning: Thermal limits breached! Adjust regulation.",
                                color = if (inRange) colors.textSecondary else Color(0xFFEF4444),
                                fontSize = 12.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Active Batch Duration Timer Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color(0x10000000))
                            .clip(RoundedCornerShape(24.dp))
                            .background(colors.cardBg)
                            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(24.dp))
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
                                        text = "Elapsed batch duration",
                                        color = colors.textSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .scale(timerPulseAlpha)
                                        .clip(CircleShape)
                                        .background(FluxOrange)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "04h 28m",
                                    color = FluxOrange,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Notice Card
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
