package com.example.petscue.ui.auth.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun SignupScreen(
    onSignupSuccess:    () -> Unit = {},
    onNavigateToLogin:  () -> Unit = {},
    vm: SignupViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onSignupSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(
                listOf(Color(0xFF64B5F6), Color(0xFF1565C0))))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear Cuenta", color = Color.White,
            fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
        Spacer(Modifier.height(4.dp))
        Text("¡Únete a Petscue!",
            color = Color.White.copy(alpha = 0.85f))
        Spacer(Modifier.height(32.dp))

        // Nombre + Apellido
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value         = state.nombre,
                onValueChange = { vm.onNombreChange(it) },
                label         = { Text("Nombre *", color = Color.White) },
                modifier      = Modifier.weight(1f),
                colors        = fieldColors()
            )
            OutlinedTextField(
                value         = state.apellido,
                onValueChange = { vm.onApellidoChange(it) },
                label         = { Text("Apellido *", color = Color.White) },
                modifier      = Modifier.weight(1f),
                colors        = fieldColors()
            )
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value         = state.email,
            onValueChange = { vm.onEmailChange(it) },
            label         = { Text("Email *", color = Color.White) },
            modifier      = Modifier.fillMaxWidth(),
            colors        = fieldColors()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value         = state.password,
            onValueChange = { vm.onPasswordChange(it) },
            label         = { Text("Contraseña *", color = Color.White) },
            modifier      = Modifier.fillMaxWidth(),
            visualTransformation = if (state.passwordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { vm.onTogglePasswordVisibility() }) {
                    Icon(
                        imageVector = if (state.passwordVisible)
                            Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            },
            colors = fieldColors()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value         = state.telefono,
            onValueChange = { vm.onTelefonoChange(it) },
            label         = { Text("Teléfono", color = Color.White) },
            modifier      = Modifier.fillMaxWidth(),
            colors        = fieldColors()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value         = state.direccion,
            onValueChange = { vm.onDireccionChange(it) },
            label         = { Text("Dirección", color = Color.White) },
            modifier      = Modifier.fillMaxWidth(),
            colors        = fieldColors()
        )
        Spacer(Modifier.height(20.dp))

        state.errorMessage?.let {
            Text(it, color = Color(0xFFFFCDD2), fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
        }

        Text("* campo obligatorio",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))

        Button(
            onClick  = { vm.onRegisterClick() },
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
                Text("Crear Cuenta", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onNavigateToLogin) {
            Text("¿Ya tienes cuenta? Inicia sesión", color = Color.White)
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = Color.White,
    unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
    focusedTextColor     = Color.White,
    unfocusedTextColor   = Color.White,
    cursorColor          = Color.White
)