package com.example.petscue.admin.ui.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.admin.data.model.ProtectoraRequest
import com.example.petscue.admin.data.repository.AdminRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
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

    private val _uiState = MutableStateFlow(AdminRequestsUiState(isLoading = true))
    val uiState: StateFlow<AdminRequestsUiState> = _uiState.asStateFlow()

    init {
        observeRequests()
    }

    private fun observeRequests() {
        viewModelScope.launch {
            repository.observePendingRequests()
                .catch { e ->
                    _uiState.value = AdminRequestsUiState(
                        isLoading = false,
                        requests = emptyList(),
                        errorMessage = e.message ?: "Error al escuchar solicitudes."
                    )
                }
                .collect { list ->
                    _uiState.value = AdminRequestsUiState(
                        isLoading = false,
                        requests = list,
                        errorMessage = null
                    )
                }
        }
    }

    fun approveRequest(request: ProtectoraRequest) {
        viewModelScope.launch {
            repository.approveRequest(request.id)
                .onFailure { e ->
                    _uiState.update {
                        it.copy(errorMessage = e.message ?: "Error al aprobar solicitud.")
                    }
                }
        }
    }

    fun rejectRequest(
        requestId: String,
        motivo: String = "Solicitud rechazada por el administrador"
    ) {
        viewModelScope.launch {
            repository.rejectRequest(requestId, motivo)
                .onFailure { e ->
                    _uiState.update {
                        it.copy(errorMessage = e.message ?: "Error al rechazar solicitud.")
                    }
                }
        }
    }
}