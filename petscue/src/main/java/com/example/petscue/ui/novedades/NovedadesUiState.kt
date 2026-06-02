package com.example.petscue.ui.novedades

import com.example.petscue.data.model.Post

data class NovedadesUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)