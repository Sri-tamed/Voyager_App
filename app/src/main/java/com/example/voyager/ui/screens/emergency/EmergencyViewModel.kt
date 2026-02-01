package com.example.voyager.ui.screens.emergency


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voyager.data.model.DangerLevel
import com.example.voyager.data.model.EmergencyContact
import com.example.voyager.data.model.EmergencyData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmergencyUiState(
    val isOnline: Boolean = true,
    val dangerLevel: DangerLevel = DangerLevel.SAFE,
    val currentLocationName: String? = null,
    val isLocationCached: Boolean = false,
    val hasLastKnownLocation: Boolean = false,
    val hasSmsPermission: Boolean = false,
    val isSOSActive: Boolean = false,
    val sosStatusMessage: String = "",
    val sosQueuedCount: Int = 0,
    val emergencyContacts: List<EmergencyContact> = emptyList(),
    val lastKnownLocation: Location? = null
)

@HiltViewModel
class EmergencyViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    // Inject your repository here
    // private val emergencyRepository: EmergencyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

    private var toneGenerator: ToneGenerator? = null

    init {
        checkPermissions()
        loadEmergencyContacts()
        checkNetworkStatus()
        // Initialize tone generator for alarm
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkPermissions() {
        val hasSms = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        _uiState.update { it.copy(hasSmsPermission = hasSms) }
    }

    private fun checkNetworkStatus() {
        // TODO: Implement actual network check
        // You can use ConnectivityManager to check network status
        viewModelScope.launch {
            // Simulate network check
            _uiState.update { it.copy(isOnline = true) }
        }
    }

    private fun loadEmergencyContacts() {
        viewModelScope.launch {
            // TODO: Load from repository
            // val contacts = emergencyRepository.getEmergencyContacts()

            // Sample data for testing
            val sampleContacts = listOf(
                EmergencyContact(
                    id = 1,
                    name = "John Doe",
                    phoneNumber = "+1234567890",
                    relationship = "Friend",
                    isPrimary = true,
                    position = 0
                ),
                EmergencyContact(
                    id = 2,
                    name = "Jane Smith",
                    phoneNumber = "+0987654321",
                    relationship = "Family",
                    isPrimary = false,
                    position = 1
                )
            )

            _uiState.update { it.copy(emergencyContacts = sampleContacts) }
        }
    }

    fun triggerOfflineSOS() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSOSActive = true, sosStatusMessage = "Sending alerts...") }

            val emergencyData = EmergencyData(
                latitude = _uiState.value.lastKnownLocation?.latitude,
                longitude = _uiState.value.lastKnownLocation?.longitude,
                locationName = _uiState.value.currentLocationName,
                dangerLevel = _uiState.value.dangerLevel
            )

            if (_uiState.value.isOnline) {
                sendSOSToContacts(emergencyData)
            } else {
                queueSOSForLater(emergencyData)
            }
        }
    }

    private fun sendSOSToContacts(data: EmergencyData) {
        val contacts = _uiState.value.emergencyContacts

        if (contacts.isEmpty()) {
            _uiState.update {
                it.copy(
                    isSOSActive = false,
                    sosStatusMessage = "No emergency contacts configured"
                )
            }
            return
        }

        val message = buildSOSMessage(data)

        contacts.forEach { contact ->
            if (_uiState.value.hasSmsPermission) {
                sendSMS(contact.phoneNumber, message)
            } else {
                // Fallback to call
                makePhoneCall(contact.phoneNumber)
            }
        }

        _uiState.update {
            it.copy(
                isSOSActive = true,
                sosStatusMessage = "Alert sent to ${contacts.size} contact(s)"
            )
        }

        // Auto-deactivate after 5 seconds
        viewModelScope.launch {
            kotlinx.coroutines.delay(5000)
            _uiState.update { it.copy(isSOSActive = false, sosStatusMessage = "") }
        }
    }

    private fun queueSOSForLater(data: EmergencyData) {
        // TODO: Save to local database for later sending
        _uiState.update {
            it.copy(
                sosQueuedCount = it.sosQueuedCount + 1,
                isSOSActive = false,
                sosStatusMessage = ""
            )
        }
    }

    private fun buildSOSMessage(data: EmergencyData): String {
        return buildString {
            append("🚨 EMERGENCY SOS ALERT 🚨\n\n")
            append(data.message)
            append("\n\nTime: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(data.timestamp)}")

            if (data.latitude != null && data.longitude != null) {
                append("\n\nLocation: ${data.locationName ?: "Unknown"}")
                append("\nCoordinates: ${data.latitude}, ${data.longitude}")
                append("\nGoogle Maps: https://maps.google.com/?q=${data.latitude},${data.longitude}")
            } else {
                append("\n\nLocation: Not available")
            }

            append("\n\nDanger Level: ${data.dangerLevel.name}")

            if (data.batteryLevel != null) {
                append("\nBattery: ${data.batteryLevel}%")
            }
        }
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        try {
            val smsManager = context.getSystemService(SmsManager::class.java)
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(
                phoneNumber,
                null,
                parts,
                null,
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun callLocalEmergency() {
        // Call local emergency number (e.g., 911, 112, etc.)
        makePhoneCall("911") // TODO: Make this configurable based on country
    }

    fun shareLocation() {
        val location = _uiState.value.lastKnownLocation
        if (location != null) {
            val message = "My current location: https://maps.google.com/?q=${location.latitude},${location.longitude}"

            if (_uiState.value.hasSmsPermission && !_uiState.value.isOnline) {
                // Send via SMS when offline
                _uiState.value.emergencyContacts.forEach { contact ->
                    sendSMS(contact.phoneNumber, message)
                }
            } else {
                // Share via intent
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Location").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        }
    }

    fun callContact(contact: EmergencyContact) {
        makePhoneCall(contact.phoneNumber)
    }

    private fun makePhoneCall(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun triggerLocalAlarm() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 3000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateDangerLevel(level: DangerLevel) {
        _uiState.update { it.copy(dangerLevel = level) }
    }

    fun updateLocation(location: Location?, locationName: String?) {
        _uiState.update {
            it.copy(
                lastKnownLocation = location,
                currentLocationName = locationName,
                hasLastKnownLocation = location != null,
                isLocationCached = location != null && !_uiState.value.isOnline
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        toneGenerator?.release()
        toneGenerator = null
    }
}