package com.example.petscue.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun AdminApprovalScreen(
    vm: AdminApprovalViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Solicitudes de protectoras pendientes")

        Spacer(modifier = Modifier.height(16.dp))

        state.errorMessage?.let {
            Text(text = it)
            Spacer(modifier = Modifier.height(8.dp))
        }

        state.successMessage?.let {
            Text(text = it)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.users) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Responsable: ${user.nombre} ${user.apellido}")
                            Text("Email: ${user.email}")
                            Text("Protectora: ${user.nombreProtectora}")
                            Text("Provincia: ${user.provincia}")
                            Text("Ciudad: ${user.ciudad}")
                            Text("Web: ${user.web}")

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { vm.approve(user.uid) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Aceptar")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = { vm.reject(user.uid) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Rechazar")
                            }
                        }
                    }
                }
            }
        }
    }
}