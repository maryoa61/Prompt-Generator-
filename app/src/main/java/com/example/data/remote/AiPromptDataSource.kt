package com.example.data.remote

import com.example.BuildConfig
import com.example.domain.model.PromptStyle
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of an AI-backed prompt generation call.
 */
sealed interface AiPromptResult {
    data class Success(val text: String) : AiPromptResult
    data class Failure(val reason: String) : AiPromptResult
    /** The user has not configured an NVIDIA_API_KEY in their local .env file. */
    data object NotConfigured : AiPromptResult
}

/**
 * Talks to the NVIDIA NIM chat completions endpoint (build.nvidia.com) to
 * turn raw user keywords into a fully structured prompt for a given
 * [PromptStyle]. Falls back gracefully (via [AiPromptResult]) so callers can
 * always fall back to fully offline/local template generation.
 */
@Singleton
class AiPromptDataSource @Inject constructor(
    private val nvidiaApiService: NvidiaApiService,
    private val moshi: Moshi
) {

    private val apiKey: String get() = BuildConfig.NVIDIA_API_KEY
    private val model: String
        get() = BuildConfig.NVIDIA_MODEL.ifBlank { DEFAULT_MODEL }

    suspend fun generateStructuredPrompt(
        style: PromptStyle,
        rawInput: String
    ): AiPromptResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext AiPromptResult.NotConfigured
        }

        val systemPrompt = buildSystemPrompt(style)
        val userPrompt = buildUserPrompt(style, rawInput)

        val request = NvidiaChatRequest(
            model = model,
            messages = listOf(
                NvidiaChatMessage(role = "system", content = systemPrompt),
                NvidiaChatMessage(role = "user", content = userPrompt)
            ),
            maxTokens = 900,
            temperature = 0.5
        )

        try {
            val response = nvidiaApiService.getChatCompletion(
                bearerToken = "Bearer $apiKey",
                request = request
            )

            if (!response.isSuccessful) {
                val message = parseErrorMessage(response.errorBody()?.string())
                return@withContext AiPromptResult.Failure(
                    "NVIDIA API error (${response.code()}): $message"
                )
            }

            val content = response.body()?.choices?.firstOrNull()?.message?.content
            if (content.isNullOrBlank()) {
                AiPromptResult.Failure("Empty response from NVIDIA API")
            } else {
                AiPromptResult.Success(content.trim())
            }
        } catch (e: IOException) {
            AiPromptResult.Failure(e.message ?: "Network error while calling NVIDIA API")
        } catch (e: Exception) {
            AiPromptResult.Failure(e.message ?: "Unexpected error while calling NVIDIA API")
        }
    }

    private fun parseErrorMessage(rawBody: String?): String {
        if (rawBody.isNullOrBlank()) return "Unknown error"
        return try {
            val adapter = moshi.adapter(NvidiaErrorResponse::class.java)
            adapter.fromJson(rawBody)?.error?.message ?: rawBody.take(200)
        } catch (e: Exception) {
            rawBody.take(200)
        }
    }

    private fun buildSystemPrompt(style: PromptStyle): String = """
        You are an expert prompt engineer. Given a short list of keywords or
        requirements from the user, write a single, polished, production-ready
        prompt intended to be pasted into another AI model as a "${style.displayName}"
        request.

        The user's raw input may be written in ANY language (e.g. Persian/Farsi,
        English, or a mix). Understand the full meaning regardless of language,
        but ALWAYS write the final prompt entirely in English, no matter what
        language the input was in.

        Respond with PLAIN TEXT ONLY, structured using exactly these five
        section headers, each on its own line, in this order:

        ROLE:
        CONTEXT:
        TASK:
        CONSTRAINTS:
        OUTPUT FORMAT:

        Rules:
        - Do not use Markdown formatting, code fences, or JSON.
        - Keep each section concise but specific and actionable.
        - Tailor the ROLE to a "${style.displayName}" persona relevant to the user's input.
        - Do not duplicate the same clause or sentence across multiple sections.
        - Preserve important technical details exactly (numbers, identifiers,
          code in backticks, file/class/function names, IP addresses).
        - Do not add any preamble, explanation, or closing remarks outside the five sections.
    """.trimIndent()

    private fun buildUserPrompt(style: PromptStyle, rawInput: String): String {
        val cleaned = rawInput.trim().ifBlank { "General ${style.displayName} task" }
        return "Style: ${style.displayName}\nKeywords / requirements: $cleaned"
    }

    companion object {
        // A solid general-purpose default from the NVIDIA API Catalog.
        // Any model id from https://build.nvidia.com/models can be used instead
        // by setting NVIDIA_MODEL in the local .env file.
        private const val DEFAULT_MODEL = "nvidia/llama-3.3-nemotron-super-49b-v1.5"
    }
}
