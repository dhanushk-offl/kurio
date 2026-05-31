package com.dhanu.kurio.data

import com.dhanu.kurio.data.local.KurioDatabase

fun createDatabase(): KurioDatabase {
    throw IllegalStateException("Room is not available on iOS")
}
