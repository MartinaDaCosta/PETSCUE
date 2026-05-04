package com.example.petscue.domain.usecase

import com.example.petscue.data.repository.PetRepository
import javax.inject.Inject

class GetPetsByEstadoUseCase @Inject constructor(
    private val repository: PetRepository
) {
    operator fun invoke(estado: String) = repository.getByEstado(estado)
}