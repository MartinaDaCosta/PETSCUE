package com.example.petscue.data.repository

import com.example.petscue.data.model.AvisoMapa
import kotlinx.coroutines.flow.Flow

interface AlertRepository {
    fun getAllAlerts(): Flow<List<AvisoMapa>>
    suspend fun getAlertByPetId(petId: String): AvisoMapa?
    suspend fun upsertAlert(alert: AvisoMapa)
    suspend fun deleteAlertByPetId(petId: String)
}