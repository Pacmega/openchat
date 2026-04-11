package com.openrouter.chat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.openrouter.chat.data.local.dao.ConversationDao
import com.openrouter.chat.data.local.dao.MessageDao
import com.openrouter.chat.data.local.dao.ModelDao
import com.openrouter.chat.data.local.entity.ConversationEntity
import com.openrouter.chat.data.local.entity.MessageEntity
import com.openrouter.chat.data.local.entity.ModelEntity

@Database(
    entities = [ModelEntity::class, ConversationEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun modelDao(): ModelDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}