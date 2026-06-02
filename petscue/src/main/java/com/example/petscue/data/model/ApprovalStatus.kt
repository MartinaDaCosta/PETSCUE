package com.example.petscue.data.model

// Estado de validación de una cuenta de protectora.
// APPROVED: cuenta aprobada.
// PENDING: pendiente de revisión.
// REJECTED: rechazada.
enum class ApprovalStatus {
    APPROVED,
    PENDING,
    REJECTED
}