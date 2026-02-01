package com.example.voyager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.voyager.ui.navigation.BottomNavItem
import com.example.voyager.ui.theme.VoyagerCream
import com.example.voyager.ui.theme.VoyagerYellow
import com.example.voyager.ui.theme.TextMuted

@Composable
fun VoyagerBottomBar(navController: NavController) {

    val items = listOf(
        BottomNavItem.Explore,
        BottomNavItem.Map,
        BottomNavItem.Emergency,
        BottomNavItem.Profile
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        containerColor = VoyagerCream,
        tonalElevation = 6.dp,
        modifier = Modifier
            .padding(14.dp)
            .clip(RoundedCornerShape(22.dp))
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(item.title)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VoyagerYellow,
                    selectedTextColor = VoyagerYellow,
                    indicatorColor = VoyagerYellow.copy(alpha = 0.18f),
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}