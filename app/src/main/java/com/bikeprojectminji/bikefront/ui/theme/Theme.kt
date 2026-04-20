package com.bikeprojectminji.bikefront.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object GajaColors {
    val Primary = Color(0xFF994700)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFFF8224)
    val Accent = Color(0xFFFFB68B)
    val OnPrimaryContainer = Color(0xFF622B00)

    val Secondary = Color(0xFF89502C)
    val SecondaryContainer = Color(0xFFFDB386)
    val Tertiary = Color(0xFF00658B)
    val TertiaryContainer = Color(0xFF00B0EF)

    val Background = Color(0xFFF7F9FC)
    val Surface = Color(0xFFF7F9FC)
    val SurfaceBright = Color(0xFFF7F9FC)
    val SurfaceDim = Color(0xFFD8DADD)
    val SurfaceContainerLow = Color(0xFFF2F4F7)
    val SurfaceContainer = Color(0xFFECEEF1)
    val SurfaceContainerHigh = Color(0xFFE6E8EB)
    val SurfaceContainerHighest = Color(0xFFE0E3E6)

    val White = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF191C1E)
    val TextSecondary = Color(0xFF574236)
    val TextTertiary = Color(0xFF8B7264)

    val Success = Color(0xFF0C7D45)
    val Warning = Color(0xFFFFB703)
    val Error = Color(0xFFBA1A1A)
    val ErrorContainer = Color(0xFFFFDAD6)

    val Border = Color(0xFFDEC1B1)
    val Divider = Color(0xFFE0E3E6)
    val BrandGradient = listOf(Color(0xFF994700), Color(0xFFFF8224))
    val DarkGradient = listOf(Color(0xFF2D3133), Color(0xFF994700))
}

private val LightColorScheme = lightColorScheme(
    primary = GajaColors.Primary,
    onPrimary = GajaColors.OnPrimary,
    primaryContainer = GajaColors.PrimaryContainer,
    onPrimaryContainer = GajaColors.OnPrimaryContainer,
    secondary = GajaColors.Secondary,
    onSecondary = GajaColors.White,
    secondaryContainer = GajaColors.SecondaryContainer,
    background = GajaColors.Background,
    onBackground = GajaColors.TextPrimary,
    surface = GajaColors.Surface,
    onSurface = GajaColors.TextPrimary,
    surfaceVariant = GajaColors.SurfaceContainerHighest,
    onSurfaceVariant = GajaColors.TextSecondary,
    outline = GajaColors.Border,
    error = GajaColors.Error,
    onError = GajaColors.White,
    errorContainer = GajaColors.ErrorContainer,
    onErrorContainer = GajaColors.Error,
)

private val DarkColorScheme = darkColorScheme(
    primary = GajaColors.PrimaryContainer,
    onPrimary = GajaColors.TextPrimary,
    primaryContainer = GajaColors.Primary,
    onPrimaryContainer = GajaColors.White,
    secondary = GajaColors.SecondaryContainer,
    onSecondary = GajaColors.TextPrimary,
    background = Color(0xFF191C1E),
    onBackground = GajaColors.White,
    surface = Color(0xFF222527),
    onSurface = GajaColors.White,
    surfaceVariant = Color(0xFF2D3133),
    onSurfaceVariant = Color(0xFFE0E3E6),
    outline = Color(0xFF574236),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val GajaTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1).sp,
        color = GajaColors.TextPrimary,
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp,
        color = GajaColors.TextPrimary,
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        color = GajaColors.TextPrimary,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = GajaColors.TextPrimary,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 24.sp,
        color = GajaColors.TextPrimary,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = GajaColors.TextPrimary,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = GajaColors.TextPrimary,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = GajaColors.TextSecondary,
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        color = GajaColors.TextSecondary,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp,
        color = GajaColors.TextPrimary,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp,
        color = GajaColors.TextSecondary,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.7.sp,
        color = GajaColors.TextSecondary,
    ),
)

private val GajaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

object GajaSpacing {
    val ScreenPadding = 24.dp
    val Large = 24.dp
    val Medium = 16.dp
    val ItemSpacing = 16.dp
    val Small = 8.dp
    val Tiny = 4.dp
    val SectionGap = 32.dp
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
fun BikeFrontTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) = GajaTheme(darkTheme, content)
