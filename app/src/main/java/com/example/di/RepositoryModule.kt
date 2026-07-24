package com.example.di

import com.example.data.repository.AiPromptRepository
import com.example.data.repository.AiPromptRepositoryImpl
import com.example.data.repository.PromptRepository
import com.example.data.repository.PromptRepositoryImpl
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
    abstract fun bindPromptRepository(
        impl: PromptRepositoryImpl
    ): PromptRepository

    @Binds
    @Singleton
    abstract fun bindAiPromptRepository(
        impl: AiPromptRepositoryImpl
    ): AiPromptRepository
}
