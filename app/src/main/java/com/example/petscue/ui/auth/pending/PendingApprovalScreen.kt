package com.example.petscue.ui.auth.pending

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun PendingApprovalScreen(
    onLogout: () -> Unit = {},
    vm: PendingApprovalViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            vm.onFileSelected(
                uri = it,
                fileName = it.lastPathSegment ?: "documento"
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF64B5F6),
                        Color(0xFF1565C0)
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Cuenta pendiente de validación",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = "Tu cuenta de protectora ha sido creada, pero aún no ha sido validada por el administrador. Sube la documentación necesaria para completar la revisión.",
            color = Color.White.copy(alpha = 0.9f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Documentación recomendada",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "- CIF o documento identificativo\n- Documento acreditativo de la protectora\n- Información adicional de contacto",
            color = Color.White.copy(alpha = 0.9f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                launcher.launch(
                    arrayOf("application/pdf", "image/*")
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF1565C0)
            )
        ) {
            Text("Seleccionar documento")
        }

        if (state.selectedFileName.isNotBlank()) {
            Text(
                text = "Archivo seleccionado: ${state.selectedFileName}",
                color = Color.White
            )
        }

        OutlinedTextField(
            value = state.notes,
            onValueChange = vm::onNotesChange,
            label = { Text("Notas o información adicional", color = Color.White) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            colors = pendingFieldColors()
        )

        state.infoMessage?.let {
            Text(
                text = it,
                color = Color(0xFFC8E6C9),
                modifier = Modifier.fillMaxWidth()
            )
        }

        state.errorMessage?.let {
            Text(
                text = it,
                color = Color(0xFFFFCDD2),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(
            onClick = { vm.submitDocuments() },
            enabled = !state.isUploading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF1565C0)
            )
        ) {
            if (state.isUploading) {
                CircularProgressIndicator(color = Color(0xFF1565C0))
            } else {
                Text("Enviar documentación")
            }
        }

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Cerrar sesión",
                color = Color.White
            )
        }
    }
}

@Composable
private fun pendingFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color.White,
    unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color.White,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = Color.White.copy(alpha = 0.85f),
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)