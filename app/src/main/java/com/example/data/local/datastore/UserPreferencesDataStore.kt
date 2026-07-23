package com.example.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class UserPreferences(
    val defaultStyleName: String = "SOFTWARE_DEV",
    val autoCopyOnGenerate: Boolean = false,
    val isDarkMode: Boolean? = null
)

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val DEFAULT_STYLE = stringPreferencesKey("default_style")
        val AUTO_COPY = booleanPreferencesKey("auto_copy")
        val DARK_MODE = stringPreferencesKey("dark_mode")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            val style = preferences[PreferencesKeys.DEFAULT_STYLE] ?: "SOFTWARE_DEV"
            val autoCopy = preferences[PreferencesKeys.AUTO_COPY] ?: false
            val darkModeStr = preferences[PreferencesKeys.DARK_MODE] ?: "SYSTEM"
            val isDarkMode = when (darkModeStr) {
                "DARK" -> true
                "LIGHT" -> false
                else -> null
            }
            UserPreferences(
                defaultStyleName = style,
                autoCopyOnGenerate = autoCopy,
                isDarkMode = isDarkMode
            )
        }

    suspend fun updateDefaultStyle(styleName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_STYLE] = styleName
        }
    }

    suspend fun updateAutoCopy(autoCopy: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_COPY] = autoCopy
        }
    }

    suspend fun updateThemeMode(themeMode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = themeMode
        }
    }
}

// Retain alias repository wrapper for backwards compatibility
typealias UserPreferencesRepository = UserPreferencesDataStore
