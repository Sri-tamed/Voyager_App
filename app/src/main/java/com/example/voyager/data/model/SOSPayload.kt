package com.example.voyager.data.model




data class SOSPayload(
    val userId: String,
    val timestamp: Long,
    val location: LocationData,  // Changed from CachedLocation to LocationData
    val dangerLevel: DangerLevel,
    val message: String = "",  // Has default value to make it optional
    val emergencyContacts: List<EmergencyContact> = emptyList(),
    val deviceInfo: DeviceInfo? = null  // Made nullable with default null
)
