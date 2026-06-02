package com.example.petscue.data.model

// Conversación entre un usuario y una protectora
// asociada a un animal y, opcionalmente, a una solicitud de adopción.
data class Conversation(
    val id: String = "",

    val animalId: String = "",
    val animalNombre: String = "",
    val animalImagen: String = "",

    val requestId: String = "",

    val participants: List<String> = emptyList(),
    val userId: String = "",
    val protectoraId: String = "",

    val lastMessage: String = "",
    val lastUpdated: Long = 0L
)