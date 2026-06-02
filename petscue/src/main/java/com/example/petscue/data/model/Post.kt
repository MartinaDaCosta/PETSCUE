package com.example.petscue.data.model

data class Post(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userHandle: String = "",
    val userAvatar: String = "",
    val mensaje: String = "",
    val ubicacion: String = "",
    val tipo: String = "Avistamiento",
    val fotos: List<String> = emptyList(),
    val timestamp: Long = 0L,
    val likes: Int = 0,
    val comentarios: Int = 0
)