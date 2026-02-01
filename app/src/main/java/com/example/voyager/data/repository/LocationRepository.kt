package com.example.voyager.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.os.Looper
import com.example.voyager.data.model.LocationData
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val lastLocationCache: LastLocationCache
) {
    private val geocoder = Geocoder(context, Locale.getDefault())

    /**
     * Get current location as a single emission
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Result<LocationData> = suspendCancellableCoroutine { continuation ->
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val locationData = LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        timestamp = System.currentTimeMillis(),
                        isLastKnown = false
                    )

                    // Cache location
                    lastLocationCache.save(locationData)

                    continuation.resume(Result.success(locationData))
                } else {
                    // Fallback to cached location
                    val cached = lastLocationCache.get()
                    if (cached != null) {
                        continuation.resume(Result.success(cached.copy(isLastKnown = true)))
                    } else {
                        continuation.resume(Result.failure(Exception("Location unavailable")))
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Try to return cached location
                val cached = lastLocationCache.get()
                if (cached != null) {
                    continuation.resume(Result.success(cached.copy(isLastKnown = true)))
                } else {
                    continuation.resume(Result.failure(exception))
                }
            }
    }

    /**
     * Get location updates as a Flow
     */
    @SuppressLint("MissingPermission")
    fun getLocationUpdates(intervalMillis: Long = 10000L): Flow<LocationData> = callbackFlow {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            intervalMillis
        ).apply {
            setMinUpdateIntervalMillis(intervalMillis / 2)
            setWaitForAccurateLocation(false)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val locationData = LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        timestamp = System.currentTimeMillis(),
                        isLastKnown = false
                    )

                    // Cache location
                    lastLocationCache.save(locationData)

                    trySend(locationData)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    /**
     * Get last known location from cache
     */
    fun getLastKnownLocation(): LocationData? {
        return lastLocationCache.get()
    }

    /**
     * Reverse geocode to get address from coordinates
     */
    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use new API for Android 13+
                var addressResult: String? = null
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    addressResult = addresses.firstOrNull()?.let { address ->
                        listOfNotNull(
                            address.locality,
                            address.adminArea,
                            address.countryName
                        ).joinToString(", ")
                    }
                }
                addressResult
            } else {
                // Use deprecated API for older versions
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.firstOrNull()?.let { address ->
                    listOfNotNull(
                        address.locality,
                        address.adminArea,
                        address.countryName
                    ).joinToString(", ")
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculate distance between two points in meters
     */
    fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }
}

/**
 * Simple cache for last known location using SharedPreferences
 */
@Singleton
class LastLocationCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("location_cache", Context.MODE_PRIVATE)

    fun save(location: LocationData) {
        prefs.edit().apply {
            putString("latitude", location.latitude.toString())
            putString("longitude", location.longitude.toString())
            putFloat("accuracy", location.accuracy)
            putLong("timestamp", location.timestamp)
            apply()
        }
    }

    fun get(): LocationData? {
        val lat = prefs.getString("latitude", null)?.toDoubleOrNull() ?: return null
        val lon = prefs.getString("longitude", null)?.toDoubleOrNull() ?: return null
        val accuracy = prefs.getFloat("accuracy", 0f)
        val timestamp = prefs.getLong("timestamp", 0L)

        return LocationData(
            latitude = lat,
            longitude = lon,
            accuracy = accuracy,
            timestamp = timestamp,
            isLastKnown = true
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}