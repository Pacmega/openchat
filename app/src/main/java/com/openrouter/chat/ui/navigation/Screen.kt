package com.openrouter.chat.ui.navigation

sealed class Screen(val route: String) {
    object Models : Screen("models")
    object Conversations : Screen("conversations/{modelId}/{modelName}") {
        fun createRoute(modelId: String, modelName: String) = "conversations/$modelId/$modelName"
    }
    object Chat : Screen("chat/{conversationId}/{modelId}") {
        fun createRoute(conversationId: Long, modelId: String) = "chat/$conversationId/$modelId"
    }
    object Settings : Screen("settings")
}