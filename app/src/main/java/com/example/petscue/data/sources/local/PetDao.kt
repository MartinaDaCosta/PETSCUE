package com.example.petscue.data.sources.local

import androidx.room.*
import com.example.petscue.data.model.Pet
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pets ORDER BY timestamp DESC")
    fun getAll(): Flow<List<Pet>>

    @Query("SELECT * FROM pets WHERE estado = :estado ORDER BY timestamp DESC")
    fun getByEstado(estado: String): Flow<List<Pet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pet: Pet)

    @Delete
    suspend fun delete(pet: Pet)
}