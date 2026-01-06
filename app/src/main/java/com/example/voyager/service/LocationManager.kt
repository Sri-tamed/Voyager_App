package com.example.voyager.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Looper
import com.google.android.gms.location.*
import com.example.voyager.data.model.UserLocation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale

class LocationManager(private val context: Context) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val geocoder = Geocoder(context, Locale.getDefault())

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(): Flow<UserLocation> = callbackFlow {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000
        )
            .setMinUpdateIntervalMillis(2000)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    val city = try {
                        geocoder.getFromLocation(
                            loc.latitude,
                            loc.longitude,
                            1
                        )?.firstOrNull()?.locality ?: "Unknown Location"
                    } catch (e: Exception) {
                        "Unknown Location"
                    }

                    trySend(
                        UserLocation(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            city = city
                        )
                    )
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            request,
            callback,
            Looper.getMainLooper()
        )

        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }
}
