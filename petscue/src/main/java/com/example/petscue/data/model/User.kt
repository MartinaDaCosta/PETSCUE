package com.example.petscue.data.model

data class User(
    val uid: String = "",
    val role: UserRole = UserRole.USER,
    val approvalStatus: ApprovalStatus = ApprovalStatus.APPROVED,

    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val photoUrl: String = "",

    val nombreProtectora: String = "",
    val descripcionProtectora: String = "",
    val web: String = "",
    val facebook: String = "",
    val instagram: String = "",
    val provincia: String = "",
    val ciudad: String = "",

    val createdAt: Long = 0L,
    val isAdmin: Boolean = false
)