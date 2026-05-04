package com.example.petscue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import com.example.petscue.ui.theme.PETSCUETheme
import com.example.petscue.ui.navigation.PetscueNavHost
import androidx.navigation.compose.rememberNavController

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PETSCUETheme {
                val navController = rememberNavController()
                PetscueNavHost(navController = navController)
            }
        }
    }
}
