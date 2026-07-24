package com.example.data.repository

import com.example.BuildConfig
import com.example.data.remote.api.GeminiApi
import com.example.data.remote.model.GeminiContent
import com.example.data.remote.model.GeminiGenerationConfig
import com.example.data.remote.model.GeminiPart
import com.example.data.remote.model.GeminiRequest
import com.example.data.remote.model.StructuredPromptJsonResponse
import com.example.domain.model.PromptStyle
import com.example.domain.model.PromptTemplate
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiPromptRepositoryImpl @Inject constructor(
    private val geminiApi: GeminiApi,
    private val moshi: Moshi
) : AiPromptRepository {

    override suspend fun generatePromptWithGemini(
        rawInput: String,
        style: PromptStyle
    ): Result<PromptTemplate> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyValid(apiKey)) {
            return Result.failure(IllegalStateException("Gemini API key is not configured"))
        }

        val systemInstruction = GeminiContent(
            parts = listOf(
                GeminiPart(
                    text = """
                        You are an expert AI Prompt Engineer specializing in transforming user requests into structured, high-performing system prompts for Large Language Models.

                        INPUT HANDLING:
                        - The user input idea may be written in ANY language (such as Persian/Farsi, Spanish, French, German, Chinese, etc.) or mixed languages.
                        - You MUST analyze and translate the underlying intent into fluent, professional ENGLISH.
                        - PRESERVE EXACT TECHNICAL DETAILS without translation or modification: numbers, identifiers, code snippets, file paths, class names, function names, IP addresses, variables, backticked code (`like this`), and specific technical terms.

                        OUTPUT REQUIREMENTS:
                        Output strictly a JSON object with 5 keys: "role", "context", "task", "constraints", "outputFormat".
                        Each field value MUST be in ENGLISH.

                        FIELD GUIDELINES:
                        1. "role": Define a precise, highly-qualified expert persona suitable for the request and specified style (e.g. "SENIOR SOFTWARE ARCHITECT & KOTLIN SPECIALIST" or "MARKETING STRATEGIST").
                        2. "context": Provide specific, relevant background information, target audience, and scenario details necessary for executing the request. Do NOT repeat or duplicate sentences from the task field.
                        3. "task": State the exact, actionable objective the AI must accomplish. Make it clear and direct.
                        4. "constraints": List bulleted rules, boundaries, style rules, edge cases to handle, and negative constraints. Tailor them to the specified style.
                        5. "outputFormat": Specify the structural layout, section breakdown, or exact format the AI must use for its final response (e.g., "1. Technical Overview\n2. Implementation Snippets\n3. Trade-offs").

                        Output ONLY valid JSON matching this schema.
                    """.trimIndent()
                )
            )
        )

        val userPromptText = """
            User Input Idea / Request:
            ""${'"'}
            ${rawInput.trim()}
            ""${'"'}

            Selected Prompt Style / Domain:
            ${style.displayName} (${style.defaultRole})
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = userPromptText))
                )
            ),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            ),
            systemInstruction = systemInstruction
        )

        return try {
            val response = geminiApi.generateContent(apiKey, request)
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: ""
                val code = response.code()
                return Result.failure(
                    RuntimeException("Gemini API error ($code): ${errorBody.take(150)}")
                )
            }

            val body = response.body()
            val candidateText = body?.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()
                ?.text

            if (candidateText.isNullOrBlank()) {
                return Result.failure(IllegalStateException("Received empty response from Gemini AI"))
            }

            val cleanedJson = candidateText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val adapter = moshi.adapter(StructuredPromptJsonResponse::class.java)
            val structuredResponse = adapter.fromJson(cleanedJson)
                ?: return Result.failure(IllegalStateException("Failed to parse Gemini response JSON"))

            val template = PromptTemplate(
                style = style,
                role = structuredResponse.role.trim(),
                context = structuredResponse.context.trim(),
                task = structuredResponse.task.trim(),
                constraints = structuredResponse.constraints.trim(),
                outputFormat = structuredResponse.outputFormat.trim(),
                isGeminiGenerated = true
            )

            Result.success(template)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isApiKeyValid(apiKey: String): Boolean {
        return apiKey.isNotBlank() &&
                apiKey != "MY_GEMINI_API_KEY" &&
                apiKey != "MY_NEW_API_KEY_DEFAULT_VALUE" &&
                apiKey != "YOUR_API_KEY_HERE"
    }
}
