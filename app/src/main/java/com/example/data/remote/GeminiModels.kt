package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ---- Request models sent to the Gemini "generateContent" REST endpoint ----

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Double = 0.4,
    // Forces Gemini to reply with raw JSON (no markdown fences), which we can
    // parse directly into AiPromptDto below.
    @Json(name = "responseMimeType") val responseMimeType: String = "application/json"
)

// ---- Response models returned by the Gemini REST endpoint ----

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null
)

/**
 * The structured prompt payload we *ask* Gemini to produce (see the meta-prompt
 * built in [com.example.domain.usecase.GenerateAiPromptUseCase]). Gemini's reply
 * text (a JSON string, because of responseMimeType above) is parsed into this.
 */
@JsonClass(generateAdapter = true)
data class AiPromptDto(
    val role: String,
    val context: String,
    val task: String,
    val constraints: String,
    val outputFormat: String
)
