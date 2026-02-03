package com.example.voyager.emergency.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyDao {

    // Emergency Contacts
    @Query("SELECT * FROM emergency_contacts WHERE isActive = 1 ORDER BY priority ASC LIMIT 5")
    fun getActiveContacts(): Flow<List<EmergencyContact>>

    @Query("SELECT * FROM emergency_contacts WHERE isActive = 1 ORDER BY priority ASC LIMIT 5")
    suspend fun getActiveContactsList(): List<EmergencyContact>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContact): Long

    @Update
    suspend fun updateContact(contact: EmergencyContact)

    @Delete
    suspend fun deleteContact(contact: EmergencyContact)

    @Query("UPDATE emergency_contacts SET priority = :priority WHERE id = :contactId")
    suspend fun updateContactPriority(contactId: Int, priority: Int)

    // Emergency Events
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EmergencyEvent): Long

    @Update
    suspend fun updateEvent(event: EmergencyEvent)

    @Query("SELECT * FROM emergency_events WHERE deliveryStatus != 'DELIVERED' ORDER BY timestamp DESC")
    suspend fun getPendingEvents(): List<EmergencyEvent>

    @Query("SELECT * FROM emergency_events ORDER BY timestamp DESC LIMIT 50")
    fun getAllEvents(): Flow<List<EmergencyEvent>>

    @Query("UPDATE emergency_events SET deliveryStatus = :status, retryCount = retryCount + 1, lastRetryTimestamp = :timestamp WHERE id = :eventId")
    suspend fun updateEventStatus(eventId: Int, status: DeliveryStatus, timestamp: Long = System.currentTimeMillis())
}