package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlayerEntity::class,
        LineupSlotEntity::class,
        SavedFormationEntity::class,
        CoachEntity::class,
        SongEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class FifCardDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun lineupDao(): LineupDao
    abstract fun savedFormationDao(): SavedFormationDao
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
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
