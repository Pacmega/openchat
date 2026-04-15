package com.openrouter.chat.data.remote.dto

import org.junit.Test
import org.junit.Assert.*

class ChatMessageDtoTest {

    @Test
    fun `ChatMessage should create with role and content`() {
        val message = ChatMessage(role = "user", content = "Hello")

        assertEquals("user", message.role)
        assertEquals("Hello", message.content)
    }

    @Test
    fun `ChatMessage should support equals`() {
        val msg1 = ChatMessage(role = "user", content = "Hello")
        val msg2 = ChatMessage(role = "user", content = "Hello")

        assertEquals(msg1, msg2)
    }
}

class ChatCompletionRequestTest {

    @Test
    fun `ChatCompletionRequest should create with model and messages`() {
        val messages = listOf(
            ChatMessage(role = "user", content = "Hello")
        )
        val request = ChatCompletionRequest(
            model = "gpt-4",
            messages = messages
        )

        assertEquals("gpt-4", request.model)
        assertEquals(1, request.messages.size)
        assertTrue(request.stream)
    }

    @Test
    fun `ChatCompletionRequest should default stream to true`() {
        val request = ChatCompletionRequest(
            model = "gpt-4",
            messages = emptyList()
        )

        assertTrue(request.stream)
    }
}

class ChatCompletionResponseTest {

    @Test
    fun `ChatCompletionResponse should create with choices`() {
        val response = ChatCompletionResponse(
            id = "chat-1",
            model = "gpt-4",
            choices = listOf(
                Choice(
                    index = 0,
                    message = ChatMessage(role = "assistant", content = "Hello")
                )
            )
        )

        assertEquals("chat-1", response.id)
        assertEquals(1, response.choices?.size)
    }

    @Test
    fun `ChatCompletionResponse choices can be null`() {
        val response = ChatCompletionResponse(
            id = "chat-1",
            model = "gpt-4"
        )

        assertNull(response.choices)
    }
}

class ChoiceTest {

    @Test
    fun `Choice should create with index and message`() {
        val choice = Choice(
            index = 0,
            message = ChatMessage(role = "assistant", content = "Hi")
        )

        assertEquals(0, choice.index)
        assertEquals("assistant", choice.message?.role)
    }

    @Test
    fun `Choice should support delta for streaming`() {
        val choice = Choice(
            index = 0,
            delta = ChatDelta(role = "assistant", content = "Hi")
        )

        assertEquals("assistant", choice.delta?.role)
        assertEquals("Hi", choice.delta?.content)
    }

    @Test
    fun `Choice should have nullable finishReason`() {
        val choice = Choice(
            index = 0,
            message = ChatMessage(role = "assistant", content = "Hi")
        )

        assertNull(choice.finishReason)
    }

    @Test
    fun `Choice should set finishReason when complete`() {
        val choice = Choice(
            index = 0,
            message = ChatMessage(role = "assistant", content = "Hi"),
            finishReason = "stop"
        )

        assertEquals("stop", choice.finishReason)
    }
}

class ChatDeltaTest {

    @Test
    fun `ChatDelta should create with nullable fields`() {
        val delta = ChatDelta(content = "Hello")

        assertEquals("Hello", delta.content)
        assertNull(delta.role)
    }

    @Test
    fun `ChatDelta should support role and content`() {
        val delta = ChatDelta(role = "assistant", content = "Hi")

        assertEquals("assistant", delta.role)
        assertEquals("Hi", delta.content)
    }

    @Test
    fun `ChatDelta should handle null content`() {
        val delta = ChatDelta()

        assertNull(delta.content)
        assertNull(delta.role)
    }
}

class OpenRouterModelDtoTest {

    @Test
    fun `OpenRouterModel should create with required fields`() {
        val model = OpenRouterModel(
            id = "model-1",
            name = "GPT-4"
        )

        assertEquals("model-1", model.id)
        assertEquals("GPT-4", model.name)
    }

    @Test
    fun `OpenRouterModel should have nullable topProvider`() {
        val model = OpenRouterModel(
            id = "model-1",
            name = "GPT-4"
        )

        assertNull(model.topProvider)
    }

    @Test
    fun `OpenRouterModel should parse topProvider correctly`() {
        val model = OpenRouterModel(
            id = "model-1",
            name = "GPT-4",
            topProvider = TopProvider(
                contextLength = 128000L,
                maxCompletionTokens = 8192L,
                isModerated = true
            )
        )

        assertEquals(128000L, model.topProvider?.contextLength)
        assertEquals(8192L, model.topProvider?.maxCompletionTokens)
        assertEquals(true, model.topProvider?.isModerated)
    }

    @Test
    fun `OpenRouterModel should have nullable contextLength`() {
        val model = OpenRouterModel(
            id = "model-1",
            name = "GPT-4"
        )

        assertNull(model.contextLength)
    }

    @Test
    fun `OpenRouterModel should parse contextLength when provided`() {
        val model = OpenRouterModel(
            id = "model-1",
            name = "GPT-4",
            contextLength = 128000L
        )

        assertEquals(128000L, model.contextLength)
    }
}

class TopProviderDtoTest {

    @Test
    fun `TopProvider should create with default values`() {
        val topProvider = TopProvider()

        assertNull(topProvider.contextLength)
        assertNull(topProvider.maxCompletionTokens)
        assertNull(topProvider.isModerated)
    }

    @Test
    fun `TopProvider should parse all fields when provided`() {
        val topProvider = TopProvider(
            contextLength = 128000L,
            maxCompletionTokens = 8192L,
            isModerated = false
        )

        assertEquals(128000L, topProvider.contextLength)
        assertEquals(8192L, topProvider.maxCompletionTokens)
        assertEquals(false, topProvider.isModerated)
    }
}