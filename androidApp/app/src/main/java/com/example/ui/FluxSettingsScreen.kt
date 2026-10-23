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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.R
import com.example.ui.theme.*

@Composable
fun FluxSettingsScreen(
    viewModel: MainViewModel,
    onNotificationClick: () -> Unit = {}
) {
    val lowThreshold by viewModel.lowTempThreshold.collectAsState()
    val highThreshold by viewModel.highTempThreshold.collectAsState()
    val preferredUnit by viewModel.preferredUnit.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()

    var alertsEnabled by remember { mutableStateOf(true) }
    var currentMinLimit by remember(lowThreshold) { mutableStateOf(lowThreshold.toInt()) }
    var currentMaxLimit by remember(highThreshold) { mutableStateOf(highThreshold.toInt()) }
    var selectedTheme by remember(isDarkTheme) { mutableStateOf(if (isDarkTheme) "Dark" else "Light") }
    var selectedUnit by remember(preferredUnit) { mutableStateOf(preferredUnit) }

    val isOnline = connectionState == com.example.ble.ConnectionState.CONNECTED || true

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Shared Flux Header
            FluxHeader(
                isOnline = isOnline,
                hasUnreadAlerts = true,
                onNotificationClick = onNotificationClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Title & Subtitle
            Text(
                text = "Settings",
                color = FluxTextDark,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Personalize your Flux experience",
                color = FluxTextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Card 1 — App Theme (Light / Dark Selection)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(26.dp), spotColor = Color(0x10000000))
                    .background(Color.White, RoundedCornerShape(26.dp))
                    .border(0.5.dp, Color(0xFFF1F5F9), RoundedCornerShape(26.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sun Icon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF2EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LightMode,
                            contentDescription = null,
                            tint = FluxOrange,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "App Theme",
                            color = FluxTextDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Choose how Flux looks",
                            color = FluxTextMuted,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Light / Dark Toggle Capsule
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(20.dp))
                            .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                            .padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Light Option
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedTheme == "Light") Color.White else Color.Transparent)
                                .border(
                                    width = if (selectedTheme == "Light") 1.dp else 0.dp,
                                    color = if (selectedTheme == "Light") FluxOrange else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    selectedTheme = "Light"
                                    viewModel.setDarkTheme(false)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LightMode,
                                contentDescription = null,
                                tint = if (selectedTheme == "Light") FluxOrange else FluxTextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Light",
                                color = if (selectedTheme == "Light") FluxOrange else FluxTextMuted,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Dark Option
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedTheme == "Dark") Color.White else Color.Transparent)
                                .clickable {
                                    selectedTheme = "Dark"
                                    viewModel.setDarkTheme(true)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DarkMode,
                                contentDescription = null,
                                tint = if (selectedTheme == "Dark") FluxTextDark else FluxTextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Dark",
                                color = if (selectedTheme == "Dark") FluxTextDark else FluxTextMuted,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
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
                    .background(Color.White, RoundedCornerShape(26.dp))
                    .border(0.5.dp, Color(0xFFF1F5F9), RoundedCornerShape(26.dp))
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
                            .background(Color(0xFFDBEAFE)),
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
                            color = FluxTextDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Display temperature in",
                            color = FluxTextMuted,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // °C / °F Pill
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(20.dp))
                            .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                            .padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedUnit == "C") Color(0xFF111827) else Color.Transparent)
                                .clickable {
                                    selectedUnit = "C"
                                    viewModel.setPreferredUnit("C")
                                }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "°C",
                                color = if (selectedUnit == "C") Color.White else FluxTextMuted,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedUnit == "F") Color(0xFF111827) else Color.Transparent)
                                .clickable {
                                    selectedUnit = "F"
                                    viewModel.setPreferredUnit("F")
                                }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "°F",
                                color = if (selectedUnit == "F") Color.White else FluxTextMuted,
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
                            colors = listOf(Color(0xFFFFF9F5), Color(0xFFFFE8DC))
                        )
                    )
                    .border(0.5.dp, Color(0xFFFFD9C7), RoundedCornerShape(26.dp))
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
                                .background(Color.White),
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
                                color = FluxTextDark,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Get notified when temperature is out of range",
                                color = FluxTextMuted,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Switch(
                            checked = alertsEnabled,
                            onCheckedChange = { alertsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = FluxOrange,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFCBD5E1)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Divider(color = Color(0xFFFFD9C7).copy(alpha = 0.6f), thickness = 0.5.dp)

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
                                color = FluxTextMuted,
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
                                        .background(Color.White)
                                        .border(0.5.dp, Color(0xFFE2E8F0), CircleShape)
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
                                        tint = FluxTextDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = "$currentMinLimit°C",
                                    color = FluxTextDark,
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
                                color = FluxTextMuted,
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
                                        .background(Color.White)
                                        .border(0.5.dp, Color(0xFFE2E8F0), CircleShape)
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
                                        tint = FluxTextDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = "$currentMaxLimit°C",
                                    color = FluxTextDark,
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

            // 6. Card 4 — Device Information (With Angled ESP32 Board Image)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(26.dp), spotColor = Color(0x10000000))
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White)
                    .border(0.5.dp, Color(0xFFF1F5F9), RoundedCornerShape(26.dp))
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
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.esp32_board),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Device Information",
                                color = FluxTextDark,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "View your connected device details",
                                color = FluxTextMuted,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Detail items on the left and Angled Board on the right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            // Row 1: Device
                            DeviceDetailRow(
                                icon = Icons.Outlined.Wifi,
                                label = "Device",
                                value = "ESP32"
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Row 2: Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.MonitorHeart,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Status",
                                    color = FluxTextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(FluxGreenOnline)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Online",
                                        color = FluxTextDark,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Row 3: Device ID
                            DeviceDetailRow(
                                icon = Icons.Outlined.LocationOn,
                                label = "Device ID",
                                value = "DS18B20 • ESP32"
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Right: Angled photo of the ESP32 board
                        Box(
                            modifier = Modifier
                                .size(105.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFFFF2EB)),
                            contentAlignment = Alignment.Center
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

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
private fun DeviceDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            color = FluxTextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            color = FluxTextDark,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
