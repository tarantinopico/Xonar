package com.tarantino.xonarx.presentation.theme.futuristic

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor

/**
 * A beautiful animated gradient backdrop representing a live, breathing visual style.
 * Uses an infinite transition to slowly pan and rotate a multi-stop brush algorithm.
 * 
 * @param colors The colors to include in the gradient.
 * @param modifier The modifier to apply to the drawing canvas.
 */
@Composable
fun AnimatedGradientBackdrop(
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    if (colors.isEmpty()) {
        Canvas(modifier = modifier.fillMaxSize()) {}
        return
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "GradientMotion")
    
    val xOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "xOffset"
    )

    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "yOffset"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val brush = if (colors.size == 1) {
            SolidColor(colors.first())
        } else {
            Brush.linearGradient(
                colors = colors,
                start = Offset(xOffset * width * 0.2f, yOffset * height * 0.2f),
                end = Offset(width * (0.8f + xOffset * 0.2f), height * (0.8f + yOffset * 0.2f))
            )
        }

        drawRect(brush = brush)
    }
}
