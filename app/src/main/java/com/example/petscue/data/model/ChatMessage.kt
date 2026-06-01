package com.example.petscue.data.model

// Mensaje individual dentro de una conversación.
// Puede ser de texto normal, resumen del formulario o imagen.
data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val type: MessageType = MessageType.TEXT,
    val createdAt: Long = 0L,

    // Se usa solo cuando type = FORM_SUMMARY
    val formSnapshot: Map<String, String> = emptyMap()
)