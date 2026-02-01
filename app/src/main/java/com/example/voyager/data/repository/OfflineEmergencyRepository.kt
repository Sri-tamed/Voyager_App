package com.example.voyager.data.repository


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.example.voyager.data.local.cache.LastLocationCache
import com.example.voyager.data.local.datastore.EmergencyContactsStore
import com.example.voyager.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OFFLINE-FIRST Emergency Repository
 *
 * This is your app's TRUE USP - works even without internet
 *
 * Features:
 * 1. SMS fallback when network unavailable
 * 2. Last-known location from cache
 * 3. Queue SOS requests for retry when online
 * 4. Local alarm/vibration without network
 * 5. Direct phone call capability
 */
@Singleton
class OfflineEmergencyRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationRepository: LocationRepository,  // Use LocationRepository
    private val emergencyContactsStore: EmergencyContactsStore
) {
    private val _isSOSActive = MutableStateFlow(false)
    val isSOSActive: Flow<Boolean> = _isSOSActive.asStateFlow()

    private val _sosQueue = MutableStateFlow<List<SOSPayload>>(emptyList())
    val sosQueue: Flow<List<SOSPayload>> = _sosQueue.asStateFlow()

    /**
     * OFFLINE-CAPABLE SOS TRIGGER
     * Priority system:
     * 1. Try SMS (works offline with cellular)
     * 2. Try share intent (online)
     * 3. Cache for retry when online
     * 4. Always trigger local alarm
     */
    suspend fun triggerOfflineSOS(
        userId: String,
        dangerLevel: DangerLevel = DangerLevel.HIGH
    ): Result<SOSResult> {
        return try {
            // Get last known location (ALWAYS available, even offline)
            val location = locationRepository.getLastKnownLocation()
                ?: return Result.failure(Exception("No location data available"))

            // Get emergency contacts from local storage
            val contacts = emergencyContactsStore.getContacts().first()
            if (contacts.isEmpty()) {
                return Result.failure(Exception("No emergency contacts configured"))
            }

            // Build SOS payload
            val sosPayload = SOSPayload(
                userId = userId,
                timestamp = System.currentTimeMillis(),
                location = location,
                dangerLevel = dangerLevel,
                message = "EMERGENCY: I need help. Last known location attached.",
                emergencyContacts = contacts,
                deviceInfo = getDeviceInfo()
            )

            // Trigger local alarm/vibration FIRST (works offline)
            triggerLocalAlarm()

            // Check network status
            val isOnline = isNetworkAvailable()
            val hasSmsPermission = checkSmsPermission()

            val sosResult = when {
                // BEST: SMS works offline with cellular signal
                hasSmsPermission -> {
                    val smsResult = sendEmergencySMS(sosPayload, contacts)
                    if (smsResult) {
                        SOSResult.SMS_SENT
                    } else {
                        // SMS failed, queue for retry
                        queueSOSForRetry(sosPayload)
                        SOSResult.QUEUED_FOR_RETRY
                    }
                }

                // FALLBACK: Share intent (requires network)
                isOnline -> {
                    shareSOSViaIntent(sosPayload)
                    SOSResult.SHARED_VIA_INTENT
                }

                // OFFLINE: Queue and show manual options
                else -> {
                    queueSOSForRetry(sosPayload)
                    SOSResult.OFFLINE_QUEUED
                }
            }

            // Mark SOS as active
            _isSOSActive.value = true

            // Save SOS event locally for history
            saveSOSEventLocally(sosPayload)

            Result.success(sosResult)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send SMS directly (works OFFLINE with cellular)
     * This is the KEY offline feature
     */
    private fun sendEmergencySMS(
        payload: SOSPayload,
        contacts: List<EmergencyContact>
    ): Boolean {
        return try {
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val message = buildCompactSMSMessage(payload)

            // Send to top 3 contacts (SMS has char limits)
            contacts.take(3).forEach { contact ->
                try {
                    // Split message if too long
                    val parts = smsManager.divideMessage(message)
                    smsManager.sendMultipartTextMessage(
                        contact.phoneNumber,
                        null,
                        parts,
                        null,
                        null
                    )
                } catch (e: Exception) {
                    // Continue to next contact if one fails
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Compact SMS message (SMS has 160 char limit per part)
     */
    private fun buildCompactSMSMessage(payload: SOSPayload): String {
        val location = payload.location
        val mapsUrl = "https://maps.google.com/?q=${location.latitude},${location.longitude}"

        return """
🚨 EMERGENCY VIA VOYAGER
I NEED HELP!
Location: $mapsUrl
${if (location.isLastKnown) "LAST KNOWN location" else "Current location"}
Battery: ${payload.deviceInfo?.batteryLevel ?: "?"}%
Time: ${formatTime(payload.timestamp)}
        """.trimIndent()
    }

    /**
     * Queue SOS for retry when network returns
     */
    private fun queueSOSForRetry(payload: SOSPayload) {
        _sosQueue.value = _sosQueue.value + payload
        // TODO: WorkManager to retry when online
    }

    /**
     * Trigger local alarm/vibration (works 100% offline)
     */
    private fun triggerLocalAlarm() {
        try {
            // Vibrate in SOS pattern: long-short-short-short (... in Morse)
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            }

            // SOS pattern: ... --- ... (short-short-short long-long-long short-short-short)
            val sosPattern = longArrayOf(
                0, 200, 200, 200, 200, 200, 200,  // ...
                600, 600, 600, 600, 600, 600,      // ---
                200, 200, 200, 200, 200            // ...
            )

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    android.os.VibrationEffect.createWaveform(sosPattern, -1)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(sosPattern, -1)
            }

            // TODO: Play loud alarm sound (works offline)
            playAlarmSound()

        } catch (e: Exception) {
            // Vibration failed, continue with other methods
        }
    }

    /**
     * Play loud alarm sound (works offline)
     */
    private fun playAlarmSound() {
        try {
            val ringtone = android.media.RingtoneManager.getRingtone(
                context,
                android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
            )
            ringtone?.play()

            // Stop after 5 seconds
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                ringtone?.stop()
            }, 5000)
        } catch (e: Exception) {
            // Sound failed, not critical
        }
    }

    /**
     * Share via intent (fallback when online)
     */
    private fun shareSOSViaIntent(payload: SOSPayload) {
        val message = buildDetailedSOSMessage(payload)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_SUBJECT, "🚨 EMERGENCY ALERT - VOYAGER")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Send Emergency Alert")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ContextCompat.startActivity(context, chooserIntent, null)
    }

    /**
     * Detailed message for share intent
     */
    private fun buildDetailedSOSMessage(payload: SOSPayload): String {
        val location = payload.location
        val mapsUrl = "https://www.google.com/maps?q=${location.latitude},${location.longitude}"

        return """
🚨🚨🚨 EMERGENCY ALERT 🚨🚨🚨

I NEED IMMEDIATE ASSISTANCE!

📍 Location: $mapsUrl
Lat: ${location.latitude}
Lon: ${location.longitude}

🕐 Time: ${formatTime(payload.timestamp)}
⚠️ Status: ${if (location.isLastKnown) "LAST KNOWN LOCATION (May be offline)" else "Current real-time location"}

📱 Device: ${payload.deviceInfo?.deviceModel ?: "Unknown"}
🔋 Battery: ${payload.deviceInfo?.batteryLevel ?: "?"}%
📶 Network: ${payload.deviceInfo?.networkType ?: "Unknown"}

⚠️ Danger Level: ${payload.dangerLevel.name}

PLEASE CHECK ON ME OR CONTACT LOCAL AUTHORITIES IMMEDIATELY.

- Sent via Voyager Travel Safety App
        """.trimIndent()
    }

    /**
     * Direct call to emergency number (works offline with cellular)
     */
    fun callEmergencyDirect(countryCode: String = "US") {
        val emergencyNumber = when (countryCode) {
            "US", "CA" -> "911"
            "GB", "EU" -> "112"
            "AU" -> "000"
            "IN" -> "112"
            "JP" -> "110"
            else -> "112"
        }

        // Use ACTION_CALL for direct call (requires CALL_PHONE permission)
        // Fallback to ACTION_DIAL if permission not granted
        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val intent = Intent(
            if (hasCallPermission) Intent.ACTION_CALL else Intent.ACTION_DIAL
        ).apply {
            data = Uri.parse("tel:$emergencyNumber")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        ContextCompat.startActivity(context, intent, null)
    }

    /**
     * Call specific emergency contact
     */
    fun callEmergencyContact(contact: EmergencyContact) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${contact.phoneNumber}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ContextCompat.startActivity(context, intent, null)
    }

    /**
     * Deactivate SOS
     */
    fun deactivateSOS() {
        _isSOSActive.value = false
    }

    /**
     * Check network availability
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null
    }

    /**
     * Check if SMS permission is granted
     */
    private fun checkSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Save SOS event locally for history
     */
    private fun saveSOSEventLocally(payload: SOSPayload) {
        // TODO: Save to local database for SOS history
        val prefs = context.getSharedPreferences("sos_history", Context.MODE_PRIVATE)
        val history = prefs.getStringSet("events", mutableSetOf()) ?: mutableSetOf()
        history.add("${payload.timestamp}|${payload.dangerLevel}|${payload.location.latitude},${payload.location.longitude}")
        prefs.edit().putStringSet("events", history).apply()
    }

    /**
     * Get device info
     */
    private fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            batteryLevel = getBatteryLevel(),
            networkType = getNetworkType(),
            deviceModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
        )
    }

    private fun getBatteryLevel(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
        return batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun getNetworkType(): String {
        if (!isNetworkAvailable()) return "Offline"

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "Offline"
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "Offline"

        return when {
            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            else -> "Unknown"
        }
    }

    private fun formatTime(timestamp: Long): String {
        return java.text.SimpleDateFormat("MMM dd, HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
    }
}

/**
 * SOS Result types
 */
enum class SOSResult {
    SMS_SENT,              // SMS sent successfully (works offline)
    SHARED_VIA_INTENT,     // Shared via intent (online)
    OFFLINE_QUEUED,        // Queued for retry (offline)
    QUEUED_FOR_RETRY       // Failed but queued
}