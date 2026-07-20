package com.example.petscue.util


import androidx.lifecycle.SavedStateHandle
import com.example.petscue.data.model.AvisoMapa
import com.example.petscue.data.model.Pet
import com.example.petscue.data.model.User
import com.example.petscue.data.model.UserRole
import com.example.petscue.data.repository.AlertRepository
import com.example.petscue.data.repository.PetRepository
import com.example.petscue.data.repository.ProfileRepository
import com.example.petscue.ui.mapa.alerts.create.AlertType
import com.example.petscue.ui.mapa.alerts.create.CreateAlertViewModel
import com.example.petscue.ui.novedades.location.SelectedLocation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CreateAlertViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val petRepository: PetRepository = mock()
    private val alertRepository: AlertRepository = mock()
    private val profileRepository: ProfileRepository = mock()

    private fun createViewModel(petId: String = "pet-1"): CreateAlertViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("petId" to petId))
        return CreateAlertViewModel(
            petRepository = petRepository,
            alertRepository = alertRepository,
            profileRepository = profileRepository,
            savedStateHandle = savedStateHandle
        )
    }

    @Test
    fun loadPet_siMascotaExiste_laCargaCorrectamente() = runTest {
        val pet = mock<Pet>()
        whenever(pet.id).thenReturn("pet-1")
        whenever(petRepository.getAnyPetById("pet-1")).thenReturn(pet)

        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(pet, viewModel.uiState.value.pet)
        assertEquals("pet-1", viewModel.uiState.value.petId)
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun loadPet_siMascotaNoExiste_muestraError() = runTest {
        whenever(petRepository.getAnyPetById("pet-1")).thenReturn(null)

        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(null, viewModel.uiState.value.pet)
        assertEquals("No se encontró la mascota", viewModel.uiState.value.error)
    }

    @Test
    fun onAlertTypeSelected_actualizaTipo() = runTest {
        whenever(petRepository.getAnyPetById("pet-1")).thenReturn(null)
        val viewModel = createViewModel()

        viewModel.onAlertTypeSelected(AlertType.FOUND)

        assertEquals(AlertType.FOUND, viewModel.uiState.value.alertType)
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun onLocationSelected_actualizaUbicacion() = runTest {
        whenever(petRepository.getAnyPetById("pet-1")).thenReturn(null)
        val viewModel = createViewModel()

        val location = SelectedLocation(
            address = "Calle Mayor 1",
            lat = 40.0,
            lng = -3.0
        )

        viewModel.onLocationSelected(location)

        assertEquals(location, viewModel.uiState.value.selectedLocation)
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun saveAlert_siNoHayMascota_muestraError() = runTest {
        whenever(petRepository.getAnyPetById("pet-1")).thenReturn(null)
        val viewModel = createViewModel()

        viewModel.saveAlert()

        assertEquals("No hay mascota seleccionada.", viewModel.uiState.value.error)
    }

    @Test
    fun saveAlert_siNoHayUbicacion_muestraError() = runTest {
        val pet = mock<Pet>()
        whenever(pet.id).thenReturn("pet-1")
        whenever(petRepository.getAnyPetById("pet-1")).thenReturn(pet)

        val viewModel = createViewModel()
        viewModel.saveAlert()

        assertEquals("Selecciona una ubicación para el aviso.", viewModel.uiState.value.error)
    }

    @Test
    fun saveAlert_siYaExisteAvisoActivo_muestraError() = runTest {
        val pet = mock<Pet>()
        whenever(pet.id).thenReturn("pet-1")
        whenever(petRepository.getAnyPetById("pet-1")).thenReturn(pet)
        whenever(alertRepository.getAlertByPetId("pet-1")).thenReturn(mock<AvisoMapa>())

        val viewModel = createViewModel()
        viewModel.onLocationSelected(
            SelectedLocation(
                address = "Calle Mayor 1",
                lat = 40.0,
                lng = -3.0
            )
        )

        viewModel.saveAlert()

        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals("Esta mascota ya tiene un aviso activo.", viewModel.uiState.value.error)
    }

    @Test
    fun saveAlert_siSeGuardaCorrectamente_actualizaExito() = runTest {
        val pet = mock<Pet>()
        whenever(pet.id).thenReturn("pet-1")
        whenever(pet.userId).thenReturn("user-1")
        whenever(pet.nombre).thenReturn("Luna")
        whenever(pet.genero).thenReturn("Hembra")
        whenever(pet.raza).thenReturn("Mestiza")
        whenever(pet.edad).thenReturn("2 años")
        whenever(pet.descripcion).thenReturn("Muy sociable")
        whenever(pet.fotos).thenReturn(listOf("foto.jpg"))

        val user = User(
            nombre = "Ana",
            apellido = "López",
            username = "ana",
            email = "test@email.com",
            role = UserRole.USER
        )

        whenever(petRepository.getAnyPetById("pet-1")).thenReturn(pet)
        whenever(alertRepository.getAlertByPetId("pet-1")).thenReturn(null)
        whenever(profileRepository.getCurrentUserProfile()).thenReturn(user)

        val viewModel = createViewModel()
        viewModel.onLocationSelected(
            SelectedLocation(
                address = "Calle Mayor 1",
                lat = 40.0,
                lng = -3.0
            )
        )

        viewModel.saveAlert()

        assertFalse(viewModel.uiState.value.isSaving)
        assertTrue(viewModel.uiState.value.isSaved)
        verify(alertRepository).upsertAlert(any())
    }

    @Test
    fun saveAlert_siFallaAlGuardar_muestraError() = runTest {
        val pet = mock<Pet>()
        whenever(pet.id).thenReturn("pet-1")
        whenever(pet.userId).thenReturn("user-1")
        whenever(pet.nombre).thenReturn("Luna")
        whenever(pet.genero).thenReturn("Hembra")
        whenever(pet.raza).thenReturn("Mestiza")
        whenever(pet.edad).thenReturn("2 años")
        whenever(pet.descripcion).thenReturn("Muy sociable")
        whenever(pet.fotos).thenReturn(listOf("foto.jpg"))

        val user = User(
            nombre = "Ana",
            apellido = "López",
            username = "ana",
            email = "test@email.com",
            role = UserRole.USER
        )

        whenever(petRepository.getAnyPetById("pet-1")).thenReturn(pet)
        whenever(alertRepository.getAlertByPetId("pet-1")).thenReturn(null)
        whenever(profileRepository.getCurrentUserProfile()).thenReturn(user)
        whenever(alertRepository.upsertAlert(any()))
            .thenThrow(RuntimeException("No se pudo guardar el aviso."))

        val viewModel = createViewModel()
        viewModel.onLocationSelected(
            SelectedLocation(
                address = "Calle Mayor 1",
                lat = 40.0,
                lng = -3.0
            )
        )

        viewModel.saveAlert()

        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals("No se pudo guardar el aviso.", viewModel.uiState.value.error)
    }
}