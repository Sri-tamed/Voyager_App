package com.example.voyager.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.voyager.data.model.UserLocation
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen(userLocation: UserLocation?) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)

                    userLocation?.let {
                        val point = GeoPoint(it.latitude, it.longitude)
                        controller.setCenter(point)

                        val marker = Marker(this)
                        marker.position = point
                        marker.setAnchor(
                            Marker.ANCHOR_CENTER,
                            Marker.ANCHOR_BOTTOM
                        )
                        marker.title = "You are here"
                        overlays.add(marker)
                    }
                }
            },
            update = { mapView ->
                userLocation?.let {
                    val point = GeoPoint(it.latitude, it.longitude)
                    mapView.controller.setCenter(point)
                }
            }
        )
    }
}
