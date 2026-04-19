package com.bikeprojectminji.bikefront.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
// GAJA Material 3 Design System
// A premium, cohesive design system built on Material 3 foundations
// ============================================================================

// ============================================================================
// SECTION 1: COLOR TOKENS
// Brand identity: graphite cockpit with lime and amber accents
// ============================================================================

/** Primary brand colors - The core GAJA identity */
object GajaColors {
    // Primary accents
    val LimePrimary = Color(0xFF7EE2C2)
    val LimeLight = Color(0xFFB8F2E1)
    val LimeDark = Color(0xFF24564C)
    val LimeSurface = Color(0xFFDDF4EC)

    // Background & Surfaces
    val Background = Color(0xFFF5F3ED)
    val Surface = Color(0xFFFFFCF8)
    val SurfaceVariant = Color(0xFFE7E4DB)
    val SurfaceContainer = Color(0xFFF1ECE3)

    // Text
    val TextPrimary = Color(0xFF171611)
    val TextSecondary = Color(0xFF5F594D)
    val TextTertiary = Color(0xFF938C7E)
    val TextInverse = Color(0xFFFFFFFF)

    // Semantic
    val Success = Color(0xFF5F8D2D)
    val Warning = Color(0xFFD7842D)
    val Error = Color(0xFFB84D37)
    val Info = Color(0xFF446E8D)

    // UI Elements
    val Divider = Color(0xFFD8D0C3)
    val Outline = Color(0xFFC8BEAE)
    val OutlineVariant = Color(0xFFDED6CA)

    // Gradients
    val GradientStart = LimePrimary
    val GradientEnd = Color(0xFF2C5A4F)
}

/** Light color scheme - Primary theme for GAJA app */
private val LightColorScheme = lightColorScheme(
    primary = GajaColors.LimePrimary,
    onPrimary = GajaColors.TextPrimary,
    primaryContainer = Color(0xFFD8F4EB),
    onPrimaryContainer = GajaColors.TextPrimary,
    
    secondary = GajaColors.LimeDark,
    onSecondary = GajaColors.TextInverse,
    secondaryContainer = GajaColors.LimeSurface,
    onSecondaryContainer = GajaColors.TextPrimary,
    
    tertiary = Color(0xFFC69A5F),
    onTertiary = GajaColors.TextPrimary,
    tertiaryContainer = Color(0xFFF1E2CB),
    onTertiaryContainer = GajaColors.TextPrimary,
    
    background = GajaColors.Background,
    onBackground = GajaColors.TextPrimary,
    
    surface = GajaColors.Surface,
    onSurface = GajaColors.TextPrimary,
    
    surfaceVariant = GajaColors.SurfaceVariant,
    onSurfaceVariant = GajaColors.TextSecondary,
    
    surfaceContainer = GajaColors.SurfaceContainer,
    surfaceContainerHigh = Color(0xFFEDE7DD),
    
    outline = GajaColors.Outline,
    outlineVariant = GajaColors.OutlineVariant,
    
    error = GajaColors.Error,
    onError = GajaColors.TextInverse,
    errorContainer = Color(0xFFF7DAD4),
    onErrorContainer = Color(0xFF410E0E),
    
    inverseSurface = GajaColors.TextPrimary,
    inverseOnSurface = GajaColors.Surface,
    inversePrimary = GajaColors.LimeLight,
    
    scrim = Color(0xFF000000),
)

/** Dark color scheme - Dark mode support */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8BE8CB),
    onPrimary = Color(0xFF10201C),
    primaryContainer = Color(0xFF17312B),
    onPrimaryContainer = Color(0xFFD4F8EE),

    secondary = Color(0xFF9CC8B9),
    onSecondary = Color(0xFF11201B),
    secondaryContainer = Color(0xFF203631),
    onSecondaryContainer = Color(0xFFD5ECE5),

    tertiary = Color(0xFFD5AE78),
    onTertiary = Color(0xFF23170A),
    tertiaryContainer = Color(0xFF4E3921),
    onTertiaryContainer = Color(0xFFF8DFC1),

    background = Color(0xFF0C1614),
    onBackground = GajaColors.TextInverse,

    surface = Color(0xFF111D1A),
    onSurface = GajaColors.TextInverse,

    surfaceVariant = Color(0xFF1A2A26),
    onSurfaceVariant = Color(0xFFB7C8C1),

    surfaceContainer = Color(0xFF152420),
    surfaceContainerHigh = Color(0xFF1C2F2A),

    outline = Color(0xFF385049),
    outlineVariant = Color(0xFF243530),

    error = Color(0xFFFF8A72),
    onError = Color(0xFF3B0800),
    errorContainer = Color(0xFF5A170A),
    onErrorContainer = Color(0xFFFFDAD6),

    inverseSurface = GajaColors.Surface,
    inverseOnSurface = GajaColors.TextPrimary,
    inversePrimary = GajaColors.LimeDark,

    scrim = Color(0xFF000000),
)

// ============================================================================
// SECTION 2: TYPOGRAPHY
// Clean, modern, readable type scale
// ============================================================================

/** GAJA Typography - Material 3 type scale with Korean-optimized sizing */
private val GajaTypography = Typography(
    // Display - Large headlines, hero sections
    displayLarge = TextStyle(
        fontFamily = null, // Use system default for Korean support
        fontWeight = FontWeight.Bold,
        fontSize = 52.sp,
        letterSpacing = (-0.6).sp,
        lineHeight = 58.sp,
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        letterSpacing = 0.sp,
        lineHeight = 46.sp,
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = 0.sp,
        lineHeight = 38.sp,
    ),
    
    // Headline - Section headers, prominent titles
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        letterSpacing = 0.sp,
        lineHeight = 36.sp,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        letterSpacing = 0.sp,
        lineHeight = 32.sp,
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = (-0.1).sp,
        lineHeight = 28.sp,
    ),
    
    // Title - Card titles, important labels
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = 0.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        letterSpacing = 0.05.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        letterSpacing = 0.05.sp,
        lineHeight = 20.sp,
    ),
    
    // Body - Main content text
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        letterSpacing = 0.2.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        letterSpacing = 0.15.sp,
        lineHeight = 19.sp,
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.2.sp,
        lineHeight = 16.sp,
    ),
    
    // Label - Buttons, chips, small labels
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        letterSpacing = 0.1.sp,
        lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.3.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 0.3.sp,
        lineHeight = 13.sp,
    ),
)

// ============================================================================
// SECTION 3: SHAPE SYSTEM
// Consistent corner radii across all components
// ============================================================================

/** GAJA Shape System - Rounded corners for a friendly, modern feel */
private val GajaShapes = Shapes(
    // Extra small - Chips, small buttons
    extraSmall = RoundedCornerShape(10.dp),
    
    // Small - Small cards, text fields
    small = RoundedCornerShape(14.dp),
    
    // Medium - Cards, dialogs
    medium = RoundedCornerShape(20.dp),
    
    // Large - Large cards, bottom sheets
    large = RoundedCornerShape(28.dp),
    
    // Extra large - Hero cards, full-width containers
    extraLarge = RoundedCornerShape(36.dp),
)

// ============================================================================
// SECTION 4: SPACING & DIMENSION TOKENS
// Consistent spacing scale for layout
// ============================================================================

/** Spacing scale - 4dp base unit */
object GajaSpacing {
    val None = 0.dp
    val ExtraSmall = 6.dp
    val Small = 10.dp
    val Medium = 14.dp
    val Large = 18.dp
    val ExtraLarge = 24.dp
    val XXLarge = 30.dp
    val XXXLarge = 40.dp
    val Huge = 56.dp
    
    // Component-specific spacing
    val CardPadding = 24.dp
    val ScreenPadding = 20.dp
    val SectionSpacing = 30.dp
    val ItemSpacing = 14.dp
}

/** Elevation tokens - Shadow depths */
object GajaElevation {
    val None = 0.dp
    val Low = 2.dp
    val Medium = 5.dp
    val High = 10.dp
    val ExtraHigh = 14.dp
    val Hero = 18.dp
}

/** Component dimensions */
object GajaDimensions {
    // Buttons
    val ButtonHeight = 52.dp
    val ButtonHeightCompact = 42.dp
    val ButtonCornerRadius = 18.dp
    
    // Cards
    val CardCornerRadius = 22.dp
    val CardCornerRadiusLarge = 34.dp
    
    // Navigation
    val NavigationBarHeight = 82.dp
    val TopAppBarHeight = 64.dp
    
    // Icons
    val IconSmall = 18.dp
    val IconMedium = 24.dp
    val IconLarge = 32.dp
    
    // Avatars
    val AvatarSmall = 32.dp
    val AvatarMedium = 48.dp
    val AvatarLarge = 72.dp
    
    // Chips
    val ChipHeight = 34.dp
    val ChipCornerRadius = 18.dp
}

// ============================================================================
// SECTION 5: COMPOSITION LOCALS
// Provide design tokens to composition tree
// ============================================================================

/** Local spacing provider for easy access in composables */
val LocalGajaSpacing = staticCompositionLocalOf { GajaSpacing }

/** Local dimensions provider */
val LocalGajaDimensions = staticCompositionLocalOf { GajaDimensions }

/** Local elevation provider */
val LocalGajaElevation = staticCompositionLocalOf { GajaElevation }

// ============================================================================
// SECTION 6: THEME COMPOSABLE
// Main theme wrapper for the app
// ============================================================================

/**
 * GAJA Theme - Material 3 theme with custom design tokens
 * 
 * Usage:
 * ```kotlin
 * GajaTheme {
 *     // Your content here
 * }
 * ```
 */
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

/**
 * Legacy theme name for backward compatibility
 * @deprecated Use GajaTheme instead
 */
@Composable
fun BikeFrontTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    GajaTheme(darkTheme = darkTheme, content = content)
}
