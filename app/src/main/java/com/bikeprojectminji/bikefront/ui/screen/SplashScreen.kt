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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    var rawProgress by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("앱 시작 중") }
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 420),
        label = "splash-progress",
    )

    LaunchedEffect(Unit) {
        val steps = listOf(
            0.12f to "앱 시작 중",
            0.38f to "사용자 정보 확인 중",
            0.72f to "홈 화면 준비 중",
            1f to "곧 시작합니다",
        )
        steps.forEachIndexed { index, (progress, message) ->
            statusText = message
            rawProgress = progress
            delay(if (index == steps.lastIndex) 260 else 360)
        }
        delay(220)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GajaColors.SurfaceContainerLow),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(168.dp)
                    .height(168.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                GajaColors.PrimaryContainer.copy(alpha = 0.22f),
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
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .width(180.dp)
                    .height(8.dp),
                color = GajaColors.PrimaryContainer,
                trackColor = GajaColors.SurfaceContainerHigh,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = GajaColors.TextSecondary,
            )
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = GajaColors.TextTertiary,
            )
        }
    }
}
