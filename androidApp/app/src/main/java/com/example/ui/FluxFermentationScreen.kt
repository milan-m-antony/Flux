package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timer
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
fun FluxFermentationScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val reading by viewModel.latestReading.collectAsState()
    val preferredUnit by viewModel.preferredUnit.collectAsState()
    val lowThreshold by viewModel.lowTempThreshold.collectAsState()
    val highThreshold by viewModel.highTempThreshold.collectAsState()
    val isConnected = connectionState == ConnectionState.CONNECTED

    val rawTemp = reading?.temp ?: 0.0
    val displayTemp = if (preferredUnit == "F") {
        if (rawTemp > -50.0) String.format(Locale.US, "%.1f", (rawTemp * 9.0 / 5.0) + 32.0) else "--.-"
    } else {
        if (rawTemp > -50.0) String.format(Locale.US, "%.1f", rawTemp) else "--.-"
    }
    val unitSymbol = if (preferredUnit == "F") "°F" else "°C"
    val inRange = rawTemp in 18.0..24.0

    Scaffold(
        containerColor = FluxBackground,
        topBar = {
            TopAppBar(
                title = { Text("Fermentation Profile", color = FluxTextPrimary, fontWeight = FontWeight.Bold) },
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
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (inRange) FluxPrimary else DangerRed
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Science, contentDescription = null, tint = FluxPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Vessel Temperature", color = FluxTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (inRange) StatusNormal.copy(alpha = 0.2f) else DangerRed.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (inRange) "IN BAND (18-24°C)" else "OUT OF BAND!",
                                color = if (inRange) StatusNormal else DangerRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (isConnected) "$displayTemp $unitSymbol" else "--.-",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (inRange) FluxPrimary else DangerRed
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (inRange) "Yeast thermal kinetics optimal" else "Warning: Check temperature regulation!",
                        color = FluxTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            // Elapsed Fermentation Duration Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FluxSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, FluxBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = FluxSecondary, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Active Fermentation Time", color = FluxTextSecondary, fontSize = 12.sp)
                        val elapsedSec = reading?.elapsedSec ?: 0L
                        val h = elapsedSec / 3600
                        val m = (elapsedSec % 3600) / 60
                        val s = elapsedSec % 60
                        Text(
                            text = String.format(Locale.US, "%02d hours %02d mins %02d secs", h, m, s),
                            color = FluxTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Scientific Transparency Disclaimer (Section 5.4)
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
                        text = "Notice: This profile measures temperature only. It does not measure or infer sugar attenuation, specific gravity (Brix/Plato), pH, ethanol yield, dissolved oxygen, or fermentation completion.",
                        color = FluxTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
