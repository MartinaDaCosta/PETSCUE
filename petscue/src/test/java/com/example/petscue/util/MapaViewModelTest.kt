package com.example.petscue.util


import androidx.lifecycle.SavedStateHandle
import com.example.petscue.data.repository.AlertRepository
import com.example.petscue.data.repository.AuthRepository
import com.example.petscue.data.repository.UserLocationRepository
import com.example.petscue.ui.mapa.MapaViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class MapaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val alertRepository: AlertRepository = mock()
    private val authRepository: AuthRepository = mock()
    private val userLocationRepository: UserLocationRepository = mock()

    private fun createViewModel(
        initialRadio: Double = 1500.0,
        currentUserId: String = "user-1"
    ): MapaViewModel {
        whenever(authRepository.getCurrentUserId()).thenReturn(currentUserId)
        whenever(alertRepository.getAllAlerts()).thenReturn(flowOf(emptyList()))

        val savedStateHandle = SavedStateHandle(
            mapOf("radioNotificaciones" to initialRadio)
        )

        return MapaViewModel(
            alertRepository = alertRepository,
            authRepository = authRepository,
            savedStateHandle = savedStateHandle,
            userLocationRepository = userLocationRepository
        )
    }

    @Test
    fun init_cargaRadioInicial_yCurrentUserId() = runTest {
        val viewModel = createViewModel(initialRadio = 2000.0, currentUserId = "user-99")
        advanceUntilIdle()

        assertEquals(2000.0, viewModel.uiState.value.radioNotificaciones, 0.0)
        assertEquals("user-99", viewModel.uiState.value.currentUserId)
    }

    @Test
    fun onRadioChanged_actualizaRadio_yUbicacionSiHayCoordenadas() = runTest {
        val viewModel = createViewModel()

        viewModel.onRadioChanged(
            value = 2500.0,
            currentLat = 40.4,
            currentLng = -3.7
        )

        assertEquals(2500.0, viewModel.uiState.value.radioNotificaciones, 0.0)

        verify(userLocationRepository).updateUserLocation(
            lat = 40.4,
            lng = -3.7,
            notificationsEnabled = true,
            notificationRadius = 2500.0
        )
    }

    @Test
    fun onRadioChanged_actualizaRadio_sinUbicacionNoLlamaRepositorio() = runTest {
        val viewModel = createViewModel()

        viewModel.onRadioChanged(
            value = 3000.0,
            currentLat = null,
            currentLng = null
        )

        assertEquals(3000.0, viewModel.uiState.value.radioNotificaciones, 0.0)
    }

    @Test
    fun updateNotificationRadius_actualizaSoloElRadio() = runTest {
        val viewModel = createViewModel()

        viewModel.updateNotificationRadius(1800.0)

        assertEquals(1800.0, viewModel.uiState.value.radioNotificaciones, 0.0)
    }

    @Test
    fun updateMyLocation_enviaUbicacionConRadioActual() = runTest {
        val viewModel = createViewModel(initialRadio = 1600.0)

        viewModel.updateMyLocation(40.1, -3.2)

        verify(userLocationRepository).updateUserLocation(
            lat = 40.1,
            lng = -3.2,
            notificationsEnabled = true,
            notificationRadius = 1600.0
        )
    }

    @Test
    fun isMyAlert_devuelveTrue_siEsDelUsuarioActual() = runTest {
        val viewModel = createViewModel(currentUserId = "user-1")

        assertTrue(viewModel.isMyAlert("user-1"))
    }

    @Test
    fun isMyAlert_devuelveFalse_siNoEsDelUsuarioActual() = runTest {
        val viewModel = createViewModel(currentUserId = "user-1")

        assertFalse(viewModel.isMyAlert("user-2"))
    }

    @Test
    fun isMyAlert_devuelveFalse_siUserIdEstaVacio() = runTest {
        val viewModel = createViewModel(currentUserId = "user-1")

        assertFalse(viewModel.isMyAlert(""))
    }

    @Test
    fun deleteAlert_siSeElimina_correctamenteInvocaCallback() = runTest {
        var callbackCalled = false
        val viewModel = createViewModel()

        viewModel.deleteAlert("pet-1") {
            callbackCalled = true
        }
        advanceUntilIdle()

        verify(alertRepository).deleteAlertByPetId("pet-1")
        assertTrue(callbackCalled)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun deleteAlert_siFalla_muestraError() = runTest {
        doThrow(RuntimeException("Error al borrar alerta"))
            .whenever(alertRepository)
            .deleteAlertByPetId("pet-1")

        val viewModel = createViewModel()

        viewModel.deleteAlert("pet-1")
        advanceUntilIdle()

        assertEquals("Error al borrar alerta", viewModel.uiState.value.error)
    }
}