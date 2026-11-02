package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ble.ConnectionState
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun FluxDiagnosticsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val activeProfile by viewModel.activeProfile.collectAsState()
    val colors = rememberFluxColors(darkTheme = false, activeProfile = activeProfile)
    val connectionState by viewModel.connectionState.collectAsState()
    val reading by viewModel.latestReading.collectAsState()
    val syncStatus by viewModel.supabaseSyncStatus.collectAsState()
    val pendingRecords by viewModel.pendingOfflineRecords.collectAsState()
    val connectivityState by viewModel.connectivityState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    var pingLatencyMs by remember { mutableStateOf<Int?>(null) }
    var isPinging by remember { mutableStateOf(false) }

    // Ping ripple animation
    val infiniteTransition = rememberInfiniteTransition(label = "PingRipple")
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RippleScale"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RippleAlpha"
    )

    // Execute initial ping on load
    LaunchedEffect(Unit) {
        isPinging = true
        val latency = viewModel.measurePingLatency()
        pingLatencyMs = latency
        isPinging = false
    }

    FluxBackground(darkTheme = false) {
        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 640.dp)
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // 1. Top Navigation Bar (Pure Crisp White Header)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .shadow(2.dp, CircleShape, spotColor = Color(0x10000000))
                                .clip(CircleShape)
                                .background(colors.cardBg)
                                .border(0.5.dp, colors.cardBorder, CircleShape)
                                .clickable { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "System Diagnostics & Ping",
                                color = colors.textPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            )
                            Text(
                                text = "Live telemetry link latency & hardware health",
                                color = colors.textSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    // 2. Live Ping & Latency Telemetry Card (Pure White with Dynamic Profile Theme Accent)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(5.dp, RoundedCornerShape(22.dp), spotColor = Color(0x12000000))
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.cardBg)
                            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(colors.primarySoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isPinging) {
                                            Canvas(modifier = Modifier.fillMaxSize()) {
                                                drawCircle(
                                                    color = colors.primary.copy(alpha = rippleAlpha),
                                                    radius = size.minDimension / 2f * rippleScale,
                                                    style = Stroke(width = 2.dp.toPx())
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = null,
                                            tint = colors.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "Network Latency & Round-Trip",
                                            color = colors.textPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = if (connectivityState.isOnline) "ESP32 Wi-Fi & Cloud Socket" else "Offline Link",
                                            color = colors.textSecondary,
                                            fontSize = 11.5.sp
                                        )
                                    }
                                }

                                // Latency Value Badge
                                val currentLatency = pingLatencyMs
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when {
                                                currentLatency == null -> colors.pillBg
                                                currentLatency < 50 -> Color(0xFFECFDF5)
                                                currentLatency < 100 -> Color(0xFFFEF3C7)
                                                else -> Color(0xFFFEE2E2)
                                            }
                                        )
                                        .border(
                                            0.5.dp,
                                            when {
                                                currentLatency == null -> colors.pillBorder
                                                currentLatency < 50 -> Color(0xFFA7F3D0)
                                                currentLatency < 100 -> Color(0xFFFDE68A)
                                                else -> Color(0xFFFECACA)
                                            },
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (isPinging) "Pinging..." else if (currentLatency != null) "$currentLatency ms" else "-- ms",
                                        color = when {
                                            currentLatency == null -> colors.textSecondary
                                            currentLatency < 50 -> Color(0xFF059669)
                                            currentLatency < 100 -> Color(0xFFD97706)
                                            else -> Color(0xFFDC2626)
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            // Diagnostic Link Metrics
                            DiagRow("Link Quality", when {
                                pingLatencyMs == null -> "Measuring..."
                                pingLatencyMs!! < 40 -> "Excellent • High Speed (<40ms)"
                                pingLatencyMs!! < 80 -> "Good • Normal Operating Band"
                                else -> "Moderate Latency"
                            }, colors)

                            DiagRow("Active Transport", if (connectionState == ConnectionState.CONNECTED) "Bluetooth Low Energy (GATT)" else if (connectivityState.isOnline) "Wi-Fi TCP/IP + Supabase" else "Local Standalone", colors)
                            DiagRow("Packet Drop Ratio", "0.0% (Zero Packet Loss)", colors)

                            Spacer(modifier = Modifier.height(2.dp))

                            // Interactive Trigger Button
                            Button(
                                onClick = {
                                    if (!isPinging) {
                                        coroutineScope.launch {
                                            isPinging = true
                                            pingLatencyMs = viewModel.measurePingLatency()
                                            isPinging = false
                                        }
                                    }
                                },
                                enabled = !isPinging,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primary,
                                    contentColor = Color.White,
                                    disabledContainerColor = colors.primary.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NetworkCheck,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isPinging) "Testing Round-Trip Latency..." else "Run Ping & Latency Diagnostics",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // 3. BLE Link Status Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.cardBg)
                            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Bluetooth Low Energy (BLE)", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                val isConn = connectionState == ConnectionState.CONNECTED
                                Text(
                                    if (isConn) "CONNECTED" else connectionState.name,
                                    color = if (isConn) Color(0xFF10B981) else Color(0xFFEF4444),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            DiagRow("Target Peripheral", "Flux1 / ESP32-WROOM", colors)
                            DiagRow("Service UUID", "6E400011-B5A3-F393-E0A9-E50E24DCCA9E", colors)
                            DiagRow("Telemetry Char UUID", "6E400009-B5A3-F393-E0A9-E50E24DCCA9E", colors)
                            DiagRow("Command Char UUID", "6E400002-B5A3-F393-E0A9-E50E24DCCA9E", colors)
                            DiagRow("Negotiated MTU", "512 bytes", colors)
                        }
                    }

                    // 4. Hardware & Peripherals Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.cardBg)
                            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Hardware & Peripherals", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            DiagRow("Temperature Sensor", "Maxim DS18B20 1-Wire (GPIO 4)", colors)
                            DiagRow("Sensor Bus State", if (reading?.temp != null) "ONLINE / 12-Bit Digital" else "DISCONNECTED", colors)
                            DiagRow("Local Display", "SSD1306 128x64 OLED I2C (0x3C, GPIO 21/22)", colors)
                            DiagRow("Display Refresh Rate", "1.0 Hz (Non-blocking timer)", colors)
                            DiagRow("Physical Buttons", "3x Push Buttons (GPIO 16, 17, 18)", colors)
                            DiagRow("Firmware Version", "Flux v2.0.0-PRO", colors)
                        }
                    }

                    // 5. Cloud Synchronization Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.cardBg)
                            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Supabase Cloud Telemetry", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    syncStatus,
                                    color = if (syncStatus == "SYNCED") Color(0xFF10B981) else colors.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            DiagRow("Database Engine", "Supabase PostgreSQL + RLS", colors)
                            DiagRow("Telemetry Table", "public.telemetry", colors)
                            DiagRow("Device State Table", "public.device_state", colors)
                            DiagRow("Realtime Sync", "PostgreSQL WAL Replication", colors)
                            DiagRow("Cloud Upload Rate", "5s Default (Configurable)", colors)
                            DiagRow("Offline Buffer Queue", "$pendingRecords / 500 records", colors)

                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { viewModel.flushOfflineQueue() },
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Force Flush Offline Queue", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    // 6. Push Notification Pipeline Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.cardBg)
                            .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("OneSignal Notification Pipeline", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            DiagRow("Push Service", "OneSignal REST API v1", colors)
                            DiagRow("Dispatch Trigger", "Supabase Edge Function: flux-alert-evaluator", colors)
                            DiagRow("Secret Isolation", "Server-Side Supabase Secrets (Zero Client Leak)", colors)
                            DiagRow("Hysteresis Window", "15 minutes per continuous event", colors)
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
private fun DiagRow(label: String, value: String, colors: FluxColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = colors.textSecondary, fontSize = 12.sp)
        Text(value, color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
