package com.example.petscue.ui.novedades

import com.example.petscue.data.model.Post
import com.example.petscue.data.model.User

data class NovedadesUiState(
    val posts: List<Post> = emptyList(),
    val currentUser: User = User(),
    val savedPostIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null
)