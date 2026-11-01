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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun PrivacySecurityScreen(onBack: () -> Unit) {
    val colors = rememberFluxColors(false)

    var encrytedLogs by remember { mutableStateOf(true) }
    var biometricUnlock by remember { mutableStateOf(false) }
    var analyticsSharing by remember { mutableStateOf(true) }
    var showPurgeConfirm by remember { mutableStateOf(false) }

    if (showPurgeConfirm) {
        AlertDialog(
            onDismissRequest = { showPurgeConfirm = false },
            title = { Text("Confirm Purging Telemetry?", fontWeight = FontWeight.Bold) },
            text = { Text("This will completely delete all local measurement history, sensor values, and cached calibration scores from this device. This operation is irreversible.") },
            confirmButton = {
                TextButton(
                    onClick = { showPurgeConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("Purge All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurgeConfirm = false }) {
                    Text("Cancel", color = colors.textPrimary)
                }
            }
        )
    }

    FluxBackground(darkTheme = false) {
        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Top Navigation Header
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

                    Text(
                        text = "Privacy & Security",
                        color = colors.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Security Preferences",
                    color = colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Encryption and Biometrics Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(22.dp), spotColor = Color(0x0C000000))
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.cardBg)
                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SecurityRow(
                            icon = Icons.Default.Lock,
                            title = "Local Logs Encryption",
                            subtitle = "Apply AES-256 local database encryption to all cached Room telemetry.",
                            checked = encrytedLogs,
                            onCheckedChange = { encrytedLogs = it },
                            colors = colors
                        )
                        Divider(color = colors.cardBorder.copy(alpha = 0.6f), thickness = 0.5.dp)
                        SecurityRow(
                            icon = Icons.Default.Fingerprint,
                            title = "Biometric Lock",
                            subtitle = "Require fingerprint biometric or face authentication before viewing journals.",
                            checked = biometricUnlock,
                            onCheckedChange = { biometricUnlock = it },
                            colors = colors
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Data Retention & Telemetry",
                    color = colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Diagnostics & Purge Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(22.dp), spotColor = Color(0x0C000000))
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.cardBg)
                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SecurityRow(
                            icon = Icons.Default.Share,
                            title = "Anonymous Diagnostics",
                            subtitle = "Share anonymized Bluetooth signal RSSI and packet loss to improve stability.",
                            checked = analyticsSharing,
                            onCheckedChange = { analyticsSharing = it },
                            colors = colors
                        )

                        Divider(color = colors.cardBorder.copy(alpha = 0.6f), thickness = 0.5.dp)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPurgeConfirm = true }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Purge Local Telemetry Records", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color(0xFFEF4444))
                                Text("Irreversibly delete all offline cached Room database logs", fontSize = 11.5.sp, color = colors.textSecondary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun SecurityRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: FluxColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(FluxOrange.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = FluxOrange, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = colors.textPrimary)
                Text(subtitle, fontSize = 11.5.sp, color = colors.textSecondary)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = FluxOrange,
                uncheckedThumbColor = colors.textSecondary,
                uncheckedTrackColor = colors.pillBg
            )
        )
    }
}
