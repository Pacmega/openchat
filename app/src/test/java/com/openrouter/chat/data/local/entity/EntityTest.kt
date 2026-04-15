package com.openrouter.chat.data.local.entity

import org.junit.Test
import org.junit.Assert.*

class ModelEntityTest {

    @Test
    fun `ModelEntity should create with required fields`() {
        val entity = ModelEntity(
            id = "model-1",
            name = "GPT-4",
            provider = "OpenAI"
        )

        assertEquals("model-1", entity.id)
        assertEquals("GPT-4", entity.name)
        assertEquals("OpenAI", entity.provider)
    }

    @Test
    fun `ModelEntity should support lastMessage updates`() {
        val entity = ModelEntity(
            id = "model-1",
            name = "GPT-4",
            provider = "OpenAI"
        )

        assertNull(entity.lastMessage)

        val withMessage = entity.copy(
            lastMessage = "Hello",
            lastMessageTimestamp = 1000L
        )

        assertEquals("Hello", withMessage.lastMessage)
        assertEquals(1000L, withMessage.lastMessageTimestamp)
    }
}

class ConversationEntityTest {

    @Test
    fun `ConversationEntity should create with required fields`() {
        val entity = ConversationEntity(
            modelId = "model-1",
            title = "Test Chat",
            createdAt = 1000L,
            updatedAt = 1000L
        )

        assertEquals("model-1", entity.modelId)
        assertEquals("Test Chat", entity.title)
        assertEquals(1000L, entity.createdAt)
        assertEquals(1000L, entity.updatedAt)
    }

    @Test
    fun `ConversationEntity id should default to 0`() {
        val entity = ConversationEntity(
            modelId = "model-1",
            title = "Test",
            createdAt = 1000L,
            updatedAt = 1000L
        )

        assertEquals(0L, entity.id)
    }
}

class MessageEntityTest {

    @Test
    fun `MessageEntity should create with required fields`() {
        val entity = MessageEntity(
            conversationId = 1L,
            content = "Hello",
            isFromUser = true,
            timestamp = 1000L
        )

        assertEquals(1L, entity.conversationId)
        assertEquals("Hello", entity.content)
        assertTrue(entity.isFromUser)
        assertEquals(1000L, entity.timestamp)
    }

    @Test
    fun `MessageEntity should default id to 0`() {
        val entity = MessageEntity(
            conversationId = 1L,
            content = "Hello",
            isFromUser = true,
            timestamp = 1000L
        )

        assertEquals(0L, entity.id)
    }

    @Test
    fun `MessageEntity should default isStreaming to false`() {
        val entity = MessageEntity(
            conversationId = 1L,
            content = "Hello",
            isFromUser = true,
            timestamp = 1000L
        )

        assertFalse(entity.isStreaming)
    }

    @Test
    fun `MessageEntity should support isStreaming true`() {
        val entity = MessageEntity(
            conversationId = 1L,
            content = "Hello",
            isFromUser = false,
            timestamp = 1000L,
            isStreaming = true
        )

        assertTrue(entity.isStreaming)
    }
}