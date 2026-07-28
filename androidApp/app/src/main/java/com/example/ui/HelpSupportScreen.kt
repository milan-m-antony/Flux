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
import androidx.compose.material.icons.filled.ChevronRight
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
fun HelpSupportScreen(onBack: () -> Unit) {
    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val activeAccent = if (darkTheme) AccentCyan else Color(0xFF059669)

    var expandedFaqIndex by remember { mutableStateOf<Int?>(null) }

    val faqs = listOf(
        FAQItem(
            question = "How do I pair my NaTrack device over Bluetooth?",
            answer = "Switch on Bluetooth in your system settings, then make sure the NaTrack wearable is powered on and within range. Navigate to the 'Device' tab in this app and click 'Search Device' to pair instantly."
        ),
        FAQItem(
            question = "What should I do if my sodium reading seems inaccurate?",
            answer = "Ensure the sensor contact face is clean and flush against your wrist. Navigate to the 'Device' screen, choose 'Calibrate Sensor' from the action list, enter your laboratory reference mV, and save calibration."
        ),
        FAQItem(
            question = "How is battery life optimized on the NaTrack wearable?",
            answer = "The hardware will automatically power down BLE advertising channels when not performing readings. Charging to 100% takes about 45 minutes and typically powers continuous background state tracking for 5 days."
        ),
        FAQItem(
            question = "How do I secure my historical health journals?",
            answer = "Go to Profile -> Privacy & Security to activate military-grade AES-256 database encryption. You can also enforce fingerprint biometric unlock challenges before the database journal opens."
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            NeoTopBar(
                title = "Help & Support",
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
            // FAQs Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = activeAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Frequently Asked Questions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (darkTheme) activeAccent else Color(0xFF111827)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // FAQs List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                faqs.forEachIndexed { index, faq ->
                    val isExpanded = expandedFaqIndex == index
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neoCard()
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
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = activeAccent
                                )
                            }
                            
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = faq.answer,
                                        fontSize = 13.sp,
                                        color = TextGrey,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Contact support title
            Text(
                text = "Contact Support",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (darkTheme) activeAccent else Color(0xFF111827)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Support rows
            Box(modifier = Modifier.fillMaxWidth().neoCard().padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SupportRow(
                        icon = Icons.Default.Email,
                        title = "Email Support",
                        value = "support@natrack.io",
                        activeAccent = activeAccent
                    )
                    Divider(color = if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                    SupportRow(
                        icon = Icons.Default.Forum,
                        title = "Live Customer Chat",
                        value = "Open instant ticket",
                        activeAccent = activeAccent
                    )
                    Divider(color = if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                    SupportRow(
                        icon = Icons.Default.MenuBook,
                        title = "Online Knowledge Base",
                        value = "Read documentation",
                        activeAccent = activeAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

data class FAQItem(val question: String, val answer: String)

@Composable
fun SupportRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    activeAccent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle support action */ }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = activeAccent, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(value, fontSize = 12.sp, color = TextGrey)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextGrey)
    }
}
