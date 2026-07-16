// data/repository/AuthRepositoryImpl.kt
package com.example.petscue.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.petscue.data.model.ApprovalStatus
import com.example.petscue.data.model.ProtectoraDocument
import com.example.petscue.data.model.User
import com.example.petscue.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : AuthRepository {

    override suspend fun login(
        email: String,
        password: String
    ): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(
            email.trim(),
            password
        ).await()

        Unit
    }

    override suspend fun isUsernameAvailable(
        username: String
    ): Result<Boolean> = runCatching {
        val normalizedUsername = username.trim().lowercase()

        require(normalizedUsername.isNotBlank()) {
            "El nombre de usuario es obligatorio."
        }

        val snapshot = db.collection("users")
            .whereEqualTo("username", normalizedUsername)
            .get()
            .await()

        snapshot.isEmpty
    }

    override suspend fun register(
        user: User,
        password: String,
        profileImageUri: Uri?,
        verificationDocuments: List<Uri>
    ): Result<Unit> = runCatching {
        val normalizedUsername = user.username.trim().lowercase()
        val normalizedEmail = user.email.trim()

        require(user.nombre.trim().isNotBlank()) {
            "El nombre es obligatorio."
        }

        require(user.apellido.trim().isNotBlank()) {
            "El apellido es obligatorio."
        }

        require(normalizedUsername.isNotBlank()) {
            "El nombre de usuario es obligatorio."
        }

        require(normalizedUsername.length >= 3) {
            "El nombre de usuario debe tener al menos 3 caracteres."
        }

        require(normalizedUsername.matches(Regex("^[a-z0-9._]+$"))) {
            "El nombre de usuario solo puede contener letras minúsculas, números, puntos y guion bajo."
        }

        require(normalizedEmail.isNotBlank()) {
            "El email es obligatorio."
        }

        require(password.length >= 6) {
            "La contraseña debe tener al menos 6 caracteres."
        }

        if (user.role == UserRole.PROTECTORA) {
            require(user.nombreProtectora.trim().isNotBlank()) {
                "El nombre de la protectora es obligatorio."
            }

            require(verificationDocuments.isNotEmpty()) {
                "Adjunta al menos un documento de verificación."
            }
        }

        val existingUsers = db.collection("users")
            .whereEqualTo("username", normalizedUsername)
            .get()
            .await()

        require(existingUsers.isEmpty) {
            "Ese nombre de usuario ya está en uso."
        }

        val authResult = auth.createUserWithEmailAndPassword(
            normalizedEmail,
            password
        ).await()

        val firebaseUser = authResult.user
            ?: error("No se pudo crear el usuario.")

        try {
            val uid = firebaseUser.uid

            val photoUrl = if (profileImageUri != null) {
                val photoReference = storage.reference
                    .child("profile_images/$uid.jpg")

                photoReference.putFile(profileImageUri).await()
                photoReference.downloadUrl.await().toString()
            } else {
                ""
            }

            val documentos = if (user.role == UserRole.PROTECTORA) {
                verificationDocuments.mapIndexed { index, uri ->
                    uploadDocumentToStorage(
                        uid = uid,
                        fileUri = uri,
                        index = index
                    )
                }
            } else {
                emptyList()
            }

            val approvalStatus = if (user.role == UserRole.PROTECTORA) {
                ApprovalStatus.PENDING.name
            } else {
                ApprovalStatus.APPROVED.name
            }

            val userMap = hashMapOf(
                "uid" to uid,
                "role" to user.role.name,
                "approvalStatus" to approvalStatus,

                "nombre" to user.nombre.trim(),
                "apellido" to user.apellido.trim(),
                "username" to normalizedUsername,
                "email" to normalizedEmail,
                "telefono" to user.telefono.trim(),
                "direccion" to user.direccion.trim(),
                "photoUrl" to photoUrl,

                "followers" to 0,
                "following" to 0,

                "nombreProtectora" to user.nombreProtectora.trim(),
                "descripcionProtectora" to user.descripcionProtectora.trim(),
                "web" to user.web.trim(),
                "facebook" to user.facebook.trim(),
                "instagram" to user.instagram.trim(),
                "comunidad" to user.comunidad.trim(),
                "provincia" to user.provincia.trim(),
                "ciudad" to user.ciudad.trim(),

                "latitude" to user.latitude,
                "longitude" to user.longitude,

                "documentacionEnviada" to documentos.isNotEmpty(),
                "documentos" to documentos.map { document ->
                    mapOf(
                        "name" to document.name,
                        "url" to document.url
                    )
                },

                "motivoRevision" to "",
                "createdAt" to System.currentTimeMillis(),
                "admin" to false
            )

            db.collection("users")
                .document(uid)
                .set(userMap)
                .await()

            firebaseUser.sendEmailVerification().await()
        } catch (error: Exception) {
            runCatching {
                auth.currentUser?.delete()?.await()
            }

            throw error
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
        val uid = auth.currentUser?.uid
            ?: error("No hay usuario autenticado.")

        val normalizedUsername = username.trim().lowercase()

        require(nombre.trim().isNotBlank()) {
            "El nombre es obligatorio."
        }

        require(apellido.trim().isNotBlank()) {
            "El apellido es obligatorio."
        }

        require(normalizedUsername.isNotBlank()) {
            "El nombre de usuario es obligatorio."
        }

        val usernameQuery = db.collection("users")
            .whereEqualTo("username", normalizedUsername)
            .get()
            .await()

        val usernameTaken = usernameQuery.documents.any {
            it.id != uid
        }

        require(!usernameTaken) {
            "Ese nombre de usuario ya está en uso."
        }

        val updates = mutableMapOf<String, Any>(
            "nombre" to nombre.trim(),
            "apellido" to apellido.trim(),
            "username" to normalizedUsername,
            "telefono" to telefono.trim(),
            "direccion" to direccion.trim()
        )

        if (profileImageUri != null) {
            val reference = storage.reference
                .child("profile_images/$uid.jpg")

            reference.putFile(profileImageUri).await()

            updates["photoUrl"] = reference.downloadUrl.await().toString()
        }

        db.collection("users")
            .document(uid)
            .update(updates)
            .await()
    }

    override suspend fun sendVerificationEmail(): Result<Unit> = runCatching {
        val user = auth.currentUser
            ?: error("No hay usuario autenticado.")

        user.sendEmailVerification().await()
    }

    override suspend fun resetPassword(
        email: String
    ): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
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

    override fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override suspend fun getCurrentUserProfile(): Result<User> = runCatching {
        val uid = auth.currentUser?.uid
            ?: error("No hay usuario autenticado.")

        val snapshot = db.collection("users")
            .document(uid)
            .get()
            .await()

        if (!snapshot.exists()) {
            error("No se encontró el perfil de usuario.")
        }

        snapshot.toUser()
    }

    override suspend fun uploadProtectoraDocument(
        fileUri: Uri
    ): Result<ProtectoraDocument> = runCatching {
        val uid = auth.currentUser?.uid
            ?: error("No hay usuario autenticado.")

        uploadDocumentToStorage(
            uid = uid,
            fileUri = fileUri,
            index = null
        )
    }

    override suspend fun submitProtectoraDocuments(
        documents: List<ProtectoraDocument>,
        notes: String
    ): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid
            ?: error("No hay usuario autenticado.")

        require(documents.isNotEmpty()) {
            "Debes adjuntar al menos un documento."
        }

        val userReference = db.collection("users")
            .document(uid)

        val snapshot = userReference.get().await()

        val previousDocuments = snapshot.get("documentos")
            .toProtectoraDocuments()

        val allDocuments = (previousDocuments + documents)
            .filter { it.url.isNotBlank() }
            .distinctBy { it.url }
            .take(5)

        userReference.update(
            mapOf(
                "documentacionEnviada" to allDocuments.isNotEmpty(),
                "documentos" to allDocuments.map { document ->
                    mapOf(
                        "name" to document.name,
                        "url" to document.url
                    )
                },
                "approvalStatus" to ApprovalStatus.PENDING.name,
                "motivoRevision" to ""
            )
        ).await()
    }

    override suspend fun deleteProtectoraDocument(
        document: ProtectoraDocument
    ): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid
            ?: error("No hay usuario autenticado.")

        val userReference = db.collection("users")
            .document(uid)

        val snapshot = userReference.get().await()

        val currentDocuments = snapshot.get("documentos")
            .toProtectoraDocuments()

        val updatedDocuments = currentDocuments.filterNot {
            it.url == document.url
        }

        userReference.update(
            mapOf(
                "documentos" to updatedDocuments.map { item ->
                    mapOf(
                        "name" to item.name,
                        "url" to item.url
                    )
                },
                "documentacionEnviada" to updatedDocuments.isNotEmpty()
            )
        ).await()

        runCatching {
            storage.getReferenceFromUrl(document.url)
                .delete()
                .await()
        }
    }

    override fun observePendingProtectoras(): Flow<List<User>> = callbackFlow {
        val listener = db.collection("users")
            .whereEqualTo("role", UserRole.PROTECTORA.name)
            .whereEqualTo("approvalStatus", ApprovalStatus.PENDING.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val users = snapshot?.documents
                    ?.map { document -> document.toUser() }
                    .orEmpty()

                trySend(users)
            }

        awaitClose {
            listener.remove()
        }
    }

    override suspend fun getPendingProtectoras(): Result<List<User>> = runCatching {
        val snapshot = db.collection("users")
            .whereEqualTo("role", UserRole.PROTECTORA.name)
            .whereEqualTo("approvalStatus", ApprovalStatus.PENDING.name)
            .get()
            .await()

        snapshot.documents.map { document ->
            document.toUser()
        }
    }

    override suspend fun approveProtectora(
        uid: String
    ): Result<Unit> = runCatching {
        db.collection("users")
            .document(uid)
            .update(
                mapOf(
                    "approvalStatus" to ApprovalStatus.APPROVED.name,
                    "motivoRevision" to ""
                )
            )
            .await()
    }

    override suspend fun rejectProtectora(
        uid: String,
        reason: String
    ): Result<Unit> = runCatching {
        require(reason.trim().isNotBlank()) {
            "Debes indicar el motivo del rechazo."
        }

        db.collection("users")
            .document(uid)
            .update(
                mapOf(
                    "approvalStatus" to ApprovalStatus.REJECTED.name,
                    "motivoRevision" to reason.trim()
                )
            )
            .await()
    }

    private suspend fun uploadDocumentToStorage(
        uid: String,
        fileUri: Uri,
        index: Int?
    ): ProtectoraDocument {
        val originalFileName = getFileName(fileUri)

        val uniqueFileName = buildString {
            append(System.currentTimeMillis())
            append("_")
            append(UUID.randomUUID().toString())
            append("_")

            if (index != null) {
                append("${index + 1}_")
            }

            append(originalFileName)
        }

        val reference = storage.reference
            .child("protectoras")
            .child(uid)
            .child("documentos")
            .child(uniqueFileName)

        reference.putFile(fileUri).await()

        return ProtectoraDocument(
            name = originalFileName,
            url = reference.downloadUrl.await().toString()
        )
    }

    private fun getFileName(uri: Uri): String {
        var result = "documento_${System.currentTimeMillis()}"

        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(
                OpenableColumns.DISPLAY_NAME
            )

            if (cursor.moveToFirst() && nameIndex >= 0) {
                result = cursor.getString(nameIndex)
            }
        }

        return result
    }

    private fun DocumentSnapshot.toUser(): User {
        val roleString = getString("role") ?: UserRole.USER.name

        val approvalStatusString = getString("approvalStatus")
            ?: ApprovalStatus.PENDING.name

        val documentos = get("documentos")
            .toProtectoraDocuments()

        return User(
            uid = getString("uid") ?: id,
            role = roleString.toUserRole(),
            approvalStatus = approvalStatusString.toApprovalStatus(),

            nombre = getString("nombre").orEmpty(),
            apellido = getString("apellido").orEmpty(),
            username = getString("username").orEmpty(),
            email = getString("email").orEmpty(),
            telefono = getString("telefono").orEmpty(),
            direccion = getString("direccion").orEmpty(),
            photoUrl = getString("photoUrl").orEmpty(),

            followers = getLong("followers")?.toInt() ?: 0,
            following = getLong("following")?.toInt() ?: 0,

            nombreProtectora = getString("nombreProtectora").orEmpty(),
            descripcionProtectora = getString("descripcionProtectora").orEmpty(),
            web = getString("web").orEmpty(),
            facebook = getString("facebook").orEmpty(),
            instagram = getString("instagram").orEmpty(),
            comunidad = getString("comunidad").orEmpty(),
            provincia = getString("provincia").orEmpty(),
            ciudad = getString("ciudad").orEmpty(),

            latitude = getDouble("latitude") ?: 0.0,
            longitude = getDouble("longitude") ?: 0.0,

            documentacionEnviada = documentos.isNotEmpty(),
            documentos = documentos,
            motivoRevision = getString("motivoRevision").orEmpty(),

            createdAt = getLong("createdAt") ?: 0L,
            admin = getBoolean("admin") ?: false
        )
    }

    private fun Any?.toProtectoraDocuments(): List<ProtectoraDocument> {
        val rawDocuments = this as? List<*> ?: return emptyList()

        return rawDocuments.mapNotNull { rawItem ->
            val map = rawItem as? Map<*, *> ?: return@mapNotNull null

            val url = map["url"] as? String
                ?: return@mapNotNull null

            ProtectoraDocument(
                name = map["name"] as? String ?: "Documento",
                url = url
            )
        }
    }

    private fun String.toUserRole(): UserRole {
        return runCatching {
            UserRole.valueOf(this)
        }.getOrDefault(UserRole.USER)
    }

    private fun String.toApprovalStatus(): ApprovalStatus {
        return runCatching {
            ApprovalStatus.valueOf(this)
        }.getOrDefault(ApprovalStatus.PENDING)
    }
}