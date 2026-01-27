package com.example.voyager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voyager.data.model.Destination
import com.example.voyager.data.model.Experience
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Load mock data or from repository
            _uiState.update { currentState ->
                currentState.copy(
                    destinations = getMockDestinations(),
                    experiences = getMockExperiences()
                )
            }
        }
    }

    fun searchDestinations(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                loadInitialData()
                return@launch
            }

            // Filter destinations based on query
            val filtered = _uiState.value.destinations.filter { destination ->
                destination.name.contains(query, ignoreCase = true) ||
                        destination.country.contains(query, ignoreCase = true) ||
                        destination.category.contains(query, ignoreCase = true)
            }

            _uiState.update { it.copy(destinations = filtered) }
        }
    }

    fun toggleFavorite(destinationId: String) {
        _uiState.update { currentState ->
            val newFavorites = if (currentState.favoriteIds.contains(destinationId)) {
                currentState.favoriteIds - destinationId
            } else {
                currentState.favoriteIds + destinationId
            }

            val savedDestinations = currentState.destinations.filter {
                newFavorites.contains(it.id)
            }

            currentState.copy(
                favoriteIds = newFavorites,
                savedDestinations = savedDestinations
            )
        }
    }

    fun requestLocationPermission() {
        _uiState.update { it.copy(hasLocationPermission = true) }
    }

    // Mock data functions - replace with real repository calls
    private fun getMockDestinations(): List<Destination> {
        return listOf(
            Destination(
                id = "1",
                name = "Paris",
                country = "France",
                imageUrl = "https://images.unsplash.com/photo-1502602898657-3e91760cbb34",
                rating = 4.8,
                category = "Cultural"
            ),
            Destination(
                id = "2",
                name = "Tokyo",
                country = "Japan",
                imageUrl = "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf",
                rating = 4.9,
                category = "Urban"
            ),
            Destination(
                id = "3",
                name = "Bali",
                country = "Indonesia",
                imageUrl = "https://images.unsplash.com/photo-1537996194471-e657df975ab4",
                rating = 4.7,
                category = "Beach"
            ),
            Destination(
                id = "4",
                name = "New York",
                country = "USA",
                imageUrl = "https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9",
                rating = 4.6,
                category = "Urban"
            ),
            Destination(
                id = "5",
                name = "Dubai",
                country = "UAE",
                imageUrl = "https://images.unsplash.com/photo-1512453979798-5ea266f8880c",
                rating = 4.8,
                category = "Luxury"
            )
        )
    }

    private fun getMockExperiences(): List<Experience> {
        return listOf(
            Experience(
                id = "exp1",
                title = "Eiffel Tower Night Tour",
                description = "Experience the magic of Paris at night",
                price = "From $89",
                rating = 4.9,
                imageUrl = "https://images.unsplash.com/photo-1511739001486-6bfe10ce785f"
            ),
            Experience(
                id = "exp2",
                title = "Tokyo Food Walking Tour",
                description = "Discover authentic Japanese cuisine",
                price = "From $120",
                rating = 4.8,
                imageUrl = "https://images.unsplash.com/photo-1554797589-7241bb691973"
            ),
            Experience(
                id = "exp3",
                title = "Bali Temple & Rice Terrace",
                description = "Explore Bali's spiritual heart",
                price = "From $65",
                rating = 4.7,
                imageUrl = "https://images.unsplash.com/photo-1555400082-2c1d6b0635cb"
            ),
            Experience(
                id = "exp4",
                title = "NYC Helicopter Tour",
                description = "See New York from the sky",
                price = "From $250",
                rating = 4.9,
                imageUrl = "https://images.unsplash.com/photo-1546436836-07a91091f160"
            ),
            Experience(
                id = "exp5",
                title = "Dubai Desert Safari",
                description = "Adventure in the Arabian desert",
                price = "From $75",
                rating = 4.8,
                imageUrl = "https://images.unsplash.com/photo-1451337516015-6b6e9a44a8a3"
            )
        )
    }
}