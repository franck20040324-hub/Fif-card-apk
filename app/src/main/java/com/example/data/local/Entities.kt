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
    val abilityNicknames: String = "",
    val nationality: String,
    val flagEmoji: String = "",
    val club: String,
    val league: String,
    val mainPosition: String,
    val secondaryPositions: String = "",
    val avatarGender: String = AvatarGender.MALE.name,
    val customImageUri: String? = null,
    val customClubLogoUri: String? = null,
    val preferredFoot: String = PreferredFoot.RIGHT.code,
    val pace: Int = 78,
    val shooting: Int = 75,
    val passing: Int = 77,
    val dribbling: Int = 78,
    val defending: Int = 65,
    val physical: Int = 75,
    val heading: Int = 72,
    val crossing: Int = 72,
    val tackling: Int = 65,
    val stamina: Int = 78,
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
            .take(5)

    val abilityNicknameList: List<String>
        get() = abilityNicknames
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

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
        return powerForLine(slotPosition.line).coerceIn(1, 99)
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
            heading,
            crossing,
            tackling,
            stamina,
            gkStat,
            powerGk,
            powerDef,
            powerMid,
            powerAtt,
            abilityNicknames
        ).joinToString("|")
    }

    fun restoredFromSnapshot(): PlayerEntity {
        if (!isBuiltIn || defaultSnapshot.isBlank()) return this
        val parts = defaultSnapshot.split("|")
        if (parts.size < 18) return this
        return copy(
            mainPosition = parts[0],
            secondaryPositions = parts[1],
            pace = parts[2].toIntOrNull() ?: pace,
            shooting = parts[3].toIntOrNull() ?: shooting,
            passing = parts[4].toIntOrNull() ?: passing,
            dribbling = parts[5].toIntOrNull() ?: dribbling,
            defending = parts[6].toIntOrNull() ?: defending,
            physical = parts[7].toIntOrNull() ?: physical,
            heading = parts[8].toIntOrNull() ?: heading,
            crossing = parts[9].toIntOrNull() ?: crossing,
            tackling = parts[10].toIntOrNull() ?: tackling,
            stamina = parts[11].toIntOrNull() ?: stamina,
            gkStat = parts[12].toIntOrNull() ?: gkStat,
            powerGk = parts[13].toIntOrNull() ?: powerGk,
            powerDef = parts[14].toIntOrNull() ?: powerDef,
            powerMid = parts[15].toIntOrNull() ?: powerMid,
            powerAtt = parts[16].toIntOrNull() ?: powerAtt,
            abilityNicknames = parts[17]
        )
    }
}

@Entity(tableName = "lineup_slots")
data class LineupSlotEntity(
    @PrimaryKey val slotKey: String,
    val teamSide: String, // "HOME"
    val isStarter: Boolean,
    val slotIndex: Int,
    val playerId: Long?,
    val customX: Float? = null,
    val customY: Float? = null,
    val customPosition: String? = null
)

@Entity(tableName = "saved_formations")
data class SavedFormationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val baseFormationId: String,
    val slotsData: String, // "slotIndex,posCode,customX,customY,playerId;..."
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "coaches")
data class CoachEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val nationality: String,
    val flagEmoji: String = "",
    val customImageUri: String? = null,
    val isBuiltIn: Boolean = false
)

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val uriString: String,
    val addedAt: Long = System.currentTimeMillis()
)
