package com.example.petscue.data.model

// Tipo de mensaje dentro del chat.
// TEXT: mensaje normal.
// FORM_SUMMARY: resumen del formulario enviado.
// IMAGE: mensaje con imagen.
enum class MessageType {
    TEXT,
    FORM_SUMMARY,
    IMAGE
}