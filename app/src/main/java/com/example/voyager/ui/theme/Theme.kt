package com.example.voyager.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val VoyagerLightColorScheme = lightColorScheme(
    primary = VoyagerYellow,
    onPrimary = TextOnYellow,
    primaryContainer = GoldLight,
    onPrimaryContainer = TextPrimary,

    secondary = VoyagerOrange,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFE0B2),
    onSecondaryContainer = Color(0xFF4A3800),

    tertiary = AccentBlue,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBBDEFB),
    onTertiaryContainer = Color(0xFF003C5D),

    error = DangerRed,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = BackgroundPrimary,
    onBackground = TextPrimary,

    surface = SurfaceElevated,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundSecondary,
    onSurfaceVariant = TextSecondary,

    outline = Color(0xFFBFB5A9),
    outlineVariant = Color(0xFFE6DED2),

    scrim = OverlayDark,
    inverseSurface = Color(0xFF353025),
    inverseOnSurface = Color(0xFFF5EFE7),
    inversePrimary = GoldMedium,

    surfaceTint = VoyagerYellow
)

@Composable
fun VoyagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+, but we use custom theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = VoyagerLightColorScheme // Only light mode for now

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundPrimary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VoyagerTypography,
        shapes = VoyagerShapes,
        content = content
    )
}