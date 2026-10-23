package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.MainViewModel
import com.example.R
import com.example.ui.theme.*

@Composable
fun FluxProfilesScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val activeProfile by viewModel.activeProfile.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val isOnline = connectionState == com.example.ble.ConnectionState.CONNECTED || true

    val darkTheme by viewModel.isDarkTheme.collectAsState()
    val colors = rememberFluxColors(darkTheme)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            // Spacer to clear the fixed top header cleanly
            Spacer(modifier = Modifier.height(68.dp))

            // 2. Back Button & Title Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = colors.textPrimary,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onBack() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Monitoring Profiles",
                    color = colors.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Card 1 — Live Monitor (Stainless Steel Temperature Probe)
            ProfileCardItem(
                title = "Live Monitor",
                imageRes = R.drawable.probe_temp,
                darkTheme = darkTheme,
                colors = colors,
                isActive = activeProfile == "Live Monitor",
                onClick = {
                    viewModel.saveActiveProfile("Live Monitor")
                    Toast.makeText(context, "Monitoring mode: Live Monitor", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Card 2 — Environment (Green Plant)
            ProfileCardItem(
                title = "Environment",
                imageRes = R.drawable.plant_env,
                darkTheme = darkTheme,
                colors = colors,
                isActive = activeProfile == "Environment",
                onClick = {
                    viewModel.saveActiveProfile("Environment")
                    Toast.makeText(context, "Monitoring mode: Environment", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Card 3 — Liquid (Transparent Glass of Water)
            ProfileCardItem(
                title = "Liquid",
                imageRes = R.drawable.glass_water,
                darkTheme = darkTheme,
                colors = colors,
                isActive = activeProfile == "Liquid",
                onClick = {
                    viewModel.saveActiveProfile("Liquid")
                    Toast.makeText(context, "Monitoring mode: Liquid", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Card 4 — Fermentation (Fermenting Dough Jar)
            ProfileCardItem(
                title = "Fermentation",
                imageRes = R.drawable.ferment_jar,
                darkTheme = darkTheme,
                colors = colors,
                isActive = activeProfile == "Fermentation",
                onClick = {
                    viewModel.saveActiveProfile("Fermentation")
                    Toast.makeText(context, "Monitoring mode: Fermentation", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
private fun ProfileCardItem(
    title: String,
    imageRes: Int,
    darkTheme: Boolean,
    colors: FluxColors,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val gradientColors = if (darkTheme) {
        when (title) {
            "Live Monitor" -> listOf(Color(0xFF2A201A), Color(0xFF1E1E28))
            "Environment" -> listOf(Color(0xFF132B1C), Color(0xFF131D24))
            "Liquid" -> listOf(Color(0xFF14243A), Color(0xFF131B28))
            else -> listOf(Color(0xFF2B2014), Color(0xFF1E1B24))
        }
    } else {
        when (title) {
            "Live Monitor" -> listOf(Color(0xFFFFF9F5), Color(0xFFFFE8DC))
            "Environment" -> listOf(Color(0xFFF7FDF9), Color(0xFFDCFCE7))
            "Liquid" -> listOf(Color(0xFFF8FAFC), Color(0xFFDBEAFE))
            else -> listOf(Color(0xFFFFFDF8), Color(0xFFFFEDD5))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(124.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color(0x12000000)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(
                brush = Brush.horizontalGradient(colors = gradientColors)
            )
            .border(
                width = if (isActive) 1.5.dp else 0.5.dp,
                color = if (isActive) FluxOrange else (if (darkTheme) Color(0xFF334155) else Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(26.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Image (Clipped inside rounded rectangle)
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(22.dp))

            // Center Title
            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Right Chevron
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
