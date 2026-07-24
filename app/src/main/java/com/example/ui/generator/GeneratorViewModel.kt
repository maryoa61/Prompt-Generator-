package com.example.ui.generator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.db.PromptEntity
import com.example.data.repository.PromptRepository
import com.example.domain.model.PromptStyle
import com.example.domain.usecase.GenerateAiPromptUseCase
import com.example.domain.usecase.GeneratePromptUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GeneratorUiState(
    val inputText: String = "Jetpack Compose, MVVM, Room Database, Material 3, Clean Architecture, Kotlin Flow",
    val selectedStyle: PromptStyle = PromptStyle.SOFTWARE_DEV,
    val generatedRole: String = "SENIOR SOFTWARE ARCHITECT & PRINCIPAL ENGINEER",
    val generatedContext: String = "Developing robust, scalable, and maintainable software systems adhering to clean architecture.",
    val generatedTask: String = "Create a production-ready Jetpack Compose screen implementing MVVM with Room persistence and Kotlin Flow.",
    val generatedConstraints: String = "Follow strict typing, clean architecture, handle edge cases gracefully, write clean idiomatic code.",
    val generatedOutputFormat: String = "1. Technical Overview & Architecture\n2. Implementation Snippet\n3. Trade-offs",
    val fullGeneratedPrompt: String = "",
    val isGenerating: Boolean = false,
    val isSaved: Boolean = false,
    val userMessage: String? = null
)

@HiltViewModel
class GeneratorViewModel @Inject constructor(
    private val repository: PromptRepository,
    private val generatePromptUseCase: GeneratePromptUseCase,
    private val generateAiPromptUseCase: GenerateAiPromptUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GeneratorUiState())
    val uiState: StateFlow<GeneratorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = repository.userPreferences.first()
            val initialStyle = PromptStyle.fromName(prefs.defaultStyleName)
            _uiState.update { it.copy(selectedStyle = initialStyle) }
            generatePrompt()
        }
    }

    fun onInputChanged(newInput: String) {
        _uiState.update { it.copy(inputText = newInput, isSaved = false) }
    }

    fun onStyleSelected(style: PromptStyle) {
        _uiState.update { it.copy(selectedStyle = style, isSaved = false) }
        generatePrompt()
    }

    fun generatePrompt() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            val state = _uiState.value

            // Prefer the Gemini-backed generator (understands any input
            // language, e.g. Persian, and always writes the prompt in
            // English). Fall back to the local offline template if no API
            // key is configured or the network call fails for any reason.
            val (template, fallbackMessage) = if (generateAiPromptUseCase.hasApiKey()) {
                try {
                    generateAiPromptUseCase(
                        inputText = state.inputText,
                        style = state.selectedStyle
                    ) to null
                } catch (e: Exception) {
                    generatePromptUseCase(
                        inputText = state.inputText,
                        style = state.selectedStyle
                    ) to "AI generation failed (${e.message ?: "network error"}), used offline template instead."
                }
            } else {
                generatePromptUseCase(
                    inputText = state.inputText,
                    style = state.selectedStyle
                ) to "No Gemini API key configured - used offline template. Add GEMINI_API_KEY to .env to enable AI generation."
            }

            val fullPrompt = template.toFormattedString()

            _uiState.update {
                it.copy(
                    generatedRole = template.role.uppercase(),
                    generatedContext = template.context,
                    generatedTask = template.task,
                    generatedConstraints = template.constraints,
                    generatedOutputFormat = template.outputFormat,
                    fullGeneratedPrompt = fullPrompt,
                    isGenerating = false,
                    isSaved = false,
                    userMessage = fallbackMessage ?: it.userMessage
                )
            }

            val prefs = repository.userPreferences.first()
            if (prefs.autoCopyOnGenerate && fullPrompt.isNotBlank()) {
                savePromptToHistory()
            }
        }
    }

    fun savePromptToHistory() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.fullGeneratedPrompt.isBlank()) return@launch

            val title = if (state.inputText.isNotBlank()) {
                state.inputText.take(40).trim()
            } else {
                "${state.selectedStyle.displayName} Prompt"
            }

            val entity = PromptEntity(
                title = title,
                styleName = state.selectedStyle.name,
                inputKeywords = state.inputText,
                role = state.generatedRole,
                context = state.generatedContext,
                task = state.generatedTask,
                constraints = state.generatedConstraints,
                outputFormat = state.generatedOutputFormat,
                fullGeneratedPrompt = state.fullGeneratedPrompt
            )

            repository.savePrompt(entity)
            _uiState.update { it.copy(isSaved = true, userMessage = "Saved to History!") }
        }
    }

    fun copyToClipboard(context: Context) {
        val fullPrompt = _uiState.value.fullGeneratedPrompt
        if (fullPrompt.isBlank()) return

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Generated AI Prompt", fullPrompt)
        clipboard.setPrimaryClip(clip)

        _uiState.update { it.copy(userMessage = "Prompt copied to clipboard!") }
        savePromptToHistory()
    }

    fun sharePrompt(context: Context) {
        val fullPrompt = _uiState.value.fullGeneratedPrompt
        if (fullPrompt.isBlank()) return

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, fullPrompt)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share AI Prompt")
        context.startActivity(shareIntent)
    }

    fun loadSamplePreset(sampleText: String, style: PromptStyle) {
        _uiState.update {
            it.copy(
                inputText = sampleText,
                selectedStyle = style
            )
        }
        generatePrompt()
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
