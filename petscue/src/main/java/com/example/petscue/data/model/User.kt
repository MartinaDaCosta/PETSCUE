// data/model/User.kt
package com.example.petscue.data.model

data class User(
    val uid: String = "",
    val role: UserRole = UserRole.USER,
    val approvalStatus: ApprovalStatus = ApprovalStatus.PENDING,

    val nombre: String = "",
    val apellido: String = "",
    val username: String = "",
    val email: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val photoUrl: String = "",

    val followers: Int = 0,
    val following: Int = 0,

    val nombreProtectora: String = "",
    val descripcionProtectora: String = "",
    val web: String = "",
    val facebook: String = "",
    val instagram: String = "",
    val comunidad: String = "",
    val provincia: String = "",
    val ciudad: String = "",

    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    val documentacionEnviada: Boolean = false,
    val documentos: List<ProtectoraDocument> = emptyList(),
    val motivoRevision: String = "",

    val createdAt: Long = 0L,
    val admin: Boolean = false
)