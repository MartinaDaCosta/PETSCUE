package com.example.petscue.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthScreen(
    onNavigateToLogin:  () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF64B5F6), Color(0xFF1565C0))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Logo
            Text("🐾", fontSize = 72.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text          = "PETSCUE",
                color         = Color.White,
                fontSize      = 38.sp,
                fontWeight    = FontWeight.ExtraBold,
                letterSpacing = 4.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text     = "Encuentra y ayuda a mascotas perdidas",
                color    = Color.White.copy(alpha = 0.85f),
                fontSize = 15.sp
            )

            Spacer(Modifier.height(56.dp))

            // Registrarse
            Button(
                onClick  = onNavigateToSignup,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor   = Color(0xFF1565C0)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Registrarse gratis", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(12.dp))

            // Iniciar sesión
            OutlinedButton(
                onClick  = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors   = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                shape  = RoundedCornerShape(14.dp)
            ) {
                Text("Iniciar sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}