package com.example.petscue.domain.usecase

import com.example.petscue.data.model.Pet
import com.example.petscue.data.repository.PetRepository
import javax.inject.Inject

class InsertPetUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(pet: Pet) = repository.insert(pet)
}