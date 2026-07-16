package com.example.petscue.admin.ui.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.admin.data.model.ProtectoraRequest
import com.example.petscue.admin.data.repository.AdminRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminRequestsViewModel(
    private val repository: AdminRepositoryImpl =
        AdminRepositoryImpl(FirebaseFirestore.getInstance())
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AdminRequestsUiState(isLoading = true)
    )

    val uiState: StateFlow<AdminRequestsUiState> =
        _uiState.asStateFlow()

    private var requestsJob: Job? = null

    init {
        observeRequests()
    }

    private fun observeRequests() {
        requestsJob?.cancel()

        requestsJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            repository.observePendingRequests()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            requests = emptyList(),
                            errorMessage = error.message
                                ?: "Error al cargar solicitudes."
                        )
                    }
                }
                .collect { requests ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            requests = requests,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    fun refresh() {
        observeRequests()
    }

    fun approveRequest(request: ProtectoraRequest) {
        viewModelScope.launch {
            repository.approveRequest(request.id)
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            errorMessage = error.message
                                ?: "Error al aprobar la solicitud."
                        )
                    }
                }
        }
    }

    fun rejectRequest(
        requestId: String,
        motivo: String
    ) {
        if (motivo.trim().isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Debes indicar el motivo del rechazo."
                )
            }
            return
        }

        viewModelScope.launch {
            repository.rejectRequest(
                requestId = requestId,
                motivo = motivo.trim()
            )
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            errorMessage = error.message
                                ?: "Error al rechazar la solicitud."
                        )
                    }
                }
        }
    }
}