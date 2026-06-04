package com.example.petscue.ui.auth.pending

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.ApprovalStatus
import com.example.petscue.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PendingApprovalViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(PendingApprovalUiState())
    val uiState: StateFlow<PendingApprovalUiState> = _uiState.asStateFlow()

    init {
        observeApprovalStatus()
    }

    fun onNotesChange(value: String) {
        _uiState.update { it.copy(notes = value, errorMessage = null) }
    }

    fun onFileSelected(uri: Uri, fileName: String) {
        _uiState.update {
            it.copy(
                selectedFileUri = uri,
                selectedFileName = fileName,
                errorMessage = null
            )
        }
    }

    private fun observeApprovalStatus() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "Error al comprobar el estado.")
                    }
                    return@addSnapshotListener
                }

                val approvalStatus = snapshot?.getString("approvalStatus")
                if (approvalStatus == ApprovalStatus.APPROVED.name) {
                    _uiState.update { it.copy(isApproved = true) }
                }
            }
    }

    fun submitDocuments() {
        viewModelScope.launch {
            val fileUri = _uiState.value.selectedFileUri
                ?: run {
                    _uiState.update { it.copy(errorMessage = "Selecciona un documento primero.") }
                    return@launch
                }

            _uiState.update { it.copy(isUploading = true, errorMessage = null, infoMessage = null) }

            repository.uploadProtectoraDocument(fileUri)
                .onSuccess { url ->
                    repository.submitProtectoraDocuments(
                        documentUrl = url,
                        notes = _uiState.value.notes
                    ).onSuccess {
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                documentSubmitted = true,
                                infoMessage = "Documentación enviada correctamente."
                            )
                        }
                    }.onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                errorMessage = e.message ?: "No se pudo guardar la documentación."
                            )
                        }
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isUploading = false,
                            errorMessage = e.message ?: "No se pudo subir el documento."
                        )
                    }
                }
        }
    }
}