package com.example.petscue.admin.ui.requests.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.petscue.admin.ui.requests.AdminRequestsViewModel

@Composable
fun AdminRequestDetailScreen(
    vm: AdminRequestsViewModel,
    requestId: String,
    onBack: () -> Unit
) {
    val state by vm.uiState.collectAsState()
    val request = state.requests.find { it.id == requestId }

    if (state.isLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Cargando detalle...")
        }
        return
    }

    if (request == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Solicitud no encontrada")
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onBack) {
                Text("Volver")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        OutlinedButton(onClick = onBack) {
            Text("Volver")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Detalle de solicitud",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Nombre: ${request.nombre}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Email: ${request.email}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Teléfono: ${request.telefono}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Dirección: ${request.direccion}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Descripción: ${request.descripcion}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Comunidad: ${request.comunidad}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Provincia: ${request.provincia}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Ciudad: ${request.ciudad}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Estado: ${request.estado}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Documentos: ${request.documentUrls.joinToString()}")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                vm.approveRequest(request)
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Aprobar")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                vm.rejectRequest(request.id)
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Rechazar")
        }
    }
}