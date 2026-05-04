package com.example.petscue.data.sources.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.petscue.data.model.Pet
import com.example.petscue.data.model.Post

@Database(
    entities = [Pet::class, Post::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun postDao(): PostDao
}