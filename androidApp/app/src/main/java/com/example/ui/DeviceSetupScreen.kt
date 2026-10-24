package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.BorderStroke
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.provider.Settings
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ble.BLEPermissionsManager
import com.example.ble.ConnectionState
import com.example.ble.DiscoveredDevice
import com.example.ui.theme.*

@Composable
fun DeviceSetupScreen(
    viewModel: MainViewModel,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val initialStep = if (viewModel.connectionState.value == ConnectionState.CONNECTED) 3 else 1
    var step by remember { mutableStateOf(initialStep) }
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val colors = rememberFluxColors(isDarkTheme)
    val connectionState by viewModel.connectionState.collectAsState()
    val reading by viewModel.latestReading.collectAsState()

    BackHandler {
        if (step > 1) {
            step--
        } else {
            onSkip()
        }
    }

    FluxBackground(darkTheme = isDarkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.cardBg)
                        .border(0.5.dp, colors.cardBorder, CircleShape)
                        .clickable {
                            if (step > 1) step-- else onSkip()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Step Indicator Pills
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in 1..4) {
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (i == step) 24.dp else 8.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (i == step) FluxOrange
                                    else if (i < step) FluxOrange.copy(alpha = 0.5f)
                                    else colors.cardBorder
                                )
                        )
                    }
                }

                Text(
                    text = if (step < 4) "Skip" else "",
                    color = colors.textSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { onSkip() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            // Step Content Switcher
            when (step) {
                1 -> StepOneDiscover(
                    viewModel = viewModel,
                    colors = colors,
                    onStartScan = { step = 2 }
                )
                2 -> StepTwoConnect(
                    viewModel = viewModel,
                    colors = colors,
                    onConnected = { step = 3 }
                )
                3 -> StepThreeProvisionWifi(
                    viewModel = viewModel,
                    colors = colors,
                    onProvisionSent = { step = 4 },
                    onSkipWifi = { onNext() }
                )
                4 -> StepFourVerify(
                    viewModel = viewModel,
                    colors = colors,
                    onComplete = onNext
                )
            }
        }
    }
}

// ==============================================================================
// STEP 1: Discover & Pre-flight Checklist
// ==============================================================================
@Composable
private fun StepOneDiscover(
    viewModel: MainViewModel,
    colors: FluxColors,
    onStartScan: () -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        if (BLEPermissionsManager.hasPermissions(context)) {
            viewModel.startScan()
            onStartScan()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Large Animated Hardware Icon
        Box(
            modifier = Modifier
                .size(130.dp)
                .shadow(12.dp, CircleShape, spotColor = FluxOrange.copy(alpha = 0.3f))
                .clip(CircleShape)
                .background(colors.cardBg)
                .border(2.dp, FluxOrange.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.BluetoothSearching,
                contentDescription = null,
                tint = FluxOrange,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Connect Flux Sensor",
            color = colors.textPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Keep your ESP32 device close to your phone with the Dallas DS18B20 temperature probe connected.",
            color = colors.textSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Pre-check Checklist Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0x10000000))
                .clip(RoundedCornerShape(20.dp))
                .background(colors.cardBg)
                .border(0.5.dp, colors.cardBorder, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ChecklistRow("ESP32 power LED and OLED are ON", colors)
                ChecklistRow("Bluetooth enabled on this phone", colors)
                ChecklistRow("2.4 GHz Wi-Fi credentials ready", colors)
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = {
                if (BLEPermissionsManager.hasPermissions(context)) {
                    viewModel.startScan()
                    onStartScan()
                } else {
                    permissionLauncher.launch(BLEPermissionsManager.permissions.toTypedArray())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FluxOrange,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Search for Devices", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==============================================================================
// STEP 2: BLE Device Selection & Connect
// ==============================================================================
@Composable
private fun StepTwoConnect(
    viewModel: MainViewModel,
    colors: FluxColors,
    onConnected: () -> Unit
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "Radar")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarAlpha"
    )

    LaunchedEffect(Unit) {
        viewModel.startScan()
    }

    LaunchedEffect(connectionState) {
        if (connectionState == ConnectionState.CONNECTED) {
            onConnected()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isScanning || connectionState == ConnectionState.SCANNING) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(FluxOrange.copy(alpha = pulseAlpha))
                )
            }
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(colors.cardBg)
                    .border(1.5.dp, FluxOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = FluxOrange,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (connectionState == ConnectionState.CONNECTING) "Connecting..." else "Available Devices",
            color = colors.textPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Select your Flux peripheral from the list below",
            color = colors.textSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (discoveredDevices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.cardBg)
                    .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            color = FluxOrange,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Scanning for Flux1 / ESP32...",
                            color = colors.textSecondary,
                            fontSize = 14.sp
                        )
                    } else {
                        Text(
                            text = "No Flux devices detected yet.",
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ensure ESP32 is powered on and within range.",
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { viewModel.startScan() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FluxOrange),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FluxOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan Again")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(discoveredDevices) { device ->
                    DeviceDiscoveryCard(
                        device = device,
                        colors = colors,
                        onClick = { viewModel.connectDevice(device) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ==============================================================================
private fun getActiveWifiSsid(context: Context): String? {
    try {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val network = connectivityManager?.activeNetwork
            val capabilities = connectivityManager?.getNetworkCapabilities(network)
            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                val wifiInfo = capabilities.transportInfo as? android.net.wifi.WifiInfo
                val tSsid = wifiInfo?.ssid
                if (!tSsid.isNullOrBlank() && tSsid != "<unknown ssid>" && tSsid != "0x") {
                    return tSsid.removeSurrounding("\"")
                }
            }
        }
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val connectionInfo = wifiManager?.connectionInfo
        if (connectionInfo != null && connectionInfo.networkId != -1) {
            val ssid = connectionInfo.ssid
            if (!ssid.isNullOrBlank() && ssid != "<unknown ssid>" && ssid != "0x") {
                return ssid.removeSurrounding("\"")
            }
        }
    } catch (_: Exception) {}
    return null
}

// ==============================================================================
// STEP 3: Wi-Fi Provisioning
// ==============================================================================
@Composable
private fun StepThreeProvisionWifi(
    viewModel: MainViewModel,
    colors: FluxColors,
    onProvisionSent: () -> Unit,
    onSkipWifi: () -> Unit
) {
    val context = LocalContext.current
    var autoDetectedSsid by remember { mutableStateOf<String?>(null) }
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var turnOffBleAfterWifi by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val refreshWifiDetection: () -> Unit = {
        val detected = getActiveWifiSsid(context)
        if (!detected.isNullOrBlank()) {
            autoDetectedSsid = detected
            ssid = detected
        } else {
            autoDetectedSsid = null
        }
    }

    LaunchedEffect(Unit) {
        refreshWifiDetection()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(colors.cardBg)
                .border(1.5.dp, FluxOrange, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Wifi,
                contentDescription = null,
                tint = FluxOrange,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Connect Device to Wi-Fi",
            color = colors.textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.4).sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Provision 2.4 GHz Wi-Fi credentials to ESP32 for cloud synchronization and remote alerts.",
            color = colors.textSecondary,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Phone Wi-Fi Detection Banner
        if (autoDetectedSsid != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(FluxGreenOnline.copy(alpha = 0.10f))
                    .border(1.dp, FluxGreenOnline.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = FluxGreenOnline,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Detected Phone Network",
                                color = FluxGreenOnline,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = autoDetectedSsid ?: "",
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(onClick = refreshWifiDetection, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.cardBg)
                    .border(0.5.dp, colors.cardBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = FluxOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Auto-detect Phone Wi-Fi",
                            color = colors.textPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Connect phone to 2.4 GHz Wi-Fi to auto-fill, or enter network details manually below.",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = refreshWifiDetection,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = FluxOrange)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Detect Wi-Fi", fontSize = 12.sp, color = colors.textPrimary)
                        }
                        OutlinedButton(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Outlined.Settings, contentDescription = null, modifier = Modifier.size(14.dp), tint = FluxOrange)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Wi-Fi Settings", fontSize = 12.sp, color = colors.textPrimary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Input Fields Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                .clip(RoundedCornerShape(22.dp))
                .background(colors.cardBg)
                .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Wi-Fi SSID
                OutlinedTextField(
                    value = ssid,
                    onValueChange = { ssid = it },
                    label = { Text("Wi-Fi Network Name (SSID)") },
                    placeholder = { Text("e.g. MyHome-2.4G") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Wifi, contentDescription = null, tint = FluxOrange)
                    },
                    trailingIcon = {
                        if (ssid.isNotEmpty()) {
                            IconButton(onClick = { ssid = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = colors.textSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FluxOrange,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedLabelColor = FluxOrange
                    )
                )

                // Wi-Fi Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Wi-Fi Password (leave empty for open)") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = FluxOrange)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = colors.textSecondary
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FluxOrange,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedLabelColor = FluxOrange
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Battery Saving / BLE Option Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.cardBg)
                .border(0.5.dp, colors.cardBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "Turn off BLE after Wi-Fi connects",
                    color = colors.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Conserves ESP32 battery once cloud sync is online.",
                    color = colors.textSecondary,
                    fontSize = 11.5.sp
                )
            }
            Switch(
                checked = turnOffBleAfterWifi,
                onCheckedChange = { turnOffBleAfterWifi = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = FluxOrange
                )
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = {
                if (ssid.isNotBlank()) {
                    viewModel.sendWifiCredentials(ssid.trim(), password)
                    if (turnOffBleAfterWifi) {
                        viewModel.turnOffBle()
                    }
                    onProvisionSent()
                }
            },
            enabled = ssid.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FluxOrange,
                contentColor = Color.White,
                disabledContainerColor = FluxOrange.copy(alpha = 0.35f),
                disabledContentColor = Color.White.copy(alpha = 0.6f)
            )
        ) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (ssid.isNotBlank()) "Connect ESP32 to '${ssid.trim()}'" else "Enter Wi-Fi Network to Connect",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onSkipWifi,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, colors.cardBorder)
        ) {
            Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.textSecondary)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Continue with Bluetooth Only (Skip Wi-Fi)",
                color = colors.textPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ==============================================================================
// STEP 4: Verification & Final Setup Confirmation
// ==============================================================================
@Composable
private fun StepFourVerify(
    viewModel: MainViewModel,
    colors: FluxColors,
    onComplete: () -> Unit
) {
    val reading by viewModel.latestReading.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val syncStatus by viewModel.supabaseSyncStatus.collectAsState()

    val isBleOk = connectionState == ConnectionState.CONNECTED
    val isWifiOk = reading?.wifiConnected == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .shadow(8.dp, CircleShape, spotColor = FluxGreenOnline.copy(alpha = 0.3f))
                .clip(CircleShape)
                .background(colors.cardBg)
                .border(2.dp, if (isBleOk || isWifiOk) FluxGreenOnline else FluxOrange, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isBleOk || isWifiOk) Icons.Default.CheckCircle else Icons.Default.Sync,
                contentDescription = null,
                tint = if (isBleOk || isWifiOk) FluxGreenOnline else FluxOrange,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Verifying Flux Setup",
            color = colors.textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.4).sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Confirming communication links and telemetry pipeline.",
            color = colors.textSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(26.dp))

        // Live Diagnostic Checks Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                .clip(RoundedCornerShape(22.dp))
                .background(colors.cardBg)
                .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                VerifyStatusRow(
                    label = "BLE Peripheral Session",
                    sublabel = if (isBleOk) "Connected to Flux1" else "Disconnected / Idle",
                    isPassed = isBleOk,
                    colors = colors
                )
                VerifyStatusRow(
                    label = "Wi-Fi Station Connection",
                    sublabel = if (isWifiOk) "Connected (${reading?.wifiSsid})" else "Connecting to network...",
                    isPassed = isWifiOk,
                    colors = colors
                )
                VerifyStatusRow(
                    label = "Dallas Temperature Probe",
                    sublabel = if (reading != null) "${reading?.temp}°C live reading" else "Awaiting reading...",
                    isPassed = reading != null && (reading?.temp ?: 0.0) > -50.0,
                    colors = colors
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isBleOk) {
                OutlinedButton(
                    onClick = { viewModel.turnOffBle() },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Icon(Icons.Default.BluetoothDisabled, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.textSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Turn Off BLE", fontSize = 13.sp, color = colors.textPrimary)
                }
            }
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .weight(if (isBleOk) 1.5f else 1f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FluxOrange,
                    contentColor = Color.White
                )
            ) {
                Text("Open Dashboard", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==============================================================================
// Subcomponents
// ==============================================================================
@Composable
private fun ChecklistRow(text: String, colors: FluxColors) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(FluxOrange.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = FluxOrange,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, color = colors.textPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DeviceDiscoveryCard(
    device: DiscoveredDevice,
    colors: FluxColors,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp), spotColor = Color(0x08000000))
            .clip(RoundedCornerShape(18.dp))
            .background(colors.cardBg)
            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(FluxOrange.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = FluxOrange,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = device.name.ifBlank { "Flux Device" },
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = device.address,
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${device.rssi} dBm",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun VerifyStatusRow(
    label: String,
    sublabel: String,
    isPassed: Boolean,
    colors: FluxColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = sublabel, color = colors.textSecondary, fontSize = 12.sp)
        }

        if (isPassed) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = FluxGreenOnline,
                modifier = Modifier.size(22.dp)
            )
        } else {
            CircularProgressIndicator(
                color = FluxOrange,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
