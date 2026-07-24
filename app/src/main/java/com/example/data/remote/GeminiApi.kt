package com.example.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Thin Retrofit wrapper around the public Gemini Developer API.
 * Base URL: https://generativelanguage.googleapis.com/
 *
 * The API key is passed as a query parameter (`?key=...`), exactly like
 * Google's own REST examples, so no extra auth interceptor is needed.
 */
interface GeminiApi {

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
