package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GajaColors.LimeAccent,
                        GajaColors.BrandGradient.first(),
                        GajaColors.BrandGradient.last(),
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.graphicsLayer { alpha = alphaAnim.value }
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = GajaColors.White.copy(alpha = 0.86f),
                border = BorderStroke(1.dp, GajaColors.White.copy(alpha = 0.55f)),
                shadowElevation = 10.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                        contentDescription = "gaja 로고",
                        modifier = Modifier.size(52.dp),
                        tint = GajaColors.Accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "gaja",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 44.sp,
                    letterSpacing = (-1.2).sp,
                    fontWeight = FontWeight.Bold,
                    color = GajaColors.TextPrimary
                )
            )

            Text(
                text = "가볍게 떠나는 자전거 이동",
                style = MaterialTheme.typography.labelLarge,
                color = GajaColors.TextSecondary,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}
