package com.example.petscue.ui.profile.pet.editpet

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.Pet
import com.example.petscue.data.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditPhotoItem(
    val remoteUrl: String? = null,
    val localUri: Uri? = null
) {
    val preview: Any
        get() = localUri ?: remoteUrl.orEmpty()
}

data class EditAdoptionPetUiState(
    val petId: String = "",
    val nombre: String = "",
    val especie: String = "",
    val raza: String = "",
    val genero: String = "",
    val edad: String = "",
    val peso: String = "",
    val descripcion: String = "",
    val ubicacion: String = "",
    val estado: String = "",
    val photoItems: List<EditPhotoItem> = emptyList(),
    val originalPet: Pet? = null,
    val isInitialLoading: Boolean = true,
    val isLoading: Boolean = false,
    val isUpdated: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditAdoptionPetViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val petRepository: PetRepository
) : ViewModel() {

    private val petId: String = checkNotNull(savedStateHandle["petId"])

    private val _uiState = MutableStateFlow(EditAdoptionPetUiState(petId = petId))
    val uiState: StateFlow<EditAdoptionPetUiState> = _uiState.asStateFlow()

    init {
        loadPet()
    }

    private fun loadPet() {
        viewModelScope.launch {
            runCatching {
                petRepository.getAdoptionPetById(petId)
                    ?: error("No se encontró la mascota.")
            }.onSuccess { pet ->
                _uiState.update {
                    it.copy(
                        nombre = pet.nombre,
                        especie = pet.especie,
                        raza = pet.raza,
                        genero = pet.genero,
                        edad = pet.edad,
                        peso = pet.peso,
                        descripcion = pet.descripcion,
                        ubicacion = pet.ubicacion,
                        estado = pet.estado,
                        photoItems = pet.fotos.map { url ->
                            EditPhotoItem(remoteUrl = url)
                        },
                        originalPet = pet,
                        isInitialLoading = false,
                        error = null
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isInitialLoading = false,
                        error = e.message ?: "No se pudo cargar la mascota."
                    )
                }
            }
        }
    }

    fun onNombreChange(value: String) {
        _uiState.update { it.copy(nombre = value, error = null) }
    }

    fun onEspecieChange(value: String) {
        _uiState.update { it.copy(especie = value, error = null) }
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
                photoItems = current.photoItems + uris.distinct().map { uri ->
                    EditPhotoItem(localUri = uri)
                },
                error = null
            )
        }
    }

    fun updatePet() {
        val state = _uiState.value
        val original = state.originalPet ?: return

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
            _uiState.update { it.copy(isLoading = true, error = null) }

            runCatching {
                val remoteUrls = state.photoItems
                    .mapNotNull { it.remoteUrl }

                val newUris = state.photoItems
                    .mapNotNull { it.localUri }

                val uploadedUrls = if (newUris.isNotEmpty()) {
                    petRepository.uploadPetImages(
                        petId = state.petId,
                        imageUris = newUris
                    )
                } else {
                    emptyList()
                }

                val updatedPet = original.copy(
                    nombre = state.nombre.trim(),
                    especie = state.especie.trim(),
                    raza = state.raza.trim(),
                    genero = state.genero.trim(),
                    edad = state.edad.trim(),
                    peso = state.peso.trim(),
                    descripcion = state.descripcion.trim(),
                    ubicacion = state.ubicacion.trim(),
                    estado = state.estado.trim().ifBlank { "en adopción" },
                    fotos = remoteUrls + uploadedUrls
                )

                petRepository.updateAdoptionPet(updatedPet)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isUpdated = true
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "No se pudo actualizar la mascota."
                    )
                }
            }
        }
    }
}