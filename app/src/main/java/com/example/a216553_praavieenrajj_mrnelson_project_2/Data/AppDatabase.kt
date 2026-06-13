package com.example.a216553_praavieenrajj_mrnelson_project_2.Data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Envelope::class, FavoriteFoodBank::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun envelopeDao(): EnvelopeDao
    abstract fun foodBankDao(): FavoriteFoodBankDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_budget_db"
                )
                .fallbackToDestructiveMigration() // Simple for development
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
