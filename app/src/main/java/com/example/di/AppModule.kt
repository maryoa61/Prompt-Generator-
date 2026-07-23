package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.db.AppDatabase
import com.example.data.local.db.PromptDao
import com.example.domain.usecase.GeneratePromptUseCase
import com.example.domain.usecase.PromptFormatterUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "prompt_generator.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun providePromptDao(appDatabase: AppDatabase): PromptDao {
        return appDatabase.promptDao()
    }

    @Provides
    @Singleton
    fun providePromptFormatterUseCase(): PromptFormatterUseCase {
        return PromptFormatterUseCase()
    }

    @Provides
    @Singleton
    fun provideGeneratePromptUseCase(
        promptFormatterUseCase: PromptFormatterUseCase
    ): GeneratePromptUseCase {
        return GeneratePromptUseCase(promptFormatterUseCase)
    }
}
