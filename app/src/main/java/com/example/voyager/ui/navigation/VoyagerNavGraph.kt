package com.example.voyager.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.voyager.data.model.DangerLevel
import com.example.voyager.ui.screens.EmergencyModeScreen
import com.example.voyager.ui.screens.MapScreen
import com.example.voyager.ui.screens.ProfileScreen
import com.example.voyager.ui.screens.explore.HomeDashboard
import com.example.voyager.ui.theme.VoyagerColors
import com.google.android.gms.maps.model.LatLng

// ---------------------------------------------------------------------------
// Single source of truth for every route string in the app.
// ---------------------------------------------------------------------------
sealed class Screens(val route: String) {
    object Home          : Screens("home")
    object Map           : Screens("map")
    object Explore       : Screens("explore")
    object Profile       : Screens("profile")
    object Emergency     : Screens("emergency")
    object PrivacyPolicy : Screens("privacy_policy")
}

@Composable
fun VoyagerNavGraph(
    navController: NavHostController,
    userLocation: LatLng,
    userCity: String,
    dangerLevel: DangerLevel,
    hasLocationPermission: Boolean,
    onRequestLocationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController  = navController,
        startDestination = Screens.Home.route,
        modifier       = modifier
    ) {

        // ----------------------------------------------------------
        // HOME
        // ----------------------------------------------------------
        composable(Screens.Home.route) {
            HomeDashboard(
                userLocation          = userLocation,
                userCity              = userCity,
                dangerLevel           = dangerLevel,
                hasLocationPermission = hasLocationPermission,
                onEmergencyClick = {
                    navController.navigate(Screens.Emergency.route) { launchSingleTop = true }
                },
                onPermissionClick = {
                    onRequestLocationPermission()
                }
            )
        }

        // ----------------------------------------------------------
        // EMERGENCY
        // ----------------------------------------------------------
        composable(Screens.Emergency.route) {
            EmergencyModeScreen(
                onCancel = { navController.popBackStack() }
            )
        }

        // ----------------------------------------------------------
        // MAP
        // ----------------------------------------------------------
        composable(Screens.Map.route) {
            MapScreen(
                userLocation = userLocation,
                userCity     = userCity,
                onBackClick  = { navController.popBackStack() }
            )
        }

        // ----------------------------------------------------------
        // EXPLORE — dynamic scrollable screen.
        // userLocation and userCity are live values from MainActivity;
        // every time GPS fires a new fix, this recomposes automatically.
        //
        // When your MainViewModel + custom components are ready, replace
        // this block with the real ExploreScreen composable (see comment
        // at the bottom of this file).
        // ----------------------------------------------------------
        composable(Screens.Explore.route) {
            ExploreScreenDynamic(
                userLocation = userLocation,
                userCity     = userCity
            )
        }

        // ----------------------------------------------------------
        // PROFILE — receives the live city so the subtitle updates
        // in real time as the user moves.
        // ----------------------------------------------------------
        composable(Screens.Profile.route) {
            ProfileScreen(userCity = userCity)
        }

        // ----------------------------------------------------------
        // PRIVACY POLICY — uncomment when PrivacyPolicyScreen exists:
        //
        //   composable(Screens.PrivacyPolicy.route) {
        //       com.example.voyager.ui.screens.PrivacyPolicyScreen(
        //           onBackClick = { navController.popBackStack() }
        //       )
        //   }
        // ----------------------------------------------------------
    }
}

// ---------------------------------------------------------------------------
// Dynamic Explore placeholder
//
// Uses LazyColumn so the list is fully scrollable regardless of item count.
// Every card reads from userLocation / userCity — both are live mutableStateOf
// values from MainActivity, so the whole list recomposes when GPS updates.
//
// The destination data is hardcoded here for now. When your backend / ViewModel
// is ready, replace SAMPLE_DESTINATIONS with a flow from MainViewModel and
// swap this composable with the real ExploreScreen.
// ---------------------------------------------------------------------------

private data class ExploreItem(
    val name: String,
    val tagline: String,
    val rating: Double,
    val reviews: Int,
    val price: String,
    val emoji: String
)

private val SAMPLE_DESTINATIONS = listOf(
    ExploreItem("Victoria Memorial",  "Iconic colonial landmark",          4.7, 2140, "₹0",      "🏛️"),
    ExploreItem("Howrah Bridge",      "Engineering marvel over Hooghly",  4.5, 1890, "₹0",      "🌉"),
    ExploreItem("New Market",         "Historic shopping district",       4.2, 980,  "Varies",  "🛍️"),
    ExploreItem("Eden Gardens",       "Home of cricket legends",          4.6, 1450, "₹150+",   "🏟️"),
    ExploreItem("South Street",       "Legendary street food paradise",   4.8, 2300, "₹200+",   "🍜"),
    ExploreItem("Dakkhineswar Kali",  "Sacred riverside temple",         4.9, 1780, "₹0",      "🛕"),
    ExploreItem("Kalighat Temple",    "Ancient city of devotion",         4.7, 1560, "₹0",      "🙏"),
    ExploreItem("Botanical Garden",   "100-year-old banyan paradise",     4.4, 870,  "₹30",     "🌳"),
    ExploreItem("Planetarium",        "Journey through the cosmos",       4.3, 640,  "₹80",     "🌌"),
    ExploreItem("Kumartuli",          "Idol sculptor's neighbourhood",    4.6, 1100, "₹0",      "🎭")
)

@Composable
fun ExploreScreenDynamic(
    userLocation: LatLng,
    userCity: String
) {
    // LazyColumn handles arbitrarily long lists with zero performance penalty —
    // only visible items are composed at any time.
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VoyagerColors.CreamBackground),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {

        // ── Hero header — updates city in real time ──
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                VoyagerColors.SunsetGold.copy(alpha = 0.6f),
                                VoyagerColors.CreamBackground
                            )
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Text(
                        text       = "Explore",
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = VoyagerColors.DarkCharcoal
                    )
                    // ── DYNAMIC: city name from live GPS ──
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Filled.LocationOn,
                            contentDescription = "Current location",
                            tint               = VoyagerColors.DarkCharcoal,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text     = "Near $userCity  •  ${String.format("%.4f", userLocation.latitude)}, ${String.format("%.4f", userLocation.longitude)}",
                            fontSize = 14.sp,
                            color    = VoyagerColors.MediumGray
                        )
                    }
                }
            }
        }

        // ── Section label ──
        item {
            Text(
                text       = "Popular in $userCity",   // ── DYNAMIC: city updates live ──
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = VoyagerColors.DarkCharcoal,
                modifier   = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }

        // ── Destination cards — one LazyColumn item per card ──
        items(SAMPLE_DESTINATIONS, key = { it.name }) { item ->
            ExploreCard(item = item)
        }
    }
}

@Composable
private fun ExploreCard(item: ExploreItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { /* navigate to detail when ready */ },
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = VoyagerColors.WarmIvory),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji thumbnail
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = VoyagerColors.SunsetGold.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.emoji, fontSize = 30.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Title + tagline + price
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name,    fontSize = 16.sp, fontWeight = FontWeight.Bold,      color = VoyagerColors.DarkCharcoal)
                Text(text = item.tagline, fontSize = 13.sp, color = VoyagerColors.MediumGray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.price,   fontSize = 13.sp, fontWeight = FontWeight.SemiBold,  color = VoyagerColors.SunsetGold)
            }

            // Rating badge
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint               = VoyagerColors.SunsetGold,
                        modifier           = Modifier.size(16.dp)
                    )
                    Text(text = " ${item.rating}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VoyagerColors.DarkCharcoal)
                }
                Text(text = "${item.reviews}", fontSize = 11.sp, color = VoyagerColors.MediumGray)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// When your MainViewModel is ready, replace the ExploreScreenDynamic block
// in the NavHost above with:
//
//   composable(Screens.Explore.route) {
//       val vm = viewModel<com.example.voyager.ui.viewmodel.MainViewModel>()
//       com.example.voyager.ui.screens.explore.ExploreScreen(
//           viewModel          = vm,
//           onDestinationClick = { id -> /* navigate to detail */ },
//           onMapClick         = {
//               navController.navigate(Screens.Map.route) { launchSingleTop = true }
//           }
//       )
//   }
// ---------------------------------------------------------------------------