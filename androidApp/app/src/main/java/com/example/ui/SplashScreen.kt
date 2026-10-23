package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import com.example.R
import com.example.ui.theme.AccentCyan
import com.example.MainViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.example.util.BiometricHelper
import android.widget.Toast

@Composable
fun SplashScreen(viewModel: MainViewModel, onNavigateToNext: (Boolean) -> Unit) {
    val context = LocalContext.current
    
    LaunchedEffect(key1 = true) {
        delay(2000)
        val isLoggedIn = viewModel.currentUser.value != null
        
        if (isLoggedIn) {
            val activity = context as? FragmentActivity
            if (activity != null && BiometricHelper.isAppLockEnabled(activity) && BiometricHelper.isBiometricAvailable(activity)) {
                BiometricHelper.promptBiometricAuth(
                    activity = activity,
                    title = "App Lock",
                    subtitle = "Unlock Flux using your biometric credentials",
                    onSuccess = {
                        onNavigateToNext(true)
                    },
                    onFailed = {
                        Toast.makeText(activity, "Biometric authentication failed", Toast.LENGTH_SHORT).show()
                    },
                    onError = { err ->
                        Toast.makeText(activity, "Biometric Error: $err", Toast.LENGTH_SHORT).show()
                        onNavigateToNext(true) // Fallback to let them in or you can force exit
                    }
                )
            } else {
                onNavigateToNext(true)
            }
        } else {
            onNavigateToNext(false)
        }
    }

    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(
                        brush = if (darkTheme) {
                            Brush.linearGradient(colors = listOf(Color(0xFF2B2B2D), Color(0xFF1E1E20)))
                        } else {
                            Brush.linearGradient(colors = listOf(Color(0xFFFFFFFF), Color(0xFFF2F2F7)))
                        },
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = if (darkTheme) {
                                listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.02f))
                            } else {
                                listOf(Color.Black.copy(alpha = 0.05f), Color.Transparent)
                            }
                        ),
                        shape = CircleShape
                    )
                    .padding(32.dp)
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_flux_flame),
                    contentDescription = "Flux Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = "Flux",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = com.example.ui.theme.FluxOrangeFlame,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "SMART TELEMETRY SYSTEM",
                style = MaterialTheme.typography.labelMedium,
                color = com.example.ui.theme.TextGrey,
                letterSpacing = 4.sp
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            CircularProgressIndicator(
                color = AccentCyan.copy(alpha = 0.5f),
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
