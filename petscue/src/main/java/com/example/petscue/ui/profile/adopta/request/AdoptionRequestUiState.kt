package com.example.petscue.ui.profile.adopta.request

import com.example.petscue.data.model.Pet

data class AdoptionRequestUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val pet: Pet? = null,
    val mensaje: String = "",
    val telefono: String = "",
    val vivienda: String = "",
    val experiencia: String = "",
    val otrosAnimales: String = "",
    val submittedConversationId: String? = null,
    val error: String? = null
)