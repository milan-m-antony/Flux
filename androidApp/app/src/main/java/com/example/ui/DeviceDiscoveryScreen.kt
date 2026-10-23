package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ble.BLEViewModel
import com.example.ble.ConnectionState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.neoCard
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ble.BLEPermissionsManager
import androidx.compose.ui.platform.LocalContext

@Composable
fun DeviceDiscoveryScreen(
    onNavigateToHome: () -> Unit,
    onBack: () -> Unit,
    bleViewModel: BLEViewModel = viewModel()
) {
    val connectionState by bleViewModel.connectionState.collectAsState()
    val discoveredDevices by bleViewModel.discoveredDevices.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            bleViewModel.startScan()
        }
    }

    LaunchedEffect(connectionState) {
        if (connectionState == ConnectionState.CONNECTED) {
            onNavigateToHome()
        }
    }

    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            com.example.ui.theme.NeoTopBar(
                title = "Device Discovery",
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Flux Device Discovery",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Pair with your Flux ESP32 sensor to stream telemetry",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (connectionState == ConnectionState.SCANNING) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(AccentCyan.copy(alpha = pulseAlpha))
                        )
                    }

                    com.example.ui.theme.NeoButton(
                        onClick = {
                            if (BLEPermissionsManager.hasPermissions(context)) {
                                bleViewModel.startScan()
                            } else {
                                permissionLauncher.launch(BLEPermissionsManager.permissions.toTypedArray())
                            }
                        },
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        enabled = connectionState != ConnectionState.SCANNING && connectionState != ConnectionState.CONNECTING,
                        showShadow = false
                    ) {
                        if (connectionState == ConnectionState.CONNECTING) {
                            CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(36.dp))
                        } else {
                            Icon(
                                imageVector = if (connectionState == ConnectionState.SCANNING) Icons.Filled.BluetoothSearching else Icons.Filled.Bluetooth,
                                contentDescription = "Scan",
                                tint = if (connectionState == ConnectionState.SCANNING) AccentCyan else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = if (connectionState == ConnectionState.SCANNING) "Scanning..." else "Found Flux Sensors",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .neoCard(RoundedCornerShape(24.dp)),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (discoveredDevices.isEmpty() && connectionState != ConnectionState.SCANNING) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("Tap the icon above to search for devices", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    items(discoveredDevices) { device ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .neoCard(RoundedCornerShape(16.dp))
                                .clickable { bleViewModel.connect(device.address) }
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(AccentCyan.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Bluetooth, contentDescription = null, tint = AccentCyan)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(text = device.name, fontWeight = FontWeight.Bold)
                                    Text(text = device.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "${device.rssi} dBm",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AccentCyan
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
