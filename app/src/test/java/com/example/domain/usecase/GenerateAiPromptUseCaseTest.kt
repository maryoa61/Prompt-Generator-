package com.example.domain.usecase

import com.example.data.repository.AiPromptRepository
import com.example.domain.model.PromptStyle
import com.example.domain.model.PromptTemplate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GenerateAiPromptUseCaseTest {

    private lateinit var fakeAiRepository: FakeAiPromptRepository
    private lateinit var localUseCase: GeneratePromptUseCase
    private lateinit var generateAiPromptUseCase: GenerateAiPromptUseCase

    @Before
    fun setUp() {
        fakeAiRepository = FakeAiPromptRepository()
        localUseCase = GeneratePromptUseCase(PromptFormatterUseCase())
        generateAiPromptUseCase = GenerateAiPromptUseCase(fakeAiRepository, localUseCase)
    }

    @Test
    fun testSuccessfulGeminiGeneration() = runTest {
        fakeAiRepository.shouldSucceed = true
        val result = generateAiPromptUseCase("Create a login screen in Jetpack Compose", PromptStyle.SOFTWARE_DEVELOPMENT)

        assertTrue(result is AiPromptResult.Success)
        val success = result as AiPromptResult.Success
        assertEquals("SENIOR SOFTWARE ARCHITECT & KOTLIN SPECIALIST", success.template.role)
        assertTrue(success.template.isGeminiGenerated)
    }

    @Test
    fun testFallbackWhenGeminiFails() = runTest {
        fakeAiRepository.shouldSucceed = false
        val result = generateAiPromptUseCase("Create a login screen in Jetpack Compose", PromptStyle.SOFTWARE_DEVELOPMENT)

        assertTrue(result is AiPromptResult.Fallback)
        val fallback = result as AiPromptResult.Fallback
        assertEquals("Gemini API key is not configured", fallback.reason)
        assertEquals(false, fallback.template.isGeminiGenerated)
    }

    private class FakeAiPromptRepository : AiPromptRepository {
        var shouldSucceed: Boolean = true

        override suspend fun generatePromptWithGemini(
            rawInput: String,
            style: PromptStyle
        ): Result<PromptTemplate> {
            return if (shouldSucceed) {
                Result.success(
                    PromptTemplate(
                        style = style,
                        role = "SENIOR SOFTWARE ARCHITECT & KOTLIN SPECIALIST",
                        context = "Developing Android Jetpack Compose authentication flows.",
                        task = "Implement a modern login screen with state management.",
                        constraints = "- Follow clean architecture\n- Handle error states",
                        outputFormat = "1. Architecture Overview\n2. Compose Code",
                        isGeminiGenerated = true
                    )
                )
            } else {
                Result.failure(IllegalStateException("Gemini API key is not configured"))
            }
        }
    }
}
