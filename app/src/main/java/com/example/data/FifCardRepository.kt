package com.example.data

import com.example.data.local.CoachDao
import com.example.data.local.CoachEntity
import com.example.data.local.FifCardDatabase
import com.example.data.local.FifProSeedData
import com.example.data.local.LineupDao
import com.example.data.local.LineupSlotEntity
import com.example.data.local.PlayerDao
import com.example.data.local.PlayerEntity
import com.example.data.local.SavedFormationDao
import com.example.data.local.SavedFormationEntity
import com.example.data.local.SongDao
import com.example.data.local.SongEntity
import com.example.model.TeamSide
import com.example.ui.SlotContext
import kotlinx.coroutines.flow.Flow

class FifCardRepository(
    db: FifCardDatabase
) {
    private val playerDao: PlayerDao = db.playerDao()
    private val lineupDao: LineupDao = db.lineupDao()
    private val savedFormationDao: SavedFormationDao = db.savedFormationDao()
    private val coachDao: CoachDao = db.coachDao()
    private val songDao: SongDao = db.songDao()

    val playersFlow: Flow<List<PlayerEntity>> = playerDao.observeAllPlayers()
    val lineupSlotsFlow: Flow<List<LineupSlotEntity>> = lineupDao.observeLineupSlots()
    val savedFormationsFlow: Flow<List<SavedFormationEntity>> = savedFormationDao.observeSavedFormations()
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

    suspend fun updateSlotCoordinates(slotKey: String, x: Float?, y: Float?) {
        val side = if (slotKey.startsWith(TeamSide.AWAY.name)) TeamSide.AWAY.name else TeamSide.HOME.name
        val slot = lineupDao.getSlotsForTeamOnce(side).firstOrNull { it.slotKey == slotKey } ?: return
        lineupDao.upsertSlot(slot.copy(customX = x, customY = y))
    }

    suspend fun clearCustomPositionsForTeam(side: TeamSide) {
        lineupDao.clearCustomPositionsForTeam(side.name)
    }

    suspend fun saveFormationLocally(
        name: String,
        baseFormationId: String,
        starterSlots: List<SlotContext>
    ) {
        val packed = starterSlots.joinToString(";") { slot ->
            val pid = slot.player?.id?.toString() ?: ""
            "${slot.slotIndex},${slot.slotPosition.code},${slot.x},${slot.y},$pid"
        }
        savedFormationDao.insertSavedFormation(
            SavedFormationEntity(
                name = name.trim().ifEmpty { baseFormationId },
                baseFormationId = baseFormationId,
                slotsData = packed
            )
        )
    }

    suspend fun loadSavedFormation(side: TeamSide, saved: SavedFormationEntity) {
        val existingSlots = lineupDao.getSlotsForTeamOnce(side.name).associateBy { it.slotKey }
        val updated = mutableListOf<LineupSlotEntity>()
        saved.slotsData.split(";").forEach { token ->
            val parts = token.split(",")
            if (parts.size >= 5) {
                val idx = parts[0].toIntOrNull() ?: return@forEach
                val x = parts[2].toFloatOrNull()
                val y = parts[3].toFloatOrNull()
                val pid = parts[4].toLongOrNull()
                val key = starterKey(side, idx)
                val base = existingSlots[key] ?: LineupSlotEntity(
                    slotKey = key,
                    teamSide = side.name,
                    isStarter = true,
                    slotIndex = idx,
                    playerId = null
                )
                updated.add(base.copy(playerId = pid, customX = x, customY = y))
            }
        }
        if (updated.isNotEmpty()) {
            lineupDao.upsertSlots(updated)
        }
    }

    suspend fun deleteSavedFormation(id: Long) {
        savedFormationDao.deleteSavedFormation(id)
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
