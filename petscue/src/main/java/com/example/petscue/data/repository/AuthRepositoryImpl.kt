package com.example.petscue.data.repository

import android.net.Uri
import com.example.petscue.data.model.ApprovalStatus
import com.example.petscue.data.model.ProtectoraDocument
import com.example.petscue.data.model.User
import com.example.petscue.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
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
        profileImageUri: Uri?,
        verificationDocuments: List<Uri>
    ): Result<Unit> = runCatching {
        val normalizedUsername = user.username.trim().lowercase()
        val normalizedEmail = user.email.trim()

        require(user.nombre.trim().isNotBlank()) { "El nombre es obligatorio." }
        require(user.apellido.trim().isNotBlank()) { "El apellido es obligatorio." }
        require(normalizedUsername.isNotBlank()) { "El nombre de usuario es obligatorio." }
        require(normalizedUsername.length >= 3) {
            "El nombre de usuario debe tener al menos 3 caracteres."
        }
        require(normalizedUsername.matches(Regex("^[a-z0-9._]+$"))) {
            "El nombre de usuario solo puede contener letras minúsculas, números, puntos y guion bajo."
        }
        require(normalizedEmail.isNotBlank()) { "El email es obligatorio." }
        require(password.length >= 6) { "La contraseña debe tener al menos 6 caracteres." }

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

        require(existingUsers.isEmpty) { "Ese nombre de usuario ya está en uso." }

        val result = auth.createUserWithEmailAndPassword(normalizedEmail, password).await()
        val firebaseUser = result.user
            ?: error("La cuenta se creó, pero no se pudo obtener el usuario autenticado.")

        try {
            val uid = firebaseUser.uid

            val photoUrl = if (profileImageUri != null) {
                val ref = storage.reference.child("profile_images/$uid.jpg")
                ref.putFile(profileImageUri).await()
                ref.downloadUrl.await().toString()
            } else {
                ""
            }

            val documentos = if (user.role == UserRole.PROTECTORA) {
                verificationDocuments.mapIndexed { index, uri ->
                    val fileName = uri.lastPathSegment?.substringAfterLast('/')?.substringBefore('?')
                        ?.ifBlank { "documento_${index + 1}" }
                        ?: "documento_${index + 1}"

                    val ref = storage.reference.child(
                        "protectoras/$uid/registro_doc_${index}_${System.currentTimeMillis()}_$fileName"
                    )
                    ref.putFile(uri).await()
                    val url = ref.downloadUrl.await().toString()

                    ProtectoraDocument(
                        name = fileName,
                        url = url
                    )
                }
            } else {
                emptyList()
            }

            val createdAt = System.currentTimeMillis()
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
                "provincia" to user.provincia.trim(),
                "ciudad" to user.ciudad.trim(),
                "comunidad" to user.comunidad.trim(),
                "latitude" to user.latitude,
                "longitude" to user.longitude,

                "documentacionEnviada" to documentos.isNotEmpty(),
                "documentos" to documentos.map { mapOf("name" to it.name, "url" to it.url) },
                "motivoRevision" to "",

                "createdAt" to createdAt,
                "admin" to false
            )

            db.collection("users")
                .document(uid)
                .set(userMap)
                .await()

            firebaseUser.sendEmailVerification().await()
        } catch (e: Exception) {
            try {
                auth.currentUser?.delete()?.await()
            } catch (_: Exception) {
            }
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
        require(normalizedUsername.length >= 3) {
            "El nombre de usuario debe tener al menos 3 caracteres."
        }
        require(normalizedUsername.matches(Regex("^[a-z0-9._]+$"))) {
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
            val ref = storage.reference.child("profile_images/$uid.jpg")
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
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    override fun isEmailVerified(): Boolean {
        auth.currentUser?.reload()
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

        val snapshot = db.collection("users")
            .document(uid)
            .get()
            .await()

        if (!snapshot.exists()) {
            error("No se encontró el perfil del usuario")
        }

        val roleString = snapshot.getString("role") ?: UserRole.USER.name
        val approvalStatusString =
            snapshot.getString("approvalStatus") ?: ApprovalStatus.PENDING.name

        val documentos = snapshot.get("documentos").toProtectoraDocuments()

        User(
            uid = snapshot.getString("uid") ?: uid,
            role = roleString.toUserRole(),
            approvalStatus = approvalStatusString.toApprovalStatus(),

            nombre = snapshot.getString("nombre").orEmpty(),
            apellido = snapshot.getString("apellido").orEmpty(),
            username = snapshot.getString("username").orEmpty(),
            email = snapshot.getString("email").orEmpty(),
            telefono = snapshot.getString("telefono").orEmpty(),
            direccion = snapshot.getString("direccion").orEmpty(),
            photoUrl = snapshot.getString("photoUrl").orEmpty(),

            followers = snapshot.getLong("followers")?.toInt() ?: 0,
            following = snapshot.getLong("following")?.toInt() ?: 0,

            nombreProtectora = snapshot.getString("nombreProtectora").orEmpty(),
            descripcionProtectora = snapshot.getString("descripcionProtectora").orEmpty(),
            web = snapshot.getString("web").orEmpty(),
            facebook = snapshot.getString("facebook").orEmpty(),
            instagram = snapshot.getString("instagram").orEmpty(),
            comunidad = snapshot.getString("comunidad").orEmpty(),
            provincia = snapshot.getString("provincia").orEmpty(),
            ciudad = snapshot.getString("ciudad").orEmpty(),

            latitude = snapshot.getDouble("latitude") ?: 0.0,
            longitude = snapshot.getDouble("longitude") ?: 0.0,

            documentacionEnviada = documentos.isNotEmpty(),
            documentos = documentos,
            motivoRevision = snapshot.getString("motivoRevision").orEmpty(),

            createdAt = snapshot.getLong("createdAt") ?: 0L,
            admin = snapshot.getBoolean("admin") ?: false
        )
    }

    override suspend fun uploadProtectoraDocument(fileUri: Uri): Result<ProtectoraDocument> = runCatching {
        val uid = auth.currentUser?.uid ?: error("No hay usuario autenticado")
        val fileName = fileUri.lastPathSegment?.substringAfterLast('/')?.substringBefore('?')
            ?.ifBlank { "documento_${System.currentTimeMillis()}" }
            ?: "documento_${System.currentTimeMillis()}"

        val ref = storage.reference.child("protectoras/$uid/$fileName")
        ref.putFile(fileUri).await()
        val url = ref.downloadUrl.await().toString()

        ProtectoraDocument(
            name = fileName,
            url = url
        )
    }

    override suspend fun submitProtectoraDocuments(
        documents: List<ProtectoraDocument>,
        notes: String
    ): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: error("No hay usuario autenticado")

        require(documents.isNotEmpty()) { "Debes subir al menos un documento." }

        val snapshot = db.collection("users")
            .document(uid)
            .get()
            .await()

        val currentDocs = snapshot.get("documentos").toProtectoraDocuments()

        val updatedDocs = (currentDocs + documents)
            .distinctBy { it.url }
            .take(5)

        db.collection("users")
            .document(uid)
            .update(
                mapOf(
                    "documentacionEnviada" to updatedDocs.isNotEmpty(),
                    "documentos" to updatedDocs.map { mapOf("name" to it.name, "url" to it.url) },
                    "motivoRevision" to notes.trim(),
                    "approvalStatus" to ApprovalStatus.PENDING.name
                )
            )
            .await()
    }

    override suspend fun deleteProtectoraDocument(document: ProtectoraDocument): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: error("No hay usuario autenticado")

        val snapshot = db.collection("users")
            .document(uid)
            .get()
            .await()

        val currentDocs = snapshot.get("documentos").toProtectoraDocuments()

        val updatedDocs = currentDocs.filterNot { it.url == document.url }

        db.collection("users")
            .document(uid)
            .update(
                mapOf(
                    "documentos" to updatedDocs.map { mapOf("name" to it.name, "url" to it.url) },
                    "documentacionEnviada" to updatedDocs.isNotEmpty()
                )
            )
            .await()

        try {
            storage.getReferenceFromUrl(document.url).delete().await()
        } catch (_: Exception) {
        }
    }

    override suspend fun getPendingProtectoras(): Result<List<User>> = runCatching {
        val snapshot = db.collection("users")
            .whereEqualTo("role", UserRole.PROTECTORA.name)
            .whereEqualTo("approvalStatus", ApprovalStatus.PENDING.name)
            .get()
            .await()

        snapshot.documents.map { document ->
            val roleString = document.getString("role") ?: UserRole.USER.name
            val approvalStatusString =
                document.getString("approvalStatus") ?: ApprovalStatus.PENDING.name

            val documentos = document.get("documentos").toProtectoraDocuments()

            User(
                uid = document.getString("uid") ?: document.id,
                role = roleString.toUserRole(),
                approvalStatus = approvalStatusString.toApprovalStatus(),

                nombre = document.getString("nombre").orEmpty(),
                apellido = document.getString("apellido").orEmpty(),
                username = document.getString("username").orEmpty(),
                email = document.getString("email").orEmpty(),
                telefono = document.getString("telefono").orEmpty(),
                direccion = document.getString("direccion").orEmpty(),
                photoUrl = document.getString("photoUrl").orEmpty(),

                followers = document.getLong("followers")?.toInt() ?: 0,
                following = document.getLong("following")?.toInt() ?: 0,

                nombreProtectora = document.getString("nombreProtectora").orEmpty(),
                descripcionProtectora = document.getString("descripcionProtectora").orEmpty(),
                web = document.getString("web").orEmpty(),
                facebook = document.getString("facebook").orEmpty(),
                instagram = document.getString("instagram").orEmpty(),
                comunidad = document.getString("comunidad").orEmpty(),
                provincia = document.getString("provincia").orEmpty(),
                ciudad = document.getString("ciudad").orEmpty(),

                latitude = document.getDouble("latitude") ?: 0.0,
                longitude = document.getDouble("longitude") ?: 0.0,

                documentacionEnviada = documentos.isNotEmpty(),
                documentos = documentos,
                motivoRevision = document.getString("motivoRevision").orEmpty(),

                createdAt = document.getLong("createdAt") ?: 0L,
                admin = document.getBoolean("admin") ?: false
            )
        }
    }

    override suspend fun approveProtectora(uid: String): Result<Unit> = runCatching {
        db.collection("users")
            .document(uid)
            .update(
                mapOf(
                    "approvalStatus" to ApprovalStatus.APPROVED.name
                )
            )
            .await()
    }

    override suspend fun rejectProtectora(uid: String, reason: String): Result<Unit> = runCatching {
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

    private fun String.toUserRole(): UserRole {
        return try {
            UserRole.valueOf(this)
        } catch (_: Exception) {
            UserRole.USER
        }
    }

    private fun String.toApprovalStatus(): ApprovalStatus {
        return try {
            ApprovalStatus.valueOf(this)
        } catch (_: Exception) {
            ApprovalStatus.PENDING
        }
    }

    private fun Any?.toProtectoraDocuments(): List<ProtectoraDocument> {
        val rawList = this as? List<*> ?: return emptyList()

        return rawList.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            val name = map["name"] as? String ?: "Documento"
            val url = map["url"] as? String ?: return@mapNotNull null

            ProtectoraDocument(
                name = name,
                url = url
            )
        }
    }
}