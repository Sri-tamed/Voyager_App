package com.example.voyager.domain.geofence


import com.example.voyager.data.model.DangerZone
import com.example.voyager.data.model.UserLocation
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class GeofencingManager {

    fun checkGeofenceEntry(
        userLocation: UserLocation,
        dangerZones: List<DangerZone>
    ): DangerZone? {
        return dangerZones.firstOrNull { zone ->
            calculateDistance(
                userLocation.latitude,
                userLocation.longitude,
                zone.latitude,
                zone.longitude
            ) <= zone.radius
        }
    }

    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val earthRadius = 6_371_000.0 // meters

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (earthRadius * c).toFloat()
    }
}
