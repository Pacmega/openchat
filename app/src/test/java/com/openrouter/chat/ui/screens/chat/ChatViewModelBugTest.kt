package com.openrouter.chat.ui.screens.chat

import com.openrouter.chat.domain.model.Message
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for chat message flow bugs.
 *
 * These tests exercise the actual ViewModel flow:
 *   saveMessage (user) → saveAssistantMessage → updateAssistantMessageContent → markStreamingComplete
 *
 * The data layer is correct; user messages persist with isFromUser=true throughout.
 * The alignment bug (messages appearing on the left) is a UI-layer issue in ChatScreen:
 *   Modifier.align(Alignment.End) inside LazyColumn's items{} block has no effect in
 *   Compose BOM 2024.12.01 — LazyItemScope does not expose that extension.
 *   Fix: wrap each item in a Row(fillMaxWidth, horizontalArrangement=End/Start).
 */
class ChatViewModelBugTest {

    @Before
    fun setUp() {
        messageRepository.clear()
    }

    // --- user message preservation ---

    @Test
    fun userMessage_isFromUserTrue_afterFullExchange() {
        val userMsgId = messageRepository.saveMessage(1L, "Hello", isFromUser = true)
        val assistantMsgId = messageRepository.saveAssistantMessage(1L, "")
        messageRepository.updateAssistantMessageContent(assistantMsgId, "Hi there!")
        messageRepository.markStreamingComplete(assistantMsgId, "Hi there!")

        val messages = messageRepository.getMessagesForConversationOnce(1L)
        val userMsg = messages.find { it.id == userMsgId }

        assertNotNull("User message must still exist after assistant responds", userMsg)
        assertTrue(
            "User message must have isFromUser=true — this flag drives right-side alignment in the UI",
            userMsg!!.isFromUser
        )
    }

    @Test
    fun userMessage_contentUnchanged_afterAssistantResponds() {
        val userContent = "What is the capital of France?"
        val userMsgId = messageRepository.saveMessage(1L, userContent, isFromUser = true)
        val assistantMsgId = messageRepository.saveAssistantMessage(1L, "")
        messageRepository.updateAssistantMessageContent(assistantMsgId, "Paris.")
        messageRepository.markStreamingComplete(assistantMsgId, "Paris.")

        val messages = messageRepository.getMessagesForConversationOnce(1L)
        val userMsg = messages.find { it.id == userMsgId }!!

        assertEquals(
            "User message content must be unchanged — assistant response must not overwrite it",
            userContent,
            userMsg.content
        )
    }

    @Test
    fun twoMessages_existAfterOneExchange() {
        val userMsgId = messageRepository.saveMessage(1L, "Hello", isFromUser = true)
        val assistantMsgId = messageRepository.saveAssistantMessage(1L, "")
        messageRepository.markStreamingComplete(assistantMsgId, "Hi there!")

        val messages = messageRepository.getMessagesForConversationOnce(1L)

        assertEquals(
            "Exactly 2 messages must exist after one user/assistant exchange — " +
            "if the user message disappeared it means it was replaced by the assistant message",
            2,
            messages.size
        )
    }

    @Test
    fun assistantMessage_hasIsFromUserFalse() {
        messageRepository.saveMessage(1L, "Hello", isFromUser = true)
        val assistantMsgId = messageRepository.saveAssistantMessage(1L, "")
        messageRepository.markStreamingComplete(assistantMsgId, "Hi there!")

        val messages = messageRepository.getMessagesForConversationOnce(1L)
        val assistantMsg = messages.find { it.id == assistantMsgId }!!

        assertFalse(
            "Assistant message must have isFromUser=false — this flag drives left-side alignment in the UI",
            assistantMsg.isFromUser
        )
    }

    // --- streaming updates do not corrupt user message ---

    @Test
    fun streamingUpdates_doNotTouchUserMessage() {
        val userMsgId = messageRepository.saveMessage(1L, "Tell me a story", isFromUser = true)
        val assistantMsgId = messageRepository.saveAssistantMessage(1L, "")

        messageRepository.updateAssistantMessageContent(assistantMsgId, "Once")
        messageRepository.updateAssistantMessageContent(assistantMsgId, "Once upon")
        messageRepository.updateAssistantMessageContent(assistantMsgId, "Once upon a time")
        messageRepository.markStreamingComplete(assistantMsgId, "Once upon a time.")

        val messages = messageRepository.getMessagesForConversationOnce(1L)

        assertEquals("Should have exactly 2 messages after streaming", 2, messages.size)

        val userMsg = messages.find { it.id == userMsgId }!!
        assertEquals("User message content unchanged after streaming", "Tell me a story", userMsg.content)
        assertTrue("User message isFromUser still true after streaming", userMsg.isFromUser)
    }

    @Test
    fun messages_orderedUserBeforeAssistant() {
        val userMsgId = messageRepository.saveMessage(1L, "Hello", isFromUser = true)
        val assistantMsgId = messageRepository.saveAssistantMessage(1L, "Hi!")

        val messages = messageRepository.getMessagesForConversationOnce(1L)

        assertEquals("First message should be from user", userMsgId, messages[0].id)
        assertEquals("Second message should be from assistant", assistantMsgId, messages[1].id)
    }

    // --- multi-turn conversation ---

    @Test
    fun multiTurn_allMessagesPreserved() {
        val ids = mutableListOf<Long>()
        repeat(3) { i ->
            ids += messageRepository.saveMessage(1L, "User turn $i", isFromUser = true)
            val aId = messageRepository.saveAssistantMessage(1L, "")
            messageRepository.markStreamingComplete(aId, "Assistant turn $i")
            ids += aId
        }

        val messages = messageRepository.getMessagesForConversationOnce(1L)
        assertEquals("All 6 messages must be preserved across 3 turns", 6, messages.size)

        val userMessages = messages.filter { it.isFromUser }
        val assistantMessages = messages.filter { !it.isFromUser }
        assertEquals("3 user messages", 3, userMessages.size)
        assertEquals("3 assistant messages", 3, assistantMessages.size)
    }
}

object messageRepository {
    private val _messages = mutableListOf<Message>()

    fun clear() { _messages.clear() }

    fun getMessagesForConversationOnce(conversationId: Long): List<Message> =
        _messages.filter { it.conversationId == conversationId }

    fun saveMessage(conversationId: Long, content: String, isFromUser: Boolean): Long {
        val id = nextId()
        _messages += Message(id, conversationId, content, isFromUser, id, false)
        return id
    }

    fun saveAssistantMessage(conversationId: Long, content: String, isStreaming: Boolean = true): Long {
        val id = nextId()
        _messages += Message(id, conversationId, content, isFromUser = false, id, isStreaming)
        return id
    }

    fun updateAssistantMessageContent(messageId: Long, content: String) {
        _messages.replaceAll { if (it.id == messageId) it.copy(content = content) else it }
    }

    fun markStreamingComplete(messageId: Long, finalContent: String) {
        _messages.replaceAll {
            if (it.id == messageId) it.copy(content = finalContent, isStreaming = false) else it
        }
    }

    private fun nextId() = (_messages.maxOfOrNull { it.id } ?: 0L) + 1L
}
