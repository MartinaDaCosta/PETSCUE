package com.example.petscue.domain.usecase

import android.util.Patterns
import com.example.petscue.data.model.User
import com.example.petscue.data.model.UserRole
import com.example.petscue.domain.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(user: User, password: String): Result<Unit> {
        if (
            user.nombre.isBlank() ||
            user.apellido.isBlank() ||
            user.email.isBlank() ||
            password.isBlank()
        ) {
            return Result.failure(Exception("Completa los campos obligatorios."))
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(user.email).matches()) {
            return Result.failure(Exception("Introduce un correo válido."))
        }

        if (password.length < 6) {
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres."))
        }

        if (user.role == UserRole.PROTECTORA) {
            if (user.nombreProtectora.isBlank()) {
                return Result.failure(Exception("Introduce el nombre de la protectora."))
            }

            if (user.provincia.isBlank() || user.ciudad.isBlank()) {
                return Result.failure(Exception("Completa la ubicación de la protectora."))
            }
        }

        return repository.register(user, password)
    }
}