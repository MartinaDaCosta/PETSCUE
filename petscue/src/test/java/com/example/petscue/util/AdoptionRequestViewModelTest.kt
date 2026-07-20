package com.example.petscue.util

import com.example.petscue.ui.profile.adopta.request.AdoptionRequestViewModel


import androidx.lifecycle.SavedStateHandle
import com.example.petscue.data.model.Pet
import com.example.petscue.data.repository.MensajesRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AdoptionRequestViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val firestore: FirebaseFirestore = mock()
    private val mensajesRepository: MensajesRepository = mock()
    private val auth: FirebaseAuth = mock()

    private fun mockPetLoad(
        petId: String = "pet-1",
        pet: Pet? = null
    ) {
        val adoptionPetsCollection: CollectionReference = mock()
        val petDocument: DocumentReference = mock()
        val petSnapshot: DocumentSnapshot = mock()

        whenever(firestore.collection("adoption_pets")).thenReturn(adoptionPetsCollection)
        whenever(adoptionPetsCollection.document(petId)).thenReturn(petDocument)
        whenever(petDocument.get()).thenReturn(Tasks.forResult(petSnapshot))
        whenever(petSnapshot.id).thenReturn(petId)
        whenever(petSnapshot.toObject(Pet::class.java)).thenReturn(pet)
    }

    private fun createPet(): Pet {
        return Pet(
            id = "pet-1",
            nombre = "Luna",
            userId = "shelter-1",
            fotos = listOf("foto1.jpg")
        )
    }

    @Test
    fun init_cargaPetCorrectamente() = runTest {
        val pet = createPet()
        mockPetLoad(pet = pet)
        whenever(auth.currentUser).thenReturn(null)

        val viewModel = AdoptionRequestViewModel(
            savedStateHandle = SavedStateHandle(mapOf("petId" to "pet-1")),
            firestore = firestore,
            mensajesRepository = mensajesRepository,
            auth = auth
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("pet-1", state.pet?.id)
        assertEquals("Luna", state.pet?.nombre)
        assertNull(state.error)
    }

    @Test
    fun onMensajeChange_actualizaCampo() = runTest {
        mockPetLoad(pet = null)
        whenever(auth.currentUser).thenReturn(null)

        val viewModel = AdoptionRequestViewModel(
            savedStateHandle = SavedStateHandle(mapOf("petId" to "pet-1")),
            firestore = firestore,
            mensajesRepository = mensajesRepository,
            auth = auth
        )
        advanceUntilIdle()

        viewModel.onMensajeChange("Hola, me interesa adoptar")

        assertEquals("Hola, me interesa adoptar", viewModel.uiState.value.mensaje)
    }

    @Test
    fun onTelefonoChange_actualizaCampo() = runTest {
        mockPetLoad(pet = null)
        whenever(auth.currentUser).thenReturn(null)

        val viewModel = AdoptionRequestViewModel(
            savedStateHandle = SavedStateHandle(mapOf("petId" to "pet-1")),
            firestore = firestore,
            mensajesRepository = mensajesRepository,
            auth = auth
        )
        advanceUntilIdle()

        viewModel.onTelefonoChange("666555444")

        assertEquals("666555444", viewModel.uiState.value.telefono)
    }

    @Test
    fun onViviendaChange_actualizaCampo() = runTest {
        mockPetLoad(pet = null)
        whenever(auth.currentUser).thenReturn(null)

        val viewModel = AdoptionRequestViewModel(
            savedStateHandle = SavedStateHandle(mapOf("petId" to "pet-1")),
            firestore = firestore,
            mensajesRepository = mensajesRepository,
            auth = auth
        )
        advanceUntilIdle()

        viewModel.onViviendaChange("Piso con terraza")

        assertEquals("Piso con terraza", viewModel.uiState.value.vivienda)
    }

    @Test
    fun consumeNavigation_limpiaConversationId() = runTest {
        val pet = createPet()
        mockPetLoad(pet = pet)

        val firebaseUser: FirebaseUser = mock()
        whenever(firebaseUser.uid).thenReturn("user-1")
        whenever(auth.currentUser).thenReturn(firebaseUser)

        val usersCollection: CollectionReference = mock()
        val userDocument: DocumentReference = mock()
        val userSnapshot: DocumentSnapshot = mock()
        whenever(firestore.collection("users")).thenReturn(usersCollection)
        whenever(usersCollection.document("user-1")).thenReturn(userDocument)
        whenever(userDocument.get()).thenReturn(Tasks.forResult(userSnapshot))
        whenever(userSnapshot.getString("username")).thenReturn("ana123")

        val requestsCollection: CollectionReference = mock()
        val newRequestDocument: DocumentReference = mock()
        whenever(firestore.collection("adoption_requests")).thenReturn(requestsCollection)
        whenever(requestsCollection.document()).thenReturn(newRequestDocument)
        whenever(newRequestDocument.id).thenReturn("req-1")
        whenever(requestsCollection.document("req-1")).thenReturn(newRequestDocument)
        whenever(newRequestDocument.set(any())).thenReturn(Tasks.forResult(null))

        whenever(
            mensajesRepository.createOrGetAdoptionConversation(
                currentUserId = "user-1",
                shelterId = "shelter-1",
                petId = "pet-1",
                petName = "Luna",
                petImageUrl = "foto1.jpg",
                adoptionFormId = "req-1",
                adoptionFormStatus = "PENDIENTE"
            )
        ).thenReturn("conv-1")

        val viewModel = AdoptionRequestViewModel(
            savedStateHandle = SavedStateHandle(mapOf("petId" to "pet-1")),
            firestore = firestore,
            mensajesRepository = mensajesRepository,
            auth = auth
        )
        advanceUntilIdle()

        viewModel.onTelefonoChange("666555444")
        viewModel.onViviendaChange("Piso con terraza")
        viewModel.submitRequest()
        advanceUntilIdle()

        assertEquals("conv-1", viewModel.uiState.value.submittedConversationId)

        viewModel.consumeNavigation()

        assertNull(viewModel.uiState.value.submittedConversationId)
    }

    @Test
    fun submitRequest_sinLogin_muestraError() = runTest {
        val pet = createPet()
        mockPetLoad(pet = pet)
        whenever(auth.currentUser).thenReturn(null)

        val viewModel = AdoptionRequestViewModel(
            savedStateHandle = SavedStateHandle(mapOf("petId" to "pet-1")),
            firestore = firestore,
            mensajesRepository = mensajesRepository,
            auth = auth
        )
        advanceUntilIdle()

        viewModel.submitRequest()

        assertEquals("Debes iniciar sesión", viewModel.uiState.value.error)
    }

    @Test
    fun submitRequest_sinPet_muestraError() = runTest {
        mockPetLoad(pet = null)

        val firebaseUser: FirebaseUser = mock()
        whenever(firebaseUser.uid).thenReturn("user-1")
        whenever(auth.currentUser).thenReturn(firebaseUser)

        val viewModel = AdoptionRequestViewModel(
            savedStateHandle = SavedStateHandle(mapOf("petId" to "pet-1")),
            firestore = firestore,
            mensajesRepository = mensajesRepository,
            auth = auth
        )
        advanceUntilIdle()

        viewModel.submitRequest()

        assertEquals("No se ha encontrado el animal", viewModel.uiState.value.error)
    }

    @Test
    fun submitRequest_sinTelefonoOVivienda_muestraError() = runTest {
        val pet = createPet()
        mockPetLoad(pet = pet)

        val firebaseUser: FirebaseUser = mock()
        whenever(firebaseUser.uid).thenReturn("user-1")
        whenever(auth.currentUser).thenReturn(firebaseUser)

        val viewModel = AdoptionRequestViewModel(
            savedStateHandle = SavedStateHandle(mapOf("petId" to "pet-1")),
            firestore = firestore,
            mensajesRepository = mensajesRepository,
            auth = auth
        )
        advanceUntilIdle()

        viewModel.onTelefonoChange("")
        viewModel.onViviendaChange("")
        viewModel.submitRequest()

        assertEquals("Completa al menos teléfono y vivienda", viewModel.uiState.value.error)
    }

    @Test
    fun submitRequest_siTodoVaBien_enviaSolicitudYMensaje() = runTest {
        val pet = createPet()
        mockPetLoad(pet = pet)

        val firebaseUser: FirebaseUser = mock()
        whenever(firebaseUser.uid).thenReturn("user-1")
        whenever(auth.currentUser).thenReturn(firebaseUser)

        val usersCollection: CollectionReference = mock()
        val userDocument: DocumentReference = mock()
        val userSnapshot: DocumentSnapshot = mock()
        whenever(firestore.collection("users")).thenReturn(usersCollection)
        whenever(usersCollection.document("user-1")).thenReturn(userDocument)
        whenever(userDocument.get()).thenReturn(Tasks.forResult(userSnapshot))
        whenever(userSnapshot.getString("username")).thenReturn("ana123")

        val requestsCollection: CollectionReference = mock()
        val newRequestDocument: DocumentReference = mock()
        whenever(firestore.collection("adoption_requests")).thenReturn(requestsCollection)
        whenever(requestsCollection.document()).thenReturn(newRequestDocument)
        whenever(newRequestDocument.id).thenReturn("req-1")
        whenever(requestsCollection.document("req-1")).thenReturn(newRequestDocument)
        whenever(newRequestDocument.set(any())).thenReturn(Tasks.forResult(null))

        whenever(
            mensajesRepository.createOrGetAdoptionConversation(
                currentUserId = "user-1",
                shelterId = "shelter-1",
                petId = "pet-1",
                petName = "Luna",
                petImageUrl = "foto1.jpg",
                adoptionFormId = "req-1",
                adoptionFormStatus = "PENDIENTE"
            )
        ).thenReturn("conv-1")

        val viewModel = AdoptionRequestViewModel(
            savedStateHandle = SavedStateHandle(mapOf("petId" to "pet-1")),
            firestore = firestore,
            mensajesRepository = mensajesRepository,
            auth = auth
        )
        advanceUntilIdle()

        viewModel.onMensajeChange("Me interesa mucho")
        viewModel.onTelefonoChange("666555444")
        viewModel.onViviendaChange("Piso con terraza")
        viewModel.onExperienciaChange("He tenido perros")
        viewModel.onOtrosAnimalesChange("Un gato")
        viewModel.submitRequest()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertEquals("conv-1", state.submittedConversationId)
        assertNull(state.error)

        verify(mensajesRepository).sendMessage(
            conversationId = eq("conv-1"),
            senderId = eq("user-1"),
            senderName = eq("ana123"),
            text = any()
        )
    }

    @Test
    fun submitRequest_siFalla_muestraError() = runTest {
        val pet = createPet()
        mockPetLoad(pet = pet)

        val firebaseUser: FirebaseUser = mock()
        whenever(firebaseUser.uid).thenReturn("user-1")
        whenever(auth.currentUser).thenReturn(firebaseUser)

        val usersCollection: CollectionReference = mock()
        val userDocument: DocumentReference = mock()
        val userSnapshot: DocumentSnapshot = mock()
        whenever(firestore.collection("users")).thenReturn(usersCollection)
        whenever(usersCollection.document("user-1")).thenReturn(userDocument)
        whenever(userDocument.get()).thenReturn(Tasks.forResult(userSnapshot))
        whenever(userSnapshot.getString("username")).thenReturn("ana123")

        val requestsCollection: CollectionReference = mock()
        val newRequestDocument: DocumentReference = mock()
        whenever(firestore.collection("adoption_requests")).thenReturn(requestsCollection)
        whenever(requestsCollection.document()).thenReturn(newRequestDocument)
        whenever(newRequestDocument.id).thenReturn("req-1")
        whenever(requestsCollection.document("req-1")).thenReturn(newRequestDocument)
        whenever(newRequestDocument.set(any()))
            .thenReturn(Tasks.forException(RuntimeException("No se pudo enviar la solicitud")))

        val viewModel = AdoptionRequestViewModel(
            savedStateHandle = SavedStateHandle(mapOf("petId" to "pet-1")),
            firestore = firestore,
            mensajesRepository = mensajesRepository,
            auth = auth
        )
        advanceUntilIdle()

        viewModel.onTelefonoChange("666555444")
        viewModel.onViviendaChange("Piso con terraza")
        viewModel.submitRequest()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSubmitting)
        assertEquals("No se pudo enviar la solicitud", viewModel.uiState.value.error)
        verify(mensajesRepository, never()).sendMessage(
            conversationId = any(),
            senderId = any(),
            senderName = any(),
            text = any()
        )
    }
}