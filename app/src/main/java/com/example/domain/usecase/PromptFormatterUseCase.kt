package com.example.domain.usecase

import com.example.domain.model.GeneratedPrompt
import com.example.domain.model.PromptTemplateProvider
import com.example.domain.model.UserPromptInput
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptFormatterUseCase @Inject constructor() {

    fun generate(input: UserPromptInput): GeneratedPrompt {
        val cleanedText = normalizeText(input.rawText)
        val extractedKeywords = resolveKeywords(cleanedText, input.keywords)

        val template = PromptTemplateProvider.getTemplateForStyle(input.style)

        val contextString = if (cleanedText.isNotBlank()) {
            cleanedText
        } else {
            "General context for ${input.style.displayName} execution."
        }

        val taskString = if (cleanedText.isNotBlank()) {
            val firstSentence = cleanedText.split(Regex("[.\\n]")).firstOrNull { it.isNotBlank() }?.trim()
            firstSentence ?: cleanedText
        } else {
            "Execute tasks according to the specified ${input.style.displayName} guidelines."
        }

        val keywordsFormatted = if (extractedKeywords.isNotEmpty()) {
            extractedKeywords.joinToString(separator = "\n") { "• $it" }
        } else {
            "• Standard ${input.style.displayName} Domain Best Practices"
        }

        val formattedPrompt = template
            .replace("{{CONTEXT}}", contextString)
            .replace("{{TASK}}", taskString)
            .replace("{{KEYWORDS}}", keywordsFormatted)

        return GeneratedPrompt(
            formattedPrompt = formattedPrompt.trim(),
            timestamp = System.currentTimeMillis(),
            style = input.style
        )
    }

    private fun normalizeText(rawText: String): String {
        if (rawText.isBlank()) return ""
        return rawText
            .trim()
            .replace(Regex("\\s+"), " ")
    }

    private fun resolveKeywords(cleanedText: String, explicitKeywords: List<String>): List<String> {
        val combined = mutableListOf<String>()

        // Add explicit keywords
        explicitKeywords.forEach { kw ->
            val trimmed = kw.trim()
            if (trimmed.isNotBlank()) {
                combined.add(trimmed)
            }
        }

        // Parse comma-separated keywords if embedded in input
        if (combined.isEmpty() && cleanedText.contains(",")) {
            val parts = cleanedText.split(",")
            if (parts.size >= 2) {
                parts.map { it.trim() }.filter { it.isNotBlank() && it.length <= 40 }.forEach {
                    combined.add(it)
                }
            }
        }

        // Deduplicate keywords while preserving case of first occurrence
        val uniqueKeywords = mutableListOf<String>()
        val seenLower = mutableSetOf<String>()

        for (kw in combined) {
            val lower = kw.lowercase()
            if (!seenLower.contains(lower)) {
                seenLower.add(lower)
                uniqueKeywords.add(kw)
            }
        }

        return uniqueKeywords
    }
}
