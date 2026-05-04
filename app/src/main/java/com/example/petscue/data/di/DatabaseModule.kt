package com.example.petscue.data.di

import android.content.Context
import androidx.room.Room
import com.example.petscue.data.repository.PetRepository
import com.example.petscue.data.repository.PetRepositoryImpl
import com.example.petscue.data.repository.PostRepository
import com.example.petscue.data.repository.PostRepositoryImpl
import com.example.petscue.data.sources.local.AppDatabase
import com.example.petscue.data.sources.local.PetDao
import com.example.petscue.data.sources.local.PostDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "petscue_db"
    ).build()

    @Provides
    fun providePetDao(db: AppDatabase): PetDao = db.petDao()

    @Provides
    fun providePostDao(db: AppDatabase): PostDao = db.postDao()

    @Provides
    fun providePetRepository(impl: PetRepositoryImpl): PetRepository = impl

    @Provides
    fun providePostRepository(impl: PostRepositoryImpl): PostRepository = impl
}