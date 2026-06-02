package com.example.petscue.ui.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.petscue.data.model.ApprovalStatus
import com.example.petscue.data.model.UserRole
import com.example.petscue.ui.navigation.Routes

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit = {},
    onNavigateToSignup: () -> Unit = {},
    vm: LoginViewModel = hiltViewModel()
){
    val state by vm.uiState.collectAsState()

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
    if (state.showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { vm.hideForgotPasswordDialog() },
            title = { Text("Recuperar contraseña") },
            text = {
                Column {
                    Text(
                        "Te enviaremos un correo para restablecer la contraseña a la dirección indicada."
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { vm.onEmailChange(it) },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF64B5F6), Color(0xFF1565C0))
                )
            )
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Iniciar Sesión",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Bienvenido de vuelta a Petscue",
            color = Color.White.copy(alpha = 0.85f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = { vm.onEmailChange(it) },
            label = { Text("Email", color = Color.White) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { vm.onPasswordChange(it) },
            label = { Text("Contraseña", color = Color.White) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (state.passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(onClick = { vm.onTogglePasswordVisibility() }) {
                    Icon(
                        imageVector = if (state.passwordVisible) {
                            Icons.Default.Visibility
                        } else {
                            Icons.Default.VisibilityOff
                        },
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White
            )
        )

        TextButton(
            onClick = { vm.showForgotPasswordDialog() },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                text = "¿Has olvidado tu contraseña?",
                color = Color.White
            )
        }

        state.errorMessage?.let {
            Text(
                text = it,
                color = Color(0xFFFFCDD2),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        state.successMessage?.let {
            Text(
                text = it,
                color = Color(0xFFC8E6C9),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { vm.onLoginClick() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            enabled = !state.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF1565C0)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color(0xFF1565C0)
                )
            } else {
                Text(
                    text = "Iniciar Sesión",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToSignup) {
            Text(
                text = "¿No tienes cuenta? Regístrate",
                color = Color.White
            )
        }
    }
}