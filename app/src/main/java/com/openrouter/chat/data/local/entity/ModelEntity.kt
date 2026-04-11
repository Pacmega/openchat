package com.openrouter.chat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "models")
data class ModelEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val provider: String,
    val lastMessage: String? = null,
    val lastMessageTimestamp: Long? = null
)