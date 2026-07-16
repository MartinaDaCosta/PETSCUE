package com.example.petscue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.petscue.data.notifications.createNotificationChannel
import com.example.petscue.ui.navigation.PetscueNavHost
import com.example.petscue.ui.theme.PetscueTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel(this)
        setContent {
            PetscueTheme {
                val navController = rememberNavController()
                PetscueNavHost(navController = navController)
            }
        }
    }
}