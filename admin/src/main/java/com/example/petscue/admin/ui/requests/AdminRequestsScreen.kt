package com.example.petscue.admin.ui.requests

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AdminRequestsScreen(
    vm: AdminRequestsViewModel,
    onOpenDetail: (String) -> Unit
) {
    val state by vm.uiState.collectAsState()

    when {
        state.isLoading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Cargando solicitudes...",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        state.errorMessage != null -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Error: ${state.errorMessage}",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        state.requests.isEmpty() -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No hay solicitudes pendientes",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp)
            ) {
                items(state.requests) { request ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { onOpenDetail(request.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = request.nombre.ifBlank { "Sin nombre de protectora" },
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(text = request.email)
                            Text(text = "${request.ciudad}, ${request.provincia}")
                            Text(text = "Estado: ${request.estado}")
                        }
                    }
                }
            }
        }
    }
}