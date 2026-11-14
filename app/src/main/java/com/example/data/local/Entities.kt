package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.AvatarGender
import com.example.model.PlayingLine
import com.example.model.PositionCode
import com.example.model.PreferredFoot

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val nickname: String = "",
    val nationality: String,
    val flagEmoji: String,
    val club: String,
    val league: String,
    val mainPosition: String,
    val secondaryPositions: String = "",
    val avatarGender: String = AvatarGender.MALE.name,
    val customImageUri: String? = null,
    val heightCm: Int = 180,
    val weightKg: Int = 75,
    val preferredFoot: String = PreferredFoot.RIGHT.code,
    val pace: Int = 78,
    val shooting: Int = 75,
    val passing: Int = 77,
    val dribbling: Int = 78,
    val defending: Int = 65,
    val physical: Int = 75,
    val gkStat: Int = 20,
    val powerGk: Int = 45,
    val powerDef: Int = 70,
    val powerMid: Int = 76,
    val powerAtt: Int = 76,
    val isBuiltIn: Boolean = false,
    val defaultSnapshot: String = ""
) {
    val mainPositionCode: PositionCode
        get() = PositionCode.fromCode(mainPosition)

    val mainLine: PlayingLine
        get() = mainPositionCode.line

    val extraPositionList: List<PositionCode>
        get() = secondaryPositions
            .split(",")
            .mapNotNull { token ->
                val trimmed = token.trim()
                if (trimmed.isEmpty()) null else PositionCode.fromCode(trimmed)
            }
            .distinct()
            .filter { it != mainPositionCode }
            .take(3)

    val naturalPower: Int
        get() = powerForLine(mainLine)

    fun powerForLine(line: PlayingLine): Int {
        return when (line) {
            PlayingLine.GK -> powerGk
            PlayingLine.DEF -> powerDef
            PlayingLine.MID -> powerMid
            PlayingLine.ATT -> powerAtt
        }.coerceIn(1, 99)
    }

    fun powerForSlot(slotPosition: PositionCode): Int {
        val base = powerForLine(slotPosition.line)
        // Slight tactical chemistry bonus if exact natural or secondary match
        return when {
            slotPosition == mainPositionCode -> base
            extraPositionList.contains(slotPosition) -> base
            else -> base
        }.coerceIn(1, 99)
    }

    fun packDefaults(): String {
        return listOf(
            mainPosition,
            secondaryPositions,
            pace,
            shooting,
            passing,
            dribbling,
            defending,
            physical,
            gkStat,
            powerGk,
            powerDef,
            powerMid,
            powerAtt
        ).joinToString("|")
    }

    fun restoredFromSnapshot(): PlayerEntity {
        if (!isBuiltIn || defaultSnapshot.isBlank()) return this
        val parts = defaultSnapshot.split("|")
        if (parts.size < 13) return this
        return copy(
            mainPosition = parts[0],
            secondaryPositions = parts[1],
            pace = parts[2].toIntOrNull() ?: pace,
            shooting = parts[3].toIntOrNull() ?: shooting,
            passing = parts[4].toIntOrNull() ?: passing,
            dribbling = parts[5].toIntOrNull() ?: dribbling,
            defending = parts[6].toIntOrNull() ?: defending,
            physical = parts[7].toIntOrNull() ?: physical,
            gkStat = parts[8].toIntOrNull() ?: gkStat,
            powerGk = parts[9].toIntOrNull() ?: powerGk,
            powerDef = parts[10].toIntOrNull() ?: powerDef,
            powerMid = parts[11].toIntOrNull() ?: powerMid,
            powerAtt = parts[12].toIntOrNull() ?: powerAtt
        )
    }
}

@Entity(tableName = "lineup_slots")
data class LineupSlotEntity(
    @PrimaryKey val slotKey: String,
    val teamSide: String, // "HOME" or "AWAY"
    val isStarter: Boolean,
    val slotIndex: Int,
    val playerId: Long?
)

@Entity(tableName = "coaches")
data class CoachEntity(
    @PrimaryKey val teamSide: String, // "HOME" or "AWAY"
    val name: String,
    val nationality: String,
    val flagEmoji: String,
    val customImageUri: String? = null
)

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val uriString: String,
    val addedAt: Long = System.currentTimeMillis()
)
