package com.example.voyager.ui.viewmodel


import com.example.voyager.data.model.Destination
import com.example.voyager.data.model.Experience

data class MainUiState(
    val destinations: List<Destination> = emptyList(),
    val experiences: List<Experience> = emptyList(),
    val savedDestinations: List<Destination> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val hasLocationPermission: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)