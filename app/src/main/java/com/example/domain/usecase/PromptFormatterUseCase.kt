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

    /**
     * Public helper used by [GeneratePromptUseCase] to build a clean list of
     * keywords from the raw user input without generating the full nested
     * template (see [generate]).
     */
    fun extractKeywords(rawText: String, explicitKeywords: List<String> = emptyList()): List<String> {
        val cleaned = normalizeText(rawText)
        return resolveKeywords(cleaned, explicitKeywords)
    }

    /**
     * Public helper exposing the same text-cleaning logic used internally,
     * useful for callers that only need normalized text (e.g. building a
     * task/context string) without the full template.
     */
    fun cleanText(rawText: String): String = normalizeText(rawText)

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

        // Parse comma-separated keywords if embedded in input.
        // IMPORTANT: only do this when the text genuinely looks like a short
        // comma-separated list (e.g. "Kotlin, Android, VPN"), NOT when it's a
        // full sentence/paragraph that merely happens to contain commas -
        // otherwise a clause like "...set the MTU to `1400`, and set the DNS
        // to..." gets sliced out and duplicated as a standalone "keyword"
        // even though it's just part of one larger instruction.
        val looksLikeSentenceOrParagraph = cleanedText.contains(Regex("[.!?]")) ||
            cleanedText.length > 150
        if (combined.isEmpty() && cleanedText.contains(",") && !looksLikeSentenceOrParagraph) {
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
