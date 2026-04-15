package com.openrouter.chat.ui.screens.models

import com.openrouter.chat.domain.model.AIModel
import org.junit.Test
import org.junit.Assert.*

class ModelsUiStateTest {

    @Test
    fun `ModelsUiState should have correct defaults`() {
        val state = ModelsUiState()

        assertTrue(state.models.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `ModelsUiState copy should allow overriding values`() {
        val state = ModelsUiState()
        val models = listOf(AIModel(id = "1", name = "GPT-4", provider = "OpenAI"))
        val updated = state.copy(
            models = models,
            isLoading = true
        )

        assertEquals(1, updated.models.size)
        assertTrue(updated.isLoading)
    }
}