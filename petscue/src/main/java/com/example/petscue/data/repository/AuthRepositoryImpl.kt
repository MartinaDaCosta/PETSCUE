package com.example.petscue.data.repository

import android.net.Uri
import com.example.petscue.data.model.ApprovalStatus
import com.example.petscue.data.model.User
import com.example.petscue.data.model.UserRole
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

    override suspend fun isUsernameAvailable(username: String): Result<Boolean> = runCatching {
        val normalized = username.trim().lowercase()
        require(normalized.isNotBlank()) { "El nombre de usuario es obligatorio." }

        val snapshot = db.collection("users")
            .whereEqualTo("username", normalized)
            .get()
            .await()

        snapshot.isEmpty
    }

    override suspend fun register(
        user: User,
        password: String,
        profileImageUri: Uri?
    ): Result<Unit> = runCatching {
        val normalizedUsername = user.username.trim().lowercase()
        require(normalizedUsername.isNotBlank()) { "El nombre de usuario es obligatorio." }

        val existingUsers = db.collection("users")
            .whereEqualTo("username", normalizedUsername)
            .get()
            .await()

        require(existingUsers.isEmpty) { "Ese nombre de usuario ya está en uso." }

        val result = auth.createUserWithEmailAndPassword(user.email.trim(), password).await()
        val firebaseUser = result.user
            ?: error("La cuenta se creó, pero no se pudo obtener el usuario autenticado.")

        try {
            val photoUrl = if (profileImageUri != null) {
                val ref = storage.reference.child("profile_images/${firebaseUser.uid}.jpg")
                ref.putFile(profileImageUri).await()
                ref.downloadUrl.await().toString()
            } else {
                ""
            }

            val createdAt = System.currentTimeMillis()

            val userToSave = user.copy(
                uid = firebaseUser.uid,
                username = normalizedUsername,
                email = user.email.trim(),
                photoUrl = photoUrl,
                followers = 0,
                following = 0,
                createdAt = createdAt,
                approvalStatus = if (user.role == UserRole.PROTECTORA) {
                    ApprovalStatus.PENDING
                } else {
                    ApprovalStatus.APPROVED
                }
            )

            db.collection("users")
                .document(firebaseUser.uid)
                .set(userToSave)
                .await()

            firebaseUser.sendEmailVerification().await()
        } catch (e: Exception) {
            auth.currentUser?.delete()?.await()
            throw e
        }
    }

    override suspend fun updateProfile(
        nombre: String,
        apellido: String,
        username: String,
        telefono: String,
        direccion: String,
        profileImageUri: Uri?
    ): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: error("No hay usuario autenticado")
        val normalizedUsername = username.trim().lowercase()

        require(nombre.trim().isNotBlank()) { "El nombre es obligatorio." }
        require(apellido.trim().isNotBlank()) { "El apellido es obligatorio." }
        require(normalizedUsername.isNotBlank()) { "El nombre de usuario es obligatorio." }
        require(normalizedUsername.length >= 3) { "El nombre de usuario debe tener al menos 3 caracteres." }
        require(
            normalizedUsername.matches(Regex("^[a-z0-9._]+$"))
        ) {
            "El nombre de usuario solo puede contener letras minúsculas, números, puntos y guion bajo."
        }

        val usernameQuery = db.collection("users")
            .whereEqualTo("username", normalizedUsername)
            .get()
            .await()

        val takenByAnotherUser = usernameQuery.documents.any { it.id != uid }
        require(!takenByAnotherUser) { "Ese nombre de usuario ya está en uso." }

        val updates = mutableMapOf<String, Any>(
            "nombre" to nombre.trim(),
            "apellido" to apellido.trim(),
            "username" to normalizedUsername,
            "telefono" to telefono.trim(),
            "direccion" to direccion.trim()
        )

        if (profileImageUri != null) {
            val ref = storage.reference.child("profile_images/${uid}.jpg")
            ref.putFile(profileImageUri).await()
            val photoUrl = ref.downloadUrl.await().toString()
            updates["photoUrl"] = photoUrl
        }

        db.collection("users")
            .document(uid)
            .update(updates)
            .await()
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

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

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
                    "motivoRevision" to notes,
                    "approvalStatus" to ApprovalStatus.PENDING.name
                )
            )
            .await()
    }

    override suspend fun getPendingProtectoras(): Result<List<User>> = runCatching {
        db.collection("users")
            .whereEqualTo("role", UserRole.PROTECTORA.name)
            .whereEqualTo("approvalStatus", ApprovalStatus.PENDING.name)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(User::class.java) }
    }

    override suspend fun approveProtectora(uid: String): Result<Unit> = runCatching {
        db.collection("users")
            .document(uid)
            .update(
                mapOf("approvalStatus" to ApprovalStatus.APPROVED.name)
            )
            .await()
    }

    override suspend fun rejectProtectora(uid: String, reason: String): Result<Unit> = runCatching {
        db.collection("users")
            .document(uid)
            .update(
                mapOf(
                    "approvalStatus" to ApprovalStatus.REJECTED.name,
                    "motivoRevision" to reason
                )
            )
            .await()
    }
}