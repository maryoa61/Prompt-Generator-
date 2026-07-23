package com.example.domain.model

enum class PromptStyle(
    val displayName: String,
    val defaultRole: String,
    val defaultContext: String,
    val defaultConstraints: String,
    val defaultOutputFormat: String
) {
    SOFTWARE_DEVELOPMENT(
        displayName = "Software Dev",
        defaultRole = "Senior Software Architect & Principal Engineer",
        defaultContext = "Developing robust, scalable, and maintainable software systems adhering to clean architecture.",
        defaultConstraints = "Follow strict typing, clean architecture, handle edge cases gracefully, write clean idiomatic code.",
        defaultOutputFormat = "1. Technical Overview & Architecture\n2. Implementation Snippet\n3. Trade-offs"
    ),
    CREATIVE_WRITING(
        displayName = "Creative Writing",
        defaultRole = "Master Storyteller & Narrative Designer",
        defaultContext = "Crafting compelling narratives, vivid scenes, rich character dynamics, and immersive world-building.",
        defaultConstraints = "Maintain evocative imagery, strong sensory details, natural dialogue pacing, and avoid cliché tropes.",
        defaultOutputFormat = "1. Scene / Story Title & Logline\n2. Narrative Prose\n3. Character / World-building Breakdown"
    ),
    BUSINESS_MARKETING(
        displayName = "Business & Marketing",
        defaultRole = "Chief Marketing Officer & Direct Response Copywriter",
        defaultContext = "Creating persuasive, high-converting copy and strategic growth positioning tailored to a target demographic.",
        defaultConstraints = "Focus on customer pain points, benefit-driven messaging, strong Call to Action (CTA), and professional tone.",
        defaultOutputFormat = "1. Value Proposition / Hook\n2. Body Copy / Core Messaging\n3. Primary Call to Action & Selling Points"
    ),
    DATA_ANALYSIS(
        displayName = "Data Analysis",
        defaultRole = "Lead Data Scientist & Business Intelligence Specialist",
        defaultContext = "Analyzing complex datasets to extract actionable insights, detect trends, and optimize performance.",
        defaultConstraints = "Rely on objective metrics, define clear methodology, highlight statistical relevance, and structure recommendations.",
        defaultOutputFormat = "1. Executive Summary & Key Findings\n2. Detailed Analytical Metrics\n3. Strategic Recommendations"
    ),
    GENERAL(
        displayName = "General",
        defaultRole = "Expert Subject Matter Advisor & Concise Assistant",
        defaultContext = "Providing accurate, structured, and comprehensive information across broad topic domains.",
        defaultConstraints = "Be direct, well-organized, factually grounded, logically formatted, and easy to act upon.",
        defaultOutputFormat = "1. Summary / Core Answer\n2. Detailed Step-by-Step Breakdown\n3. Pro-Tips / Next Steps"
    );

    companion object {
        val SOFTWARE_DEV: PromptStyle get() = SOFTWARE_DEVELOPMENT

        fun fromName(name: String): PromptStyle {
            return entries.find {
                it.name.equals(name, ignoreCase = true) ||
                        (name.equals("SOFTWARE_DEV", ignoreCase = true) && it == SOFTWARE_DEVELOPMENT) ||
                        it.displayName.equals(name, ignoreCase = true)
            } ?: SOFTWARE_DEVELOPMENT
        }
    }
}

data class UserPromptInput(
    val rawText: String,
    val keywords: List<String> = emptyList(),
    val style: PromptStyle = PromptStyle.SOFTWARE_DEVELOPMENT
)

data class GeneratedPrompt(
    val formattedPrompt: String,
    val timestamp: Long = System.currentTimeMillis(),
    val style: PromptStyle
)

data class PromptTemplate(
    val style: PromptStyle,
    val role: String,
    val context: String,
    val task: String,
    val constraints: String,
    val outputFormat: String
) {
    fun toFormattedString(): String {
        return """
ROLE: $role

CONTEXT:
$context

TASK:
$task

CONSTRAINTS:
$constraints

OUTPUT FORMAT:
$outputFormat
""".trimIndent()
    }
}

object PromptTemplateProvider {

    fun getTemplateForStyle(style: PromptStyle): String {
        return when (style) {
            PromptStyle.SOFTWARE_DEVELOPMENT -> """
Role: Senior Software Architect & Principal Engineer

Context:
You are designing and engineering high-performance software solutions. The project involves the following technical context and domain rules:
{{CONTEXT}}

Task:
Architect and implement a robust, production-ready solution based on the requirement:
"{{TASK}}"

Key Requirements & Focus Areas:
{{KEYWORDS}}

Constraints:
- Follow clean architecture, SOLID principles, and idiomatic coding standards.
- Ensure strict type safety, proper error handling, and performance optimization.
- Provide clear explanatory documentation alongside implementation snippets.

Output Format:
1. Architectural Overview & System Design
2. Production Code / Implementation
3. Key Considerations, Edge Cases & Trade-offs
            """.trimIndent()

            PromptStyle.CREATIVE_WRITING -> """
Role: Master Storyteller & Worldbuilding Specialist

Context:
You are crafting evocative narrative prose and immersive scenes. The narrative context is set around:
{{CONTEXT}}

Task:
Write a compelling story segment or narrative scene based on:
"{{TASK}}"

Core Motifs & Themes:
{{KEYWORDS}}

Constraints:
- Use rich sensory details, strong subtext, and natural dialogue pacing.
- Avoid cliché tropes unless subverting them intentionally.
- Maintain an engaging tone and distinct narrative voice.

Output Format:
1. Logline & Scene Synopsis
2. Main Narrative Prose
3. Character & Worldbuilding Elements Breakdown
            """.trimIndent()

            PromptStyle.BUSINESS_MARKETING -> """
Role: Chief Marketing Officer & Strategic Copywriter

Context:
You are crafting high-converting marketing campaigns and market positioning. Context:
{{CONTEXT}}

Task:
Develop persuasive marketing copy and positioning strategy based on:
"{{TASK}}"

Target Angle & Keywords:
{{KEYWORDS}}

Constraints:
- Focus on customer pain points, value proposition, and actionable benefits.
- Include compelling Hooks, clear value drivers, and strong Calls to Action (CTAs).
- Use concise, punchy, and persuasive language tailored to the audience.

Output Format:
1. Value Proposition & Attention Hook
2. Core Campaign Copy & Key Selling Points
3. Call to Action (CTA) & Conversion Strategy
            """.trimIndent()

            PromptStyle.DATA_ANALYSIS -> """
Role: Lead Data Scientist & Business Intelligence Specialist

Context:
You are analyzing complex dataset logic and statistical patterns to extract actionable insights. Analytical context:
{{CONTEXT}}

Task:
Perform rigorous analytical breakdown and data strategy for:
"{{TASK}}"

Focus Metrics & Attributes:
{{KEYWORDS}}

Constraints:
- Ground all conclusions in objective data points and clear methodology.
- Highlight statistical significance, anomalies, and optimization opportunities.
- Provide actionable, structured recommendations.

Output Format:
1. Executive Summary & Key Analytical Findings
2. In-Depth Metrics & Trend Analysis
3. Strategic Recommendations & Next Steps
            """.trimIndent()

            PromptStyle.GENERAL -> """
Role: Subject Matter Expert & Clear Technical Communicator

Context:
You are providing accurate, comprehensive, and well-structured answers. Context:
{{CONTEXT}}

Task:
Deliver an in-depth, structured response addressing:
"{{TASK}}"

Key Topics Covered:
{{KEYWORDS}}

Constraints:
- Be concise, direct, and logically organized.
- Use bullet points, bold emphasis, and clear headings.
- Ensure factual accuracy and practical utility.

Output Format:
1. Overview & Core Concept
2. Step-by-Step Explanation
3. Pro-Tips & Practical Takeaways
            """.trimIndent()
        }
    }
}
