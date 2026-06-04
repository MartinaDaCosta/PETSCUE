package com.example.petscue.admin.data.repository

import com.example.petscue.admin.data.model.ProtectoraRequest
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    fun observePendingRequests(): Flow<List<ProtectoraRequest>>
    suspend fun approveRequest(requestId: String): Result<Unit>
    suspend fun rejectRequest(requestId: String, motivo: String): Result<Unit>
}