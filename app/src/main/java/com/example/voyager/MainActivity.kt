package com.example.voyager

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
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
import androidx.navigation.compose.rememberNavController
import com.example.voyager.data.model.DangerLevel
import com.example.voyager.ui.VoyagerScaffold
import com.example.voyager.ui.theme.VoyagerTheme
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // ---------------------------------------------------------------------------
    // Live state — every field is mutableStateOf so Compose recomposes automatically
    // whenever any of them change. MainActivity is the single owner; nothing else
    // writes to these.
    // ---------------------------------------------------------------------------
    private var currentLatLng         by mutableStateOf<LatLng?>(null)
    private var hasLocationPermission by mutableStateOf(false)
    private var currentDangerLevel    by mutableStateOf(DangerLevel.SAFE)
    private var currentCity           by mutableStateOf("Kolkata")

    // ---------------------------------------------------------------------------
    // LocationCallback — called by the system every time a new location arrives.
    // This is what makes the map marker, the coordinate text, and the city name
    // update in real time without restarting the app.
    // ---------------------------------------------------------------------------
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            currentLatLng  = LatLng(location.latitude, location.longitude)
            currentCity    = reverseGeocode(location)
            currentDangerLevel = evaluateDangerLevel(location)
        }

        override fun onLocationAvailability(availability: LocationAvailability) {
            if (!availability.isLocationAvailable) {
                // GPS lost — fall back to last known or default
                if (currentLatLng == null) setDefaultLocation()
            }
        }
    }

    // ---------------------------------------------------------------------------
    // Permission request callback — unchanged logic, same as before
    // ---------------------------------------------------------------------------
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                hasLocationPermission = true
                startLocationUpdates()
            }
            else -> {
                hasLocationPermission = false
                Toast.makeText(this, "Location permission is required for better experience", Toast.LENGTH_SHORT).show()
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
                    val navController = rememberNavController()

                    VoyagerScaffold(
                        navController            = navController,
                        userLocation             = currentLatLng ?: LatLng(22.5726, 88.3639),
                        userCity                 = currentCity,
                        dangerLevel              = currentDangerLevel,
                        hasLocationPermission    = hasLocationPermission,
                        onRequestLocationPermission = { requestLocationPermission() }
                    )
                }
            }
        }
    }

    // ---------------------------------------------------------------------------
    // Lifecycle — start updates on resume, stop on pause.
    // This keeps location fresh while the user is looking at the app, and stops
    // draining the battery the moment they switch away.
    // ---------------------------------------------------------------------------
    override fun onResume() {
        super.onResume()
        if (hasLocationPermission) startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // ---------------------------------------------------------------------------
    // Permission helpers
    // ---------------------------------------------------------------------------
    private fun checkLocationPermission() {
        hasLocationPermission = when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)   == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                startLocationUpdates(); true
            }
            else -> { requestLocationPermission(); false }
        }
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    // ---------------------------------------------------------------------------
    // Location updates — replaces the old one-shot getCurrentLocation().
    //
    // LocationRequest settings:
    //   interval        = 10 s   — ask the system for a new fix every 10 seconds
    //   fastestInterval = 5 s    — never deliver faster than every 5 seconds
    //   priority        = HIGH   — use GPS when available
    //
    // These are reasonable defaults for a travel app. Increase interval if battery
    // life matters more than precision.
    // ---------------------------------------------------------------------------
    private fun startLocationUpdates() {
        try {
            if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)   == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            ) {
                val request = LocationRequest.create().apply {
                    interval = 10_000L                                   // interval: 10 s
                    fastestInterval = 5_000L                             // fastest: 5 s
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }

                fusedLocationClient.requestLocationUpdates(request, locationCallback, mainLooper)
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            setDefaultLocation()
        }
    }

    // ---------------------------------------------------------------------------
    // Danger evaluation — called on every fresh location fix.
    //
    // Right now this is a stub that you can replace with a real API call
    // (e.g. a safety-index endpoint, weather alerts, crime-zone overlay).
    // The important thing is that it returns a DangerLevel and the result is
    // written to currentDangerLevel, which is mutableStateOf — so the
    // HomeDashboard banner and emergency-button colour recompose instantly.
    // ---------------------------------------------------------------------------
    private fun evaluateDangerLevel(location: Location): DangerLevel {
        // TODO: replace with real safety-data API call.
        // For now, return SAFE so nothing breaks. When your backend is ready,
        // swap this body with an actual network request and return the result.
        return DangerLevel.SAFE
    }

    // ---------------------------------------------------------------------------
    // Reverse geocoding — called on every fresh location fix.
    //
    // Uses Android's built-in Geocoder. On devices without Google Play Services
    // or offline, it returns the fallback string.
    // ---------------------------------------------------------------------------
    private fun reverseGeocode(location: Location): String {
        return try {
            val geocoder = android.location.Geocoder(this)
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                // Try city → district → state in order; return whichever is available first
                addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Unknown"
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            // Geocoder unavailable (no internet, no Play Services) — keep last known city
            currentCity
        }
    }

    private fun setDefaultLocation() {
        currentLatLng      = LatLng(22.5726, 88.3639)
        currentCity        = "Kolkata"
        currentDangerLevel = DangerLevel.SAFE
    }
}