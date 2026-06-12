package com.example.petscue.data.model

// Animal publicado por una adopta para adopción.
data class AdoptionAnimal(
    val id: String = "",
    val protectoraId: String = "",

    val nombre: String = "",
    val descripcion: String = "",
    val raza: String = "",
    val edad: String = "",
    val sexo: String = "",
    val tamano: String = "",

    val estado: AnimalStatus = AnimalStatus.DISPONIBLE,
    val imagenes: List<String> = emptyList(),

    val provincia: String = "",
    val ciudad: String = "",

    val createdAt: Long = 0L
)