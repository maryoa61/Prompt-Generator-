package com.example.domain.usecase

import com.example.data.repository.AiPromptRepository
import com.example.domain.model.PromptStyle
import com.example.domain.model.PromptTemplate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenerateAiPromptUseCase @Inject constructor(
    private val aiPromptRepository: AiPromptRepository
) {

    fun hasApiKey(): Boolean = aiPromptRepository.hasApiKey()

    suspend operator fun invoke(
        inputText: String,
        style: PromptStyle,
        customRole: String? = null,
        customConstraints: String? = null
    ): PromptTemplate {
        val metaPrompt = buildMetaPrompt(inputText, style, customRole, customConstraints)
        val dto = aiPromptRepository.generateStructuredPrompt(metaPrompt)

        return PromptTemplate(
            style = style,
            role = dto.role,
            context = dto.context,
            task = dto.task,
            constraints = dto.constraints,
            outputFormat = dto.outputFormat
        )
    }

    /**
     * The "meta-prompt": instructions we send to Gemini so *it* writes the
     * final ROLE/CONTEXT/TASK/CONSTRAINTS/OUTPUT-FORMAT fields, instead of the
     * old local logic that just split the raw text on commas. This is what
     * lets the app accept input in any language (e.g. Persian) while always
     * returning an English prompt, and avoids duplicating the same clause
     * across CONTEXT and TASK.
     */
    private fun buildMetaPrompt(
        inputText: String,
        style: PromptStyle,
        customRole: String?,
        customConstraints: String?
    ): String {
        val role = if (!customRole.isNullOrBlank()) customRole else style.defaultRole
        val baselineConstraints = if (!customConstraints.isNullOrBlank()) {
            "${style.defaultConstraints} Additional constraints: $customConstraints"
        } else {
            style.defaultConstraints
        }

        return """
            You are an expert prompt engineer. A user will give you a raw idea or
            request, written in ANY language (it may be Persian/Farsi, English, or
            a mix). Understand the full meaning regardless of language.

            Your job: turn it into a high-quality, structured prompt for an AI
            assistant, following the "${style.displayName}" style. The final
            output must ALWAYS be written entirely in English, even if the user's
            raw text was in another language.

            Rules:
            - Do not just copy the user's raw text verbatim into one field and
              also summarize it into another - each field must add distinct value.
            - "context" should describe the real domain/background implied by the
              user's request (be specific to what they actually asked, not a
              generic sentence).
            - "task" should be a clear, well-organized restatement of what the
              user wants done (rewritten/translated into English, not a raw copy).
            - Do not duplicate the same clause or sentence across "context" and
              "task".
            - Preserve important technical details exactly (numbers, identifiers,
              class/function names, file names, IP addresses, code in backticks).

            Baseline role (use as-is unless the user's request clearly implies a
            more specific one): $role

            Baseline constraints (extend, don't replace, unless irrelevant):
            $baselineConstraints

            Baseline output format:
            ${style.defaultOutputFormat}

            User's raw request:
            ---
            $inputText
            ---

            Respond with STRICT JSON only (no markdown fences, no commentary),
            using exactly these keys: "role", "context", "task", "constraints",
            "outputFormat".
        """.trimIndent()
    }
}
