package com.example.voyager.data.model


data class DangerZone(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float, // in meters
    val dangerLevel: DangerLevel
)

enum class DangerLevel {
    SAFE, CAUTION, DANGER
}

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val city: String = "Unknown Location"
)

sealed class EmergencyState {
    object Inactive : EmergencyState()
    data class Active(val triggeredBy: TriggerSource) : EmergencyState()
}

enum class TriggerSource {
    MANUAL, GEOFENCE
}