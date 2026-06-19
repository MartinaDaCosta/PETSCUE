package com.example.petscue.data.model

/**
 * Modelo simple para transportar latitud, longitud y una dirección opcional.
 */
data class Location(
    val lat: Double,
    val lng: Double,
    val direccion: String = ""
)