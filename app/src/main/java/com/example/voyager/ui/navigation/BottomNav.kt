package com.example.voyager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController

// ---------------------------------------------------------------------------
// Tab definitions — ONLY the four tabs that live in the bottom bar.
// Emergency is NOT here; it is navigated to via the button on HomeDashboard.
// Every route string must match exactly what VoyagerNavGraph registers.
// ---------------------------------------------------------------------------
private data class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val TABS = listOf(
    BottomTab(Screens.Home.route,    "Home",    Icons.Filled.Home),
    BottomTab(Screens.Map.route,     "Map",     Icons.Filled.Map),
    BottomTab(Screens.Explore.route, "Explore", Icons.Filled.Explore),
    BottomTab(Screens.Profile.route, "Profile", Icons.Filled.Person)
)

@Composable
fun BottomNav(navController: NavController) {
    // ---------------------------------------------------------------------------
    // No extension property. No delegate. No `by`.
    //
    // This is a plain mutableStateOf that we write to inside a
    // DisposableEffect listener. OnDestinationChangedListener is a core
    // NavController API — it exists on every single version of navigation,
    // compose or not. Every time NavHost switches the active screen, the
    // listener fires, we update currentRoute, Compose sees the state change,
    // and the NavigationBar recomposes with the correct tab highlighted.
    // ---------------------------------------------------------------------------
    var currentRoute by remember { mutableStateOf<String?>(null) }

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            currentRoute = destination.route
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    NavigationBar {
        TABS.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                label   = { Text(tab.label) },
                icon    = { Icon(tab.icon, contentDescription = tab.label) },
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(Screens.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}