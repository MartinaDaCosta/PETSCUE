package com.example.petscue.data.repository

import android.net.Uri
import com.example.petscue.data.model.ProtectoraDocument
import com.example.petscue.data.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>

    suspend fun register(
        user: User,
        password: String,
        profileImageUri: Uri?,
        verificationDocuments: List<Uri> = emptyList()
    ): Result<Unit>

    suspend fun isUsernameAvailable(username: String): Result<Boolean>

    suspend fun updateProfile(
        nombre: String,
        apellido: String,
        username: String,
        telefono: String,
        direccion: String,
        profileImageUri: Uri?
    ): Result<Unit>

    suspend fun sendVerificationEmail(): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    fun isEmailVerified(): Boolean
    fun isLoggedIn(): Boolean
    fun logout()

    fun getCurrentUserId(): String?
    suspend fun getCurrentUserProfile(): Result<User>

    suspend fun uploadProtectoraDocument(fileUri: Uri): Result<ProtectoraDocument>

    suspend fun submitProtectoraDocuments(
        documents: List<ProtectoraDocument>,
        notes: String
    ): Result<Unit>

    suspend fun deleteProtectoraDocument(document: ProtectoraDocument): Result<Unit>

    suspend fun getPendingProtectoras(): Result<List<User>>
    suspend fun approveProtectora(uid: String): Result<Unit>
    suspend fun rejectProtectora(uid: String, reason: String): Result<Unit>
}