// ui/admin/AdminApprovalViewModel.kt
package com.example.petscue.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.User
import com.example.petscue.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminApprovalUiState(
    val isLoading: Boolean = true,
    val users: List<User> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AdminApprovalViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AdminApprovalUiState()
    )

    val uiState: StateFlow<AdminApprovalUiState> = _uiState.asStateFlow()

    private var pendingRequestsJob: Job? = null

    init {
        observePendingProtectoras()
    }

    private fun observePendingProtectoras() {
        pendingRequestsJob?.cancel()

        pendingRequestsJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            repository.observePendingProtectoras()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message
                                ?: "No se pudieron cargar las solicitudes."
                        )
                    }
                }
                .collect { users ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            users = users
                        )
                    }
                }
        }
    }

    fun refresh() {
        observePendingProtectoras()
    }

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
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            errorMessage = error.message
                                ?: "No se pudo aprobar la protectora.",
                            successMessage = null
                        )
                    }
                }
        }
    }

    fun reject(
        uid: String,
        reason: String
    ) {
        viewModelScope.launch {
            repository.rejectProtectora(uid, reason)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            successMessage = "Solicitud rechazada correctamente.",
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            errorMessage = error.message
                                ?: "No se pudo rechazar la protectora.",
                            successMessage = null
                        )
                    }
                }
        }
    }
}