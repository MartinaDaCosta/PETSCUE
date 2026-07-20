package com.example.petscue.util


import android.net.Uri
import com.example.petscue.data.model.UserRole
import com.example.petscue.domain.usecase.RegisterUseCase
import com.example.petscue.ui.auth.signup.SignupViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SignupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val registerUseCase: RegisterUseCase = mock()

    @Test
    fun onNombreChange_actualizaNombre() = runTest {
        val viewModel = SignupViewModel(registerUseCase)

        viewModel.onNombreChange("Ana")

        assertEquals("Ana", viewModel.uiState.value.nombre)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun onApellidoChange_actualizaApellido() = runTest {
        val viewModel = SignupViewModel(registerUseCase)

        viewModel.onApellidoChange("López")

        assertEquals("López", viewModel.uiState.value.apellido)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun onUsernameChange_actualizaUsername() = runTest {
        val viewModel = SignupViewModel(registerUseCase)

        viewModel.onUsernameChange("ana123")

        assertEquals("ana123", viewModel.uiState.value.username)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun onEmailChange_actualizaEmail() = runTest {
        val viewModel = SignupViewModel(registerUseCase)

        viewModel.onEmailChange("test@email.com")

        assertEquals("test@email.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun onPasswordChange_actualizaPassword() = runTest {
        val viewModel = SignupViewModel(registerUseCase)

        viewModel.onPasswordChange("123456")

        assertEquals("123456", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun onTelefonoChange_actualizaTelefono() = runTest {
        val viewModel = SignupViewModel(registerUseCase)

        viewModel.onTelefonoChange("666555444")

        assertEquals("666555444", viewModel.uiState.value.telefono)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun onRoleSelected_actualizaRol() = runTest {
        val viewModel = SignupViewModel(registerUseCase)

        viewModel.onRoleSelected(UserRole.PROTECTORA)

        assertEquals(UserRole.PROTECTORA, viewModel.uiState.value.selectedRole)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun onTogglePasswordVisibility_cambiaValor() = runTest {
        val viewModel = SignupViewModel(registerUseCase)
        val initialValue = viewModel.uiState.value.passwordVisible

        viewModel.onTogglePasswordVisibility()

        assertEquals(!initialValue, viewModel.uiState.value.passwordVisible)
    }

    @Test
    fun onProfileImageSelected_actualizaImagen() = runTest {
        val viewModel = SignupViewModel(registerUseCase)
        val uri = mock<Uri>()

        viewModel.onProfileImageSelected(uri)

        assertEquals(uri, viewModel.uiState.value.selectedImageUri)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun onVerificationDocumentsSelected_agregaDocumentosSinDuplicados() = runTest {
        val viewModel = SignupViewModel(registerUseCase)
        val uri1 = mock<Uri>()
        val uri2 = mock<Uri>()

        viewModel.onVerificationDocumentsSelected(listOf(uri1, uri2, uri1))

        assertEquals(2, viewModel.uiState.value.verificationDocuments.size)
        assertTrue(viewModel.uiState.value.verificationDocuments.contains(uri1))
        assertTrue(viewModel.uiState.value.verificationDocuments.contains(uri2))
    }

    @Test
    fun removeVerificationDocument_eliminaDocumento() = runTest {
        val viewModel = SignupViewModel(registerUseCase)
        val uri1 = mock<Uri>()
        val uri2 = mock<Uri>()

        viewModel.onVerificationDocumentsSelected(listOf(uri1, uri2))
        viewModel.removeVerificationDocument(uri1)

        assertEquals(1, viewModel.uiState.value.verificationDocuments.size)
        assertFalse(viewModel.uiState.value.verificationDocuments.contains(uri1))
        assertTrue(viewModel.uiState.value.verificationDocuments.contains(uri2))
    }

    @Test
    fun onRegisterClick_siFaltanCamposObligatorios_muestraError() = runTest {
        val viewModel = SignupViewModel(registerUseCase)

        viewModel.onRegisterClick()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(
            "Completa nombre, apellido, nombre de usuario, email y contraseña.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun onRegisterClick_siProtectoraSinNombre_muestraError() = runTest {
        val viewModel = SignupViewModel(registerUseCase)

        viewModel.onNombreChange("Ana")
        viewModel.onApellidoChange("López")
        viewModel.onUsernameChange("ana123")
        viewModel.onEmailChange("test@email.com")
        viewModel.onPasswordChange("123456")
        viewModel.onRoleSelected(UserRole.PROTECTORA)

        viewModel.onRegisterClick()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(
            "Completa el nombre de la protectora.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun onRegisterClick_siProtectoraSinUbicacion_muestraError() = runTest {
        val viewModel = SignupViewModel(registerUseCase)

        viewModel.onNombreChange("Ana")
        viewModel.onApellidoChange("López")
        viewModel.onUsernameChange("ana123")
        viewModel.onEmailChange("test@email.com")
        viewModel.onPasswordChange("123456")
        viewModel.onRoleSelected(UserRole.PROTECTORA)
        viewModel.onNombreProtectoraChange("Refugio Huellas")

        viewModel.onRegisterClick()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(
            "Completa la ubicación de la protectora.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun onRegisterClick_siProtectoraSinDocumentos_muestraError() = runTest {
        val viewModel = SignupViewModel(registerUseCase)

        viewModel.onNombreChange("Ana")
        viewModel.onApellidoChange("López")
        viewModel.onUsernameChange("ana123")
        viewModel.onEmailChange("test@email.com")
        viewModel.onPasswordChange("123456")
        viewModel.onRoleSelected(UserRole.PROTECTORA)
        viewModel.onNombreProtectoraChange("Refugio Huellas")
        viewModel.onResolvedLocationData(
            direccion = "Calle Mayor 1",
            provincia = "Madrid",
            ciudad = "Madrid",
            lat = 40.0,
            lng = -3.0
        )

        viewModel.onRegisterClick()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(
            "Adjunta al menos un documento de verificación de la protectora.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun onRegisterClick_siRegistroCorrecto_actualizaExito() = runTest {
        val viewModel = SignupViewModel(registerUseCase)

        viewModel.onNombreChange("Ana")
        viewModel.onApellidoChange("López")
        viewModel.onUsernameChange("ana123")
        viewModel.onEmailChange("test@email.com")
        viewModel.onPasswordChange("123456")

        whenever(
            registerUseCase(
                user = any(),
                password = any(),
                profileImageUri = any(),
                verificationDocuments = any()
            )
        ).thenReturn(Result.success(Unit))

        viewModel.onRegisterClick()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.isSuccess)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun onRegisterClick_siRegistroFalla_muestraError() = runTest {
        val viewModel = SignupViewModel(registerUseCase)

        viewModel.onNombreChange("Ana")
        viewModel.onApellidoChange("López")
        viewModel.onUsernameChange("ana123")
        viewModel.onEmailChange("test@email.com")
        viewModel.onPasswordChange("123456")

        whenever(
            registerUseCase(
                user = any(),
                password = any(),
                profileImageUri = anyOrNull(),
                verificationDocuments = any()
            )
        ).thenReturn(Result.failure(Exception("Error al crear la cuenta")))

        viewModel.onRegisterClick()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Error al crear la cuenta", state.errorMessage)
    }
}