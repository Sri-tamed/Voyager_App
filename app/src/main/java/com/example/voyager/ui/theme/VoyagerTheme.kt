package com.example.voyager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Voyager Premium Color Palette
object VoyagerColors {
    // Primary warm yellow/golden tones
    val SunsetGold = Color(0xFFFDB750)
    val WarmYellow = Color(0xFFFFC947)
    val SoftGold = Color(0xFFFFD66B)

    // Background cream/beige tones
    val CreamBackground = Color(0xFFFFF8F0)
    val LightBeige = Color(0xFFFFF5E9)
    val WarmIvory = Color(0xFFFFFBF5)

    // Accent colors
    val EmergencyRed = Color(0xFFE63946)
    val SafeGreen = Color(0xFF06D6A0)
    val CautionOrange = Color(0xFFFF9F1C)

    // Neutral tones
    val DarkCharcoal = Color(0xFF2B2D42)
    val MediumGray = Color(0xFF8D99AE)
    val LightGray = Color(0xFFEDF2F4)

    // Glass/overlay effects
    val GlassWhite = Color(0xCCFFFFFF)
    val GlassBeige = Color(0xCCFFF8F0)
}

private val LightColorScheme = lightColorScheme(
    primary = VoyagerColors.SunsetGold,
    onPrimary = VoyagerColors.DarkCharcoal,
    primaryContainer = VoyagerColors.SoftGold,
    onPrimaryContainer = VoyagerColors.DarkCharcoal,

    secondary = VoyagerColors.WarmYellow,
    onSecondary = VoyagerColors.DarkCharcoal,

    background = VoyagerColors.CreamBackground,
    onBackground = VoyagerColors.DarkCharcoal,

    surface = VoyagerColors.WarmIvory,
    onSurface = VoyagerColors.DarkCharcoal,

    error = VoyagerColors.EmergencyRed,
    onError = Color.White
)

@Composable
fun VoyagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VoyagerTypography,
        content = content
    )
}