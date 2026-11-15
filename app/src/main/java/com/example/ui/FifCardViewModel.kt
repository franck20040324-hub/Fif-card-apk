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
import com.example.model.ManagerSortOption
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
    PLAYERS,
    MANAGERS
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

data class ManagerFilterState(
    val query: String = "",
    val sortOption: ManagerSortOption = ManagerSortOption.NAME_ASC
)

data class FifCardUiState(
    val isReady: Boolean = false,
    val activeTab: MainTab = MainTab.PITCH,
    val prefs: UserPrefs = UserPrefs(),
    val players: List<PlayerEntity> = emptyList(),
    val filteredPlayers: List<PlayerEntity> = emptyList(),
    val filterState: PlayerFilterState = PlayerFilterState(),
    val coaches: List<CoachEntity> = emptyList(),
    val filteredCoaches: List<CoachEntity> = emptyList(),
    val managerFilterState: ManagerFilterState = ManagerFilterState(),
    val activeCoach: CoachEntity = CoachEntity(name = "Pep Guardiola", nationality = "Spain", isBuiltIn = true),
    val homeStarterSlots: List<SlotContext> = emptyList(),
    val homeSubSlots: List<SlotContext> = emptyList(),
    val savedFormations: List<SavedFormationEntity> = emptyList(),
    val selectedSlotKey: String? = null,
    val repositioningSlotKey: String? = null,
    val slotActionTarget: SlotContext? = null,
    val pickerTargetSlot: SlotContext? = null,
    val inspectedPlayerSlot: SlotContext? = null,
    val editingPlayer: PlayerEntity? = null,
    val isCreatingNewPlayerForSlot: String? = null,
    val showManagerPitchActions: Boolean = false,
    val showManagerPickerSheet: Boolean = false,
    val inspectedManager: CoachEntity? = null,
    val editingCoach: CoachEntity? = null,
    val showPlaylistSheet: Boolean = false,
    val showResetConfirmDialog: Boolean = false,
    val showSaveFormationDialog: Boolean = false,
    val showCreateFormationDialog: Boolean = false,
    val showSavedFormationsSheet: Boolean = false,
    val musicState: MusicState = MusicState()
) {
    val homeFormation: Formation get() = FormationCatalog.byId(prefs.homeFormationId)

    val homeAveragePower: Int
        get() {
            val rated = homeStarterSlots.mapNotNull { slot ->
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
    private val managerFilterState = MutableStateFlow(ManagerFilterState())
    private val selectedSlotKey = MutableStateFlow<String?>(null)
    private val repositioningSlotKey = MutableStateFlow<String?>(null)
    private val slotActionTargetKey = MutableStateFlow<String?>(null)
    private val pickerTargetSlotKey = MutableStateFlow<String?>(null)
    private val inspectedSlotKey = MutableStateFlow<String?>(null)
    private val inspectedStandalonePlayerId = MutableStateFlow<Long?>(null)
    private val editingPlayerState = MutableStateFlow<PlayerEntity?>(null)
    private val creatingForSlotKey = MutableStateFlow<String?>(null)

    private val showManagerPitchActions = MutableStateFlow(false)
    private val showManagerPickerSheet = MutableStateFlow(false)
    private val inspectedManagerId = MutableStateFlow<Long?>(null)
    private val editingCoachState = MutableStateFlow<CoachEntity?>(null)
    private val assignCoachToPitchAfterSave = MutableStateFlow(false)

    private val showPlaylistSheet = MutableStateFlow(false)
    private val showResetConfirmDialog = MutableStateFlow(false)
    private val showSaveFormationDialog = MutableStateFlow(false)
    private val showCreateFormationDialog = MutableStateFlow(false)
    private val showSavedFormationsSheet = MutableStateFlow(false)
    private val isSeeded = MutableStateFlow(false)

    private data class ModalState(
        val tab: MainTab,
        val filter: PlayerFilterState,
        val managerFilter: ManagerFilterState,
        val selectedSlot: String?,
        val repositionSlot: String?,
        val slotActionKey: String?,
        val pickerSlot: String?,
        val inspectedSlot: String?,
        val inspectedPlayerId: Long?,
        val editingPlayer: PlayerEntity?,
        val creatingForSlot: String?,
        val managerPitchActions: Boolean,
        val managerPickerSheet: Boolean,
        val inspectedManagerId: Long?,
        val editingCoach: CoachEntity?,
        val playlistOpen: Boolean,
        val resetDialog: Boolean,
        val saveFormationDialog: Boolean,
        val createFormationDialog: Boolean,
        val savedFormationsSheet: Boolean
    )

    private val modalFlow = combine(
        combine(activeTab, filterState, managerFilterState, selectedSlotKey, repositioningSlotKey) { t, f, mf, s, rp ->
            arrayOf<Any?>(t, f, mf, s, rp)
        },
        combine(slotActionTargetKey, pickerTargetSlotKey, inspectedSlotKey, inspectedStandalonePlayerId, editingPlayerState) { sa, p, i, ip, e ->
            arrayOf<Any?>(sa, p, i, ip, e)
        },
        combine(
            creatingForSlotKey,
            showManagerPitchActions,
            showManagerPickerSheet,
            inspectedManagerId,
            editingCoachState
        ) { c, mpa, mps, im, ec ->
            arrayOf<Any?>(c, mpa, mps, im, ec)
        },
        combine(
            showPlaylistSheet,
            showResetConfirmDialog,
            showSaveFormationDialog,
            showCreateFormationDialog,
            showSavedFormationsSheet
        ) { pl, rd, sf, cf, sfs ->
            arrayOf<Any?>(pl, rd, sf, cf, sfs)
        }
    ) { a, b, c, d ->
        ModalState(
            tab = a[0] as MainTab,
            filter = a[1] as PlayerFilterState,
            managerFilter = a[2] as ManagerFilterState,
            selectedSlot = a[3] as String?,
            repositionSlot = a[4] as String?,
            slotActionKey = b[0] as String?,
            pickerSlot = b[1] as String?,
            inspectedSlot = b[2] as String?,
            inspectedPlayerId = b[3] as Long?,
            editingPlayer = b[4] as PlayerEntity?,
            creatingForSlot = c[0] as String?,
            managerPitchActions = c[1] as Boolean,
            managerPickerSheet = c[2] as Boolean,
            inspectedManagerId = c[3] as Long?,
            editingCoach = c[4] as CoachEntity?,
            playlistOpen = d[0] as Boolean,
            resetDialog = d[1] as Boolean,
            saveFormationDialog = d[2] as Boolean,
            createFormationDialog = d[3] as Boolean,
            savedFormationsSheet = d[4] as Boolean
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

        val homeStarters = buildStarterContexts(TeamSide.HOME, homeFormation, slots, playerById)
        val homeSubs = buildSubContexts(TeamSide.HOME, slots, playerById)
        val allContexts = homeStarters + homeSubs

        val activeCoach = coaches.firstOrNull { it.id == prefs.activeCoachId }
            ?: coaches.firstOrNull()
            ?: CoachEntity(name = "Pep Guardiola", nationality = "Spain", isBuiltIn = true)

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
            coaches = coaches,
            filteredCoaches = applyManagerFilterAndSort(coaches, modal.managerFilter),
            managerFilterState = modal.managerFilter,
            activeCoach = activeCoach,
            homeStarterSlots = homeStarters,
            homeSubSlots = homeSubs,
            savedFormations = savedFormations,
            selectedSlotKey = modal.selectedSlot,
            repositioningSlotKey = modal.repositionSlot,
            slotActionTarget = modal.slotActionKey?.let { key -> allContexts.firstOrNull { it.slotKey == key } },
            pickerTargetSlot = modal.pickerSlot?.let { key -> allContexts.firstOrNull { it.slotKey == key } },
            inspectedPlayerSlot = inspectedContext,
            editingPlayer = modal.editingPlayer,
            isCreatingNewPlayerForSlot = modal.creatingForSlot,
            showManagerPitchActions = modal.managerPitchActions,
            showManagerPickerSheet = modal.managerPickerSheet,
            inspectedManager = modal.inspectedManagerId?.let { id -> coaches.firstOrNull { it.id == id } },
            editingCoach = modal.editingCoach,
            showPlaylistSheet = modal.playlistOpen,
            showResetConfirmDialog = modal.resetDialog,
            showSaveFormationDialog = modal.saveFormationDialog,
            showCreateFormationDialog = modal.createFormationDialog,
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
            val customPos = entity?.customPosition?.let { PositionCode.fromCode(it) }
            SlotContext(
                slotKey = key,
                teamSide = side,
                isStarter = true,
                slotIndex = fSlot.index,
                slotPosition = customPos ?: fSlot.position,
                x = cx ?: fSlot.x,
                y = cy ?: fSlot.y,
                hasCustomPosition = (cx != null && cy != null) || customPos != null,
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
                    p.abilityNicknames.lowercase().contains(q) ||
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

    private fun applyManagerFilterAndSort(
        coaches: List<CoachEntity>,
        filter: ManagerFilterState
    ): List<CoachEntity> {
        val q = filter.query.trim().lowercase()
        val filtered = coaches.filter { c ->
            q.isEmpty() ||
                c.name.lowercase().contains(q) ||
                c.nationality.lowercase().contains(q)
        }
        return when (filter.sortOption) {
            ManagerSortOption.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            ManagerSortOption.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
            ManagerSortOption.NATIONALITY -> filtered.sortedWith(compareBy({ it.nationality.lowercase() }, { it.name.lowercase() }))
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

    fun setFormation(formationId: String) {
        repositioningSlotKey.value = null
        val form = FormationCatalog.byId(formationId)
        viewModelScope.launch {
            repository.clearCustomPositionsForTeam(TeamSide.HOME)
            prefsRepo.setHomeFormation(form.id, form.name)
        }
    }

    fun openSaveFormationDialog() {
        showSaveFormationDialog.value = true
    }

    fun closeSaveFormationDialog() {
        showSaveFormationDialog.value = false
    }

    fun saveCurrentFormation(name: String) {
        showSaveFormationDialog.value = false
        val starters = uiState.value.homeStarterSlots
        val baseId = uiState.value.prefs.homeFormationId
        val cleanName = name.trim().ifEmpty { uiState.value.homeFormation.name }
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveFormationLocally(cleanName, baseId, starters)
            prefsRepo.setHomeFormation(baseId, cleanName)
        }
    }

    fun openCreateFormationDialog() {
        showCreateFormationDialog.value = true
    }

    fun closeCreateFormationDialog() {
        showCreateFormationDialog.value = false
    }

    fun createAndApplyNewFormation(
        name: String,
        baseFormationId: String,
        customSlots: List<SlotContext>
    ) {
        showCreateFormationDialog.value = false
        repositioningSlotKey.value = null
        val cleanName = name.trim().ifEmpty { baseFormationId }
        viewModelScope.launch(Dispatchers.IO) {
            repository.createAndApplyCustomFormation(cleanName, baseFormationId, customSlots)
            prefsRepo.setHomeFormation(baseFormationId, cleanName)
        }
    }

    fun openSavedFormationsSheet() {
        showSavedFormationsSheet.value = true
    }

    fun closeSavedFormationsSheet() {
        showSavedFormationsSheet.value = false
    }

    fun loadSavedFormation(saved: SavedFormationEntity) {
        showSavedFormationsSheet.value = false
        repositioningSlotKey.value = null
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepo.setHomeFormation(saved.baseFormationId, saved.name)
            repository.loadSavedFormation(TeamSide.HOME, saved)
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

    fun setManagersGridView(isGrid: Boolean) {
        viewModelScope.launch { prefsRepo.setManagersGridView(isGrid) }
    }

    fun updateManagerFilter(transform: (ManagerFilterState) -> ManagerFilterState) {
        managerFilterState.update(transform)
    }

    fun onSlotTapped(slot: SlotContext) {
        if (repositioningSlotKey.value != null) {
            repositioningSlotKey.value = null
            return
        }

        val currentSelected = selectedSlotKey.value
        if (currentSelected != null) {
            selectedSlotKey.value = null
            if (currentSelected != slot.slotKey) {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.swapSlots(currentSelected, slot.slotKey)
                }
            } else {
                slotActionTargetKey.value = slot.slotKey
            }
            return
        }

        // Tapping any empty OR current card shows 2 primary options (Create New Card / Select from Existing List)
        slotActionTargetKey.value = slot.slotKey
    }

    fun closeSlotActionSheet() {
        slotActionTargetKey.value = null
    }

    fun openCreateFromSlotAction(slotKey: String) {
        slotActionTargetKey.value = null
        openCreatePlayer(slotKey)
    }

    fun openSelectExistingFromSlotAction(slotKey: String) {
        slotActionTargetKey.value = null
        pickerTargetSlotKey.value = slotKey
    }

    fun inspectFromSlotAction(slotKey: String) {
        slotActionTargetKey.value = null
        inspectedSlotKey.value = slotKey
        inspectedStandalonePlayerId.value = null
    }

    fun startSwapFromSlotAction(slotKey: String) {
        slotActionTargetKey.value = null
        selectedSlotKey.value = slotKey
    }

    fun clearFromSlotAction(slotKey: String) {
        slotActionTargetKey.value = null
        clearSlot(slotKey)
    }

    fun startRepositioning(slot: SlotContext) {
        if (!slot.isStarter) return
        selectedSlotKey.value = null
        slotActionTargetKey.value = null
        repositioningSlotKey.value = slot.slotKey
    }

    fun nudgeSlotPosition(slotKey: String, dx: Float, dy: Float) {
        val slot = uiState.value.homeStarterSlots.firstOrNull { it.slotKey == slotKey } ?: return
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

    fun addSubSlot() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSubSlot(TeamSide.HOME)
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
        slotActionTargetKey.value = null
        inspectedSlotKey.value = null
        inspectedStandalonePlayerId.value = null
        creatingForSlotKey.value = forSlotKey
        editingPlayerState.value = PlayerEntity(
            name = "",
            nickname = "",
            abilityNicknames = "",
            nationality = "",
            flagEmoji = "",
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

    // Manager / Coach Actions
    fun onPitchCoachTapped() {
        showManagerPitchActions.value = true
    }

    fun closeManagerPitchActions() {
        showManagerPitchActions.value = false
    }

    fun openManagerPicker() {
        showManagerPitchActions.value = false
        showManagerPickerSheet.value = true
    }

    fun closeManagerPicker() {
        showManagerPickerSheet.value = false
    }

    fun assignCoachToPitch(coachId: Long) {
        showManagerPickerSheet.value = false
        showManagerPitchActions.value = false
        inspectedManagerId.value = null
        viewModelScope.launch {
            prefsRepo.setActiveCoachId(coachId)
        }
    }

    fun openManagerDetail(coach: CoachEntity) {
        inspectedManagerId.value = coach.id
    }

    fun closeManagerDetail() {
        inspectedManagerId.value = null
    }

    fun openCreateManager(assignToPitch: Boolean) {
        showManagerPitchActions.value = false
        showManagerPickerSheet.value = false
        inspectedManagerId.value = null
        assignCoachToPitchAfterSave.value = assignToPitch
        editingCoachState.value = CoachEntity(
            id = 0L,
            name = "",
            nationality = "",
            flagEmoji = "",
            customImageUri = null,
            isBuiltIn = false
        )
    }

    fun openEditManager(coach: CoachEntity, assignToPitch: Boolean = false) {
        showManagerPitchActions.value = false
        inspectedManagerId.value = null
        assignCoachToPitchAfterSave.value = assignToPitch
        editingCoachState.value = coach
    }

    fun closeCoachEditor() {
        editingCoachState.value = null
        assignCoachToPitchAfterSave.value = false
    }

    fun saveCoach(coach: CoachEntity) {
        val assignNow = assignCoachToPitchAfterSave.value
        editingCoachState.value = null
        assignCoachToPitchAfterSave.value = false
        viewModelScope.launch(Dispatchers.IO) {
            val savedId = repository.saveCoach(coach)
            if (assignNow) {
                prefsRepo.setActiveCoachId(savedId)
            }
        }
    }

    fun deleteCustomCoach(coachId: Long) {
        inspectedManagerId.value = null
        editingCoachState.value = null
        viewModelScope.launch(Dispatchers.IO) {
            val wasActive = uiState.value.activeCoach.id == coachId
            repository.deleteCustomCoach(coachId)
            if (wasActive) {
                prefsRepo.setActiveCoachId(0L)
            }
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
