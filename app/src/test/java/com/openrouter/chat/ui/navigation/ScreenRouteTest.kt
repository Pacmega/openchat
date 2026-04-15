package com.openrouter.chat.ui.navigation

import org.junit.Test
import org.junit.Assert.*

class ScreenRouteTest {

    @Test
    fun `Conversations route should URL-encode modelId with slash`() {
        val modelId = "allenai/olmo-2-0325-32b-instruct"
        val route = Screen.Conversations.createRoute(modelId, "Conversations")

        assertFalse(route.contains("allenai/olmo"))
        assertTrue(route.contains("allenai%2Folmo"))
        assertEquals("conversations/allenai%2Folmo-2-0325-32b-instruct/Conversations", route)
    }

    @Test
    fun `Chat route should URL-encode modelId with slash`() {
        val modelId = "anthropic/claude-3-opus"
        val route = Screen.Chat.createRoute(1L, modelId)

        assertFalse(route.contains("anthropic/claude"))
        assertTrue(route.contains("anthropic%2Fclaude"))
        assertEquals("chat/1/anthropic%2Fclaude-3-opus", route)
    }

    @Test
    fun `route should work with modelId without special characters`() {
        val modelId = "gpt-4"
        val convRoute = Screen.Conversations.createRoute(modelId, "Conv")
        val chatRoute = Screen.Chat.createRoute(1L, modelId)

        assertEquals("conversations/gpt-4/Conv", convRoute)
        assertEquals("chat/1/gpt-4", chatRoute)
    }
}