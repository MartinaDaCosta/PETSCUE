package com.example.petscue.ui.mapa.alerts.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.AvisoMapa
import com.example.petscue.data.repository.AlertRepository
import com.example.petscue.data.repository.PetRepository
import com.example.petscue.data.repository.ProfileRepository
import com.example.petscue.ui.novedades.location.SelectedLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AlertType {
    LOST, FOUND, SEEN
}

@HiltViewModel
class CreateAlertViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val alertRepository: AlertRepository,
    private val profileRepository: ProfileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val petId: String = savedStateHandle.get<String>("petId") ?: ""

    private val _uiState = MutableStateFlow(CreateAlertUiState())
    val uiState: StateFlow<CreateAlertUiState> = _uiState.asStateFlow()

    init {
        loadPet()
    }

    private fun loadPet() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            runCatching {
                petRepository.getAnyPetById(petId)
            }.onSuccess { pet ->
                _uiState.update {
                    it.copy(
                        pet = pet,
                        petId = pet?.id.orEmpty(),
                        isLoading = false,
                        error = if (pet == null) "No se encontró la mascota" else null
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        pet = null,
                        isLoading = false,
                        error = e.message ?: "No se pudo cargar la mascota"
                    )
                }
            }
        }
    }

    fun onAlertTypeSelected(type: AlertType) {
        _uiState.update { it.copy(alertType = type, error = null) }
    }

    fun onLocationSelected(location: SelectedLocation) {
        _uiState.update { it.copy(selectedLocation = location, error = null) }
    }

    fun onRadiusChanged(value: Double) {
        _uiState.update { it.copy(radiusMeters = value) }
    }

    fun onDescripcionChange(value: String) {
        _uiState.update { it.copy(descripcion = value) }
    }

    fun saveAlert() {
        val state = _uiState.value
        val pet = state.pet

        if (pet == null) {
            _uiState.update { it.copy(error = "No hay mascota seleccionada.") }
            return
        }

        if (state.selectedLocation == null) {
            _uiState.update { it.copy(error = "Selecciona una ubicación para el aviso.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            val existing = alertRepository.getAlertByPetId(pet.id)
            if (existing != null) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Esta mascota ya tiene un aviso activo."
                    )
                }
                return@launch
            }

            runCatching {
                val user = profileRepository.getCurrentUserProfile()

                val alert = AvisoMapa(
                    id = pet.id,
                    petId = pet.id,
                    userId = pet.userId,
                    userName = buildString {
                        append(user.nombre)
                        if (user.apellido.isNotBlank()) {
                            append(" ")
                            append(user.apellido)
                        }
                    }.trim(),
                    userAvatar = user.photoUrl,
                    userPhotoUrl = user.photoUrl,
                    nombreMascota = pet.nombre,
                    fotoUrl = pet.fotos.firstOrNull().orEmpty(),
                    direccionAviso = state.selectedLocation.address,
                    tipoAviso = when (state.alertType) {
                        AlertType.LOST -> "PERDIDO"
                        AlertType.FOUND -> "ENCONTRADO"
                        AlertType.SEEN -> "VISTO"
                    },
                    sexo = pet.genero,
                    raza = pet.raza,
                    edad = pet.edad,
                    caracteristicas = pet.descripcion,
                    lat = state.selectedLocation.lat,
                    lng = state.selectedLocation.lng,
                    radioMetros = state.radiusMeters,
                    createdAt = System.currentTimeMillis(),
                    descripcion = state.descripcion.trim(),
                    alertaActiva = true
                )

                alertRepository.upsertAlert(alert)
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
                        error = e.message ?: "No se pudo guardar el aviso."
                    )
                }
            }
        }
    }
}