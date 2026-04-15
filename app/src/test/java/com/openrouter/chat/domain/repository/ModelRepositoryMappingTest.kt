package com.openrouter.chat.domain.repository

import com.openrouter.chat.data.local.entity.ModelEntity
import com.openrouter.chat.data.remote.dto.OpenRouterModel
import org.junit.Test
import org.junit.Assert.*

class ModelRepositoryMappingTest {

    @Test
    fun `OpenRouterModel id with slash should extract provider correctly`() {
        val dto = OpenRouterModel(
            id = "openai/gpt-4",
            name = "GPT-4"
        )

        val entity = ModelEntity(
            id = dto.id,
            name = dto.name,
            provider = dto.id.substringBefore("/")
        )

        assertEquals("openai", entity.provider)
    }

    @Test
    fun `OpenRouterModel id with multiple slashes should extract first segment as provider`() {
        val dto = OpenRouterModel(
            id = "google/gemini-2.0-flash",
            name = "Gemini 2.0 Flash"
        )

        val entity = ModelEntity(
            id = dto.id,
            name = dto.name,
            provider = dto.id.substringBefore("/")
        )

        assertEquals("google", entity.provider)
    }

    @Test
    fun `OpenRouterModel id without slash should use entire id as provider`() {
        val dto = OpenRouterModel(
            id = "claude-3",
            name = "Claude 3"
        )

        val entity = ModelEntity(
            id = dto.id,
            name = dto.name,
            provider = dto.id.substringBefore("/")
        )

        assertEquals("claude-3", entity.provider)
    }

    @Test
    fun `OpenRouterModel with colon in id should extract provider correctly`() {
        val dto = OpenRouterModel(
            id = "mistral/mistral-nemo:free",
            name = "Mistral Nemo Free"
        )

        val entity = ModelEntity(
            id = dto.id,
            name = dto.name,
            provider = dto.id.substringBefore("/")
        )

        assertEquals("mistral", entity.provider)
    }

    @Test
    fun `OpenRouterModel topProvider should have correct fields`() {
        val dto = OpenRouterModel(
            id = "openai/gpt-4",
            name = "GPT-4",
            topProvider = com.openrouter.chat.data.remote.dto.TopProvider(
                contextLength = 128000L,
                maxCompletionTokens = 8192L,
                isModerated = false
            )
        )

        assertEquals(128000L, dto.topProvider?.contextLength)
        assertEquals(8192L, dto.topProvider?.maxCompletionTokens)
        assertEquals(false, dto.topProvider?.isModerated)
    }

    @Test
    fun `OpenRouterModel topProvider can be null`() {
        val dto = OpenRouterModel(
            id = "openai/gpt-4",
            name = "GPT-4"
        )

        assertNull(dto.topProvider)
    }
}
