package com.bikeprojectminji.bikefront.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2A8CFF),
    onPrimary = Color.White,
    secondary = Color(0xFF132033),
    background = Color(0xFFF4F7FB),
    surface = Color.White,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFE7EEF8),
    onSurfaceVariant = Color(0xFF4B5563)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7AB6FF),
    onPrimary = Color(0xFF05294A),
    secondary = Color(0xFFD8E6FF),
    background = Color(0xFF09111C),
    surface = Color(0xFF101826),
    onSurface = Color(0xFFF3F7FE),
    surfaceVariant = Color(0xFF1B2637),
    onSurfaceVariant = Color(0xFFB4C2D8)
)

@Composable
fun BikeFrontTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
