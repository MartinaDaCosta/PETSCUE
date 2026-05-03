package com.example.petscue.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.petscue.data.model.User
import com.example.petscue.domain.AuthRepository
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth      = FirebaseAuth.getInstance(),
    private val db:   FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> =
        runCatching {
            auth.signInWithEmailAndPassword(email, password).await()
            Unit
        }

    override suspend fun register(user: User, password: String): Result<Unit> =
        runCatching {
            val result = auth.createUserWithEmailAndPassword(user.email, password).await()
            val uid    = result.user!!.uid
            db.collection("users").document(uid).set(user.copy(uid = uid)).await()
            auth.currentUser!!.sendEmailVerification().await()
        }

    override suspend fun sendVerificationEmail(): Result<Unit> =
        runCatching {
            auth.currentUser!!.sendEmailVerification().await()
        }

    override fun isEmailVerified(): Boolean =
        auth.currentUser?.isEmailVerified == true

    override fun isLoggedIn(): Boolean =
        auth.currentUser != null && isEmailVerified()

    override fun logout() = auth.signOut()
}