package com.openrouter.chat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.openrouter.chat.ui.screens.chat.ChatScreen
import com.openrouter.chat.ui.screens.conversations.ConversationsScreen
import com.openrouter.chat.ui.screens.models.ModelsScreen
import com.openrouter.chat.ui.screens.settings.SettingsScreen
import java.net.URLDecoder

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Models.route
    ) {
        composable(Screen.Models.route) {
            ModelsScreen(
                onModelClick = { modelId ->
                    navController.navigate(
                        Screen.Conversations.createRoute(modelId, "Conversations")
                    )
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Conversations.route,
            arguments = listOf(
                navArgument("modelId") { type = NavType.StringType },
                navArgument("modelName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val modelId = backStackEntry.arguments?.getString("modelId")?.let {
                URLDecoder.decode(it, "UTF-8")
            } ?: return@composable
            val modelName = backStackEntry.arguments?.getString("modelName") ?: "Conversations"

            ConversationsScreen(
                modelId = modelId,
                modelName = modelName,
                onConversationClick = { conversationId ->
                    navController.navigate(
                        Screen.Chat.createRoute(conversationId, modelId)
                    )
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("conversationId") { type = NavType.LongType },
                navArgument("modelId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getLong("conversationId") ?: return@composable
            val modelId = backStackEntry.arguments?.getString("modelId")?.let {
                URLDecoder.decode(it, "UTF-8")
            } ?: return@composable

            ChatScreen(
                conversationId = conversationId,
                modelId = modelId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}