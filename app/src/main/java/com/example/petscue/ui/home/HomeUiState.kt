package com.example.petscue.ui.home

import com.example.petscue.data.model.Post

data class HomeUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)