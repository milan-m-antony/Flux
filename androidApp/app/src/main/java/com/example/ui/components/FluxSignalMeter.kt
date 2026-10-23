package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connectivity.FluxConnectivityState

/**
 * 4-Bar Signal Strength Meter component exactly matching the reference design:
 * 4 ascending vertical rounded bars with bottom alignment, and "Signal: Poor/Good/Strong"
 * label below.
 */
@Composable
fun FluxSignalMeter(
    state: FluxConnectivityState,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    isOnline: Boolean = state.isOnline
) {
    // Determine active bar count and label based on state
    val (activeBars, qualityLabel, labelColor) = when {
        isOnline -> {
            val percent = state.signalPercent ?: 80
            when {
                percent >= 75 -> Triple(4, "Strong", if (darkTheme) Color(0xFF00E676) else Color(0xFF16A34A))
                percent >= 50 -> Triple(3, "Good", if (darkTheme) Color(0xFF00E676) else Color(0xFF16A34A))
                percent >= 25 -> Triple(2, "Moderate", Color(0xFFFA5C1D))
                else -> Triple(1, "Weak", if (darkTheme) Color(0xFFEF4444) else Color(0xFFDC2626))
            }
        }
        else -> {
            // When offline, display based on last reading or default to 2 orange bars with "Poor" matching screenshot
            val percent = state.signalPercent
            if (percent != null && percent > 0) {
                when {
                    percent >= 75 -> Triple(4, "Offline", if (darkTheme) Color(0xFFEF4444) else Color(0xFFDC2626))
                    percent >= 50 -> Triple(3, "Fair", Color(0xFFFA5C1D))
                    percent >= 25 -> Triple(2, "Poor", if (darkTheme) Color(0xFFEF4444) else Color(0xFFDC2626))
                    else -> Triple(1, "Poor", if (darkTheme) Color(0xFFEF4444) else Color(0xFFDC2626))
                }
            } else {
                // Exact reference screenshot state: 2 orange bars + "Signal: Poor"
                Triple(2, "Poor", if (darkTheme) Color(0xFFEF4444) else Color(0xFFDC2626))
            }
        }
    }

    // Active color for bars (Vibrant Orange #FA5C1D / Green / Red)
    val activeBarColor = when {
        qualityLabel == "Strong" || qualityLabel == "Good" -> if (darkTheme) Color(0xFF00E676) else Color(0xFF16A34A)
        qualityLabel == "Moderate" || qualityLabel == "Fair" || qualityLabel == "Poor" -> Color(0xFFFA5C1D)
        else -> if (darkTheme) Color(0xFFEF4444) else Color(0xFFDC2626)
    }

    // Inactive bar color: soft slate-lavender matching reference [191, 196, 215]
    val inactiveBarColor = if (darkTheme) Color(0xFF334155) else Color(0xFFCBD5E1)

    // Bar heights ascending (8dp, 13dp, 18dp, 23dp)
    val barHeights = listOf(8.dp, 13.dp, 18.dp, 23.dp)
    val barWidth = 4.5.dp
    val barSpacing = 2.5.dp

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 4 Vertical ascending bars aligned to bottom baseline
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(barSpacing)
        ) {
            for (index in 0 until 4) {
                val isActive = index < activeBars
                val targetColor = if (isActive) activeBarColor else inactiveBarColor
                val animatedColor by animateColorAsState(
                    targetValue = targetColor,
                    animationSpec = tween(durationMillis = 300),
                    label = "SignalBarColor_$index"
                )

                SignalBar(
                    height = barHeights[index],
                    width = barWidth,
                    color = animatedColor
                )
            }
        }

        Spacer(modifier = Modifier.height(2.5.dp))

        // "Signal: Poor" text with muted prefix and colored status label
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = if (darkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append("Signal: ")
                }
                withStyle(
                    SpanStyle(
                        color = labelColor,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(qualityLabel)
                }
            },
            fontSize = 9.5.sp,
            letterSpacing = (-0.1).sp,
            maxLines = 1
        )
    }
}

@Composable
private fun SignalBar(
    height: Dp,
    width: Dp,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(percent = 50))
            .background(color)
    )
}
