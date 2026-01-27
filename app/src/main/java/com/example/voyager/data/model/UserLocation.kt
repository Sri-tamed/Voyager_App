package com.example.voyager.data.model


/**
 * Represents the user's location with optional city and country information
 */
data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val city: String? = null,      // ⭐ Make sure this is nullable (String?)
    val country: String? = null    // ⭐ Make sure this is nullable (String?)
)