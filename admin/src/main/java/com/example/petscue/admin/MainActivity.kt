package com.example.petscue.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.petscue.admin.ui.navigation.AdminNavHost
import com.example.petscue.admin.ui.theme.PETSCUETheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PETSCUETheme {
                AdminNavHost()
                }
        }
    }
}

