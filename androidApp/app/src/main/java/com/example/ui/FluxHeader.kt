package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun FluxHeader(
    modifier: Modifier = Modifier,
    isOnline: Boolean = true,
    hasUnreadAlerts: Boolean = true,
    onNotificationClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(34.dp),
                spotColor = Color(0x14000000),
                ambientColor = Color(0x0A000000)
            )
            .background(
                color = Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(34.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(34.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Flux Flame Logo
            Image(
                painter = painterResource(id = R.drawable.ic_flux_flame),
                contentDescription = "Flux Logo",
                modifier = Modifier.size(34.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // 2. Title & Subtitle
            Column {
                Text(
                    text = "Flux",
                    color = FluxTextDark,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Smart Telemetry System",
                    color = FluxTextMuted,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 3. ESP32 Status Pill Container
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color(0x10000000)
                    )
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                    .padding(horizontal = 7.dp, vertical = 5.dp)
            ) {
                // Circular ESP32 photo container
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF8FAFC)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.esp32_board),
                        contentDescription = "ESP32 Board",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = "ESP32",
                        color = FluxTextDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) FluxGreenOnline else Color(0xFF94A3B8))
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isOnline) "Online" else "Offline",
                            color = FluxTextMuted,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // 4. Circular Notification Bell Button with Orange Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shadow(
                        elevation = 2.dp,
                        shape = CircleShape,
                        spotColor = Color(0x12000000)
                    )
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(0.5.dp, Color(0xFFE2E8F0), CircleShape)
                    .clickable { onNotificationClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Alerts",
                    tint = FluxTextDark,
                    modifier = Modifier.size(20.dp)
                )

                if (hasUnreadAlerts) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 9.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(FluxOrange)
                    )
                }
            }
        }
    }
}
