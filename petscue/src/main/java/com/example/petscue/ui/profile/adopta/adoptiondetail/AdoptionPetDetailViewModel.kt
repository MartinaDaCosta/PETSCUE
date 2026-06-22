package com.example.petscue.ui.profile.adopta.adoptiondetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.Pet
import com.example.petscue.data.repository.PetRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel
class AdoptionPetDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PetRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val petId: String = checkNotNull(savedStateHandle["petId"])

    private val _uiState = MutableStateFlow(AdoptionPetDetailUiState())
    val uiState: StateFlow<AdoptionPetDetailUiState> = _uiState.asStateFlow()

    init {
        loadPet()
    }

    private fun loadPet() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            runCatching {
                repository.getAdoptionPetById(petId)
                    ?: error("No se encontró la mascota.")
            }.onSuccess { pet ->
                val currentUserId = auth.currentUser?.uid.orEmpty()

                _uiState.value = AdoptionPetDetailUiState(
                    isLoading = false,
                    pet = pet,
                    isOwner = currentUserId.isNotBlank() && currentUserId == pet.userId,
                    isDeleted = false,
                    error = null
                )
            }.onFailure { e ->
                _uiState.value = AdoptionPetDetailUiState(
                    isLoading = false,
                    pet = null,
                    isOwner = false,
                    isDeleted = false,
                    error = e.message ?: "No se pudo cargar la mascota."
                )
            }
        }
    }

    fun deletePet() {
        val pet = _uiState.value.pet ?: return

        if (!_uiState.value.isOwner) {
            _uiState.update {
                it.copy(error = "No tienes permisos para eliminar esta mascota.")
            }
            return
        }

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