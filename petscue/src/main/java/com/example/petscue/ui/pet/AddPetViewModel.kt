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

@HiltViewModel
class AddPetViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPetUiState())
    val uiState: StateFlow<AddPetUiState> = _uiState.asStateFlow()

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
                photoUris = (current.photoUris + uris).distinct(),
                error = null
            )
        }
    }

    fun removePhoto(uri: Uri) {
        _uiState.update { current ->
            current.copy(
                photoUris = current.photoUris.filterNot { it == uri },
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