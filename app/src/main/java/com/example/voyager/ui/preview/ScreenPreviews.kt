package com.example.voyager.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.voyager.ui.theme.VoyagerTheme
import com.example.voyager.ui.screens.HomeDashboard
import com.example.voyager.ui.screens.EmergencyModeScreen
import com.example.voyager.data.model.DangerLevel
import com.example.voyager.data.model.UserLocation

@Preview(showBackground = true, name = "Home Dashboard - Safe")
@Composable
fun HomeDashboardPreview() {
    VoyagerTheme {
        HomeDashboard(
            userLocation = UserLocation(
                latitude = 22.5726,
                longitude = 88.3639,
                city = "Kolkata"
            ),
            dangerLevel = DangerLevel.SAFE,
            onEmergencyClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Home Dashboard - Caution")
@Composable
fun HomeDashboardCautionPreview() {
    VoyagerTheme {
        HomeDashboard(
            userLocation = UserLocation(
                latitude = 22.5726,
                longitude = 88.3639,
                city = "Kolkata"
            ),
            dangerLevel = DangerLevel.CAUTION,
            onEmergencyClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Home Dashboard - Danger")
@Composable
fun HomeDashboardDangerPreview() {
    VoyagerTheme {
        HomeDashboard(
            userLocation = UserLocation(
                latitude = 22.5726,
                longitude = 88.3639,
                city = "Kolkata"
            ),
            dangerLevel = DangerLevel.DANGER,
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