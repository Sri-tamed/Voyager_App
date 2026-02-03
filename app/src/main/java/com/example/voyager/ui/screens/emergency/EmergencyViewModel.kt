package com.example.voyager.ui.screens.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.voyager.emergency.EmergencyManager
import com.example.voyager.data.model.DangerLevel
import com.example.voyager.data.model.DeliveryStatus
import com.example.voyager.data.model.EmergencyContact
import com.example.voyager.data.model.EmergencyEvent
import kotlinx.coroutines.flow.MutableStateFlow
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
    val isEmergencyModeActive: Boolean = false,
    val currentDangerLevel: DangerLevel = DangerLevel.SAFE,
    val isSosInProgress: Boolean = false,
    val hasPermissions: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel for Emergency functionality
 */
class EmergencyViewModel(
    private val emergencyManager: EmergencyManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

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