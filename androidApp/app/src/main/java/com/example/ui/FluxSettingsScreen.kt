package com.example.ui

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.R
import com.example.ui.theme.*

@Composable
fun FluxSettingsScreen(
    viewModel: MainViewModel,
    onNotificationClick: () -> Unit = {},
    onNavigateToDevice: () -> Unit = {}
) {
    val context = LocalContext.current
    val lowThreshold by viewModel.lowTempThreshold.collectAsState()
    val highThreshold by viewModel.highTempThreshold.collectAsState()
    val preferredUnit by viewModel.preferredUnit.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val reading by viewModel.latestReading.collectAsState()
    val connectivityState by viewModel.connectivityState.collectAsState()
    val batteryPercent = reading?.battery ?: 88
    val signalPercent = connectivityState.signalPercent ?: 82
    val signalDbm = connectivityState.signalDbm ?: -57

    var alertsEnabled by remember { mutableStateOf(true) }
    var currentMinLimit by remember(lowThreshold) { mutableStateOf(lowThreshold.toInt()) }
    var currentMaxLimit by remember(highThreshold) { mutableStateOf(highThreshold.toInt()) }
    var selectedTheme by remember(isDarkTheme) { mutableStateOf(if (isDarkTheme) "Dark" else "Light") }
    var selectedUnit by remember(preferredUnit) { mutableStateOf(preferredUnit) }

    val colors = rememberFluxColors(isDarkTheme)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            // Spacer to clear the fixed top header cleanly
            Spacer(modifier = Modifier.height(68.dp))

            // 2. Title & Subtitle
            Text(
                text = "Settings",
                color = colors.textPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Personalize your Flux experience",
                color = colors.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Card 1 — App Theme (Responsive Light / Dark Selection)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(26.dp), spotColor = Color(0x10000000))
                    .background(colors.cardBg, RoundedCornerShape(26.dp))
                    .border(0.5.dp, colors.cardBorder, RoundedCornerShape(26.dp))
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Row with Icon and Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isDarkTheme) Color(0xFF332014) else Color(0xFFFFF2EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                                contentDescription = null,
                                tint = FluxOrange,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "App Theme",
                                color = colors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isDarkTheme) "Dark mode enabled" else "Light mode enabled",
                                color = colors.textSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Responsive Full-Width Segmented Theme Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.pillBg, RoundedCornerShape(20.dp))
                            .border(0.5.dp, colors.pillBorder, RoundedCornerShape(20.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Light Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (!isDarkTheme) FluxOrange else Color.Transparent
                                )
                                .clickable {
                                    if (isDarkTheme) {
                                        viewModel.setDarkTheme(false)
                                        Toast.makeText(context, "Switched to Light theme", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LightMode,
                                    contentDescription = null,
                                    tint = if (!isDarkTheme) Color.White else colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Light",
                                    color = if (!isDarkTheme) Color.White else colors.textSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Dark Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isDarkTheme) FluxOrange else Color.Transparent
                                )
                                .clickable {
                                    if (!isDarkTheme) {
                                        viewModel.setDarkTheme(true)
                                        Toast.makeText(context, "Switched to Dark theme", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DarkMode,
                                    contentDescription = null,
                                    tint = if (isDarkTheme) Color.White else colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Dark",
                                    color = if (isDarkTheme) Color.White else colors.textSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Card 2 — Temperature Unit (°C / °F Selection)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(26.dp), spotColor = Color(0x10000000))
                    .background(colors.cardBg, RoundedCornerShape(26.dp))
                    .border(0.5.dp, colors.cardBorder, RoundedCornerShape(26.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Thermometer Icon in blue
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isDarkTheme) Color(0xFF172554) else Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Thermostat,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Temperature Unit",
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Display temperature in",
                            color = colors.textSecondary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // °C / °F Pill
                    Row(
                        modifier = Modifier
                            .background(colors.pillBg, RoundedCornerShape(20.dp))
                            .border(0.5.dp, colors.pillBorder, RoundedCornerShape(20.dp))
                            .padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedUnit == "C") (if (isDarkTheme) FluxOrange else Color(0xFF111827)) else Color.Transparent)
                                .clickable {
                                    selectedUnit = "C"
                                    viewModel.setPreferredUnit("C")
                                    Toast.makeText(context, "Temperature unit: °C", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "°C",
                                color = if (selectedUnit == "C") Color.White else colors.textSecondary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedUnit == "F") (if (isDarkTheme) FluxOrange else Color(0xFF111827)) else Color.Transparent)
                                .clickable {
                                    selectedUnit = "F"
                                    viewModel.setPreferredUnit("F")
                                    Toast.makeText(context, "Temperature unit: °F", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "°F",
                                color = if (selectedUnit == "F") Color.White else colors.textSecondary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Card 3 — Temperature Alerts (Toggle + Min/Max Steppers)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(26.dp), spotColor = Color(0x12000000))
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isDarkTheme) {
                                listOf(Color(0xFF2A2420), Color(0xFF1E1E28))
                            } else {
                                listOf(Color(0xFFFFF9F5), Color(0xFFFFE8DC))
                            }
                        )
                    )
                    .border(
                        0.5.dp,
                        if (isDarkTheme) FluxOrange.copy(alpha = 0.35f) else Color(0xFFFFD9C7),
                        RoundedCornerShape(26.dp)
                    )
                    .padding(18.dp)
            ) {
                Column {
                    // Header Row: Bell + Title + Switch Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isDarkTheme) Color(0xFF332014) else Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = null,
                                tint = FluxOrange,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Temperature Alerts",
                                color = colors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Get notified when temperature is out of range",
                                color = colors.textSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Switch(
                            checked = alertsEnabled,
                            onCheckedChange = {
                                alertsEnabled = it
                                Toast.makeText(context, if (it) "Temperature alerts enabled" else "Temperature alerts disabled", Toast.LENGTH_SHORT).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = FluxOrange,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFCBD5E1)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Divider(
                        color = if (isDarkTheme) Color(0xFF334155) else Color(0xFFFFD9C7).copy(alpha = 0.6f),
                        thickness = 0.5.dp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Min Limit & Max Limit Adjusters Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Min Limit Adjuster
                        Column {
                            Text(
                                text = "Min Limit",
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Minus button
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isDarkTheme) Color(0xFF334155) else Color.White)
                                        .border(0.5.dp, colors.cardBorder, CircleShape)
                                        .clickable {
                                            if (currentMinLimit > 0) {
                                                currentMinLimit--
                                                viewModel.updateThresholds(currentMinLimit.toDouble(), currentMaxLimit.toDouble())
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Remove,
                                        contentDescription = null,
                                        tint = colors.textPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = "$currentMinLimit°C",
                                    color = colors.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                // Plus button
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(FluxOrange)
                                        .clickable {
                                            if (currentMinLimit < currentMaxLimit - 1) {
                                                currentMinLimit++
                                                viewModel.updateThresholds(currentMinLimit.toDouble(), currentMaxLimit.toDouble())
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Max Limit Adjuster
                        Column {
                            Text(
                                text = "Max Limit",
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Minus button
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isDarkTheme) Color(0xFF334155) else Color.White)
                                        .border(0.5.dp, colors.cardBorder, CircleShape)
                                        .clickable {
                                            if (currentMaxLimit > currentMinLimit + 1) {
                                                currentMaxLimit--
                                                viewModel.updateThresholds(currentMinLimit.toDouble(), currentMaxLimit.toDouble())
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Remove,
                                        contentDescription = null,
                                        tint = colors.textPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = "$currentMaxLimit°C",
                                    color = colors.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                // Plus button
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(FluxOrange)
                                        .clickable {
                                            if (currentMaxLimit < 90) {
                                                currentMaxLimit++
                                                viewModel.updateThresholds(currentMinLimit.toDouble(), currentMaxLimit.toDouble())
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Card 4 — Device Information (With Animated ESP32 Board Image & Telemetry)
            val infiniteTransition = rememberInfiniteTransition(label = "SettingsDeviceAnim")
            val boardFloatY by infiniteTransition.animateFloat(
                initialValue = -3f,
                targetValue = 3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "BoardFloatY"
            )
            val boardGlow by infiniteTransition.animateFloat(
                initialValue = 0.25f,
                targetValue = 0.60f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "BoardGlow"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(26.dp), spotColor = Color(0x10000000))
                    .clip(RoundedCornerShape(26.dp))
                    .background(colors.cardBg)
                    .border(0.5.dp, colors.cardBorder, RoundedCornerShape(26.dp))
                    .clickable { onNavigateToDevice() }
                    .padding(18.dp)
            ) {
                Column {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isDarkTheme) Color(0xFF332014) else Color(0xFFFFF2EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MonitorHeart,
                                contentDescription = null,
                                tint = FluxOrange,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Device Information",
                                color = colors.textPrimary,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ESP32 telemetry, battery & signal",
                                color = colors.textSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Detail items on the left and Animated Board on the right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            // Row 1: Device Name
                            DeviceDetailRow(
                                icon = Icons.Outlined.Wifi,
                                label = "Device",
                                value = connectivityState.deviceName.ifBlank { "Flux1" },
                                colors = colors
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Row 2: Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.MonitorHeart,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Status",
                                    color = colors.textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val isOnline = connectivityState.isOnline
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isOnline) FluxGreenOnline else Color(0xFFDC2626))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isOnline) "Online" else "Offline",
                                        color = if (isOnline) FluxGreenOnline else Color(0xFFDC2626),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Row 3: Battery %
                            DeviceDetailRow(
                                icon = Icons.Outlined.Thermostat,
                                label = "Battery",
                                value = "$batteryPercent% • Healthy",
                                colors = colors
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Row 4: Signal
                            DeviceDetailRow(
                                icon = Icons.Outlined.Wifi,
                                label = "Signal",
                                value = "$signalPercent% ($signalDbm dBm)",
                                colors = colors
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Right: Animated floating photo of the ESP32 board with ambient glow
                        Box(
                            modifier = Modifier
                                .size(105.dp)
                                .offset(y = boardFloatY.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Subtle radial glow
                            Box(
                                modifier = Modifier
                                    .size(95.dp)
                                    .scale(1.15f)
                                    .alpha(boardGlow)
                                    .clip(CircleShape)
                                    .background(FluxOrange.copy(alpha = 0.35f))
                            )

                            // Image container
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0x18000000))
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isDarkTheme) Color(0xFF332014) else Color(0xFFFFF2EB))
                                    .border(1.dp, FluxOrange.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.esp32_board),
                                    contentDescription = "ESP32 Microcontroller",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
private fun DeviceDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    colors: FluxColors = rememberFluxColors(false)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            color = colors.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
