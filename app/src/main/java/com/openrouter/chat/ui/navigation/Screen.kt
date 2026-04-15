package com.openrouter.chat.ui.navigation

sealed class Screen(val route: String) {
    object Models : Screen("models")
    object Conversations : Screen("conversations/{modelId}/{modelName}") {
        fun createRoute(modelId: String, modelName: String) =
            "conversations/${java.net.URLEncoder.encode(modelId, "UTF-8")}/$modelName"
    }
    object Chat : Screen("chat/{conversationId}/{modelId}") {
        fun createRoute(conversationId: Long, modelId: String) =
            "chat/$conversationId/${java.net.URLEncoder.encode(modelId, "UTF-8")}"
    }
    object Settings : Screen("settings")
}