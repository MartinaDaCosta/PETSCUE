package com.example.petscue.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.User
import com.example.petscue.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado de la pantalla de revisión de protectoras
data class AdminApprovalUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AdminApprovalViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    // Estado mutable interno del ViewModel
    private val _uiState = MutableStateFlow(AdminApprovalUiState())

    // Estado público solo lectura para la UI
    val uiState: StateFlow<AdminApprovalUiState> = _uiState.asStateFlow()

    init {
        loadPendingProtectoras()
    }

    // Carga todas las protectoras pendientes de validación
    fun loadPendingProtectoras() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            repository.getPendingProtectoras()
                .onSuccess { users ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            users = users
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message
                                ?: "No se pudieron cargar las protectoras pendientes."
                        )
                    }
                }
        }
    }

    // Aprueba una protectora y recarga la lista
    fun approve(uid: String) {
        viewModelScope.launch {
            repository.approveProtectora(uid)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            successMessage = "Protectora aprobada correctamente.",
                            errorMessage = null
                        )
                    }
                    loadPendingProtectoras()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            errorMessage = e.message ?: "No se pudo aprobar la protectora.",
                            successMessage = null
                        )
                    }
                }
        }
    }

    // Rechaza una protectora con un motivo simple por defecto
    fun reject(uid: String, reason: String = "Solicitud rechazada") {
        viewModelScope.launch {
            repository.rejectProtectora(uid, reason)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            successMessage = "Protectora rechazada correctamente.",
                            errorMessage = null
                        )
                    }
                    loadPendingProtectoras()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            errorMessage = e.message ?: "No se pudo rechazar la protectora.",
                            successMessage = null
                        )
                    }
                }
        }
    }
}