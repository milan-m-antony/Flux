package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.MainViewModel
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun FluxProfilesScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onNavigateToLiveMonitor: () -> Unit = {},
    onNavigateToEnvironment: () -> Unit = {},
    onNavigateToLiquid: () -> Unit = {},
    onNavigateToFermentation: () -> Unit = {}
) {
    val context = LocalContext.current
    val activeProfile by viewModel.activeProfile.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val colors = rememberFluxColors(isDarkTheme)

    // Staggered card entrance animation triggers
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(60)
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
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Select an optimized telemetry preset",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Card 1 — Live Monitor (Direct Temperature Probe)
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(350, delayMillis = 50)) + slideInVertically(
                    initialOffsetY = { 40 },
                    animationSpec = tween(350, delayMillis = 50, easing = FastOutSlowInEasing)
                )
            ) {
                ProfileCardItem(
                    title = "Live Monitor",
                    subtitle = "Real-time direct probe telemetry",
                    targetRange = "Continuous Stream",
                    imageRes = R.drawable.probe_temp,
                    colors = colors,
                    isActive = activeProfile == "Live Monitor",
                    onClick = {
                        viewModel.saveActiveProfile("Live Monitor")
                        Toast.makeText(context, "Monitoring mode: Live Monitor", Toast.LENGTH_SHORT).show()
                        onNavigateToLiveMonitor()
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Card 2 — Environment (Ambient Green Plant)
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(350, delayMillis = 120)) + slideInVertically(
                    initialOffsetY = { 40 },
                    animationSpec = tween(350, delayMillis = 120, easing = FastOutSlowInEasing)
                )
            ) {
                ProfileCardItem(
                    title = "Environment",
                    subtitle = "Ambient thermal monitoring & grow rooms",
                    targetRange = "18°C – 26°C",
                    imageRes = R.drawable.plant_env,
                    colors = colors,
                    isActive = activeProfile == "Environment",
                    onClick = {
                        viewModel.saveActiveProfile("Environment")
                        viewModel.updateThresholds(18.0, 26.0)
                        Toast.makeText(context, "Environment Preset Active (18°C - 26°C)", Toast.LENGTH_SHORT).show()
                        onNavigateToEnvironment()
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Card 3 — Liquid (Submerged Glass Probe)
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(350, delayMillis = 190)) + slideInVertically(
                    initialOffsetY = { 40 },
                    animationSpec = tween(350, delayMillis = 190, easing = FastOutSlowInEasing)
                )
            ) {
                ProfileCardItem(
                    title = "Liquid",
                    subtitle = "Water baths, aquariums & liquid tanks",
                    targetRange = "10°C – 35°C",
                    imageRes = R.drawable.glass_water,
                    colors = colors,
                    isActive = activeProfile == "Liquid",
                    onClick = {
                        viewModel.saveActiveProfile("Liquid")
                        viewModel.updateThresholds(10.0, 35.0)
                        Toast.makeText(context, "Liquid Preset Active (10°C - 35°C)", Toast.LENGTH_SHORT).show()
                        onNavigateToLiquid()
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Card 4 — Fermentation (Vessel & Dough Jar)
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(350, delayMillis = 260)) + slideInVertically(
                    initialOffsetY = { 40 },
                    animationSpec = tween(350, delayMillis = 260, easing = FastOutSlowInEasing)
                )
            ) {
                ProfileCardItem(
                    title = "Fermentation",
                    subtitle = "Precision brew, yeast & sourdough vessels",
                    targetRange = "18°C – 24°C",
                    imageRes = R.drawable.ferment_jar,
                    colors = colors,
                    isActive = activeProfile == "Fermentation",
                    onClick = {
                        viewModel.saveActiveProfile("Fermentation")
                        viewModel.updateThresholds(18.0, 24.0)
                        Toast.makeText(context, "Fermentation Preset Active (18°C - 24°C)", Toast.LENGTH_SHORT).show()
                        onNavigateToFermentation()
                    }
                )
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
private fun ProfileCardItem(
    title: String,
    subtitle: String,
    targetRange: String,
    imageRes: Int,
    colors: FluxColors,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth subtle press bounce & active breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseBorderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseBorderAlpha"
    )

    val activeScalePulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.012f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "activeScalePulse"
    )

    val targetScale = when {
        isPressed -> 0.985f
        isActive -> activeScalePulse
        else -> 1.0f
    }
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "animatedScale"
    )

    // Signature light gradients per profile
    val gradientColors = when (title) {
        "Live Monitor" -> listOf(Color(0xFFFFF9F5), Color(0xFFFFE8DC))
        "Environment" -> listOf(Color(0xFFF7FDF9), Color(0xFFDCFCE7))
        "Liquid" -> listOf(Color(0xFFF8FAFC), Color(0xFFDBEAFE))
        else -> listOf(Color(0xFFFFFDF8), Color(0xFFFFEDD5))
    }

    val activeBorderColor = FluxOrange.copy(alpha = if (isActive) pulseBorderAlpha else 0.4f)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale)
            .shadow(
                elevation = if (isActive) 8.dp else 3.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = if (isActive) FluxOrange.copy(alpha = 0.22f) else Color(0x10000000)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(brush = Brush.horizontalGradient(colors = gradientColors))
            .border(
                width = if (isActive) 1.8.dp else 0.6.dp,
                color = if (isActive) activeBorderColor else Color(0xFFEEF2F6),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        val isNarrow = maxWidth < 360.dp
        val imageSize = if (isNarrow) 78.dp else 88.dp
        val horizontalPad = if (isNarrow) 14.dp else 18.dp
        val cardHeight = if (isNarrow) 116.dp else 126.dp

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .padding(horizontal = horizontalPad, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Image Box with soft shadow
            Box(
                modifier = Modifier
                    .size(imageSize)
                    .shadow(3.dp, RoundedCornerShape(18.dp), spotColor = Color(0x12000000))
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(if (isNarrow) 12.dp else 16.dp))

            // Center Content (Title, Subtitle & Target Range Tag)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        color = colors.textPrimary,
                        fontSize = if (isNarrow) 17.sp else 19.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(FluxOrange.copy(alpha = 0.15f))
                                .border(0.5.dp, FluxOrange.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = FluxOrange,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "ACTIVE",
                                    color = FluxOrange,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = subtitle,
                    color = colors.textSecondary,
                    fontSize = if (isNarrow) 10.5.sp else 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(5.dp))

                // Target Range Badge Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.75f))
                        .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = targetRange,
                        color = colors.textPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Chevron with subtle active translation bounce
            val chevronOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = if (isActive) 3f else 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "chevronBounce"
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View Profile",
                tint = if (isActive) FluxOrange else colors.textSecondary,
                modifier = Modifier
                    .size(22.dp)
                    .offset(x = chevronOffset.dp)
            )
        }
    }
}
