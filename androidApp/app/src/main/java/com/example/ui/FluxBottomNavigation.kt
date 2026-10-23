package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class FluxScreen {
    HOME,
    HISTORY,
    PROFILES,
    ALERTS,
    SETTINGS
}

@Composable
fun FluxBottomNavigation(
    currentScreen: FluxScreen,
    onScreenSelected: (FluxScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(38.dp),
                spotColor = Color(0x18000000),
                ambientColor = Color(0x0C000000)
            )
            .background(
                color = Color.White.copy(alpha = 0.98f),
                shape = RoundedCornerShape(38.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(38.dp)
            )
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Home
            BottomNavItem(
                label = "Home",
                isSelected = currentScreen == FluxScreen.HOME,
                activeColor = FluxTextDark,
                onClick = { onScreenSelected(FluxScreen.HOME) },
                icon = { tint ->
                    Icon(
                        imageVector = if (currentScreen == FluxScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                        contentDescription = "Home",
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.weight(1f)
            )

            // 2. History
            BottomNavItem(
                label = "History",
                isSelected = currentScreen == FluxScreen.HISTORY,
                activeColor = FluxOrange,
                onClick = { onScreenSelected(FluxScreen.HISTORY) },
                icon = { tint ->
                    HistoryNavIcon(tint = tint)
                },
                modifier = Modifier.weight(1f)
            )

            // 3. Profiles (Center Prominent Button)
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .clickable { onScreenSelected(FluxScreen.PROFILES) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = CircleShape,
                                spotColor = FluxOrange.copy(alpha = 0.5f)
                            )
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(FluxOrangeGradientStart, FluxOrangeGradientEnd)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.GridView,
                            contentDescription = "Profiles",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "Profiles",
                        color = FluxOrange,
                        fontSize = 10.5.sp,
                        fontWeight = if (currentScreen == FluxScreen.PROFILES) FontWeight.Bold else FontWeight.SemiBold
                    )
                }
            }

            // 4. Alerts
            BottomNavItem(
                label = "Alerts",
                isSelected = currentScreen == FluxScreen.ALERTS,
                activeColor = FluxOrange,
                onClick = { onScreenSelected(FluxScreen.ALERTS) },
                icon = { tint ->
                    Icon(
                        imageVector = if (currentScreen == FluxScreen.ALERTS) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                        contentDescription = "Alerts",
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.weight(1f)
            )

            // 5. Settings
            BottomNavItem(
                label = "Settings",
                isSelected = currentScreen == FluxScreen.SETTINGS,
                activeColor = FluxOrange,
                onClick = { onScreenSelected(FluxScreen.SETTINGS) },
                icon = { tint ->
                    Icon(
                        imageVector = if (currentScreen == FluxScreen.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (isSelected) activeColor else FluxTextLight

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon(tint)

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            color = tint,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Active indicator dot
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(FluxOrange)
            )
        } else {
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

@Composable
private fun HistoryNavIcon(tint: Color) {
    Row(
        modifier = Modifier.size(24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .width(4.5.dp)
                .height(11.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(tint)
        )
        Box(
            modifier = Modifier
                .width(4.5.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(tint)
        )
        Box(
            modifier = Modifier
                .width(4.5.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(tint)
        )
    }
}
