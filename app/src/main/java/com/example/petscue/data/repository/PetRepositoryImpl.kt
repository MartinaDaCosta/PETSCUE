package com.example.petscue.data.repository

import com.example.petscue.data.model.Pet
import com.example.petscue.data.sources.local.PetDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor(
    private val petDao: PetDao
) : PetRepository {

    override fun getAll(): Flow<List<Pet>> = petDao.getAll()

    override fun getByEstado(estado: String): Flow<List<Pet>> = petDao.getByEstado(estado)

    override suspend fun insert(pet: Pet) = petDao.insert(pet)

    override suspend fun delete(pet: Pet) = petDao.delete(pet)
}