package com.example.data.repository

import com.example.domain.model.PromptStyle
import com.example.domain.model.PromptTemplate

interface AiPromptRepository {
    suspend fun generatePromptWithGemini(
        rawInput: String,
        style: PromptStyle
    ): Result<PromptTemplate>
}
