package com.example.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.MainViewModel
import com.example.data.Reading
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeoButton
import com.example.ui.theme.NeoTopBar
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.StatusHigh
import com.example.ui.theme.StatusLow
import com.example.ui.theme.StatusNormal
import com.example.ui.theme.neoCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val allReadings by viewModel.historyReadings.collectAsState()
    
    val lowThreshold by viewModel.lowAlertThreshold.collectAsState()
    val highThreshold by viewModel.highAlertThreshold.collectAsState()
    
    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val activeAccent = if (darkTheme) AccentCyan else Color(0xFF059669)
    
    val context = LocalContext.current
    var selectedReading by remember { mutableStateOf<Reading?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    
    val sheetState = rememberModalBottomSheetState()
    
    val filteredReadings: List<Reading> = allReadings.filter { r: Reading -> 
        (searchQuery.isEmpty() || r.naValue.toString().contains(searchQuery) || r.status.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { 
            NeoTopBar(
                title = "History",
                onBackClick = onBack,
                actions = {
                    com.example.ui.theme.NeoIconButton(
                        onClick = {
                            val csvHeader = "Timestamp,NaValue(mmol/L),Temperature(C),Status\n"
                            val csvData = allReadings.joinToString(separator = "\n") { r: Reading ->
                                val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(r.timestamp))
                                "${date},${r.naValue},${r.temperature},${r.status}"
                            }
                            val csv = csvHeader + csvData
                            
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, csv)
                                type = "text/csv"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Export History")
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
                Box(modifier = Modifier.fillMaxWidth().neoCard(RoundedCornerShape(16.dp))) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search readings...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = activeAccent) }
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Filters
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
                                .background(if (selected) activeAccent.copy(alpha = 0.1f) else Color.Transparent),
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
                
                Spacer(modifier = Modifier.height(24.dp))

                if (filteredReadings.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No readings found.", color = com.example.ui.theme.TextGrey)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(filteredReadings) { reading ->
                        val statusColor = when {
                            reading.naValue < lowThreshold -> StatusLow
                            reading.naValue > highThreshold -> StatusHigh
                            else -> StatusNormal
                        }
                        val statusText = when {
                            reading.naValue < lowThreshold -> "LOW"
                            reading.naValue > highThreshold -> "HIGH"
                            else -> "NORMAL"
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
                                        text = "${reading.naValue} mmol/L",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val dateText = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(reading.timestamp))
                                    Text(
                                        text = dateText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = com.example.ui.theme.TextGrey
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
                    .padding(16.dp)
            ) {
                Text("Reading Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                val dateText = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(Date(selectedReading!!.timestamp))
                Text("Time: $dateText")
                Text("Sodium (Na+): ${selectedReading!!.naValue} mmol/L")
                Text("Temperature: ${selectedReading!!.temperature} °C")
                Text("Voltage: ${selectedReading!!.millivolts} mV")
                Text("Status: ${selectedReading!!.status}")
                Text("Calibration E0: ${selectedReading!!.calibrationE0} mV")
                Text("Calibration Slope: ${selectedReading!!.calibrationSlope} mV/decade")
                
                Spacer(modifier = Modifier.height(32.dp))
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
