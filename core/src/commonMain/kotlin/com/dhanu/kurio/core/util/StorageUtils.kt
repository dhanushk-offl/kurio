package com.dhanu.kurio.core.util

import kotlin.math.pow
import kotlin.math.round

object StorageUtils {
    private fun roundTo(value: Double, decimals: Int): String {
        val factor = 10.0.pow(decimals)
        val rounded = round(value * factor) / factor
        val parts = rounded.toString().split(".")
        val intPart = parts[0]
        val decPart = if (parts.size > 1) parts[1] else ""
        return "$intPart.${decPart.padEnd(decimals, '0').take(decimals)}"
    }

    fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> {
                val mb = bytes.toDouble() / (1024 * 1024)
                "${roundTo(mb, 1)} MB"
            }
            else -> {
                val gb = bytes.toDouble() / (1024 * 1024 * 1024)
                "${roundTo(gb, 2)} GB"
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
