package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlayerEntity::class,
        LineupSlotEntity::class,
        CoachEntity::class,
        SongEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FifCardDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun lineupDao(): LineupDao
    abstract fun coachDao(): CoachDao
    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var INSTANCE: FifCardDatabase? = null

        fun getInstance(context: Context): FifCardDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FifCardDatabase::class.java,
                    "fif_card_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
