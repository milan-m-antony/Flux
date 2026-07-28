package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeoButton
import com.example.ui.theme.NeoTopBar
import com.example.ui.theme.TextGrey
import com.example.ui.theme.neoCard

@Composable
fun WaterIntakeScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val activeAccent = if (darkTheme) AccentCyan else Color(0xFF059669)

    val currentIntake by viewModel.waterIntakeMl.collectAsStateWithLifecycle()
    val dailyTargetIntake by viewModel.waterGoalMl.collectAsStateWithLifecycle()
    val dailyTarget = dailyTargetIntake.toFloat()
    
    val percentage = (currentIntake.toFloat() / dailyTarget).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 800),
        label = "WaterProgress"
    )

    var reminderMorning by remember { mutableStateOf(true) }
    var reminderAfternoon by remember { mutableStateOf(true) }
    var reminderEvening by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            NeoTopBar(
                title = "Water Intake Goal",
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Interactive Progress Gauge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoCard()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Today's Progress", fontSize = 14.sp, color = TextGrey)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier.size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Circular track
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f),
                            strokeWidth = 14.dp
                        )
                        // Circular active
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxSize(),
                            color = activeAccent,
                            strokeWidth = 14.dp,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.LocalDrink,
                                contentDescription = null,
                                tint = activeAccent,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$currentIntake ml",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Target ${dailyTarget.toInt()} ml",
                                fontSize = 12.sp,
                                color = TextGrey
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action add intake selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NeoButton(
                    onClick = { viewModel.updateWaterIntake((currentIntake + 250).coerceIn(0, 8000)) },
                    modifier = Modifier.weight(1f).height(64.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("250 ml", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                NeoButton(
                    onClick = { viewModel.updateWaterIntake((currentIntake + 500).coerceIn(0, 8000)) },
                    modifier = Modifier.weight(1f).height(64.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("500 ml", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Adjust Slider Row
            var sliderValue by remember(dailyTargetIntake) { mutableFloatStateOf(dailyTargetIntake.toFloat()) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoCard()
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Adjust Goal Target", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("${sliderValue.toInt()} ml", fontWeight = FontWeight.Bold, color = activeAccent)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        onValueChangeFinished = { viewModel.updateWaterGoal(sliderValue.toInt()) },
                        valueRange = 1000f..5000f,
                        steps = 7,
                        colors = SliderDefaults.colors(thumbColor = activeAccent, activeTrackColor = activeAccent)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Water Intake Reminders
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = activeAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Intake Reminders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (darkTheme) activeAccent else Color(0xFF111827)
                )
            }

            Box(modifier = Modifier.fillMaxWidth().neoCard().padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReminderRow(
                        label = "Morning Reminders (08:00 - 12:00)",
                        checked = reminderMorning,
                        onCheckedChange = { reminderMorning = it },
                        activeAccent = activeAccent
                    )
                    Divider(color = if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                    ReminderRow(
                        label = "Afternoon Reminders (12:00 - 17:00)",
                        checked = reminderAfternoon,
                        onCheckedChange = { reminderAfternoon = it },
                        activeAccent = activeAccent
                    )
                    Divider(color = if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                    ReminderRow(
                        label = "Evening Reminders (17:00 - 22:00)",
                        checked = reminderEvening,
                        onCheckedChange = { reminderEvening = it },
                        activeAccent = activeAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            NeoButton(
                onClick = { viewModel.updateWaterIntake(0) },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Reset Progress", color = com.example.ui.theme.DangerRed, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ReminderRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, activeAccent: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = activeAccent,
                checkedTrackColor = activeAccent.copy(alpha = 0.5f)
            )
        )
    }
}
