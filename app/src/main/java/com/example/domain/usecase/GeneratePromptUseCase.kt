package com.example.domain.usecase

import com.example.domain.model.PromptStyle
import com.example.domain.model.PromptTemplate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneratePromptUseCase @Inject constructor(
    private val promptFormatterUseCase: PromptFormatterUseCase = PromptFormatterUseCase()
) {

    operator fun invoke(
        inputText: String,
        style: PromptStyle,
        customRole: String? = null,
        customConstraints: String? = null
    ): PromptTemplate {
        val role = if (!customRole.isNullOrBlank()) customRole else style.defaultRole
        val constraints = if (!customConstraints.isNullOrBlank()) {
            "${style.defaultConstraints}\n- Additional Constraints: $customConstraints"
        } else {
            style.defaultConstraints
        }

        // Build a proper CONTEXT string: the style's base context description,
        // enriched with keywords extracted from the user's input (if any).
        // NOTE: previously this incorrectly embedded the *entire* nested
        // formatted template (role/context/task/constraints/output format)
        // produced by PromptFormatterUseCase.generate() into this single
        // CONTEXT field, which duplicated content and broke the output.
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
}
