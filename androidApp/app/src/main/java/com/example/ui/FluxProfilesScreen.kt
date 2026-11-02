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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
        delay(50)
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
            // Spacer to clear the fixed top header cleanly
            Spacer(modifier = Modifier.height(72.dp))

            // 1. Back Button & Title Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Color(0x10000000))
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.cardBg)
                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Monitoring Profiles",
                        color = colors.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Select a profile to transform app theme & presets",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Card 1 — Live Monitor (Signature Orange)
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(320, delayMillis = 40)) + slideInVertically(
                    initialOffsetY = { 35 },
                    animationSpec = tween(320, delayMillis = 40, easing = FastOutSlowInEasing)
                )
            ) {
                DynamicProfileThemeCard(
                    title = "Live Monitor",
                    subtitle = "Real-time direct probe telemetry & continuous stream",
                    targetRange = "Continuous Stream",
                    accentColor = FluxOrange,
                    accentGradient = listOf(FluxOrangeGradientStart, FluxOrangeGradientEnd),
                    accentSoft = FluxOrangeSoft,
                    icon = Icons.Filled.Thermostat,
                    imageRes = R.drawable.probe_temp,
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

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Card 2 — Environment (Nature Green)
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(320, delayMillis = 90)) + slideInVertically(
                    initialOffsetY = { 35 },
                    animationSpec = tween(320, delayMillis = 90, easing = FastOutSlowInEasing)
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
                    imageRes = R.drawable.plant_env,
                    isActive = activeProfile == "Environment",
                    animationType = ProfileAnimType.SPORES,
                    onClick = {
                        viewModel.saveActiveProfile("Environment")
                        viewModel.updateThresholds(18.0, 26.0)
                        viewModel.setProfile("Environment")
                        Toast.makeText(context, "Environment Active • Green Theme (18°C - 26°C)", Toast.LENGTH_SHORT).show()
                        onNavigateToHome()
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Card 3 — Liquid (Aquatic Blue)
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(320, delayMillis = 140)) + slideInVertically(
                    initialOffsetY = { 35 },
                    animationSpec = tween(320, delayMillis = 140, easing = FastOutSlowInEasing)
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
                    imageRes = R.drawable.glass_water,
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

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Card 4 — Fermentation (Golden Amber)
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(320, delayMillis = 190)) + slideInVertically(
                    initialOffsetY = { 35 },
                    animationSpec = tween(320, delayMillis = 190, easing = FastOutSlowInEasing)
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
                    imageRes = R.drawable.ferment_jar,
                    isActive = activeProfile == "Fermentation",
                    animationType = ProfileAnimType.FROTH,
                    onClick = {
                        viewModel.saveActiveProfile("Fermentation")
                        viewModel.updateThresholds(18.0, 24.0)
                        viewModel.setProfile("Fermentation")
                        Toast.makeText(context, "Fermentation Active • Amber Theme (18°C - 24°C)", Toast.LENGTH_SHORT).show()
                        onNavigateToHome()
                    }
                )
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

enum class ProfileAnimType {
    RADAR,
    SPORES,
    WAVE,
    FROTH
}

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

    // Breathing pulse for active card
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

    // Dynamic wave phase for animated canvas accent
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .shadow(
                elevation = if (isActive) 6.dp else 2.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = if (isActive) accentColor.copy(alpha = 0.25f) else Color(0x10000000)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .border(
                width = if (isActive) 1.8.dp else 0.5.dp,
                color = if (isActive) accentColor.copy(alpha = activeBorderAlpha) else Color(0xFFEEF2F6),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        // Subtle background dynamic canvas wave in bottom corner
        Canvas(modifier = Modifier.fillMaxWidth().height(110.dp).align(Alignment.BottomCenter)) {
            val w = size.width
            val h = size.height
            val path = Path()
            path.moveTo(0f, h)
            path.lineTo(0f, h * 0.72f)
            val waveHeight = 7.dp.toPx()
            for (x in 0..w.toInt() step 12) {
                val y = h * 0.75f + sin((x / w * 2.5f * PI + wavePhase).toFloat()) * waveHeight
                path.lineTo(x.toFloat(), y)
            }
            path.lineTo(w, h)
            path.close()
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(accentSoft.copy(alpha = 0.45f), accentSoft.copy(alpha = 0.85f))
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Graphic / Image with Accent Badge
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(accentSoft),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(52.dp)
                )

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
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
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(accentColor.copy(alpha = 0.12f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = accentColor,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = subtitle,
                    color = FluxTextMuted,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Target Range Tag with Theme Gradient Accent
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentSoft)
                            .border(0.5.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = targetRange,
                                color = accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tap to apply theme",
                        color = FluxTextLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Arrow Circle Button
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
