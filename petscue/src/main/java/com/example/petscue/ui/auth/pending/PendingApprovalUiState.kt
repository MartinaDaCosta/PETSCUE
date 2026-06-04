package com.example.petscue.ui.auth.pending

import android.net.Uri

data class PendingApprovalUiState(
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val notes: String = "",
    val selectedFileUri: Uri? = null,
    val selectedFileName: String = "",
    val infoMessage: String? = null,
    val errorMessage: String? = null,
    val documentSubmitted: Boolean = false,
    val isApproved: Boolean = false
)