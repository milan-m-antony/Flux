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
import androidx.compose.material.icons.filled.Refresh
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

@Composable
fun FluxDiagnosticsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val darkTheme by viewModel.isDarkTheme.collectAsState()
    val colors = rememberFluxColors(darkTheme)
    val connectionState by viewModel.connectionState.collectAsState()
    val reading by viewModel.latestReading.collectAsState()
    val syncStatus by viewModel.supabaseSyncStatus.collectAsState()
    val pendingRecords by viewModel.pendingOfflineRecords.collectAsState()

    FluxBackground(darkTheme = darkTheme) {
        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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
                        text = "System Diagnostics",
                        color = colors.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                }

                // BLE Link Status Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.cardBg)
                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Bluetooth Link (BLE)", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            val isConn = connectionState == ConnectionState.CONNECTED
                            Text(
                                if (isConn) "CONNECTED" else connectionState.name,
                                color = if (isConn) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        DiagRow("Target Peripheral", "Flux1 / ESP32-WROOM", colors)
                        DiagRow("Service UUID", "6E400011-B5A3-F393-E0A9-E50E24DCCA9E", colors)
                        DiagRow("Telemetry Char UUID", "6E400009-B5A3-F393-E0A9-E50E24DCCA9E", colors)
                        DiagRow("Command Char UUID", "6E400002-B5A3-F393-E0A9-E50E24DCCA9E", colors)
                        DiagRow("Negotiated MTU", "512 bytes", colors)
                    }
                }

                // Hardware & Peripherals Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.cardBg)
                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Hardware & Peripherals", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        DiagRow("Temperature Sensor", "Maxim DS18B20 1-Wire (GPIO 4)", colors)
                        DiagRow("Sensor Bus State", if (reading?.temp != null) "ONLINE / 12-Bit Digital" else "DISCONNECTED", colors)
                        DiagRow("Local Display", "SSD1306 128x64 OLED I2C (0x3C, GPIO 21/22)", colors)
                        DiagRow("Display Refresh Rate", "1.0 Hz (Non-blocking timer)", colors)
                        DiagRow("Physical Buttons", "3x Push Buttons (GPIO 16, 17, 18)", colors)
                        DiagRow("Firmware Version", "Flux v2.0.0-PRO", colors)
                    }
                }

                // Cloud Synchronization Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.cardBg)
                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Supabase Cloud Telemetry", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                syncStatus,
                                color = if (syncStatus == "SYNCED") Color(0xFF10B981) else FluxOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        DiagRow("Database Engine", "Supabase PostgreSQL + RLS", colors)
                        DiagRow("Telemetry Table", "public.telemetry", colors)
                        DiagRow("Device State Table", "public.device_state", colors)
                        DiagRow("Realtime Sync", "PostgreSQL WAL Replication", colors)
                        DiagRow("Cloud Upload Rate", "5s Default (Configurable)", colors)
                        DiagRow("Offline Buffer Queue", "$pendingRecords / 500 records", colors)

                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = { viewModel.flushOfflineQueue() },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FluxOrange,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Force Flush Offline Queue", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                // Push Notification Pipeline Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.cardBg)
                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("OneSignal Notification Pipeline", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        DiagRow("Push Service", "OneSignal REST API v1", colors)
                        DiagRow("Dispatch Trigger", "Supabase Edge Function: flux-alert-evaluator", colors)
                        DiagRow("Secret Isolation", "Server-Side Supabase Secrets (Zero Client Leak)", colors)
                        DiagRow("Hysteresis Window", "15 minutes per continuous event", colors)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun DiagRow(label: String, value: String, colors: FluxColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = colors.textSecondary, fontSize = 12.sp)
        Text(value, color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
