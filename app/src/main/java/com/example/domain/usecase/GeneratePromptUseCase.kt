package com.example.domain.usecase

import com.example.data.remote.AiPromptDataSource
import com.example.data.remote.AiPromptResult
import com.example.domain.model.PromptSource
import com.example.domain.model.PromptStyle
import com.example.domain.model.PromptTemplate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneratePromptUseCase @Inject constructor(
    private val promptFormatterUseCase: PromptFormatterUseCase,
    private val aiPromptDataSource: AiPromptDataSource
) {

    /**
     * @param template the resulting prompt (either produced by the NVIDIA API or,
     *   on failure/missing key, by the local offline formatter).
     * @param source which path produced [template].
     * @param notice optional user-facing message explaining why the offline
     *   fallback was used (null when [source] is [PromptSource.AI]).
     */
    data class Result(
        val template: PromptTemplate,
        val source: PromptSource,
        val notice: String? = null
    )

    suspend operator fun invoke(
        inputText: String,
        style: PromptStyle,
        customRole: String? = null,
        customConstraints: String? = null
    ): Result {
        val offlineTemplate = buildOfflineTemplate(inputText, style, customRole, customConstraints)

        return when (val aiResult = aiPromptDataSource.generateStructuredPrompt(style, inputText)) {
            is AiPromptResult.Success -> {
                val parsedSections = parseSections(aiResult.text)
                val template = PromptTemplate(
                    style = style,
                    role = parsedSections["ROLE"]?.takeIf { it.isNotBlank() } ?: offlineTemplate.role,
                    context = parsedSections["CONTEXT"]?.takeIf { it.isNotBlank() } ?: offlineTemplate.context,
                    task = parsedSections["TASK"]?.takeIf { it.isNotBlank() } ?: offlineTemplate.task,
                    constraints = parsedSections["CONSTRAINTS"]?.takeIf { it.isNotBlank() } ?: offlineTemplate.constraints,
                    outputFormat = parsedSections["OUTPUT FORMAT"]?.takeIf { it.isNotBlank() } ?: offlineTemplate.outputFormat
                )
                Result(template = template, source = PromptSource.AI)
            }

            is AiPromptResult.NotConfigured -> Result(
                template = offlineTemplate,
                source = PromptSource.OFFLINE_FALLBACK,
                notice = "Notice: GROQ API key is not configured. Using offline generation."
            )

            is AiPromptResult.Failure -> Result(
                template = offlineTemplate,
                source = PromptSource.OFFLINE_FALLBACK,
                notice = "Notice: ${aiResult.reason}. Using offline generation."
            )
        }
    }

    /**
     * The original fully-local template generation logic. Used as the base
     * (and as a fallback for any AI-response section that comes back empty
     * or unparsable) so the app always has a usable prompt on hand.
     */
    private fun buildOfflineTemplate(
        inputText: String,
        style: PromptStyle,
        customRole: String?,
        customConstraints: String?
    ): PromptTemplate {
        val role = if (!customRole.isNullOrBlank()) customRole else style.defaultRole
        val constraints = if (!customConstraints.isNullOrBlank()) {
            "${style.defaultConstraints}\n- Additional Constraints: $customConstraints"
        } else {
            style.defaultConstraints
        }

        val keywords = promptFormatterUseCase.extractKeywords(inputText)
        val context = if (keywords.isNotEmpty()) {
            buildString {
                append(style.defaultContext)
                append("\n\nKey Focus Areas:\n")
                append(keywords.joinToString(separator = "\n") { "• $it" })
            }
        } else {
            style.defaultContext
        }

        return PromptTemplate(
            style = style,
            role = role,
            context = context,
            task = if (inputText.isNotBlank()) inputText.trim() else "Execute ${style.displayName} task",
            constraints = constraints,
            outputFormat = style.defaultOutputFormat
        )
    }

    /**
     * Parses a plain-text AI response formatted with "ROLE:", "CONTEXT:",
     * "TASK:", "CONSTRAINTS:" and "OUTPUT FORMAT:" section headers (as
     * instructed in [AiPromptDataSource]'s system prompt) into a map of
     * header -> body. Missing or malformed sections are simply absent from
     * the map, letting the caller fall back to the offline template per field.
     */
    private fun parseSections(text: String): Map<String, String> {
        val headers = listOf("ROLE", "CONTEXT", "TASK", "CONSTRAINTS", "OUTPUT FORMAT")
        val headerRegex = Regex(
            "(?:" + headers.joinToString("|") { Regex.escape(it) } + "):"
        )
        val matches = headerRegex.findAll(text).toList()
        if (matches.isEmpty()) return emptyMap()

        val result = mutableMapOf<String, String>()
        for (i in matches.indices) {
            val current = matches[i]
            val headerName = current.value.removeSuffix(":").trim().uppercase()
            val start = current.range.last + 1
            val end = if (i + 1 < matches.size) matches[i + 1].range.first else text.length
            result[headerName] = text.substring(start, end).trim()
        }
        return result
    }
}
