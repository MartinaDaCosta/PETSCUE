package com.example.petscue.domain.usecase

import com.example.petscue.data.model.Pet
import com.example.petscue.data.repository.PetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPetsAllUseCase @Inject constructor(
    private val repository: PetRepository
) {
    operator fun invoke(): Flow<List<Pet>> = repository.getAll()
}