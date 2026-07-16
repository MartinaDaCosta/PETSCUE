package com.example.petscue.data.model

data class Post(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userHandle: String = "",
    val userAvatar: String = "",
    val mensaje: String = "",
    val ubicacion: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val tipo: String = "Visto",
    val fotos: List<String> = emptyList(),
    val timestamp: Long = 0L,
    val likes: Int = 0,
    val likedBy: List<String> = emptyList(),
    val repostedBy: List<String> = emptyList(),
    val sharedBy: List<String> = emptyList(),
    val comentarios: Int = 0
)