package com.example.petscue.ui.mapa

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.repository.AlertRepository
import com.example.petscue.data.repository.AuthRepository
import com.example.petscue.data.repository.UserLocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapaViewModel @Inject constructor(
    private val alertRepository: AlertRepository,
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle,
    private val userLocationRepository: UserLocationRepository,
) : ViewModel() {

    private val initialRadio = savedStateHandle["radioNotificaciones"] ?: 1500.0

    private val _uiState = MutableStateFlow(
        MapaUiState(
            radioNotificaciones = initialRadio,
            currentUserId = authRepository.getCurrentUserId()
        )
    )
    val uiState: StateFlow<MapaUiState> = _uiState.asStateFlow()

    init {
        observeAlerts()
    }

    fun onRadioChanged(value: Double, currentLat: Double?, currentLng: Double?) {
        savedStateHandle["radioNotificaciones"] = value
        _uiState.update { it.copy(radioNotificaciones = value) }

        if (currentLat != null && currentLng != null) {
            userLocationRepository.updateUserLocation(
                lat = currentLat,
                lng = currentLng,
                notificationsEnabled = true,
                notificationRadius = value
            )
        }
    }

    fun deleteAlert(petId: String, onDeleted: (() -> Unit)? = null) {
        viewModelScope.launch {
            runCatching {
                alertRepository.deleteAlertByPetId(petId)
            }.onSuccess {
                onDeleted?.invoke()
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    fun updateMyLocation(lat: Double, lng: Double) {
        userLocationRepository.updateUserLocation(
            lat = lat,
            lng = lng,
            notificationsEnabled = true,
            notificationRadius = _uiState.value.radioNotificaciones
        )
    }

    fun updateNotificationRadius(radius: Double) {
        savedStateHandle["radioNotificaciones"] = radius
        _uiState.update { it.copy(radioNotificaciones = radius) }
    }
    fun isMyAlert(userId: String): Boolean {
        return userId.isNotBlank() && userId == _uiState.value.currentUserId
    }

    private fun observeAlerts() {
        viewModelScope.launch {
            alertRepository.getAllAlerts()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
                }
                .collectLatest { alerts ->
                    _uiState.update {
                        it.copy(
                            alerts = alerts,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
}