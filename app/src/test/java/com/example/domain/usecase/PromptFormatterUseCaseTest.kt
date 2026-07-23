package com.example.domain.usecase

import com.example.domain.model.PromptStyle
import com.example.domain.model.UserPromptInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PromptFormatterUseCaseTest {

    private lateinit var useCase: PromptFormatterUseCase

    @Before
    fun setUp() {
        useCase = PromptFormatterUseCase()
    }

    @Test
    fun testSoftwareDevelopmentStyleGeneration() {
        val input = UserPromptInput(
            rawText = "  Jetpack Compose,   Room Database, MVVM, Clean Architecture  \n\n ",
            keywords = listOf("Kotlin Flow", "Coroutines", "Room Database"),
            style = PromptStyle.SOFTWARE_DEVELOPMENT
        )

        val result = useCase.generate(input)

        assertNotNull(result)
        assertEquals(PromptStyle.SOFTWARE_DEVELOPMENT, result.style)
        assertTrue(result.formattedPrompt.contains("Role: Senior Software Architect & Principal Engineer"))
        assertTrue(result.formattedPrompt.contains("Jetpack Compose, Room Database, MVVM, Clean Architecture"))
        assertTrue(result.formattedPrompt.contains("• Kotlin Flow"))
        assertTrue(result.formattedPrompt.contains("• Coroutines"))
        assertTrue(result.formattedPrompt.contains("Output Format:"))
    }

    @Test
    fun testCreativeWritingStyleGeneration() {
        val input = UserPromptInput(
            rawText = "A mysterious artifact discovered in an abandoned orbital station.",
            keywords = listOf("Sci-Fi", "Atmospheric", "Space Exploration"),
            style = PromptStyle.CREATIVE_WRITING
        )

        val result = useCase.generate(input)

        assertNotNull(result)
        assertEquals(PromptStyle.CREATIVE_WRITING, result.style)
        assertTrue(result.formattedPrompt.contains("Role: Master Storyteller & Worldbuilding Specialist"))
        assertTrue(result.formattedPrompt.contains("mysterious artifact discovered"))
        assertTrue(result.formattedPrompt.contains("• Sci-Fi"))
        assertTrue(result.formattedPrompt.contains("• Atmospheric"))
        assertTrue(result.formattedPrompt.contains("Logline & Scene Synopsis"))
    }

    @Test
    fun testBusinessMarketingStyleGeneration() {
        val input = UserPromptInput(
            rawText = "Launch campaign for an AI productivity app for busy software developers.",
            keywords = listOf("Conversion", "High Impact", "SaaS Growth"),
            style = PromptStyle.BUSINESS_MARKETING
        )

        val result = useCase.generate(input)

        assertNotNull(result)
        assertEquals(PromptStyle.BUSINESS_MARKETING, result.style)
        assertTrue(result.formattedPrompt.contains("Role: Chief Marketing Officer & Strategic Copywriter"))
        assertTrue(result.formattedPrompt.contains("AI productivity app"))
        assertTrue(result.formattedPrompt.contains("• Conversion"))
        assertTrue(result.formattedPrompt.contains("Value Proposition & Attention Hook"))
    }

    @Test
    fun testDataAnalysisStyleGeneration() {
        val input = UserPromptInput(
            rawText = "Optimize SQL query execution times across 10 million user transaction records.",
            keywords = listOf("PostgreSQL", "Indexing", "Query Execution Plan"),
            style = PromptStyle.DATA_ANALYSIS
        )

        val result = useCase.generate(input)

        assertNotNull(result)
        assertEquals(PromptStyle.DATA_ANALYSIS, result.style)
        assertTrue(result.formattedPrompt.contains("Role: Lead Data Scientist & Business Intelligence Specialist"))
        assertTrue(result.formattedPrompt.contains("Optimize SQL query execution times"))
        assertTrue(result.formattedPrompt.contains("• PostgreSQL"))
        assertTrue(result.formattedPrompt.contains("Executive Summary & Key Analytical Findings"))
    }

    @Test
    fun testGeneralStyleAndTextNormalization() {
        val input = UserPromptInput(
            rawText = "   Explain     quantum computing    principles   \n\n\n\n  in plain English.  ",
            keywords = emptyList(),
            style = PromptStyle.GENERAL
        )

        val result = useCase.generate(input)

        assertNotNull(result)
        assertEquals(PromptStyle.GENERAL, result.style)
        assertTrue(result.formattedPrompt.contains("Role: Subject Matter Expert & Clear Technical Communicator"))
        assertTrue(result.formattedPrompt.contains("Explain quantum computing principles in plain English."))
        assertTrue(result.formattedPrompt.contains("Step-by-Step Explanation"))
    }
}
