package com.example.petscue.ui.auth.pending

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.ApprovalStatus
import com.example.petscue.data.model.ProtectoraDocument
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
        loadCurrentDocuments()
        observeApprovalStatus()
    }

    fun onNotesChange(value: String) {
        _uiState.update { it.copy(notes = value, errorMessage = null) }
    }

    fun onFilesSelected(uris: List<Uri>) {
        _uiState.update { current ->
            val available = 5 - current.existingDocuments.size
            val merged = (current.selectedFiles + uris).distinct()
            val finalList = merged.take(available.coerceAtLeast(0))

            current.copy(
                selectedFiles = finalList,
                errorMessage = if (merged.size > available) {
                    "Máximo 5 documentos en total."
                } else null
            )
        }
    }

    fun removeFile(uri: Uri) {
        _uiState.update { current ->
            current.copy(selectedFiles = current.selectedFiles.filterNot { it == uri })
        }
    }

    fun deleteExistingDocument(document: ProtectoraDocument) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null, infoMessage = null) }

            repository.deleteProtectoraDocument(document)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            infoMessage = "Documento eliminado correctamente."
                        )
                    }
                    loadCurrentDocuments()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            errorMessage = e.message ?: "No se pudo eliminar el documento."
                        )
                    }
                }
        }
    }

    private fun loadCurrentDocuments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            repository.getCurrentUserProfile()
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notes = user.motivoRevision,
                            existingDocuments = user.documentos.take(5)
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "No se pudieron cargar los documentos."
                        )
                    }
                }
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
            val files = _uiState.value.selectedFiles
            if (files.isEmpty()) {
                _uiState.update { it.copy(errorMessage = "Selecciona al menos un documento primero.") }
                return@launch
            }

            _uiState.update { it.copy(isUploading = true, errorMessage = null, infoMessage = null) }

            val documents = mutableListOf<ProtectoraDocument>()

            for (uri in files) {
                repository.uploadProtectoraDocument(uri)
                    .onSuccess { documents.add(it) }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                errorMessage = e.message ?: "No se pudo subir uno de los documentos."
                            )
                        }
                        return@launch
                    }
            }

            repository.submitProtectoraDocuments(
                documents = documents,
                notes = _uiState.value.notes
            ).onSuccess {
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        selectedFiles = emptyList(),
                        documentSubmitted = true,
                        infoMessage = "Documentación enviada correctamente."
                    )
                }
                loadCurrentDocuments()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        errorMessage = e.message ?: "No se pudo guardar la documentación."
                    )
                }
            }
        }
    }
}