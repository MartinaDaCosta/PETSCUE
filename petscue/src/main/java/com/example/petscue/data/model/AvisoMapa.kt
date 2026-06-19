package com.example.petscue.data.model

data class AvisoMapa(
    val id: String = "",
    val petId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String = "",
    val nombreMascota: String = "",
    val fotoUrl: String = "",
    val direccionAviso: String = "",
    val tipoAviso: String = "",
    val sexo: String? = null,
    val raza: String? = null,
    val edad: String? = null,
    val caracteristicas: String? = null,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val radioMetros: Double = 1500.0,
    val createdAt: Long = System.currentTimeMillis()
)