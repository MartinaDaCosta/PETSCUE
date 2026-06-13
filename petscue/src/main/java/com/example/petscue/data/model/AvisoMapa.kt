package com.example.petscue.data.model

data class AvisoMapa(
    val id: String = "",
    val petId: String = "",
    val userId: String = "",
    val nombreMascota: String = "",
    val fotoUrl: String = "",
    val tipoAviso: String = "",
    val direccionAviso: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val radioMetros: Double = 1500.0,
    val createdAt: Long = System.currentTimeMillis()
)