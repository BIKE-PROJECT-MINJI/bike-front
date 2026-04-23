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
// Premium Mobility & Action Design System
// High-contrast, sharp, Lime/Electric Mobility-inspired look
// ============================================================================

object GajaColors {
    // Primary: Electric Green
    val Primary = Color(0xFF00D05A) 
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFE5FAE8)
    
    // Accent: Sleek Dark
    val Accent = Color(0xFF111111)
    val LimeAccent = Color(0xFFF0F0F0)
    val BrightLime = Color(0xFFEAEAEA)
    
    // Neutrals
    val White = Color(0xFFFFFFFF)
    val Background = Color(0xFFF7F7FD) // Pure mobility gray-white
    val Surface = Color(0xFFFFFFFF)
    
    // Text: High Contrast
    val TextPrimary = Color(0xFF111111) // Deep Black
    val TextSecondary = Color(0xFF555555) // Neutral Gray
    val TextTertiary = Color(0xFF888888) // Light Gray
    
    // UI Helpers 
    val Carbon = Color(0xFF1A1A1A) 
    val Graphite = Color(0xFF333333)
    
    // Semantic
    val Success = Color(0xFF00D05A)
    val Warning = Color(0xFFFFB703)
    val Error = Color(0xFFDE3226)

    // Layout
    val Border = Color(0xFFEBEBEB)
    val Divider = Color(0xFFF2F2F2)
    
    // Brand Gradients 
    val BrandGradient = listOf(Color(0xFF00D05A), Color(0xFF009C43))
}

private val LightColorScheme = lightColorScheme(
    primary = GajaColors.Primary,
    onPrimary = GajaColors.White,
    primaryContainer = GajaColors.PrimaryContainer,
    onPrimaryContainer = GajaColors.Primary,
    
    secondary = GajaColors.Accent,
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
    primary = GajaColors.Primary,
    onPrimary = Color(0xFF111111),
    primaryContainer = GajaColors.Carbon,
    
    background = Color(0xFF111111),
    onBackground = GajaColors.White,
    
    surface = Color(0xFF1C1C1E),
    onSurface = GajaColors.White,
    
    outline = Color(0xFF333333),
)

private val GajaTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 36.sp,
        letterSpacing = (-1).sp,
        lineHeight = 42.sp,
        color = Color.Unspecified
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        letterSpacing = (-0.5).sp,
        lineHeight = 34.sp,
        color = Color.Unspecified
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = (-0.2).sp,
        lineHeight = 32.sp,
        color = Color.Unspecified
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        color = Color.Unspecified
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = Color.Unspecified
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = Color.Unspecified
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = Color.Unspecified
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = Color.Unspecified
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        letterSpacing = 0.5.sp,
        color = Color.Unspecified
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp,
        color = Color.Unspecified
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
    val Small = 12.dp
    val Tiny = 8.dp
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
