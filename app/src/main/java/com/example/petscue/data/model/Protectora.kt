package com.example.petscue.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Protectora(
    val nombre: String = "",
    val descripcion: String = "",
    val web: String = "",
    val email: String = "",
    val telefono: String = "",
    val facebook: String = "",
    val twitter: String = "",
    val instagram: String = "",
    val youtube: String = "",
    val teaming: String = "",
    val comunidad: String = "",
    val provincia: String = "",
    val ciudad: String = ""
)