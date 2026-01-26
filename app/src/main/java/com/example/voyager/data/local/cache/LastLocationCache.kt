package com.example.voyager.data.local.cache


import android.content.Context
import android.content.SharedPreferences
import com.example.voyager.data.model.CachedLocation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache for last known location
 * Critical for offline emergency functionality
 */
@Singleton
class LastLocationCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("last_location_cache", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
        private const val KEY_ACCURACY = "accuracy"
        private const val KEY_TIMESTAMP = "timestamp"
        private const val KEY_LOCATION_NAME = "location_name"
    }

    /**
     * Save location to cache
     */
    suspend fun save(location: CachedLocation) = withContext(Dispatchers.IO) {
        prefs.edit().apply {
            putString(KEY_LATITUDE, location.latitude.toString())
            putString(KEY_LONGITUDE, location.longitude.toString())
            putFloat(KEY_ACCURACY, location.accuracy ?: 0f)
            putLong(KEY_TIMESTAMP, location.timestamp)
            putString(KEY_LOCATION_NAME, location.locationName)
            apply()
        }
    }

    /**
     * Get cached location
     * Returns null if no location cached
     */
    suspend fun get(): CachedLocation? = withContext(Dispatchers.IO) {
        try {
            val latitude = prefs.getString(KEY_LATITUDE, null)?.toDoubleOrNull()
            val longitude = prefs.getString(KEY_LONGITUDE, null)?.toDoubleOrNull()
            val accuracy = prefs.getFloat(KEY_ACCURACY, 0f)
            val timestamp = prefs.getLong(KEY_TIMESTAMP, 0L)
            val locationName = prefs.getString(KEY_LOCATION_NAME, null)

            if (latitude != null && longitude != null && timestamp > 0) {
                CachedLocation(
                    latitude = latitude,
                    longitude = longitude,
                    accuracy = if (accuracy > 0) accuracy else null,
                    timestamp = timestamp,
                    isLastKnown = true,
                    locationName = locationName
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if cache has valid location
     */
    fun hasCache(): Boolean {
        return prefs.contains(KEY_LATITUDE) && prefs.contains(KEY_LONGITUDE)
    }

    /**
     * Clear cache
     */
    fun clear() {
        prefs.edit().clear().apply()
    }

    /**
     * Get cache age in milliseconds
     */
    fun getCacheAge(): Long {
        val timestamp = prefs.getLong(KEY_TIMESTAMP, 0L)
        if (timestamp == 0L) return Long.MAX_VALUE
        return System.currentTimeMillis() - timestamp
    }

    /**
     * Check if cache is fresh (less than 1 hour old)
     */
    fun isCacheFresh(): Boolean {
        val oneHour = 60 * 60 * 1000L
        return getCacheAge() < oneHour
    }
}