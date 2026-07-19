package com.example.petscue.ui.auth.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Política de privacidad",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Información básica sobre protección de datos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Responsable del tratamiento: Petscue."
            )

            Text(
                text = "Finalidad del tratamiento: gestionar el registro de usuarios, permitir el acceso a la aplicación, administrar el perfil, facilitar la comunicación entre usuarios y prestar las funcionalidades relacionadas con mascotas, avisos, publicaciones y mensajería."
            )

            Text(
                text = "Conservación de los datos: los datos se conservarán mientras la cuenta permanezca activa y, posteriormente, durante el plazo necesario para cumplir obligaciones legales o atender posibles responsabilidades derivadas del servicio."
            )

            Text(
                text = "Legitimación: el tratamiento de los datos es necesario para la ejecución del servicio solicitado por el usuario y, cuando corresponda, sobre la base del consentimiento prestado."
            )

            Text(
                text = "Derechos: puedes ejercer tus derechos de acceso, rectificación, supresión, limitación del tratamiento y portabilidad dirigiéndote al responsable del tratamiento."
            )

            Text(
                text = "Asimismo, podrás solicitar información adicional sobre el uso de tus datos y presentar, en su caso, una reclamación ante la autoridad de control competente."
            )
        }
    }
}