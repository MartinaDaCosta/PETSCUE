package com.example.petscue.data.model

data class Pet(
    val id: String = "",
    val nombre: String = "",
    val especie: String = "",
    val raza: String = "",
    val genero: String = "",
    val edad: String = "",
    val peso: String = "",
    val descripcion: String = "",
    val ubicacion: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fotos: List<String> = emptyList(),
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String = "",
    val estado: String = "perdido",
    val timestamp: Long = 0L
)