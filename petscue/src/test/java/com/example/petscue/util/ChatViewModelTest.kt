package com.example.petscue.util

import com.example.petscue.ui.mensajes.detail.ChatViewModel
import androidx.lifecycle.SavedStateHandle
import com.example.petscue.data.model.ChatMessage
import com.example.petscue.data.repository.MensajesRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mensajesRepository: MensajesRepository = mock()
    private val firestore: FirebaseFirestore = mock()
    private val auth: FirebaseAuth = mock()

    private fun createViewModel(
        conversationId: String = "conv-1",
        currentUserId: String = "user-1",
        messagesFlow: MutableStateFlow<List<ChatMessage>> = MutableStateFlow(emptyList())
    ): ChatViewModel {
        val firebaseUser: FirebaseUser = mock()
        whenever(firebaseUser.uid).thenReturn(currentUserId)
        whenever(auth.currentUser).thenReturn(firebaseUser)

        whenever(mensajesRepository.observeMessages(conversationId))
            .thenReturn(messagesFlow)


        val conversationsCollection: CollectionReference = mock()
        val conversationDocument: DocumentReference = mock()
        whenever(firestore.collection("conversations")).thenReturn(conversationsCollection)
        whenever(conversationsCollection.document(conversationId)).thenReturn(conversationDocument)
        whenever(conversationDocument.addSnapshotListener(any())).thenReturn(mock())

        val savedStateHandle = SavedStateHandle(
            mapOf("conversationId" to conversationId)
        )

        return ChatViewModel(
            savedStateHandle = savedStateHandle,
            mensajesRepository = mensajesRepository,
            firestore = firestore,
            auth = auth
        )
    }

    @Test
    fun init_cargaMensajes_yMarcaComoLeido() = runTest {
        val messagesFlow = MutableStateFlow(
            listOf(
                ChatMessage(
                    id = "msg-1",
                    conversationId = "conv-1",
                    senderId = "user-2",
                    senderName = "Ana",
                    text = "Hola"
                )
            )
        )

        val viewModel = createViewModel(messagesFlow = messagesFlow)

        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals("user-1", state.currentUserId)
        assertEquals(1, state.messages.size)

        verify(mensajesRepository).markConversationAsRead("conv-1", "user-1")
        collector.cancel()
    }

    @Test
    fun observeMessages_siCambianLosMensajes_actualizaEstado() = runTest {
        val messagesFlow = MutableStateFlow(emptyList<ChatMessage>())
        val viewModel = createViewModel(messagesFlow = messagesFlow)

        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        messagesFlow.value = listOf(
            ChatMessage(
                id = "msg-1",
                conversationId = "conv-1",
                senderId = "user-2",
                senderName = "Ana",
                text = "Hola"
            ),
            ChatMessage(
                id = "msg-2",
                conversationId = "conv-1",
                senderId = "user-1",
                senderName = "Yo",
                text = "Qué tal"
            )
        )
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.messages.size)
        collector.cancel()
    }

    @Test
    fun sendMessage_siTextoVacio_noEnviaNada() = runTest {
        val viewModel = createViewModel()

        viewModel.sendMessage("   ")
        advanceUntilIdle()

        verify(mensajesRepository, org.mockito.kotlin.never()).sendMessage(
            conversationId = any(),
            senderId = any(),
            senderName = any(),
            text = any()
        )
    }

    @Test
    fun sendMessage_siTextoValido_enviaMensajeConUsername() = runTest {
        val messagesFlow = MutableStateFlow(emptyList<ChatMessage>())

        val firebaseUser: FirebaseUser = mock()
        whenever(firebaseUser.uid).thenReturn("user-1")
        whenever(auth.currentUser).thenReturn(firebaseUser)

        whenever(mensajesRepository.observeMessages("conv-1"))
            .thenReturn(messagesFlow)
        whenever(mensajesRepository.markConversationAsRead("conv-1", "user-1"))
            .thenReturn(Unit)

        val conversationsCollection: CollectionReference = mock()
        val conversationDocument: DocumentReference = mock()
        whenever(firestore.collection("conversations")).thenReturn(conversationsCollection)
        whenever(conversationsCollection.document("conv-1")).thenReturn(conversationDocument)
        whenever(conversationDocument.addSnapshotListener(any())).thenReturn(mock())

        val usersCollection: CollectionReference = mock()
        val userDocument: DocumentReference = mock()
        val userSnapshot: DocumentSnapshot = mock()

        whenever(firestore.collection("users")).thenReturn(usersCollection)
        whenever(usersCollection.document("user-1")).thenReturn(userDocument)
        whenever(userDocument.get()).thenReturn(Tasks.forResult(userSnapshot))
        whenever(userSnapshot.getString("username")).thenReturn("ana123")

        val savedStateHandle = SavedStateHandle(mapOf("conversationId" to "conv-1"))

        val viewModel = ChatViewModel(
            savedStateHandle = savedStateHandle,
            mensajesRepository = mensajesRepository,
            firestore = firestore,
            auth = auth
        )

        viewModel.sendMessage("  Hola mundo  ")
        advanceUntilIdle()

        verify(mensajesRepository).sendMessage(
            conversationId = "conv-1",
            senderId = "user-1",
            senderName = "ana123",
            text = "Hola mundo"
        )
    }

    @Test
    fun sendMessage_siNoHayUsername_usaUsuarioPorDefecto() = runTest {
        val messagesFlow = MutableStateFlow(emptyList<ChatMessage>())

        val firebaseUser: FirebaseUser = mock()
        whenever(firebaseUser.uid).thenReturn("user-1")
        whenever(auth.currentUser).thenReturn(firebaseUser)

        whenever(mensajesRepository.observeMessages("conv-1"))
            .thenReturn(messagesFlow)
        whenever(mensajesRepository.markConversationAsRead("conv-1", "user-1"))
            .thenReturn(Unit)

        val conversationsCollection: CollectionReference = mock()
        val conversationDocument: DocumentReference = mock()
        whenever(firestore.collection("conversations")).thenReturn(conversationsCollection)
        whenever(conversationsCollection.document("conv-1")).thenReturn(conversationDocument)
        whenever(conversationDocument.addSnapshotListener(any())).thenReturn(mock())

        val usersCollection: CollectionReference = mock()
        val userDocument: DocumentReference = mock()
        val userSnapshot: DocumentSnapshot = mock()

        whenever(firestore.collection("users")).thenReturn(usersCollection)
        whenever(usersCollection.document("user-1")).thenReturn(userDocument)
        whenever(userDocument.get()).thenReturn(Tasks.forResult(userSnapshot))
        whenever(userSnapshot.getString("username")).thenReturn(null)
        whenever(userSnapshot.getString("name")).thenReturn(null)

        val savedStateHandle = SavedStateHandle(mapOf("conversationId" to "conv-1"))

        val viewModel = ChatViewModel(
            savedStateHandle = savedStateHandle,
            mensajesRepository = mensajesRepository,
            firestore = firestore,
            auth = auth
        )

        viewModel.sendMessage("Hola")
        advanceUntilIdle()

        verify(mensajesRepository).sendMessage(
            conversationId = "conv-1",
            senderId = "user-1",
            senderName = "Usuario",
            text = "Hola"
        )
    }

    @Test
    fun init_sinConversationId_noMarcaComoLeido() = runTest {
        val firebaseUser: FirebaseUser = mock()
        whenever(firebaseUser.uid).thenReturn("user-1")
        whenever(auth.currentUser).thenReturn(firebaseUser)

        whenever(mensajesRepository.observeMessages(""))
            .thenReturn(MutableStateFlow(emptyList()))

        val conversationsCollection: CollectionReference = mock()
        val conversationDocument: DocumentReference = mock()
        whenever(firestore.collection("conversations")).thenReturn(conversationsCollection)
        whenever(conversationsCollection.document("")).thenReturn(conversationDocument)
        whenever(conversationDocument.addSnapshotListener(any())).thenReturn(mock())

        val viewModel = ChatViewModel(
            savedStateHandle = SavedStateHandle(mapOf("conversationId" to "")),
            mensajesRepository = mensajesRepository,
            firestore = firestore,
            auth = auth
        )

        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.messages.isEmpty())
        verify(mensajesRepository, org.mockito.kotlin.never())
            .markConversationAsRead(any(), any())

        collector.cancel()
    }
}