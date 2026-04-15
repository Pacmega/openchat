package com.openrouter.chat.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.openrouter.chat.data.remote.api.OpenRouterApi
import com.openrouter.chat.di.SecurePreferences
import com.openrouter.chat.data.local.dao.ConversationDao
import com.openrouter.chat.data.local.dao.MessageDao
import com.openrouter.chat.data.local.dao.ModelDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

sealed class ValidationResult {
    data object Idle : ValidationResult()
    data object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}

data class SettingsUiState(
    val apiKey: String = "",
    val showClearDialog: Boolean = false,
    val isValidating: Boolean = false,
    val validationResult: ValidationResult = ValidationResult.Idle
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val securePreferences: SecurePreferences,
    private val openRouterApi: OpenRouterApi,
    private val modelDao: ModelDao,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                apiKey = securePreferences.apiKey ?: ""
            )
        }
    }

    fun onApiKeyChange(key: String) {
        _uiState.value = _uiState.value.copy(
            apiKey = key,
            validationResult = ValidationResult.Idle
        )
    }

    fun validateAndSaveApiKey() {
        val key = _uiState.value.apiKey.trim()
        if (key.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isValidating = true,
                validationResult = ValidationResult.Idle
            )

            try {
                val response = openRouterApi.validateKey("Bearer $key")
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.data.label.isNotBlank()) {
                        securePreferences.apiKey = key
                        _uiState.value = _uiState.value.copy(
                            isValidating = false,
                            validationResult = ValidationResult.Valid
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isValidating = false,
                            validationResult = ValidationResult.Invalid("Invalid API key")
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isValidating = false,
                        validationResult = ValidationResult.Invalid("Invalid API key")
                    )
                }
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(
                    isValidating = false,
                    validationResult = ValidationResult.Invalid(
                        when (e.code()) {
                            401 -> "Invalid API key"
                            403 -> "API key forbidden"
                            else -> "Validation failed: ${e.code()}"
                        }
                    )
                )
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(
                    isValidating = false,
                    validationResult = ValidationResult.Invalid("Network error")
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isValidating = false,
                    validationResult = ValidationResult.Invalid("Validation failed")
                )
            }
        }
    }

    fun showClearDialog() {
        _uiState.value = _uiState.value.copy(showClearDialog = true)
    }

    fun hideClearDialog() {
        _uiState.value = _uiState.value.copy(showClearDialog = false)
    }

    fun clearAllData() {
        viewModelScope.launch {
            messageDao.deleteAll()
            conversationDao.deleteAll()
            modelDao.deleteAll()
            securePreferences.clearApiKey()
            _uiState.value = _uiState.value.copy(
                apiKey = "",
                showClearDialog = false,
                validationResult = ValidationResult.Idle
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val hasApiKey = remember {
        derivedStateOf { uiState.apiKey.isNotBlank() }
    }

    if (uiState.showClearDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideClearDialog() },
            title = { Text("Clear All Data?") },
            text = { Text("This will delete all conversations, messages, and your API key.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearAllData() }) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideClearDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "OpenRouter API Key",
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedTextField(
                        value = uiState.apiKey,
                        onValueChange = viewModel::onApiKeyChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your API key") }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = viewModel::validateAndSaveApiKey,
                            enabled = !uiState.isValidating && uiState.apiKey.isNotBlank()
                        ) {
                            if (uiState.isValidating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Save")
                            }
                        }
                    }
                    when (val result = uiState.validationResult) {
                        is ValidationResult.Valid -> {
                            Text(
                                text = "API key is valid!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        is ValidationResult.Invalid -> {
                            Text(
                                text = result.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        is ValidationResult.Idle -> {
                            Text(
                                text = "Get your API key from openrouter.ai",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Data Management",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        onClick = viewModel::showClearDialog,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Clear All Data")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "OpenRouter Chat v1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(
        onBackClick = {}
    )
}