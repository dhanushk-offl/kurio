package com.dhanu.kurio.core.util

object StorageUtils {
    fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> {
                val mb = bytes.toDouble() / (1024 * 1024)
                "${"%.1f".format(mb)} MB"
            }
            else -> {
                val gb = bytes.toDouble() / (1024 * 1024 * 1024)
                "${"%.2f".format(gb)} GB"
            }
        }
    }

    fun hasSufficientSpace(bytes: Long): Boolean {
        return bytes > 0
    }

    fun calculateModelStorageRequired(sizeBytes: Long): Long {
        return sizeBytes + (sizeBytes * 0.1).toLong()
    }
}
