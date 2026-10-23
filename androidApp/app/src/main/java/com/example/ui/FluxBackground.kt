package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.example.ui.theme.FluxBgLight
import com.example.ui.theme.FluxWaveOrange
import com.example.ui.theme.FluxWavePeach

@Composable
fun FluxBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Base clean cool background
            drawRect(color = FluxBgLight)

            // 2. Upper diagonal flowing peach/orange wave gradients
            val wave1 = Path().apply {
                moveTo(width * 0.2f, 0f)
                cubicTo(
                    width * 0.45f, height * 0.08f,
                    width * 0.85f, height * 0.04f,
                    width, height * 0.16f
                )
                lineTo(width, 0f)
                close()
            }
            drawPath(
                path = wave1,
                brush = Brush.linearGradient(
                    colors = listOf(
                        FluxWaveOrange.copy(alpha = 0.55f),
                        FluxWavePeach.copy(alpha = 0.40f)
                    ),
                    start = Offset(width * 0.3f, 0f),
                    end = Offset(width, height * 0.16f)
                )
            )

            val wave2 = Path().apply {
                moveTo(0f, height * 0.18f)
                cubicTo(
                    width * 0.25f, height * 0.12f,
                    width * 0.65f, height * 0.26f,
                    width, height * 0.19f
                )
                lineTo(width, height * 0.38f)
                cubicTo(
                    width * 0.65f, height * 0.32f,
                    width * 0.25f, height * 0.35f,
                    0f, height * 0.28f
                )
                close()
            }
            drawPath(
                path = wave2,
                brush = Brush.linearGradient(
                    colors = listOf(
                        FluxWavePeach.copy(alpha = 0.45f),
                        FluxWaveOrange.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    start = Offset(0f, height * 0.15f),
                    end = Offset(width, height * 0.35f)
                )
            )

            val wave3 = Path().apply {
                moveTo(width * 0.35f, height * 0.46f)
                cubicTo(
                    width * 0.65f, height * 0.42f,
                    width * 0.90f, height * 0.52f,
                    width, height * 0.48f
                )
                lineTo(width, height * 0.62f)
                cubicTo(
                    width * 0.70f, height * 0.66f,
                    width * 0.45f, height * 0.58f,
                    width * 0.25f, height * 0.54f
                )
                close()
            }
            drawPath(
                path = wave3,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        FluxWaveOrange.copy(alpha = 0.30f),
                        FluxWavePeach.copy(alpha = 0.20f)
                    ),
                    start = Offset(width * 0.3f, height * 0.45f),
                    end = Offset(width, height * 0.62f)
                )
            )
        }

        content()
    }
}
