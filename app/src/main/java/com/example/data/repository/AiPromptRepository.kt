package com.example.data.repository

import com.example.data.remote.AiPromptDto

interface AiPromptRepository {
    /**
     * Whether a Gemini API key is configured at all (via .env -> BuildConfig).
     * The UI/use case checks this first so it can fall back to the local
     * template generator instead of making a network call that will 401.
     */
    fun hasApiKey(): Boolean

    /**
     * Sends [metaPrompt] to Gemini and parses the structured JSON reply into
     * an [AiPromptDto]. Throws on network/parse failure - callers decide how
     * to fall back (see GenerateAiPromptUseCase).
     */
    suspend fun generateStructuredPrompt(metaPrompt: String): AiPromptDto
}
