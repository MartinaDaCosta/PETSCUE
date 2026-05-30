package com.example.petscue.domain.usecase

import com.example.petscue.data.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        if (email.isBlank() || password.isBlank())
            return Result.failure(Exception("Completa todos los campos."))
        if (password.length < 6)
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres."))
        return repository.login(email.trim(), password)
    }
}