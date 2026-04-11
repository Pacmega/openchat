package com.openrouter.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalTextApi::class)
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current
) {
    val annotatedString = buildAnnotatedString {
        var currentIndex = 0
        val pattern = Regex("(\\*\\*[^*]+\\*\\*|\\*[^*]+\\*|`[^`]+`|```[\\s\\S]*?```)")

        val matches = pattern.findAll(text)
        for (match in matches) {
            if (match.range.first > currentIndex) {
                append(text.substring(currentIndex, match.range.first))
            }
            when {
                match.value.startsWith("```") && match.value.endsWith("```") -> {
                    withStyle(style = SpanStyle(fontFamily = FontFamily.Monospace)) {
                        append(match.value.removeSurrounding("```"))
                    }
                    append("\n")
                }
                match.value.startsWith("`") && match.value.endsWith("`") -> {
                    withStyle(style = SpanStyle(fontFamily = FontFamily.Monospace)) {
                        append(match.value.removeSurrounding("`"))
                    }
                }
                match.value.startsWith("**") && match.value.endsWith("**") -> {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(match.value.removeSurrounding("**"))
                    }
                }
                match.value.startsWith("*") && match.value.endsWith("*") -> {
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(match.value.removeSurrounding("*"))
                    }
                }
                else -> {
                    append(match.value)
                }
            }
            currentIndex = match.range.last + 1
        }
        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        style = style
    )
}

@Composable
fun MessageBubble(
    content: String,
    isFromUser: Boolean,
    modifier: Modifier = Modifier
) {
    val bubbleColor = if (isFromUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val shape = RoundedCornerShape(
        topStart = 18f,
        topEnd = 18f,
        bottomStart = if (isFromUser) 18f else 4f,
        bottomEnd = if (isFromUser) 4f else 18f
    )

    Box(
        modifier = modifier
            .widthIn(max = 280.dp)
            .clip(shape)
            .background(bubbleColor)
            .padding(12.dp)
    ) {
        MarkdownText(
            text = content,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}