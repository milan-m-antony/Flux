package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ble.BLEViewModel
import com.example.ble.ConnectionState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.neoCard

@Composable
fun BluetoothSettingsScreen(
    onBack: () -> Unit,
    bleViewModel: BLEViewModel = viewModel()
) {
    val connectionState by bleViewModel.connectionState.collectAsState()
    val reading by bleViewModel.latestReading.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            com.example.ui.theme.NeoTopBar(
                title = "Bluetooth Settings",
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Card showing Connection Status
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoCard(RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (connectionState == ConnectionState.CONNECTED) Icons.Filled.Bluetooth else Icons.Filled.BluetoothDisabled,
                            contentDescription = null,
                            tint = if (connectionState == ConnectionState.CONNECTED) AccentCyan else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Status: ${connectionState.name}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Details Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoCard(RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        val currentReading = reading

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = AccentCyan)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Device Name", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Flux ESP32 Sensor", fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = AccentCyan)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Firmware Version", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currentReading?.status ?: "v1.0.0", fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.OfflineBolt, contentDescription = null, tint = AccentCyan)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Battery Voltage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val voltVal = if (currentReading != null) String.format("%.2f V", currentReading.mv / 1000.0) else "--- V"
                                Text(voltVal, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    com.example.ui.theme.NeoButton(
                        onClick = {
                            if (connectionState == ConnectionState.CONNECTED) {
                                bleViewModel.disconnect()
                            } else {
                                bleViewModel.startScan()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (connectionState == ConnectionState.CONNECTED) "Disconnect" else "Reconnect")
                    }
                }
            }
        }
    }
}
