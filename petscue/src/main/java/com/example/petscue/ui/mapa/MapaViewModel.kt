package com.example.petscue.ui.mapa

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.repository.AlertRepository
import com.example.petscue.data.repository.AuthRepository
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
    private val savedStateHandle: SavedStateHandle
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

    fun onRadioChanged(value: Double) {
        savedStateHandle["radioNotificaciones"] = value
        _uiState.update { it.copy(radioNotificaciones = value) }
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