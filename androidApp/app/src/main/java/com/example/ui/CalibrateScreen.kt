package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.MainViewModel
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeoButton
import com.example.ui.theme.NeoTopBar
import com.example.ui.theme.neoCard
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrateScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    var p1Conc by remember { mutableStateOf("120") }
    var p1Mv by remember { mutableDoubleStateOf(0.0) }
    
    var p2Conc by remember { mutableStateOf("150") }
    var p2Mv by remember { mutableDoubleStateOf(0.0) }
    
    var finalSlope by remember { mutableDoubleStateOf(0.0) }
    var finalE0 by remember { mutableDoubleStateOf(0.0) }

    val liveMv by viewModel.latestMv.collectAsState()
    val lastCalibration by viewModel.lastCalibration.collectAsState()

    var simulatedMv by remember { mutableDoubleStateOf(48.5) }
    LaunchedEffect(step) {
        val targetMv = if (step == 1) 48.0 else 78.0
        while (true) {
            simulatedMv = targetMv + (Math.random() * 1.6 - 0.8)
            kotlinx.coroutines.delay(800)
        }
    }
    val currentMv = liveMv ?: simulatedMv

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { 
            NeoTopBar(
                title = "Calibration",
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
                    .padding(16.dp)
            ) {
            
            if (lastCalibration != null && step == 1) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).neoCard()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Current Calibration", style = MaterialTheme.typography.titleSmall)
                        Text("E0: ${String.format(Locale.getDefault(), "%.2f", lastCalibration!!.e0)} mV")
                        Text("Slope: ${String.format(Locale.getDefault(), "%.2f", lastCalibration!!.slope)} mV/decade")
                        val dateText = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(lastCalibration!!.timestamp))
                        Text("Last calibrated: $dateText")
                    }
                }
            }

            LinearProgressIndicator(
                progress = { if (step == 1) 0.5f else 1.0f },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (step == 1) {
                Text("Step 1 of 2: LOW standard", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Insert LOW standard strip and wait for stable mV reading.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(modifier = Modifier.fillMaxWidth().neoCard(RoundedCornerShape(16.dp))) {
                    TextField(
                        value = p1Conc,
                        onValueChange = { p1Conc = it },
                        placeholder = { Text("Known Concentration (mmol/L)", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = AccentCyan
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Live Reading: ${String.format(Locale.getDefault(), "%.1f", currentMv)} mV", style = MaterialTheme.typography.titleLarge, color = AccentCyan, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                NeoButton(
                    onClick = {
                        p1Mv = currentMv
                        step = 2
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(
                        "Confirm Point 1",
                        color = AccentCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (step == 2) {
                Text("Step 2 of 2: HIGH standard", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Insert HIGH standard strip and wait for stable mV reading.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(modifier = Modifier.fillMaxWidth().neoCard(RoundedCornerShape(16.dp))) {
                    TextField(
                        value = p2Conc,
                        onValueChange = { p2Conc = it },
                        placeholder = { Text("Known Concentration (mmol/L)", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = AccentCyan
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Live Reading: ${String.format(Locale.getDefault(), "%.1f", currentMv)} mV", style = MaterialTheme.typography.titleLarge, color = AccentCyan, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                NeoButton(
                    onClick = {
                        p2Mv = currentMv
                        val c1 = p1Conc.toDoubleOrNull() ?: 120.0
                        val c2 = p2Conc.toDoubleOrNull() ?: 150.0
                        
                        val denominator = log10(c2) - log10(c1)
                        if (denominator != 0.0) {
                            finalSlope = (p2Mv - p1Mv) / denominator
                            finalE0 = p1Mv - finalSlope * log10(c1)
                        }
                        step = 3
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(
                        "Confirm Point 2",
                        color = AccentCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (step == 3) {
                Text("Calibration Complete", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text("New E0: ${String.format(Locale.getDefault(), "%.2f", finalE0)} mV")
                Text("New Slope: ${String.format(Locale.getDefault(), "%.2f", finalSlope)} mV/decade")
                Spacer(modifier = Modifier.weight(1f))
                NeoButton(
                    onClick = {
                        viewModel.applyCalibration(finalE0, finalSlope)
                        step = 1
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Calibration", color = AccentCyan, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                NeoButton(
                    onClick = { step = 1 },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            }
        }
        }
    }
}
