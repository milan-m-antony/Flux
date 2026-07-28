package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.MainViewModel
import com.example.ble.ConnectionState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.neoCard

@Composable
fun ScanScreen(viewModel: MainViewModel, onNavigateToHome: () -> Unit, onBack: () -> Unit) {
    val connectionState by viewModel.connectionState.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()

    androidx.compose.runtime.LaunchedEffect(connectionState) {
        if (connectionState == com.example.ble.ConnectionState.CONNECTED) {
            onNavigateToHome()
        }
    }
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.startScan()
    }
    
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(2000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        )
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(2000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            com.example.ui.theme.NeoTopBar(
                title = "Scan Device",
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
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "Connect Device",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Find and pair your Na+ sensor",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Animated scanning radar
        Box(
            modifier = Modifier
                .size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            if (connectionState == ConnectionState.SCANNING) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(AccentCyan.copy(alpha = pulseAlpha))
                )
            }
            
            com.example.ui.theme.NeoButton(
                onClick = { viewModel.startScan() },
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                enabled = connectionState != ConnectionState.SCANNING && connectionState != ConnectionState.CONNECTING,
                showShadow = false
            ) {
                if (connectionState == ConnectionState.CONNECTING) {
                    CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(48.dp))
                } else {
                    Icon(
                        imageVector = if (connectionState == ConnectionState.SCANNING) Icons.Filled.BluetoothSearching else Icons.Filled.Bluetooth,
                        contentDescription = "Scan",
                        tint = if (connectionState == ConnectionState.SCANNING) AccentCyan else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = if (connectionState == ConnectionState.SCANNING) "Scanning for devices..." else "Available Devices",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .neoCard(RoundedCornerShape(24.dp)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (discoveredDevices.isEmpty() && connectionState != ConnectionState.SCANNING) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No devices found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            
            items(discoveredDevices) { device ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoCard(RoundedCornerShape(16.dp))
                        .clickable { viewModel.connectDevice(device) }
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
                            Text(text = "Signal: ${device.rssi} dBm", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        }
    }
}
}
