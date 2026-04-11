package com.openrouter.chat.data.local.dao

import androidx.room.*
import com.openrouter.chat.data.local.entity.ModelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModelDao {
    @Query("SELECT * FROM models ORDER BY name ASC")
    fun getAllModels(): Flow<List<ModelEntity>>

    @Query("SELECT * FROM models WHERE id = :id")
    suspend fun getModelById(id: String): ModelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModels(models: List<ModelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(model: ModelEntity)

    @Query("UPDATE models SET lastMessage = :lastMessage, lastMessageTimestamp = :timestamp WHERE id = :modelId")
    suspend fun updateLastMessage(modelId: String, lastMessage: String?, timestamp: Long?)

    @Query("DELETE FROM models")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(models: List<ModelEntity>) {
        deleteAll()
        insertModels(models)
    }
}