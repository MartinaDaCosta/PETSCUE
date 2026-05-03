package com.example.petscue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.petscue.ui.navigation.PetscueNavHost
import com.example.petscue.ui.theme.PETSCUETheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PETSCUETheme {
                PetscueNavHost(navController = rememberNavController())
            }
        }
    }
}