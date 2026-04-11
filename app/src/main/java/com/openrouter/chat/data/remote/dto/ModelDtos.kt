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
    @SerializedName("provider")
    val provider: String,
    @SerializedName("provider_name")
    val providerName: String? = null,
    @SerializedName("context_length")
    val contextLength: Long? = null
)