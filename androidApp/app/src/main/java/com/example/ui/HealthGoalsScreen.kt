package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeoButton
import com.example.ui.theme.NeoTopBar
import com.example.ui.theme.TextGrey
import com.example.ui.theme.neoCard

@Composable
fun HealthGoalsScreen(onBack: () -> Unit) {
    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val activeAccent = if (darkTheme) AccentCyan else Color(0xFF059669)

    // Form States
    var dailyStepsGoal by remember { mutableFloatStateOf(10000f) }
    var dailyCalorieGoal by remember { mutableFloatStateOf(600f) }
    var activeMinutesGoal by remember { mutableFloatStateOf(45f) }
    var sleepHoursGoal by remember { mutableFloatStateOf(8f) }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            NeoTopBar(
                title = "Health Goals",
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
            Text(
                text = "Target Thresholds",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (darkTheme) activeAccent else Color(0xFF111827)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Step Goal Slider
            Box(modifier = Modifier.fillMaxWidth().neoCard().padding(20.dp)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = activeAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Daily Steps Target", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text("${dailyStepsGoal.toInt()} steps", fontWeight = FontWeight.Bold, color = activeAccent, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Slider(
                        value = dailyStepsGoal,
                        onValueChange = { dailyStepsGoal = it },
                        valueRange = 2000f..20000f,
                        steps = 18,
                        colors = SliderDefaults.colors(thumbColor = activeAccent, activeTrackColor = activeAccent)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calories Goal Slider
            Box(modifier = Modifier.fillMaxWidth().neoCard().padding(20.dp)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = activeAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Daily Calorie Target", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text("${dailyCalorieGoal.toInt()} kcal", fontWeight = FontWeight.Bold, color = activeAccent, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Slider(
                        value = dailyCalorieGoal,
                        onValueChange = { dailyCalorieGoal = it },
                        valueRange = 200f..1500f,
                        steps = 13,
                        colors = SliderDefaults.colors(thumbColor = activeAccent, activeTrackColor = activeAccent)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active Minutes Goal Slider
            Box(modifier = Modifier.fillMaxWidth().neoCard().padding(20.dp)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.QueryBuilder, contentDescription = null, tint = activeAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Active Minutes", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text("${activeMinutesGoal.toInt()} mins", fontWeight = FontWeight.Bold, color = activeAccent, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Slider(
                        value = activeMinutesGoal,
                        onValueChange = { activeMinutesGoal = it },
                        valueRange = 15f..120f,
                        steps = 7,
                        colors = SliderDefaults.colors(thumbColor = activeAccent, activeTrackColor = activeAccent)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sleep Hours Goal Slider
            Box(modifier = Modifier.fillMaxWidth().neoCard().padding(20.dp)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Scale, contentDescription = null, tint = activeAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Target Sleep", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(String.format("%.1f hrs", sleepHoursGoal), fontWeight = FontWeight.Bold, color = activeAccent, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Slider(
                        value = sleepHoursGoal,
                        onValueChange = { sleepHoursGoal = it },
                        valueRange = 4f..12f,
                        steps = 16,
                        colors = SliderDefaults.colors(thumbColor = activeAccent, activeTrackColor = activeAccent)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (showSuccessMessage) {
                Text(
                    text = "Goals updated successfully!",
                    color = activeAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            NeoButton(
                onClick = {
                    isSaving = true
                    showSuccessMessage = true
                    isSaving = false
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(
                    text = "Save Health Goals",
                    color = if (darkTheme) Color.White else Color(0xFF111827),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
