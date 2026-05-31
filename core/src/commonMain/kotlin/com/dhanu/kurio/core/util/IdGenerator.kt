package com.dhanu.kurio.core.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object IdGenerator {
    fun generate(): String {
        val now = Clock.System.now()
        val random = (1000..9999).random()
        return "${now.toEpochMilliseconds()}-$random"
    }
}
