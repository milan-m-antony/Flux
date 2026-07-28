package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeoButton
import com.example.ui.theme.NeoTopBar
import com.example.ui.theme.TextGrey
import com.example.ui.theme.neoCard

@Composable
fun DoctorSharingScreen(onBack: () -> Unit) {
    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val activeAccent = if (darkTheme) AccentCyan else Color(0xFF059669)

    var realTimeSync by remember { mutableStateOf(true) }
    var emailReports by remember { mutableStateOf(false) }
    var alertOnSpike by remember { mutableStateOf(true) }
    
    var showExportSuccess by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            NeoTopBar(
                title = "Doctor Sharing",
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
            // Connected Physician Card
            Text(
                text = "Primary Physician",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (darkTheme) activeAccent else Color(0xFF111827)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth().neoCard().padding(20.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(activeAccent.copy(alpha = 0.1f), CircleShape)
                                .border(1.dp, activeAccent.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MedicalServices, contentDescription = null, tint = activeAccent, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dr. Elizabeth Vance",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "St. Jude Biomedical Analytics",
                                fontSize = 12.sp,
                                color = TextGrey
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(activeAccent.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = activeAccent, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Linked", color = activeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sharing Controls
            Text(
                text = "Sharing Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (darkTheme) activeAccent else Color(0xFF111827)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth().neoCard().padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DoctorSharingToggle(
                        icon = Icons.Default.VerifiedUser,
                        title = "Real-Time Telemetry Sync",
                        subtitle = "Transmit telemetry recordings securely to Dr. Vance's workspace whenever active.",
                        checked = realTimeSync,
                        onCheckedChange = { realTimeSync = it },
                        activeAccent = activeAccent
                    )
                    Divider(color = if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                    DoctorSharingToggle(
                        icon = Icons.Default.Email,
                        title = "Monthly Report Dispatch",
                        subtitle = "Email a structured PDF report containing average measurements to the clinic automatically.",
                        checked = emailReports,
                        onCheckedChange = { emailReports = it },
                        activeAccent = activeAccent
                    )
                    Divider(color = if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                    DoctorSharingToggle(
                        icon = Icons.Default.Share,
                        title = "Critical Alert Redirection",
                        subtitle = "Alert the clinic triage unit immediately if sodium temperature reading spikes above threshold.",
                        checked = alertOnSpike,
                        onCheckedChange = { alertOnSpike = it },
                        activeAccent = activeAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Manual Export Buttons
            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (darkTheme) activeAccent else Color(0xFF111827)
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (showExportSuccess) {
                Text(
                    text = "Encrypted ZIP package shared successfully with Dr. Vance!",
                    color = activeAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 12.dp)
                )
            }

            NeoButton(
                onClick = { showExportSuccess = true },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Compile & Share Log Package", color = if (darkTheme) Color.White else Color(0xFF111827), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoCard()
                    .clickable { /* Add new provider action */ }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = activeAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Another Care Provider", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun DoctorSharingToggle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    activeAccent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = activeAccent,
            modifier = Modifier.size(22.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextGrey,
                lineHeight = 15.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
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
