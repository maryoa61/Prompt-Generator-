package com.example.data.repository

import com.example.data.local.datastore.UserPreferences
import com.example.data.local.db.PromptEntity
import kotlinx.coroutines.flow.Flow

interface PromptRepository {
    val allPrompts: Flow<List<PromptEntity>>
    val favoritePrompts: Flow<List<PromptEntity>>
    val userPreferences: Flow<UserPreferences>

    fun searchPrompts(query: String): Flow<List<PromptEntity>>
    suspend fun savePrompt(prompt: PromptEntity): Long
    suspend fun saveToHistory(prompt: PromptEntity): Long
    fun observeHistory(): Flow<List<PromptEntity>>
    fun observeSettings(): Flow<UserPreferences>
    suspend fun updateSettings(defaultStyleName: String, autoCopy: Boolean)
    suspend fun deletePrompt(id: Long)
    suspend fun deleteById(id: Long)
    suspend fun toggleFavorite(id: Long, currentFavorite: Boolean)
    suspend fun clearHistory()
    suspend fun clearAll()
    suspend fun updateDefaultStyle(styleName: String)
    suspend fun updateAutoCopy(autoCopy: Boolean)
    suspend fun updateThemeMode(themeMode: String)
}
