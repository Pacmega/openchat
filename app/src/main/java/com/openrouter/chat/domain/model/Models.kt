package com.openrouter.chat.domain.model

data class AIModel(
    val id: String,
    val name: String,
    val provider: String,
    val lastMessage: String? = null,
    val lastMessageTimestamp: Long? = null
)

data class Conversation(
    val id: Long,
    val modelId: String,
    val modelName: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)

data class Message(
    val id: Long,
    val conversationId: Long,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val isStreaming: Boolean = false
)