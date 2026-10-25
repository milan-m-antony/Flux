package com.example.ui.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
    val isBleConnected = bleConnectionState == ConnectionState.CONNECTED
    val espWifiConnected by viewModel.espWifiConnected.collectAsState()
    val espWifiRssi by viewModel.espWifiRssi.collectAsState()

    val savedSsid by viewModel.savedWifiSsid.collectAsState()
    val savedPass by viewModel.savedWifiPass.collectAsState()

    // Automatically detect phone's connected Wi-Fi SSID
    val detectedSsid = remember { getConnectedWifiSsid(context) }

    var ssid by remember(detectedSsid, savedSsid) {
        mutableStateOf(
            if (!detectedSsid.isNullOrBlank()) detectedSsid
            else if (savedSsid.isNotBlank()) savedSsid
            else ""
        )
    }
    var password by remember(savedPass) {
        mutableStateOf(savedPass)
    }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberCredentials by remember { mutableStateOf(true) }
    var provisionState by remember { mutableStateOf(WifiProvisionState.IDLE) }
    var statusMessage by remember { mutableStateOf("") }

    // Listen to real-time ESP32 Wi-Fi state when provision in progress
    LaunchedEffect(espWifiConnected, provisionState) {
        if (provisionState == WifiProvisionState.CONNECTING && espWifiConnected) {
            provisionState = WifiProvisionState.SUCCESS
            statusMessage = "ESP32 is Online!"
            delay(2200)
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = {
            if (provisionState != WifiProvisionState.CONNECTING) {
                onDismiss()
            }
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
                .clickable {
                    if (provisionState != WifiProvisionState.CONNECTING) onDismiss()
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {} // Prevent click-through
                    .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), spotColor = Color(0x30000000))
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(colors.cardBg)
                    .border(
                        0.5.dp,
                        colors.cardBorder,
                        RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
                    .padding(horizontal = 22.dp, vertical = 20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
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
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
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
                                        text = "1-step automated setup for your Flux node",
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

                            // Auto-detected Network Pill
                            if (!detectedSsid.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (darkTheme) Color(0xFF132E27) else Color(0xFFD1FAE5))
                                        .border(
                                            0.5.dp,
                                            if (darkTheme) Color(0xFF059669) else Color(0xFF10B981),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(FluxGreenOnline)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Phone Network: '$detectedSsid' (Auto-filled)",
                                        color = if (darkTheme) Color(0xFF34D399) else Color(0xFF047857),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                            }

                            // BLE Link Status Pill
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isBleConnected) {
                                            if (darkTheme) Color(0xFF1E293B) else Color(0xFFEFF6FF)
                                        } else {
                                            if (darkTheme) Color(0xFF2A1C1C) else Color(0xFFFEE2E2)
                                        }
                                    )
                                    .border(
                                        0.5.dp,
                                        if (isBleConnected) FluxOrange.copy(alpha = 0.35f) else Color(0xFFEF4444).copy(alpha = 0.5f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isBleConnected) Icons.Default.BluetoothConnected else Icons.Default.BluetoothDisabled,
                                    contentDescription = null,
                                    tint = if (isBleConnected) FluxOrange else Color(0xFFEF4444),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBleConnected) "Linked to Flux node via BLE • Ready to provision"
                                           else "Flux not connected via BLE. Reconnect Bluetooth first.",
                                    color = if (isBleConnected) colors.textPrimary else Color(0xFFEF4444),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Network SSID Input
                            OutlinedTextField(
                                value = ssid,
                                onValueChange = { ssid = it },
                                label = { Text("Wi-Fi Network Name (SSID)") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Wifi,
                                        contentDescription = null,
                                        tint = FluxOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
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

                            Spacer(modifier = Modifier.height(12.dp))

                            // Password Input
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Wi-Fi Password") },
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

                            Spacer(modifier = Modifier.height(10.dp))

                            // Remember Credentials Checkbox Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { rememberCredentials = !rememberCredentials }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = rememberCredentials,
                                    onCheckedChange = { rememberCredentials = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = FluxOrange
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Remember credentials for future automatic connects",
                                    color = colors.textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // 1-Tap Connect Action Button
                            Button(
                                onClick = {
                                    if (ssid.isNotBlank()) {
                                        focusManager.clearFocus()
                                        provisionState = WifiProvisionState.CONNECTING
                                        statusMessage = "Transmitting credentials to Flux node..."
                                        viewModel.sendWifiCredentials(ssid.trim(), password)

                                        // Start a 15-second verification timeout
                                        coroutineScope.launch {
                                            var secondsLeft = 15
                                            while (secondsLeft > 0 && provisionState == WifiProvisionState.CONNECTING) {
                                                delay(1000)
                                                secondsLeft--
                                                if (secondsLeft <= 10) {
                                                    statusMessage = "Connecting to '${ssid.trim()}' ($secondsLeft s)..."
                                                }
                                            }
                                            if (provisionState == WifiProvisionState.CONNECTING && !espWifiConnected) {
                                                provisionState = WifiProvisionState.FAILED
                                                statusMessage = "Could not verify connection within 15 seconds."
                                            }
                                        }
                                    }
                                },
                                enabled = isBleConnected && ssid.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
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
                                    text = if (!isBleConnected) "Connect Bluetooth First"
                                           else if (ssid.isNotBlank()) "Connect ESP32 to '${ssid.trim()}'"
                                           else "Enter Wi-Fi Network Name",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
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
                                    .size(80.dp)
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
                                    modifier = Modifier.size(38.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Connecting Flux to Wi-Fi",
                                color = colors.textPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = statusMessage,
                                color = colors.textSecondary,
                                fontSize = 13.5.sp,
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

                            Spacer(modifier = Modifier.height(24.dp))

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
                                    .size(80.dp)
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
                                    modifier = Modifier.size(46.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "Connected Successfully!",
                                color = colors.textPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Flux node is online with '${ssid.trim()}'. Cloud telemetry upload is active.",
                                color = colors.textSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )

                            if (espWifiRssi != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Signal: ${espWifiRssi} dBm",
                                    color = FluxGreenOnline,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(22.dp))

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
                                    .size(80.dp)
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
                                    modifier = Modifier.size(46.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "Connection Failed",
                                color = colors.textPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "ESP32 could not connect to '${ssid.trim()}'. Please verify the password and ensure your router provides a 2.4 GHz channel.",
                                color = colors.textSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(22.dp))

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

// Helper to safely extract current phone Wi-Fi SSID
private fun getConnectedWifiSsid(context: Context): String? {
    try {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return null
        val info = wifiManager.connectionInfo ?: return null
        val rawSsid = info.ssid
        if (!rawSsid.isNullOrBlank() && rawSsid != "<unknown ssid>" && rawSsid != "0x") {
            return rawSsid.removeSurrounding("\"")
        }
    } catch (_: Exception) {}
    return null
}
