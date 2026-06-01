package com.example.petscue.data.repository

import android.net.Uri
import com.example.petscue.data.model.User
import com.example.petscue.domain.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
        Unit
    }

    override suspend fun register(user: User, password: String): Result<Unit> = runCatching {
        val result = auth.createUserWithEmailAndPassword(user.email, password).await()
        val firebaseUser = result.user
            ?: error("La cuenta se creó, pero no se pudo obtener el usuario autenticado.")

        val userToSave = user.copy(
            uid = firebaseUser.uid,
            createdAt = System.currentTimeMillis()
        )

        db.collection("users")
            .document(firebaseUser.uid)
            .set(userToSave)
            .await()

        firebaseUser.sendEmailVerification().await()
    }

    override suspend fun sendVerificationEmail(): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("No hay usuario autenticado")
        user.sendEmailVerification().await()
    }

    override suspend fun resetPassword(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    override fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified == true
    }

    override fun isLoggedIn(): Boolean {
        return auth.currentUser != null && isEmailVerified()
    }

    override fun logout() {
        auth.signOut()
    }

    override suspend fun getCurrentUserProfile(): Result<User> = runCatching {
        val uid = auth.currentUser?.uid ?: error("No hay usuario autenticado")

        db.collection("users")
            .document(uid)
            .get()
            .await()
            .toObject<User>()
            ?: error("No se encontró el perfil del usuario")
    }

    override suspend fun uploadProtectoraDocument(fileUri: Uri): Result<String> = runCatching {
        val uid = auth.currentUser?.uid ?: error("No hay usuario autenticado")
        val fileName = "doc_${System.currentTimeMillis()}"
        val ref = storage.reference.child("protectoras/$uid/$fileName")

        ref.putFile(fileUri).await()
        ref.downloadUrl.await().toString()
    }

    override suspend fun submitProtectoraDocuments(
        documentUrl: String,
        notes: String
    ): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: error("No hay usuario autenticado")

        db.collection("users")
            .document(uid)
            .update(
                mapOf(
                    "documentacionEnviada" to true,
                    "documentosUrls" to listOf(documentUrl),
                    "motivoRevision" to notes
                )
            )
            .await()
    }
}