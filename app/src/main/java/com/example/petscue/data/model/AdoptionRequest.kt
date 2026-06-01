package com.example.petscue.data.model

// Solicitud de adopción enviada por un usuario para un animal concreto.
data class AdoptionRequest(
    val id: String = "",

    val animalId: String = "",
    val animalNombre: String = "",
    val animalImagen: String = "",

    val protectoraId: String = "",
    val userId: String = "",
    val userNombre: String = "",

    val mensaje: String = "",
    val telefono: String = "",
    val vivienda: String = "",
    val experiencia: String = "",
    val otrosAnimales: String = "",

    val estado: RequestStatus = RequestStatus.PENDIENTE,
    val createdAt: Long = 0L
)