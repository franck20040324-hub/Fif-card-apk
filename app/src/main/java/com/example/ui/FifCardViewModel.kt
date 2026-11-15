package com.example.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.MusicPlayerManager
import com.example.audio.MusicState
import com.example.data.FifCardRepository
import com.example.data.local.CoachEntity
import com.example.data.local.FifCardDatabase
import com.example.data.local.LineupSlotEntity
import com.example.data.local.PlayerEntity
import com.example.data.local.PreferencesRepository
import com.example.data.local.SavedFormationEntity
import com.example.data.local.UserPrefs
import com.example.model.AppLanguage
import com.example.model.Formation
import com.example.model.FormationCatalog
import com.example.model.LocalTrack
import com.example.model.PitchTheme
import com.example.model.PlayerSortOption
import com.example.model.PlayingLine
import com.example.model.PositionCode
import com.example.model.TeamSide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MainTab {
    PITCH,
    PLAYERS
}

data class SlotContext(
    val slotKey: String,
    val teamSide: TeamSide,
    val isStarter: Boolean,
    val slotIndex: Int,
    val slotPosition: PositionCode,
    val x: Float,
    val y: Float,
    val hasCustomPosition: Boolean,
    val player: PlayerEntity?
)

data class PlayerFilterState(
    val query: String = "",
    val sortOption: PlayerSortOption = PlayerSortOption.POWER_DESC,
    val lineFilter: PlayingLine? = null,
    val leagueFilter: String? = null,
    val clubFilter: String? = null,
    val nationalityFilter: String? = null,
    val famousOnly: Boolean = false
)

data class FifCardUiState(
    val isReady: Boolean = false,
    val activeTab: MainTab = MainTab.PITCH,
    val prefs: UserPrefs = UserPrefs(),
    val players: List<PlayerEntity> = emptyList(),
    val filteredPlayers: List<PlayerEntity> = emptyList(),
    val filterState: PlayerFilterState = PlayerFilterState(),
    val homeStarterSlots: List<SlotContext> = emptyList(),
    val homeSubSlots: List<SlotContext> = emptyList(),
    val awayStarterSlots: List<SlotContext> = emptyList(),
    val awaySubSlots: List<SlotContext> = emptyList(),
    val savedFormations: List<SavedFormationEntity> = emptyList(),
    val homeCoach: CoachEntity = CoachEntity(TeamSide.HOME.name, "Manager Home", "Spain", "🇪🇸"),
    val awayCoach: CoachEntity = CoachEntity(TeamSide.AWAY.name, "Manager Away", "France", "🇫🇷"),
    val activeMatchSide: TeamSide = TeamSide.HOME,
    val selectedSlotKey: String? = null,
    val repositioningSlotKey: String? = null,
    val pickerTargetSlot: SlotContext? = null,
    val inspectedPlayerSlot: SlotContext? = null,
    val editingPlayer: PlayerEntity? = null,
    val isCreatingNewPlayerForSlot: String? = null,
    val editingCoachSide: TeamSide? = null,
    val showPlaylistSheet: Boolean = false,
    val showResetConfirmDialog: Boolean = false,
    val showSaveFormationDialog: Boolean = false,
    val showSavedFormationsSheet: Boolean = false,
    val musicState: MusicState = MusicState()
) {
    val homeFormation: Formation get() = FormationCatalog.byId(prefs.homeFormationId)
    val awayFormation: Formation get() = FormationCatalog.byId(prefs.awayFormationId)

    val homeAveragePower: Int
        get() {
            val rated = homeStarterSlots.mapNotNull { slot ->
                slot.player?.powerForSlot(slot.slotPosition)
            }
            return if (rated.isEmpty()) 0 else rated.sum() / rated.size
        }

    val awayAveragePower: Int
        get() {
            val rated = awayStarterSlots.mapNotNull { slot ->
                slot.player?.powerForSlot(slot.slotPosition)
            }
            return if (rated.isEmpty()) 0 else rated.sum() / rated.size
        }
}

class FifCardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FifCardDatabase.getInstance(application)
    private val repository = FifCardRepository(db)
    private val prefsRepo = PreferencesRepository(application)
    val musicManager = MusicPlayerManager(application)

    private val activeTab = MutableStateFlow(MainTab.PITCH)
    private val filterState = MutableStateFlow(PlayerFilterState())
    private val activeMatchSide = MutableStateFlow(TeamSide.HOME)
    private val selectedSlotKey = MutableStateFlow<String?>(null)
    private val repositioningSlotKey = MutableStateFlow<String?>(null)
    private val pickerTargetSlotKey = MutableStateFlow<String?>(null)
    private val inspectedSlotKey = MutableStateFlow<String?>(null)
    private val inspectedStandalonePlayerId = MutableStateFlow<Long?>(null)
    private val editingPlayerState = MutableStateFlow<PlayerEntity?>(null)
    private val creatingForSlotKey = MutableStateFlow<String?>(null)
    private val editingCoachSide = MutableStateFlow<TeamSide?>(null)
    private val showPlaylistSheet = MutableStateFlow(false)
    private val showResetConfirmDialog = MutableStateFlow(false)
    private val showSaveFormationDialog = MutableStateFlow(false)
    private val showSavedFormationsSheet = MutableStateFlow(false)
    private val isSeeded = MutableStateFlow(false)

    private data class ModalState(
        val tab: MainTab,
        val filter: PlayerFilterState,
        val matchSide: TeamSide,
        val selectedSlot: String?,
        val repositionSlot: String?,
        val pickerSlot: String?,
        val inspectedSlot: String?,
        val inspectedPlayerId: Long?,
        val editingPlayer: PlayerEntity?,
        val creatingForSlot: String?,
        val editingCoach: TeamSide?,
        val playlistOpen: Boolean,
        val resetDialog: Boolean,
        val saveFormationDialog: Boolean,
        val savedFormationsSheet: Boolean
    )

    private val modalFlow = combine(
        combine(activeTab, filterState, activeMatchSide, selectedSlotKey, repositioningSlotKey) { t, f, m, s, rp ->
            arrayOf<Any?>(t, f, m, s, rp)
        },
        combine(pickerTargetSlotKey, inspectedSlotKey, inspectedStandalonePlayerId, editingPlayerState) { p, i, ip, e ->
            arrayOf<Any?>(p, i, ip, e)
        },
        combine(
            creatingForSlotKey,
            editingCoachSide,
            showPlaylistSheet,
            showResetConfirmDialog,
            showSaveFormationDialog
        ) { c, ec, pl, rd, sf ->
            arrayOf<Any?>(c, ec, pl, rd, sf)
        },
        showSavedFormationsSheet
    ) { a, b, c, sfs ->
        ModalState(
            tab = a[0] as MainTab,
            filter = a[1] as PlayerFilterState,
            matchSide = a[2] as TeamSide,
            selectedSlot = a[3] as String?,
            repositionSlot = a[4] as String?,
            pickerSlot = b[0] as String?,
            inspectedSlot = b[1] as String?,
            inspectedPlayerId = b[2] as Long?,
            editingPlayer = b[3] as PlayerEntity?,
            creatingForSlot = c[0] as String?,
            editingCoach = c[1] as TeamSide?,
            playlistOpen = c[2] as Boolean,
            resetDialog = c[3] as Boolean,
            saveFormationDialog = c[4] as Boolean,
            savedFormationsSheet = sfs
        )
    }

    val uiState: StateFlow<FifCardUiState> = combine(
        combine(
            isSeeded,
            prefsRepo.prefsFlow,
            repository.playersFlow,
            repository.lineupSlotsFlow
        ) { seeded, prefs, players, slots ->
            arrayOf<Any?>(seeded, prefs, players, slots)
        },
        combine(
            repository.coachesFlow,
            repository.savedFormationsFlow,
            musicManager.state,
            modalFlow
        ) { coaches, savedForms, music, modal ->
            arrayOf<Any?>(coaches, savedForms, music, modal)
        }
    ) { core, extras ->
        val seeded = core[0] as Boolean
        val prefs = core[1] as UserPrefs
        @Suppress("UNCHECKED_CAST")
        val players = core[2] as List<PlayerEntity>
        @Suppress("UNCHECKED_CAST")
        val slots = core[3] as List<LineupSlotEntity>
        @Suppress("UNCHECKED_CAST")
        val coaches = extras[0] as List<CoachEntity>
        @Suppress("UNCHECKED_CAST")
        val savedFormations = extras[1] as List<SavedFormationEntity>
        val music = extras[2] as MusicState
        val modal = extras[3] as ModalState

        val playerById = players.associateBy { it.id }
        val homeFormation = FormationCatalog.byId(prefs.homeFormationId)
        val awayFormation = FormationCatalog.byId(prefs.awayFormationId)

        val homeStarters = buildStarterContexts(TeamSide.HOME, homeFormation, slots, playerById)
        val homeSubs = buildSubContexts(TeamSide.HOME, slots, playerById)
        val awayStarters = buildStarterContexts(TeamSide.AWAY, awayFormation, slots, playerById)
        val awaySubs = buildSubContexts(TeamSide.AWAY, slots, playerById)

        val allContexts = homeStarters + homeSubs + awayStarters + awaySubs

        val homeCoach = coaches.firstOrNull { it.teamSide == TeamSide.HOME.name }
            ?: CoachEntity(TeamSide.HOME.name, "Manager Home", "Spain", "🇪🇸")
        val awayCoach = coaches.firstOrNull { it.teamSide == TeamSide.AWAY.name }
            ?: CoachEntity(TeamSide.AWAY.name, "Manager Away", "France", "🇫🇷")

        val inspectedContext = when {
            modal.inspectedSlot != null -> allContexts.firstOrNull { it.slotKey == modal.inspectedSlot }
            modal.inspectedPlayerId != null -> playerById[modal.inspectedPlayerId]?.let { p ->
                SlotContext(
                    slotKey = "",
                    teamSide = TeamSide.HOME,
                    isStarter = false,
                    slotIndex = -1,
                    slotPosition = p.mainPositionCode,
                    x = 0.5f,
                    y = 0.5f,
                    hasCustomPosition = false,
                    player = p
                )
            }
            else -> null
        }

        FifCardUiState(
            isReady = seeded,
            activeTab = modal.tab,
            prefs = prefs,
            players = players,
            filteredPlayers = applyFilterAndSort(players, modal.filter),
            filterState = modal.filter,
            homeStarterSlots = homeStarters,
            homeSubSlots = homeSubs,
            awayStarterSlots = awayStarters,
            awaySubSlots = awaySubs,
            savedFormations = savedFormations,
            homeCoach = homeCoach,
            awayCoach = awayCoach,
            activeMatchSide = modal.matchSide,
            selectedSlotKey = modal.selectedSlot,
            repositioningSlotKey = modal.repositionSlot,
            pickerTargetSlot = modal.pickerSlot?.let { key -> allContexts.firstOrNull { it.slotKey == key } },
            inspectedPlayerSlot = inspectedContext,
            editingPlayer = modal.editingPlayer,
            isCreatingNewPlayerForSlot = modal.creatingForSlot,
            editingCoachSide = modal.editingCoach,
            showPlaylistSheet = modal.playlistOpen,
            showResetConfirmDialog = modal.resetDialog,
            showSaveFormationDialog = modal.saveFormationDialog,
            showSavedFormationsSheet = modal.savedFormationsSheet,
            musicState = music
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FifCardUiState()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureSeeded()
            isSeeded.value = true
        }
        viewModelScope.launch {
            repository.songsFlow.collect { songs ->
                musicManager.setTracks(
                    songs.map { LocalTrack(id = it.id, title = it.title, uriString = it.uriString) }
                )
            }
        }
    }

    private fun buildStarterContexts(
        side: TeamSide,
        formation: Formation,
        allSlots: List<LineupSlotEntity>,
        playerById: Map<Long, PlayerEntity>
    ): List<SlotContext> {
        val map = allSlots.filter { it.teamSide == side.name && it.isStarter }.associateBy { it.slotIndex }
        return formation.slots.map { fSlot ->
            val entity = map[fSlot.index]
            val key = entity?.slotKey ?: FifCardRepository.starterKey(side, fSlot.index)
            val cx = entity?.customX
            val cy = entity?.customY
            SlotContext(
                slotKey = key,
                teamSide = side,
                isStarter = true,
                slotIndex = fSlot.index,
                slotPosition = fSlot.position,
                x = cx ?: fSlot.x,
                y = cy ?: fSlot.y,
                hasCustomPosition = cx != null && cy != null,
                player = entity?.playerId?.let { playerById[it] }
            )
        }
    }

    private fun buildSubContexts(
        side: TeamSide,
        allSlots: List<LineupSlotEntity>,
        playerById: Map<Long, PlayerEntity>
    ): List<SlotContext> {
        return allSlots
            .filter { it.teamSide == side.name && !it.isStarter }
            .sortedBy { it.slotIndex }
            .map { entity ->
                val player = entity.playerId?.let { playerById[it] }
                SlotContext(
                    slotKey = entity.slotKey,
                    teamSide = side,
                    isStarter = false,
                    slotIndex = entity.slotIndex,
                    slotPosition = player?.mainPositionCode ?: PositionCode.CM,
                    x = 0.5f,
                    y = 0.5f,
                    hasCustomPosition = false,
                    player = player
                )
            }
    }

    private fun applyFilterAndSort(
        players: List<PlayerEntity>,
        filter: PlayerFilterState
    ): List<PlayerEntity> {
        val q = filter.query.trim().lowercase()
        val filtered = players.filter { p ->
            val matchesQuery = if (q.isEmpty()) true else {
                p.name.lowercase().contains(q) ||
                    p.nickname.lowercase().contains(q) ||
                    p.club.lowercase().contains(q) ||
                    p.nationality.lowercase().contains(q) ||
                    p.league.lowercase().contains(q) ||
                    p.mainPosition.lowercase().contains(q) ||
                    p.secondaryPositions.lowercase().contains(q)
            }
            val matchesLine = filter.lineFilter == null || p.mainLine == filter.lineFilter
            val matchesLeague = filter.leagueFilter == null || p.league.equals(filter.leagueFilter, ignoreCase = true)
            val matchesClub = filter.clubFilter == null || p.club.equals(filter.clubFilter, ignoreCase = true)
            val matchesNation = filter.nationalityFilter == null || p.nationality.equals(filter.nationalityFilter, ignoreCase = true)
            val matchesFamous = !filter.famousOnly || p.naturalPower >= 86
            matchesQuery && matchesLine && matchesLeague && matchesClub && matchesNation && matchesFamous
        }

        return when (filter.sortOption) {
            PlayerSortOption.POWER_DESC -> filtered.sortedByDescending { it.naturalPower }
            PlayerSortOption.POWER_ASC -> filtered.sortedBy { it.naturalPower }
            PlayerSortOption.POSITION -> filtered.sortedWith(compareBy({ it.mainLine.ordinal }, { -it.naturalPower }))
            PlayerSortOption.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            PlayerSortOption.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
            PlayerSortOption.NATIONALITY -> filtered.sortedWith(compareBy({ it.nationality.lowercase() }, { -it.naturalPower }))
            PlayerSortOption.CLUB -> filtered.sortedWith(compareBy({ it.club.lowercase() }, { -it.naturalPower }))
            PlayerSortOption.LEAGUE -> filtered.sortedWith(compareBy({ it.league.lowercase() }, { -it.naturalPower }))
        }
    }

    fun setTab(tab: MainTab) {
        activeTab.value = tab
        selectedSlotKey.value = null
        repositioningSlotKey.value = null
    }

    fun toggleTheme() {
        viewModelScope.launch {
            prefsRepo.setDarkAmoled(!uiState.value.prefs.isDarkAmoled)
        }
    }

    fun setDarkAmoled(dark: Boolean) {
        viewModelScope.launch { prefsRepo.setDarkAmoled(dark) }
    }

    fun toggleLanguage() {
        val next = if (uiState.value.prefs.language == AppLanguage.ENGLISH) AppLanguage.ARABIC else AppLanguage.ENGLISH
        setLanguage(next)
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { prefsRepo.setLanguage(language) }
    }

    fun markFirstLaunchComplete() {
        viewModelScope.launch { prefsRepo.markFirstLaunchComplete() }
    }

    fun setPitchTheme(theme: PitchTheme) {
        viewModelScope.launch { prefsRepo.setPitchTheme(theme) }
    }

    fun setMatchMode(enabled: Boolean) {
        selectedSlotKey.value = null
        repositioningSlotKey.value = null
        viewModelScope.launch { prefsRepo.setMatchMode(enabled) }
    }

    fun setActiveMatchSide(side: TeamSide) {
        activeMatchSide.value = side
    }

    fun setFormation(side: TeamSide, formationId: String) {
        repositioningSlotKey.value = null
        viewModelScope.launch {
            repository.clearCustomPositionsForTeam(side)
            if (side == TeamSide.HOME) prefsRepo.setHomeFormation(formationId)
            else prefsRepo.setAwayFormation(formationId)
        }
    }

    fun openSaveFormationDialog() {
        showSaveFormationDialog.value = true
    }

    fun closeSaveFormationDialog() {
        showSaveFormationDialog.value = false
    }

    fun saveCurrentFormation(side: TeamSide, name: String) {
        showSaveFormationDialog.value = false
        val starters = if (side == TeamSide.HOME) uiState.value.homeStarterSlots else uiState.value.awayStarterSlots
        val baseId = if (side == TeamSide.HOME) uiState.value.prefs.homeFormationId else uiState.value.prefs.awayFormationId
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveFormationLocally(name, baseId, starters)
        }
    }

    fun openSavedFormationsSheet() {
        showSavedFormationsSheet.value = true
    }

    fun closeSavedFormationsSheet() {
        showSavedFormationsSheet.value = false
    }

    fun loadSavedFormation(side: TeamSide, saved: SavedFormationEntity) {
        showSavedFormationsSheet.value = false
        repositioningSlotKey.value = null
        viewModelScope.launch(Dispatchers.IO) {
            if (side == TeamSide.HOME) prefsRepo.setHomeFormation(saved.baseFormationId)
            else prefsRepo.setAwayFormation(saved.baseFormationId)
            repository.loadSavedFormation(side, saved)
        }
    }

    fun deleteSavedFormation(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSavedFormation(id)
        }
    }

    fun setPlayersGridView(isGrid: Boolean) {
        viewModelScope.launch { prefsRepo.setPlayersGridView(isGrid) }
    }

    fun updateFilter(transform: (PlayerFilterState) -> PlayerFilterState) {
        filterState.update(transform)
    }

    fun onSlotTapped(slot: SlotContext) {
        if (repositioningSlotKey.value != null) {
            repositioningSlotKey.value = null
            return
        }

        val currentSelected = selectedSlotKey.value
        if (currentSelected != null) {
            if (currentSelected == slot.slotKey) {
                selectedSlotKey.value = null
                if (slot.player != null) {
                    inspectedSlotKey.value = slot.slotKey
                    inspectedStandalonePlayerId.value = null
                } else {
                    pickerTargetSlotKey.value = slot.slotKey
                }
            } else {
                selectedSlotKey.value = null
                viewModelScope.launch(Dispatchers.IO) {
                    repository.swapSlots(currentSelected, slot.slotKey)
                }
            }
            return
        }

        if (slot.player == null) {
            pickerTargetSlotKey.value = slot.slotKey
        } else {
            selectedSlotKey.value = slot.slotKey
        }
    }

    fun startRepositioning(slot: SlotContext) {
        if (!slot.isStarter) return
        selectedSlotKey.value = null
        repositioningSlotKey.value = slot.slotKey
    }

    fun nudgeSlotPosition(slotKey: String, dx: Float, dy: Float) {
        val all = uiState.value.homeStarterSlots + uiState.value.awayStarterSlots
        val slot = all.firstOrNull { it.slotKey == slotKey } ?: return
        val newX = (slot.x + dx).coerceIn(0.08f, 0.92f)
        val newY = (slot.y + dy).coerceIn(0.08f, 0.92f)
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSlotCoordinates(slotKey, newX, newY)
        }
    }

    fun resetSlotPosition(slotKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSlotCoordinates(slotKey, null, null)
        }
    }

    fun finishRepositioning() {
        repositioningSlotKey.value = null
    }

    fun cancelSlotSelection() {
        selectedSlotKey.value = null
    }

    fun inspectSelectedSlot() {
        val key = selectedSlotKey.value ?: return
        selectedSlotKey.value = null
        inspectedSlotKey.value = key
        inspectedStandalonePlayerId.value = null
    }

    fun swapSlots(slotKeyA: String, slotKeyB: String) {
        selectedSlotKey.value = null
        repositioningSlotKey.value = null
        if (slotKeyA == slotKeyB) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.swapSlots(slotKeyA, slotKeyB)
        }
    }

    fun assignPlayerToTargetSlot(playerId: Long) {
        val key = pickerTargetSlotKey.value ?: return
        pickerTargetSlotKey.value = null
        viewModelScope.launch(Dispatchers.IO) {
            repository.assignPlayerToSlot(key, playerId)
        }
    }

    fun clearSlot(slotKey: String) {
        inspectedSlotKey.value = null
        selectedSlotKey.value = null
        viewModelScope.launch(Dispatchers.IO) {
            repository.assignPlayerToSlot(slotKey, null)
        }
    }

    fun addSubSlot(side: TeamSide) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSubSlot(side)
        }
    }

    fun removeSubSlot(slotKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeSubSlot(slotKey)
        }
    }

    fun closePickerSheet() {
        pickerTargetSlotKey.value = null
    }

    fun openPlayerDetail(player: PlayerEntity) {
        inspectedSlotKey.value = null
        inspectedStandalonePlayerId.value = player.id
    }

    fun closePlayerDetail() {
        inspectedSlotKey.value = null
        inspectedStandalonePlayerId.value = null
    }

    fun openCreatePlayer(forSlotKey: String? = null) {
        pickerTargetSlotKey.value = null
        inspectedSlotKey.value = null
        inspectedStandalonePlayerId.value = null
        creatingForSlotKey.value = forSlotKey
        editingPlayerState.value = PlayerEntity(
            name = "",
            nickname = "",
            nationality = "Spain",
            flagEmoji = "🇪🇸",
            club = "Real Madrid",
            league = "La Liga",
            mainPosition = "CM",
            secondaryPositions = "",
            isBuiltIn = false
        )
    }

    fun openEditPlayer(player: PlayerEntity) {
        inspectedSlotKey.value = null
        inspectedStandalonePlayerId.value = null
        creatingForSlotKey.value = null
        editingPlayerState.value = player
    }

    fun closePlayerEditor() {
        editingPlayerState.value = null
        creatingForSlotKey.value = null
    }

    fun savePlayer(player: PlayerEntity) {
        val targetSlot = creatingForSlotKey.value
        editingPlayerState.value = null
        creatingForSlotKey.value = null
        viewModelScope.launch(Dispatchers.IO) {
            val savedId = repository.savePlayer(player)
            if (targetSlot != null) {
                repository.assignPlayerToSlot(targetSlot, savedId)
            }
        }
    }

    fun deleteCustomPlayer(playerId: Long) {
        editingPlayerState.value = null
        inspectedSlotKey.value = null
        inspectedStandalonePlayerId.value = null
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCustomPlayer(playerId)
        }
    }

    fun openCoachEditor(side: TeamSide) {
        editingCoachSide.value = side
    }

    fun closeCoachEditor() {
        editingCoachSide.value = null
    }

    fun saveCoach(coach: CoachEntity) {
        editingCoachSide.value = null
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveCoach(coach)
        }
    }

    fun askResetFifPro() {
        showResetConfirmDialog.value = true
    }

    fun dismissResetFifPro() {
        showResetConfirmDialog.value = false
    }

    fun confirmResetFifPro() {
        showResetConfirmDialog.value = false
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetBuiltInFifProPlayers()
        }
    }

    fun openPlaylistSheet() {
        showPlaylistSheet.value = true
    }

    fun closePlaylistSheet() {
        showPlaylistSheet.value = false
    }

    fun addLocalSongs(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val app = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            uris.forEach { uri ->
                try {
                    app.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                }
                val title = resolveFileName(app, uri)
                repository.addSong(title, uri.toString())
            }
        }
    }

    fun removeSong(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSong(id)
        }
    }

    fun persistPhotoUriPermission(uri: Uri) {
        try {
            getApplication<Application>().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
        }
    }

    private fun resolveFileName(app: Application, uri: Uri): String {
        var name: String? = null
        try {
            app.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) name = cursor.getString(idx)
                    }
                }
        } catch (_: Exception) {
        }
        val clean = name?.substringBeforeLast('.')?.trim()
        return if (!clean.isNullOrBlank()) clean else (uri.lastPathSegment ?: "Track")
    }

    override fun onCleared() {
        super.onCleared()
        musicManager.stopAndRelease()
    }
}
