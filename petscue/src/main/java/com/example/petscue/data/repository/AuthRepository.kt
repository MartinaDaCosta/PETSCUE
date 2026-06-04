package com.example.petscue.data.repository

import android.net.Uri
import com.example.petscue.data.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(user: User, password: String): Result<Unit>
    suspend fun sendVerificationEmail(): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    fun isEmailVerified(): Boolean
    fun isLoggedIn(): Boolean
    fun logout()

    suspend fun getCurrentUserProfile(): Result<User>
    suspend fun uploadProtectoraDocument(fileUri: Uri): Result<String>
    suspend fun submitProtectoraDocuments(
        documentUrl: String,
        notes: String
    ): Result<Unit>

    suspend fun getPendingProtectoras(): Result<List<User>>
    suspend fun approveProtectora(uid: String): Result<Unit>
    suspend fun rejectProtectora(uid: String, reason: String): Result<Unit>
}