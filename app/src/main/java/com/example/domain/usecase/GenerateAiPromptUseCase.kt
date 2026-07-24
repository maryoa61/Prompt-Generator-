package com.example.domain.usecase

import com.example.data.repository.AiPromptRepository
import com.example.domain.model.PromptStyle
import com.example.domain.model.PromptTemplate
import javax.inject.Inject
import javax.inject.Singleton

sealed interface AiPromptResult {
    data class Success(val template: PromptTemplate) : AiPromptResult
    data class Fallback(val template: PromptTemplate, val reason: String) : AiPromptResult
}

@Singleton
class GenerateAiPromptUseCase @Inject constructor(
    private val aiPromptRepository: AiPromptRepository,
    private val localPromptUseCase: GeneratePromptUseCase
) {
    suspend operator fun invoke(
        inputText: String,
        style: PromptStyle
    ): AiPromptResult {
        val trimmedInput = inputText.trim()
        if (trimmedInput.isBlank()) {
            val fallbackTemplate = localPromptUseCase(
                inputText = "",
                style = style
            )
            return AiPromptResult.Fallback(
                template = fallbackTemplate,
                reason = "Input is empty"
            )
        }

        val aiResult = aiPromptRepository.generatePromptWithGemini(trimmedInput, style)
        return if (aiResult.isSuccess) {
            val template = aiResult.getOrThrow()
            AiPromptResult.Success(template)
        } else {
            val throwable = aiResult.exceptionOrNull()
            val reason = when {
                throwable?.message?.contains("key", ignoreCase = true) == true ->
                    "Gemini API key is not configured"
                throwable?.message?.contains("timeout", ignoreCase = true) == true ->
                    "Network request timed out"
                throwable?.message?.contains("429", ignoreCase = true) == true ->
                    "Rate limit reached"
                else ->
                    throwable?.localizedMessage ?: "Failed to reach Gemini AI"
            }
            val fallbackTemplate = localPromptUseCase(
                inputText = trimmedInput,
                style = style
            )
            AiPromptResult.Fallback(
                template = fallbackTemplate,
                reason = reason
            )
        }
    }
}
