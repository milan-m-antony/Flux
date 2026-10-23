package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
fun FluxStatsTimerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val reading by viewModel.latestReading.collectAsState()
    val preferredUnit by viewModel.preferredUnit.collectAsState()
    val isConnected = connectionState == ConnectionState.CONNECTED

    val unitSymbol = if (preferredUnit == "F") "°F" else "°C"
    val minVal = reading?.minTemp ?: 0.0
    val maxVal = reading?.maxTemp ?: 0.0
    val avgVal = reading?.avgTemp ?: 0.0

    Scaffold(
        containerColor = FluxBackground,
        topBar = {
            TopAppBar(
                title = { Text("Session Statistics & Timer", color = FluxTextPrimary, fontWeight = FontWeight.Bold) },
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
            // Elapsed Monotonic Duration
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = FluxSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, FluxBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ACTIVE MONITORING DURATION", color = FluxTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    val elapsedSec = reading?.elapsedSec ?: 0L
                    val h = elapsedSec / 3600
                    val m = (elapsedSec % 3600) / 60
                    val s = elapsedSec % 60
                    Text(
                        text = String.format(Locale.US, "%02d:%02d:%02d", h, m, s),
                        color = FluxPrimary,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Monotonic Timer (Uptime Sync)",
                        color = FluxTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Stat Cards 2x2 Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = FluxSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, FluxBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("SESSION MIN", color = FluxTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isConnected && minVal > -50.0) String.format(Locale.US, "%.1f %s", minVal, unitSymbol) else "--",
                            color = FluxSecondary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = FluxSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, FluxBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("SESSION MAX", color = FluxTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isConnected && maxVal > -50.0) String.format(Locale.US, "%.1f %s", maxVal, unitSymbol) else "--",
                            color = DangerRed,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = FluxSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, FluxBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("SESSION AVERAGE", color = FluxTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isConnected && avgVal > -50.0) String.format(Locale.US, "%.1f %s", avgVal, unitSymbol) else "--",
                            color = FluxTextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = FluxSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, FluxBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("DATA POINTS", color = FluxTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isConnected) "Live Active" else "Paused",
                            color = FluxPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Reset Button
            Button(
                onClick = {
                    viewModel.createNotification("Session Reset", "Thermal session statistics and timer were restarted.", "INFO")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FluxSurfaceElevated),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = FluxPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Restart Session Statistics", color = FluxPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
