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
import androidx.compose.material.icons.outlined.SyncAlt
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.filled.Check
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
    onNavigateToDevice: () -> Unit = {},
    onNavigateToWifiSetup: () -> Unit = {},
    onNavigateToPrivacySecurity: () -> Unit = {},
    onNavigateToHelpSupport: () -> Unit = {},
    onNavigateToVersionDetails: () -> Unit = {},
    onNavigateToFcmToken: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val lowThreshold by viewModel.lowTempThreshold.collectAsState()
    val highThreshold by viewModel.highTempThreshold.collectAsState()
    val preferredUnit by viewModel.preferredUnit.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val reading by viewModel.latestReading.collectAsState()
    val connectivityState by viewModel.connectivityState.collectAsState()
    val isOnline = connectivityState.isOnline
    val batteryPercent = reading?.battery
    val signalPercent = connectivityState.signalPercent
    val signalDbm = connectivityState.signalDbm
    val smartSwitchMode by viewModel.smartSwitchMode.collectAsState()
    val manualPreferredTransport by viewModel.manualPreferredTransport.collectAsState()

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
                                text = "Flux signature light theme active",
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
                        // Light Signature Theme Pill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(FluxOrange)
                                .clickable {
                                    Toast.makeText(context, "Flux Signature Light Theme Active", Toast.LENGTH_SHORT).show()
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
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Signature Light Theme",
                                    color = Color.White,
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

            // Card: Smart Switch Mode (Automatic vs Manual)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(26.dp), spotColor = Color(0x10000000))
                    .background(colors.cardBg, RoundedCornerShape(26.dp))
                    .border(0.5.dp, colors.cardBorder, RoundedCornerShape(26.dp))
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Row
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
                                imageVector = Icons.Outlined.SyncAlt,
                                contentDescription = null,
                                tint = FluxOrange,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Smart Switch Mode",
                                color = colors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Connection routing and failover behavior",
                                color = colors.textSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Segmented Selector: Automatic vs Manual
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.pillBg, RoundedCornerShape(20.dp))
                            .border(0.5.dp, colors.pillBorder, RoundedCornerShape(20.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Automatic Option
                        val isAuto = smartSwitchMode.equals("automatic", ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isAuto) FluxOrange else Color.Transparent)
                                .clickable {
                                    viewModel.setSmartSwitchMode("automatic")
                                    Toast.makeText(context, "Smart Switch: Automatic mode enabled", Toast.LENGTH_SHORT).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isAuto) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = "Automatic",
                                    color = if (isAuto) Color.White else colors.textSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Manual Option
                        val isManual = !isAuto
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isManual) FluxOrange else Color.Transparent)
                                .clickable {
                                    viewModel.setSmartSwitchMode("manual")
                                    Toast.makeText(context, "Smart Switch: Manual mode enabled", Toast.LENGTH_SHORT).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isManual) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = "Manual",
                                    color = if (isManual) Color.White else colors.textSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Description text
                    Text(
                        text = if (smartSwitchMode.equals("automatic", ignoreCase = true)) {
                            "Flux dynamically routes telemetry via Bluetooth when nearby, ESP32 Wi-Fi for telemetry, and phone network for cloud access. Active links are held without flapping."
                        } else {
                            "Manual mode: You choose the preferred connection transport below. Flux respects your selection without unexpected auto-switching."
                        },
                        color = colors.textSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Normal
                    )

                    // If Manual is selected, display transport selection controls
                    if (smartSwitchMode.equals("manual", ignoreCase = true)) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = colors.cardBorder, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Preferred Transport",
                            color = colors.textPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val isBlePreferred = manualPreferredTransport.equals("bluetooth", ignoreCase = true)

                            // Bluetooth Transport Pill
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isBlePreferred) (if (isDarkTheme) Color(0xFF332014) else Color(0xFFFFF2EB))
                                        else colors.pillBg
                                    )
                                    .border(
                                        1.dp,
                                        if (isBlePreferred) FluxOrange else colors.pillBorder,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        viewModel.setManualPreferredTransport("bluetooth")
                                        Toast.makeText(context, "Preferred link: Bluetooth", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Bluetooth,
                                            contentDescription = null,
                                            tint = if (isBlePreferred) FluxOrange else colors.textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Bluetooth",
                                            color = colors.textPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    val bleConnected = connectivityState.bleConnected
                                    Text(
                                        text = if (bleConnected) "Connected" else "Disconnected",
                                        color = if (bleConnected) FluxGreenOnline else colors.textSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            val isWifiPreferred = manualPreferredTransport.equals("wifi", ignoreCase = true)

                            // Wi-Fi Transport Pill
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isWifiPreferred) (if (isDarkTheme) Color(0xFF332014) else Color(0xFFFFF2EB))
                                        else colors.pillBg
                                    )
                                    .border(
                                        1.dp,
                                        if (isWifiPreferred) FluxOrange else colors.pillBorder,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        viewModel.setManualPreferredTransport("wifi")
                                        Toast.makeText(context, "Preferred link: Wi-Fi", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Wifi,
                                            contentDescription = null,
                                            tint = if (isWifiPreferred) FluxOrange else colors.textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Wi-Fi",
                                            color = colors.textPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    val wifiOnline = connectivityState.espWifiConnected
                                    Text(
                                        text = if (wifiOnline) "Online" else "Offline",
                                        color = if (wifiOnline) FluxGreenOnline else colors.textSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick system settings helper
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                                    } catch (_: Exception) {
                                        try {
                                            context.startActivity(Intent(Settings.ACTION_SETTINGS))
                                        } catch (_: Exception) {}
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Open Android Network & Radio Settings",
                                color = FluxOrange,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = FluxOrange,
                                modifier = Modifier.size(14.dp)
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
                                value = if (batteryPercent != null) "$batteryPercent% • Healthy" else if (isOnline) "Measuring..." else "Unavailable",
                                colors = colors
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Row 4: Signal
                            DeviceDetailRow(
                                icon = Icons.Outlined.Wifi,
                                label = "Signal",
                                value = if (signalPercent != null && signalDbm != null) "$signalPercent% ($signalDbm dBm)" else if (isOnline) "Connecting..." else "Disconnected",
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

            Spacer(modifier = Modifier.height(14.dp))

            // 7. Quick Device Wi-Fi Setup Button
            OutlinedButton(
                onClick = onNavigateToWifiSetup,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, FluxOrange.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = FluxOrange
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Wifi,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = FluxOrange
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Configure Device Wi-Fi Network",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 8. Account & Authentication Card
            val currentUser by viewModel.currentUser.collectAsState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color(0x10000000))
                    .background(colors.cardBg, RoundedCornerShape(24.dp))
                    .border(0.5.dp, colors.cardBorder, RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isDarkTheme) Color(0xFF332014) else Color(0xFFFFF2EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = FluxOrange,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: (if (currentUser != null) "Flux Operator" else "Guest Account"),
                                color = colors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentUser?.email ?: "Offline mode • Local Room DB active",
                                color = colors.textSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (currentUser != null) {
                        OutlinedButton(
                            onClick = {
                                viewModel.logout()
                                Toast.makeText(context, "Signed out of Flux account", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                        ) {
                            Text("Sign Out", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onNavigateToLogin,
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FluxOrange,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Sign In or Create Account", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 9. System, Privacy & Support Navigation Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color(0x10000000))
                    .background(colors.cardBg, RoundedCornerShape(24.dp))
                    .border(0.5.dp, colors.cardBorder, RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "System & Support",
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    SettingsNavRow(
                        icon = Icons.Outlined.Security,
                        title = "Privacy & Security",
                        subtitle = "Biometrics, encrypted logs & telemetry purge",
                        colors = colors,
                        onClick = onNavigateToPrivacySecurity
                    )

                    Divider(color = colors.cardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                    SettingsNavRow(
                        icon = Icons.Outlined.HelpOutline,
                        title = "Help & Support",
                        subtitle = "Hardware setup guide, FAQs & troubleshooting",
                        colors = colors,
                        onClick = onNavigateToHelpSupport
                    )

                    Divider(color = colors.cardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                    SettingsNavRow(
                        icon = Icons.Outlined.Info,
                        title = "App & Firmware Details",
                        subtitle = "v1.0.4 • ESP32 firmware release & licenses",
                        colors = colors,
                        onClick = onNavigateToVersionDetails
                    )

                    Divider(color = colors.cardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                    SettingsNavRow(
                        icon = Icons.Outlined.Key,
                        title = "FCM Push Token",
                        subtitle = "Firebase Cloud Messaging diagnostic registration",
                        colors = colors,
                        onClick = onNavigateToFcmToken
                    )
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    colors: FluxColors,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.pillBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FluxOrange,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = colors.textSecondary,
                fontSize = 11.sp
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(18.dp)
        )
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
