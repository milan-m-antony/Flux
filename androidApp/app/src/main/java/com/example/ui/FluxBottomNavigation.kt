package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
    modifier: Modifier = Modifier,
    darkTheme: Boolean = false
) {
    // Reduced size and curved capsule container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(
                elevation = if (darkTheme) 6.dp else 8.dp,
                shape = RoundedCornerShape(percent = 50),
                spotColor = if (darkTheme) Color(0x30000000) else Color(0x14000000),
                ambientColor = Color(0x0A000000)
            )
            .background(
                color = if (darkTheme) Color(0xFF1E293B).copy(alpha = 0.98f) else Color.White.copy(alpha = 0.98f),
                shape = RoundedCornerShape(percent = 50)
            )
            .border(
                width = 1.dp,
                color = if (darkTheme) Color(0xFF334155) else Color(0xFFF1F5F9),
                shape = RoundedCornerShape(percent = 50)
            )
            .padding(horizontal = 6.dp, vertical = 4.dp)
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
                activeColor = if (darkTheme) Color(0xFFF8FAFC) else FluxTextDark,
                inactiveColor = if (darkTheme) Color(0xFF94A3B8) else FluxTextLight,
                onClick = { onScreenSelected(FluxScreen.HOME) },
                icon = { tint ->
                    Icon(
                        imageVector = if (currentScreen == FluxScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                        contentDescription = "Home",
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.weight(1f)
            )

            // 2. History
            BottomNavItem(
                label = "History",
                isSelected = currentScreen == FluxScreen.HISTORY,
                activeColor = FluxOrange,
                inactiveColor = if (darkTheme) Color(0xFF94A3B8) else FluxTextLight,
                onClick = { onScreenSelected(FluxScreen.HISTORY) },
                icon = { tint ->
                    HistoryNavIcon(tint = tint)
                },
                modifier = Modifier.weight(1f)
            )

            // 3. Profiles (Center Prominent Button with subtle scale bounce)
            val centerScale by animateFloatAsState(
                targetValue = if (currentScreen == FluxScreen.PROFILES) 1.08f else 1.0f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                label = "CenterScale"
            )
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onScreenSelected(FluxScreen.PROFILES) }
                    .scale(centerScale),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .shadow(
                                elevation = 5.dp,
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
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Profiles",
                        color = FluxOrange,
                        fontSize = 9.sp,
                        fontWeight = if (currentScreen == FluxScreen.PROFILES) FontWeight.Bold else FontWeight.SemiBold
                    )
                }
            }

            // 4. Alerts
            BottomNavItem(
                label = "Alerts",
                isSelected = currentScreen == FluxScreen.ALERTS,
                activeColor = FluxOrange,
                inactiveColor = if (darkTheme) Color(0xFF94A3B8) else FluxTextLight,
                onClick = { onScreenSelected(FluxScreen.ALERTS) },
                icon = { tint ->
                    Icon(
                        imageVector = if (currentScreen == FluxScreen.ALERTS) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                        contentDescription = "Alerts",
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.weight(1f)
            )

            // 5. Settings
            BottomNavItem(
                label = "Settings",
                isSelected = currentScreen == FluxScreen.SETTINGS,
                activeColor = FluxOrange,
                inactiveColor = if (darkTheme) Color(0xFF94A3B8) else FluxTextLight,
                onClick = { onScreenSelected(FluxScreen.SETTINGS) },
                icon = { tint ->
                    Icon(
                        imageVector = if (currentScreen == FluxScreen.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = tint,
                        modifier = Modifier.size(20.dp)
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
    inactiveColor: Color,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "ItemScale"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val tint = if (isSelected) activeColor else inactiveColor
        icon(tint)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = tint,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun HistoryNavIcon(tint: Color) {
    Box(
        modifier = Modifier.size(20.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
            val strokeWidth = 1.6.dp.toPx()
            val w = size.width
            val h = size.height

            // Outer clock / trend circular arc
            drawArc(
                color = tint,
                startAngle = 45f,
                sweepAngle = 270f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )

            // Clock hands
            drawLine(
                color = tint,
                start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f),
                end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.22f),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            drawLine(
                color = tint,
                start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f),
                end = androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.5f),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}
