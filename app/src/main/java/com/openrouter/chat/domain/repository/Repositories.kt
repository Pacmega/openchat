package com.openrouter.chat.domain.repository

import com.openrouter.chat.data.local.dao.ConversationDao
import com.openrouter.chat.data.local.dao.MessageDao
import com.openrouter.chat.data.local.dao.ModelDao
import com.openrouter.chat.data.local.entity.ConversationEntity
import com.openrouter.chat.data.local.entity.MessageEntity
import com.openrouter.chat.data.local.entity.ModelEntity
import com.openrouter.chat.data.remote.api.OpenRouterApi
import com.openrouter.chat.data.remote.dto.ChatCompletionRequest
import com.openrouter.chat.data.remote.dto.ChatMessage
import com.openrouter.chat.domain.model.AIModel
import com.openrouter.chat.domain.model.Conversation
import com.openrouter.chat.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelRepository @Inject constructor(
    private val api: OpenRouterApi,
    private val modelDao: ModelDao
) {
    fun getAllModels(): Flow<List<AIModel>> = modelDao.getAllModels().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getModelById(id: String): AIModel? = modelDao.getModelById(id)?.toDomain()

    suspend fun fetchModels(): Result<List<AIModel>> {
        return try {
            val response = api.getModels()
            if (response.isSuccessful) {
                val models = response.body()?.data?.map { dto ->
                    ModelEntity(
                        id = dto.id,
                        name = dto.name,
                        provider = dto.providerName ?: dto.provider
                    )
                } ?: emptyList()
                modelDao.replaceAll(models)
                Result.success(models.map { it.toDomain() })
            } else {
                Result.failure(Exception("Failed to fetch models: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateLastMessage(modelId: String, message: String?, timestamp: Long?) {
        modelDao.updateLastMessage(modelId, message, timestamp)
    }

    private fun ModelEntity.toDomain() = AIModel(
        id = id,
        name = name,
        provider = provider,
        lastMessage = lastMessage,
        lastMessageTimestamp = lastMessageTimestamp
    )
}

@Singleton
class ConversationRepository @Inject constructor(
    private val conversationDao: ConversationDao,
    private val modelDao: ModelDao,
    private val messageDao: MessageDao
) {
    fun getConversationsForModel(modelId: String): Flow<List<Conversation>> =
        conversationDao.getConversationsForModel(modelId).map { entities ->
            entities.map { entity ->
                val model = modelDao.getModelById(entity.modelId)
                entity.toDomain(model?.name ?: "Unknown")
            }
        }

    suspend fun createConversation(modelId: String): Long {
        val conversation = ConversationEntity(
            modelId = modelId,
            title = "New Conversation",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return conversationDao.insertConversation(conversation)
    }

    suspend fun updateTitle(conversationId: Long, title: String) {
        conversationDao.updateTitle(conversationId, title)
    }

    suspend fun deleteConversation(conversationId: Long) {
        conversationDao.deleteConversation(conversationId)
    }

    suspend fun getConversationById(conversationId: Long): Conversation? {
        val entity = conversationDao.getConversationById(conversationId) ?: return null
        val model = modelDao.getModelById(entity.modelId)
        return entity.toDomain(model?.name ?: "Unknown")
    }

    private fun ConversationEntity.toDomain(modelName: String) = Conversation(
        id = id,
        modelId = modelId,
        modelName = modelName,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

@Singleton
class MessageRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao
) {
    fun getMessagesForConversation(conversationId: Long): Flow<List<Message>> =
        messageDao.getMessagesForConversation(conversationId).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun getMessagesForConversationOnce(conversationId: Long): List<Message> =
        messageDao.getMessagesForConversationOnce(conversationId).map { it.toDomain() }

    suspend fun saveMessage(conversationId: Long, content: String, isFromUser: Boolean): Long {
        val message = MessageEntity(
            conversationId = conversationId,
            content = content,
            isFromUser = isFromUser,
            timestamp = System.currentTimeMillis()
        )
        val id = messageDao.insertMessage(message)
        
        conversationDao.getConversationById(conversationId)?.let { conv ->
            conversationDao.updateConversation(
                conv.copy(updatedAt = System.currentTimeMillis())
            )
        }
        
        return id
    }

    suspend fun updateMessageContent(messageId: Long, content: String) {
        messageDao.updateContent(messageId, content)
    }

    suspend fun markStreamingComplete(messageId: Long, finalContent: String) {
        messageDao.updateStreaming(messageId, false)
        messageDao.updateContent(messageId, finalContent)
    }

    suspend fun deleteAll() {
        messageDao.deleteAll()
    }

    private fun MessageEntity.toDomain() = Message(
        id = id,
        conversationId = conversationId,
        content = content,
        isFromUser = isFromUser,
        timestamp = timestamp,
        isStreaming = isStreaming
    )
}