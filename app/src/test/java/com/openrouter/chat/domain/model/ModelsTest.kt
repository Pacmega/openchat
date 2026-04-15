package com.openrouter.chat.domain.model

import org.junit.Test
import org.junit.Assert.*

class AIModelTest {

    @Test
    fun `AIModel should create with required fields`() {
        val model = AIModel(
            id = "model-1",
            name = "GPT-4",
            provider = "OpenAI"
        )

        assertEquals("model-1", model.id)
        assertEquals("GPT-4", model.name)
        assertEquals("OpenAI", model.provider)
    }

    @Test
    fun `AIModel should have default null values for optional fields`() {
        val model = AIModel(
            id = "model-1",
            name = "GPT-4",
            provider = "OpenAI"
        )

        assertNull(model.lastMessage)
        assertNull(model.lastMessageTimestamp)
    }

    @Test
    fun `AIModel should accept optional fields`() {
        val timestamp = System.currentTimeMillis()
        val model = AIModel(
            id = "model-1",
            name = "GPT-4",
            provider = "OpenAI",
            lastMessage = "Hello",
            lastMessageTimestamp = timestamp
        )

        assertEquals("Hello", model.lastMessage)
        assertEquals(timestamp, model.lastMessageTimestamp)
    }

    @Test
    fun `AIModel should support equals`() {
        val model1 = AIModel(id = "model-1", name = "GPT-4", provider = "OpenAI")
        val model2 = AIModel(id = "model-1", name = "GPT-4", provider = "OpenAI")
        val model3 = AIModel(id = "model-2", name = "GPT-3", provider = "OpenAI")

        assertEquals(model1, model2)
        assertNotEquals(model1, model3)
    }
}

class ConversationTest {

    @Test
    fun `Conversation should create with all fields`() {
        val timestamp = System.currentTimeMillis()
        val conversation = Conversation(
            id = 1L,
            modelId = "model-1",
            modelName = "GPT-4",
            title = "Test Conversation",
            createdAt = timestamp,
            updatedAt = timestamp
        )

        assertEquals(1L, conversation.id)
        assertEquals("model-1", conversation.modelId)
        assertEquals("GPT-4", conversation.modelName)
        assertEquals("Test Conversation", conversation.title)
        assertEquals(timestamp, conversation.createdAt)
        assertEquals(timestamp, conversation.updatedAt)
    }
}

class MessageTest {

    @Test
    fun `Message should create with required fields`() {
        val timestamp = System.currentTimeMillis()
        val message = Message(
            id = 1L,
            conversationId = 1L,
            content = "Hello",
            isFromUser = true,
            timestamp = timestamp
        )

        assertEquals(1L, message.id)
        assertEquals(1L, message.conversationId)
        assertEquals("Hello", message.content)
        assertTrue(message.isFromUser)
        assertEquals(timestamp, message.timestamp)
    }

    @Test
    fun `Message should have default false for isStreaming`() {
        val message = Message(
            id = 1L,
            conversationId = 1L,
            content = "Hello",
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )

        assertFalse(message.isStreaming)
    }
}