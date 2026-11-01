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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun VersionDetailsScreen(onBack: () -> Unit) {
    val colors = rememberFluxColors(false)

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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Top Header Row
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
                        text = "App & Firmware Details",
                        color = colors.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Brand Logo
                FluxLogo(modifier = Modifier.size(100.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Flux Smart Telemetry",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Version 1.0.4 (Build 104)",
                    fontSize = 13.5.sp,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Up to Date Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.12f))
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Firmware & App Synced",
                        color = Color(0xFF10B981),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(26.dp))

                // Build Specifications Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.cardBg)
                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Build Specifications",
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        BuildSpecItem(
                            icon = Icons.Default.Layers,
                            label = "UI Architecture",
                            value = "Jetpack Compose (M3)",
                            colors = colors
                        )
                        Divider(color = colors.cardBorder.copy(alpha = 0.6f), thickness = 0.5.dp)
                        BuildSpecItem(
                            icon = Icons.Default.Terminal,
                            label = "BLE Telemetry Engine",
                            value = "Nordic Semiconductor BLE GATT",
                            colors = colors
                        )
                        Divider(color = colors.cardBorder.copy(alpha = 0.6f), thickness = 0.5.dp)
                        BuildSpecItem(
                            icon = Icons.Default.Code,
                            label = "Local Storage",
                            value = "Room DB SQLite Cache",
                            colors = colors
                        )
                        Divider(color = colors.cardBorder.copy(alpha = 0.6f), thickness = 0.5.dp)
                        BuildSpecItem(
                            icon = Icons.Default.Info,
                            label = "Hardware Target",
                            value = "ESP32-WROOM-32 (PlatformIO)",
                            colors = colors
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun BuildSpecItem(
    icon: ImageVector,
    label: String,
    value: String,
    colors: FluxColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FluxOrange,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(label, color = colors.textSecondary, fontSize = 12.5.sp)
        }
        Text(value, color = colors.textPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
    }
}
