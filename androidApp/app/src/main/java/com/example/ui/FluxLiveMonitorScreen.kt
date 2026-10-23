package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluxLiveMonitorScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
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

    Scaffold(
        containerColor = FluxBackground,
        topBar = {
            TopAppBar(
                title = { Text("Live Telemetry Monitor", color = FluxTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FluxTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FluxBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Gauge Display
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(FluxSurface)
                    .border(3.dp, FluxPrimary.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = FluxPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isConnected) displayTemp else "--.-",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = FluxTextPrimary
                    )
                    Text(
                        text = unitSymbol,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FluxPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isConnected) "DS18B20 Live" else "Offline",
                        fontSize = 11.sp,
                        color = if (isConnected) StatusNormal else DangerRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Telemetry Specs Grid
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FluxSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, FluxBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sensor Status", color = FluxTextSecondary, fontSize = 13.sp)
                        Text(
                            text = reading?.status ?: "Unknown",
                            color = FluxTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Divider(color = FluxBorder.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Battery Voltage", color = FluxTextSecondary, fontSize = 13.sp)
                        Text(
                            text = String.format(Locale.US, "%.2f V (%d%%)", reading?.batteryVoltage ?: 0.0, reading?.battery ?: 0),
                            color = FluxSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Divider(color = FluxBorder.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sampling Cadence", color = FluxTextSecondary, fontSize = 13.sp)
                        Text("1000 ms (Non-blocking)", color = FluxPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Divider(color = FluxBorder.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Protocol", color = FluxTextSecondary, fontSize = 13.sp)
                        Text("BLE GATT (6E400009)", color = FluxTextPrimary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
