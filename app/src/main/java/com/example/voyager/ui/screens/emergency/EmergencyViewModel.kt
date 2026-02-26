package com.example.voyager.ui.screens.emergency

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.voyager.emergency.EmergencyManager
import com.example.voyager.data.model.DangerLevel
import com.example.voyager.data.model.DeliveryStatus
import com.example.voyager.data.model.EmergencyContact
import com.example.voyager.data.model.EmergencyEvent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Emergency Screen
 */
data class EmergencyUiState(
    val contacts: List<EmergencyContact> = emptyList(),
    val recentEvents: List<EmergencyEvent> = emptyList(),
    // Bug 2 fix: ensure default is false, and keep it explicit here.
    val isEmergencyModeActive: Boolean = false,
    val currentDangerLevel: DangerLevel = DangerLevel.SAFE,
    val isSosInProgress: Boolean = false,
    val hasPermissions: Boolean = false,
    val errorMessage: String? = null
)

/**
 * One-shot UI events (so ViewModel can request an Intent-like action
 * without holding a Context). The composable collects these and launches intents
 * using LocalContext.current (Compose best practice).
 */
sealed interface EmergencyUiEvent {
    data class OpenSmsApp(
        val phoneNumbers: List<String>,
        val message: String
    ) : EmergencyUiEvent

    data class OpenDialer(
        val phoneNumber: String
    ) : EmergencyUiEvent
}

/**
 * ViewModel for Emergency functionality
 */
class EmergencyViewModel(
    private val emergencyManager: EmergencyManager
) : ViewModel() {

    // Bug 2 fix: initialize explicitly with isEmergencyModeActive = false.
    private val _uiState = MutableStateFlow(EmergencyUiState(isEmergencyModeActive = false))
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

    // Bug 1 fix: event stream for share/dial actions.
    private val _events = MutableSharedFlow<EmergencyUiEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<EmergencyUiEvent> = _events

    init {
        loadEmergencyData()
    }

    /**
     * Load contacts and events from database
     */
    private fun loadEmergencyData() {
        viewModelScope.launch {
            try {
                combine(
                    emergencyManager.getEmergencyContacts(),
                    emergencyManager.getEmergencyEvents()
                ) { contacts, events ->
                    Pair(contacts, events)
                }.collect { (contacts, events) ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            contacts = contacts,
                            recentEvents = events.take(10),
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to load data: ${e.message}") }
            }
        }
    }

    /**
     * Trigger SOS - Send emergency alerts
     */
    fun triggerSos(dangerLevel: DangerLevel = DangerLevel.HIGH) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSosInProgress = true, errorMessage = null) }

            try {
                emergencyManager.triggerSos(dangerLevel)
                _uiState.update { it.copy(isSosInProgress = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSosInProgress = false,
                        errorMessage = "SOS failed: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Add new emergency contact
     */
    fun addContact(name: String, phoneNumber: String) {
        viewModelScope.launch {
            try {
                emergencyManager.addEmergencyContact(name, phoneNumber)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to add contact: ${e.message}") }
            }
        }
    }

    /**
     * Delete emergency contact
     */
    fun deleteContact(contact: EmergencyContact) {
        viewModelScope.launch {
            try {
                emergencyManager.deleteEmergencyContact(contact)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to delete contact: ${e.message}") }
            }
        }
    }

    /**
     * Reorder contacts (for drag and drop)
     */
    fun reorderContacts(contacts: List<EmergencyContact>) {
        viewModelScope.launch {
            try {
                emergencyManager.reorderContacts(contacts)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to reorder: ${e.message}") }
            }
        }
    }

    /**
     * Start emergency monitoring mode
     */
    fun startEmergencyMode(dangerLevel: DangerLevel = DangerLevel.SAFE) {
        _uiState.update {
            it.copy(
                isEmergencyModeActive = true,
                currentDangerLevel = dangerLevel
            )
        }
        emergencyManager.startEmergencyMode(dangerLevel)
    }

    /**
     * Stop emergency monitoring mode
     */
    fun stopEmergencyMode() {
        _uiState.update { it.copy(isEmergencyModeActive = false) }
        emergencyManager.stopEmergencyMode()
    }

    /**
     * Bug 1 fix: Share Location button handler.
     *
     * Emits an event that the composable will turn into an ACTION_SENDTO SMS intent
     * with the message pre-filled.
     */
    fun shareLocation() {
        viewModelScope.launch {
            try {
                // Prefer using the most recent event's location if available.
                // TODO: Replace with real-time location from a repository/fused provider.
                val lastEvent = _uiState.value.recentEvents.firstOrNull()
                val lat = lastEvent?.latitude ?: 0.0
                val lon = lastEvent?.longitude ?: 0.0

                val mapsUrl = "https://maps.google.com/?q=$lat,$lon"
                val message = """
                    🚨 EMERGENCY - Voyager App
                    I need help! My location: $mapsUrl
                """.trimIndent()

                val numbers = _uiState.value.contacts
                    .sortedBy { it.position }
                    .map { it.phoneNumber }
                    .filter { it.isNotBlank() }

                println("EmergencyViewModel.shareLocation() contacts=${numbers.size} lat=$lat lon=$lon")

                _events.emit(
                    EmergencyUiEvent.OpenSmsApp(
                        phoneNumbers = numbers,
                        message = message
                    )
                )
            } catch (e: Exception) {
                println("EmergencyViewModel.shareLocation() failed: ${e.message}")
                _uiState.update { it.copy(errorMessage = "Share location failed: ${e.message}") }
            }
        }
    }

    /**
     * Bug 1 fix: Call Contact button handler.
     *
     * Emits an event that the composable will turn into an ACTION_DIAL intent:
     * tel:PHONE_NUMBER (no runtime permission required).
     */
    fun callContact(contact: EmergencyContact) {
        viewModelScope.launch {
            try {
                val number = contact.phoneNumber.trim()
                println("EmergencyViewModel.callContact() name=${contact.name} number=$number")

                if (number.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Contact phone number is missing") }
                    return@launch
                }

                // Basic sanity check (does not enforce strict validation).
                val tel = Uri.parse("tel:$number")
                if (tel.scheme != "tel") {
                    _uiState.update { it.copy(errorMessage = "Invalid phone number") }
                    return@launch
                }

                _events.emit(EmergencyUiEvent.OpenDialer(phoneNumber = number))
            } catch (e: Exception) {
                println("EmergencyViewModel.callContact() failed: ${e.message}")
                _uiState.update { it.copy(errorMessage = "Call failed: ${e.message}") }
            }
        }
    }

    /**
     * Update permissions state
     */
    fun updatePermissionsState(hasPermissions: Boolean) {
        _uiState.update { it.copy(hasPermissions = hasPermissions) }
    }

    /**
     * Retry failed emergency deliveries
     */
    fun retryFailedDeliveries() {
        viewModelScope.launch {
            try {
                emergencyManager.retryFailedDeliveries()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Retry failed: ${e.message}") }
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

/**
 * Factory for creating EmergencyViewModel
 */
class EmergencyViewModelFactory(
    private val emergencyManager: EmergencyManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmergencyViewModel::class.java)) {
            return EmergencyViewModel(emergencyManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}