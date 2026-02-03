package com.example.voyager.emergency.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val priority: Int = 0, // 0-4 for top 5 ranking
    val isActive: Boolean = true,
    val addedTimestamp: Long = System.currentTimeMillis()
)