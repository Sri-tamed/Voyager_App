package com.example.voyager.ui.screens.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.voyager.data.model.Destination
import com.example.voyager.data.model.Experience
import com.example.voyager.ui.components.*
import com.example.voyager.ui.theme.*
import com.example.voyager.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: MainViewModel,
    onDestinationClick: (String) -> Unit,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                // Hero section with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    GoldLight,
                                    BackgroundPrimary
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                            .padding(top = 60.dp)
                    ) {
                        Text(
                            text = "Explore",
                            style = MaterialTheme.typography.displayMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Your next adventure awaits",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Search bar
                        VoyagerSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onSearch = { viewModel.searchDestinations(it) },
                            placeholder = "Find destinations worldwide"
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Permission banner if needed
            if (!uiState.hasLocationPermission) {
                item {
                    PermissionBanner(
                        onRequestPermission = { viewModel.requestLocationPermission() },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }

            // Tabs: Discover | Saved
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = BackgroundPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .wrapContentSize(Alignment.BottomStart)
                                    .offset(x = tabPositions[selectedTab].left)
                                    .width(tabPositions[selectedTab].width)
                                    .height(3.dp)
                                    .background(color = VoyagerYellow)
                            )
                        }
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "Discover",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "Saved",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // Discover content
                    item {
                        Text(
                            text = "Popular Destinations",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        )
                    }

                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = uiState.destinations,
                                key = { destination: Destination -> destination.id }
                            ) { destination: Destination ->
                                DestinationCard(
                                    destination = destination,
                                    onCardClick = { onDestinationClick(destination.id) },
                                    onFavoriteClick = { viewModel.toggleFavorite(destination.id) },
                                    isFavorite = uiState.favoriteIds.contains(destination.id)
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    item {
                        Text(
                            text = "Experiences",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        )
                    }

                    items(
                        count = minOf(5, uiState.experiences.size),
                        key = { index: Int ->
                            if (index < uiState.experiences.size) {
                                uiState.experiences[index].id
                            } else {
                                "experience_$index"
                            }
                        }
                    ) { index: Int ->
                        if (index < uiState.experiences.size) {
                            val experience = uiState.experiences[index]
                            ExperienceCard(
                                experience = experience,
                                onClick = { /* Navigate to experience detail */ },
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                1 -> {
                    // Saved content
                    if (uiState.savedDestinations.isEmpty()) {
                        item {
                            EmptyState(
                                message = "No saved destinations yet",
                                actionText = "Explore destinations",
                                onActionClick = { selectedTab = 0 },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp)
                            )
                        }
                    } else {
                        items(
                            items = uiState.savedDestinations,
                            key = { destination: Destination -> destination.id }
                        ) { destination: Destination ->
                            DestinationCard(
                                destination = destination,
                                onCardClick = { onDestinationClick(destination.id) },
                                onFavoriteClick = { viewModel.toggleFavorite(destination.id) },
                                isFavorite = true,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}