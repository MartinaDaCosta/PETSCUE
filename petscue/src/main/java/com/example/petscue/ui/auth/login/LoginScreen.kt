package com.example.petscue.ui.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.petscue.data.model.ApprovalStatus
import com.example.petscue.data.model.UserRole
import com.example.petscue.ui.navigation.Routes
import com.example.petscue.ui.theme.AuthCardShape
import com.example.petscue.ui.theme.AuthScreenContainer
import com.example.petscue.ui.theme.PetscueError
import com.example.petscue.ui.theme.PetscueSuccess
import com.example.petscue.ui.theme.authFieldColors
import com.example.petscue.ui.theme.authPrimaryButtonColors
import com.example.petscue.ui.theme.AuthTextFieldShape

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit = {},
    onNavigateToSignup: () -> Unit = {},
    vm: LoginViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    // Redirige según el tipo de usuario y su estado de validación
    LaunchedEffect(state.isSuccess, state.userRole, state.approvalStatus) {
        if (state.isSuccess) {
            val destination = if (
                state.userRole == UserRole.PROTECTORA &&
                state.approvalStatus == ApprovalStatus.PENDING
            ) {
                Routes.PENDING_APPROVAL
            } else {
                Routes.MAIN
            }
            onLoginSuccess(destination)
        }
    }

    // Diálogo para enviar correo de recuperación de contraseña
    if (state.showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { vm.hideForgotPasswordDialog() },
            title = { Text("Recuperar contraseña") },
            text = {
                Column {
                    Text("Te enviaremos un correo para restablecer la contraseña a la dirección indicada.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = vm::onEmailChange,
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = AuthTextFieldShape,
                        colors = authFieldColors()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { vm.onForgotPasswordConfirm() }) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.hideForgotPasswordDialog() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Contenedor base compartido para las pantallas de autenticación
    AuthScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título principal de la pantalla
            Text(
                text = "Iniciar sesión",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Texto de apoyo bajo el título
            Text(
                text = "Bienvenido de vuelta a Petscue",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo de email con colores reutilizados del tema auth
            OutlinedTextField(
                value = state.email,
                onValueChange = vm::onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AuthTextFieldShape,
                colors = authFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo de contraseña con botón para mostrar u ocultar texto
            OutlinedTextField(
                value = state.password,
                onValueChange = vm::onPasswordChange,
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AuthTextFieldShape,
                visualTransformation = if (state.passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = vm::onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (state.passwordVisible) {
                                Icons.Default.Visibility
                            } else {
                                Icons.Default.VisibilityOff
                            },
                            contentDescription = "Mostrar u ocultar contraseña"
                        )
                    }
                },
                colors = authFieldColors()
            )

            // Acceso al flujo de recuperación de contraseña
            TextButton(
                onClick = vm::showForgotPasswordDialog,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "¿Has olvidado tu contraseña?",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Mensaje de error devuelto por el ViewModel
            state.errorMessage?.let {
                Text(
                    text = it,
                    color = PetscueError,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Mensaje de éxito, por ejemplo en recuperación de contraseña
            state.successMessage?.let {
                Text(
                    text = it,
                    color = PetscueSuccess,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón principal de acceso a la aplicación
            Button(
                onClick = vm::onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !state.isLoading,
                colors = authPrimaryButtonColors(),
                shape = AuthCardShape
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "Iniciar sesión",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navegación a la pantalla de registro
            TextButton(onClick = onNavigateToSignup) {
                Text(
                    text = "¿No tienes cuenta? Regístrate",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}