package com.example.voyager.data.model

import com.example.voyager.emergency.location.LocationSource

/**
 * Emergency event data model
 * Represents a single emergency alert with location and delivery status
 */
data class EmergencyEvent(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val locationAccuracy: Float,
    val locationSource: LocationSource,
    val dangerLevel: DangerLevel,
    val timestamp: Long,
    val deliveryStatus: DeliveryStatus = DeliveryStatus.PENDING
)