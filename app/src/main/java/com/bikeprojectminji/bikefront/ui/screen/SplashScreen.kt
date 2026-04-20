package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
                    colors = listOf(GajaColors.Primary, GajaColors.TextPrimary)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.graphicsLayer(alpha = alphaAnim.value)
        ) {
            // GAJA Brand Icon (Bike Pictogram)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                contentDescription = "GAJA Logo",
                modifier = Modifier.size(120.dp),
                tint = GajaColors.LimeAccent
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // GAJA Brand Text
            Text(
                text = "GAJA",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 56.sp,
                    letterSpacing = 6.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            )
            
            Text(
                text = "Ride Your Discovery",
                style = MaterialTheme.typography.labelLarge,
                color = GajaColors.LimeAccent.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}
