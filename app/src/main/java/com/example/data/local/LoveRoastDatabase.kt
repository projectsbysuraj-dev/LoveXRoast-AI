package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.LoveRoastItem

@Database(entities = [LoveRoastItem::class], version = 1, exportSchema = false)
abstract class LoveRoastDatabase : RoomDatabase() {
    abstract fun loveRoastDao(): LoveRoastDao

    companion object {
        @Volatile
        private var INSTANCE: LoveRoastDatabase? = null

        fun getDatabase(context: Context): LoveRoastDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LoveRoastDatabase::class.java,
                    "love_roast_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
