package com.bikeprojectminji.bikefront.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// GAJA MVP Color Palette - Mint/Teal with White emphasis
private val GajaMint = Color(0xFF00BFA5) // Primary mint/teal
private val GajaMintLight = Color(0xFF64FFDA) // Light mint accent
private val GajaMintDark = Color(0xFF00897B) // Dark mint for emphasis

private val GajaWhite = Color(0xFFFFFFFF) // Pure white
private val GajaBackground = Color(0xFFF8FAFB) // Soft off-white background
private val GajaSurface = Color(0xFFFFFFFF) // White surface for cards
private val GajaSurfaceVariant = Color(0xFFE8F5F3) // Light mint-tinted surface

private val GajaTextPrimary = Color(0xFF1A1A1A) // Near-black for primary text
private val GajaTextSecondary = Color(0xFF6B7280) // Gray for secondary text
private val GajaTextTertiary = Color(0xFF9CA3AF) // Light gray for tertiary

private val GajaAccent = Color(0xFF26A69A) // Teal accent
private val GajaSuccess = Color(0xFF4CAF50) // Green for success states
private val GajaWarning = Color(0xFFFF9800) // Orange for warnings
private val GajaError = Color(0xFFE53935) // Red for errors

private val GajaDivider = Color(0xFFE5E7EB) // Light gray for dividers
private val GajaCardShadow = Color(0x1A000000) // Subtle shadow

private val LightColors = lightColorScheme(
    primary = GajaMint,
    onPrimary = GajaWhite,
    primaryContainer = GajaMintLight,
    onPrimaryContainer = GajaMintDark,
    
    secondary = GajaAccent,
    onSecondary = GajaWhite,
    secondaryContainer = GajaSurfaceVariant,
    onSecondaryContainer = GajaTextPrimary,
    
    tertiary = GajaMintDark,
    onTertiary = GajaWhite,
    
    background = GajaBackground,
    onBackground = GajaTextPrimary,
    
    surface = GajaSurface,
    onSurface = GajaTextPrimary,
    
    surfaceVariant = GajaSurfaceVariant,
    onSurfaceVariant = GajaTextSecondary,
    
    outline = GajaDivider,
    outlineVariant = GajaDivider,
    
    error = GajaError,
    onError = GajaWhite,
)

private val DarkColors = darkColorScheme(
    primary = GajaMintLight,
    onPrimary = GajaMintDark,
    primaryContainer = GajaMintDark,
    onPrimaryContainer = GajaMintLight,
    
    secondary = GajaAccent,
    onSecondary = GajaWhite,
    secondaryContainer = Color(0xFF1A3A36),
    onSecondaryContainer = GajaMintLight,
    
    tertiary = GajaMint,
    onTertiary = GajaMintDark,
    
    background = Color(0xFF0D1F1C),
    onBackground = GajaWhite,
    
    surface = Color(0xFF152623),
    onSurface = GajaWhite,
    
    surfaceVariant = Color(0xFF1A3A36),
    onSurfaceVariant = GajaMintLight,
    
    outline = Color(0xFF2D4A45),
    outlineVariant = Color(0xFF1A3A36),
    
    error = Color(0xFFEF5350),
    onError = Color(0xFF1A0000),
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