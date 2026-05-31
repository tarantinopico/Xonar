package com.tarantino.xonarx.presentation.theme.futuristic

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A reusable component for creating frosted glass / blurred surfaces.
 * Uses a hardware accelerated blur on modern devices, with a fallback semi-transparent color on older ones.
 */
@Composable
fun FrostedGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape,
    blurRadius: Dp = 16.dp,
    containerColor: Color = Color.White.copy(alpha = 0.4f),
    borderColor: Color = Color.White.copy(alpha = 0.2f),
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val needsFallback = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
    
    val baseModifier = if (!needsFallback) {
        modifier
            .clip(shape)
            .blur(blurRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded)
    } else {
        modifier.clip(shape)
    }

    Box(
        modifier = baseModifier
            .background(containerColor)
            .border(borderWidth, borderColor, shape)
    ) {
        content()
    }
}
