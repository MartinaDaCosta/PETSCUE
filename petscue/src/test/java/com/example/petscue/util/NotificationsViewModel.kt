package com.example.petscue.util

import com.example.petscue.data.model.AppNotification
import com.example.petscue.data.repository.NotificationsRepository
import com.example.petscue.ui.notifications.NotificationsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: NotificationsRepository = mock()

    @Test
    fun init_cargaNotificaciones_yCalculaNoLeidas() = runTest {
        val notificationsFlow = MutableStateFlow(
            listOf(
                AppNotification(
                    id = "1",
                    title = "Alerta SOS",
                    body = "Mascota perdida cerca de ti",
                    isRead = false
                ),
                AppNotification(
                    id = "2",
                    title = "Mensaje",
                    body = "Tienes un nuevo mensaje",
                    isRead = true
                ),
                AppNotification(
                    id = "3",
                    title = "Aviso",
                    body = "Nueva publicación",
                    isRead = false
                )
            )
        )

        whenever(repository.observeNotifications()).thenReturn(notificationsFlow)

        val viewModel = NotificationsViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.items.size)
        assertEquals(2, state.unreadCount)
    }

    @Test
    fun init_siCambianLasNotificaciones_actualizaEstado() = runTest {
        val notificationsFlow = MutableStateFlow(emptyList<AppNotification>())
        whenever(repository.observeNotifications()).thenReturn(notificationsFlow)

        val viewModel = NotificationsViewModel(repository)
        advanceUntilIdle()

        notificationsFlow.value = listOf(
            AppNotification(
                id = "1",
                title = "Alerta",
                body = "Nueva alerta SOS",
                isRead = false
            )
        )
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.items.size)
        assertEquals(1, viewModel.uiState.value.unreadCount)
    }

    @Test
    fun markAsRead_llamaAlRepositorio() = runTest {
        val notificationsFlow = MutableStateFlow(emptyList<AppNotification>())
        whenever(repository.observeNotifications()).thenReturn(notificationsFlow)

        val viewModel = NotificationsViewModel(repository)
        viewModel.markAsRead("notif-1")

        verify(repository).markAsRead("notif-1")
    }
}