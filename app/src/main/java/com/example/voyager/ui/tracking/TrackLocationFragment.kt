package com.example.voyager.ui.tracking

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.voyager.R
import com.example.voyager.ui.viewmodel.TrackLocationViewModel
import com.example.voyager.ui.viewmodel.TrackLocationUiState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import android.preference.PreferenceManager

/**
 * Fragment that shows live tracking of a target user using OSMDroid.
 *
 * Entry point: requires a userId argument.
 */
class TrackLocationFragment : Fragment() {

    companion object {
        private const val ARG_USER_ID = "userId"

        fun newInstance(userId: String): TrackLocationFragment {
            val args = Bundle().apply { putString(ARG_USER_ID, userId) }
            val fragment = TrackLocationFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel: TrackLocationViewModel by viewModels()

    private var mapView: MapView? = null
    private var targetMarker: Marker? = null
    private var viewerMarker: Marker? = null
    private var routePolyline: Polyline? = null

    private var btnStartTracking: Button? = null
    private var btnToggleRoute: Button? = null
    private var infoText: TextView? = null
    private var statusText: TextView? = null

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location: Location = result.lastLocation ?: return
            val lat = location.latitude
            val lon = location.longitude
            println("TrackLocationFragment: viewer location update=($lat,$lon)")
            viewModel.updateViewerLocation(lat, lon)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_track_location, container, false)

        mapView = view.findViewById(R.id.mapView)
        btnStartTracking = view.findViewById(R.id.btnStartTracking)
        btnToggleRoute = view.findViewById(R.id.btnToggleRoute)
        infoText = view.findViewById(R.id.txtInfo)
        statusText = view.findViewById(R.id.txtStatus)

        setupMap()
        setupButtons()

        val userId = arguments?.getString(ARG_USER_ID)
        if (!userId.isNullOrBlank()) {
            viewModel.startPolling(userId)
        } else {
            Toast.makeText(requireContext(), "Missing user id", Toast.LENGTH_SHORT).show()
        }

        observeState()

        return view
    }

    private fun setupMap() {
        val ctx = requireContext().applicationContext
        Configuration.getInstance().load(
            ctx,
            PreferenceManager.getDefaultSharedPreferences(ctx)
        )

        mapView?.let { map ->
            map.setTileSource(TileSourceFactory.MAPNIK)
            map.setMultiTouchControls(true)
            map.controller.setZoom(15.0)
        }
    }

    private fun setupButtons() {
        btnStartTracking?.setOnClickListener {
            startViewerLocationUpdates()
        }

        btnToggleRoute?.setOnClickListener {
            viewModel.toggleRoute()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                renderState(state)
            }
        }
    }

    private fun renderState(state: TrackLocationUiState) {
        val map = mapView ?: return

        // Loading / expired states
        when {
            state.isLoading -> {
                statusText?.visibility = View.VISIBLE
                statusText?.text = "CONNECTING..."
            }
            state.trackingExpired -> {
                statusText?.visibility = View.VISIBLE
                statusText?.text = "Tracking link has expired"
            }
            else -> {
                statusText?.visibility = View.GONE
            }
        }

        // Target marker
        state.targetLocation?.let { target ->
            val point = GeoPoint(target.latitude, target.longitude)
            if (targetMarker == null) {
                targetMarker = Marker(map).apply {
                    title = "Target"
                    position = point
                }
                map.overlays.add(targetMarker)
                map.controller.animateTo(point)
            } else {
                targetMarker?.position = point
            }
        }

        // Viewer marker
        state.viewerLocation?.let { (lat, lon) ->
            val point = GeoPoint(lat, lon)
            if (viewerMarker == null) {
                viewerMarker = Marker(map).apply {
                    title = "You"
                    position = point
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    // Simple blue dot
                    icon = context?.getDrawable(R.drawable.ic_viewer_dot)
                }
                map.overlays.add(viewerMarker)
            } else {
                viewerMarker?.position = point
            }
        }

        // Route polyline
        if (state.isRouteVisible && state.routePoints.isNotEmpty()) {
            val points = state.routePoints.map { (lat, lon) -> GeoPoint(lat, lon) }
            if (routePolyline == null) {
                routePolyline = Polyline().apply {
                    outlinePaint.color = Color.parseColor("#3b82f6")
                    outlinePaint.strokeWidth = 10f
                }
                map.overlays.add(routePolyline)
            }
            routePolyline?.setPoints(points)
        } else {
            if (routePolyline != null) {
                map.overlays.remove(routePolyline)
                routePolyline = null
            }
        }

        // Info card content
        val target = state.targetLocation
        val distanceText = state.distanceKm?.let { String.format("Distance: %.2f km", it) } ?: ""
        if (target != null) {
            val latText = String.format("%.6f", target.latitude)
            val lonText = String.format("%.6f", target.longitude)
            val updatedText = target.updatedAt
            infoText?.text = buildString {
                append("Target: $latText, $lonText\n")
                append("Updated: $updatedText\n")
                if (distanceText.isNotEmpty()) append(distanceText)
            }
        } else {
            infoText?.text = distanceText
        }

        // Route button visibility/text
        btnToggleRoute?.visibility =
            if (state.viewerLocation != null && !state.trackingExpired) View.VISIBLE else View.GONE
        btnToggleRoute?.text = if (state.isRouteVisible) "STOP ROUTE" else "DRAW ROUTE"

        map.invalidate()
    }

    private fun startViewerLocationUpdates() {
        if (!hasLocationPermission()) {
            Toast.makeText(requireContext(), "Location permission required", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val request = LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                3_000L
            )
                .setMinUpdateIntervalMillis(3_000L)
                .build()

            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                requireActivity().mainLooper
            )

            Toast.makeText(requireContext(), "Live tracking started", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Location permission missing", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopViewerLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun hasLocationPermission(): Boolean {
        val context = requireContext()
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
        stopViewerLocationUpdates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView = null
        targetMarker = null
        viewerMarker = null
        routePolyline = null
        btnStartTracking = null
        btnToggleRoute = null
        infoText = null
        statusText = null
    }
}

