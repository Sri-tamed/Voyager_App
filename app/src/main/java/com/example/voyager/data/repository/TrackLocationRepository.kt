package com.example.voyager.data.repository

import com.example.voyager.data.remote.LocationData as RemoteLocationData
import com.example.voyager.data.remote.LocationResponse
import com.example.voyager.data.remote.OsrmApiService
import com.example.voyager.data.remote.RemoteApiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Repository for backend-driven live tracking and OSRM routing.
 *
 * NOTE: This is separate from data.repository.LocationRepository,
 * which handles on-device GPS/location utilities.
 */
class TrackLocationRepository(
    private val api: com.example.voyager.data.remote.LocationApiService = RemoteApiProvider.locationApi,
    private val osrmApi: OsrmApiService = RemoteApiProvider.osrmApi
) {

    /**
     * Fetch target user's latest location.
     *
     * Returns the raw Retrofit Response so ViewModel can handle 404 specially.
     */
    suspend fun fetchTargetLocation(userId: String): Response<LocationResponse> =
        withContext(Dispatchers.IO) {
            api.getLocation(userId)
        }

    /**
     * Fetch a driving route between viewer and target using OSRM.
     *
     * Returns a list of (lat, lon) pairs in correct order for map rendering.
     */
    suspend fun fetchRoute(
        viewerLat: Double,
        viewerLon: Double,
        targetLat: Double,
        targetLon: Double
    ): Result<List<Pair<Double, Double>>> = withContext(Dispatchers.IO) {
        try {
            val coords = "${viewerLon},${viewerLat};${targetLon},${targetLat}"
            val response = osrmApi.getRoute(coords = coords)

            if (!response.isSuccessful) {
                return@withContext Result.failure(HttpException(response))
            }

            val body = response.body()
            val route = body?.routes?.firstOrNull()
            val geometry = route?.geometry
            val coordinates = geometry?.coordinates ?: emptyList()

            // OSRM returns [lon, lat] — swap to [lat, lon]
            val points = coordinates.mapNotNull { pair ->
                if (pair.size >= 2) {
                    val lon = pair[0]
                    val lat = pair[1]
                    Pair(lat, lon)
                } else {
                    null
                }
            }

            Result.success(points)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

