package com.example.petscue.ui.profile.adopta.adoptiondetail

import com.example.petscue.data.model.Pet

data class AdoptionPetDetailUiState(
    val isLoading: Boolean = true,
    val pet: Pet? = null,
    val isOwner: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null
)
