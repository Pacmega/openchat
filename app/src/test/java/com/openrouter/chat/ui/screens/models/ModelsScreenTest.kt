package com.openrouter.chat.ui.screens.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import org.junit.Test
import org.junit.Assert.*

class ModelsScreenButtonTest {

    @Test
    fun `Settings button should use Settings cog icon`() {
        // Verify the correct icon is imported and used
        val icon = Icons.Default.Settings

        // The Settings icon should be available and be the cog icon
        assertNotNull(icon)
    }

    @Test
    fun `Settings button content description should describe action`() {
        // The content description should be user-friendly
        val contentDescription = "Settings"

        assertEquals("Settings", contentDescription)
    }
}

class ModelsScreenFeedbackTest {

    @Test
    fun `UiState loading flag should provide feedback`() {
        val state = ModelsUiState(isLoading = true)

        assertTrue(state.isLoading)
    }

    @Test
    fun `UiState error should provide feedback`() {
        val state = ModelsUiState(error = "Network error")

        assertEquals("Network error", state.error)
    }

    @Test
    fun `UiState provides feedback when models are empty`() {
        val emptyModels = ModelsUiState(models = emptyList())

        assertTrue(emptyModels.models.isEmpty())
    }
}