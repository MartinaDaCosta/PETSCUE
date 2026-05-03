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
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onLoginSuccess:     () -> Unit = {},
    onNavigateToSignup: () -> Unit = {},
    vm: LoginViewModel  = viewModel()
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onLoginSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(
                listOf(Color(0xFF64B5F6), Color(0xFF1565C0))))
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Iniciar Sesión", color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp)
        Spacer(Modifier.height(8.dp))
        Text("Bienvenido de vuelta a Petscue",
            color = Color.White.copy(alpha = 0.85f))
        Spacer(Modifier.height(32.dp))

        // Email
        OutlinedTextField(
            value         = state.email,
            onValueChange = { vm.onEmailChange(it) },
            label         = { Text("Email", color = Color.White) },
            modifier      = Modifier.fillMaxWidth(),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                focusedTextColor     = Color.White,
                unfocusedTextColor   = Color.White
            )
        )
        Spacer(Modifier.height(12.dp))

        // Password
        OutlinedTextField(
            value         = state.password,
            onValueChange = { vm.onPasswordChange(it) },
            label         = { Text("Contraseña", color = Color.White) },
            modifier      = Modifier.fillMaxWidth(),
            visualTransformation = if (state.passwordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon  = {
                IconButton(onClick = { vm.onTogglePasswordVisibility() }) {
                    Icon(
                        imageVector = if (state.passwordVisible)
                            Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                focusedTextColor     = Color.White,
                unfocusedTextColor   = Color.White
            )
        )
        Spacer(Modifier.height(20.dp))

        // Error
        state.errorMessage?.let {
            Text(it, color = Color(0xFFFFCDD2), fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
        }

        // Botón login
        Button(
            onClick  = { vm.onLoginClick() },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            enabled  = !state.isLoading,
            colors   = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor   = Color(0xFF1565C0)),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (state.isLoading)
                CircularProgressIndicator(Modifier.size(20.dp), color = Color(0xFF1565C0))
            else
                Text("Iniciar Sesión", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onNavigateToSignup) {
            Text("¿No tienes cuenta? Regístrate", color = Color.White)
        }
    }
}