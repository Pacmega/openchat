package com.openrouter.chat.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OpenRouterModelsResponse(
    @SerializedName("data")
    val data: List<OpenRouterModel>
)

data class OpenRouterModel(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("top_provider")
    val topProvider: TopProvider? = null,
    @SerializedName("context_length")
    val contextLength: Long? = null
)

data class TopProvider(
    @SerializedName("context_length")
    val contextLength: Long? = null,
    @SerializedName("max_completion_tokens")
    val maxCompletionTokens: Long? = null,
    @SerializedName("is_moderated")
    val isModerated: Boolean? = null
)

data class ApiKeyResponse(
    @SerializedName("data")
    val data: ApiKeyInfo
)

data class ApiKeyInfo(
    @SerializedName("label")
    val label: String
)