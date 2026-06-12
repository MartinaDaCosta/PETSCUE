package com.example.petscue.ui.profile.adoption

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.Pet
import com.example.petscue.data.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdoptionPetDetailUiState(
    val isLoading: Boolean = true,
    val pet: Pet? = null,
    val isDeleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdoptionPetDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PetRepository
) : ViewModel() {

    private val petId: String = checkNotNull(savedStateHandle["petId"])

    private val _uiState = MutableStateFlow(AdoptionPetDetailUiState())
    val uiState: StateFlow<AdoptionPetDetailUiState> = _uiState.asStateFlow()

    init {
        loadPet()
    }

    private fun loadPet() {
        viewModelScope.launch {
            runCatching {
                repository.getAdoptionPetById(petId)
                    ?: error("No se encontró la mascota.")
            }.onSuccess { pet ->
                _uiState.value = AdoptionPetDetailUiState(
                    isLoading = false,
                    pet = pet
                )
            }.onFailure { e ->
                _uiState.value = AdoptionPetDetailUiState(
                    isLoading = false,
                    error = e.message ?: "No se pudo cargar la mascota."
                )
            }
        }
    }

    fun deletePet() {
        val pet = _uiState.value.pet ?: return

        viewModelScope.launch {
            runCatching {
                repository.delete(pet)
            }.onSuccess {
                _uiState.update { it.copy(isDeleted = true) }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(error = e.message ?: "No se pudo eliminar la mascota.")
                }
            }
        }
    }
}