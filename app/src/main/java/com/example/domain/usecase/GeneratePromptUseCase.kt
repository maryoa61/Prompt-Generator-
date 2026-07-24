package com.example.domain.usecase

import com.example.domain.model.GeneratedPrompt
import com.example.domain.model.PromptStyle
import com.example.domain.model.PromptTemplate
import com.example.domain.model.UserPromptInput
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
        val input = UserPromptInput(
            rawText = inputText,
            keywords = emptyList(),
            style = style
        )
        val generatedPrompt: GeneratedPrompt = promptFormatterUseCase.generate(input)

        val role = if (!customRole.isNullOrBlank()) customRole else style.defaultRole
        val constraints = if (!customConstraints.isNullOrBlank()) {
            "${style.defaultConstraints}\n- Additional Constraints: $customConstraints"
        } else {
            style.defaultConstraints
        }

        return PromptTemplate(
            style = style,
            role = role,
            context = generatedPrompt.formattedPrompt,
            task = if (inputText.isNotBlank()) inputText.trim() else "Execute ${style.displayName} task",
            constraints = constraints,
            outputFormat = style.defaultOutputFormat
        )
    }
}
