package com.example.petscue.domain

import com.example.petscue.data.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(user: User, password: String): Result<Unit>
    suspend fun sendVerificationEmail(): Result<Unit>
    fun isEmailVerified(): Boolean
    fun isLoggedIn(): Boolean
    fun logout()
}