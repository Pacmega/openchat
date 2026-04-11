package com.openrouter.chat.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.openrouter.chat.data.remote.dto.ChatCompletionRequest
import com.openrouter.chat.data.remote.dto.ChatCompletionResponse
import com.openrouter.chat.data.remote.dto.ChatDelta
import com.openrouter.chat.data.remote.dto.ChatMessage
import com.openrouter.chat.di.SecurePreferences
import com.openrouter.chat.domain.model.Conversation
import com.openrouter.chat.domain.model.Message
import com.openrouter.chat.domain.repository.ConversationRepository
import com.openrouter.chat.domain.repository.MessageRepository
import com.openrouter.chat.domain.repository.ModelRepository
import com.openrouter.chat.ui.components.MessageBubble
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okio.Buffer
import javax.inject.Inject

data class ChatUiState(
    val conversation: Conversation? = null,
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isStreaming: Boolean = false,
    val error: String? = null,
    val initialTitleSet: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val modelRepository: ModelRepository,
    private val securePreferences: SecurePreferences,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentConversationId: Long = 0
    private var currentModelId: String = ""
    private var streamingJob: Job? = null

    fun loadChat(conversationId: Long, modelId: String) {
        currentConversationId = conversationId
        currentModelId = modelId

        viewModelScope.launch {
            val conversation = conversationRepository.getConversationById(conversationId)
            _uiState.value = _uiState.value.copy(conversation = conversation)
        }

        viewModelScope.launch {
            messageRepository.getMessagesForConversation(conversationId).collect { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }
    }

    fun onInputChange(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendMessage() {
        val inputText = _uiState.value.inputText.trim()
        if (inputText.isEmpty() || _uiState.value.isStreaming) return

        val apiKey = securePreferences.apiKey
        if (apiKey.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(error = "API key not set. Please configure in Settings.")
            return
        }

        _uiState.value = _uiState.value.copy(
            inputText = "",
            isStreaming = true,
            error = null
        )

        viewModelScope.launch {
            val messageId = messageRepository.saveMessage(
                currentConversationId,
                inputText,
                isFromUser = true
            )

            if (!_uiState.value.initialTitleSet && inputText.isNotBlank()) {
                val title = if (inputText.length > 40) {
                    inputText.take(37) + "..."
                } else {
                    inputText
                }
                conversationRepository.updateTitle(currentConversationId, title)
                modelRepository.updateLastMessage(currentModelId, inputText, System.currentTimeMillis())
                _uiState.value = _uiState.value.copy(initialTitleSet = true)
            }

            streamResponse(inputText, apiKey, messageId)
        }
    }

    private fun streamResponse(userMessage: String, apiKey: String, messageId: Long) {
        viewModelScope.launch {
            try {
                val history = messageRepository.getMessagesForConversationOnce(currentConversationId)
                val messages = history.map { msg ->
                    com.openrouter.chat.data.remote.dto.ChatMessage(
                        role = if (msg.isFromUser) "user" else "assistant",
                        content = msg.content
                    )
                }

                val requestBody = """
                {
                    "model": "$currentModelId",
                    "messages": ${toJson(messages)},
                    "stream": true
                }
                """.trimIndent()

                val request = Request.Builder()
                    .url("https://openrouter.ai/api/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody.toRequestBody("application/json"))
                    .build()

                val fullResponse = StringBuilder()

                okHttpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        viewModelScope.launch {
                            _uiState.value = _uiState.value.copy(
                                isStreaming = false,
                                error = "Connection error: ${e.message}"
                            )
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use { resp ->
                            if (!resp.isSuccessful) {
                                viewModelScope.launch {
                                    _uiState.value = _uiState.value.copy(
                                        isStreaming = false,
                                        error = "Error: ${resp.code}"
                                    )
                                }
                                return
                            }

                            resp.body?.source()?.let { source ->
                                val buffer = Buffer()
                                while (!source.exhausted()) {
                                    source.read(buffer, 8192)
                                    val chunk = buffer.readUtf8()
                                    fullResponse.append(processSseChunk(chunk))

                                    viewModelScope.launch {
                                        messageRepository.updateMessageContent(
                                            messageId,
                                            fullResponse.toString()
                                        )
                                    }
                                }
                            }

                            viewModelScope.launch {
                                messageRepository.markStreamingComplete(
                                    messageId,
                                    fullResponse.toString()
                                )
                                _uiState.value = _uiState.value.copy(isStreaming = false)
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isStreaming = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    private fun processSseChunk(chunk: String): String {
        val content = StringBuilder()
        val lines = chunk.split("\n")
        for (line in lines) {
            if (line.startsWith("data: ")) {
                val data = line.removePrefix("data: ").trim()
                if (data.isNotBlank() && data != "[DONE]") {
                    try {
                        val json = Gson().fromJson(data, ChatCompletionResponse::class.java)
                        json.choices?.firstOrNull()?.delta?.content?.let {
                            content.append(it)
                        }
                    } catch (e: Exception) {
                    }
                }
            }
        }
        return content.toString()
    }

    private fun toJson(messages: List<ChatMessage>): String {
        return messages.joinToString(",", "[", "]") { msg ->
            """{"role":"${msg.role}","content":${Gson().toJson(msg.content)}}"""
        }
    }

    private fun String.toRequestBody(mediaType: String): RequestBody {
        return RequestBody.create(mediaType.toMediaType(), this)
    }

    override fun onCleared() {
        super.onCleared()
        streamingJob?.cancel()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: Long,
    modelId: String,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(conversationId, modelId) {
        viewModel.loadChat(conversationId, modelId)
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.conversation?.title?.take(20) ?: "Chat"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            MessageInput(
                value = uiState.inputText,
                onValueChange = viewModel::onInputChange,
                onSend = {
                    viewModel.sendMessage()
                    focusManager.clearFocus()
                },
                enabled = !uiState.isStreaming
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.error!!,
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.messages) { message ->
                    MessageBubble(
                        content = message.content,
                        isFromUser = message.isFromUser,
                        modifier = Modifier.align(
                            if (message.isFromUser) Alignment.End else Alignment.Start
                        )
                    )
                }

                if (uiState.isStreaming) {
                    item {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message…") },
                enabled = enabled,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                maxLines = 4
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                enabled = enabled && value.isNotBlank()
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}