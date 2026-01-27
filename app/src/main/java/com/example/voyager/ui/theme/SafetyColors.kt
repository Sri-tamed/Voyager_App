package com.example.voyager.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.voyager.data.model.DangerLevel

fun getSafetyColor(level: DangerLevel): Color {
    return when (level) {
        DangerLevel.SAFE -> Color(0xFF2ECC71)      // Green
        DangerLevel.MODERATE -> Color(0xFFF1C40F)  // Yellow
        DangerLevel.HIGH -> Color(0xFFE74C3C)      // Red
        DangerLevel.CRITICAL -> Color(0xFF8B0000)   // Dark Red
    }
}
