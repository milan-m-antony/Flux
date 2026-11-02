package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FluxProfilesScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    val context = LocalContext.current
    val activeProfile by viewModel.activeProfile.collectAsState()
    val colors = rememberFluxColors(darkTheme = false, activeProfile = activeProfile)

    // Staggered card entrance animation triggers
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(40)
        isVisible = true
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            // Clearance below fixed header
            Spacer(modifier = Modifier.height(86.dp))

            // 2. Card 1 — Live Monitor (Signature Orange)
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(300, delayMillis = 30)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = tween(300, delayMillis = 30, easing = FastOutSlowInEasing)
                )
            ) {
                DynamicProfileThemeCard(
                    title = "Live Monitor",
                    subtitle = "Real-time probe telemetry & continuous thermal stream",
                    targetRange = "Continuous Stream",
                    accentColor = FluxOrange,
                    accentGradient = listOf(FluxOrangeGradientStart, FluxOrangeGradientEnd),
                    accentSoft = FluxOrangeSoft,
                    icon = Icons.Filled.Thermostat,
                    imageRes = R.drawable.ic_profile_live,
                    isActive = activeProfile == "Live Monitor",
                    animationType = ProfileAnimType.RADAR,
                    onClick = {
                        viewModel.saveActiveProfile("Live Monitor")
                        viewModel.setProfile("Live Monitor")
                        Toast.makeText(context, "Live Monitor Active • Orange Theme", Toast.LENGTH_SHORT).show()
                        onNavigateToHome()
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Card 2 — Environment (Nature Green with Falling Leaves Effect)
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(300, delayMillis = 80)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = tween(300, delayMillis = 80, easing = FastOutSlowInEasing)
                )
            ) {
                DynamicProfileThemeCard(
                    title = "Environment",
                    subtitle = "Ambient thermal monitoring, botanical grow rooms & climate",
                    targetRange = "18°C – 26°C",
                    accentColor = Color(0xFF10B981),
                    accentGradient = listOf(Color(0xFF059669), Color(0xFF10B981)),
                    accentSoft = Color(0xFFECFDF5),
                    icon = Icons.Filled.Eco,
                    imageRes = R.drawable.ic_profile_env,
                    isActive = activeProfile == "Environment",
                    animationType = ProfileAnimType.LEAVES,
                    onClick = {
                        viewModel.saveActiveProfile("Environment")
                        viewModel.updateThresholds(18.0, 26.0)
                        viewModel.setProfile("Environment")
                        Toast.makeText(context, "Environment Active • Green Theme (18°C - 26°C)", Toast.LENGTH_SHORT).show()
                        onNavigateToHome()
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Card 3 — Liquid (Aquatic Blue with Water Wave Effect)
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(300, delayMillis = 130)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = tween(300, delayMillis = 130, easing = FastOutSlowInEasing)
                )
            ) {
                DynamicProfileThemeCard(
                    title = "Liquid",
                    subtitle = "Submerged probe monitoring, water baths & aquariums",
                    targetRange = "10°C – 35°C",
                    accentColor = Color(0xFF0284C7),
                    accentGradient = listOf(Color(0xFF0284C7), Color(0xFF38BDF8)),
                    accentSoft = Color(0xFFF0F9FF),
                    icon = Icons.Filled.WaterDrop,
                    imageRes = R.drawable.ic_profile_liquid,
                    isActive = activeProfile == "Liquid",
                    animationType = ProfileAnimType.WAVE,
                    onClick = {
                        viewModel.saveActiveProfile("Liquid")
                        viewModel.updateThresholds(10.0, 35.0)
                        viewModel.setProfile("Liquid")
                        Toast.makeText(context, "Liquid Active • Cyan Theme (10°C - 35°C)", Toast.LENGTH_SHORT).show()
                        onNavigateToHome()
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Card 4 — Fermentation (Golden Amber with Rising Effervescence)
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(300, delayMillis = 180)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = tween(300, delayMillis = 180, easing = FastOutSlowInEasing)
                )
            ) {
                DynamicProfileThemeCard(
                    title = "Fermentation",
                    subtitle = "Precision brew, active yeast & sourdough vessel tracking",
                    targetRange = "18°C – 24°C",
                    accentColor = Color(0xFFD97706),
                    accentGradient = listOf(Color(0xFFB45309), Color(0xFFF59E0B)),
                    accentSoft = Color(0xFFFFFBEB),
                    icon = Icons.Filled.Science,
                    imageRes = R.drawable.ic_profile_ferment,
                    isActive = activeProfile == "Fermentation",
                    animationType = ProfileAnimType.FIZZ,
                    onClick = {
                        viewModel.saveActiveProfile("Fermentation")
                        viewModel.updateThresholds(18.0, 24.0)
                        viewModel.setProfile("Fermentation")
                        Toast.makeText(context, "Fermentation Active • Amber Theme (18°C - 24°C)", Toast.LENGTH_SHORT).show()
                        onNavigateToHome()
                    }
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

enum class ProfileAnimType {
    RADAR,
    LEAVES,
    WAVE,
    FIZZ
}

// Falling leaf specification for procedural particle system
private data class FallingLeafSpec(
    val initialX: Float,
    val speed: Float,
    val swayAmp: Float,
    val swayFreq: Float,
    val phase: Float,
    val sizeDp: Float,
    val rotSpeed: Float,
    val color: Color
)

private val LEAF_SPECS = listOf(
    FallingLeafSpec(0.12f, 0.85f, 18f, 2.2f, 0.4f, 11f, 180f, Color(0xFF10B981)),
    FallingLeafSpec(0.28f, 1.15f, 24f, 1.8f, 1.9f, 14f, -220f, Color(0xFF059669)),
    FallingLeafSpec(0.45f, 0.70f, 14f, 2.5f, 3.2f, 9f, 260f, Color(0xFF34D399)),
    FallingLeafSpec(0.62f, 1.00f, 20f, 2.0f, 4.5f, 13f, -190f, Color(0xFF10B981)),
    FallingLeafSpec(0.78f, 1.25f, 22f, 1.6f, 0.8f, 15f, 240f, Color(0xFF047857)),
    FallingLeafSpec(0.90f, 0.90f, 16f, 2.3f, 2.7f, 10f, -170f, Color(0xFF6EE7B7)),
    FallingLeafSpec(0.35f, 1.05f, 26f, 1.7f, 5.1f, 12f, 210f, Color(0xFF10B981)),
    FallingLeafSpec(0.72f, 0.80f, 15f, 2.4f, 1.2f, 8f, -250f, Color(0xFF34D399))
)

// Rising effervescence bubble specification
private data class FizzBubbleSpec(
    val relX: Float,
    val speed: Float,
    val sizeDp: Float,
    val wobbleFreq: Float,
    val phase: Float
)

private val FIZZ_SPECS = listOf(
    FizzBubbleSpec(0.18f, 1.0f, 4.5f, 3.0f, 0.2f),
    FizzBubbleSpec(0.26f, 1.4f, 3.0f, 4.0f, 1.7f),
    FizzBubbleSpec(0.38f, 0.8f, 5.5f, 2.5f, 3.1f),
    FizzBubbleSpec(0.52f, 1.2f, 3.5f, 3.5f, 4.8f),
    FizzBubbleSpec(0.67f, 0.9f, 5.0f, 3.0f, 0.9f),
    FizzBubbleSpec(0.82f, 1.5f, 2.8f, 4.5f, 2.4f),
    FizzBubbleSpec(0.91f, 1.1f, 4.0f, 3.2f, 5.2f)
)

// Aqua droplet specification
private data class AquaDropletSpec(
    val relX: Float,
    val speed: Float,
    val radiusDp: Float,
    val phase: Float
)

private val DROPLET_SPECS = listOf(
    AquaDropletSpec(0.15f, 0.9f, 3.0f, 0.5f),
    AquaDropletSpec(0.32f, 1.2f, 2.2f, 2.1f),
    AquaDropletSpec(0.55f, 0.8f, 3.8f, 4.3f),
    AquaDropletSpec(0.74f, 1.1f, 2.6f, 1.2f),
    AquaDropletSpec(0.88f, 0.7f, 3.2f, 3.7f)
)

@Composable
private fun DynamicProfileThemeCard(
    title: String,
    subtitle: String,
    targetRange: String,
    accentColor: Color,
    accentGradient: List<Color>,
    accentSoft: Color,
    icon: ImageVector,
    imageRes: Int,
    isActive: Boolean,
    animationType: ProfileAnimType,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "profileCardScale"
    )

    // Breathing pulse for active card border and aura
    val infiniteTransition = rememberInfiniteTransition(label = "profilePulse")
    val activeBorderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "activeBorderAlpha"
    )

    // Dynamic wave phase for liquid wave effect
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    // Continuous progress cycle for falling leaves and rising bubbles
    val particleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleProgress"
    )

    // Radar rotation angle for live monitor
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAngle"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .shadow(
                elevation = if (isActive) 5.dp else 2.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = if (isActive) accentColor.copy(alpha = 0.25f) else Color(0x10000000)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(
                width = if (isActive) 1.6.dp else 0.5.dp,
                color = if (isActive) accentColor.copy(alpha = activeBorderAlpha) else Color(0xFFEEF2F6),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        // High-Quality Kinetic Particle Canvas layer rendered right on the card
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            when (animationType) {
                ProfileAnimType.LEAVES -> {
                    // Procedural Falling Leaves System: Leaves drift, flutter, rotate, and sway downwards
                    LEAF_SPECS.forEach { leaf ->
                        val cycleProgress = ((particleProgress * leaf.speed + leaf.phase / (2f * PI.toFloat())) % 1.0f)
                        val totalH = h + 40.dp.toPx()
                        val ly = (cycleProgress * totalH) - 20.dp.toPx()
                        val swayOffset = sin(cycleProgress * leaf.swayFreq * 2f * PI.toFloat() + leaf.phase) * leaf.swayAmp.dp.toPx()
                        val lx = leaf.initialX * w + swayOffset

                        // Smooth alpha fade at top and bottom edges
                        val alphaNorm = when {
                            cycleProgress < 0.15f -> cycleProgress / 0.15f
                            cycleProgress > 0.85f -> (1.0f - cycleProgress) / 0.15f
                            else -> 1.0f
                        }
                        val leafAlpha = (alphaNorm * 0.72f).coerceIn(0f, 1f)

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
                            // Delicate central vein
                            drawLine(
                                color = Color(0xFF064E3B).copy(alpha = leafAlpha * 0.5f),
                                start = Offset(lx, ly - leafH * 0.38f),
                                end = Offset(lx, ly + leafH * 0.38f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
                }

                ProfileAnimType.WAVE -> {
                    // Layer 1: Back secondary wave
                    val backPath = Path()
                    backPath.moveTo(0f, h)
                    backPath.lineTo(0f, h * 0.74f)
                    val backWaveH = 6.dp.toPx()
                    for (x in 0..w.toInt() step 12) {
                        val y = h * 0.78f + sin((x / w * 2.2f * PI - wavePhase * 0.8f).toFloat()) * backWaveH
                        backPath.lineTo(x.toFloat(), y)
                    }
                    backPath.lineTo(w, h)
                    backPath.close()
                    drawPath(
                        path = backPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.15f), Color(0xFF0284C7).copy(alpha = 0.25f))
                        )
                    )

                    // Layer 2: Front primary wave
                    val frontPath = Path()
                    frontPath.moveTo(0f, h)
                    frontPath.lineTo(0f, h * 0.70f)
                    val frontWaveH = 8.dp.toPx()
                    for (x in 0..w.toInt() step 12) {
                        val y = h * 0.74f + sin((x / w * 2.8f * PI + wavePhase).toFloat()) * frontWaveH
                        frontPath.lineTo(x.toFloat(), y)
                    }
                    frontPath.lineTo(w, h)
                    frontPath.close()
                    drawPath(
                        path = frontPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(accentSoft.copy(alpha = 0.45f), Color(0xFF38BDF8).copy(alpha = 0.4f))
                        )
                    )

                    // Rising aqua droplets
                    DROPLET_SPECS.forEach { drop ->
                        val cycle = ((particleProgress * drop.speed + drop.phase / (2f * PI.toFloat())) % 1.0f)
                        val dy = h * 0.85f - (cycle * h * 0.65f)
                        val dx = drop.relX * w + sin(cycle * 3f * PI.toFloat() + drop.phase) * 6.dp.toPx()
                        val dropAlpha = ((1f - cycle) * 0.65f).coerceIn(0f, 1f)
                        drawCircle(
                            color = Color(0xFF38BDF8).copy(alpha = dropAlpha),
                            radius = drop.radiusDp.dp.toPx(),
                            center = Offset(dx, dy)
                        )
                    }
                }

                ProfileAnimType.FIZZ -> {
                    // Rising effervescent golden bubbles with specular highlights
                    FIZZ_SPECS.forEach { fizz ->
                        val cycle = ((particleProgress * fizz.speed + fizz.phase / (2f * PI.toFloat())) % 1.0f)
                        val by = (1.0f - cycle) * (h + 20.dp.toPx()) - 10.dp.toPx()
                        val wobble = sin(cycle * fizz.wobbleFreq * 2f * PI.toFloat() + fizz.phase) * 8.dp.toPx()
                        val bx = fizz.relX * w + wobble
                        val r = fizz.sizeDp.dp.toPx()

                        val bubbleAlpha = when {
                            cycle < 0.1f -> cycle / 0.1f
                            cycle > 0.85f -> (1.0f - cycle) / 0.15f
                            else -> 1.0f
                        } * 0.75f

                        // Outer amber ring
                        drawCircle(
                            color = Color(0xFFF59E0B).copy(alpha = bubbleAlpha),
                            radius = r,
                            center = Offset(bx, by),
                            style = Stroke(1.2.dp.toPx())
                        )
                        // Translucent golden fill
                        drawCircle(
                            color = Color(0xFFFEF3C7).copy(alpha = bubbleAlpha * 0.4f),
                            radius = r * 0.9f,
                            center = Offset(bx, by)
                        )
                        // Specular shine dot
                        drawCircle(
                            color = Color.White.copy(alpha = bubbleAlpha * 0.85f),
                            radius = r * 0.28f,
                            center = Offset(bx - r * 0.32f, by - r * 0.32f)
                        )
                    }

                    // Soft ambient golden mist at base
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xFFF59E0B).copy(alpha = 0.08f)),
                            startY = h * 0.5f,
                            endY = h
                        ),
                        topLeft = Offset(0f, h * 0.5f),
                        size = Size(w, h * 0.5f)
                    )
                }

                ProfileAnimType.RADAR -> {
                    // Expanding concentric telemetry pulses
                    val probeCenter = Offset(w * 0.14f, h * 0.5f)
                    val ringProgress1 = particleProgress
                    val ringProgress2 = (particleProgress + 0.5f) % 1.0f

                    drawCircle(
                        color = FluxOrange.copy(alpha = ((1f - ringProgress1) * 0.28f).coerceIn(0f, 1f)),
                        radius = ringProgress1 * 90.dp.toPx(),
                        center = probeCenter,
                        style = Stroke(1.5.dp.toPx())
                    )
                    drawCircle(
                        color = FluxOrange.copy(alpha = ((1f - ringProgress2) * 0.22f).coerceIn(0f, 1f)),
                        radius = ringProgress2 * 90.dp.toPx(),
                        center = probeCenter,
                        style = Stroke(1.2.dp.toPx())
                    )

                    // Sweeping radar arc beam
                    rotate(degrees = radarAngle, pivot = probeCenter) {
                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(FluxOrange.copy(alpha = 0.45f), Color.Transparent),
                                start = probeCenter,
                                end = Offset(probeCenter.x + 85.dp.toPx(), probeCenter.y)
                            ),
                            start = probeCenter,
                            end = Offset(probeCenter.x + 85.dp.toPx(), probeCenter.y),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
            }
        }

        // Foreground Card Content Layout (Enlarged prominent proportions)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Floating High-Quality Transparent Image with Soft Glow
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                // Ambient soft radial glow aura
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    accentSoft.copy(alpha = 0.95f),
                                    accentColor.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // High-Quality Transparent PNG Graphic
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(62.dp)
                        .scale(if (isActive) 1.05f else 1.0f)
                )

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(1.dp)
                            .size(20.dp)
                            .shadow(2.dp, CircleShape, spotColor = accentColor)
                            .clip(CircleShape)
                            .background(accentColor)
                            .border(1.2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Information Column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = FluxTextDark,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.width(7.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentColor.copy(alpha = 0.12f))
                                .border(0.5.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = accentColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    color = FluxTextMuted,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Target Range Tag with Theme Gradient Accent
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentSoft)
                        .border(0.5.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 3.5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = targetRange,
                            color = accentColor,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Arrow Circle Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isActive) accentColor else Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Apply Theme",
                    tint = if (isActive) Color.White else FluxTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
