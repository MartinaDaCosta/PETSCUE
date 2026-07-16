package com.example.petscue.admin.data.model

data class ProtectoraRequest(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val descripcion: String = "",
    val comunidad: String = "",
    val provincia: String = "",
    val ciudad: String = "",
    val documentos: List<ProtectoraDocument> = emptyList(),
    val estado: String = "PENDING",
    val motivoRechazo: String = "",
    val createdAt: Long = 0L,
    val reviewedAt: Long? = null
)