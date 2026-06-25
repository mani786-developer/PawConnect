package com.example.authapp.presentation.vets


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authapp.model.Vet
import com.example.authapp.domain.repository.VetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FindVetsViewModel @Inject constructor(
    private val vetRepository: VetRepository
) : ViewModel() {

    sealed class UiState {
        object Idle    : UiState()
        object Loading : UiState()
        data class Success(val vets: List<Vet>) : UiState()
        data class Error(val message: String)   : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    // Holds the full unfiltered list for client-side filtering
    private var allVets: List<Vet> = emptyList()

    fun loadAllVets() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            vetRepository.getAllVets()
                .onSuccess { vets ->
                    allVets = vets
                    _uiState.value = UiState.Success(vets)
                }
                .onFailure {
                    _uiState.value = UiState.Error(it.message ?: "Failed to load vets")
                }
        }
    }

    fun filterVets(name: String = "", city: String = "", specialization: String = "") {
        val filtered = allVets.filter { vet ->

            val matchesName = name.isBlank() ||
                    vet.displayName.contains(name, ignoreCase = true) ||
                    vet.clinicName.contains(name, ignoreCase = true)

            val matchesCity = city.isBlank() ||
                    city == "All" ||
                    vet.city.trim().equals(city.trim(), ignoreCase = true)

            val matchesSpecialization = specialization.isBlank() ||
                    specialization == "All" ||
                    vet.specialization.trim().equals(specialization.trim(), ignoreCase = true)

            matchesName && matchesCity && matchesSpecialization
        }

        _uiState.value = UiState.Success(filtered)
    }

    fun getVetById(uid: String): Vet? = allVets.find { it.uid == uid }
}