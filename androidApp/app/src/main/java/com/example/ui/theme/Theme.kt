package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp

private val DarkGlassColorScheme = darkColorScheme(
    primary = AccentCyan,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onBackground = TextWhite,
    onSurface = TextWhite,
    surfaceVariant = DarkSurfaceLight,
    onSurfaceVariant = TextGrey,
    error = DangerRed,
    onError = Color.White
)

private val LightGlassColorScheme = lightColorScheme(
    primary = AccentCyan,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceLight,
    onSurfaceVariant = LightTextSecondary,
    error = DangerRed,
    onError = Color.White
)

fun Modifier.neoShadow(
    color: Color,
    offsetX: Dp,
    offsetY: Dp,
    blurRadius: Dp,
    shapeRadius: Dp = 24.dp
) = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        if (blurRadius != 0.dp) {
            frameworkPaint.maskFilter = android.graphics.BlurMaskFilter(blurRadius.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        frameworkPaint.color = color.toArgb()

        val leftPixel = offsetX.toPx()
        val topPixel = offsetY.toPx()
        val rightPixel = size.width + leftPixel
        val bottomPixel = size.height + topPixel

        canvas.drawRoundRect(
            left = leftPixel,
            top = topPixel,
            right = rightPixel,
            bottom = bottomPixel,
            radiusX = shapeRadius.toPx(),
            radiusY = shapeRadius.toPx(),
            paint = paint
        )
    }
}

@Composable
fun NeoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(vertical = 14.dp, horizontal = 24.dp),
    showShadow: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val bottomShadowColor = if (darkTheme) BottomShadow else LightBottomShadow
    val topHighlightColor = if (darkTheme) TopHighlight else LightTopHighlight
    val bgColor = if (darkTheme) DarkButtonBg else LightButtonBg

    Box(
        modifier = modifier
            .then(
                if (showShadow) {
                    Modifier
                        .neoShadow(color = bottomShadowColor, offsetX = 6.dp, offsetY = 6.dp, blurRadius = 12.dp, shapeRadius = 20.dp)
                        .neoShadow(color = topHighlightColor, offsetX = (-3).dp, offsetY = (-3).dp, blurRadius = 6.dp, shapeRadius = 20.dp)
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .background(if (enabled) bgColor else bgColor.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = if (darkTheme) {
                        listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.02f))
                    } else {
                        listOf(Color.Black.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.03f))
                    }
                ),
                shape = shape
            )
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun NeoIconButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    showShadow: Boolean = true
) {
    NeoButton(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        enabled = enabled,
        contentPadding = PaddingValues(0.dp),
        showShadow = showShadow,
        content = icon
    )
}

@Composable
fun NeoTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        if (onBackClick != null) {
            NeoIconButton(
                onClick = onBackClick,
                icon = {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterStart)
            )
        }
        
        androidx.compose.material3.Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 56.dp)
        )
        
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            actions()
        }
    }
}

fun Modifier.glass(
    shape: Shape = RoundedCornerShape(32.dp),
    borderWidth: androidx.compose.ui.unit.Dp = 0.5.dp
): Modifier = this
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.2f),
                Color.Transparent
            )
        ),
        shape = shape
    )

fun Modifier.neoCard(shape: Shape = RoundedCornerShape(32.dp)): Modifier = composed {
    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val bottomShadowColor = if (darkTheme) BottomShadow else LightBottomShadow
    val topHighlightColor = if (darkTheme) TopHighlight else LightTopHighlight
    
    this
        .neoShadow(color = bottomShadowColor, offsetX = 10.dp, offsetY = 10.dp, blurRadius = 20.dp, shapeRadius = 32.dp)
        .neoShadow(color = topHighlightColor, offsetX = (-4).dp, offsetY = (-4).dp, blurRadius = 12.dp, shapeRadius = 32.dp)
        .clip(shape)
        .background(
            brush = if (darkTheme) {
                Brush.linearGradient(colors = listOf(Color(0xFF2B2B2D), Color(0xFF1E1E20)))
            } else {
                Brush.linearGradient(colors = listOf(Color(0xFFFFFFFF), Color(0xFFEFEFF4)))
            }
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = if (darkTheme) {
                    listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.0f))
                } else {
                    listOf(Color.Black.copy(alpha = 0.16f), Color.Black.copy(alpha = 0.04f))
                },
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            ),
            shape = shape
        )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, 
  dynamicColor: Boolean = false, 
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkGlassColorScheme else LightGlassColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
