package com.example.voyager.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.voyager.data.model.*
import com.example.voyager.data.repository.DangerZoneRepository
import com.example.voyager.domain.geofence.GeofencingManager
import com.example.voyager.service.EmergencyAlarmService
import com.example.voyager.service.LocationManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val locationManager = LocationManager(application)
    private val geofencingManager = GeofencingManager()
    private val alarmService = EmergencyAlarmService(application)

    private val _userLocation = MutableStateFlow<UserLocation?>(null)
    val userLocation: StateFlow<UserLocation?> = _userLocation.asStateFlow()

    private val _emergencyState =
        MutableStateFlow<EmergencyState>(EmergencyState.Inactive)
    val emergencyState: StateFlow<EmergencyState> = _emergencyState.asStateFlow()

    private val _currentDangerLevel =
        MutableStateFlow(DangerLevel.SAFE)
    val currentDangerLevel: StateFlow<DangerLevel> = _currentDangerLevel.asStateFlow()

    private val dangerZones =
        DangerZoneRepository.getMockDangerZones()

    fun startLocationTracking() {
        viewModelScope.launch {
            locationManager.getLocationUpdates().collect { location ->
                _userLocation.value = location
                checkGeofences(location)
            }
        }
    }

    private fun checkGeofences(location: UserLocation) {
        val enteredZone =
            geofencingManager.checkGeofenceEntry(location, dangerZones)

        enteredZone?.let { zone ->
            _currentDangerLevel.value = zone.dangerLevel

            if (
                zone.dangerLevel == DangerLevel.DANGER &&
                _emergencyState.value is EmergencyState.Inactive
            ) {
                triggerEmergency(TriggerSource.GEOFENCE)
            }
        } ?: run {
            _currentDangerLevel.value = DangerLevel.SAFE
        }
    }

    fun triggerEmergency(source: TriggerSource) {
        _emergencyState.value = EmergencyState.Active(source)
        alarmService.startAlarm()
    }

    fun cancelEmergency() {
        _emergencyState.value = EmergencyState.Inactive
        alarmService.stopAlarm()
    }

    override fun onCleared() {
        super.onCleared()
        alarmService.stopAlarm()
    }
}
