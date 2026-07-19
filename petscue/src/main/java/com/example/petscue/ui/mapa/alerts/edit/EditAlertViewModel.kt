package com.example.petscue.ui.mapa.alerts.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.AvisoMapa
import com.example.petscue.data.repository.EditAlertRepository
import com.example.petscue.ui.novedades.location.SelectedLocation
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditAlertViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: EditAlertRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val alertId: String = savedStateHandle.get<String>("alertId").orEmpty()
    private var currentAlert: AvisoMapa? = null

    private val _uiState = MutableStateFlow(EditAlertUiState())
    val uiState: StateFlow<EditAlertUiState> = _uiState.asStateFlow()

    init {
        loadAlert()
    }

    private fun loadAlert() {
        if (alertId.isBlank()) {
            _uiState.value = EditAlertUiState(
                isLoading = false,
                errorMessage = "Falta el identificador del aviso"
            )
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            runCatching {
                repository.getAlert(alertId)
            }.onSuccess { alert ->
                if (alert == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No se encontró el aviso"
                        )
                    }
                    return@onSuccess
                }

                currentAlert = alert

                _uiState.value = EditAlertUiState(
                    isLoading = false,
                    alertId = alert.id,
                    petId = alert.petId,
                    ownerId = alert.userId,
                    userName = alert.userName,
                    userPhotoUrl = alert.userPhotoUrl.ifBlank { alert.userAvatar },
                    nombreMascota = alert.nombreMascota,
                    fotoUrl = alert.fotoUrl,
                    tipoAviso = alert.tipoAviso,
                    sexo = alert.sexo.orEmpty(),
                    raza = alert.raza.orEmpty(),
                    edad = alert.edad.orEmpty(),
                    direccionAviso = alert.direccionAviso,
                    selectedLocation = SelectedLocation(
                        address = alert.direccionAviso,
                        lat = alert.lat,
                        lng = alert.lng
                    ),
                    radiusMeters = alert.radioMetros,
                    descripcion = alert.descripcion
                )
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Error cargando el aviso"
                    )
                }
            }
        }
    }

    fun onLocationSelected(location: SelectedLocation) {
        _uiState.update {
            it.copy(
                selectedLocation = location,
                direccionAviso = location.address,
                errorMessage = null
            )
        }
    }

    fun onRadiusChanged(value: Double) {
        _uiState.update { it.copy(radiusMeters = value) }
    }

    fun onDescripcionChange(value: String) {
        _uiState.update { it.copy(descripcion = value) }
    }

    fun save() {
        val state = _uiState.value
        val currentUserId = auth.currentUser?.uid.orEmpty()
        val baseAlert = currentAlert

        if (baseAlert == null) {
            _uiState.update {
                it.copy(errorMessage = "No se pudo cargar el aviso actual")
            }
            return
        }

        if (state.ownerId.isNotBlank() && state.ownerId != currentUserId) {
            _uiState.update {
                it.copy(errorMessage = "No puedes editar este aviso")
            }
            return
        }

        if (state.selectedLocation == null) {
            _uiState.update {
                it.copy(errorMessage = "Selecciona una ubicación")
            }
            return
        }


        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null
                )
            }

            val updatedAlert = baseAlert.copy(
                direccionAviso = state.selectedLocation.address,
                lat = state.selectedLocation.lat,
                lng = state.selectedLocation.lng,
                radioMetros = state.radiusMeters,
                descripcion = state.descripcion.trim()
            )

            runCatching {
                repository.updateAlert(updatedAlert)
            }.onSuccess {
                currentAlert = updatedAlert
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        success = true
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = throwable.message ?: "No se pudo guardar el aviso"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}