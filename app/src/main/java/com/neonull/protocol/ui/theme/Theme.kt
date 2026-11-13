package com.neonull.protocol.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val NeoDarkColorScheme = darkColorScheme(
    primary = NeoColors.MatrixGreen,
    onPrimary = NeoColors.ObsidianVoid,
    secondary = NeoColors.CyberCyan,
    onSecondary = NeoColors.ObsidianVoid,
    tertiary = NeoColors.CrimsonRed,
    onTertiary = NeoColors.TextPrimary,
    background = NeoColors.DeepNavyBlack,
    onBackground = NeoColors.TextPrimary,
    surface = NeoColors.TerminalSurface,
    onSurface = NeoColors.TextPrimary,
    surfaceVariant = NeoColors.ElevatedPanel,
    onSurfaceVariant = NeoColors.TextSecondary,
    outline = NeoColors.BorderSubtle,
    error = NeoColors.CrimsonRed,
    onError = NeoColors.TextPrimary
)

val NeoTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.8.sp,
        color = NeoColors.TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp,
        color = NeoColors.TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        color = NeoColors.TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = NeoColors.TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 19.sp,
        color = NeoColors.TextSecondary
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 11.5.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.6.sp,
        color = NeoColors.CyberCyan
    )
)

val CodeTextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Normal,
    fontSize = 13.5.sp,
    lineHeight = 21.sp,
    color = NeoColors.TextPrimary
)

val ConsoleTextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Normal,
    fontSize = 12.5.sp,
    lineHeight = 19.sp,
    color = NeoColors.MatrixGreen
)

@Composable
fun NeoNullTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NeoDarkColorScheme,
        typography = NeoTypography,
        content = content
    )
}
