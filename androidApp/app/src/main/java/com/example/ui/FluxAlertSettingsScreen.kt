package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluxAlertSettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val lowThresholdC by viewModel.lowTempThreshold.collectAsState()
    val highThresholdC by viewModel.highTempThreshold.collectAsState()
    val preferredUnit by viewModel.preferredUnit.collectAsState()

    var lowVal by remember(lowThresholdC) { mutableFloatStateOf(lowThresholdC.toFloat()) }
    var highVal by remember(highThresholdC) { mutableFloatStateOf(highThresholdC.toFloat()) }
    var pushEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var saveSuccess by remember { mutableStateOf(false) }

    val unitSymbol = if (preferredUnit == "F") "°F" else "°C"
    fun toDisp(c: Float) = if (preferredUnit == "F") (c * 9f / 5f) + 32f else c

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Alert Configuration",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Notifications, contentDescription = null, tint = AccentGreen)
                    }
                    Column {
                        Text("Threshold Safety Limits", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("OneSignal push notifications trigger when readings breach limits", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }

            // High Limit Slider Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Upper Critical Threshold", color = TextSecondary, fontSize = 13.sp)
                        Text(
                            "%.1f %s".format(toDisp(highVal), unitSymbol),
                            color = AlertRed,
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
                            thumbColor = AlertRed,
                            activeTrackColor = AlertRed,
                            inactiveTrackColor = ElevatedSurface
                        )
                    )
                    Text("Triggers HIGH temperature push notification via Supabase & OneSignal", color = TextSecondary, fontSize = 11.sp)
                }
            }

            // Low Limit Slider Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Lower Critical Threshold", color = TextSecondary, fontSize = 13.sp)
                        Text(
                            "%.1f %s".format(toDisp(lowVal), unitSymbol),
                            color = AccentCyan,
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
                            thumbColor = AccentCyan,
                            activeTrackColor = AccentCyan,
                            inactiveTrackColor = ElevatedSurface
                        )
                    )
                    Text("Triggers LOW temperature push notification via Supabase & OneSignal", color = TextSecondary, fontSize = 11.sp)
                }
            }

            // Push Notification Options
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Delivery Channels", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("OneSignal Push Notifications", color = TextPrimary, fontSize = 14.sp)
                            Text("Cloud notifications even when app is closed", color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = pushEnabled,
                            onCheckedChange = { pushEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AccentGreen,
                                checkedTrackColor = AccentGreen.copy(alpha = 0.3f),
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = ElevatedSurface
                            )
                        )
                    }

                    HorizontalDivider(color = ElevatedSurface)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("In-App Audio / Haptic Alert", color = TextPrimary, fontSize = 14.sp)
                            Text("Vibrate and play alert tone on limit breach", color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { soundEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AccentGreen,
                                checkedTrackColor = AccentGreen.copy(alpha = 0.3f),
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = ElevatedSurface
                            )
                        )
                    }
                }
            }

            // Hysteresis & Anti-Flap Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                    Text(
                        "Deduplication Hysteresis: Alerts are rate-limited to 1 per 15-minute window for the same continuous breach state to prevent notification spam.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            if (saveSuccess) {
                Text(
                    "Thresholds saved and synced successfully!",
                    color = AccentGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
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
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Thresholds", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
