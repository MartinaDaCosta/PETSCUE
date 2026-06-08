package com.example.petscue.data.model

data class AdoptionPet(
    val id: String = "",
    val protectoraId: String = "",
    val nombre: String = "",
    val edad: String = "",
    val fotoUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
)