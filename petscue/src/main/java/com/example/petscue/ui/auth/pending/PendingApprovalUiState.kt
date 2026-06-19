package com.example.petscue.ui.auth.pending

import android.net.Uri
import com.example.petscue.data.model.ProtectoraDocument

// Estado de la pantalla de validación pendiente
data class PendingApprovalUiState(
    val isLoading: Boolean = true,
    val isUploading: Boolean = false,
    val isDeleting: Boolean = false,
    val notes: String = "",
    val existingDocuments: List<ProtectoraDocument> = emptyList(),
    val selectedFiles: List<Uri> = emptyList(),
    val infoMessage: String? = null,
    val errorMessage: String? = null,
    val documentSubmitted: Boolean = false,
    val isApproved: Boolean = false
) {
    val totalDocuments: Int
        get() = existingDocuments.size + selectedFiles.size

    val canAddMore: Boolean
        get() = totalDocuments < 5
}