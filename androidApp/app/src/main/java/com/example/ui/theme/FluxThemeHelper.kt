package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class FluxColors(
    val cardBg: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val surfaceElevated: Color,
    val pillBg: Color,
    val pillBorder: Color,
    val subcardBg: Color,
    val subcardBorder: Color,
    val dividerColor: Color
)

@Composable
fun rememberFluxColors(darkTheme: Boolean = false): FluxColors {
    // Strictly follow Flux signature light palette
    return FluxColors(
        cardBg = Color.White,
        cardBorder = Color(0xFFEEF2F6),
        textPrimary = FluxTextDark,
        textSecondary = FluxTextMuted,
        surfaceElevated = Color(0xFFF8FAFC),
        pillBg = Color(0xFFF8FAFC),
        pillBorder = Color(0xFFE2E8F0),
        subcardBg = Color(0xFFF8FAFC),
        subcardBorder = Color(0xFFE2E8F0),
        dividerColor = Color(0xFFF1F5F9)
    )
}
