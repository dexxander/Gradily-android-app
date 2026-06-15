package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, Subject::class, Student::class, Assessment::class], version = 2, exportSchema = false)
abstract class GradilyDatabase : RoomDatabase() {
    abstract fun gradilyDao(): GradilyDao

    companion object {
        @Volatile
        private var INSTANCE: GradilyDatabase? = null

        fun getDatabase(context: Context): GradilyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GradilyDatabase::class.java,
                    "gradily_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
