package com.example.voyager.ui.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Explore : BottomNavItem("explore", "Explore", Icons.Filled.Explore)
    object Map : BottomNavItem("map", "Map", Icons.Filled.Map)
    object Emergency : BottomNavItem("emergency", "SOS", Icons.Filled.Warning)
    object Profile : BottomNavItem("profile", "Profile", Icons.Filled.Person)
}
