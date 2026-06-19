package com.example.petscue.data.repository

import com.example.petscue.data.model.User

interface ProtectorasRepository {
    suspend fun getProtectoras(): List<User>
}