package com.dhanu.kurio.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dhanu.kurio.data.local.entity.HistoryEntity
import com.dhanu.kurio.data.local.entity.ModelEntity
import com.dhanu.kurio.data.local.dao.HistoryDao
import com.dhanu.kurio.data.local.dao.ModelDao

@Database(
    entities = [HistoryEntity::class, ModelEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KurioDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun modelDao(): ModelDao
}
