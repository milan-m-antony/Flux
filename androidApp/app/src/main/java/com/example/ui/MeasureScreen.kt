package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ble.ConnectionState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeoButton
import com.example.ui.theme.NeoTopBar
import com.example.ui.theme.TextGrey
import kotlinx.coroutines.delay

enum class MeasureStep {
    CHECK_CONNECTION,
    PREPARING,
    READING,
    PROCESSING,
    COMPLETE
}

@Composable
fun MeasureScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val connectionState by viewModel.connectionState.collectAsState()
    var currentStep by remember { mutableStateOf(MeasureStep.CHECK_CONNECTION) }
    var resultValue by remember { mutableStateOf(0.0) }
    
    LaunchedEffect(connectionState) {
        if (connectionState == ConnectionState.CONNECTED && currentStep == MeasureStep.CHECK_CONNECTION) {
            currentStep = MeasureStep.PREPARING
        } else if (connectionState != ConnectionState.CONNECTED) {
            currentStep = MeasureStep.CHECK_CONNECTION
        }
    }
    
    LaunchedEffect(currentStep) {
        when (currentStep) {
            MeasureStep.PREPARING -> {
                delay(2000)
                currentStep = MeasureStep.READING
            }
            MeasureStep.READING -> {
                viewModel.takeNewReading()
                delay(3000)
                currentStep = MeasureStep.PROCESSING
            }
            MeasureStep.PROCESSING -> {
                delay(2000)
                currentStep = MeasureStep.COMPLETE
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { NeoTopBar(title = "Measure", onBackClick = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (currentStep) {
                MeasureStep.CHECK_CONNECTION -> {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(80.dp), tint = TextGrey)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Connection Required", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                    Text("Please connect your NaTrack device to start.", color = TextGrey)
                    Spacer(modifier = Modifier.height(48.dp))
                    NeoButton(onClick = onBack) {
                        Text("Go Back", color = AccentCyan)
                    }
                }
                MeasureStep.PREPARING, MeasureStep.READING, MeasureStep.PROCESSING -> {
                    LoadingAnimation()
                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        text = when (currentStep) {
                            MeasureStep.PREPARING -> "Preparing Device..."
                            MeasureStep.READING -> "Reading Sample..."
                            MeasureStep.PROCESSING -> "Processing Data..."
                            else -> ""
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text("Hold still for accurate results", color = TextGrey)
                }
                MeasureStep.COMPLETE -> {
                    val reading by viewModel.latestReading.collectAsState()
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(100.dp), tint = AccentCyan)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Measurement Complete", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    Text(
                        text = "%.1f".format(reading?.na ?: 0.0),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                    )
                    Text("mmol/L", fontSize = 24.sp, color = TextGrey)
                    
                    Spacer(modifier = Modifier.height(64.dp))
                    
                    NeoButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                        Text("Done", color = AccentCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .background(AccentCyan.copy(alpha = 0.2f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(AccentCyan, CircleShape)
        )
    }
}
