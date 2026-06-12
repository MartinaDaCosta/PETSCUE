package com.example.petscue.data.model

// Estado de validación de una cuenta de adopta.
// APPROVED: cuenta aprobada.
// PENDING: pendiente de revisión.
// REJECTED: rechazada.
enum class ApprovalStatus {
    APPROVED,
    PENDING,
    REJECTED
}