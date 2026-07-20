package com.example.petscue.util

import com.example.petscue.data.model.ApprovalStatus
import com.example.petscue.data.model.User
import com.example.petscue.data.model.UserRole
import com.example.petscue.data.repository.AuthRepository
import com.example.petscue.domain.usecase.LoginUseCase
import com.example.petscue.ui.auth.login.LoginViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: AuthRepository = mock()
    private val loginUseCase: LoginUseCase = mock()

    @Test
    fun onEmailChange_actualizaEmail_yLimpiaMensajes() = runTest {
        val viewModel = LoginViewModel(repository, loginUseCase)

        viewModel.onEmailChange("test@email.com")

        val state = viewModel.uiState.value
        assertEquals("test@email.com", state.email)
        assertNull(state.errorMessage)
        assertNull(state.successMessage)
    }

    @Test
    fun onPasswordChange_actualizaPassword_yLimpiaMensajes() = runTest {
        val viewModel = LoginViewModel(repository, loginUseCase)

        viewModel.onPasswordChange("123456")

        val state = viewModel.uiState.value
        assertEquals("123456", state.password)
        assertNull(state.errorMessage)
        assertNull(state.successMessage)
    }

    @Test
    fun onTogglePasswordVisibility_cambiaVisibilidad() = runTest {
        val viewModel = LoginViewModel(repository, loginUseCase)
        val initialValue = viewModel.uiState.value.passwordVisible

        viewModel.onTogglePasswordVisibility()

        assertEquals(!initialValue, viewModel.uiState.value.passwordVisible)
    }

    @Test
    fun showForgotPasswordDialog_muestraDialogo() = runTest {
        val viewModel = LoginViewModel(repository, loginUseCase)

        viewModel.showForgotPasswordDialog()

        assertTrue(viewModel.uiState.value.showForgotPasswordDialog)
        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun hideForgotPasswordDialog_ocultaDialogo() = runTest {
        val viewModel = LoginViewModel(repository, loginUseCase)

        viewModel.showForgotPasswordDialog()
        viewModel.hideForgotPasswordDialog()

        assertFalse(viewModel.uiState.value.showForgotPasswordDialog)
    }

    @Test
    fun onForgotPasswordConfirm_conEmailVacio_muestraError() = runTest {
        val viewModel = LoginViewModel(repository, loginUseCase)

        viewModel.onForgotPasswordConfirm()

        assertEquals(
            "Introduce tu correo para recuperar la contraseña.",
            viewModel.uiState.value.errorMessage
        )
        assertNull(viewModel.uiState.value.successMessage)
        verify(repository, never()).resetPassword(any())
    }

    @Test
    fun onForgotPasswordConfirm_conExito_muestraMensaje() = runTest {
        val viewModel = LoginViewModel(repository, loginUseCase)
        viewModel.onEmailChange("test@email.com")

        whenever(repository.resetPassword("test@email.com"))
            .thenReturn(Result.success(Unit))

        viewModel.onForgotPasswordConfirm()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.showForgotPasswordDialog)
        assertNull(state.errorMessage)
        assertEquals(
            "Te hemos enviado un correo para restablecer la contraseña.",
            state.successMessage
        )
    }

    @Test
    fun onForgotPasswordConfirm_conFallo_muestraError() = runTest {
        val viewModel = LoginViewModel(repository, loginUseCase)
        viewModel.onEmailChange("test@email.com")

        whenever(repository.resetPassword("test@email.com"))
            .thenReturn(Result.failure(Exception("No se pudo enviar el correo")))

        viewModel.onForgotPasswordConfirm()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("No se pudo enviar el correo", state.errorMessage)
        assertNull(state.successMessage)
    }

    @Test
    fun onLoginClick_siLoginFalla_muestraError() = runTest {
        val viewModel = LoginViewModel(repository, loginUseCase)
        viewModel.onEmailChange("test@email.com")
        viewModel.onPasswordChange("123456")

        whenever(loginUseCase("test@email.com", "123456"))
            .thenReturn(Result.failure(Exception("Credenciales incorrectas")))

        viewModel.onLoginClick()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals("Credenciales incorrectas", state.errorMessage)
        assertNull(state.successMessage)
    }

    @Test
    fun onLoginClick_siEmailNoVerificado_haceLogout_yMuestraError() = runTest {
        val viewModel = LoginViewModel(repository, loginUseCase)
        viewModel.onEmailChange("test@email.com")
        viewModel.onPasswordChange("123456")

        whenever(loginUseCase("test@email.com", "123456"))
            .thenReturn(Result.success(Unit))
        whenever(repository.isEmailVerified())
            .thenReturn(false)

        viewModel.onLoginClick()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals("Verifica tu email antes de continuar.", state.errorMessage)
        assertNull(state.successMessage)
        verify(repository).logout()
    }

    @Test
    fun onLoginClick_siCargaPerfilCorrectamente_actualizaEstado() = runTest {
        val viewModel = LoginViewModel(repository, loginUseCase)
        viewModel.onEmailChange("test@email.com")
        viewModel.onPasswordChange("123456")

        val user = User(
            nombre = "Ana",
            apellido = "López",
            username = "ana",
            email = "test@email.com",
            role = UserRole.USER,
            approvalStatus = ApprovalStatus.APPROVED
        )

        whenever(loginUseCase("test@email.com", "123456"))
            .thenReturn(Result.success(Unit))
        whenever(repository.isEmailVerified())
            .thenReturn(true)
        whenever(repository.getCurrentUserProfile())
            .thenReturn(Result.success(user))

        viewModel.onLoginClick()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isSuccess)
        assertNull(state.errorMessage)
        assertNull(state.successMessage)
        assertEquals(UserRole.USER, state.userRole)
        assertEquals(ApprovalStatus.APPROVED, state.approvalStatus)
    }

    @Test
    fun onLoginClick_siFallaCargaPerfil_haceLogout_yMuestraError() = runTest {
        val viewModel = LoginViewModel(repository, loginUseCase)
        viewModel.onEmailChange("test@email.com")
        viewModel.onPasswordChange("123456")

        whenever(loginUseCase("test@email.com", "123456"))
            .thenReturn(Result.success(Unit))
        whenever(repository.isEmailVerified())
            .thenReturn(true)
        whenever(repository.getCurrentUserProfile())
            .thenReturn(Result.failure(Exception("No se pudo cargar el perfil del usuario")))

        viewModel.onLoginClick()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals("No se pudo cargar el perfil del usuario", state.errorMessage)
        assertNull(state.successMessage)
        verify(repository).logout()
    }
}