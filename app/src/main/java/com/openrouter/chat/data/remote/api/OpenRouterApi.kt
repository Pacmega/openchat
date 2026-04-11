package com.openrouter.chat.data.remote.api

import com.openrouter.chat.data.remote.dto.ChatCompletionRequest
import com.openrouter.chat.data.remote.dto.ChatCompletionResponse
import com.openrouter.chat.data.remote.dto.OpenRouterModelsResponse
import retrofit2.Response
import retrofit2.http.*

interface OpenRouterApi {
    @GET("models")
    suspend fun getModels(): Response<OpenRouterModelsResponse>

    @POST("chat/completions")
    @Streaming
    suspend fun sendMessage(
        @Body request: ChatCompletionRequest
    ): Response<ChatCompletionResponse>
}