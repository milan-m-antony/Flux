package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.sin

// Fullscreen procedural leaf specification for Environment profile
private data class BgLeaf(
    val relX: Float,
    val speed: Float,
    val sizeDp: Float,
    val swayFreq: Float,
    val phase: Float,
    val swayAmpDp: Float,
    val rotSpeed: Float,
    val color: Color
)

private val BG_LEAVES = listOf(
    BgLeaf(0.08f, 0.75f, 16f, 2.0f, 0.3f, 18f, 160f, Color(0xFF10B981)),
    BgLeaf(0.18f, 1.10f, 20f, 1.6f, 1.8f, 22f, -200f, Color(0xFF059669)),
    BgLeaf(0.28f, 0.65f, 13f, 2.4f, 3.1f, 15f, 240f, Color(0xFF34D399)),
    BgLeaf(0.40f, 0.95f, 18f, 1.8f, 4.2f, 20f, -180f, Color(0xFF10B981)),
    BgLeaf(0.53f, 1.20f, 22f, 1.5f, 0.7f, 24f, 220f, Color(0xFF047857)),
    BgLeaf(0.65f, 0.85f, 15f, 2.2f, 2.5f, 17f, -160f, Color(0xFF6EE7B7)),
    BgLeaf(0.77f, 1.05f, 24f, 1.7f, 5.0f, 21f, 190f, Color(0xFF10B981)),
    BgLeaf(0.88f, 0.70f, 14f, 2.3f, 1.1f, 14f, -230f, Color(0xFF34D399)),
    BgLeaf(0.96f, 1.15f, 19f, 1.9f, 3.8f, 23f, 170f, Color(0xFF059669)),
    BgLeaf(0.22f, 0.80f, 15f, 2.1f, 5.7f, 16f, 210f, Color(0xFF34D399)),
    BgLeaf(0.58f, 1.00f, 17f, 1.8f, 2.9f, 19f, -190f, Color(0xFF10B981)),
    BgLeaf(0.82f, 0.90f, 21f, 1.6f, 4.6f, 22f, 230f, Color(0xFF047857))
)

// Fullscreen rising bubble specification for Liquid & Fermentation profiles
private data class BgBubble(
    val relX: Float,
    val speed: Float,
    val sizeDp: Float,
    val wobbleFreq: Float,
    val phase: Float,
    val wobbleAmpDp: Float
)

private val BG_BUBBLES = listOf(
    BgBubble(0.09f, 0.9f, 5.5f, 2.8f, 0.4f, 10f),
    BgBubble(0.19f, 1.3f, 3.8f, 3.6f, 1.9f, 12f),
    BgBubble(0.31f, 0.7f, 7.0f, 2.2f, 3.3f, 8f),
    BgBubble(0.44f, 1.1f, 4.2f, 3.2f, 4.7f, 11f),
    BgBubble(0.57f, 0.85f, 6.2f, 2.6f, 1.2f, 9f),
    BgBubble(0.69f, 1.4f, 3.5f, 4.0f, 2.7f, 13f),
    BgBubble(0.81f, 1.0f, 5.0f, 3.0f, 5.1f, 10f),
    BgBubble(0.92f, 1.2f, 4.0f, 3.5f, 0.8f, 12f),
    BgBubble(0.25f, 0.8f, 6.5f, 2.4f, 4.0f, 9f),
    BgBubble(0.74f, 1.15f, 4.8f, 3.3f, 2.1f, 11f)
)

@Composable
fun FluxBackground(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = false,
    activeProfile: String = "Live Monitor",
    content: @Composable () -> Unit
) {
    val normProfile = activeProfile.lowercase()
    val isEnv = normProfile.contains("env")
    val isLiquid = normProfile.contains("liq")
    val isFerm = normProfile.contains("ferm")

    // Continuous smooth animation transitions for full-screen ambient effects
    val infiniteTransition = rememberInfiniteTransition(label = "fluxBgMotion")

    val particleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bgParticleProgress"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bgWavePhase"
    )

    val radarRadiusProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bgRadarProgress"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Base Canvas Background (Signature Light)
            drawRect(color = FluxBgLight)

            // 2. Profile-Themed Organic Ambient Wave Gradients
            when {
                isEnv -> {
                    // Nature Green Ambient Wave Accents
                    val wave1 = Path().apply {
                        moveTo(width * 0.30f, 0f)
                        cubicTo(width * 0.52f, height * 0.07f, width * 0.80f, height * 0.04f, width, height * 0.16f)
                        lineTo(width, 0f)
                        close()
                    }
                    drawPath(
                        path = wave1,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFD1FAE5).copy(alpha = 0.55f), Color(0xFFA7F3D0).copy(alpha = 0.35f)),
                            start = Offset(width * 0.35f, 0f),
                            end = Offset(width, height * 0.16f)
                        )
                    )

                    val wave2 = Path().apply {
                        moveTo(0f, height * 0.18f)
                        cubicTo(width * 0.30f, height * 0.14f, width * 0.44f, height * 0.25f, width * 0.36f, height * 0.36f)
                        cubicTo(width * 0.26f, height * 0.42f, width * 0.10f, height * 0.38f, 0f, height * 0.37f)
                        close()
                    }
                    drawPath(
                        path = wave2,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFA7F3D0).copy(alpha = 0.35f), Color(0xFFD1FAE5).copy(alpha = 0.18f), Color.Transparent),
                            start = Offset(0f, height * 0.20f),
                            end = Offset(width * 0.40f, height * 0.28f)
                        )
                    )
                }
                isLiquid -> {
                    // Ocean Aqua Ambient Wave Accents
                    val wave1 = Path().apply {
                        moveTo(width * 0.32f, 0f)
                        cubicTo(width * 0.54f, height * 0.06f, width * 0.82f, height * 0.03f, width, height * 0.15f)
                        lineTo(width, 0f)
                        close()
                    }
                    drawPath(
                        path = wave1,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFBAE6FD).copy(alpha = 0.55f), Color(0xFF7DD3FC).copy(alpha = 0.30f)),
                            start = Offset(width * 0.35f, 0f),
                            end = Offset(width, height * 0.15f)
                        )
                    )

                    val wave2 = Path().apply {
                        moveTo(0f, height * 0.16f)
                        cubicTo(width * 0.28f, height * 0.12f, width * 0.42f, height * 0.22f, width * 0.35f, height * 0.34f)
                        cubicTo(width * 0.25f, height * 0.40f, width * 0.10f, height * 0.36f, 0f, height * 0.35f)
                        close()
                    }
                    drawPath(
                        path = wave2,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFBAE6FD).copy(alpha = 0.40f), Color(0xFFE0F2FE).copy(alpha = 0.20f), Color.Transparent),
                            start = Offset(0f, height * 0.18f),
                            end = Offset(width * 0.40f, height * 0.26f)
                        )
                    )
                }
                isFerm -> {
                    // Warm Golden Amber Ambient Wave Accents
                    val wave1 = Path().apply {
                        moveTo(width * 0.32f, 0f)
                        cubicTo(width * 0.55f, height * 0.06f, width * 0.82f, height * 0.03f, width, height * 0.15f)
                        lineTo(width, 0f)
                        close()
                    }
                    drawPath(
                        path = wave1,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFDE68A).copy(alpha = 0.55f), Color(0xFFFCD34D).copy(alpha = 0.30f)),
                            start = Offset(width * 0.35f, 0f),
                            end = Offset(width, height * 0.15f)
                        )
                    )

                    val wave2 = Path().apply {
                        moveTo(0f, height * 0.16f)
                        cubicTo(width * 0.28f, height * 0.12f, width * 0.42f, height * 0.22f, width * 0.35f, height * 0.34f)
                        cubicTo(width * 0.25f, height * 0.40f, width * 0.10f, height * 0.36f, 0f, height * 0.35f)
                        close()
                    }
                    drawPath(
                        path = wave2,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFDE68A).copy(alpha = 0.40f), Color(0xFFFEF3C7).copy(alpha = 0.20f), Color.Transparent),
                            start = Offset(0f, height * 0.18f),
                            end = Offset(width * 0.40f, height * 0.26f)
                        )
                    )
                }
                else -> {
                    // Signature Warm Peach & Orange Waves (Live Monitor)
                    val wave1 = Path().apply {
                        moveTo(width * 0.35f, 0f)
                        cubicTo(width * 0.55f, height * 0.06f, width * 0.82f, height * 0.03f, width, height * 0.14f)
                        lineTo(width, 0f)
                        close()
                    }
                    drawPath(
                        path = wave1,
                        brush = Brush.linearGradient(
                            colors = listOf(FluxWaveOrange.copy(alpha = 0.60f), FluxWavePeach.copy(alpha = 0.45f)),
                            start = Offset(width * 0.4f, 0f),
                            end = Offset(width, height * 0.14f)
                        )
                    )

                    val wave2 = Path().apply {
                        moveTo(0f, height * 0.15f)
                        cubicTo(width * 0.28f, height * 0.12f, width * 0.42f, height * 0.22f, width * 0.35f, height * 0.32f)
                        cubicTo(width * 0.25f, height * 0.38f, width * 0.10f, height * 0.35f, 0f, height * 0.34f)
                        close()
                    }
                    drawPath(
                        path = wave2,
                        brush = Brush.linearGradient(
                            colors = listOf(FluxWavePeach.copy(alpha = 0.50f), FluxWaveOrange.copy(alpha = 0.30f), Color.Transparent),
                            start = Offset(0f, height * 0.20f),
                            end = Offset(width * 0.40f, height * 0.25f)
                        )
                    )

                    val wave3 = Path().apply {
                        moveTo(width, height * 0.38f)
                        cubicTo(width * 0.72f, height * 0.42f, width * 0.58f, height * 0.48f, width * 0.68f, height * 0.58f)
                        cubicTo(width * 0.78f, height * 0.64f, width * 0.90f, height * 0.60f, width, height * 0.58f)
                        close()
                    }
                    drawPath(
                        path = wave3,
                        brush = Brush.linearGradient(
                            colors = listOf(Color.Transparent, FluxWaveOrange.copy(alpha = 0.35f), FluxWavePeach.copy(alpha = 0.45f)),
                            start = Offset(width * 0.60f, height * 0.48f),
                            end = Offset(width, height * 0.54f)
                        )
                    )
                }
            }

            // 3. Fullscreen Kinetic Particle Systems Based on Active Profile
            when {
                isEnv -> {
                    // Fullscreen Procedural Falling Leaves System: Leaves drift, flutter, rotate, and sway downwards
                    BG_LEAVES.forEach { leaf ->
                        val cycleProgress = ((particleProgress * leaf.speed + leaf.phase / (2f * PI.toFloat())) % 1.0f)
                        val totalH = height + 60.dp.toPx()
                        val ly = (cycleProgress * totalH) - 30.dp.toPx()
                        val swayOffset = sin(cycleProgress * leaf.swayFreq * 2f * PI.toFloat() + leaf.phase) * leaf.swayAmpDp.dp.toPx()
                        val lx = (leaf.relX * width + swayOffset).coerceIn(0f, width)

                        // Smooth edge alpha fade so leaves appear & disappear gently
                        val alphaNorm = when {
                            cycleProgress < 0.12f -> cycleProgress / 0.12f
                            cycleProgress > 0.88f -> (1.0f - cycleProgress) / 0.12f
                            else -> 1.0f
                        }
                        val leafAlpha = (alphaNorm * 0.42f).coerceIn(0f, 1f)

                        val leafW = leaf.sizeDp.dp.toPx()
                        val leafH = leafW * 1.55f
                        val leafRot = (cycleProgress * leaf.rotSpeed) % 360f

                        rotate(degrees = leafRot, pivot = Offset(lx, ly)) {
                            val leafPath = Path().apply {
                                moveTo(lx, ly - leafH * 0.5f)
                                cubicTo(
                                    lx + leafW * 0.65f, ly - leafH * 0.25f,
                                    lx + leafW * 0.65f, ly + leafH * 0.25f,
                                    lx, ly + leafH * 0.5f
                                )
                                cubicTo(
                                    lx - leafW * 0.65f, ly + leafH * 0.25f,
                                    lx - leafW * 0.65f, ly - leafH * 0.25f,
                                    lx, ly - leafH * 0.5f
                                )
                                close()
                            }
                            drawPath(
                                path = leafPath,
                                color = leaf.color.copy(alpha = leafAlpha)
                            )
                            drawLine(
                                color = Color(0xFF064E3B).copy(alpha = leafAlpha * 0.45f),
                                start = Offset(lx, ly - leafH * 0.38f),
                                end = Offset(lx, ly + leafH * 0.38f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
                }
                isLiquid -> {
                    // Fluid Liquid Wave Layer at Bottom Screen
                    val waveH = 14.dp.toPx()
                    val waveY = height * 0.82f
                    val wavePath = Path().apply {
                        moveTo(0f, height)
                        lineTo(0f, waveY)
                        for (x in 0..width.toInt() step 16) {
                            val y = waveY + sin((x / width * 2.8f * PI + wavePhase).toFloat()) * waveH
                            lineTo(x.toFloat(), y)
                        }
                        lineTo(width, height)
                        close()
                    }
                    drawPath(
                        path = wavePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.18f), Color(0xFF0284C7).copy(alpha = 0.28f)),
                            startY = waveY - waveH,
                            endY = height
                        )
                    )

                    // Fullscreen Rising Aquatic Bubbles
                    BG_BUBBLES.forEach { bubble ->
                        val cycle = ((particleProgress * bubble.speed + bubble.phase / (2f * PI.toFloat())) % 1.0f)
                        val by = (1.0f - cycle) * (height + 40.dp.toPx()) - 20.dp.toPx()
                        val wobble = sin(cycle * bubble.wobbleFreq * 2f * PI.toFloat() + bubble.phase) * bubble.wobbleAmpDp.dp.toPx()
                        val bx = (bubble.relX * width + wobble).coerceIn(0f, width)
                        val r = bubble.sizeDp.dp.toPx()

                        val bubbleAlpha = when {
                            cycle < 0.12f -> cycle / 0.12f
                            cycle > 0.88f -> (1.0f - cycle) / 0.12f
                            else -> 1.0f
                        } * 0.45f

                        // Aqua bubble outer ring
                        drawCircle(
                            color = Color(0xFF0284C7).copy(alpha = bubbleAlpha),
                            radius = r,
                            center = Offset(bx, by),
                            style = Stroke(1.2.dp.toPx())
                        )
                        // Translucent aqua fill
                        drawCircle(
                            color = Color(0xFFBAE6FD).copy(alpha = bubbleAlpha * 0.5f),
                            radius = r * 0.88f,
                            center = Offset(bx, by)
                        )
                        // Specular shine dot
                        drawCircle(
                            color = Color.White.copy(alpha = bubbleAlpha * 0.85f),
                            radius = r * 0.28f,
                            center = Offset(bx - r * 0.32f, by - r * 0.32f)
                        )
                    }
                }
                isFerm -> {
                    // Fullscreen Rising Effervescent Golden Bubbles with Specular Highlights
                    BG_BUBBLES.forEach { bubble ->
                        val cycle = ((particleProgress * bubble.speed + bubble.phase / (2f * PI.toFloat())) % 1.0f)
                        val by = (1.0f - cycle) * (height + 40.dp.toPx()) - 20.dp.toPx()
                        val wobble = sin(cycle * bubble.wobbleFreq * 2f * PI.toFloat() + bubble.phase) * bubble.wobbleAmpDp.dp.toPx()
                        val bx = (bubble.relX * width + wobble).coerceIn(0f, width)
                        val r = bubble.sizeDp.dp.toPx()

                        val bubbleAlpha = when {
                            cycle < 0.12f -> cycle / 0.12f
                            cycle > 0.88f -> (1.0f - cycle) / 0.12f
                            else -> 1.0f
                        } * 0.50f

                        // Golden amber outer ring
                        drawCircle(
                            color = Color(0xFFF59E0B).copy(alpha = bubbleAlpha),
                            radius = r,
                            center = Offset(bx, by),
                            style = Stroke(1.2.dp.toPx())
                        )
                        // Translucent honey fill
                        drawCircle(
                            color = Color(0xFFFEF3C7).copy(alpha = bubbleAlpha * 0.55f),
                            radius = r * 0.88f,
                            center = Offset(bx, by)
                        )
                        // Specular shine dot
                        drawCircle(
                            color = Color.White.copy(alpha = bubbleAlpha * 0.90f),
                            radius = r * 0.28f,
                            center = Offset(bx - r * 0.32f, by - r * 0.32f)
                        )
                    }

                    // Soft ambient golden mist at base
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xFFF59E0B).copy(alpha = 0.08f)),
                            startY = height * 0.65f,
                            endY = height
                        ),
                        topLeft = Offset(0f, height * 0.65f),
                        size = Size(width, height * 0.35f)
                    )
                }
                else -> {
                    // Subtle Live Monitor Concentric Radar Pulses behind gauge
                    val centerPt = Offset(width * 0.5f, height * 0.25f)
                    val r1 = radarRadiusProgress * width * 0.55f
                    val r2 = ((radarRadiusProgress + 0.5f) % 1.0f) * width * 0.55f

                    drawCircle(
                        color = FluxOrange.copy(alpha = ((1f - radarRadiusProgress) * 0.14f).coerceIn(0f, 1f)),
                        radius = r1,
                        center = centerPt,
                        style = Stroke(1.5.dp.toPx())
                    )
                    drawCircle(
                        color = FluxOrange.copy(alpha = ((1f - ((radarRadiusProgress + 0.5f) % 1.0f)) * 0.10f).coerceIn(0f, 1f)),
                        radius = r2,
                        center = centerPt,
                        style = Stroke(1.2.dp.toPx())
                    )
                }
            }
        }

        content()
    }
}
