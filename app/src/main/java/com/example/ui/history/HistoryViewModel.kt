package com.example.ui.history

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.db.PromptEntity
import com.example.data.repository.PromptRepository
import com.example.domain.usecase.ExportFormat
import com.example.domain.usecase.ExportPromptHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val onlyFavorites: Boolean = false,
    val userMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: PromptRepository,
    private val exportPromptHistoryUseCase: ExportPromptHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    val prompts: StateFlow<List<PromptEntity>> = _uiState
        .flatMapLatest { state ->
            if (state.onlyFavorites) {
                repository.favoritePrompts
            } else {
                repository.searchPrompts(state.searchQuery)
            }
        }
        .combine(_uiState) { list, state ->
            var filtered = list
            if (state.selectedCategory != null) {
                filtered = filtered.filter { it.styleName.equals(state.selectedCategory, ignoreCase = true) }
            }
            filtered
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onCategoryFilterSelected(category: String?) {
        _uiState.update { it.copy(selectedCategory = if (it.selectedCategory == category) null else category) }
    }

    fun toggleOnlyFavorites() {
        _uiState.update { it.copy(onlyFavorites = !it.onlyFavorites) }
    }

    fun toggleFavorite(id: Long, currentFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(id, currentFavorite)
        }
    }

    fun deletePrompt(id: Long) {
        viewModelScope.launch {
            repository.deletePrompt(id)
            _uiState.update { it.copy(userMessage = "Prompt deleted") }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _uiState.update { it.copy(userMessage = "History cleared") }
        }
    }

    fun copyPromptToClipboard(context: Context, fullText: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Generated Prompt", fullText)
        clipboard.setPrimaryClip(clip)
        _uiState.update { it.copy(userMessage = "Copied to clipboard!") }
    }

    fun sharePrompt(context: Context, fullText: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, fullText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Prompt")
        context.startActivity(shareIntent)
    }

    fun exportHistory(context: Context, format: ExportFormat, uri: Uri) {
        viewModelScope.launch {
            val list = repository.allPrompts.first()
            if (list.isEmpty()) {
                _uiState.update { it.copy(userMessage = "No prompts to export") }
                return@launch
            }
            val success = when (format) {
                ExportFormat.JSON -> exportPromptHistoryUseCase.exportToJson(context, list, uri)
                ExportFormat.TXT -> exportPromptHistoryUseCase.exportToText(context, list, uri)
            }
            if (success) {
                _uiState.update { it.copy(userMessage = "Exported ${list.size} prompts successfully!") }
            } else {
                _uiState.update { it.copy(userMessage = "Export failed. Please try again.") }
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
