package com.example.petscue.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey
    val id:          String = "",
    val userId:      String = "",
    val userName:    String = "",
    val userHandle:  String = "",
    val userAvatar:  String = "",
    val mensaje:     String = "",
    val ubicacion:   String = "",
    val fotos:       List<String> = emptyList(),
    val timestamp:   Long   = 0L,
    val likes:       Int    = 0,
    val comentarios: Int    = 0
)