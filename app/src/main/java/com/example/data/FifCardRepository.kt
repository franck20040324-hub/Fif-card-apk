package com.example.data

import com.example.data.local.CoachDao
import com.example.data.local.CoachEntity
import com.example.data.local.FifCardDatabase
import com.example.data.local.FifProSeedData
import com.example.data.local.LineupDao
import com.example.data.local.LineupSlotEntity
import com.example.data.local.PlayerDao
import com.example.data.local.PlayerEntity
import com.example.data.local.SongDao
import com.example.data.local.SongEntity
import com.example.model.TeamSide
import kotlinx.coroutines.flow.Flow

class FifCardRepository(
    db: FifCardDatabase
) {
    private val playerDao: PlayerDao = db.playerDao()
    private val lineupDao: LineupDao = db.lineupDao()
    private val coachDao: CoachDao = db.coachDao()
    private val songDao: SongDao = db.songDao()

    val playersFlow: Flow<List<PlayerEntity>> = playerDao.observeAllPlayers()
    val lineupSlotsFlow: Flow<List<LineupSlotEntity>> = lineupDao.observeLineupSlots()
    val coachesFlow: Flow<List<CoachEntity>> = coachDao.observeCoaches()
    val songsFlow: Flow<List<SongEntity>> = songDao.observeSongs()

    suspend fun ensureSeeded() {
        if (playerDao.countPlayers() == 0) {
            val seedPlayers = FifProSeedData.initialPlayers()
            playerDao.insertPlayers(seedPlayers)
        }
        if (coachDao.countCoaches() == 0) {
            coachDao.upsertCoaches(
                listOf(
                    CoachEntity(
                        teamSide = TeamSide.HOME.name,
                        name = "Manager Home",
                        nationality = "Spain",
                        flagEmoji = "🇪🇸",
                        customImageUri = null
                    ),
                    CoachEntity(
                        teamSide = TeamSide.AWAY.name,
                        name = "Manager Away",
                        nationality = "France",
                        flagEmoji = "🇫🇷",
                        customImageUri = null
                    )
                )
            )
        }
        if (lineupDao.countSlots() == 0) {
            val players = playerDao.getAllPlayersOnce()
            val homeDefaults = buildInitialSlots(TeamSide.HOME, players)
            val awayDefaults = buildInitialSlots(TeamSide.AWAY, players.drop(11))
            lineupDao.upsertSlots(homeDefaults + awayDefaults)
        }
    }

    private fun buildInitialSlots(side: TeamSide, pool: List<PlayerEntity>): List<LineupSlotEntity> {
        val slots = mutableListOf<LineupSlotEntity>()
        val gk = pool.firstOrNull { it.mainPosition == "GK" }
        val defs = pool.filter { it.mainLine.code == "DEF" }.take(4)
        val mids = pool.filter { it.mainLine.code == "MID" }.take(3)
        val atts = pool.filter { it.mainLine.code == "ATT" }.take(3)
        val picked = listOfNotNull(gk) + defs + mids + atts
        for (i in 0 until 11) {
            slots.add(
                LineupSlotEntity(
                    slotKey = starterKey(side, i),
                    teamSide = side.name,
                    isStarter = true,
                    slotIndex = i,
                    playerId = picked.getOrNull(i)?.id
                )
            )
        }
        // Start with 3 substitution slots; user can add infinite substitution slots
        for (s in 0 until 3) {
            slots.add(
                LineupSlotEntity(
                    slotKey = subKey(side, s),
                    teamSide = side.name,
                    isStarter = false,
                    slotIndex = s,
                    playerId = null
                )
            )
        }
        return slots
    }

    suspend fun assignPlayerToSlot(slotKey: String, playerId: Long?) {
        val currentSlots = lineupDao.getSlotsForTeamOnce(
            if (slotKey.startsWith(TeamSide.AWAY.name)) TeamSide.AWAY.name else TeamSide.HOME.name
        )
        val target = currentSlots.firstOrNull { it.slotKey == slotKey } ?: return
        if (playerId != null) {
            // Avoid duplicate of same player in same team lineup
            val existing = currentSlots.firstOrNull { it.playerId == playerId && it.slotKey != slotKey }
            if (existing != null) {
                lineupDao.upsertSlot(existing.copy(playerId = target.playerId))
            }
        }
        lineupDao.upsertSlot(target.copy(playerId = playerId))
    }

    suspend fun swapSlots(slotKeyA: String, slotKeyB: String) {
        if (slotKeyA == slotKeyB) return
        val allHome = lineupDao.getSlotsForTeamOnce(TeamSide.HOME.name)
        val allAway = lineupDao.getSlotsForTeamOnce(TeamSide.AWAY.name)
        val all = allHome + allAway
        val a = all.firstOrNull { it.slotKey == slotKeyA } ?: return
        val b = all.firstOrNull { it.slotKey == slotKeyB } ?: return
        lineupDao.upsertSlots(
            listOf(
                a.copy(playerId = b.playerId),
                b.copy(playerId = a.playerId)
            )
        )
    }

    suspend fun addSubSlot(teamSide: TeamSide): String {
        val existingSubs = lineupDao.getSlotsForTeamOnce(teamSide.name).filter { !it.isStarter }
        val nextIndex = (existingSubs.maxOfOrNull { it.slotIndex } ?: -1) + 1
        val key = subKey(teamSide, nextIndex)
        lineupDao.upsertSlot(
            LineupSlotEntity(
                slotKey = key,
                teamSide = teamSide.name,
                isStarter = false,
                slotIndex = nextIndex,
                playerId = null
            )
        )
        return key
    }

    suspend fun removeSubSlot(slotKey: String) {
        lineupDao.deleteSlot(slotKey)
    }

    suspend fun savePlayer(player: PlayerEntity): Long {
        return if (player.id == 0L) {
            playerDao.insertPlayer(player)
        } else {
            playerDao.updatePlayer(player)
            player.id
        }
    }

    suspend fun deleteCustomPlayer(playerId: Long) {
        lineupDao.clearPlayerFromAllSlots(playerId)
        playerDao.deleteCustomPlayer(playerId)
    }

    suspend fun resetBuiltInFifProPlayers() {
        val all = playerDao.getAllPlayersOnce()
        val restoredBuiltIn = all.filter { it.isBuiltIn }.map { it.restoredFromSnapshot() }
        if (restoredBuiltIn.isNotEmpty()) {
            playerDao.updatePlayers(restoredBuiltIn)
        }
    }

    suspend fun saveCoach(coach: CoachEntity) {
        coachDao.upsertCoach(coach)
    }

    suspend fun addSong(title: String, uriString: String) {
        songDao.insertSong(SongEntity(title = title, uriString = uriString))
    }

    suspend fun deleteSong(id: Long) {
        songDao.deleteSong(id)
    }

    companion object {
        fun starterKey(side: TeamSide, index: Int): String = "${side.name}_STARTER_$index"
        fun subKey(side: TeamSide, index: Int): String = "${side.name}_SUB_$index"
    }
}
