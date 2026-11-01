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
import com.example.ui.theme.FluxOrange
import com.example.ui.theme.FluxWaveOrange
import com.example.ui.theme.FluxWavePeach

@Composable
fun FluxBackground(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Base background: Clean cool light canvas
            val baseColor = FluxBgLight
            drawRect(color = baseColor)

            // 2. Wave 1: Soft warm peach accent in top right corner
            val wave1 = Path().apply {
                moveTo(width * 0.35f, 0f)
                cubicTo(
                    width * 0.55f, height * 0.06f,
                    width * 0.82f, height * 0.03f,
                    width, height * 0.14f
                )
                lineTo(width, 0f)
                close()
            }
            val wave1Colors = listOf(
                FluxWaveOrange.copy(alpha = 0.60f),
                FluxWavePeach.copy(alpha = 0.45f)
            )
            drawPath(
                path = wave1,
                brush = Brush.linearGradient(
                    colors = wave1Colors,
                    start = Offset(width * 0.4f, 0f),
                    end = Offset(width, height * 0.14f)
                )
            )

            // 3. Wave 2: Organic peach blob flowing from mid-left behind circular gauge
            val wave2 = Path().apply {
                moveTo(0f, height * 0.15f)
                cubicTo(
                    width * 0.28f, height * 0.12f,
                    width * 0.42f, height * 0.22f,
                    width * 0.35f, height * 0.32f
                )
                cubicTo(
                    width * 0.25f, height * 0.38f,
                    width * 0.10f, height * 0.35f,
                    0f, height * 0.34f
                )
                close()
            }
            val wave2Colors = listOf(
                FluxWavePeach.copy(alpha = 0.50f),
                FluxWaveOrange.copy(alpha = 0.30f),
                Color.Transparent
            )
            drawPath(
                path = wave2,
                brush = Brush.linearGradient(
                    colors = wave2Colors,
                    start = Offset(0f, height * 0.20f),
                    end = Offset(width * 0.40f, height * 0.25f)
                )
            )

            // 4. Wave 3: Organic peach blob flowing from mid-right behind controls & trend card
            val wave3 = Path().apply {
                moveTo(width, height * 0.38f)
                cubicTo(
                    width * 0.72f, height * 0.42f,
                    width * 0.58f, height * 0.48f,
                    width * 0.68f, height * 0.58f
                )
                cubicTo(
                    width * 0.78f, height * 0.64f,
                    width * 0.90f, height * 0.60f,
                    width, height * 0.58f
                )
                close()
            }
            val wave3Colors = listOf(
                Color.Transparent,
                FluxWaveOrange.copy(alpha = 0.35f),
                FluxWavePeach.copy(alpha = 0.45f)
            )
            drawPath(
                path = wave3,
                brush = Brush.linearGradient(
                    colors = wave3Colors,
                    start = Offset(width * 0.60f, height * 0.48f),
                    end = Offset(width, height * 0.54f)
                )
            )
        }

        content()
    }
}
