package com.example.petscue.data.repository

import com.example.petscue.data.model.Pet
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    fun getAll(): Flow<List<Pet>>
    fun getByEstado(estado: String): Flow<List<Pet>>
    fun getByUserId(userId: String): Flow<List<Pet>>
    fun getAdoptionPetsByUserId(userId: String): Flow<List<Pet>>
    suspend fun insert(pet: Pet)
    suspend fun delete(pet: Pet)
}