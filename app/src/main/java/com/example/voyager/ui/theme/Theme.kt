package com.example.voyager.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.voyager.data.model.DangerLevel

// Earth-toned, premium color palette
private val DarkBlue = Color(0xFF1B2838)
private val DeepGrey = Color(0xFF2C3E50)
private val EarthBrown = Color(0xFF5D4E37)
private val NeutralGrey = Color(0xFF95A5A6)
private val SafeGreen = Color(0xFF27AE60)
private val CautionOrange = Color(0xFFE67E22)
private val DangerRed = Color(0xFFE74C3C)
private val EmergencyDeepRed = Color(0xFF8B0000)

private val VoyagerColorScheme = darkColorScheme(
    primary = DarkBlue,
    onPrimary = Color.White,
    secondary = EarthBrown,
    background = DeepGrey,
    surface = DarkBlue,
    onSurface = Color.White
)

@Composable
fun VoyagerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VoyagerColorScheme,
        typography = Typography(),
        content = content
    )
}

fun getSafetyColor(level: DangerLevel): Color = when (level) {
    DangerLevel.SAFE -> SafeGreen
    DangerLevel.CAUTION -> CautionOrange
    DangerLevel.DANGER -> DangerRed
}
