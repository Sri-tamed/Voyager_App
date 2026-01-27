package com.example.voyager.ui.theme


import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val VoyagerShapes = Shapes(
    // Buttons, chips
    extraSmall = RoundedCornerShape(8.dp),
    // Small cards, badges
    small = RoundedCornerShape(12.dp),
    // Medium cards, dialogs
    medium = RoundedCornerShape(16.dp),
    // Large cards, bottom sheets
    large = RoundedCornerShape(20.dp),
    // Hero sections, modals
    extraLarge = RoundedCornerShape(28.dp)
)

// Custom shapes for specific components
object CustomShapes {
    val SearchBar = RoundedCornerShape(24.dp)
    val FloatingButton = RoundedCornerShape(16.dp)
    val GlassCard = RoundedCornerShape(20.dp)
    val BottomSheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val DestinationCard = RoundedCornerShape(18.dp)
    val SOSButton = RoundedCornerShape(50) // Fully rounded
    val small = RoundedCornerShape(8.dp)
}