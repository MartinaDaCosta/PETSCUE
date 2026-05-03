package com.example.petscue.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val uid:       String = "",
    val nombre:    String = "",
    val apellido:  String = "",
    val email:     String = "",
    val telefono:  String = "",
    val direccion: String = "",
    val photoUrl:  String = "",
    val role:      String = "user"
)