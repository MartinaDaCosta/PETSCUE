package com.example.petscue.ui.pet.petdetail

import com.example.petscue.data.model.Pet

data class PetDetailUiState(
    val isLoading: Boolean = true,
    val pet: Pet? = null,
    val isDeleted: Boolean = false,
    val error: String? = null
)