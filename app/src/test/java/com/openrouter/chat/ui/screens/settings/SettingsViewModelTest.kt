package com.openrouter.chat.ui.screens.settings

import org.junit.Test
import org.junit.Assert.*

class SettingsUiStateTest {

    @Test
    fun `SettingsUiState should have correct defaults`() {
        val state = SettingsUiState()

        assertEquals("", state.apiKey)
        assertFalse(state.showClearDialog)
        assertFalse(state.isValidating)
        assertEquals(ValidationResult.Idle, state.validationResult)
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

    @Test
    fun `SettingsUiState should track validation state`() {
        val state = SettingsUiState()

        assertFalse(state.isValidating)
        assertEquals(ValidationResult.Idle, state.validationResult)

        val loading = state.copy(isValidating = true)
        assertTrue(loading.isValidating)
        assertEquals(ValidationResult.Idle, loading.validationResult)

        val success = loading.copy(isValidating = false, validationResult = ValidationResult.Valid)
        assertFalse(success.isValidating)
        assertEquals(ValidationResult.Valid, success.validationResult)

        val error = state.copy(isValidating = false, validationResult = ValidationResult.Invalid("Error"))
        assertFalse(error.isValidating)
        assertTrue(error.validationResult is ValidationResult.Invalid)
    }
}

sealed class ValidationResult {
    data object Idle : ValidationResult()
    data object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}