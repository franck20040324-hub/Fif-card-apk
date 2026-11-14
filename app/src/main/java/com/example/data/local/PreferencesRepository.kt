package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppLanguage
import com.example.model.PitchTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "fif_card_prefs")

data class UserPrefs(
    val isDarkAmoled: Boolean = true,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val pitchTheme: PitchTheme = PitchTheme.TURF_GREEN,
    val homeFormationId: String = "4-3-3",
    val awayFormationId: String = "4-2-3-1",
    val isMatchMode: Boolean = false,
    val isPlayersGridView: Boolean = true,
    val hasCompletedFirstLaunch: Boolean = false
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val IS_DARK_AMOLED = booleanPreferencesKey("is_dark_amoled")
        val LANGUAGE = stringPreferencesKey("language")
        val PITCH_THEME = stringPreferencesKey("pitch_theme")
        val HOME_FORMATION = stringPreferencesKey("home_formation")
        val AWAY_FORMATION = stringPreferencesKey("away_formation")
        val IS_MATCH_MODE = booleanPreferencesKey("is_match_mode")
        val IS_PLAYERS_GRID = booleanPreferencesKey("is_players_grid")
        val FIRST_LAUNCH_DONE = booleanPreferencesKey("first_launch_done")
    }

    val prefsFlow: Flow<UserPrefs> = context.dataStore.data.map { prefs ->
        UserPrefs(
            isDarkAmoled = prefs[Keys.IS_DARK_AMOLED] ?: true,
            language = if (prefs[Keys.LANGUAGE] == AppLanguage.ARABIC.name) AppLanguage.ARABIC else AppLanguage.ENGLISH,
            pitchTheme = PitchTheme.fromId(prefs[Keys.PITCH_THEME]),
            homeFormationId = prefs[Keys.HOME_FORMATION] ?: "4-3-3",
            awayFormationId = prefs[Keys.AWAY_FORMATION] ?: "4-2-3-1",
            isMatchMode = prefs[Keys.IS_MATCH_MODE] ?: false,
            isPlayersGridView = prefs[Keys.IS_PLAYERS_GRID] ?: true,
            hasCompletedFirstLaunch = prefs[Keys.FIRST_LAUNCH_DONE] ?: false
        )
    }

    suspend fun setDarkAmoled(dark: Boolean) {
        context.dataStore.edit { it[Keys.IS_DARK_AMOLED] = dark }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setPitchTheme(theme: PitchTheme) {
        context.dataStore.edit { it[Keys.PITCH_THEME] = theme.id }
    }

    suspend fun setHomeFormation(formationId: String) {
        context.dataStore.edit { it[Keys.HOME_FORMATION] = formationId }
    }

    suspend fun setAwayFormation(formationId: String) {
        context.dataStore.edit { it[Keys.AWAY_FORMATION] = formationId }
    }

    suspend fun setMatchMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_MATCH_MODE] = enabled }
    }

    suspend fun setPlayersGridView(isGrid: Boolean) {
        context.dataStore.edit { it[Keys.IS_PLAYERS_GRID] = isGrid }
    }

    suspend fun markFirstLaunchComplete() {
        context.dataStore.edit { it[Keys.FIRST_LAUNCH_DONE] = true }
    }
}
