package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ble.ConnectionState
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluxLiquidScreen(
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
                title = { Text("Liquid Monitoring Profile", color = FluxTextPrimary, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = FluxSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, FluxBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = FluxSecondary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Liquid Probe Temperature", color = FluxTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (isConnected) "$displayTemp $unitSymbol" else "--.-",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = FluxSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (!isConnected) "⚠️ Probe Disconnected or Out of Range" 
                               else "Probe Active • Submerged Liquid Reading",
                        color = if (isConnected) StatusNormal else DangerRed,
                        fontSize = 13.sp
                    )
                }
            }

            // Material & Immersion Notice (Section 5.3)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FluxSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, FluxBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = FluxSecondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Hardware Notice: Probe immersion safety depends on your specific probe's stainless-steel sheath rating (standard DS18B20 waterproof capsule). Do not use in corrosive, highly acidic, or unverified food-preparation solutions without explicit certification.",
                        color = FluxTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
