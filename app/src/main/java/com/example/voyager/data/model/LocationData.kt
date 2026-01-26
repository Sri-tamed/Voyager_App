package com.example.voyager.data.model


data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long,
    val isLastKnown: Boolean = false
)

/**
 * Cached location data (works offline)
 */
data class CachedLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isLastKnown: Boolean = false,
    val locationName: String? = null
)