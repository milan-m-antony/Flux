package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

@Composable
fun AbstractBackground() {
    val darkTheme = androidx.compose.material3.MaterialTheme.colorScheme.background == Color(0xFF000000)
    val bgColor = if (darkTheme) Color.Black else Color(0xFFFAF9F6)
    val accentColor = if (darkTheme) Color.White else Color.Black
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            if (!darkTheme) {
                // Subtle background pattern of thin, intersecting soft sand-beige accent lines (#E2DDD4)
                val sandBeige = Color(0xFFE2DDD4)
                
                // Set background lines
                val step = 150.dp.toPx()
                for (i in -10..20) {
                    val offset = i * step
                    // Left-to-right diagonal
                    drawLine(
                        color = sandBeige.copy(alpha = 0.35f),
                        start = Offset(offset, -100f),
                        end = Offset(offset + h, h + 100f),
                        strokeWidth = 1.25f.dp.toPx()
                    )
                    // Right-to-left diagonal
                    drawLine(
                        color = sandBeige.copy(alpha = 0.35f),
                        start = Offset(offset + w, -100f),
                        end = Offset(offset, h + 100f),
                        strokeWidth = 1.25f.dp.toPx()
                    )
                }
            } else {
                // Top sweeping highlight
                drawPath(
                    path = Path().apply {
                        moveTo(0f, h * 0.15f)
                        quadraticBezierTo(w * 0.5f, h * 0.1f, w, h * 0.3f)
                        lineTo(w, 0f)
                        lineTo(0f, 0f)
                        close()
                    },
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E1E22),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = h * 0.3f
                    )
                )

                // Central swooping line (metallic rim)
                val rimPath = Path().apply {
                    moveTo(-w * 0.2f, h * 0.5f)
                    cubicTo(
                        w * 0.3f, h * 0.4f,
                        w * 0.7f, h * 0.2f,
                        w * 1.2f, h * 0.1f
                    )
                }
                
                drawPath(
                    path = rimPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.1f),
                            accentColor.copy(alpha = 0.4f),
                            accentColor.copy(alpha = 0.05f)
                        ),
                        start = Offset(0f, h * 0.3f),
                        end = Offset(w, h * 0.1f)
                    ),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                )

                // Middle faint glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.8f, h * 0.4f),
                        radius = w * 0.8f
                    ),
                    center = Offset(w * 0.8f, h * 0.4f),
                    radius = w * 0.8f
                )
                
                // Bottom sweeping rim
                val bottomRimPath = Path().apply {
                    moveTo(-w * 0.2f, h * 0.8f)
                    cubicTo(
                        w * 0.4f, h * 0.7f,
                        w * 0.8f, h * 0.9f,
                        w * 1.2f, h * 0.85f
                    )
                }
                
                drawPath(
                    path = bottomRimPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.05f),
                            accentColor.copy(alpha = 0.2f),
                            accentColor.copy(alpha = 0.05f)
                        ),
                        start = Offset(0f, h * 0.8f),
                        end = Offset(w, h * 0.85f)
                    ),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                )
            }
        }
    }
}
