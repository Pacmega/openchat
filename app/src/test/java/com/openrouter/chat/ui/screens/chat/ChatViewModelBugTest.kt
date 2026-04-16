package com.openrouter.chat.ui.screens.chat

import com.openrouter.chat.domain.model.Message
import org.junit.Test
import org.junit.Assert.*

class ChatViewModelBugTest {

    @Test
    fun sendingMessage_shouldCreateSeparateAssistantMessage() {
        val userContent = "Hello"
        
        val userMessageId = messageRepository.saveMessage(1L, userContent, isFromUser = true)
        
        messageRepository.updateMessageContent(userMessageId, "Hi there!")

        val messages = messageRepository.getMessagesForConversationOnce(1L)
        
        assertEquals(
            "Expected 2 separate messages (user + assistant), but got 1. " +
            "The bug: updateMessageContent overwrites user message instead of creating assistant message.",
            2,
            messages.size
        )
    }

    @Test
    fun userMessageContent_shouldBePreservedAfterAssistantResponse() {
        val userContent = "Hello"
        
        val userMessageId = messageRepository.saveMessage(1L, userContent, isFromUser = true)
        
        messageRepository.updateMessageContent(userMessageId, "Assistant response")

        val messages = messageRepository.getMessagesForConversationOnce(1L)
        val userMessage = messages.first { it.id == userMessageId }

        assertEquals(
            "User message content should be preserved. " +
            "The bug: user message content gets overwritten.",
            userContent,
            userMessage.content
        )
    }

    @Test
    fun assistantMessage_shouldBeSeparateFromUserMessage() {
        val userMessageId = messageRepository.saveMessage(1L, "Hello", isFromUser = true)
        
        messageRepository.updateMessageContent(userMessageId, "Assistant response")

        val messages = messageRepository.getMessagesForConversationOnce(1L)
        val assistantMessages = messages.filter { !it.isFromUser }

        assertEquals(
            "Should have 1 separate assistant message. " +
            "The bug: no separate assistant message is created.",
            1,
            assistantMessages.size
        )
    }

    @Test
    fun userMessage_shouldRetainIsFromUserFlag_afterUpdateOnlyIfCorrectlyDesigned() {
        val userContent = "Hello"
        
        val userMessageId = messageRepository.saveMessage(1L, userContent, isFromUser = true)
        
        messageRepository.updateMessageContent(userMessageId, "Assistant response")

        val messages = messageRepository.getMessagesForConversationOnce(1L)
        val message = messages.first { it.id == userMessageId }

        assertTrue(
            "User message should have isFromUser = true",
            message.isFromUser
        )
        assertEquals(
            "User message content should remain 'Hello', not 'Assistant response'. " +
            "The bug: content gets changed but alignment stays wrong.",
            userContent,
            message.content
        )
    }

    @Test
    fun chatHistory_shouldShowBothUserAndAssistantMessages() {
        messageRepository.saveMessage(1L, "First question", isFromUser = true)
        
        messageRepository.updateMessageContent(1L, "First answer")

        val messages = messageRepository.getMessagesForConversationOnce(1L)
        val userMessages = messages.filter { it.isFromUser }
        val assistantMessages = messages.filter { !it.isFromUser }

        assertEquals("Should have at least 1 user message", 1, userMessages.size)
        assertEquals(
            "Should have at least 1 assistant message. " +
            "The bug: assistant message replaces user message.",
            1,
            assistantMessages.size
        )
    }
}

object messageRepository {
    private val _messages = mutableListOf<Message>()

    fun getMessagesForConversationOnce(conversationId: Long): List<Message> {
        return _messages.filter { it.conversationId == conversationId }
    }

    fun updateMessageContent(messageId: Long, content: String) {
        _messages.replaceAll { msg ->
            if (msg.id == messageId) {
                msg.copy(content = content)
            } else {
                msg
            }
        }
    }

    fun saveMessage(conversationId: Long, content: String, isFromUser: Boolean): Long {
        val newId = (_messages.maxOfOrNull { it.id } ?: 0) + 1
        val newMessage = Message(
            id = newId,
            conversationId = conversationId,
            content = content,
            isFromUser = isFromUser,
            timestamp = System.currentTimeMillis(),
            isStreaming = false
        )
        _messages.add(newMessage)
        return newId
    }
}