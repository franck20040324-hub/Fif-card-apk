package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
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
    val activeFormationLabel: String = "4-3-3",
    val activeCoachId: Long = 0L,
    val isPlayersGridView: Boolean = true,
    val isManagersGridView: Boolean = true,
    val hasCompletedFirstLaunch: Boolean = false
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val IS_DARK_AMOLED = booleanPreferencesKey("is_dark_amoled")
        val LANGUAGE = stringPreferencesKey("language")
        val PITCH_THEME = stringPreferencesKey("pitch_theme")
        val HOME_FORMATION = stringPreferencesKey("home_formation")
        val ACTIVE_FORMATION_LABEL = stringPreferencesKey("active_formation_label")
        val ACTIVE_COACH_ID = longPreferencesKey("active_coach_id")
        val IS_PLAYERS_GRID = booleanPreferencesKey("is_players_grid")
        val IS_MANAGERS_GRID = booleanPreferencesKey("is_managers_grid")
        val FIRST_LAUNCH_DONE = booleanPreferencesKey("first_launch_done")
    }

    val prefsFlow: Flow<UserPrefs> = context.dataStore.data.map { prefs ->
        val formId = prefs[Keys.HOME_FORMATION] ?: "4-3-3"
        UserPrefs(
            isDarkAmoled = prefs[Keys.IS_DARK_AMOLED] ?: true,
            language = if (prefs[Keys.LANGUAGE] == AppLanguage.ARABIC.name) AppLanguage.ARABIC else AppLanguage.ENGLISH,
            pitchTheme = PitchTheme.fromId(prefs[Keys.PITCH_THEME]),
            homeFormationId = formId,
            activeFormationLabel = prefs[Keys.ACTIVE_FORMATION_LABEL] ?: formId,
            activeCoachId = prefs[Keys.ACTIVE_COACH_ID] ?: 0L,
            isPlayersGridView = prefs[Keys.IS_PLAYERS_GRID] ?: true,
            isManagersGridView = prefs[Keys.IS_MANAGERS_GRID] ?: true,
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

    suspend fun setHomeFormation(formationId: String, label: String = formationId) {
        context.dataStore.edit {
            it[Keys.HOME_FORMATION] = formationId
            it[Keys.ACTIVE_FORMATION_LABEL] = label
        }
    }

    suspend fun setActiveCoachId(coachId: Long) {
        context.dataStore.edit { it[Keys.ACTIVE_COACH_ID] = coachId }
    }

    suspend fun setPlayersGridView(isGrid: Boolean) {
        context.dataStore.edit { it[Keys.IS_PLAYERS_GRID] = isGrid }
    }

    suspend fun setManagersGridView(isGrid: Boolean) {
        context.dataStore.edit { it[Keys.IS_MANAGERS_GRID] = isGrid }
    }

    suspend fun markFirstLaunchComplete() {
        context.dataStore.edit { it[Keys.FIRST_LAUNCH_DONE] = true }
    }
}
