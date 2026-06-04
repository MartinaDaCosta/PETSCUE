package com.example.petscue.admin.ui.requests

import com.example.petscue.admin.data.model.ProtectoraRequest

data class AdminRequestsUiState(
    val isLoading: Boolean = false,
    val requests: List<ProtectoraRequest> = emptyList(),
    val errorMessage: String? = null
)