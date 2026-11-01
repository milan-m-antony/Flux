package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QuestionAnswer
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
fun HelpSupportScreen(onBack: () -> Unit) {
    val colors = rememberFluxColors(false)
    var expandedFaqIndex by remember { mutableStateOf<Int?>(null) }

    val faqs = listOf(
        FAQItem(
            question = "How do I pair my Flux ESP32 device over Bluetooth?",
            answer = "Switch on Bluetooth in your phone settings, then ensure your Flux device is powered on and within range. Open the 'Device' screen in the top header and click 'Scan' to pair instantly."
        ),
        FAQItem(
            question = "What should I do if my telemetry reading seems inaccurate?",
            answer = "Ensure the stainless steel DS18B20 probe is clean and firmly connected to GPIO 4. Navigate to the 'Device' screen to verify sensor calibration and bus health."
        ),
        FAQItem(
            question = "How is battery life optimized on the Flux hardware?",
            answer = "The ESP32 firmware dynamically throttles BLE advertising and OLED frame updates during periods of thermal stability to maximize operational battery runtime."
        ),
        FAQItem(
            question = "How does offline buffering work when Wi-Fi is lost?",
            answer = "When network is unavailable, telemetry points queue securely in the local SQLite Room database (up to 500 records). They flush automatically to the cloud as soon as link connectivity restores."
        )
    )

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
                        text = "Help & Support",
                        color = colors.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section Title: FAQs
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = FluxOrange, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Frequently Asked Questions",
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // FAQs List
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    faqs.forEachIndexed { index, faq ->
                        val isExpanded = expandedFaqIndex == index
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color(0x0C000000))
                                .clip(RoundedCornerShape(20.dp))
                                .background(colors.cardBg)
                                .border(0.5.dp, colors.cardBorder, RoundedCornerShape(20.dp))
                                .clickable { expandedFaqIndex = if (isExpanded) null else index }
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = faq.question,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp,
                                        color = colors.textPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = FluxOrange
                                    )
                                }

                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = expandVertically(),
                                    exit = shrinkVertically()
                                ) {
                                    Column {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = faq.answer,
                                            fontSize = 12.5.sp,
                                            color = colors.textSecondary,
                                            lineHeight = 17.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Contact Support Section
                Text(
                    text = "Contact Support Channels",
                    color = colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(22.dp), spotColor = Color(0x0C000000))
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.cardBg)
                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        SupportRowItem(
                            icon = Icons.Default.Email,
                            title = "Email Technical Support",
                            value = "support@fluxtelemetry.io",
                            colors = colors
                        )
                        Divider(color = colors.cardBorder.copy(alpha = 0.6f), thickness = 0.5.dp)
                        SupportRowItem(
                            icon = Icons.Default.Forum,
                            title = "Live Community Discussions",
                            value = "GitHub Discussions & Discord",
                            colors = colors
                        )
                        Divider(color = colors.cardBorder.copy(alpha = 0.6f), thickness = 0.5.dp)
                        SupportRowItem(
                            icon = Icons.Default.MenuBook,
                            title = "Hardware Documentation",
                            value = "docs.fluxtelemetry.io",
                            colors = colors
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

data class FAQItem(val question: String, val answer: String)

@Composable
private fun SupportRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    colors: FluxColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
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
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = colors.textPrimary)
            Text(value, fontSize = 11.5.sp, color = colors.textSecondary)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
    }
}
