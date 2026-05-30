package com.example.petscue.domain.usecase

import android.util.Patterns
import com.example.petscue.data.model.User
import com.example.petscue.data.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(user: User, password: String): Result<Unit> {
        if (user.nombre.isBlank() || user.apellido.isBlank() || user.email.isBlank() || password.isBlank())
            return Result.failure(Exception("Completa los campos obligatorios."))
        if (!Patterns.EMAIL_ADDRESS.matcher(user.email).matches())
            return Result.failure(Exception("Introduce un correo válido."))
        if (password.length < 6)
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres."))
        return repository.register(user, password)
    }
}