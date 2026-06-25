package com.example.authapp.presentation.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authapp.model.Pet
import com.example.authapp.domain.repository.AuthRepository
import com.example.authapp.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiscoverUiState>(DiscoverUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private var allPets: List<Pet> = emptyList()

    fun loadAllPets() {
        viewModelScope.launch {
            _uiState.value = DiscoverUiState.Loading
            val result = petRepository.getAllPets()

            if (result.isSuccess) {
                allPets = result.getOrThrow()
                showFiltered("All", "")
            } else {
                _uiState.value = DiscoverUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to load pets"
                )
            }
        }
    }

    fun filter(species: String, query: String) {
        showFiltered(species, query)
    }

    private fun showFiltered(species: String, query: String) {
        val currentUid = authRepository.getCurrentUid()

        val filtered = allPets.filter { pet ->
            val isNotMyPet = pet.ownerId != currentUid

            val matchesSpecies = species == "All" ||
                    pet.species.equals(species, ignoreCase = true)

            val matchesQuery = query.isBlank() ||
                    pet.name.contains(query, ignoreCase = true) ||
                    pet.breed.contains(query, ignoreCase = true)

            isNotMyPet && matchesSpecies && matchesQuery
        }

        _uiState.value = if (filtered.isEmpty()) {
            DiscoverUiState.Empty
        } else {
            DiscoverUiState.Success(filtered)
        }
    }
}

sealed class DiscoverUiState {
    object Idle : DiscoverUiState()
    object Loading : DiscoverUiState()
    object Empty : DiscoverUiState()
    data class Success(val pets: List<Pet>) : DiscoverUiState()
    data class Error(val message: String) : DiscoverUiState()
}