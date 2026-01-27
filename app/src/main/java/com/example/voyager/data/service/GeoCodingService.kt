package com.example.voyager.data.service


import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


object GeocodingService {

    private const val TAG = "GeocodingService"
    private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org"
    private const val USER_AGENT = "Voyager/1.0 (Safety Travel App)"

    /**
     * Reverse geocode: Convert coordinates to address
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @return Pair of (city, country) or null if failed
     */
    suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): Pair<String, String>? = withContext(Dispatchers.IO) {
        try {
            val url = "$NOMINATIM_BASE_URL/reverse?format=json&lat=$latitude&lon=$longitude&zoom=10"

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", USER_AGENT)
                connectTimeout = 10000
                readTimeout = 10000
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = org.json.JSONObject(response)
                val address = json.optJSONObject("address")

                val city = address?.optString("city")
                    ?: address?.optString("town")
                    ?: address?.optString("village")
                    ?: address?.optString("state")
                    ?: "Unknown"

                val country = address?.optString("country") ?: "Unknown"

                Log.d(TAG, "Reverse geocode success: $city, $country")
                Pair(city, country)
            } else {
                Log.e(TAG, "Reverse geocode failed with code: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Reverse geocode error: ${e.message}", e)
            null
        }
    }

    /**
     * Forward geocode: Convert city name to coordinates
     * @param city City name to search
     * @param country Optional country to narrow search
     * @return Pair of (latitude, longitude) or null if failed
     */
    suspend fun forwardGeocode(
        city: String,
        country: String = "India"
    ): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        try {
            val query = URLEncoder.encode("$city, $country", "UTF-8")
            val url = "$NOMINATIM_BASE_URL/search?format=json&q=$query&limit=1"

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", USER_AGENT)
                connectTimeout = 10000
                readTimeout = 10000
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(response)

                if (jsonArray.length() > 0) {
                    val result = jsonArray.getJSONObject(0)
                    val lat = result.getDouble("lat")
                    val lon = result.getDouble("lon")

                    Log.d(TAG, "Forward geocode success: $city -> ($lat, $lon)")
                    Pair(lat, lon)
                } else {
                    Log.e(TAG, "No results found for: $city")
                    null
                }
            } else {
                Log.e(TAG, "Forward geocode failed with code: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Forward geocode error: ${e.message}", e)
            null
        }
    }

    /**
     * Search for places matching a query
     * @param query Search query (e.g., "Taj Mahal", "Mumbai beaches")
     * @param limit Maximum number of results
     * @return List of search results
     */
    suspend fun searchPlaces(
        query: String,
        limit: Int = 5
    ): List<PlaceResult> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "$NOMINATIM_BASE_URL/search?format=json&q=$encodedQuery&limit=$limit"

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", USER_AGENT)
                connectTimeout = 10000
                readTimeout = 10000
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(response)

                val results = mutableListOf<PlaceResult>()
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    results.add(
                        PlaceResult(
                            displayName = item.getString("display_name"),
                            latitude = item.getDouble("lat"),
                            longitude = item.getDouble("lon"),
                            type = item.optString("type", "unknown")
                        )
                    )
                }

                Log.d(TAG, "Search places success: ${results.size} results")
                results
            } else {
                Log.e(TAG, "Search places failed with code: $responseCode")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Search places error: ${e.message}", e)
            emptyList()
        }
    }
}

/**
 * Data class representing a place search result
 */
data class PlaceResult(
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val type: String
)