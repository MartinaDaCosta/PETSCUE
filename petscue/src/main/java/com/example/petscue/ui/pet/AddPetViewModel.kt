package com.example.petscue.ui.pet

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.Pet
import com.example.petscue.data.repository.PetRepository
import com.example.petscue.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddPetUiState(
    val nombre: String = "",
    val especie: String = "",
    val raza: String = "",
    val genero: String = "",
    val edad: String = "",
    val peso: String = "",
    val descripcion: String = "",
    val ubicacion: String = "",
    val estado: String = "propia",
    val photoUris: List<Uri> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddPetViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPetUiState())
    val uiState: StateFlow<AddPetUiState> = _uiState.asStateFlow()

    fun onNombreChange(value: String) {
        _uiState.update { it.copy(nombre = value) }
    }

    fun onEspecieChange(value: String) {
        _uiState.update { it.copy(especie = value) }
    }

    fun onRazaChange(value: String) {
        _uiState.update { it.copy(raza = value) }
    }

    fun onGeneroChange(value: String) {
        _uiState.update { it.copy(genero = value) }
    }

    fun onEdadChange(value: String) {
        _uiState.update { it.copy(edad = value) }
    }

    fun onPesoChange(value: String) {
        _uiState.update { it.copy(peso = value) }
    }

    fun onDescripcionChange(value: String) {
        _uiState.update { it.copy(descripcion = value) }
    }

    fun onUbicacionChange(value: String) {
        _uiState.update { it.copy(ubicacion = value) }
    }

    fun onEstadoChange(value: String) {
        _uiState.update { it.copy(estado = value) }
    }

    fun onPhotosSelected(uris: List<Uri>) {
        _uiState.update {
            it.copy(
                photoUris = uris,
                error = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun savePet() {
        val state = _uiState.value

        if (state.nombre.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es obligatorio.") }
            return
        }

        if (state.especie.isBlank()) {
            _uiState.update { it.copy(error = "La especie es obligatoria.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            runCatching {
                val user = profileRepository.getCurrentUserProfile()
                val petId = UUID.randomUUID().toString()

                val uploadedUrls = petRepository.uploadPetImages(
                    petId = petId,
                    imageUris = state.photoUris
                )

                val pet = Pet(
                    id = petId,
                    nombre = state.nombre.trim(),
                    especie = state.especie.trim(),
                    raza = state.raza.trim(),
                    genero = state.genero.trim(),
                    edad = state.edad.trim(),
                    peso = state.peso.trim(),
                    descripcion = state.descripcion.trim(),
                    ubicacion = state.ubicacion.trim(),
                    latitud = 0.0,
                    longitud = 0.0,
                    fotos = uploadedUrls,
                    userId = user.uid,
                    userName = buildString {
                        append(user.nombre)
                        if (user.apellido.isNotBlank()) {
                            append(" ")
                            append(user.apellido)
                        }
                    }.trim(),
                    userAvatar = user.photoUrl,
                    estado = state.estado.trim().ifBlank { "propia" },
                    timestamp = System.currentTimeMillis()
                )

                petRepository.insert(pet)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSaved = true
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "No se pudo guardar la mascota."
                    )
                }
            }
        }
    }
}