package com.tarantino.xonarx.presentation.ambient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tarantino.xonarx.presentation.theme.futuristic.AnimatedGradientBackdrop
import com.tarantino.xonarx.presentation.theme.futuristic.FrostedGlassSurface
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AmbientModeScreen provides an immersive visual experience for docked or charging states.
 * It leverages the futuristic theme system to display a calming, ambient clock with live stats.
 */
@Composable
fun AmbientModeScreen(
    onExit: () -> Unit
) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Immersive animated backdrop for the ambient mode
        AnimatedGradientBackdrop(
            colors = listOf(
                Color(0xFF101E42),
                Color(0xFF2E1A47),
                Color(0xFF0F2027)
            )
        )

        // Exit button
        IconButton(
            onClick = onExit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Exit Ambient Mode",
                tint = Color.White.copy(alpha = 0.5f)
            )
        }

        // Central floating clock
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            FrostedGlassSurface(
                shape = RoundedCornerShape(32.dp),
                containerColor = Color.White.copy(alpha = 0.1f),
                borderColor = Color.White.copy(alpha = 0.05f),
                blurRadius = 32.dp,
                modifier = Modifier.padding(32.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(64.dp)
                ) {
                    Text(
                        text = timeFormat.format(Date(currentTime)),
                        fontSize = 96.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = dateFormat.format(Date(currentTime)),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    FrostedGlassSurface(
                        shape = RoundedCornerShape(16.dp),
                        containerColor = Color.Black.copy(alpha = 0.2f),
                        borderColor = Color.Transparent,
                        blurRadius = 16.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BatteryChargingFull,
                                contentDescription = "Charging",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ambient Mode Actively Charging",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }
    }
}
