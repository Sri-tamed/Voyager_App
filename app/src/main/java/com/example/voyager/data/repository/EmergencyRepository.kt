package com.example.voyager.data.repository


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.voyager.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationRepository: LocationRepository
) {
    private val _isSOSActive = MutableStateFlow(false)
    val isSOSActive: Flow<Boolean> = _isSOSActive.asStateFlow()

    /**
     * Trigger SOS emergency alert
     * Uses share intent instead of direct SMS to avoid SEND_SMS permission
     */
    suspend fun triggerSOS(
        emergencyContacts: List<EmergencyContact>,
        userId: String,
        dangerLevel: DangerLevel = DangerLevel.DANGER
    ): Result<Unit> {
        return try {
            // Get current location
            val location = locationRepository.getCurrentLocation().getOrNull()
                ?: locationRepository.getLastKnownLocation()
                ?: return Result.failure(Exception("Location unavailable"))

            // Build SOS payload
            val sosPayload = SOSPayload(
                userId = userId,
                timestamp = System.currentTimeMillis(),
                location = location,
                dangerLevel = dangerLevel,
                emergencyContacts = emergencyContacts,
                deviceInfo = getDeviceInfo()
            )

            // Create shareable message
            val message = buildSOSMessage(sosPayload)

            // Share via intent (WhatsApp, SMS, Email, etc.)
            shareSOSMessage(message, emergencyContacts)

            // Mark SOS as active
            _isSOSActive.value = true

            // TODO: Send to backend/Firebase for tracking
            // sendToBackend(sosPayload)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deactivate SOS
     */
    fun deactivateSOS() {
        _isSOSActive.value = false
    }

    /**
     * Build human-readable SOS message
     */
    private fun buildSOSMessage(payload: SOSPayload): String {
        val location = payload.location
        val mapsUrl = "https://www.google.com/maps?q=${location.latitude},${location.longitude}"

        return """
            🚨 EMERGENCY ALERT - Voyager App 🚨
            
            I need immediate assistance!
            
            📍 My Location: 
            $mapsUrl
            
            🕐 Time: ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss", java.util.Locale.getDefault()).format(payload.timestamp)}
            
            ⚠️ Danger Level: ${payload.dangerLevel.name}
            
            ${if (location.isLastKnown) "⚠️ This is my LAST KNOWN location (network may be unavailable)" else "✅ Current real-time location"}
            
            Please check on me or contact local authorities.
            
            - Sent via Voyager Travel Safety App
        """.trimIndent()
    }

    /**
     * Share SOS message using system share sheet
     * This avoids needing SEND_SMS permission
     */
    private fun shareSOSMessage(message: String, contacts: List<EmergencyContact>) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_SUBJECT, "🚨 EMERGENCY ALERT")

            // Add phone numbers as suggestion (for SMS apps)
            if (contacts.isNotEmpty()) {
                val phoneNumbers = contacts.joinToString(",") { it.phoneNumber }
                putExtra("address", phoneNumbers)
            }

            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Send Emergency Alert")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ContextCompat.startActivity(context, chooserIntent, null)
    }

    /**
     * Call local emergency number (911, 112, etc.)
     */
    fun callEmergencyNumber(countryCode: String = "US") {
        val emergencyNumber = when (countryCode) {
            "US", "CA" -> "911"
            "GB", "EU" -> "112"
            "AU" -> "000"
            "IN" -> "112"
            else -> "112" // Default to EU standard
        }

        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$emergencyNumber")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        ContextCompat.startActivity(context, intent, null)
    }

    /**
     * Share current location (for non-emergency situations)
     */
    suspend fun shareLocation(): Result<Unit> {
        return try {
            val location = locationRepository.getCurrentLocation().getOrNull()
                ?: locationRepository.getLastKnownLocation()
                ?: return Result.failure(Exception("Location unavailable"))

            val mapsUrl = "https://www.google.com/maps?q=${location.latitude},${location.longitude}"
            val message = "📍 My current location: $mapsUrl\n\nShared via Voyager"

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                putExtra(Intent.EXTRA_SUBJECT, "My Location")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share Location")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            ContextCompat.startActivity(context, chooserIntent, null)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get device info for SOS payload
     */
    private fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            batteryLevel = getBatteryLevel(),
            networkType = getNetworkType(),
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"
        )
    }

    private fun getBatteryLevel(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
        return batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun getNetworkType(): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "Offline"
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "Offline"

        return when {
            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile"
            else -> "Unknown"
        }
    }
}