package com.example.voyager.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.voyager.data.model.DangerLevel
import com.example.voyager.ui.navigation.BottomNav
import com.example.voyager.ui.navigation.VoyagerNavGraph
import com.google.android.gms.maps.model.LatLng

/**
 * Root layout composable. This is the ONLY place Scaffold exists.
 *
 * Structure:
 *   Scaffold
 *     ├── bottomBar  → BottomNav        (visible on home/map/explore/profile)
 *     └── content    → VoyagerNavGraph   (owns every screen via NavHost)
 *
 * Both slots receive the exact same navController instance.
 * No other file in the project should create a Scaffold or a NavController.
 */
@Composable
fun VoyagerScaffold(
    navController: NavHostController,
    userLocation: LatLng,
    userCity: String,
    dangerLevel: DangerLevel,
    hasLocationPermission: Boolean,
    onRequestLocationPermission: () -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNav(navController = navController)
        }
    ) { paddingValues ->
        VoyagerNavGraph(
            navController = navController,
            userLocation = userLocation,
            userCity = userCity,
            dangerLevel = dangerLevel,
            hasLocationPermission = hasLocationPermission,
            onRequestLocationPermission = onRequestLocationPermission,
            modifier = Modifier.padding(paddingValues)
        )
    }
}