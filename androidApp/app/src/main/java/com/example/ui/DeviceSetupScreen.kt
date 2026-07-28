package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import com.example.MainViewModel
import com.example.ble.BLEPermissionsManager
import com.example.ble.ConnectionState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeoButton
import com.example.ui.theme.TextGrey
import com.example.ui.theme.neoCard

@Composable
fun DeviceSetupScreen(viewModel: MainViewModel, onNext: () -> Unit, onSkip: () -> Unit) {
    var step by remember { mutableStateOf(1) }
    
    BackHandler {
        onSkip()
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            if (step == 1) {
                SetupStepOne(
                    onStartSearch = { 
                        step = 2
                    },
                    viewModel = viewModel
                )
            } else {
                SetupStepTwo(
                    viewModel = viewModel,
                    onConnected = onNext
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                "Do it later", 
                color = AccentCyan, 
                fontWeight = FontWeight.Bold, 
                modifier = Modifier
                    .clickable { onSkip() }
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun SetupStepOne(onStartSearch: () -> Unit, viewModel: MainViewModel) {
    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val context = LocalContext.current
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            viewModel.startScan()
            onStartSearch()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(
                    brush = if (darkTheme) {
                        Brush.linearGradient(colors = listOf(Color(0xFF2B2B2D), Color(0xFF1E1E20)))
                    } else {
                        Brush.linearGradient(colors = listOf(Color(0xFFFFFFFF), Color(0xFFF2F2F7)))
                    },
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = if (darkTheme) {
                            listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.02f))
                        } else {
                            listOf(Color.Black.copy(alpha = 0.05f), Color.Transparent)
                        }
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Bluetooth, 
                contentDescription = null, 
                modifier = Modifier.size(80.dp), 
                tint = AccentCyan
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            "Connect Your Device",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Turn on your NaTrack device and keep it close to your phone to begin setup.",
            color = TextGrey,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(64.dp))
        
        NeoButton(
            onClick = {
                if (BLEPermissionsManager.hasPermissions(context)) {
                    viewModel.startScan()
                    onStartSearch()
                } else {
                    permissionLauncher.launch(BLEPermissionsManager.permissions.toTypedArray())
                }
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Search Device", color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}

@Composable
fun SetupStepTwo(viewModel: MainViewModel, onConnected: () -> Unit) {
    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val connectionState by viewModel.connectionState.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()

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

    LaunchedEffect(connectionState) {
        if (connectionState == ConnectionState.CONNECTED) {
            onConnected()
        }
    }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = if (darkTheme) {
                        Brush.linearGradient(colors = listOf(Color(0xFF2B2B2D), Color(0xFF1E1E20)))
                    } else {
                        Brush.linearGradient(colors = listOf(Color(0xFFFFFFFF), Color(0xFFF2F2F7)))
                    },
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = if (darkTheme) {
                            listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.02f))
                        } else {
                            listOf(Color.Black.copy(alpha = 0.05f), Color.Transparent)
                        }
                    ),
                    shape = CircleShape
                ),
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
                CircularProgressIndicator(color = AccentCyan, strokeWidth = 6.dp, modifier = Modifier.size(60.dp))
            } else if (connectionState == ConnectionState.CONNECTING) {
                CircularProgressIndicator(color = AccentCyan, strokeWidth = 6.dp, modifier = Modifier.size(60.dp))
            } else {
                Icon(
                    Icons.Default.CheckCircle, 
                    contentDescription = null, 
                    modifier = Modifier.size(60.dp), 
                    tint = com.example.ui.theme.StatusNormal
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            if (connectionState == ConnectionState.SCANNING) "Scanning..." 
            else if (connectionState == ConnectionState.CONNECTING) "Connecting..."
            else "Available Devices",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .neoCard(RoundedCornerShape(24.dp)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                            Icon(Icons.Default.Bluetooth, contentDescription = null, tint = AccentCyan)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = device.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "Signal: ${device.rssi} dBm", fontSize = 12.sp, color = TextGrey)
                        }
                    }
                }
            }
        }
    }
}
