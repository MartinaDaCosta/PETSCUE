package com.example.petscue.ui.profile.pet.petdetail

import android.net.Uri
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditPetUiState(
    val petId: String = "",
    val nombre: String = "",
    val especie: String = "",
    val raza: String = "",
    val genero: String = "",
    val edad: String = "",
    val peso: String = "",
    val descripcion: String = "",
    val ubicacion: String = "",
    val estado: String = "en casa",
    val currentPhotoUrls: List<String> = emptyList(),
    val newPhotoUris: List<Uri> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditPetViewModel @Inject constructor(
    private val petRepository: PetRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val petId: String = savedStateHandle.get<String>("petId").orEmpty()

    private val _uiState = MutableStateFlow(EditPetUiState())
    val uiState: StateFlow<EditPetUiState> = _uiState.asStateFlow()

    private var originalPet: Pet? = null

    init {
        loadPet()
    }

    private fun loadPet() {
        if (petId.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "No se recibió el id de la mascota."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            runCatching {
                val pets = petRepository.getAll().first()
                pets.firstOrNull { pet -> pet.id == petId }
            }.onSuccess { pet ->
                if (pet == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No se encontró la mascota."
                        )
                    }
                } else {
                    originalPet = pet
                    _uiState.update {
                        it.copy(
                            petId = pet.id,
                            nombre = pet.nombre,
                            especie = pet.especie,
                            raza = pet.raza,
                            genero = pet.genero,
                            edad = pet.edad,
                            peso = pet.peso,
                            descripcion = pet.descripcion,
                            ubicacion = pet.ubicacion,
                            estado = pet.estado,
                            currentPhotoUrls = pet.fotos,
                            newPhotoUris = emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar la mascota."
                    )
                }
            }
        }
    }

    fun onNombreChange(value: String) {
        _uiState.update { it.copy(nombre = value, error = null) }
    }

    fun onEspecieChange(value: String) {
        _uiState.update {
            it.copy(
                especie = value,
                raza = "",
                error = null
            )
        }
    }

    fun onRazaChange(value: String) {
        _uiState.update { it.copy(raza = value, error = null) }
    }

    fun onGeneroChange(value: String) {
        _uiState.update { it.copy(genero = value, error = null) }
    }

    fun onEdadChange(value: String) {
        _uiState.update { it.copy(edad = value, error = null) }
    }

    fun onPesoChange(value: String) {
        _uiState.update { it.copy(peso = value, error = null) }
    }

    fun onDescripcionChange(value: String) {
        _uiState.update { it.copy(descripcion = value, error = null) }
    }

    fun onUbicacionChange(value: String) {
        _uiState.update { it.copy(ubicacion = value, error = null) }
    }

    fun onEstadoChange(value: String) {
        _uiState.update { it.copy(estado = value, error = null) }
    }

    fun onPhotosSelected(uris: List<Uri>) {
        _uiState.update { current ->
            current.copy(
                newPhotoUris = (current.newPhotoUris + uris).distinct(),
                error = null
            )
        }
    }

    fun removeExistingPhoto(photoUrl: String) {
        _uiState.update { current ->
            current.copy(
                currentPhotoUrls = current.currentPhotoUrls.filterNot { it == photoUrl },
                error = null
            )
        }
    }

    fun removeNewPhoto(uri: Uri) {
        _uiState.update { current ->
            current.copy(
                newPhotoUris = current.newPhotoUris.filterNot { it == uri },
                error = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun saveChanges() {
        val state = _uiState.value
        val basePet = originalPet

        if (basePet == null) {
            _uiState.update { it.copy(error = "No se pudo cargar la mascota original.") }
            return
        }

        if (state.nombre.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es obligatorio.") }
            return
        }

        if (state.especie.isBlank()) {
            _uiState.update { it.copy(error = "La especie es obligatoria.") }
            return
        }

        if (state.raza.isBlank()) {
            _uiState.update { it.copy(error = "La raza es obligatoria.") }
            return
        }

        if (state.genero.isBlank()) {
            _uiState.update { it.copy(error = "El género es obligatorio.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            runCatching {
                val updatedPet = basePet.copy(
                    nombre = state.nombre.trim(),
                    especie = state.especie.trim(),
                    raza = state.raza.trim(),
                    genero = state.genero.trim(),
                    edad = state.edad.trim(),
                    peso = state.peso.trim(),
                    descripcion = state.descripcion.trim(),
                    ubicacion = state.ubicacion.trim(),
                    estado = state.estado.trim().ifBlank { basePet.estado },
                    fotos = state.currentPhotoUrls,
                    timestamp = System.currentTimeMillis()
                )

                petRepository.insert(updatedPet)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSaved = true
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "No se pudo actualizar la mascota."
                    )
                }
            }
        }
    }

    fun reload() {
        loadPet()
    }
}