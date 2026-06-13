package com.example.petscue.data.repository

import android.net.Uri
import com.example.petscue.data.model.AvisoMapa
import kotlinx.coroutines.flow.Flow

interface AlertRepository {
    suspend fun insertAlert(alert: AvisoMapa, imageUri: Uri? = null): Result<Unit>
    fun getAllAlerts(): Flow<List<AvisoMapa>>
}