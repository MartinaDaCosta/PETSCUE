package com.example.petscue.data.repository

import android.net.Uri
import com.example.petscue.data.model.Pet
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    fun getAll(): Flow<List<Pet>>
    fun getByEstado(estado: String): Flow<List<Pet>>
    fun getByUserId(userId: String): Flow<List<Pet>>
    fun getAdoptionPetsByUserId(userId: String): Flow<List<Pet>>
    suspend fun insert(pet: Pet)
    suspend fun delete(pet: Pet)
    suspend fun uploadPetImages(petId: String, imageUris: List<Uri>): List<String>
    suspend fun getAdoptionPetById(petId: String): Pet?
    suspend fun updateAdoptionPet(pet: Pet)
    suspend fun getPetById(petId: String): Pet?
    fun getAlertPetsByCurrentUser(): Flow<List<Pet>>
    suspend fun getAnyPetById(petId: String): Pet?
}