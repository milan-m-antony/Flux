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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
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
fun PrivacySecurityScreen(onBack: () -> Unit) {
    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val activeAccent = if (darkTheme) AccentCyan else Color(0xFF059669)

    var encrytedLogs by remember { mutableStateOf(true) }
    var biometricUnlock by remember { mutableStateOf(false) }
    var analyticsSharing by remember { mutableStateOf(true) }
    var retentionPeriod by remember { mutableStateOf("90 Days") }
    var showPurgeConfirm by remember { mutableStateOf(false) }

    if (showPurgeConfirm) {
        AlertDialog(
            onDismissRequest = { showPurgeConfirm = false },
            title = { Text("Confirm Purging Telemetry?") },
            text = { Text("This will completely delete all local measurement history, sensor values, and cached calibration scores from this device. This operation is irreversible.") },
            confirmButton = {
                TextButton(
                    onClick = { showPurgeConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = com.example.ui.theme.DangerRed)
                ) {
                    Text("Purge All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurgeConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            NeoTopBar(
                title = "Privacy & Security",
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
                text = "Security Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (darkTheme) activeAccent else Color(0xFF111827)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Encryption and Biometrics
            Box(modifier = Modifier.fillMaxWidth().neoCard().padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SecurityToggleRow(
                        icon = Icons.Default.Lock,
                        title = "Local Logs Encryption",
                        subtitle = "Apply military-grade AES-256 local database encryption to all saved sensor files.",
                        checked = encrytedLogs,
                        onCheckedChange = { encrytedLogs = it },
                        activeAccent = activeAccent,
                        darkTheme = darkTheme
                    )
                    Divider(color = if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                    SecurityToggleRow(
                        icon = Icons.Default.Fingerprint,
                        title = "Biometric Lock",
                        subtitle = "Require fingerprint biometric or facial authentication before loading local health journals.",
                        checked = biometricUnlock,
                        onCheckedChange = { biometricUnlock = it },
                        activeAccent = activeAccent,
                        darkTheme = darkTheme
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Data Retention & Telemetry",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (darkTheme) activeAccent else Color(0xFF111827)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Telemetry Sharing & Retention
            Box(modifier = Modifier.fillMaxWidth().neoCard().padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SecurityToggleRow(
                        icon = Icons.Default.Share,
                        title = "Anonymous Diagnostics",
                        subtitle = "Permit Flux to share encrypted Bluetooth signal levels and calibration timeouts to help bug-fix.",
                        checked = analyticsSharing,
                        onCheckedChange = { analyticsSharing = it },
                        activeAccent = activeAccent,
                        darkTheme = darkTheme
                    )
                    Divider(color = if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                    
                    // Retention Selector
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = activeAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Automatic Log Eraser", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Choose how long telemetry points should survive on device storage.",
                            fontSize = 11.sp,
                            color = TextGrey
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Selectables
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("30 Days", "90 Days", "Forever").forEach { option ->
                                val selected = retentionPeriod == option
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .neoCard(RoundedCornerShape(20.dp))
                                        .clickable { retentionPeriod = option }
                                        .background(if (selected) activeAccent.copy(alpha = 0.12f) else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option,
                                        color = if (selected) activeAccent else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Extreme Danger Actions
            Text(
                text = "Danger Zone",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = com.example.ui.theme.DangerRed
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth().neoCard().padding(16.dp)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showPurgeConfirm = true }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = com.example.ui.theme.DangerRed, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Purge Device Database", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                            Text("Erase all local measurement journals, logs, and sensor configurations.", fontSize = 11.sp, color = TextGrey)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SecurityToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    activeAccent: Color,
    darkTheme: Boolean
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
