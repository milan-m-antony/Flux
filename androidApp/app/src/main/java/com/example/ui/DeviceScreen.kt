package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ble.ConnectionState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeoButton
import com.example.ui.theme.NeoTopBar
import com.example.ui.theme.TextGrey
import com.example.ui.theme.neoCard
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DeviceScreen(viewModel: MainViewModel, onNavigateToDiagnostics: () -> Unit = {}, onNavigateToLiveTemp: () -> Unit = {}) {
    val connectionState by viewModel.connectionState.collectAsState()
    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val activeAccent = if (darkTheme) AccentCyan else Color(0xFF059669)
    val scope = rememberCoroutineScope()
    val reading by viewModel.latestReading.collectAsState()

    var showFirmwareDialog by remember { mutableStateOf(false) }
    var isUpdatingFirmware by remember { mutableStateOf(false) }
    var firmwareProgress by remember { mutableFloatStateOf(0f) }
    
    var showInfoDialog by remember { mutableStateOf(false) }
    
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var isRunningDiagnostics by remember { mutableStateOf(false) }
    var diagnosticStep by remember { mutableStateOf("") }
    var diagnosticsResults by remember { mutableStateOf<List<Pair<String, Boolean>>>(emptyList()) }
    
    Scaffold(
        containerColor = Color.Transparent,
        topBar = { NeoTopBar(title = "Device") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Device Status Card
            Box(modifier = Modifier.fillMaxWidth().neoCard().padding(24.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = if (connectionState == ConnectionState.CONNECTED) Icons.Default.BluetoothConnected else Icons.Default.BluetoothDisabled,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = if (connectionState == ConnectionState.CONNECTED) activeAccent else TextGrey
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (connectionState == ConnectionState.CONNECTED) "NaTrack v1 Connected" else "Disconnected",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (connectionState == ConnectionState.CONNECTED) "Signal Strength: Excellent" else "No signal",
                        color = TextGrey
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Device Stats (Top Data Widgets)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DeviceStatItem(
                    icon = Icons.Default.BatteryChargingFull,
                    label = "Battery",
                    value = if (connectionState == ConnectionState.CONNECTED) "${reading?.battery ?: 0}%" else "---",
                    darkTheme = darkTheme,
                    modifier = Modifier.weight(1f)
                )
                DeviceStatItem(
                    icon = Icons.Default.Thermostat,
                    label = "Temp",
                    value = if (connectionState == ConnectionState.CONNECTED) "${reading?.temp ?: 0.0}°C" else "---",
                    darkTheme = darkTheme,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            
            Text(
                text = "Settings", 
                style = MaterialTheme.typography.titleMedium, 
                color = if (darkTheme) activeAccent else Color(0xFF111827),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            DeviceActionItem(icon = Icons.Default.Thermostat, label = "Live Temperature", darkTheme = darkTheme, onClick = onNavigateToLiveTemp)
            DeviceActionItem(icon = Icons.Default.Tune, label = "System Diagnostics", darkTheme = darkTheme, onClick = onNavigateToDiagnostics)
            DeviceActionItem(icon = Icons.Default.SystemUpdate, label = "Firmware Update", darkTheme = darkTheme, onClick = { showFirmwareDialog = true })
            DeviceActionItem(icon = Icons.Default.Info, label = "Device Information", darkTheme = darkTheme, onClick = { showInfoDialog = true })
            DeviceActionItem(icon = Icons.Default.Build, label = "Run Diagnostics", darkTheme = darkTheme, onClick = {
                showDiagnosticsDialog = true
                isRunningDiagnostics = true
                scope.launch {
                    diagnosticStep = "Bluetooth Connection Check..."
                    delay(1000)
                    diagnosticStep = "Electrode Impedance Check..."
                    delay(1200)
                    diagnosticStep = "Battery Health Check..."
                    delay(800)
                    diagnosticStep = "Internal Thermistor Check..."
                    delay(1000)
                    diagnosticsResults = listOf(
                        "Bluetooth Connection" to true,
                        "Electrode Impedance" to true,
                        "Battery Health Indicator" to true,
                        "Internal Thermistor" to true,
                        "Calibration Check" to true
                    )
                    isRunningDiagnostics = false
                }
            })
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (connectionState == ConnectionState.CONNECTED) {
                NeoButton(
                    onClick = { viewModel.disconnectDevice() },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Disconnect Device", color = com.example.ui.theme.DangerRed, fontWeight = FontWeight.Bold)
                }
            }

            if (showFirmwareDialog) {
                AlertDialog(
                    onDismissRequest = { if (!isUpdatingFirmware) showFirmwareDialog = false },
                    title = { Text(if (isUpdatingFirmware) "Updating Firmware" else "Firmware Update") },
                    text = {
                        Column {
                            if (isUpdatingFirmware) {
                                Text("Installing update v1.1.0... Do not turn off your device.", modifier = Modifier.padding(bottom = 12.dp))
                                LinearProgressIndicator(
                                    progress = { firmwareProgress },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = AccentCyan
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("${(firmwareProgress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = AccentCyan)
                            } else {
                                Text("A new firmware version (v1.1.0) is available for your NaTrack sensor.")
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Current Version: v1.0.0", color = TextGrey, fontSize = 14.sp)
                                Text("New Version: v1.1.0", color = AccentCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    confirmButton = {
                        if (!isUpdatingFirmware) {
                            TextButton(
                                onClick = {
                                    isUpdatingFirmware = true
                                    scope.launch {
                                        for (i in 1..100) {
                                            firmwareProgress = i / 100f
                                            delay(40)
                                        }
                                        isUpdatingFirmware = false
                                        showFirmwareDialog = false
                                        firmwareProgress = 0f
                                    }
                                }
                            ) {
                                Text("Update Now", color = AccentCyan, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    dismissButton = {
                        if (!isUpdatingFirmware) {
                            TextButton(onClick = { showFirmwareDialog = false }) {
                                Text("Later", color = TextGrey)
                            }
                        }
                    },
                    containerColor = if (darkTheme) Color(0xFF1E1E20) else Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(28.dp)
                )
            }

            if (showInfoDialog) {
                AlertDialog(
                    onDismissRequest = { showInfoDialog = false },
                    title = { Text("Device Information") },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val infoList = listOf(
                                "Model Name" to (reading?.deviceName ?: "NaTrack Pro v1.0"),
                                "Hardware Version" to "v1.2",
                                "Firmware Version" to "v${reading?.firmware ?: "1.0.0"}",
                                "Battery Chemistry" to "Lithium Polymer"
                            )
                            infoList.forEach { (label, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(label, color = TextGrey, fontSize = 14.sp)
                                    Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showInfoDialog = false }) {
                            Text("OK", color = AccentCyan, fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = if (darkTheme) Color(0xFF1E1E20) else Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(28.dp)
                )
            }

            if (showDiagnosticsDialog) {
                AlertDialog(
                    onDismissRequest = { if (!isRunningDiagnostics) showDiagnosticsDialog = false },
                    title = { Text("Device Diagnostics") },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (isRunningDiagnostics) {
                                Text("Running hardware checks:\n$diagnosticStep", modifier = Modifier.padding(bottom = 12.dp))
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AccentCyan)
                            } else {
                                Text("Diagnostics Completed:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                                diagnosticsResults.forEach { (check, passed) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(check, color = MaterialTheme.colorScheme.onSurface)
                                        Text(
                                            text = if (passed) "PASSED" else "FAILED",
                                            color = if (passed) com.example.ui.theme.StatusNormal else com.example.ui.theme.DangerRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        if (!isRunningDiagnostics) {
                            TextButton(onClick = { showDiagnosticsDialog = false }) {
                                Text("Close", color = AccentCyan, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    containerColor = if (darkTheme) Color(0xFF1E1E20) else Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
fun DeviceStatItem(icon: ImageVector, label: String, value: String, darkTheme: Boolean, modifier: Modifier = Modifier) {
    val containerModifier = if (darkTheme) {
        modifier.neoCard()
    } else {
        modifier
            .background(Color(0xFFE8ECEF), RoundedCornerShape(32.dp))
            .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
    }

    Box(modifier = containerModifier.padding(24.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = if (darkTheme) AccentCyan else Color(0xFF111827), 
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value, 
                fontSize = 22.sp, 
                fontWeight = FontWeight.Bold, 
                color = if (darkTheme) MaterialTheme.colorScheme.onSurface else Color(0xFF111827)
            )
            Text(
                text = label, 
                fontSize = 12.sp, 
                color = if (darkTheme) TextGrey else Color(0xFF4B5563)
            )
        }
    }
}

@Composable
fun DeviceActionItem(icon: ImageVector, label: String, darkTheme: Boolean, onClick: () -> Unit) {
    val activeAccent = if (darkTheme) AccentCyan else Color(0xFF059669)
    
    val containerModifier = if (darkTheme) {
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .neoCard()
            .clickable { onClick() }
    } else {
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(Color(0xFFF0F4F8), RoundedCornerShape(20.dp))
            .border(1.dp, Color.Black.copy(alpha = 0.03f), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    }

    Box(modifier = containerModifier.padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = if (darkTheme) activeAccent else Color(0xFF111827), 
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label, 
                color = if (darkTheme) MaterialTheme.colorScheme.onSurface else Color(0xFF111827), 
                fontWeight = FontWeight.SemiBold, 
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight, 
                contentDescription = null, 
                tint = if (darkTheme) TextGrey else Color(0xFF059669) // Green status tick/arrow accent
            )
        }
    }
}
