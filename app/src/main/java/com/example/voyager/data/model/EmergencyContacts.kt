package com.example.voyager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val relationship: String,
    val isPrimary: Boolean = false,
    val position: Int = 0 // For ordering (0-4 for top 5)
)



// EmergencyData - not a Room entity, just for SOS messages
data class EmergencyData(
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double?,
    val longitude: Double?,
    val locationName: String?,
    val dangerLevel: DangerLevel,
    val message: String = "Emergency SOS Alert!",
    val batteryLevel: Int? = null
)