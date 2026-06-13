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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AlertType {
    LOST, FOUND, SEEN
}

@HiltViewModel
class CreateAlertViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val petRepository: PetRepository,
    private val profileRepository: ProfileRepository,
    private val alertRepository: AlertRepository
) : ViewModel() {

    private val petId: String = checkNotNull(savedStateHandle["petId"])

    private val _uiState = MutableStateFlow(
        CreateAlertUiState(petId = petId)
    )
    val uiState: StateFlow<CreateAlertUiState> = _uiState.asStateFlow()

    init {
        loadPet()
    }

    private fun loadPet() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            runCatching {
                val user = profileRepository.getCurrentUserProfile()
                val pets = petRepository.getByUserId(user.uid).first()
                pets.firstOrNull { it.id == petId }
                    ?: error("No se encontró la mascota.")
            }.onSuccess { pet ->
                _uiState.update {
                    it.copy(
                        pet = pet,
                        isLoading = false,
                        error = null
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "No se pudo cargar la mascota."
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

            runCatching {
                val alert = AvisoMapa(
                    id = "",
                    petId = pet.id,
                    userId = pet.userId,
                    nombreMascota = pet.nombre,
                    fotoUrl = pet.fotos.firstOrNull().orEmpty(),
                    tipoAviso = when (state.alertType) {
                        AlertType.LOST -> "PERDIDO"
                        AlertType.FOUND -> "ENCONTRADO"
                        AlertType.SEEN -> "VISTO"
                    },
                    direccionAviso = state.selectedLocation.address,
                    lat = state.selectedLocation.lat,
                    lng = state.selectedLocation.lng,
                    radioMetros = state.radiusMeters,
                    createdAt = System.currentTimeMillis()
                )

                alertRepository.insertAlert(alert)
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