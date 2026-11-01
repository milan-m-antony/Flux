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
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.CheckCircle
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
    val isConnected = connectionState == ConnectionState.CONNECTED

    val rawTemp = reading?.temp ?: 0.0
    val displayTemp = if (preferredUnit == "F") {
        if (rawTemp > -50.0) String.format(Locale.US, "%.1f", (rawTemp * 9.0 / 5.0) + 32.0) else "--.-"
    } else {
        if (rawTemp > -50.0) String.format(Locale.US, "%.1f", rawTemp) else "--.-"
    }
    val unitSymbol = if (preferredUnit == "F") "°F" else "°C"

    FluxBackground(darkTheme = darkTheme) {
        Scaffold(
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
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

                    Text(
                        text = "Live Telemetry Monitor",
                        color = colors.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Hero Circular Gauge
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .shadow(8.dp, CircleShape, spotColor = Color(0x18000000))
                        .clip(CircleShape)
                        .background(colors.cardBg)
                        .border(3.dp, FluxOrange.copy(alpha = 0.85f), CircleShape),
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
                            fontSize = 46.sp,
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
                                .background(if (isConnected) Color(0xFF10B981).copy(alpha = 0.14f) else Color(0xFFEF4444).copy(alpha = 0.14f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isConnected) "DS18B20 Live Stream" else "Sensor Disconnected",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
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
                            colors = colors
                        )

                        Divider(color = colors.cardBorder.copy(alpha = 0.6f), thickness = 0.5.dp)

                        MetricRow(
                            icon = Icons.Outlined.BatteryChargingFull,
                            label = "Battery Voltage",
                            value = String.format(Locale.US, "%.2f V (%d%%)", reading?.batteryVoltage ?: 0.0, reading?.battery ?: 0),
                            valueColor = colors.textPrimary,
                            colors = colors
                        )

                        Divider(color = colors.cardBorder.copy(alpha = 0.6f), thickness = 0.5.dp)

                        MetricRow(
                            icon = Icons.Outlined.Speed,
                            label = "Sampling Interval",
                            value = "1000 ms (Real-time)",
                            valueColor = FluxOrange,
                            colors = colors
                        )

                        Divider(color = colors.cardBorder.copy(alpha = 0.6f), thickness = 0.5.dp)

                        MetricRow(
                            icon = Icons.Outlined.Bluetooth,
                            label = "BLE Telemetry Link",
                            value = if (isConnected) "Connected (GATT)" else "Disconnected",
                            valueColor = if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444),
                            colors = colors
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
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
    colors: FluxColors
) {
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
        Text(
            text = value,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
