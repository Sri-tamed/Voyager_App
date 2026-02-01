package com.example.voyager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack  // Add this import
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.voyager.data.model.UserLocation
import com.example.voyager.ui.theme.VoyagerColors
import com.google.android.gms.maps.model.LatLng
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)  // Add this annotation to fix experimental API warning
@Composable
fun MapScreen(
    userLocation: LatLng?,  // Changed from UserLocation? to LatLng?
    userCity: String = "",  // Add city parameter
    onBackClick: () -> Unit  // Add back click parameter
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isOffline by remember { mutableStateOf(false) }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    // Initialize OSMDroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Scaffold(  // Use Scaffold for proper structure
        topBar = {
            TopAppBar(
                title = { Text("Live Map") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")  // Fixed: Icons.Filled.ArrowBack
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(VoyagerColors.CreamBackground)
        ) {
            // OSMDroid Map View
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        mapView = this

                        // Configure map
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)

                        // Set user location if available
                        userLocation?.let { location ->
                            val point = GeoPoint(location.latitude, location.longitude)
                            controller.setCenter(point)

                            // Add marker
                            val marker = Marker(this)
                            marker.position = point
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            marker.title = "You are here"
                            overlays.add(marker)
                        }
                    }
                },
                update = { map ->
                    userLocation?.let { location ->
                        val point = GeoPoint(location.latitude, location.longitude)
                        map.controller.setCenter(point)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Lifecycle management
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                        Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                        Lifecycle.Event.ON_DESTROY -> mapView?.onDetach()
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            // Offline banner at top
            if (isOffline) {
                OfflineBanner()
            }

            // Floating action buttons at bottom right
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingMapButton(
                    icon = Icons.Default.Share,
                    label = "Share Location",
                    onClick = { /* Share location logic */ }
                )

                FloatingMapButton(
                    icon = Icons.Default.MyLocation,
                    label = "Recenter",
                    onClick = {
                        userLocation?.let { location ->
                            mapView?.controller?.animateTo(
                                GeoPoint(location.latitude, location.longitude)
                            )
                        }
                    }
                )
            }

            // Location info card at bottom
            userLocation?.let { location ->
                LocationInfoCard(
                    location = location,
                    city = userCity,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(20.dp)
                )
            }
        }
    }
}

@Composable
private fun OfflineBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = VoyagerColors.CautionOrange.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.WifiOff,
                    contentDescription = "Offline",
                    tint = VoyagerColors.DarkCharcoal,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "You're offline",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = VoyagerColors.DarkCharcoal
                    )
                    Text(
                        text = "Showing last known location",
                        style = MaterialTheme.typography.bodySmall,
                        color = VoyagerColors.DarkCharcoal.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)  // Add this for FloatingActionButton
@Composable
private fun FloatingMapButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .size(56.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = VoyagerColors.SunsetGold.copy(alpha = 0.3f)
            ),
        containerColor = VoyagerColors.GlassWhite,
        contentColor = VoyagerColors.DarkCharcoal,
        shape = CircleShape
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun LocationInfoCard(
    location: LatLng,  // Changed from UserLocation to LatLng
    city: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = VoyagerColors.GlassWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 12.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Location",
                        style = MaterialTheme.typography.labelMedium,
                        color = VoyagerColors.MediumGray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = city,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = VoyagerColors.DarkCharcoal
                    )
                }

                // Signal strength indicator
                Icon(
                    imageVector = Icons.Outlined.SignalCellularAlt,
                    contentDescription = "Signal",
                    tint = VoyagerColors.SafeGreen,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Use HorizontalDivider for Material3
            Divider(
                color = VoyagerColors.MediumGray.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Coordinates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CoordinateItem("Latitude", String.format("%.4f", location.latitude))
                CoordinateItem("Longitude", String.format("%.4f", location.longitude))
            }
        }
    }
}

@Composable
private fun CoordinateItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = VoyagerColors.MediumGray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = VoyagerColors.DarkCharcoal
        )
    }
}