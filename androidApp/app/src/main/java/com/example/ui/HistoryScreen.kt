package com.example.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.Reading
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val allReadings by viewModel.historyReadings.collectAsState()
    val lowThreshold by viewModel.lowTempThreshold.collectAsState()
    val highThreshold by viewModel.highTempThreshold.collectAsState()
    val preferredUnit by viewModel.preferredUnit.collectAsState()
    
    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val activeAccent = if (darkTheme) AccentCyan else Color(0xFF059669)
    val unitSymbol = if (preferredUnit == "F") "°F" else "°C"
    fun dispTemp(c: Double) = if (preferredUnit == "F") (c * 9.0 / 5.0) + 32.0 else c

    val context = LocalContext.current
    var selectedReading by remember { mutableStateOf<Reading?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    
    val sheetState = rememberModalBottomSheetState()
    
    val filteredReadings: List<Reading> = allReadings.filter { r: Reading -> 
        (searchQuery.isEmpty() || "%.1f".format(dispTemp(r.temperature)).contains(searchQuery) || r.status.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { 
            NeoTopBar(
                title = "Telemetry History",
                onBackClick = onBack,
                actions = {
                    com.example.ui.theme.NeoIconButton(
                        onClick = {
                            val csvHeader = "Timestamp,Temperature_C,Temperature_Disp,Status\n"
                            val csvData = allReadings.joinToString(separator = "\n") { r: Reading ->
                                val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(r.timestamp))
                                "${date},${r.temperature},${"%.2f".format(dispTemp(r.temperature))},${r.status}"
                            }
                            val csv = csvHeader + csvData
                            
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, csv)
                                type = "text/csv"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Export Telemetry CSV")
                            context.startActivity(shareIntent)
                        },
                        icon = {
                            Icon(Icons.Filled.Share, contentDescription = "Export CSV", tint = MaterialTheme.colorScheme.onSurface)
                        },
                        modifier = Modifier.size(48.dp)
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by temperature or status...", color = TextGrey) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = TextGrey) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeAccent,
                        unfocusedBorderColor = if (darkTheme) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Daily", "Weekly", "Monthly").forEach { filter ->
                        val selected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .neoCard(RoundedCornerShape(20.dp))
                                .clickable { selectedFilter = filter }
                                .background(if (selected) activeAccent.copy(alpha = 0.15f) else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter,
                                color = if (selected) activeAccent else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))

                if (filteredReadings.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No telemetry readings recorded yet.", color = TextGrey)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(filteredReadings) { reading ->
                            val isLow = reading.temperature < lowThreshold
                            val isHigh = reading.temperature > highThreshold
                            val statusColor = when {
                                isLow -> StatusLow
                                isHigh -> StatusHigh
                                else -> StatusNormal
                            }
                            val statusText = when {
                                isLow -> "BELOW MIN"
                                isHigh -> "ABOVE MAX"
                                else -> "OPTIMAL"
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .neoCard()
                                    .clickable { selectedReading = reading }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(statusColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "%.1f %s".format(dispTemp(reading.temperature), unitSymbol),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        val dateText = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(Date(reading.timestamp))
                                        Text(
                                            text = dateText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextGrey
                                        )
                                    }
                                    Text(
                                        text = statusText,
                                        color = statusColor,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedReading != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedReading = null },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text("Telemetry Reading Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                val dateText = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(Date(selectedReading!!.timestamp))
                Text("Timestamp: $dateText")
                Text("Temperature (Celsius): ${"%.2f".format(selectedReading!!.temperature)} °C")
                Text("Temperature (Fahrenheit): ${"%.2f".format((selectedReading!!.temperature * 9.0 / 5.0) + 32.0)} °F")
                Text("Sensor Status: ${selectedReading!!.status}")
                Text("Hardware Sensor: Dallas DS18B20 (12-bit)")
                
                Spacer(modifier = Modifier.height(28.dp))
                NeoButton(
                    onClick = { selectedReading = null },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = activeAccent, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
