package com.example

import com.example.data.local.FifProSeedData
import com.example.data.local.PlayerEntity
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
    fun fifProSeedIncludesMaleAndFemaleAndResetsCleanly() {
        val seed = FifProSeedData.initialPlayers()
        assertTrue(seed.size >= 50)
        assertTrue(seed.any { it.avatarGender == AvatarGender.MALE.name })
        assertTrue(seed.any { it.avatarGender == AvatarGender.FEMALE.name })

        val sample = seed.first { it.mainPosition == "ST" }
        val originalAttPower = sample.powerAtt
        val customized = sample.copy(powerAtt = 99, powerGk = 99)
        val restored = customized.restoredFromSnapshot()
        assertEquals(originalAttPower, restored.powerAtt)

        // Custom user-created card should not be altered by restoredFromSnapshot
        val customCard = PlayerEntity(
            name = "Custom Star",
            nationality = "Spain",
            flagEmoji = "🇪🇸",
            club = "Custom FC",
            league = "Custom",
            mainPosition = "CAM",
            powerMid = 95,
            isBuiltIn = false
        )
        assertEquals(95, customCard.restoredFromSnapshot().powerForLine(PlayingLine.MID))
    }
}
