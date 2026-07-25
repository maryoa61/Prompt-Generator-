package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Request/response models for NVIDIA's NIM API (build.nvidia.com), which is
 * fully compatible with the OpenAI Chat Completions schema. Any model
 * available in the NVIDIA API Catalog can be used by changing [model].
 */

@JsonClass(generateAdapter = true)
data class NvidiaChatMessage(
    val role: String, // "system" | "user" | "assistant"
    val content: String
)

@JsonClass(generateAdapter = true)
data class NvidiaChatRequest(
    val model: String,
    val messages: List<NvidiaChatMessage>,
    @Json(name = "max_tokens") val maxTokens: Int = 1024,
    val temperature: Double = 0.6,
    @Json(name = "top_p") val topP: Double = 0.9,
    val stream: Boolean = false
)

@JsonClass(generateAdapter = true)
data class NvidiaChatResponse(
    val id: String? = null,
    val model: String? = null,
    val choices: List<NvidiaChoice> = emptyList()
)

@JsonClass(generateAdapter = true)
data class NvidiaChoice(
    val index: Int? = null,
    val message: NvidiaChatMessage? = null,
    @Json(name = "finish_reason") val finishReason: String? = null
)

/**
 * NVIDIA's error responses generally follow the OpenAI convention of
 * wrapping details inside an "error" object. This is only used to surface
 * a friendlier message when parsing failure bodies; failures still fall
 * back to local generation regardless of whether parsing succeeds.
 */
@JsonClass(generateAdapter = true)
data class NvidiaErrorResponse(
    val error: NvidiaErrorDetail? = null
)

@JsonClass(generateAdapter = true)
data class NvidiaErrorDetail(
    val message: String? = null,
    val type: String? = null,
    val code: String? = null
)
