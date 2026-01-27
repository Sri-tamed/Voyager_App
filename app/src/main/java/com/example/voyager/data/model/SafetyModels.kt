package com.example.voyager.data.model


data class DangerZone(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float, // in meters
    val dangerLevel: DangerLevel
)






