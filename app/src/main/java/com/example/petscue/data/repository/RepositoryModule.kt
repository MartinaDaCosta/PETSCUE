package com.example.petscue.data.repository

import com.example.petscue.data.repository.PetRepository
import com.example.petscue.data.repository.PetRepositoryImpl
import com.example.petscue.data.repository.PostRepository
import com.example.petscue.data.repository.PostRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPetRepository(
        impl: PetRepositoryImpl
    ): PetRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        impl: PostRepositoryImpl
    ): PostRepository
}