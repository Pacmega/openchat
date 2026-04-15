package com.openrouter.chat.ui.screens.chat

import org.junit.Test
import org.junit.Assert.*

class ChatUiStateTest {

    @Test
    fun `ChatUiState should have correct defaults`() {
        val state = ChatUiState()

        assertNull(state.conversation)
        assertTrue(state.messages.isEmpty())
        assertEquals("", state.inputText)
        assertFalse(state.isLoading)
        assertFalse(state.isStreaming)
        assertNull(state.error)
        assertFalse(state.initialTitleSet)
    }

    @Test
    fun `ChatUiState copy should allow overriding values`() {
        val state = ChatUiState()
        val updated = state.copy(
            inputText = "Test",
            isLoading = true
        )

        assertEquals("Test", updated.inputText)
        assertTrue(updated.isLoading)
        assertFalse(updated.isStreaming)
    }
}