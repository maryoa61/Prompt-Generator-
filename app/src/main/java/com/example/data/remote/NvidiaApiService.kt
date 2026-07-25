package com.example.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * NVIDIA NIM (build.nvidia.com) exposes an OpenAI-compatible REST API at
 * https://integrate.api.nvidia.com/v1/chat/completions
 *
 * The base URL is configured on the [retrofit2.Retrofit] instance in
 * NetworkModule, so only the path is declared here.
 */
interface NvidiaApiService {

    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") bearerToken: String,
        @Body request: NvidiaChatRequest
    ): Response<NvidiaChatResponse>
}
