package com.example.voyager.emergency



import android.content.Context
import android.telephony.SmsManager
import com.example.voyager.data.local.cache.LastLocationCache
import com.example.voyager.data.local.datastore.EmergencyContactsStore
import com.example.voyager.data.model.DangerLevel
import com.example.voyager.data.model.EmergencyContact
import com.example.voyager.data.model.EmergencyEvent
import com.example.voyager.emergency.data.EmergencyDatabase
import com.example.voyager.emergency.data.LocationSource as DbLocationSource
import com.example.voyager.emergency.data.DangerLevel as DbDangerLevel
import com.example.voyager.emergency.data.EmergencyEvent as DbEmergencyEvent
import com.example.voyager.emergency.data.DeliveryStatus as DbDeliveryStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first

/**
 * Emergency Manager - Handles all emergency operations
 *
 * SYSTEM-LEVEL RELIABILITY:
 * - Works offline (no internet required)
 * - SMS delivery via SmsManager
 * - Location from GPS/Network/Fused providers
 * - Foreground service for background operation
 */
class EmergencyManager private constructor(
    private val context: Context
) {

    companion object {
        @Volatile
        private var instance: EmergencyManager? = null

        /**
         * Get singleton instance
         * Thread-safe double-checked locking
         */
        fun getInstance(context: Context): EmergencyManager {
            return instance ?: synchronized(this) {
                instance ?: EmergencyManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    // Backing flow for contacts — wired to EmergencyContactsStore (offline-first).
    // This powers EmergencyContactsScreen and the Emergency UI.
    private val contactsStore by lazy { EmergencyContactsStore(context) }
    private val contactsFlow: Flow<List<EmergencyContact>> = contactsStore.getContacts()
    private val eventsFlow = flowOf<List<EmergencyEvent>>(emptyList())

    /**
     * Get all emergency contacts from database
     * Returns Flow for real-time updates
     */
    fun getEmergencyContacts(): Flow<List<EmergencyContact>> {
        // TODO: Return dao.getAllContacts()
        return contactsFlow
    }

    /**
     * Get emergency events (history) from database
     * Returns Flow for real-time updates
     */
    fun getEmergencyEvents(): Flow<List<EmergencyEvent>> {
        // TODO: Return dao.getAllEvents()
        return eventsFlow
    }

    /**
     * CRITICAL: Trigger SOS alert
     *
     * This is the core functionality - MUST work offline!
     *
     * Steps:
     * 1. Get current location (GPS/Network/Last Known)
     * 2. Fetch emergency contacts from database
     * 3. Send SMS to each contact with location
     * 4. Start foreground service to ensure delivery
     * 5. Log event to database
     * 6. Retry failed deliveries
     */
    suspend fun triggerSos(dangerLevel: DangerLevel) {
        // Bug 3 fix: implement basic SOS SMS sending + event persistence.
        //
        // NOTE:
        // - Permissions (SMS/Location) are handled elsewhere per your note.
        // - Location is taken from LastLocationCache as an offline-friendly fallback.
        //   TODO: Replace with fused provider / real-time tracking when available.
        //
        // Steps:
        // 1) Load emergency contacts (DataStore-based, offline-first)
        // 2) Get last known cached location (or fallback placeholder)
        // 3) Send SMS to all contacts via SmsManager
        // 4) Save event into the Room emergency DB (basic history)
        try {
            println("🚨 SOS TRIGGERED - Danger Level: $dangerLevel")

            val contactsStore = EmergencyContactsStore(context)
            val contacts = contactsStore.getContacts().first()

            if (contacts.isEmpty()) {
                println("EmergencyManager.triggerSos(): no emergency contacts configured")
                // Still save an event (with fallback coordinates) so user can debug history.
            }

            val cache = LastLocationCache(context)
            val cached = cache.get()

            val lat = cached?.latitude ?: 0.0
            val lon = cached?.longitude ?: 0.0
            val mapsUrl = "https://maps.google.com/?q=$lat,$lon"

            val smsMessage = """
                🚨 EMERGENCY - Voyager App
                I need help! My location: $mapsUrl
            """.trimIndent()

            println("EmergencyManager.triggerSos(): contacts=${contacts.size} lat=$lat lon=$lon cache=${cached != null}")

            // Send SMS to all emergency contacts (best-effort; continue on failures)
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            var sentCount = 0
            var failedCount = 0

            contacts.forEach { contact ->
                val number = contact.phoneNumber.trim()
                if (number.isBlank()) {
                    failedCount += 1
                    return@forEach
                }
                try {
                    val parts = smsManager.divideMessage(smsMessage)
                    smsManager.sendMultipartTextMessage(
                        number,
                        null,
                        parts,
                        null,
                        null
                    )
                    sentCount += 1
                    println("EmergencyManager.triggerSos(): SMS queued -> ${contact.name} ($number)")
                } catch (e: Exception) {
                    failedCount += 1
                    println("EmergencyManager.triggerSos(): SMS failed -> ${contact.name} ($number) error=${e.message}")
                }
            }

            // Save event to Room database (basic history)
            try {
                val db = EmergencyDatabase.getDatabase(context)
                val dao = db.emergencyDao()

                val dbDanger = when (dangerLevel) {
                    DangerLevel.SAFE -> DbDangerLevel.LOW
                    DangerLevel.MODERATE -> DbDangerLevel.MEDIUM
                    DangerLevel.HIGH -> DbDangerLevel.HIGH
                    DangerLevel.CRITICAL -> DbDangerLevel.CRITICAL
                }

                val source = if (cached != null) DbLocationSource.LAST_KNOWN else DbLocationSource.FALLBACK
                val status = when {
                    sentCount > 0 && failedCount == 0 -> DbDeliveryStatus.SMS_SENT
                    sentCount > 0 && failedCount > 0 -> DbDeliveryStatus.PARTIALLY_SENT
                    else -> DbDeliveryStatus.FAILED
                }

                val event = DbEmergencyEvent(
                    latitude = lat,
                    longitude = lon,
                    dangerLevel = dbDanger,
                    locationAccuracy = cached?.accuracy ?: 0f,
                    locationSource = source,
                    deliveryStatus = status
                )

                dao.insertEvent(event)
                println("EmergencyManager.triggerSos(): event saved status=$status")
            } catch (e: Exception) {
                // Non-fatal: SMS may have been sent even if saving fails.
                println("EmergencyManager.triggerSos(): failed to save event: ${e.message}")
            }
        } catch (e: Exception) {
            println("EmergencyManager.triggerSos(): fatal error: ${e.message}")
            throw e
        }
    }

    /**
     * Add new emergency contact to database
     * Maximum 5 contacts allowed
     */
    suspend fun addEmergencyContact(name: String, phoneNumber: String) {
        // Basic implementation backed by EmergencyContactsStore (DataStore).
        // This makes the EmergencyContactsScreen interactive instead of "frozen".
        try {
            val trimmedName = name.trim()
            val trimmedPhone = phoneNumber.trim()

            if (trimmedName.isBlank() || trimmedPhone.isBlank()) {
                println("addEmergencyContact(): invalid input")
                return
            }

            val current = contactsStore.getContacts().first()
            if (current.size >= 5) {
                println("addEmergencyContact(): already have 5 contacts")
                return
            }

            val nextId = (current.maxOfOrNull { it.id } ?: 0) + 1
            val newContact = EmergencyContact(
                id = nextId,
                name = trimmedName,
                phoneNumber = trimmedPhone,
                relationship = "",
                isPrimary = current.isEmpty(), // First contact becomes primary by default
                position = current.size
            )

            val updated = current + newContact
            contactsStore.saveContacts(updated)
            println("📇 Added emergency contact: $trimmedName - $trimmedPhone (total=${updated.size})")
        } catch (e: Exception) {
            println("addEmergencyContact() failed: ${e.message}")
        }
    }

    /**
     * Delete emergency contact from database
     */
    suspend fun deleteEmergencyContact(contact: EmergencyContact) {
        try {
            val current = contactsStore.getContacts().first()
            val updated = current.filter { it.id != contact.id }
                .mapIndexed { index, c -> c.copy(position = index) }

            contactsStore.saveContacts(updated)
            println("🗑️ Deleted emergency contact: ${contact.name} (remaining=${updated.size})")
        } catch (e: Exception) {
            println("deleteEmergencyContact() failed: ${e.message}")
        }
    }

    /**
     * Reorder contacts (for drag and drop priority)
     */
    suspend fun reorderContacts(contacts: List<EmergencyContact>) {
        try {
            // Persist the passed order as positions 0..n.
            val reordered = contacts.mapIndexed { index, c -> c.copy(position = index) }
            contactsStore.saveContacts(reordered)
            println("🔄 Reordered ${contacts.size} contacts")
        } catch (e: Exception) {
            println("reorderContacts() failed: ${e.message}")
        }
    }

    /**
     * Start emergency monitoring mode
     *
     * Activates:
     * - Continuous location tracking
     * - Geofence monitoring (danger zones)
     * - Background service
     */
    fun startEmergencyMode(dangerLevel: DangerLevel) {
        // TODO: Implement
        // 1. Start foreground service
        // 2. Enable location updates
        // 3. Activate geofence monitoring
        // 4. Show notification

        println("🟢 Emergency mode started - Level: $dangerLevel")
    }

    /**
     * Stop emergency monitoring mode
     */
    fun stopEmergencyMode() {
        // TODO: Implement
        // 1. Stop foreground service
        // 2. Disable location updates
        // 3. Remove geofences
        // 4. Cancel notification

        println("🔴 Emergency mode stopped")
    }

    /**
     * Retry failed SMS deliveries
     * Uses WorkManager for background retry
     */
    suspend fun retryFailedDeliveries() {
        // TODO: Implement
        // 1. Query database for FAILED deliveries
        // 2. Re-send SMS for each failed contact
        // 3. Update delivery status

        println("♻️ Retrying failed deliveries")
    }
}

/**
 * IMPLEMENTATION ROADMAP:
 *
 * Phase 1: Database (Room)
 * - EmergencyContactDao
 * - EmergencyEventDao
 * - VoyagerDatabase setup
 *
 * Phase 2: Location Tracking
 * - LocationManager integration
 * - FusedLocationProvider
 * - Last known location fallback
 *
 * Phase 3: SMS Delivery (OFFLINE!)
 * - SmsManager implementation
 * - Delivery/Sent PendingIntents
 * - BroadcastReceiver for status
 *
 * Phase 4: Foreground Service
 * - EmergencyService (runs in background)
 * - Notification for ongoing emergency
 * - START_STICKY for reliability
 *
 * Phase 5: WorkManager
 * - Retry failed SMS deliveries
 * - Periodic sync when network returns
 *
 * Phase 6: Geofencing
 * - Define danger zones
 * - Monitor entry/exit
 * - Trigger alerts
 */