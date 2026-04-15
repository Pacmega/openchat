package com.openrouter.chat.ui.screens.settings

import org.junit.Test
import org.junit.Assert.*

class SettingsUiStateTest {

    @Test
    fun `SettingsUiState should have correct defaults`() {
        val state = SettingsUiState()

        assertEquals("", state.apiKey)
        assertFalse(state.showClearDialog)
    }

    @Test
    fun `SettingsUiState copy should allow overriding values`() {
        val state = SettingsUiState()
        val updated = state.copy(
            apiKey = "test-key",
            showClearDialog = true
        )

        assertEquals("test-key", updated.apiKey)
        assertTrue(updated.showClearDialog)
    }
}