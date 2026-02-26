package com.example.voyager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voyager.data.remote.LocationData as RemoteLocationData
import com.example.voyager.data.repository.TrackLocationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class TrackLocationUiState(
    val targetLocation: RemoteLocationData? = null,
    val viewerLocation: Pair<Double, Double>? = null,
    val isRouteVisible: Boolean = false,
    val distanceKm: Double? = null,
    val trackingExpired: Boolean = false,
    val isLoading: Boolean = false,
    // List of (lat, lon) along the route polyline
    val routePoints: List<Pair<Double, Double>> = emptyList()
)

class TrackLocationViewModel(
    private val repository: TrackLocationRepository = TrackLocationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackLocationUiState())
    val uiState: StateFlow<TrackLocationUiState> = _uiState

    private var pollingJob: Job? = null

    /**
     * Start polling backend for target location every 5 seconds.
     */
    fun startPolling(userId: String) {
        // Avoid launching multiple pollers
        pollingJob?.cancel()

        pollingJob = viewModelScope.launch {
            println("TrackLocationViewModel.startPolling userId=$userId")
            _uiState.update { it.copy(isLoading = true, trackingExpired = false) }

            while (isActive && !_uiState.value.trackingExpired) {
                try {
                    val response = repository.fetchTargetLocation(userId)

                    if (response.code() == 404) {
                        println("TrackLocationViewModel: tracking expired (404)")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                trackingExpired = true,
                                targetLocation = null
                            )
                        }
                        break
                    }

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.success == true && body.data != null) {
                            println("TrackLocationViewModel: location=${body.data.latitude},${body.data.longitude}")
                            _uiState.update {
                                val updated = it.copy(
                                    targetLocation = body.data,
                                    isLoading = false
                                )
                                updated.copy(
                                    distanceKm = computeDistanceKm(updated.viewerLocation, updated.targetLocation)
                                )
                            }
                        }
                    } else {
                        println("TrackLocationViewModel: backend error code=${response.code()}")
                    }
                } catch (e: IOException) {
                    println("TrackLocationViewModel: network error=${e.message}")
                } catch (e: HttpException) {
                    println("TrackLocationViewModel: http error=${e.code()} message=${e.message()}")
                } catch (e: Exception) {
                    println("TrackLocationViewModel: unknown error=${e.message}")
                }

                delay(5_000L)
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Called by the Fragment when viewer GPS location changes.
     */
    fun updateViewerLocation(lat: Double, lon: Double) {
        _uiState.update {
            val updated = it.copy(viewerLocation = lat to lon)
            updated.copy(
                distanceKm = computeDistanceKm(updated.viewerLocation, updated.targetLocation)
            )
        }
    }

    /**
     * Toggle route visibility and, when turning on, load OSRM route if both
     * endpoints are available.
     */
    fun toggleRoute() {
        val current = _uiState.value
        val newVisible = !current.isRouteVisible

        _uiState.update {
            it.copy(
                isRouteVisible = newVisible,
                // Clear existing route when toggled off
                routePoints = if (newVisible) it.routePoints else emptyList(),
                distanceKm = if (newVisible) it.distanceKm else it.distanceKm
            )
        }

        if (newVisible) {
            val viewer = current.viewerLocation
            val target = current.targetLocation
            if (viewer != null && target != null) {
                loadRoute(viewer.first, viewer.second, target.latitude, target.longitude)
            }
        }
    }

    private fun loadRoute(
        viewerLat: Double,
        viewerLon: Double,
        targetLat: Double,
        targetLon: Double
    ) {
        viewModelScope.launch {
            println("TrackLocationViewModel.loadRoute viewer=($viewerLat,$viewerLon) target=($targetLat,$targetLon)")
            when (val result = repository.fetchRoute(viewerLat, viewerLon, targetLat, targetLon)) {
                is Result.Success -> {
                    val points = result.getOrNull().orEmpty()
                    _uiState.update {
                        it.copy(routePoints = points)
                    }
                }

                else -> {
                    println("TrackLocationViewModel.loadRoute failed: ${result.exceptionOrNull()?.message}")
                }
            }
        }
    }

    /**
     * Haversine distance in kilometers between viewer and target.
     */
    private fun computeDistanceKm(
        viewer: Pair<Double, Double>?,
        target: RemoteLocationData?
    ): Double? {
        if (viewer == null || target == null) return null

        val (lat1, lon1) = viewer
        val lat2 = target.latitude
        val lon2 = target.longitude

        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }
}

