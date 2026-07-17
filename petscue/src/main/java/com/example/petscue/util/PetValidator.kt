package com.example.petscue.util

data class PetValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

object PetValidator {

    fun validate(
        name: String,
        species: String,
        breed: String
    ): PetValidationResult {

        if (name.isBlank()) {
            return PetValidationResult(
                isValid = false,
                errorMessage = "El nombre de la mascota es obligatorio."
            )
        }

        if (species.isBlank()) {
            return PetValidationResult(
                isValid = false,
                errorMessage = "La especie de la mascota es obligatoria."
            )
        }

        if (breed.isBlank()) {
            return PetValidationResult(
                isValid = false,
                errorMessage = "La raza de la mascota es obligatoria."
            )
        }

        return PetValidationResult(isValid = true)
    }
}