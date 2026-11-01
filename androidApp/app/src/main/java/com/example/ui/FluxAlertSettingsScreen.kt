package com.example.ui

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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.theme.*

@Composable
fun FluxAlertSettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val darkTheme by viewModel.isDarkTheme.collectAsState()
    val colors = rememberFluxColors(darkTheme)
    val lowThresholdC by viewModel.lowTempThreshold.collectAsState()
    val highThresholdC by viewModel.highTempThreshold.collectAsState()
    val preferredUnit by viewModel.preferredUnit.collectAsState()
    val latestReading by viewModel.latestReading.collectAsState()

    var lowVal by remember(lowThresholdC) { mutableFloatStateOf(lowThresholdC.toFloat()) }
    var highVal by remember(highThresholdC) { mutableFloatStateOf(highThresholdC.toFloat()) }
    var pushEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var hardwareBuzzerEnabled by remember(latestReading?.buzzer) { mutableStateOf(latestReading?.buzzer ?: true) }
    var saveSuccess by remember { mutableStateOf(false) }

    val unitSymbol = if (preferredUnit == "F") "°F" else "°C"
    fun toDisp(c: Float) = if (preferredUnit == "F") (c * 9f / 5f) + 32f else c

    FluxBackground(darkTheme = darkTheme) {
        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 640.dp)
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                Spacer(modifier = Modifier.height(8.dp))

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
                        text = "Alert Configuration",
                        color = colors.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                }

                // Header Overview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.cardBg)
                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(FluxOrange.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Notifications, contentDescription = null, tint = FluxOrange)
                        }
                        Column {
                            Text("Threshold Safety Limits", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Push notifications trigger when telemetry breaches limits", color = colors.textSecondary, fontSize = 12.sp)
                        }
                    }
                }

                // High Limit Slider Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.cardBg)
                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Upper Critical Threshold", color = colors.textSecondary, fontSize = 13.sp)
                            Text(
                                "%.1f %s".format(toDisp(highVal), unitSymbol),
                                color = Color(0xFFEF4444),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = highVal,
                            onValueChange = { highVal = it.coerceAtLeast(lowVal + 1f) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFEF4444),
                                activeTrackColor = Color(0xFFEF4444),
                                inactiveTrackColor = colors.pillBg
                            )
                        )
                        Text("Triggers HIGH temperature notification alert", color = colors.textSecondary, fontSize = 11.sp)
                    }
                }

                // Low Limit Slider Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.cardBg)
                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Lower Critical Threshold", color = colors.textSecondary, fontSize = 13.sp)
                            Text(
                                "%.1f %s".format(toDisp(lowVal), unitSymbol),
                                color = Color(0xFF3B82F6),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = lowVal,
                            onValueChange = { lowVal = it.coerceAtMost(highVal - 1f) },
                            valueRange = -20f..80f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF3B82F6),
                                activeTrackColor = Color(0xFF3B82F6),
                                inactiveTrackColor = colors.pillBg
                            )
                        )
                        Text("Triggers LOW temperature notification alert", color = colors.textSecondary, fontSize = 11.sp)
                    }
                }

                // Alert Delivery Channels Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.cardBg)
                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Delivery Channels", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Push Notifications", color = colors.textPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                                Text("Alert when app is closed or backgrounded", color = colors.textSecondary, fontSize = 11.5.sp)
                            }
                            Switch(
                                checked = pushEnabled,
                                onCheckedChange = { pushEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = FluxOrange,
                                    uncheckedThumbColor = colors.textSecondary,
                                    uncheckedTrackColor = colors.pillBg
                                )
                            )
                        }

                        Divider(color = colors.cardBorder.copy(alpha = 0.6f), thickness = 0.5.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("In-App Audio & Haptics", color = colors.textPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                                Text("Sound alert tone on safety limit breach", color = colors.textSecondary, fontSize = 11.5.sp)
                            }
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = { soundEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = FluxOrange,
                                    uncheckedThumbColor = colors.textSecondary,
                                    uncheckedTrackColor = colors.pillBg
                                )
                            )
                        }

                        Divider(color = colors.cardBorder.copy(alpha = 0.6f), thickness = 0.5.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ESP32 Hardware Buzzer (GPIO 25)", color = colors.textPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                                Text("Audible hardware alarm beeps from device", color = colors.textSecondary, fontSize = 11.5.sp)
                            }
                            Switch(
                                checked = hardwareBuzzerEnabled,
                                onCheckedChange = {
                                    hardwareBuzzerEnabled = it
                                    viewModel.setBuzzerEnabled(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = FluxOrange,
                                    uncheckedThumbColor = colors.textSecondary,
                                    uncheckedTrackColor = colors.pillBg
                                )
                            )
                        }
                    }
                }

                // Hysteresis Info Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.pillBg)
                        .border(0.5.dp, colors.pillBorder, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = FluxOrange, modifier = Modifier.size(20.dp))
                        Text(
                            "Anti-Flap Hysteresis: Alerts maintain a 0.3°C buffer and 5-minute cooldown to prevent spam during boundary oscillation.",
                            color = colors.textSecondary,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                if (saveSuccess) {
                    Text(
                        "Thresholds saved and synced successfully!",
                        color = Color(0xFF10B981),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Save Button
                Button(
                    onClick = {
                        viewModel.updateThresholds(lowVal.toDouble(), highVal.toDouble())
                        saveSuccess = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FluxOrange,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Save Thresholds", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}
