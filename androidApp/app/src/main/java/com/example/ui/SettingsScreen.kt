package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.MainViewModel
import com.example.ble.ConnectionState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeoButton
import com.example.ui.theme.NeoTopBar
import com.example.ui.theme.neoCard
import com.example.ui.theme.DangerRed
import com.example.ui.theme.TextGrey
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val lowThreshold by viewModel.lowAlertThreshold.collectAsState()
    val highThreshold by viewModel.highAlertThreshold.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()

    var lowSlider by remember(lowThreshold) { mutableFloatStateOf(lowThreshold.toFloat()) }
    var highSlider by remember(highThreshold) { mutableFloatStateOf(highThreshold.toFloat()) }
    
    val darkTheme = MaterialTheme.colorScheme.background == androidx.compose.ui.graphics.Color(0xFF000000)
    val activeAccent = if (darkTheme) AccentCyan else androidx.compose.ui.graphics.Color(0xFF059669)

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = { 
            NeoTopBar(
                title = "Settings",
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
            Text("Alert Thresholds", style = MaterialTheme.typography.titleMedium, color = activeAccent)
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(modifier = Modifier.fillMaxWidth().neoCard().padding(16.dp)) {
                Column {
                    Text("Low Alert: ${lowSlider.toInt()} mmol/L", color = MaterialTheme.colorScheme.onSurface)
                    Slider(
                        value = lowSlider,
                        onValueChange = { lowSlider = it },
                        onValueChangeFinished = { viewModel.saveSettings(lowSlider.toDouble(), highSlider.toDouble()) },
                        valueRange = 100f..135f,
                        colors = SliderDefaults.colors(thumbColor = activeAccent, activeTrackColor = activeAccent)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("High Alert: ${highSlider.toInt()} mmol/L", color = MaterialTheme.colorScheme.onSurface)
                    Slider(
                        value = highSlider,
                        onValueChange = { highSlider = it },
                        onValueChangeFinished = { viewModel.saveSettings(lowSlider.toDouble(), highSlider.toDouble()) },
                        valueRange = 145f..180f,
                        colors = SliderDefaults.colors(thumbColor = activeAccent, activeTrackColor = activeAccent)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Device", style = MaterialTheme.typography.titleMedium, color = activeAccent)
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(modifier = Modifier.fillMaxWidth().neoCard().padding(16.dp)) {
                if (connectionState == ConnectionState.CONNECTED) {
                    NeoButton(
                        onClick = { viewModel.disconnectDevice() },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Disconnect & Forget Device", color = DangerRed, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Column {
                        Text("No device connected.", color = TextGrey)
                        Spacer(modifier = Modifier.height(16.dp))
                        NeoButton(
                            onClick = { viewModel.disconnectDevice() },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text("Forget Saved Device", color = activeAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Security", style = MaterialTheme.typography.titleMedium, color = activeAccent)
            Spacer(modifier = Modifier.height(16.dp))
            
            val context = androidx.compose.ui.platform.LocalContext.current
            var appLockEnabled by remember { mutableStateOf(com.example.util.BiometricHelper.isAppLockEnabled(context)) }
            var showBiometricError by remember { mutableStateOf<String?>(null) }
            
            Box(modifier = Modifier.fillMaxWidth().neoCard().padding(16.dp)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("App Lock (Fingerprint)", color = MaterialTheme.colorScheme.onSurface)
                        Switch(
                            checked = appLockEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    if (com.example.util.BiometricHelper.isBiometricAvailable(context)) {
                                        com.example.util.BiometricHelper.setAppLockEnabled(context, true)
                                        appLockEnabled = true
                                        showBiometricError = null
                                    } else {
                                        showBiometricError = "Biometric authentication is not available or not set up on this device."
                                    }
                                } else {
                                    com.example.util.BiometricHelper.setAppLockEnabled(context, false)
                                    appLockEnabled = false
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = activeAccent,
                                checkedTrackColor = activeAccent.copy(alpha = 0.5f)
                            )
                        )
                    }
                    
                    if (showBiometricError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(showBiometricError!!, color = DangerRed, style = MaterialTheme.typography.bodySmall)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Require fingerprint authentication when opening the app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text("About", style = MaterialTheme.typography.titleMedium, color = activeAccent)
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().neoCard().padding(16.dp)) {
                Column {
                    Text("NaTrack v1.0", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Disclaimer: NaTrack is a prototype device for educational and research purposes. Not FDA/CDSCO approved. Not for clinical diagnosis. Consult a medical professional for health decisions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGrey
                    )
                }
            }
        }
        }
    }
}
