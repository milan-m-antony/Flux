package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class FluxColors(
    val cardBg: Color = Color.White,
    val cardBorder: Color = Color(0xFFEEF2F6),
    val textPrimary: Color = FluxTextDark,
    val textSecondary: Color = FluxTextMuted,
    val surfaceElevated: Color = Color(0xFFF8FAFC),
    val pillBg: Color = Color(0xFFF8FAFC),
    val pillBorder: Color = Color(0xFFE2E8F0),
    val subcardBg: Color = Color(0xFFF8FAFC),
    val subcardBorder: Color = Color(0xFFE2E8F0),
    val dividerColor: Color = Color(0xFFF1F5F9),

    // Dynamic Profile Theme Tokens (Strictly Light / White Theme)
    val primary: Color = FluxOrange,
    val primaryGradientStart: Color = FluxOrangeGradientStart,
    val primaryGradientEnd: Color = FluxOrangeGradientEnd,
    val primarySoft: Color = FluxOrangeSoft,
    val primaryBorder: Color = FluxOrangeBorder
)

@Composable
fun rememberFluxColors(
    darkTheme: Boolean = false,
    activeProfile: String = "Live Monitor"
): FluxColors {
    // Determine profile-specific accent colors (Pure light/white theme)
    return when {
        activeProfile.contains("Environment", ignoreCase = true) || activeProfile.contains("Env", ignoreCase = true) -> {
            // Nature Emerald Green Theme
            FluxColors(
                cardBg = Color.White,
                cardBorder = Color(0xFFEEF2F6),
                textPrimary = FluxTextDark,
                textSecondary = FluxTextMuted,
                surfaceElevated = Color(0xFFF8FAFC),
                pillBg = Color(0xFFF0FDF4),
                pillBorder = Color(0xFFDCFCE7),
                subcardBg = Color(0xFFF0FDF4),
                subcardBorder = Color(0xFFDCFCE7),
                dividerColor = Color(0xFFF1F5F9),
                primary = Color(0xFF10B981),
                primaryGradientStart = Color(0xFF059669),
                primaryGradientEnd = Color(0xFF10B981),
                primarySoft = Color(0xFFECFDF5),
                primaryBorder = Color(0xFFA7F3D0)
            )
        }
        activeProfile.contains("Liquid", ignoreCase = true) || activeProfile.contains("Liq", ignoreCase = true) -> {
            // Aquatic Ocean Blue / Cyan Theme
            FluxColors(
                cardBg = Color.White,
                cardBorder = Color(0xFFEEF2F6),
                textPrimary = FluxTextDark,
                textSecondary = FluxTextMuted,
                surfaceElevated = Color(0xFFF8FAFC),
                pillBg = Color(0xFFF0F9FF),
                pillBorder = Color(0xFFE0F2FE),
                subcardBg = Color(0xFFF0F9FF),
                subcardBorder = Color(0xFFE0F2FE),
                dividerColor = Color(0xFFF1F5F9),
                primary = Color(0xFF0284C7),
                primaryGradientStart = Color(0xFF0284C7),
                primaryGradientEnd = Color(0xFF38BDF8),
                primarySoft = Color(0xFFF0F9FF),
                primaryBorder = Color(0xFFBAE6FD)
            )
        }
        activeProfile.contains("Fermentation", ignoreCase = true) || activeProfile.contains("Ferm", ignoreCase = true) -> {
            // Golden Amber / Brew Honey Theme
            FluxColors(
                cardBg = Color.White,
                cardBorder = Color(0xFFEEF2F6),
                textPrimary = FluxTextDark,
                textSecondary = FluxTextMuted,
                surfaceElevated = Color(0xFFF8FAFC),
                pillBg = Color(0xFFFFFBEB),
                pillBorder = Color(0xFFFEF3C7),
                subcardBg = Color(0xFFFFFBEB),
                subcardBorder = Color(0xFFFEF3C7),
                dividerColor = Color(0xFFF1F5F9),
                primary = Color(0xFFD97706),
                primaryGradientStart = Color(0xFFB45309),
                primaryGradientEnd = Color(0xFFF59E0B),
                primarySoft = Color(0xFFFFFBEB),
                primaryBorder = Color(0xFFFDE68A)
            )
        }
        else -> {
            // Signature Flux Orange Theme (Live Monitor & Default)
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
                dividerColor = Color(0xFFF1F5F9),
                primary = FluxOrange,
                primaryGradientStart = FluxOrangeGradientStart,
                primaryGradientEnd = FluxOrangeGradientEnd,
                primarySoft = FluxOrangeSoft,
                primaryBorder = FluxOrangeBorder
            )
        }
    }
}
