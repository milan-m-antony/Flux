package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
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
import com.example.ble.ConnectionState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluxDiagnosticsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val reading by viewModel.latestReading.collectAsState()
    val syncStatus by viewModel.supabaseSyncStatus.collectAsState()
    val pendingRecords by viewModel.pendingOfflineRecords.collectAsState()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "System Diagnostics",
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
            // BLE Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bluetooth Link (BLE)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        val isConn = connectionState == ConnectionState.CONNECTED
                        Text(
                            if (isConn) "CONNECTED" else connectionState.name,
                            color = if (isConn) AccentGreen else AlertRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    DiagRow("Target Peripheral", "Flux-Telemetry / ESP32-WROOM")
                    DiagRow("Service UUID", "6E400011-B5A3-F393-E0A9-E50E24DCCA9E")
                    DiagRow("Telemetry Char UUID", "6E400009-B5A3-F393-E0A9-E50E24DCCA9E")
                    DiagRow("Command Char UUID", "6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
                    DiagRow("Negotiated MTU", "512 bytes")
                }
            }

            // Sensor & Hardware Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Hardware & Peripherals", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    DiagRow("Temperature Sensor", "Maxim DS18B20 1-Wire (GPIO 4)")
                    DiagRow("Sensor Bus State", if (reading?.temp != null) "ONLINE / 12-Bit Digital" else "DISCONNECTED")
                    DiagRow("Local Display", "SSD1306 128x64 OLED I2C (0x3C, GPIO 21/22)")
                    DiagRow("Display Refresh Rate", "1.0 Hz (Non-blocking timer)")
                    DiagRow("Physical Buttons", "3x Push Buttons (GPIO 16, 17, 18)")
                    DiagRow("Firmware Version", "Flux v2.0.0-PRO")
                }
            }

            // Cloud Synchronization Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Supabase Cloud Telemetry", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            syncStatus,
                            color = if (syncStatus == "SYNCED") AccentGreen else AccentCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    DiagRow("Database Engine", "Supabase PostgreSQL + RLS")
                    DiagRow("Telemetry Table", "public.telemetry")
                    DiagRow("Device State Table", "public.device_state")
                    DiagRow("Realtime Sync", "PostgreSQL WAL Replication")
                    DiagRow("Cloud Upload Rate", "5s Default (Configurable)")
                    DiagRow("Offline Buffer Queue", "$pendingRecords / 500 records")

                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { viewModel.flushOfflineQueue() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ElevatedSurface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Force Flush Offline Queue", color = AccentCyan, fontSize = 13.sp)
                    }
                }
            }

            // Push Notifications Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("OneSignal Notification Pipeline", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    DiagRow("Push Service", "OneSignal REST API v1")
                    DiagRow("Dispatch Trigger", "Supabase Edge Function: flux-alert-evaluator")
                    DiagRow("Secret Isolation", "Server-Side Supabase Secrets (Zero Client Leak)")
                    DiagRow("Hysteresis Window", "15 minutes per continuous event")
                }
            }
        }
    }
}

@Composable
private fun DiagRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Text(value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
