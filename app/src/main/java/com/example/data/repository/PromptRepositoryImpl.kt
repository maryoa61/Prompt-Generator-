package com.example.data.repository

import com.example.data.local.datastore.UserPreferences
import com.example.data.local.datastore.UserPreferencesDataStore
import com.example.data.local.db.PromptDao
import com.example.data.local.db.PromptEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptRepositoryImpl @Inject constructor(
    private val promptDao: PromptDao,
    private val preferencesDataStore: UserPreferencesDataStore
) : PromptRepository {

    override val allPrompts: Flow<List<PromptEntity>> = promptDao.getAllPrompts()
    override val favoritePrompts: Flow<List<PromptEntity>> = promptDao.getFavoritePrompts()
    override val userPreferences: Flow<UserPreferences> = preferencesDataStore.userPreferencesFlow

    override fun searchPrompts(query: String): Flow<List<PromptEntity>> {
        return if (query.isBlank()) {
            promptDao.getAllPrompts()
        } else {
            promptDao.searchPrompts(query)
        }
    }

    override suspend fun savePrompt(prompt: PromptEntity): Long {
        return promptDao.insertPrompt(prompt)
    }

    override suspend fun saveToHistory(prompt: PromptEntity): Long {
        return promptDao.insert(prompt)
    }

    override fun observeHistory(): Flow<List<PromptEntity>> {
        return promptDao.getAll()
    }

    override fun observeSettings(): Flow<UserPreferences> {
        return preferencesDataStore.userPreferencesFlow
    }

    override suspend fun updateSettings(defaultStyleName: String, autoCopy: Boolean) {
        preferencesDataStore.updateDefaultStyle(defaultStyleName)
        preferencesDataStore.updateAutoCopy(autoCopy)
    }

    override suspend fun deletePrompt(id: Long) {
        promptDao.deletePromptById(id)
    }

    override suspend fun deleteById(id: Long) {
        promptDao.deleteById(id)
    }

    override suspend fun toggleFavorite(id: Long, currentFavorite: Boolean) {
        promptDao.setFavorite(id, !currentFavorite)
    }

    override suspend fun clearHistory() {
        promptDao.deleteAllPrompts()
    }

    override suspend fun clearAll() {
        promptDao.clearAll()
    }

    override suspend fun updateDefaultStyle(styleName: String) {
        preferencesDataStore.updateDefaultStyle(styleName)
    }

    override suspend fun updateAutoCopy(autoCopy: Boolean) {
        preferencesDataStore.updateAutoCopy(autoCopy)
    }

    override suspend fun updateThemeMode(themeMode: String) {
        preferencesDataStore.updateThemeMode(themeMode)
    }
}
