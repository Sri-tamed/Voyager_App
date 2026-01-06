package com.example.voyager

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.example.voyager.data.model.EmergencyState
import com.example.voyager.data.model.TriggerSource
import com.example.voyager.ui.screens.*
import com.example.voyager.ui.theme.VoyagerTheme
import com.example.voyager.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VoyagerTheme {
                VoyagerApp()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoyagerApp() {
    val viewModel: MainViewModel = viewModel()
    var showSplash by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf("home") }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )
    )

    val userLocation by viewModel.userLocation.collectAsState()
    val emergencyState by viewModel.emergencyState.collectAsState()
    val dangerLevel by viewModel.currentDangerLevel.collectAsState()

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            viewModel.startLocationTracking()
        }
    }

    if (showSplash) {
        SplashScreen(onTimeout = {
            showSplash = false
            if (!permissionsState.allPermissionsGranted) {
                permissionsState.launchMultiplePermissionRequest()
            }
        })
    } else {
        when (emergencyState) {
            is EmergencyState.Active -> {
                EmergencyModeScreen(
                    onCancel = { viewModel.cancelEmergency() }
                )
            }
            else -> {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentScreen == "home",
                                onClick = { currentScreen = "home" },
                                icon = { Text("🏠") },
                                label = { Text("Home") }
                            )
                            NavigationBarItem(
                                selected = currentScreen == "map",
                                onClick = { currentScreen = "map" },
                                icon = { Text("🗺️") },
                                label = { Text("Map") }
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    viewModel.triggerEmergency(
                                        TriggerSource.MANUAL
                                    )
                                },
                                icon = { Text("🚨") },
                                label = { Text("Emergency") }
                            )
                        }
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        when (currentScreen) {
                            "home" -> HomeDashboard(
                                userLocation = userLocation,
                                dangerLevel = dangerLevel,
                                onEmergencyClick = {
                                    viewModel.triggerEmergency(
                                        TriggerSource.MANUAL
                                    )
                                }
                            )
                            "map" -> MapScreen(
                                userLocation = userLocation
                            )
                        }
                    }
                }
            }
        }
    }
}
