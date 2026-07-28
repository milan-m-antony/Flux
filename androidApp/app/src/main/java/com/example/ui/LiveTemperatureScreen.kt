package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ble.BLEState
import com.example.ble.BleTemperatureViewModel
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeoButton
import com.example.ui.theme.NeoTopBar
import com.example.ui.theme.TextGrey
import com.example.ui.theme.neoCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LiveTemperatureScreen(
    viewModel: BleTemperatureViewModel,
    onBack: () -> Unit
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val deviceName by viewModel.deviceName.collectAsState()
    val rssi by viewModel.rssi.collectAsState()
    val liveTemp by viewModel.liveTemperature.collectAsState()
    val lastUpdate by viewModel.lastUpdateTime.collectAsState()
    val history by viewModel.temperatureHistory.collectAsState()

    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val activeAccent = if (darkTheme) AccentCyan else Color(0xFF059669)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(connectionState) {
        if (connectionState == BLEState.FAILED) {
            snackbarHostState.showSnackbar("BLE connection failed.")
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { NeoTopBar(title = "Live Temperature", onBackClick = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            // Connection Card
            ConnectionCard(
                connectionState = connectionState,
                deviceName = deviceName.ifEmpty { "NaTrack" },
                rssi = rssi,
                activeAccent = activeAccent
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Large Temperature Card
            TemperatureCard(
                liveTemp = liveTemp,
                lastUpdate = lastUpdate,
                activeAccent = activeAccent
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Actions
            ActionButtons(
                connectionState = connectionState,
                onConnect = { viewModel.reconnect() }, // assuming autoReconnect logic tries saved device
                onDisconnect = { viewModel.disconnect() },
                onReconnect = { viewModel.reconnect() },
                activeAccent = activeAccent
            )

            Spacer(modifier = Modifier.height(24.dp))

            // History List
            Text(
                text = "History (Last 20 Readings)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (history.isEmpty()) {
                    item {
                        Text(
                            "Waiting for data...",
                            color = TextGrey,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                } else {
                    items(history) { record ->
                        HistoryItem(record = record, activeAccent = activeAccent)
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionCard(connectionState: BLEState, deviceName: String, rssi: Int?, activeAccent: Color) {
    Box(modifier = Modifier.fillMaxWidth().neoCard().padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (connectionState == BLEState.CONNECTED) Icons.Default.BluetoothConnected else Icons.Default.BluetoothDisabled,
                    contentDescription = null,
                    tint = if (connectionState == BLEState.CONNECTED) activeAccent else TextGrey,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = deviceName,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    )
                    Text(
                        text = connectionState.name,
                        color = if (connectionState == BLEState.CONNECTED) activeAccent else TextGrey,
                        fontSize = 14.sp
                    )
                }
            }
            
            if (connectionState == BLEState.CONNECTED) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SignalCellularAlt,
                        contentDescription = "Signal",
                        tint = activeAccent
                    )
                    Text(
                        text = "${rssi ?: "-"} dBm",
                        color = TextGrey,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TemperatureCard(liveTemp: Float?, lastUpdate: Long?, activeAccent: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neoCard()
            .padding(32.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Thermostat,
                contentDescription = null,
                tint = activeAccent,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (liveTemp != null) {
                AnimatedContent(
                    targetState = liveTemp,
                    transitionSpec = {
                        (slideInVertically(animationSpec = tween(400)) { height -> height } + fadeIn(animationSpec = tween(400))).togetherWith(
                            slideOutVertically(animationSpec = tween(400)) { height -> -height } + fadeOut(animationSpec = tween(400))
                        )
                    }, label = "tempAnimation"
                ) { targetTemp ->
                    Text(
                        text = String.format(Locale.US, "%.1f°C", targetTemp),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                val formatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
                Text(
                    text = "Last updated: ${formatter.format(Date(lastUpdate ?: 0))}",
                    color = TextGrey,
                    fontSize = 14.sp
                )
            } else {
                Text(
                    text = "--.-°C",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGrey
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Waiting for data...",
                    color = TextGrey,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ActionButtons(
    connectionState: BLEState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onReconnect: () -> Unit,
    activeAccent: Color
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        if (connectionState == BLEState.CONNECTED) {
            NeoButton(
                onClick = onDisconnect,
                modifier = Modifier.weight(1f).height(50.dp),
            ) {
                Text("Disconnect", color = com.example.ui.theme.DangerRed, fontWeight = FontWeight.Bold)
            }
        } else {
            NeoButton(
                onClick = onConnect,
                modifier = Modifier.weight(1f).height(50.dp),
            ) {
                Text("Connect", color = activeAccent, fontWeight = FontWeight.Bold)
            }
        }
        
        if (connectionState != BLEState.CONNECTED) {
            NeoButton(
                onClick = onReconnect,
                modifier = Modifier.weight(1f).height(50.dp),
            ) {
                Text("Reconnect", color = activeAccent, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HistoryItem(record: com.example.ble.TemperatureRecord, activeAccent: Color) {
    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val formatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (darkTheme) Color(0xFF2A2A2A) else Color(0xFFF3F4F6))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = String.format(Locale.US, "%.1f°C", record.temperature),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
            Text(
                text = formatter.format(Date(record.timestamp)),
                color = TextGrey,
                fontSize = 14.sp
            )
        }
    }
}
