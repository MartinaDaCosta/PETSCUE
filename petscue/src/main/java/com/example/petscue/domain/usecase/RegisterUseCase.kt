package com.example.petscue.domain.usecase

import android.net.Uri
import android.util.Patterns
import com.example.petscue.data.model.User
import com.example.petscue.data.model.UserRole
import com.example.petscue.data.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        user: User,
        password: String,
        profileImageUri: Uri?,
        verificationDocuments: List<Uri> = emptyList()
    ): Result<Unit> {
        if (
            user.nombre.isBlank() ||
            user.apellido.isBlank() ||
            user.username.isBlank() ||
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

        val username = user.username.trim().lowercase()

        if (username.length < 3) {
            return Result.failure(Exception("El nombre de usuario debe tener al menos 3 caracteres."))
        }

        if (!username.matches(Regex("^[a-z0-9._]+$"))) {
            return Result.failure(
                Exception("El nombre de usuario solo puede contener letras minúsculas, números, puntos y guion bajo.")
            )
        }

        if (user.role == UserRole.PROTECTORA) {
            if (user.nombreProtectora.isBlank()) {
                return Result.failure(Exception("Introduce el nombre de la protectora."))
            }

            if (user.provincia.isBlank() || user.ciudad.isBlank()) {
                return Result.failure(Exception("Completa la ubicación de la protectora."))
            }

            if (verificationDocuments.isEmpty()) {
                return Result.failure(Exception("Adjunta al menos un documento de verificación."))
            }
        }

        return repository.register(
            user = user.copy(username = username),
            password = password,
            profileImageUri = profileImageUri,
            verificationDocuments = verificationDocuments
        )
    }
}