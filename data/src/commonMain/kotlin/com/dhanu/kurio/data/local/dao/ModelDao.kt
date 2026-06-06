package com.dhanu.kurio.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dhanu.kurio.data.local.entity.ModelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModelDao {
    @Query("SELECT * FROM models")
    fun observeAll(): Flow<List<ModelEntity>>

    @Query("SELECT * FROM models WHERE id = :modelId")
    fun observeById(modelId: String): Flow<ModelEntity?>

    @Query("SELECT * FROM models WHERE id = :modelId")
    suspend fun getById(modelId: String): ModelEntity?

    @Query("SELECT * FROM models")
    suspend fun getAll(): List<ModelEntity>

    @Query("SELECT * FROM models WHERE engineType = :engineType")
    suspend fun getByEngineType(engineType: String): List<ModelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(model: ModelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(models: List<ModelEntity>)

    @Query("DELETE FROM models WHERE id = :modelId")
    suspend fun deleteById(modelId: String)

    @Query("UPDATE models SET status = :status WHERE id = :modelId")
    suspend fun updateStatus(modelId: String, status: String)

    @Query("UPDATE models SET filePath = :filePath WHERE id = :modelId")
    suspend fun updateFilePath(modelId: String, filePath: String?)

    @Query("UPDATE models SET filePath = :filePath, isDirectory = :isDirectory WHERE id = :modelId")
    suspend fun updateFilePathWithType(modelId: String, filePath: String?, isDirectory: Boolean)

    @Query("SELECT * FROM models WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActive(): ModelEntity?

    @Query("UPDATE models SET status = 'NOT_INSTALLED' WHERE id = :modelId")
    suspend fun resetStatus(modelId: String)
}
