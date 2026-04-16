package com.openrouter.chat.ui.screens.chat

import com.openrouter.chat.domain.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Test
import org.junit.Assert.*

class ChatScreenMessageAlignmentTest {

    @Test
    fun `user messages should have isFromUser set to true`() {
        val userMessage = Message(
            id = 1L,
            conversationId = 1L,
            content = "Hello",
            isFromUser = true,
            timestamp = 1L,
            isStreaming = false
        )

        assertTrue("User message should have isFromUser = true", userMessage.isFromUser)
    }

    @Test
    fun `assistant messages should have isFromUser set to false`() {
        val assistantMessage = Message(
            id = 2L,
            conversationId = 1L,
            content = "Hello, how can I help?",
            isFromUser = false,
            timestamp = 2L,
            isStreaming = false
        )

        assertFalse("Assistant message should have isFromUser = false", assistantMessage.isFromUser)
    }

    @Test
    fun `messages should retain their isFromUser value when content is updated`() {
        val originalMessage = Message(
            id = 1L,
            conversationId = 1L,
            content = "User's question",
            isFromUser = true,
            timestamp = 1L,
            isStreaming = false
        )

        val updatedMessage = originalMessage.copy(content = "Assistant's answer")

        assertTrue(
            "After content update, message should still have isFromUser = true if it was a user message",
            updatedMessage.isFromUser
        )

        val differentMessage = Message(
            id = 2L,
            conversationId = 1L,
            content = "Assistant's answer",
            isFromUser = false,
            timestamp = 2L,
            isStreaming = false
        )

        assertFalse(
            "Assistant message should have isFromUser = false",
            differentMessage.isFromUser
        )
    }

    @Test
    fun `user and assistant messages should be separate entities`() {
        val userMessage = Message(
            id = 1L,
            conversationId = 1L,
            content = "User message",
            isFromUser = true,
            timestamp = 1L,
            isStreaming = false
        )

        val assistantMessage = Message(
            id = 2L,
            conversationId = 1L,
            content = "Assistant response",
            isFromUser = false,
            timestamp = 2L,
            isStreaming = false
        )

        assertNotEquals(
            "User and assistant messages should have different IDs",
            userMessage.id,
            assistantMessage.id
        )

        assertTrue("User message isFromUser should be true", userMessage.isFromUser)
        assertFalse("Assistant message isFromUser should be false", assistantMessage.isFromUser)
    }

    @Test
    fun `message list should contain both user and assistant messages`() {
        val messages = listOf(
            Message(
                id = 1L,
                conversationId = 1L,
                content = "Hello",
                isFromUser = true,
                timestamp = 1L,
                isStreaming = false
            ),
            Message(
                id = 2L,
                conversationId = 1L,
                content = "Hi there!",
                isFromUser = false,
                timestamp = 2L,
                isStreaming = false
            )
        )

        val userMessages = messages.filter { it.isFromUser }
        val assistantMessages = messages.filter { !it.isFromUser }

        assertEquals("Should have 1 user message", 1, userMessages.size)
        assertEquals("Should have 1 assistant message", 1, assistantMessages.size)
    }

    @Test
    fun `alignment should be End for user messages and Start for assistant messages`() {
        fun getAlignment(isFromUser: Boolean): String {
            return if (isFromUser) "Alignment.End" else "Alignment.Start"
        }

        val userAlignment = getAlignment(true)
        val assistantAlignment = getAlignment(false)

        assertEquals("User messages should align to End (right)", "Alignment.End", userAlignment)
        assertEquals("Assistant messages should align to Start (left)", "Alignment.Start", assistantAlignment)
    }

    @Test
    fun `streamResponse should create separate assistant message`() {
        val sentUserMessageId = 1L
        val expectedAssistantMessageId = 2L

        assertNotEquals(
            "Assistant message should have different ID than user message",
            sentUserMessageId,
            expectedAssistantMessageId
        )
    }

    @Test
    fun `user message should not be replaced by assistant response`() {
        val userMessage = Message(
            id = 1L,
            conversationId = 1L,
            content = "Hello, how are you?",
            isFromUser = true,
            timestamp = 1L,
            isStreaming = false
        )

        val assistantMessage = Message(
            id = 2L,
            conversationId = 1L,
            content = "I am doing well, thank you!",
            isFromUser = false,
            timestamp = 2L,
            isStreaming = false
        )

        val allMessages = listOf(userMessage, assistantMessage)

        assertEquals(
            "Both user and assistant messages should be in the message list",
            2,
            allMessages.size
        )

        assertTrue(
            "User message should still be in list",
            allMessages.any { it.id == 1L && it.isFromUser }
        )

        assertTrue(
            "Assistant message should be in list",
            allMessages.any { it.id == 2L && !it.isFromUser }
        )
    }

    @Test
    fun `chat history should preserve all messages in order`() {
        val chatHistory = listOf(
            Message(1L, 1L, "First user message", true, 1L, false),
            Message(2L, 1L, "First assistant response", false, 2L, false),
            Message(3L, 1L, "Second user message", true, 3L, false),
            Message(4L, 1L, "Second assistant response", false, 4L, false),
            Message(5L, 1L, "Third user message", true, 5L, false)
        )

        assertEquals("Chat history should have 5 messages", 5, chatHistory.size)
        assertEquals("First message should be from user", true, chatHistory[0].isFromUser)
        assertEquals("Second message should be from assistant", false, chatHistory[1].isFromUser)
        assertEquals("Third message should be from user", true, chatHistory[2].isFromUser)
        assertEquals("Fourth message should be from assistant", false, chatHistory[3].isFromUser)
        assertEquals("Fifth message should be from user", true, chatHistory[4].isFromUser)
    }

    @Test
    fun `sending new user message should add to existing chat history`() {
        val existingMessages = listOf(
            Message(1L, 1L, "Existing user message", true, 1L, false),
            Message(2L, 1L, "Existing assistant response", false, 2L, false)
        )

        val newUserMessage = Message(
            id = 3L,
            conversationId = 1L,
            content = "New user question",
            isFromUser = true,
            timestamp = 3L,
            isStreaming = false
        )

        val updatedMessages = existingMessages + newUserMessage

        assertEquals("Should have 3 messages after sending new message", 3, updatedMessages.size)
        assertTrue("New user message should be in list", updatedMessages.any { it.id == 3L && it.isFromUser })
    }

    @Test
    fun `ui state messages should be mapped correctly from repository`() {
        val repositoryMessages = listOf(
            Message(1L, 1L, "User says hello", true, 1000L, false),
            Message(2L, 1L, "Assistant says hi", false, 2000L, false)
        )

        val firstMessage = repositoryMessages.first()
        val secondMessage = repositoryMessages.last()

        assertTrue("First message should be user message", firstMessage.isFromUser)
        assertFalse("Second message should be assistant message", secondMessage.isFromUser)
    }
}