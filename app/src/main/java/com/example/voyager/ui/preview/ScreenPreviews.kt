package com.example.voyager.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.voyager.ui.theme.VoyagerTheme
import com.example.voyager.ui.screens.explore.HomeDashboard
import com.example.voyager.ui.screens.EmergencyModeScreen
import com.example.voyager.data.model.DangerLevel
import com.example.voyager.data.model.UserLocation
import com.google.android.gms.maps.model.LatLng  // Add this import

@Preview(showBackground = true, name = "Home Dashboard - Safe")
@Composable
fun HomeDashboardPreview() {
    VoyagerTheme {
        HomeDashboard(
            userLocation = LatLng(22.5726, 88.3639),  // Use LatLng instead of UserLocation
            userCity = "Kolkata",  // Pass city separately
            dangerLevel = DangerLevel.SAFE,
            onEmergencyClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Home Dashboard - Moderate")
@Composable
fun HomeDashboardModeratePreview() {
    VoyagerTheme {
        HomeDashboard(
            userLocation = LatLng(22.5726, 88.3639),
            userCity = "Kolkata",
            dangerLevel = DangerLevel.MODERATE,
            onEmergencyClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Home Dashboard - High")
@Composable
fun HomeDashboardHighPreview() {
    VoyagerTheme {
        HomeDashboard(
            userLocation = LatLng(22.5726, 88.3639),
            userCity = "Kolkata",
            dangerLevel = DangerLevel.HIGH,
            onEmergencyClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Emergency Mode")
@Composable
fun EmergencyPreview() {
    VoyagerTheme {
        EmergencyModeScreen(
            onCancel = {}
        )
    }
}

// Optional: If you want to preview with location permission banners
@Preview(showBackground = true, name = "Home Dashboard - No Permission")
@Composable
fun HomeDashboardNoPermissionPreview() {
    VoyagerTheme {
        HomeDashboard(
            userLocation = LatLng(22.5726, 88.3639),
            userCity = "Kolkata",
            dangerLevel = DangerLevel.SAFE,
            hasLocationPermission = false,  // Show permission banner
            onEmergencyClick = {},
            onPermissionClick = {}
        )
    }
}