package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.db.AppDatabase
import com.example.data.local.db.PromptDao
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

    // PromptFormatterUseCase, GeneratePromptUseCase, and AiPromptDataSource
    // all declare their own @Inject constructor, so Hilt provides them
    // automatically - no explicit @Provides needed (and adding one here
    // would create a duplicate-binding compile error).
}
