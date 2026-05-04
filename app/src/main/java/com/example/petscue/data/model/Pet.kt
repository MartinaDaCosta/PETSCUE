package com.example.petscue.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pets")
data class Pet(
    @PrimaryKey
    val id:          String = "",
    val nombre:      String = "",
    val especie:     String = "",   // Perro, Gato, Conejo...
    val raza:        String = "",
    val genero:      String = "",   // Macho, Hembra
    val edad:        String = "",
    val peso:        String = "",
    val descripcion: String = "",
    val ubicacion:   String = "",
    val latitud:     Double = 0.0,
    val longitud:    Double = 0.0,
    val fotos:       List<String> = emptyList(),
    val userId:      String = "",
    val userName:    String = "",
    val userAvatar:  String = "",
    val estado:      String = "perdido",  // perdido, encontrado, adoptado
    val timestamp:   Long   = 0L
)