package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "alpha",
    )

    LaunchedEffect(true) {
        startAnimation = true
        delay(1800)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GajaColors.SurfaceContainerLow),
            contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.alpha(alphaAnim),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(160.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                GajaColors.PrimaryContainer.copy(alpha = 0.24f),
                                GajaColors.SurfaceContainerLow,
                            ),
                        ),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "gaja",
                    style = MaterialTheme.typography.displayLarge,
                    color = GajaColors.PrimaryContainer,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Surface(
                color = GajaColors.SurfaceContainerHigh,
                shape = MaterialTheme.shapes.small,
            ) {
                Box(modifier = Modifier.width(160.dp).height(6.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.45f)
                            .height(6.dp)
                            .background(Brush.horizontalGradient(GajaColors.BrandGradient), MaterialTheme.shapes.small),
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "주행을 준비하고 있습니다...",
                style = MaterialTheme.typography.bodyMedium,
                color = GajaColors.TextSecondary,
            )
        }
    }
}
