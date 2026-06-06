package com.dhanu.kurio.data

import android.content.Context
import androidx.room.Room
import com.dhanu.kurio.data.local.KurioDatabase

fun createDatabase(context: Context): KurioDatabase {
    return Room.databaseBuilder(
        context.applicationContext,
        KurioDatabase::class.java,
        "kurio.db"
    ).fallbackToDestructiveMigration()
     .build()
}
