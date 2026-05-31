package com.dhanu.kurio.core.extension

import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlin.math.max
import kotlin.math.min

fun String?.orEmpty(): String = this ?: ""

fun Long?.orZero(): Long = this ?: 0L

fun <T> Flow<T>.withErrorLogging(tag: String): Flow<T> = this.catch { e ->
    Napier.e(tag, throwable = e) { "Flow error" }
    throw e
}

fun Int.clamp(low: Int, high: Int): Int = max(low, min(high, this))

fun String.truncate(maxLength: Int): String {
    return if (this.length <= maxLength) this else "${this.take(maxLength - 3)}..."
}
