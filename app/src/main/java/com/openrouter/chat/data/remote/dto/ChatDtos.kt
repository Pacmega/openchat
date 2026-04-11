package com.openrouter.chat.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatCompletionRequest(
    @SerializedName("model")
    val model: String,
    @SerializedName("messages")
    val messages: List<ChatMessage>,
    @SerializedName("stream")
    val stream: Boolean = true
)

data class ChatMessage(
    @SerializedName("role")
    val role: String,
    @SerializedName("content")
    val content: String
)

data class ChatCompletionResponse(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("object")
    val objectType: String? = null,
    @SerializedName("created")
    val created: Long? = null,
    @SerializedName("model")
    val model: String? = null,
    @SerializedName("choices")
    val choices: List<Choice>? = null
)

data class Choice(
    @SerializedName("index")
    val index: Int,
    @SerializedName("message")
    val message: ChatMessage? = null,
    @SerializedName("delta")
    val delta: ChatDelta? = null,
    @SerializedName("finish_reason")
    val finishReason: String? = null
)

data class ChatDelta(
    @SerializedName("role")
    val role: String? = null,
    @SerializedName("content")
    val content: String? = null
)