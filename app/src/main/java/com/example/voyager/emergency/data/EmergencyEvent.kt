package com.example.voyager.emergency.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_events")
data class EmergencyEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val dangerLevel: DangerLevel,
    val locationAccuracy: Float = 0f,
    val locationSource: LocationSource = LocationSource.UNKNOWN,
    val deliveryStatus: DeliveryStatus = DeliveryStatus.PENDING,
    val retryCount: Int = 0,
    val lastRetryTimestamp: Long? = null
)

enum class DangerLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class LocationSource {
    GPS,
    NETWORK,
    LAST_KNOWN,
    FALLBACK,
    UNKNOWN
}

enum class DeliveryStatus {
    PENDING,
    SMS_SENT,
    PARTIALLY_SENT,
    FAILED,
    DELIVERED
}