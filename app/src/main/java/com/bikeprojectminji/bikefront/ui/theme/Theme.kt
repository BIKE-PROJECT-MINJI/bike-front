package com.bikeprojectminji.bikefront.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Typography
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bikeprojectminji.bikefront.R

object GajaColors {
    val Primary = Color(0xFF5F8F6B)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFE4F0E4)
    val PrimarySoft = Color(0xFFF1F6EF)

    val Accent = Color(0xFF294034)
    val AccentSoft = Color(0xFF385344)
    val LimeAccent = Color(0xFFF8FBF6)
    val BrightLime = Color(0xFFEFF6EC)

    val White = Color(0xFFFFFFFF)
    val Background = Color(0xFFF5F7F2)
    val Surface = Color(0xFFFFFEFB)
    val SurfaceMuted = Color(0xFFF7FAF5)

    val TextPrimary = Color(0xFF24342B)
    val TextSecondary = Color(0xFF5C6C61)
    val TextTertiary = Color(0xFF8A948D)

    val Carbon = Color(0xFF304437)
    val Graphite = Color(0xFF4A6254)

    val Success = Color(0xFF5A8A63)
    val Warning = Color(0xFFC78B46)
    val Error = Color(0xFFC85B50)

    val Border = Color(0xFFDCE5D8)
    val Divider = Color(0xFFEBF1E8)

    val BrandGradient = listOf(Color(0xFFEAF4E7), Color(0xFFD8EAD8))
    val HeroGradient = listOf(Color(0xFFF5FAF2), Color(0xFFE2EFE1))
}

private val LightColorScheme = lightColorScheme(
    primary = GajaColors.Primary,
    onPrimary = GajaColors.White,
    primaryContainer = GajaColors.PrimaryContainer,
    onPrimaryContainer = GajaColors.Accent,

    secondary = GajaColors.Accent,
    onSecondary = GajaColors.White,

    background = GajaColors.Background,
    onBackground = GajaColors.TextPrimary,

    surface = GajaColors.Surface,
    onSurface = GajaColors.TextPrimary,
    surfaceVariant = GajaColors.SurfaceMuted,
    onSurfaceVariant = GajaColors.TextSecondary,

    outline = GajaColors.Border,
    error = GajaColors.Error,
)

private val DarkColorScheme = darkColorScheme(
    primary = GajaColors.Primary,
    onPrimary = GajaColors.White,
    primaryContainer = GajaColors.Carbon,

    background = Color(0xFF18211B),
    onBackground = GajaColors.White,

    surface = Color(0xFF223028),
    onSurface = GajaColors.White,

    outline = Color(0xFF425247),
)

private val GajaFontFamily = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_regular, FontWeight.Medium),
    Font(R.font.pretendard_bold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold),
    Font(R.font.pretendard_bold, FontWeight.ExtraBold),
)

private val GajaTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = GajaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        letterSpacing = (-0.8).sp,
        lineHeight = 42.sp,
        color = Color.Unspecified
    ),
    displayMedium = TextStyle(
        fontFamily = GajaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = (-0.4).sp,
        lineHeight = 36.sp,
        color = Color.Unspecified
    ),
    headlineLarge = TextStyle(
        fontFamily = GajaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = (-0.3).sp,
        lineHeight = 32.sp,
        color = Color.Unspecified
    ),
    headlineMedium = TextStyle(
        fontFamily = GajaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = (-0.2).sp,
        lineHeight = 28.sp,
        color = Color.Unspecified
    ),
    headlineSmall = TextStyle(
        fontFamily = GajaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = (-0.1).sp,
        lineHeight = 27.sp,
        color = Color.Unspecified
    ),
    titleLarge = TextStyle(
        fontFamily = GajaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = (-0.1).sp,
        lineHeight = 26.sp,
        color = Color.Unspecified
    ),
    titleMedium = TextStyle(
        fontFamily = GajaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = Color.Unspecified
    ),
    titleSmall = TextStyle(
        fontFamily = GajaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        color = Color.Unspecified
    ),
    bodyLarge = TextStyle(
        fontFamily = GajaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 24.sp,
        color = Color.Unspecified
    ),
    bodyMedium = TextStyle(
        fontFamily = GajaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        color = Color.Unspecified
    ),
    bodySmall = TextStyle(
        fontFamily = GajaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 19.sp,
        color = Color.Unspecified
    ),
    labelLarge = TextStyle(
        fontFamily = GajaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        letterSpacing = 0.sp,
        color = Color.Unspecified
    ),
    labelMedium = TextStyle(
        fontFamily = GajaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        letterSpacing = 0.sp,
        color = Color.Unspecified
    ),
    labelSmall = TextStyle(
        fontFamily = GajaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 0.sp,
        color = Color.Unspecified
    )
)

private val GajaShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

object GajaSpacing {
    val ScreenPadding = 20.dp
    val ItemSpacing = 16.dp
    val Large = 24.dp
    val Medium = 16.dp
    val Small = 12.dp
    val Tiny = 8.dp
    val Micro = 4.dp
    val SectionGap = 28.dp
    val CardPadding = 20.dp
    val CompactCardPadding = 12.dp
}

object GajaRadius {
    val Small = 12.dp
    val Medium = 18.dp
    val Large = 24.dp
    val XLarge = 28.dp
    val Pill = 999.dp
}

object GajaButtonTokens {
    val Height = 52.dp
    val MinWidth = 180.dp
}

object GajaIconSizes {
    val Small = 16.dp
    val Medium = 18.dp
    val Large = 20.dp
    val Control = 22.dp
    val PrimaryControl = 24.dp
}

object GajaCardTokens {
    val DefaultPadding = 20.dp
    val CompactPadding = 12.dp
    val HeroPadding = 24.dp
    val ElevatedPadding = 20.dp
    val BorderWidth = 1.dp
    val SubtleElevation = 2.dp
}

object GajaControlTokens {
    val TopBarAction = 40.dp
    val ListLeading = 44.dp
    val LargeListLeading = 52.dp
    val BadgeHorizontalPadding = 10.dp
    val BadgeVerticalPadding = 6.dp
}

object GajaHudTokens {
    val OverlayMargin = 16.dp
    val FloatingGap = 12.dp
    val CardPadding = 16.dp
    val CompactPadding = 12.dp
    val TopBannerRadius = 24.dp
    val BottomCardRadius = 28.dp
    val SecondaryCardWidth = 156.dp
    val MapControlSize = 44.dp
    val SecondaryControlSize = 56.dp
    val PrimaryControlSize = 68.dp
    val SpeedCardWidth = 148.dp
    val SpeedCardMinHeight = 124.dp
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
