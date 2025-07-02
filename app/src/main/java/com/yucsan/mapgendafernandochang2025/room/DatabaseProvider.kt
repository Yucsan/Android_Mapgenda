package com.yucsan.aventurafernandochang2025.room

import android.content.Context
import androidx.room.Room



object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "aventura_db"
            )
                .fallbackToDestructiveMigration() // 🧨 Permite recrear la DB si cambia el esquema
                .build()
                .also { INSTANCE = it }
        }
    }
}
