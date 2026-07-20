package com.example.petscue.util

import com.example.petscue.data.model.Conversation
import com.example.petscue.data.repository.MensajesRepository
import com.example.petscue.ui.mensajes.MensajesViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class MensajesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mensajesRepository: MensajesRepository = mock()
    private val auth: FirebaseAuth = mock()

    @Test
    fun init_cargaConversaciones_yCurrentUserId() = runTest {
        val firebaseUser: FirebaseUser = mock()
        whenever(firebaseUser.uid).thenReturn("user-1")
        whenever(auth.currentUser).thenReturn(firebaseUser)

        val conversations = listOf(
            Conversation(
                id = "conv-1",
                participantIds = listOf("user-1", "user-2"),
                lastMessage = "Hola"
            ),
            Conversation(
                id = "conv-2",
                participantIds = listOf("user-1", "user-3"),
                lastMessage = "¿Qué tal?"
            )
        )

        whenever(mensajesRepository.observeConversations("user-1"))
            .thenReturn(flowOf(conversations))

        val viewModel = MensajesViewModel(mensajesRepository, auth)

        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals("user-1", state.currentUserId)
        assertEquals(2, state.conversations.size)
        assertEquals(null, state.errorMessage)

        collector.cancel()
    }
    @Test
    fun init_siFlowFalla_muestraError() = runTest {
        val firebaseUser: FirebaseUser = mock()
        whenever(firebaseUser.uid).thenReturn("user-1")
        whenever(auth.currentUser).thenReturn(firebaseUser)

        whenever(mensajesRepository.observeConversations("user-1"))
            .thenReturn(
                flow {
                    throw RuntimeException("Error al cargar conversaciones")
                }
            )

        val viewModel = MensajesViewModel(mensajesRepository, auth)

        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals("user-1", state.currentUserId)
        assertTrue(state.conversations.isEmpty())
        assertEquals("Error al cargar conversaciones", state.errorMessage)

        collector.cancel()
    }
    @Test
    fun deleteConversation_conIdValido_llamaRepositorio() = runTest {
        val firebaseUser: FirebaseUser = mock()
        whenever(firebaseUser.uid).thenReturn("user-1")
        whenever(auth.currentUser).thenReturn(firebaseUser)
        whenever(mensajesRepository.observeConversations("user-1"))
            .thenReturn(flowOf(emptyList()))

        val viewModel = MensajesViewModel(mensajesRepository, auth)
        viewModel.deleteConversation("conv-1")
        advanceUntilIdle()

        verify(mensajesRepository).hideConversation(
            conversationId = "conv-1",
            userId = "user-1"
        )
    }

    @Test
    fun deleteConversation_conIdVacio_noLlamaRepositorio() = runTest {
        val firebaseUser: FirebaseUser = mock()
        whenever(firebaseUser.uid).thenReturn("user-1")
        whenever(auth.currentUser).thenReturn(firebaseUser)
        whenever(mensajesRepository.observeConversations("user-1"))
            .thenReturn(flowOf(emptyList()))

        val viewModel = MensajesViewModel(mensajesRepository, auth)
        viewModel.deleteConversation("")
        advanceUntilIdle()

        verify(mensajesRepository, never()).hideConversation(
            conversationId = org.mockito.kotlin.any(),
            userId = org.mockito.kotlin.any()
        )
    }

    @Test
    fun deleteConversation_siNoHayUsuario_noLlamaRepositorio() = runTest {
        whenever(auth.currentUser).thenReturn(null)
        whenever(mensajesRepository.observeConversations(""))
            .thenReturn(flowOf(emptyList()))

        val viewModel = MensajesViewModel(mensajesRepository, auth)
        viewModel.deleteConversation("conv-1")
        advanceUntilIdle()

        verify(mensajesRepository, never()).hideConversation(
            conversationId = org.mockito.kotlin.any(),
            userId = org.mockito.kotlin.any()
        )
    }
}