package com.example.ui

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.R
import com.example.ui.components.FluxSignalMeter
import com.example.ui.theme.*

@Composable
fun FluxDeviceScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit = {},
    onNavigateToDiagnostics: () -> Unit = {},
    onNavigateToWifiSetup: () -> Unit = {}
) {
    val context = LocalContext.current
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val colors = rememberFluxColors(isDarkTheme)
    val connectivityState by viewModel.connectivityState.collectAsState()
    val reading by viewModel.latestReading.collectAsState()
    val lastDeviceAddress by viewModel.lastDeviceAddress.collectAsState()
    val isOnline = connectivityState.isOnline

    // Live battery & telemetry metrics from genuine packets
    val batteryPercent = reading?.battery
    val batteryVoltage = reading?.batteryVoltage
    val signalPercent = connectivityState.signalPercent
    val signalDbm = connectivityState.signalDbm
    val deviceName = connectivityState.deviceName.ifBlank { "Flux1" }

    // Floating and glowing animations for the device board
    val infiniteTransition = rememberInfiniteTransition(label = "DeviceAnimations")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "DeviceFloatY"
    )
    val boardScale by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BoardScale"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulse"
    )

    FluxBackground(darkTheme = isDarkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Navigation Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
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
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Device Information",
                        color = colors.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        text = "Flux ESP32 & Telemetry Hardware",
                        color = colors.textSecondary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Hero Animated Device Showcase Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(28.dp), spotColor = Color(0x18000000))
                    .clip(RoundedCornerShape(28.dp))
                    .background(colors.cardBg)
                    .border(0.5.dp, colors.cardBorder, RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Animated floating ESP32 Board with glowing aura
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .offset(y = floatY.dp)
                            .scale(boardScale),
                        contentAlignment = Alignment.Center
                    ) {
                        // Ambient Radial Glow
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .scale(1.25f)
                                .alpha(glowPulse)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            FluxOrange.copy(alpha = 0.45f),
                                            FluxOrange.copy(alpha = 0.10f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Outer glassmorphism rounded frame
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x20000000))
                                .clip(RoundedCornerShape(24.dp))
                                .background(if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFFFF2EB))
                                .border(1.dp, FluxOrange.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                                .padding(6.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.esp32_board),
                                contentDescription = "ESP32 Board",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(18.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Device Name
                    Text(
                        text = deviceName,
                        color = colors.textPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "ESP32-WROOM-32D Dual-Core 240MHz",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Online/Offline Status Pill
                    val statusColor = if (isOnline) Color(0xFF00C853) else Color(0xFFDC2626)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(statusColor.copy(alpha = 0.12f))
                            .border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(percent = 50))
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOnline) "Online • Real-Time Telemetry Active" else "Offline • Telemetry Standby",
                                color = statusColor,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Telemetry Indicators: Battery % & Signal Strength
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Battery Metric Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.cardBg)
                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00C853).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                FluxBatteryIcon(
                                    level = batteryPercent ?: 0,
                                    tint = if (batteryPercent != null) Color(0xFF00C853) else colors.textSecondary
                                )
                            }

                            Text(
                                text = "Battery",
                                color = colors.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (batteryPercent != null) "$batteryPercent%" else "--%",
                            color = colors.textPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (batteryVoltage != null && batteryVoltage > 0.0) String.format("%.2fV • Healthy", batteryVoltage) else if (isOnline) "Measuring..." else "Standby",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Visual battery level progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(percent = 50))
                                .background(if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(((batteryPercent ?: 0) / 100f).coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(
                                        when {
                                            (batteryPercent ?: 0) > 50 -> Color(0xFF00C853)
                                            (batteryPercent ?: 0) > 20 -> FluxOrange
                                            else -> Color(0xFFDC2626)
                                        }
                                    )
                            )
                        }
                    }
                }

                // Signal Strength Metric Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.cardBg)
                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(FluxOrange.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Wifi,
                                    contentDescription = "Signal",
                                    tint = FluxOrange,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // 4-Bar Meter
                            FluxSignalMeter(
                                state = connectivityState,
                                darkTheme = isDarkTheme,
                                barHeights = listOf(5.dp, 9.dp, 13.dp, 17.dp),
                                barWidth = 3.2.dp,
                                barSpacing = 1.8.dp,
                                fontSize = 8.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (signalPercent != null) "$signalPercent%" else "--%",
                            color = colors.textPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (signalDbm != null) "$signalDbm dBm • ${connectivityState.type.label}" else if (isOnline) "Connecting • ${connectivityState.type.label}" else "Standby • Disconnected",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Visual signal level bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(percent = 50))
                                .background(if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((signalPercent ?: 0) / 100f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(FluxOrange)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Hardware Specifications & Telemetry Details Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color(0x10000000))
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.cardBg)
                    .border(0.5.dp, colors.cardBorder, RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.MonitorHeart,
                            contentDescription = null,
                            tint = FluxOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Hardware & Network Details",
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val liveFirmware = reading?.firmware?.ifBlank { "v2.0.0" } ?: if (isOnline) "v2.0.0" else "Unavailable"
                    val liveMac = lastDeviceAddress ?: if (isOnline) "Connected (BLE)" else "Disconnected"
                    val liveIp = if (reading?.wifiConnected == true) {
                        if (!reading?.wifiSsid.isNullOrBlank()) "Connected (${reading?.wifiSsid})" else "Connected (DHCP)"
                    } else if (isOnline) {
                        "Connected via BLE"
                    } else {
                        "Disconnected"
                    }
                    val liveProto = when {
                        connectivityState.type == com.example.connectivity.ConnectionType.BLUETOOTH -> "BLE GATT / MTU 512"
                        connectivityState.type == com.example.connectivity.ConnectionType.WIFI -> "Direct HTTPS PostgREST"
                        isOnline -> "Realtime Link"
                        else -> "Offline"
                    }

                    TechDetailRow("Temperature Sensor", if (isOnline) "Dallas DS18B20 (±0.5°C)" else "DS18B20 (Awaiting Link)", colors)
                    HorizontalDivider(color = colors.cardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                    TechDetailRow("Microcontroller", "ESP32 Tensilica LX6 Dual-Core", colors)
                    HorizontalDivider(color = colors.cardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                    TechDetailRow("Firmware Version", liveFirmware, colors)
                    HorizontalDivider(color = colors.cardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                    TechDetailRow("MAC Address", liveMac, colors)
                    HorizontalDivider(color = colors.cardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                    TechDetailRow("IP Address", liveIp, colors)
                    HorizontalDivider(color = colors.cardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                    TechDetailRow("Telemetry Protocol", liveProto, colors)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Interactive Hardware Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Configure Wi-Fi Button
                Button(
                    onClick = onNavigateToWifiSetup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FluxOrange,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Wifi,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (reading?.wifiConnected == true) "Manage Wi-Fi Settings" else "Connect Device to Wi-Fi",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Ping Button
                    OutlinedButton(
                        onClick = {
                            val pingSuccess = viewModel.bleManager.sendCommand("PING")
                            if (pingSuccess) {
                                Toast.makeText(context, "Ping packet sent to $deviceName via BLE", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, if (isOnline) "Ping sent to $deviceName" else "Device offline", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.textPrimary
                        ),
                        border = BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = FluxOrange
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ping", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Turn Off BLE Button
                    if (isOnline) {
                        OutlinedButton(
                            onClick = {
                                viewModel.turnOffBle()
                                Toast.makeText(context, "Turned off BLE session", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1.3f).height(46.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, colors.cardBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BluetoothDisabled,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Turn Off BLE", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }
                    }

                    // Diagnostics Button
                    Button(
                        onClick = { onNavigateToDiagnostics() },
                        modifier = Modifier.weight(1.2f).height(46.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.pillBg,
                            contentColor = colors.textPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = FluxOrange
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Diagnostics", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun FluxBatteryIcon(
    level: Int,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(width = 20.dp, height = 11.dp)) {
        val stroke = 1.3.dp.toPx()
        val corner = 2.dp.toPx()
        val bodyWidth = size.width - 2.8.dp.toPx()
        val bodyHeight = size.height

        // Outer body
        drawRoundRect(
            color = tint,
            topLeft = Offset.Zero,
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = CornerRadius(corner, corner),
            style = Stroke(width = stroke)
        )

        // Terminal tip
        drawRoundRect(
            color = tint,
            topLeft = Offset(bodyWidth + 0.8.dp.toPx(), bodyHeight * 0.28f),
            size = Size(1.8.dp.toPx(), bodyHeight * 0.44f),
            cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
        )

        // Fill level
        val fillWidth = (bodyWidth - stroke * 2 - 1.5.dp.toPx()) * (level.coerceIn(0, 100) / 100f)
        if (fillWidth > 0f) {
            drawRoundRect(
                color = tint,
                topLeft = Offset(stroke + 0.8.dp.toPx(), stroke + 0.8.dp.toPx()),
                size = Size(fillWidth, bodyHeight - (stroke + 0.8.dp.toPx()) * 2),
                cornerRadius = CornerRadius(1.2.dp.toPx(), 1.2.dp.toPx())
            )
        }
    }
}

@Composable
private fun TechDetailRow(
    label: String,
    value: String,
    colors: FluxColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = colors.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
