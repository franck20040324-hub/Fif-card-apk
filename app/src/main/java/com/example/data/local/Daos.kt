package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY id ASC")
    fun observeAllPlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players")
    suspend fun getAllPlayersOnce(): List<PlayerEntity>

    @Query("SELECT COUNT(*) FROM players")
    suspend fun countPlayers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<PlayerEntity>): List<Long>

    @Update
    suspend fun updatePlayer(player: PlayerEntity)

    @Update
    suspend fun updatePlayers(players: List<PlayerEntity>)

    @Query("DELETE FROM players WHERE id = :id AND isBuiltIn = 0")
    suspend fun deleteCustomPlayer(id: Long)
}

@Dao
interface LineupDao {
    @Query("SELECT * FROM lineup_slots ORDER BY teamSide ASC, isStarter DESC, slotIndex ASC")
    fun observeLineupSlots(): Flow<List<LineupSlotEntity>>

    @Query("SELECT * FROM lineup_slots WHERE teamSide = :teamSide ORDER BY isStarter DESC, slotIndex ASC")
    suspend fun getSlotsForTeamOnce(teamSide: String): List<LineupSlotEntity>

    @Query("SELECT COUNT(*) FROM lineup_slots")
    suspend fun countSlots(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSlot(slot: LineupSlotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSlots(slots: List<LineupSlotEntity>)

    @Query("DELETE FROM lineup_slots WHERE slotKey = :slotKey")
    suspend fun deleteSlot(slotKey: String)

    @Query("UPDATE lineup_slots SET playerId = NULL WHERE playerId = :playerId")
    suspend fun clearPlayerFromAllSlots(playerId: Long)
}

@Dao
interface CoachDao {
    @Query("SELECT * FROM coaches")
    fun observeCoaches(): Flow<List<CoachEntity>>

    @Query("SELECT COUNT(*) FROM coaches")
    suspend fun countCoaches(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCoach(coach: CoachEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCoaches(coaches: List<CoachEntity>)
}

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY addedAt ASC, id ASC")
    fun observeSongs(): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity): Long

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSong(id: Long)
}
