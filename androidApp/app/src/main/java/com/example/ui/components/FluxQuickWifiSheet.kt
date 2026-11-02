package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.MainViewModel
import com.example.ble.ConnectionState
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class WifiProvisionState {
    IDLE,
    CONNECTING,
    SUCCESS,
    FAILED
}

enum class WifiProvisionMode {
    BLE,
    ACCESS_POINT
}

@Composable
fun FluxQuickWifiSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val darkTheme by viewModel.isDarkTheme.collectAsState()
    val colors = rememberFluxColors(darkTheme)
    val bleConnectionState by viewModel.connectionState.collectAsState()
    val isBleConnected = bleConnectionState == ConnectionState.CONNECTED && viewModel.bleManager.isBluetoothEnabled()
    val espWifiConnected by viewModel.espWifiConnected.collectAsState()
    val espWifiRssi by viewModel.espWifiRssi.collectAsState()
    val latestReading by viewModel.latestReading.collectAsState()
    val connectedSsid = latestReading?.wifiSsid ?: ""

    val savedSsid by viewModel.savedWifiSsid.collectAsState()
    val savedPass by viewModel.savedWifiPass.collectAsState()
    val phoneWifiSsid by viewModel.phoneWifiSsid.collectAsState()

    var isLocationServiceOn by remember { mutableStateOf(isLocationServiceEnabled(context)) }

    // Direct initial Wi-Fi detection
    val initialDetectedName = remember {
        val direct = getConnectedWifiName(context)
        when {
            !direct.isNullOrBlank() -> direct
            !phoneWifiSsid.isNullOrBlank() -> phoneWifiSsid
            savedSsid.isNotBlank() -> savedSsid
            connectedSsid.isNotBlank() -> connectedSsid
            else -> null
        }
    }

    var detectedWifiName by remember { mutableStateOf(initialDetectedName) }
    var wifiName by remember { mutableStateOf(initialDetectedName ?: "") }
    var userHasManuallyEdited by remember { mutableStateOf(false) }

    // Update Wi-Fi name when phone detects it or when coordinator StateFlow updates
    LaunchedEffect(phoneWifiSsid) {
        val newSsid = phoneWifiSsid
        if (!newSsid.isNullOrBlank()) {
            detectedWifiName = newSsid
            if (!userHasManuallyEdited || wifiName.isBlank()) {
                wifiName = newSsid
            }
        }
    }

    // Refresh detection whenever app resumes (e.g. returning from enabling Location)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isLocationServiceOn = isLocationServiceEnabled(context)
                val direct = getConnectedWifiName(context) ?: phoneWifiSsid
                if (!direct.isNullOrBlank()) {
                    detectedWifiName = direct
                    if (!userHasManuallyEdited || wifiName.isBlank()) {
                        wifiName = direct
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var password by remember(savedPass) { mutableStateOf(savedPass) }
    var passwordVisible by remember { mutableStateOf(false) }
    var provisionState by remember { mutableStateOf(WifiProvisionState.IDLE) }
    var statusMessage by remember { mutableStateOf("") }
    var setupMode by remember {
        mutableStateOf(if (isBleConnected) WifiProvisionMode.BLE else WifiProvisionMode.ACCESS_POINT)
    }
    var showChangeNetworkForm by remember { mutableStateOf(false) }

    val wifiPulseTransition = rememberInfiniteTransition(label = "wifiPulse")
    val wifiOnlineDotPulse by wifiPulseTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wifiOnlineDotPulse"
    )

    // Synchronize setup mode when BLE connection state changes
    LaunchedEffect(isBleConnected) {
        if (isBleConnected) {
            setupMode = WifiProvisionMode.BLE
        }
    }

    // Pre-fill saved password if network matches remembered SSID
    LaunchedEffect(savedPass, wifiName) {
        if (password.isBlank() && savedPass.isNotBlank() && (wifiName.isBlank() || wifiName == savedSsid)) {
            password = savedPass
        }
    }

    // Monitor for verified ESP32 connection when provision is active
    LaunchedEffect(espWifiConnected, provisionState) {
        if (provisionState == WifiProvisionState.CONNECTING && espWifiConnected) {
            provisionState = WifiProvisionState.SUCCESS
            statusMessage = "Flux is online with '$wifiName'"
            delay(2200)
            onDismiss()
        }
    }

    // Is the ESP32 already connected to a known Wi-Fi?
    val isDeviceAlreadyConnected = isBleConnected && espWifiConnected && connectedSsid.isNotBlank() && !showChangeNetworkForm

    Dialog(
        onDismissRequest = {
            if (provisionState != WifiProvisionState.CONNECTING) onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = provisionState != WifiProvisionState.CONNECTING,
            dismissOnClickOutside = provisionState != WifiProvisionState.CONNECTING
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .imePadding()
                .clickable {
                    if (provisionState != WifiProvisionState.CONNECTING) onDismiss()
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {} // Prevent click-through
                    .shadow(20.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), spotColor = Color(0x35000000))
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(colors.cardBg)
                    .border(
                        0.5.dp,
                        colors.cardBorder,
                        RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 20.dp)
                    .padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Drag Indicator Capsule
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(colors.cardBorder)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    when (provisionState) {
                        WifiProvisionState.IDLE -> {
                            // Header Row: Icon + Title + Close Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(FluxOrangeGradientStart, FluxOrangeGradientEnd)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Wifi,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Wi-Fi Quick Connect",
                                        color = colors.textPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = if (isDeviceAlreadyConnected) "Device is currently online"
                                               else "Connect your Flux node to local Wi-Fi",
                                        color = colors.textSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                IconButton(onClick = onDismiss) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = colors.textSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            if (isDeviceAlreadyConnected) {
                                // 1. ESP32 Already Connected: Show active connection card without asking for password
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (darkTheme) Color(0xFF132E27) else Color(0xFFD1FAE5))
                                        .border(
                                            0.5.dp,
                                            if (darkTheme) Color(0xFF059669) else Color(0xFF10B981),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .padding(16.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .scale(wifiOnlineDotPulse)
                                                    .clip(CircleShape)
                                                    .background(FluxGreenOnline)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Connected to '$connectedSsid'",
                                                color = if (darkTheme) Color(0xFF34D399) else Color(0xFF047857),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (espWifiRssi != null) "Signal: $espWifiRssi dBm • Telemetry upload active"
                                                   else "Cloud telemetry upload active",
                                            color = if (darkTheme) Color(0xFFA7F3D0) else Color(0xFF065F46),
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showChangeNetworkForm = true },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text(
                                            text = "Change Network",
                                            color = colors.textPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Button(
                                        onClick = onDismiss,
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = FluxOrange)
                                    ) {
                                        Text(
                                            text = "Done",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                // 2. Main Simplified Wi-Fi Setup Form: Wi-Fi Name + Password + Connect

                                // Wi-Fi Name field with Detect/Refresh action
                                OutlinedTextField(
                                    value = wifiName,
                                    onValueChange = {
                                        wifiName = it
                                        userHasManuallyEdited = true
                                    },
                                    label = { Text("Wi-Fi name") },
                                    placeholder = { Text("Enter Wi-Fi name") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Wifi,
                                            contentDescription = null,
                                            tint = FluxOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                isLocationServiceOn = isLocationServiceEnabled(context)
                                                val detected = getConnectedWifiName(context) ?: phoneWifiSsid
                                                if (!detected.isNullOrBlank()) {
                                                    wifiName = detected
                                                    detectedWifiName = detected
                                                    userHasManuallyEdited = false
                                                    Toast.makeText(context, "Detected '$detected'", Toast.LENGTH_SHORT).show()
                                                } else if (!isLocationServiceOn) {
                                                    Toast.makeText(context, "Turn on Location to detect Wi-Fi name", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(context, "Could not detect phone Wi-Fi. Enter manually.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Detect phone Wi-Fi",
                                                tint = FluxOrange,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = FluxOrange,
                                        unfocusedBorderColor = colors.cardBorder,
                                        focusedLabelColor = FluxOrange
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Next
                                    )
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Active network verification badge / Location guidance
                                if (wifiName.isNotBlank() && (wifiName == detectedWifiName || wifiName == phoneWifiSsid)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = FluxGreenOnline,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Active phone Wi-Fi: $wifiName",
                                            color = FluxGreenOnline,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                } else if (!isLocationServiceOn && wifiName.isBlank()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (darkTheme) Color(0xFF2E2214) else Color(0xFFFEF3C7))
                                            .border(0.5.dp, if (darkTheme) Color(0xFF78350F) else Color(0xFFF59E0B), RoundedCornerShape(10.dp))
                                            .clickable {
                                                try {
                                                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                                } catch (_: Exception) {}
                                            }
                                            .padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.LocationOff,
                                            contentDescription = null,
                                            tint = if (darkTheme) Color(0xFFFBBF24) else Color(0xFFB45309),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Location is off • Tap to turn on for auto-detection",
                                            color = if (darkTheme) Color(0xFFFBBF24) else Color(0xFF92400E),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                } else if (wifiName.isNotBlank() && userHasManuallyEdited) {
                                    Text(
                                        text = "Manual Wi-Fi network",
                                        color = colors.textSecondary,
                                        fontSize = 11.5.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Password field with Show/Hide toggle
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Password") },
                                    placeholder = { Text("Enter Wi-Fi password") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Lock,
                                            contentDescription = null,
                                            tint = FluxOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                                tint = colors.textSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = FluxOrange,
                                        unfocusedBorderColor = colors.cardBorder,
                                        focusedLabelColor = FluxOrange
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { focusManager.clearFocus() }
                                    )
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Compact Provisioning Method Selector (Secondary AP Option)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(colors.pillBg)
                                        .border(0.5.dp, colors.pillBorder, RoundedCornerShape(14.dp))
                                        .padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // BLE Method Pill
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (setupMode == WifiProvisionMode.BLE) FluxOrange else Color.Transparent)
                                            .clickable { setupMode = WifiProvisionMode.BLE },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Bluetooth,
                                                contentDescription = null,
                                                tint = if (setupMode == WifiProvisionMode.BLE) Color.White else colors.textSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Bluetooth Setup",
                                                color = if (setupMode == WifiProvisionMode.BLE) Color.White else colors.textSecondary,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // SoftAP Method Pill
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (setupMode == WifiProvisionMode.ACCESS_POINT) FluxOrange else Color.Transparent)
                                            .clickable { setupMode = WifiProvisionMode.ACCESS_POINT },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Outlined.WifiTethering,
                                                contentDescription = null,
                                                tint = if (setupMode == WifiProvisionMode.ACCESS_POINT) Color.White else colors.textSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "AP Setup (No BLE)",
                                                color = if (setupMode == WifiProvisionMode.ACCESS_POINT) Color.White else colors.textSecondary,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                if (setupMode == WifiProvisionMode.ACCESS_POINT) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Connect phone to 'Flux-Setup' Wi-Fi, then tap Connect.",
                                        color = FluxOrange,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Connect Action Button
                                Button(
                                    onClick = {
                                        if (wifiName.isNotBlank()) {
                                            focusManager.clearFocus()
                                            provisionState = WifiProvisionState.CONNECTING
                                            statusMessage = "Transmitting credentials to Flux node..."

                                            if (isBleConnected) {
                                                viewModel.sendWifiCredentials(wifiName.trim(), password)
                                            }
                                            if (setupMode == WifiProvisionMode.ACCESS_POINT) {
                                                coroutineScope.launch {
                                                    val result = viewModel.provisionWifiOverAp(wifiName.trim(), password)
                                                    if (result.isFailure && !isBleConnected) {
                                                        statusMessage = "AP connection failed: Ensure phone is connected to 'Flux-Setup'"
                                                    }
                                                }
                                            }

                                            // 25-second verification countdown
                                            coroutineScope.launch {
                                                var secondsLeft = 25
                                                while (secondsLeft > 0 && provisionState == WifiProvisionState.CONNECTING) {
                                                    delay(1000)
                                                    secondsLeft--
                                                    if (secondsLeft <= 20) {
                                                        statusMessage = "Connecting to '${wifiName.trim()}' ($secondsLeft s)..."
                                                    }
                                                }
                                                if (provisionState == WifiProvisionState.CONNECTING && !espWifiConnected) {
                                                    provisionState = WifiProvisionState.FAILED
                                                    statusMessage = "Could not verify connection within 25 seconds. Please check password."
                                                }
                                            }
                                        }
                                    },
                                    enabled = wifiName.isNotBlank() && (setupMode == WifiProvisionMode.ACCESS_POINT || isBleConnected),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = FluxOrange,
                                        contentColor = Color.White,
                                        disabledContainerColor = FluxOrange.copy(alpha = 0.35f),
                                        disabledContentColor = Color.White.copy(alpha = 0.6f)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when {
                                            setupMode == WifiProvisionMode.BLE && !isBleConnected -> "Connect Bluetooth First"
                                            setupMode == WifiProvisionMode.ACCESS_POINT -> "Connect via AP"
                                            else -> "Connect"
                                        },
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        WifiProvisionState.CONNECTING -> {
                            val infiniteTransition = rememberInfiniteTransition(label = "WifiConnectingAnim")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 0.95f,
                                targetValue = 1.15f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(900, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "Pulse"
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .scale(pulseScale)
                                    .shadow(8.dp, CircleShape, spotColor = FluxOrange.copy(alpha = 0.45f))
                                    .clip(CircleShape)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(FluxOrangeGradientStart, FluxOrangeGradientEnd)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "Connecting Flux to Wi-Fi",
                                color = colors.textPrimary,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = statusMessage,
                                color = colors.textSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = FluxOrange,
                                trackColor = colors.cardBorder
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedButton(
                                onClick = { provisionState = WifiProvisionState.IDLE },
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(text = "Cancel", color = colors.textPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        WifiProvisionState.SUCCESS -> {
                            Spacer(modifier = Modifier.height(14.dp))

                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .shadow(8.dp, CircleShape, spotColor = FluxGreenOnline.copy(alpha = 0.4f))
                                    .clip(CircleShape)
                                    .background(if (darkTheme) Color(0xFF132E27) else Color(0xFFD1FAE5))
                                    .border(2.dp, FluxGreenOnline, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = FluxGreenOnline,
                                    modifier = Modifier.size(44.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "Connected Successfully!",
                                color = colors.textPrimary,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Flux node is online with '${wifiName.trim()}'. Cloud telemetry is active.",
                                color = colors.textSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )

                            if (espWifiRssi != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Signal: ${espWifiRssi} dBm",
                                    color = FluxGreenOnline,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FluxGreenOnline)
                            ) {
                                Text(text = "Done", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        WifiProvisionState.FAILED -> {
                            Spacer(modifier = Modifier.height(14.dp))

                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .shadow(8.dp, CircleShape, spotColor = Color(0xFFEF4444).copy(alpha = 0.4f))
                                    .clip(CircleShape)
                                    .background(if (darkTheme) Color(0xFF3B1D1D) else Color(0xFFFEE2E2))
                                    .border(2.dp, Color(0xFFEF4444), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(44.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "Connection Failed",
                                color = colors.textPrimary,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Could not connect to '${wifiName.trim()}'. Please verify the password and ensure your router provides a 2.4 GHz channel.",
                                color = colors.textSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(text = "Cancel", color = colors.textPrimary)
                                }
                                Button(
                                    onClick = { provisionState = WifiProvisionState.IDLE },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = FluxOrange)
                                ) {
                                    Text(text = "Retry", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * Check if Android system location services (GPS/Network) are active.
 */
private fun isLocationServiceEnabled(context: Context): Boolean {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        lm.isLocationEnabled
    } else {
        try {
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE) != Settings.Secure.LOCATION_MODE_OFF
        } catch (_: Exception) { false }
    }
}

/**
 * Safely extract phone's active Wi-Fi name across Android API levels without crashing.
 */
private fun getConnectedWifiName(context: Context): String? {
    try {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        if (wifiManager != null) {
            val info = wifiManager.connectionInfo
            val rawName = info?.ssid
            if (!rawName.isNullOrBlank() && rawName != "<unknown ssid>" && rawName != "0x" && rawName != "\"\"") {
                return rawName.removeSurrounding("\"")
            }
        }

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (cm != null) {
            val activeNetwork = cm.activeNetwork
            if (activeNetwork != null) {
                val capabilities = cm.getNetworkCapabilities(activeNetwork)
                if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    val wifiInfo = capabilities.transportInfo as? android.net.wifi.WifiInfo
                    val name = wifiInfo?.ssid
                    if (!name.isNullOrBlank() && name != "<unknown ssid>" && name != "0x" && name != "\"\"") {
                        return name.removeSurrounding("\"")
                    }
                }
            }

            for (network in cm.allNetworks) {
                val caps = cm.getNetworkCapabilities(network) ?: continue
                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    val wifiInfo = caps.transportInfo as? android.net.wifi.WifiInfo
                    val name = wifiInfo?.ssid
                    if (!name.isNullOrBlank() && name != "<unknown ssid>" && name != "0x" && name != "\"\"") {
                        return name.removeSurrounding("\"")
                    }
                }
            }
        }
    } catch (_: Exception) {}
    return null
}
