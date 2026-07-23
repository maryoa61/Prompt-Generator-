package com.example.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.datastore.UserPreferences
import com.example.data.repository.PromptRepository
import com.example.domain.usecase.ExportFormat
import com.example.domain.usecase.ExportPromptHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: PromptRepository,
    private val exportPromptHistoryUseCase: ExportPromptHistoryUseCase
) : ViewModel() {

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun onDefaultStyleChanged(styleName: String) {
        viewModelScope.launch {
            repository.updateDefaultStyle(styleName)
        }
    }

    fun onAutoCopyChanged(autoCopy: Boolean) {
        viewModelScope.launch {
            repository.updateAutoCopy(autoCopy)
        }
    }

    fun onThemeModeChanged(themeMode: String) {
        viewModelScope.launch {
            repository.updateThemeMode(themeMode)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _userMessage.value = "Prompt history cleared"
        }
    }

    fun exportHistory(context: Context, format: ExportFormat, uri: Uri) {
        viewModelScope.launch {
            val list = repository.allPrompts.first()
            if (list.isEmpty()) {
                _userMessage.value = "No prompts to export"
                return@launch
            }
            val success = when (format) {
                ExportFormat.JSON -> exportPromptHistoryUseCase.exportToJson(context, list, uri)
                ExportFormat.TXT -> exportPromptHistoryUseCase.exportToText(context, list, uri)
            }
            if (success) {
                _userMessage.value = "Exported ${list.size} prompts successfully!"
            } else {
                _userMessage.value = "Export failed. Please try again."
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
