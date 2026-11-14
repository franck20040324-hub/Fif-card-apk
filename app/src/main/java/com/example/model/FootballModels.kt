package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.LineAttRed
import com.example.ui.theme.LineDefGreen
import com.example.ui.theme.LineGkBlue
import com.example.ui.theme.LineMidYellow

enum class PlayingLine(val color: Color, val code: String) {
    GK(LineGkBlue, "GK"),
    DEF(LineDefGreen, "DEF"),
    MID(LineMidYellow, "MID"),
    ATT(LineAttRed, "ATT")
}

enum class PositionCode(val code: String, val line: PlayingLine) {
    GK("GK", PlayingLine.GK),
    LB("LB", PlayingLine.DEF),
    LCB("LCB", PlayingLine.DEF),
    CB("CB", PlayingLine.DEF),
    RCB("RCB", PlayingLine.DEF),
    RB("RB", PlayingLine.DEF),
    LWB("LWB", PlayingLine.DEF),
    RWB("RWB", PlayingLine.DEF),
    CDM("CDM", PlayingLine.MID),
    LCM("LCM", PlayingLine.MID),
    CM("CM", PlayingLine.MID),
    RCM("RCM", PlayingLine.MID),
    CAM("CAM", PlayingLine.MID),
    LM("LM", PlayingLine.MID),
    RM("RM", PlayingLine.MID),
    LW("LW", PlayingLine.ATT),
    RW("RW", PlayingLine.ATT),
    LF("LF", PlayingLine.ATT),
    RF("RF", PlayingLine.ATT),
    CF("CF", PlayingLine.ATT),
    ST("ST", PlayingLine.ATT);

    companion object {
        fun fromCode(raw: String): PositionCode {
            return entries.firstOrNull { it.code.equals(raw.trim(), ignoreCase = true) } ?: CM
        }
    }
}

enum class PreferredFoot(val code: String) {
    RIGHT("R"),
    LEFT("L"),
    BOTH("B");

    companion object {
        fun fromCode(raw: String): PreferredFoot {
            return entries.firstOrNull { it.code.equals(raw, ignoreCase = true) } ?: RIGHT
        }
    }
}

enum class AvatarGender {
    MALE,
    FEMALE;

    companion object {
        fun fromName(raw: String): AvatarGender {
            return entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: MALE
        }
    }
}

enum class TeamSide {
    HOME,
    AWAY
}

enum class AppLanguage {
    ENGLISH,
    ARABIC
}

enum class PlayerSortOption {
    POWER_DESC,
    POWER_ASC,
    POSITION,
    NAME_ASC,
    NAME_DESC,
    NATIONALITY,
    CLUB,
    LEAGUE
}

enum class PitchTheme(
    val id: String,
    val baseColor: Color,
    val stripeColor: Color,
    val lineColor: Color,
    val nameEn: String,
    val nameAr: String
) {
    TURF_GREEN(
        id = "TURF_GREEN",
        baseColor = Color(0xFF14532D),
        stripeColor = Color(0xFF166534),
        lineColor = Color(0xFFE8F5E9),
        nameEn = "Classic Turf",
        nameAr = "عشب كلاسيكي"
    ),
    EMERALD_PRO(
        id = "EMERALD_PRO",
        baseColor = Color(0xFF065F46),
        stripeColor = Color(0xFF047857),
        lineColor = Color(0xFFECFDF5),
        nameEn = "Emerald Pro",
        nameAr = "زمردي احترافي"
    ),
    NIGHT_SLATE(
        id = "NIGHT_SLATE",
        baseColor = Color(0xFF161D29),
        stripeColor = Color(0xFF1E2736),
        lineColor = Color(0xFFCBD5E1),
        nameEn = "Tactical Slate",
        nameAr = "تكتيكي داكن"
    ),
    ROYAL_BLUE(
        id = "ROYAL_BLUE",
        baseColor = Color(0xFF102A5C),
        stripeColor = Color(0xFF163875),
        lineColor = Color(0xFFDBEAFE),
        nameEn = "Royal Arena",
        nameAr = "أزرق ملكي"
    ),
    AMBER_CLAY(
        id = "AMBER_CLAY",
        baseColor = Color(0xFF5C2D12),
        stripeColor = Color(0xFF733917),
        lineColor = Color(0xFFFFEDD5),
        nameEn = "Sunset Clay",
        nameAr = "ترابي برتقالي"
    ),
    AMOLED_MONO(
        id = "AMOLED_MONO",
        baseColor = Color(0xFF08090C),
        stripeColor = Color(0xFF101218),
        lineColor = Color(0xFFFF6D00),
        nameEn = "AMOLED Neon",
        nameAr = "أموليد ليلي"
    );

    companion object {
        fun fromId(id: String?): PitchTheme =
            entries.firstOrNull { it.id == id } ?: TURF_GREEN
    }
}

data class FormationSlot(
    val index: Int,
    val position: PositionCode,
    val x: Float, // 0f (left) .. 1f (right)
    val y: Float  // 0f (top attacking end) .. 1f (bottom GK end) for single team
)

data class Formation(
    val id: String,
    val name: String,
    val slots: List<FormationSlot>
)

object FormationCatalog {
    private fun s(i: Int, p: PositionCode, x: Float, y: Float) = FormationSlot(i, p, x, y)

    val all: List<Formation> = listOf(
        Formation(
            "4-3-3", "4-3-3",
            listOf(
                s(0, PositionCode.GK, 0.50f, 0.90f),
                s(1, PositionCode.LB, 0.14f, 0.72f),
                s(2, PositionCode.LCB, 0.37f, 0.74f),
                s(3, PositionCode.RCB, 0.63f, 0.74f),
                s(4, PositionCode.RB, 0.86f, 0.72f),
                s(5, PositionCode.LCM, 0.24f, 0.49f),
                s(6, PositionCode.CM, 0.50f, 0.52f),
                s(7, PositionCode.RCM, 0.76f, 0.49f),
                s(8, PositionCode.LW, 0.18f, 0.22f),
                s(9, PositionCode.ST, 0.50f, 0.16f),
                s(10, PositionCode.RW, 0.82f, 0.22f)
            )
        ),
        Formation(
            "4-3-3_ATT", "4-3-3 Attack",
            listOf(
                s(0, PositionCode.GK, 0.50f, 0.90f),
                s(1, PositionCode.LB, 0.14f, 0.72f),
                s(2, PositionCode.LCB, 0.37f, 0.74f),
                s(3, PositionCode.RCB, 0.63f, 0.74f),
                s(4, PositionCode.RB, 0.86f, 0.72f),
                s(5, PositionCode.LCM, 0.27f, 0.53f),
                s(6, PositionCode.RCM, 0.73f, 0.53f),
                s(7, PositionCode.CAM, 0.50f, 0.38f),
                s(8, PositionCode.LW, 0.17f, 0.20f),
                s(9, PositionCode.ST, 0.50f, 0.14f),
                s(10, PositionCode.RW, 0.83f, 0.20f)
            )
        ),
        Formation(
            "4-3-3_HOLD", "4-3-3 Holding",
            listOf(
                s(0, PositionCode.GK, 0.50f, 0.90f),
                s(1, PositionCode.LB, 0.14f, 0.72f),
                s(2, PositionCode.LCB, 0.37f, 0.74f),
                s(3, PositionCode.RCB, 0.63f, 0.74f),
                s(4, PositionCode.RB, 0.86f, 0.72f),
                s(5, PositionCode.CDM, 0.50f, 0.58f),
                s(6, PositionCode.LCM, 0.27f, 0.44f),
                s(7, PositionCode.RCM, 0.73f, 0.44f),
                s(8, PositionCode.LW, 0.18f, 0.21f),
                s(9, PositionCode.ST, 0.50f, 0.15f),
                s(10, PositionCode.RW, 0.82f, 0.21f)
            )
        ),
        Formation(
            "4-2-3-1", "4-2-3-1",
            listOf(
                s(0, PositionCode.GK, 0.50f, 0.90f),
                s(1, PositionCode.LB, 0.14f, 0.73f),
                s(2, PositionCode.LCB, 0.37f, 0.75f),
                s(3, PositionCode.RCB, 0.63f, 0.75f),
                s(4, PositionCode.RB, 0.86f, 0.73f),
                s(5, PositionCode.CDM, 0.34f, 0.57f),
                s(6, PositionCode.CDM, 0.66f, 0.57f),
                s(7, PositionCode.LM, 0.18f, 0.37f),
                s(8, PositionCode.CAM, 0.50f, 0.37f),
                s(9, PositionCode.RM, 0.82f, 0.37f),
                s(10, PositionCode.ST, 0.50f, 0.15f)
            )
        ),
        Formation(
            "4-4-2", "4-4-2",
            listOf(
                s(0, PositionCode.GK, 0.50f, 0.90f),
                s(1, PositionCode.LB, 0.14f, 0.72f),
                s(2, PositionCode.LCB, 0.37f, 0.74f),
                s(3, PositionCode.RCB, 0.63f, 0.74f),
                s(4, PositionCode.RB, 0.86f, 0.72f),
                s(5, PositionCode.LM, 0.14f, 0.46f),
                s(6, PositionCode.LCM, 0.37f, 0.49f),
                s(7, PositionCode.RCM, 0.63f, 0.49f),
                s(8, PositionCode.RM, 0.86f, 0.46f),
                s(9, PositionCode.ST, 0.34f, 0.18f),
                s(10, PositionCode.ST, 0.66f, 0.18f)
            )
        ),
        Formation(
            "4-4-2_DIA", "4-4-2 Diamond",
            listOf(
                s(0, PositionCode.GK, 0.50f, 0.90f),
                s(1, PositionCode.LB, 0.14f, 0.72f),
                s(2, PositionCode.LCB, 0.37f, 0.74f),
                s(3, PositionCode.RCB, 0.63f, 0.74f),
                s(4, PositionCode.RB, 0.86f, 0.72f),
                s(5, PositionCode.CDM, 0.50f, 0.58f),
                s(6, PositionCode.LM, 0.22f, 0.46f),
                s(7, PositionCode.RM, 0.78f, 0.46f),
                s(8, PositionCode.CAM, 0.50f, 0.35f),
                s(9, PositionCode.ST, 0.34f, 0.16f),
                s(10, PositionCode.ST, 0.66f, 0.16f)
            )
        ),
        Formation(
            "4-1-4-1", "4-1-4-1",
            listOf(
                s(0, PositionCode.GK, 0.50f, 0.90f),
                s(1, PositionCode.LB, 0.14f, 0.73f),
                s(2, PositionCode.LCB, 0.37f, 0.75f),
                s(3, PositionCode.RCB, 0.63f, 0.75f),
                s(4, PositionCode.RB, 0.86f, 0.73f),
                s(5, PositionCode.CDM, 0.50f, 0.59f),
                s(6, PositionCode.LM, 0.14f, 0.39f),
                s(7, PositionCode.LCM, 0.37f, 0.42f),
                s(8, PositionCode.RCM, 0.63f, 0.42f),
                s(9, PositionCode.RM, 0.86f, 0.39f),
                s(10, PositionCode.ST, 0.50f, 0.16f)
            )
        ),
        Formation(
            "4-5-1", "4-5-1",
            listOf(
                s(0, PositionCode.GK, 0.50f, 0.90f),
                s(1, PositionCode.LB, 0.14f, 0.73f),
                s(2, PositionCode.LCB, 0.37f, 0.75f),
                s(3, PositionCode.RCB, 0.63f, 0.75f),
                s(4, PositionCode.RB, 0.86f, 0.73f),
                s(5, PositionCode.LM, 0.14f, 0.44f),
                s(6, PositionCode.CAM, 0.34f, 0.38f),
                s(7, PositionCode.CM, 0.50f, 0.52f),
                s(8, PositionCode.CAM, 0.66f, 0.38f),
                s(9, PositionCode.RM, 0.86f, 0.44f),
                s(10, PositionCode.ST, 0.50f, 0.16f)
            )
        ),
        Formation(
            "4-2-2-2", "4-2-2-2",
            listOf(
                s(0, PositionCode.GK, 0.50f, 0.90f),
                s(1, PositionCode.LB, 0.14f, 0.73f),
                s(2, PositionCode.LCB, 0.37f, 0.75f),
                s(3, PositionCode.RCB, 0.63f, 0.75f),
                s(4, PositionCode.RB, 0.86f, 0.73f),
                s(5, PositionCode.CDM, 0.33f, 0.56f),
                s(6, PositionCode.CDM, 0.67f, 0.56f),
                s(7, PositionCode.CAM, 0.25f, 0.37f),
                s(8, PositionCode.CAM, 0.75f, 0.37f),
                s(9, PositionCode.ST, 0.34f, 0.17f),
                s(10, PositionCode.ST, 0.66f, 0.17f)
            )
        ),
        Formation(
            "4-3-2-1", "4-3-2-1",
            listOf(
                s(0, PositionCode.GK, 0.50f, 0.90f),
                s(1, PositionCode.LB, 0.14f, 0.73f),
                s(2, PositionCode.LCB, 0.37f, 0.75f),
                s(3, PositionCode.RCB, 0.63f, 0.75f),
                s(4, PositionCode.RB, 0.86f, 0.73f),
                s(5, PositionCode.LCM, 0.24f, 0.53f),
                s(6, PositionCode.CM, 0.50f, 0.55f),
                s(7, PositionCode.RCM, 0.76f, 0.53f),
                s(8, PositionCode.CAM, 0.33f, 0.34f),
                s(9, PositionCode.CAM, 0.67f, 0.34f),
                s(10, PositionCode.ST, 0.50f, 0.15f)
            )
        ),
        Formation(
            "4-2-4", "4-2-4",
            listOf(
                s(0, PositionCode.GK, 0.50f, 0.90f),
                s(1, PositionCode.LB, 0.14f, 0.73f),
                s(2, PositionCode.LCB, 0.37f, 0.75f),
                s(3, PositionCode.RCB, 0.63f, 0.75f),
                s(4, PositionCode.RB, 0.86f, 0.73f),
                s(5, PositionCode.LCM, 0.34f, 0.50f),
                s(6, PositionCode.RCM, 0.66f, 0.50f),
                s(7, PositionCode.LW, 0.15f, 0.22f),
                s(8, PositionCode.ST, 0.38f, 0.17f),
                s(9, PositionCode.ST, 0.62f, 0.17f),
                s(10, PositionCode.RW, 0.85f, 0.22f)
            )
        ),
        Formation(
            "3-5-2", "3-5-2",
            listOf(
                s(0, PositionCode.GK, 0.50f, 0.90f),
                s(1, PositionCode.LCB, 0.24f, 0.73f),
                s(2, PositionCode.CB, 0.50f, 0.75f),
                s(3, PositionCode.RCB, 0.76f, 0.73f),
                s(4, PositionCode.LM, 0.14f, 0.47f),
                s(5, PositionCode.CDM, 0.36f, 0.54f),
                s(6, PositionCode.CAM, 0.50f, 0.38f),
                s(7, PositionCode.CDM, 0.64f, 0.54f),
                s(8, PositionCode.RM, 0.86f, 0.47f),
                s(9, PositionCode.ST, 0.34f, 0.17f),
                s(10, PositionCode.ST, 0.66f, 0.17f)
            )
        ),
        Formation(
            "3-4-3", "3-4-3",
            listOf(
                s(0, PositionCode.GK, 0.50f, 0.90f),
                s(1, PositionCode.LCB, 0.24f, 0.73f),
                s(2, PositionCode.CB, 0.50f, 0.75f),
                s(3, PositionCode.RCB, 0.76f, 0.73f),
                s(4, PositionCode.LM, 0.14f, 0.47f),
                s(5, PositionCode.LCM, 0.37f, 0.49f),
                s(6, PositionCode.RCM, 0.63f, 0.49f),
                s(7, PositionCode.RM, 0.86f, 0.47f),
                s(8, PositionCode.LW, 0.19f, 0.21f),
                s(9, PositionCode.ST, 0.50f, 0.16f),
                s(10, PositionCode.RW, 0.81f, 0.21f)
            )
        ),
        Formation(
            "3-4-2-1", "3-4-2-1",
            listOf(
                s(0, PositionCode.GK, 0.50f, 0.90f),
                s(1, PositionCode.LCB, 0.24f, 0.74f),
                s(2, PositionCode.CB, 0.50f, 0.76f),
                s(3, PositionCode.RCB, 0.76f, 0.74f),
                s(4, PositionCode.LM, 0.14f, 0.51f),
                s(5, PositionCode.LCM, 0.37f, 0.54f),
                s(6, PositionCode.RCM, 0.63f, 0.54f),
                s(7, PositionCode.RM, 0.86f, 0.51f),
                s(8, PositionCode.CAM, 0.33f, 0.33f),
                s(9, PositionCode.CAM, 0.67f, 0.33f),
                s(10, PositionCode.ST, 0.50f, 0.15f)
            )
        ),
        Formation(
            "3-4-1-2", "3-4-1-2",
            listOf(
                s(0, PositionCode.GK, 0.50f, 0.90f),
                s(1, PositionCode.LCB, 0.24f, 0.74f),
                s(2, PositionCode.CB, 0.50f, 0.76f),
                s(3, PositionCode.RCB, 0.76f, 0.74f),
                s(4, PositionCode.LM, 0.14f, 0.51f),
                s(5, PositionCode.LCM, 0.37f, 0.54f),
                s(6, PositionCode.RCM, 0.63f, 0.54f),
                s(7, PositionCode.RM, 0.86f, 0.51f),
                s(8, PositionCode.CAM, 0.50f, 0.35f),
                s(9, PositionCode.ST, 0.34f, 0.17f),
                s(10, PositionCode.ST, 0.66f, 0.17f)
            )
        ),
        Formation(
            "3-1-4-2", "3-1-4-2",
            listOf(
                s(0, PositionCode.GK, 0.50f, 0.90f),
                s(1, PositionCode.LCB, 0.24f, 0.74f),
                s(2, PositionCode.CB, 0.50f, 0.76f),
                s(3, PositionCode.RCB, 0.76f, 0.74f),
                s(4, PositionCode.CDM, 0.50f, 0.59f),
                s(5, PositionCode.LM, 0.14f, 0.42f),
                s(6, PositionCode.LCM, 0.37f, 0.42f),
                s(7, PositionCode.RCM, 0.63f, 0.42f),
                s(8, PositionCode.RM, 0.86f, 0.42f),
                s(9, PositionCode.ST, 0.34f, 0.17f),
                s(10, PositionCode.ST, 0.66f, 0.17f)
            )
        ),
        Formation(
            "5-3-2", "5-3-2",
            listOf(
                s(0, PositionCode.GK, 0.50f, 0.90f),
                s(1, PositionCode.LWB, 0.12f, 0.66f),
                s(2, PositionCode.LCB, 0.31f, 0.74f),
                s(3, PositionCode.CB, 0.50f, 0.76f),
                s(4, PositionCode.RCB, 0.69f, 0.74f),
                s(5, PositionCode.RWB, 0.88f, 0.66f),
                s(6, PositionCode.LCM, 0.26f, 0.47f),
                s(7, PositionCode.CM, 0.50f, 0.49f),
                s(8, PositionCode.RCM, 0.74f, 0.47f),
                s(9, PositionCode.ST, 0.34f, 0.18f),
                s(10, PositionCode.ST, 0.66f, 0.18f)
            )
        ),
        Formation(
            "5-4-1", "5-4-1",
            listOf(
                s(0, PositionCode.GK, 0.50f, 0.90f),
                s(1, PositionCode.LWB, 0.12f, 0.66f),
                s(2, PositionCode.LCB, 0.31f, 0.74f),
                s(3, PositionCode.CB, 0.50f, 0.76f),
                s(4, PositionCode.RCB, 0.69f, 0.74f),
                s(5, PositionCode.RWB, 0.88f, 0.66f),
                s(6, PositionCode.LM, 0.16f, 0.42f),
                s(7, PositionCode.LCM, 0.38f, 0.46f),
                s(8, PositionCode.RCM, 0.62f, 0.46f),
                s(9, PositionCode.RM, 0.84f, 0.42f),
                s(10, PositionCode.ST, 0.50f, 0.17f)
            )
        ),
        Formation(
            "5-2-3", "5-2-3",
            listOf(
                s(0, PositionCode.GK, 0.50f, 0.90f),
                s(1, PositionCode.LWB, 0.12f, 0.66f),
                s(2, PositionCode.LCB, 0.31f, 0.74f),
                s(3, PositionCode.CB, 0.50f, 0.76f),
                s(4, PositionCode.RCB, 0.69f, 0.74f),
                s(5, PositionCode.RWB, 0.88f, 0.66f),
                s(6, PositionCode.LCM, 0.34f, 0.48f),
                s(7, PositionCode.RCM, 0.66f, 0.48f),
                s(8, PositionCode.LW, 0.18f, 0.22f),
                s(9, PositionCode.ST, 0.50f, 0.16f),
                s(10, PositionCode.RW, 0.82f, 0.22f)
            )
        ),
        Formation(
            "4-1-3-2", "4-1-3-2",
            listOf(
                s(0, PositionCode.GK, 0.50f, 0.90f),
                s(1, PositionCode.LB, 0.14f, 0.73f),
                s(2, PositionCode.LCB, 0.37f, 0.75f),
                s(3, PositionCode.RCB, 0.63f, 0.75f),
                s(4, PositionCode.RB, 0.86f, 0.73f),
                s(5, PositionCode.CDM, 0.50f, 0.58f),
                s(6, PositionCode.LM, 0.18f, 0.40f),
                s(7, PositionCode.CAM, 0.50f, 0.38f),
                s(8, PositionCode.RM, 0.82f, 0.40f),
                s(9, PositionCode.ST, 0.34f, 0.17f),
                s(10, PositionCode.ST, 0.66f, 0.17f)
            )
        ),
        Formation(
            "3-3-1-3", "3-3-1-3",
            listOf(
                s(0, PositionCode.GK, 0.50f, 0.90f),
                s(1, PositionCode.LCB, 0.24f, 0.74f),
                s(2, PositionCode.CB, 0.50f, 0.76f),
                s(3, PositionCode.RCB, 0.76f, 0.74f),
                s(4, PositionCode.LM, 0.17f, 0.54f),
                s(5, PositionCode.CDM, 0.50f, 0.57f),
                s(6, PositionCode.RM, 0.83f, 0.54f),
                s(7, PositionCode.CAM, 0.50f, 0.37f),
                s(8, PositionCode.LW, 0.18f, 0.20f),
                s(9, PositionCode.ST, 0.50f, 0.14f),
                s(10, PositionCode.RW, 0.82f, 0.20f)
            )
        )
    )

    fun byId(id: String?): Formation = all.firstOrNull { it.id == id } ?: all.first()
}

data class ClubVisual(
    val name: String,
    val league: String,
    val shortCode: String,
    val primaryColor: Color,
    val secondaryColor: Color
)

object ClubCatalog {
    val clubs: List<ClubVisual> = listOf(
        ClubVisual("Real Madrid", "La Liga", "RMA", Color(0xFFF8FAFC), Color(0xFFD97706)),
        ClubVisual("FC Barcelona", "La Liga", "FCB", Color(0xFF1D4ED8), Color(0xFFDC2626)),
        ClubVisual("Atlético Madrid", "La Liga", "ATM", Color(0xFFDC2626), Color(0xFFFFFFFF)),
        ClubVisual("Manchester City", "Premier League", "MCI", Color(0xFF38BDF8), Color(0xFF0F172A)),
        ClubVisual("Arsenal", "Premier League", "ARS", Color(0xFFEF4444), Color(0xFFFFFFFF)),
        ClubVisual("Liverpool", "Premier League", "LIV", Color(0xFFB91C1C), Color(0xFFFACC15)),
        ClubVisual("Chelsea", "Premier League", "CHE", Color(0xFF1E40AF), Color(0xFFFFFFFF)),
        ClubVisual("Manchester United", "Premier League", "MUN", Color(0xFFDC2626), Color(0xFFFBBF24)),
        ClubVisual("Tottenham", "Premier League", "TOT", Color(0xFFF1F5F9), Color(0xFF0F172A)),
        ClubVisual("Bayern Munich", "Bundesliga", "BAY", Color(0xFFDC2626), Color(0xFFFFFFFF)),
        ClubVisual("Bayer Leverkusen", "Bundesliga", "LEV", Color(0xFF111827), Color(0xFFEF4444)),
        ClubVisual("Borussia Dortmund", "Bundesliga", "BVB", Color(0xFFFACC15), Color(0xFF111827)),
        ClubVisual("Inter Milan", "Serie A", "INT", Color(0xFF1D4ED8), Color(0xFF09090B)),
        ClubVisual("AC Milan", "Serie A", "MIL", Color(0xFFDC2626), Color(0xFF09090B)),
        ClubVisual("Juventus", "Serie A", "JUV", Color(0xFF0F172A), Color(0xFFFFFFFF)),
        ClubVisual("Napoli", "Serie A", "NAP", Color(0xFF0284C7), Color(0xFFFFFFFF)),
        ClubVisual("PSG", "Ligue 1", "PSG", Color(0xFF1E3A8A), Color(0xFFEF4444)),
        ClubVisual("Lyon", "Ligue 1", "OL", Color(0xFFF8FAFC), Color(0xFF2563EB)),
        ClubVisual("Barcelona Femení", "Liga F", "FCB", Color(0xFF1D4ED8), Color(0xFFDC2626)),
        ClubVisual("Chelsea Women", "WSL", "CHE", Color(0xFF1E40AF), Color(0xFFFFFFFF)),
        ClubVisual("Arsenal Women", "WSL", "ARS", Color(0xFFEF4444), Color(0xFFFFFFFF)),
        ClubVisual("Lyon Féminin", "Première Ligue", "OL", Color(0xFFF8FAFC), Color(0xFF2563EB)),
        ClubVisual("Bayern Frauen", "Frauen-Bundesliga", "BAY", Color(0xFFDC2626), Color(0xFFFFFFFF))
    )

    val leagues: List<String> = clubs.map { it.league }.distinct()

    fun findClub(clubName: String): ClubVisual {
        val exact = clubs.firstOrNull { it.name.equals(clubName, ignoreCase = true) }
        if (exact != null) return exact
        val code = clubName.trim().split(" ")
            .filter { it.isNotEmpty() }
            .take(3)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifEmpty { "FC" }
        val hash = kotlin.math.abs(clubName.hashCode())
        val palette = listOf(
            Color(0xFF2563EB) to Color(0xFFFFFFFF),
            Color(0xFFDC2626) to Color(0xFFFACC15),
            Color(0xFF16A34A) to Color(0xFFFFFFFF),
            Color(0xFFFF6D00) to Color(0xFF0F172A),
            Color(0xFF7C3AED) to Color(0xFFFFFFFF)
        )
        val colors = palette[hash % palette.size]
        return ClubVisual(clubName, "Custom", code.take(3), colors.first, colors.second)
    }
}

data class NationalityOption(
    val nameEn: String,
    val nameAr: String,
    val flag: String
)

object NationalityCatalog {
    val all: List<NationalityOption> = listOf(
        NationalityOption("Argentina", "الأرجنتين", "🇦🇷"),
        NationalityOption("France", "فرنسا", "🇫🇷"),
        NationalityOption("Spain", "إسبانيا", "🇪🇸"),
        NationalityOption("England", "إنجلترا", "🏴󠁧󠁢󠁥󠁮󠁧󠁿"),
        NationalityOption("Brazil", "البرازيل", "🇧🇷"),
        NationalityOption("Germany", "ألمانيا", "🇩🇪"),
        NationalityOption("Portugal", "البرتغال", "🇵🇹"),
        NationalityOption("Netherlands", "هولندا", "🇳🇱"),
        NationalityOption("Italy", "إيطاليا", "🇮🇹"),
        NationalityOption("Belgium", "بلجيكا", "🇧🇪"),
        NationalityOption("Norway", "النرويج", "🇳🇴"),
        NationalityOption("Egypt", "مصر", "🇪🇬"),
        NationalityOption("Morocco", "المغرب", "🇲🇦"),
        NationalityOption("Algeria", "الجزائر", "🇩🇿"),
        NationalityOption("Saudi Arabia", "السعودية", "🇸🇦"),
        NationalityOption("Croatia", "كرواتيا", "🇭🇷"),
        NationalityOption("Uruguay", "أوروغواي", "🇺🇾"),
        NationalityOption("Poland", "بولندا", "🇵🇱"),
        NationalityOption("South Korea", "كوريا الجنوبية", "🇰🇷"),
        NationalityOption("Japan", "اليابان", "🇯🇵"),
        NationalityOption("Colombia", "كولومبيا", "🇨🇴"),
        NationalityOption("Nigeria", "نيجيريا", "🇳🇬"),
        NationalityOption("USA", "أمريكا", "🇺🇸"),
        NationalityOption("Sweden", "السويد", "🇸🇪"),
        NationalityOption("Australia", "أستراليا", "🇦🇺"),
        NationalityOption("Slovenia", "سلوفينيا", "🇸🇮"),
        NationalityOption("Denmark", "الدنمارك", "🇩🇰"),
        NationalityOption("Switzerland", "سويسرا", "🇨🇭"),
        NationalityOption("Senegal", "السنغال", "🇸🇳"),
        NationalityOption("Georgia", "جورجيا", "🇬🇪")
    )

    fun flagFor(nationality: String): String {
        return all.firstOrNull {
            it.nameEn.equals(nationality, ignoreCase = true) ||
                it.nameAr.equals(nationality, ignoreCase = true)
        }?.flag ?: "🏳️"
    }
}

data class LocalTrack(
    val id: Long,
    val title: String,
    val uriString: String
)
