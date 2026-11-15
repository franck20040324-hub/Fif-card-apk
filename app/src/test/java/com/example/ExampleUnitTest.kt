package com.example

import com.example.data.local.FifProSeedData
import com.example.data.local.PlayerEntity
import com.example.model.AbilityNicknameCatalog
import com.example.model.AppLanguage
import com.example.model.AvatarGender
import com.example.model.FormationCatalog
import com.example.model.PlayingLine
import com.example.model.PositionCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun formationsHaveElevenPlayersEach() {
        assertTrue(FormationCatalog.all.size >= 20)
        FormationCatalog.all.forEach { formation ->
            assertEquals("Formation ${formation.name} must have 11 slots", 11, formation.slots.size)
            assertEquals(PositionCode.GK, formation.slots.first().position)
        }
    }

    @Test
    fun fifProSeedIncludesAbilitiesFiveExtraPositionsAndManagers() {
        val seed = FifProSeedData.initialPlayers()
        assertTrue(seed.size >= 50)
        assertTrue(seed.any { it.avatarGender == AvatarGender.MALE.name })
        assertTrue(seed.any { it.avatarGender == AvatarGender.FEMALE.name })
        assertTrue(seed.all { it.heading in 1..99 && it.crossing in 1..99 && it.tackling in 1..99 && it.stamina in 1..99 })
        assertTrue(seed.all { it.abilityNicknameList.isNotEmpty() })

        val sample = seed.first { it.mainPosition == "ST" }
        val originalAttPower = sample.powerAtt
        val originalHeading = sample.heading
        val originalStamina = sample.stamina
        val originalAbilities = sample.abilityNicknames
        val customized = sample.copy(
            powerAtt = 99,
            powerGk = 99,
            heading = 10,
            stamina = 12,
            abilityNicknames = "Custom"
        )
        val restored = customized.restoredFromSnapshot()
        assertEquals(originalAttPower, restored.powerAtt)
        assertEquals(originalHeading, restored.heading)
        assertEquals(originalStamina, restored.stamina)
        assertEquals(originalAbilities, restored.abilityNicknames)

        val fiveExtrasCard = PlayerEntity(
            name = "Utility Star",
            nationality = "Morocco",
            club = "Custom FC",
            league = "Custom",
            mainPosition = "CM",
            secondaryPositions = "CDM,CAM,LCM,RCM,RM,LM",
            abilityNicknames = "Box-to-Box,Deep-Lying Playmaker",
            powerMid = 95,
            isBuiltIn = false
        )
        assertEquals(5, fiveExtrasCard.extraPositionList.size)
        assertEquals("Morocco", fiveExtrasCard.nationality)
        assertEquals(95, fiveExtrasCard.restoredFromSnapshot().powerForLine(PlayingLine.MID))
        assertEquals(
            "Box-to-Box",
            AbilityNicknameCatalog.displayName(fiveExtrasCard.abilityNicknameList.first(), AppLanguage.ENGLISH)
        )
        assertEquals(
            "محور شامل",
            AbilityNicknameCatalog.displayName(fiveExtrasCard.abilityNicknameList.first(), AppLanguage.ARABIC)
        )

        val coaches = FifProSeedData.initialCoaches()
        assertTrue(coaches.size >= 15)
        assertTrue(coaches.all { it.name.isNotBlank() && it.nationality.isNotBlank() && it.isBuiltIn })
    }
}
