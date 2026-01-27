package com.example.voyager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.voyager.data.model.DangerLevel
import com.example.voyager.data.model.UserLocation
import com.example.voyager.ui.screens.explore.HomeDashboard
import com.example.voyager.ui.theme.VoyagerTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatLng by mutableStateOf<LatLng?>(null)
    private var currentUserLocation by mutableStateOf<UserLocation?>(null)
    private var hasLocationPermission by mutableStateOf(false)
    private var currentDangerLevel by mutableStateOf(DangerLevel.SAFE)

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                hasLocationPermission = true
                getCurrentLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                hasLocationPermission = true
                getCurrentLocation()
            }
            else -> {
                hasLocationPermission = false
                Toast.makeText(
                    this,
                    "Location permission is required for better experience",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()

        setContent {
            VoyagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeDashboard(
                        userLocation = currentLatLng ?: LatLng(22.5726, 88.3639),
                        dangerLevel = currentDangerLevel,
                        onEmergencyClick = { handleEmergencyClick() }
                    )
                }
            }
        }
    }

    private fun checkLocationPermission() {
        hasLocationPermission = when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
                true
            }
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
                true
            }
            else -> {
                requestLocationPermission()
                false
            }
        }
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun getCurrentLocation() {
        try {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val cancellationTokenSource = CancellationTokenSource()

                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        currentUserLocation = UserLocation(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            city = getCityName(location.latitude, location.longitude)
                        )
                        currentLatLng = LatLng(location.latitude, location.longitude)
                        currentDangerLevel = DangerLevel.SAFE
                    } else {
                        setDefaultLocation()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Failed to get location: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    setDefaultLocation()
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(
                this,
                "Location permission not granted",
                Toast.LENGTH_SHORT
            ).show()
            setDefaultLocation()
        }
    }

    private fun setDefaultLocation() {
        currentUserLocation = UserLocation(
            latitude = 22.5726,
            longitude = 88.3639,
            city = "Kolkata"
        )
        currentLatLng = LatLng(22.5726, 88.3639)
        currentDangerLevel = DangerLevel.SAFE
    }

    private fun getCityName(latitude: Double, longitude: Double): String {
        return "Kolkata"
    }

    private fun handleEmergencyClick() {
        Toast.makeText(
            this,
            "Emergency button clicked - Stay safe!",
            Toast.LENGTH_LONG
        ).show()
    }
}