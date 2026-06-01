package com.dhanu.kurio.data.storage

import android.content.Context
import com.dhanu.kurio.data.local.dao.HistoryDao
import com.dhanu.kurio.data.local.dao.ModelDao
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class StorageManager(
    private val context: Context,
    private val historyDao: HistoryDao,
    private val modelDao: ModelDao,
    private val modelsDir: String
) {

    /**
     * Returns total storage usage in bytes across models, databases, and temporary audio files.
     */
    suspend fun getStorageUsageBytes(): Long = withContext(Dispatchers.IO) {
        var totalBytes = 0L

        // 1. Model binaries directory
        try {
            val dir = File(modelsDir)
            if (dir.exists()) {
                totalBytes += getDirectorySize(dir)
            }
        } catch (e: Exception) {
            Napier.e(throwable = e, tag = "StorageManager") { "Error reading models directory size" }
        }

        // 2. Application cache directory
        try {
            context.cacheDir?.let {
                totalBytes += getDirectorySize(it)
            }
        } catch (_: Exception) { }

        // 3. Database files size
        try {
            val dbFile = context.getDatabasePath("kurio.db")
            if (dbFile.exists()) {
                totalBytes += dbFile.length()
            }
            val dbWal = context.getDatabasePath("kurio.db-wal")
            if (dbWal.exists()) {
                totalBytes += dbWal.length()
            }
            val dbShm = context.getDatabasePath("kurio.db-shm")
            if (dbShm.exists()) {
                totalBytes += dbShm.length()
            }
        } catch (_: Exception) { }

        totalBytes
    }

    /**
     * Clears all temporary cache and recorded audio files.
     */
    suspend fun clearCache(): Boolean = withContext(Dispatchers.IO) {
        try {
            context.cacheDir?.let { dir ->
                if (dir.exists()) {
                    dir.deleteRecursively()
                    dir.mkdirs()
                }
            }
            Napier.d(tag = "StorageManager") { "Successfully cleared app cache" }
            true
        } catch (e: Exception) {
            Napier.e(throwable = e, tag = "StorageManager") { "Failed to clear app cache" }
            false
        }
    }

    /**
     * Resets the entire app storage: deletes all table entries and model binaries.
     */
    suspend fun resetAll(): Boolean = withContext(Dispatchers.IO) {
        var success = true

        // Clear transcription history entries
        try {
            historyDao.clearAll()
            Napier.d(tag = "StorageManager") { "Database history cleared successfully" }
        } catch (e: Exception) {
            Napier.e(throwable = e, tag = "StorageManager") { "Failed to clear database history" }
            success = false
        }

        // Delete downloaded model binary files from disk
        try {
            val dir = File(modelsDir)
            if (dir.exists()) {
                dir.deleteRecursively()
                dir.mkdirs()
            }
            
            // Reset model metadata status in database
            val models = modelDao.getAll()
            for (model in models) {
                modelDao.updateFilePath(model.id, null)
                modelDao.updateStatus(model.id, "NOT_DOWNLOADED")
            }
            Napier.d(tag = "StorageManager") { "All speech model binaries deleted and status reset" }
        } catch (e: Exception) {
            Napier.e(throwable = e, tag = "StorageManager") { "Failed to delete model binaries" }
            success = false
        }

        success
    }

    private fun getDirectorySize(dir: File): Long {
        var size = 0L
        val files = dir.listFiles() ?: return 0L
        for (file in files) {
            size += if (file.isDirectory) {
                getDirectorySize(file)
            } else {
                file.length()
            }
        }
        return size
    }
}
