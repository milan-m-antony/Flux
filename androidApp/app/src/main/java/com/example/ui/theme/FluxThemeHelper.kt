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
fun rememberFluxColors(darkTheme: Boolean): FluxColors {
    return if (darkTheme) {
        FluxColors(
            cardBg = Color(0xFF1E293B),
            cardBorder = Color(0xFF334155),
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFF94A3B8),
            surfaceElevated = Color(0xFF0F172A),
            pillBg = Color(0xFF334155),
            pillBorder = Color(0xFF475569),
            subcardBg = Color(0xFF162032),
            subcardBorder = Color(0xFF334155),
            dividerColor = Color(0xFF334155)
        )
    } else {
        FluxColors(
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
}
