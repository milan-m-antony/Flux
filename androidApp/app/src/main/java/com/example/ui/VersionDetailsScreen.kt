package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
fun VersionDetailsScreen(onBack: () -> Unit) {
    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val activeAccent = if (darkTheme) AccentCyan else Color(0xFF059669)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            NeoTopBar(
                title = "App Version",
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Brand Logo & Animation
            FluxLogo(modifier = Modifier.size(120.dp))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Flux Telemetry Mobile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Version 1.0.4 (Build 104)",
                fontSize = 14.sp,
                color = TextGrey
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // "Up to Date" green badge
            Row(
                modifier = Modifier
                    .background(activeAccent.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                    .border(1.dp, activeAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = activeAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "System Up to Date",
                    color = activeAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Technical build specifications
            Text(
                text = "Build Specifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (darkTheme) activeAccent else Color(0xFF111827),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(modifier = Modifier.fillMaxWidth().neoCard().padding(20.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BuildSpecRow(
                        icon = Icons.Default.Layers,
                        label = "Core Framework",
                        value = "Jetpack Compose (M3)",
                        darkTheme = darkTheme
                    )
                    Divider(color = if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                    BuildSpecRow(
                        icon = Icons.Default.Terminal,
                        label = "BLE Engine",
                        value = "v2.4.1 (Active)",
                        darkTheme = darkTheme
                    )
                    Divider(color = if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                    BuildSpecRow(
                        icon = Icons.Default.Code,
                        label = "Database Schema",
                        value = "Room Schema v12",
                        darkTheme = darkTheme
                    )
                    Divider(color = if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                    BuildSpecRow(
                        icon = Icons.Default.Info,
                        label = "Environment",
                        value = "Production (Release)",
                        darkTheme = darkTheme
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Release notes
            Text(
                text = "Release Notes (v1.0.4)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (darkTheme) activeAccent else Color(0xFF111827),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(modifier = Modifier.fillMaxWidth().neoCard().padding(20.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReleaseBulletPoint(
                        text = "Complete design overhaul of the modern light theme variant with dynamic adaptive styling.",
                        darkTheme = darkTheme
                    )
                    ReleaseBulletPoint(
                        text = "Enhanced sensor calibration accuracy and improved real-time diagnostic telemetry.",
                        darkTheme = darkTheme
                    )
                    ReleaseBulletPoint(
                        text = "Integrated dedicated doctor-sharing logs and direct clinic report exports.",
                        darkTheme = darkTheme
                    )
                    ReleaseBulletPoint(
                        text = "Fully optimized touch target click areas to meet Material 3 spacing density.",
                        darkTheme = darkTheme
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            NeoButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(
                    text = "Acknowledge",
                    color = if (darkTheme) Color.White else Color(0xFF111827),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun BuildSpecRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, darkTheme: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (darkTheme) AccentCyan else Color(0xFF111827),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = if (darkTheme) TextGrey else Color(0xFF4B5563),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ReleaseBulletPoint(text: String, darkTheme: Boolean) {
    val activeAccent = if (darkTheme) AccentCyan else Color(0xFF059669)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            color = activeAccent,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            color = if (darkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f) else Color(0xFF4B5563),
            lineHeight = 18.sp
        )
    }
}
