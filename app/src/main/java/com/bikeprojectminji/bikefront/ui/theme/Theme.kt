package com.bikeprojectminji.bikefront.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography

// ============================================================================
// GAJA "Carbon & Moss" Design System
// Deep, Professional, and High-Contrast
// ============================================================================

object GajaColors {
    // Primary: Deep Forest Moss (Stable & Premium)
    val Primary = Color(0xFF1B4332) 
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFD8F3DC)
    
    // Accent: Electric Lime (Speed & Energy)
    val Accent = Color(0xFF95D5B2)
    val LimeAccent = Color(0xFFB7E4C7)
    
    // Neutrals: Carbon Series (The "Black" base)
    val Carbon = Color(0xFF121212)
    val Graphite = Color(0xFF2D2D2D)
    val White = Color(0xFFFFFFFF)
    val Background = Color(0xFFFBFBFB)
    
    // Text: Absolute Contrast
    val TextPrimary = Color(0xFF000000) 
    val TextSecondary = Color(0xFF2D2D2D)
    val TextTertiary = Color(0xFF5F6661)
    
    // Semantic
    val Success = Color(0xFF2D6A4F)
    val Warning = Color(0xFFFFB703)
    val Error = Color(0xFFD00000)

    // Layout
    val Border = Color(0xFFE0E0E0)
    val Divider = Color(0xFFEEEEEE)
    
    // Brand Gradients
    val BrandGradient = listOf(Color(0xFF000000), Color(0xFF1B4332))
}

private val LightColorScheme = lightColorScheme(
    primary = GajaColors.Primary,
    onPrimary = GajaColors.White,
    primaryContainer = GajaColors.PrimaryContainer,
    onPrimaryContainer = GajaColors.Primary,
    
    secondary = GajaColors.Graphite,
    onSecondary = GajaColors.White,
    
    background = GajaColors.Background,
    onBackground = GajaColors.TextPrimary,
    
    surface = GajaColors.White,
    onSurface = GajaColors.TextPrimary,
    surfaceVariant = GajaColors.Divider,
    onSurfaceVariant = GajaColors.TextSecondary,
    
    outline = GajaColors.Border,
    error = GajaColors.Error,
)

private val DarkColorScheme = darkColorScheme(
    primary = GajaColors.Accent,
    onPrimary = GajaColors.Carbon,
    primaryContainer = GajaColors.Primary,
    
    background = GajaColors.Carbon,
    onBackground = GajaColors.White,
    
    surface = GajaColors.Graphite,
    onSurface = GajaColors.White,
    
    outline = GajaColors.Graphite,
)

private val GajaTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 40.sp,
        letterSpacing = (-1).sp,
        lineHeight = 46.sp,
        color = GajaColors.TextPrimary
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        letterSpacing = (-0.5).sp,
        lineHeight = 38.sp,
        color = GajaColors.TextPrimary
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        color = GajaColors.TextPrimary
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = GajaColors.TextPrimary
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 25.sp,
        color = GajaColors.TextPrimary
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = GajaColors.TextPrimary
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = GajaColors.TextSecondary
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp,
        color = GajaColors.TextPrimary
    )
)

private val GajaShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp),
)

object GajaSpacing {
    val ScreenPadding = 24.dp
    val ItemSpacing = 16.dp
    val Large = 24.dp
    val Medium = 16.dp
    val Small = 8.dp
}

val LocalGajaSpacing = staticCompositionLocalOf { GajaSpacing }

@Composable
fun GajaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = GajaTypography,
        shapes = GajaShapes,
        content = content,
    )
}

@Composable
fun BikeFrontTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) = 
    GajaTheme(darkTheme, content)
