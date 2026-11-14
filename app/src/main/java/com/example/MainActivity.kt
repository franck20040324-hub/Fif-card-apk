package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.i18n.LocalAppStrings
import com.example.i18n.stringsFor
import com.example.model.AppLanguage
import com.example.model.TeamSide
import com.example.ui.FifCardUiState
import com.example.ui.FifCardViewModel
import com.example.ui.MainTab
import com.example.ui.components.MusicAndHeaderTopBar
import com.example.ui.screens.PitchLineupScreen
import com.example.ui.screens.PlayersListScreen
import com.example.ui.sheets.CoachEditorDialog
import com.example.ui.sheets.FirstLaunchSetupDialog
import com.example.ui.sheets.PlayerDetailSheet
import com.example.ui.sheets.PlayerEditorDialog
import com.example.ui.sheets.PlayerPickerSheet
import com.example.ui.sheets.PlaylistSheet
import com.example.ui.sheets.ResetFifProConfirmDialog
import com.example.ui.theme.FifCardTheme
import com.example.ui.theme.FifOrange

class MainActivity : ComponentActivity() {

    private val viewModel: FifCardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            FifCardRoot(uiState = uiState, viewModel = viewModel)
        }
    }

    override fun onPause() {
        super.onPause()
        // Never keep playing music when app goes to background
        viewModel.musicManager.onAppBackgrounded()
    }

    override fun onStop() {
        super.onStop()
        viewModel.musicManager.onAppBackgrounded()
    }
}

@Composable
private fun FifCardRoot(
    uiState: FifCardUiState,
    viewModel: FifCardViewModel
) {
    val isArabic = uiState.prefs.language == AppLanguage.ARABIC
    val strings = stringsFor(uiState.prefs.language)
    val layoutDir = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addLocalSongs(uris)
        }
    }

    BackHandler(enabled = uiState.selectedSlotKey != null || uiState.activeTab == MainTab.PLAYERS) {
        if (uiState.selectedSlotKey != null) {
            viewModel.cancelSlotSelection()
        } else if (uiState.activeTab == MainTab.PLAYERS) {
            viewModel.setTab(MainTab.PITCH)
        }
    }

    FifCardTheme(
        darkTheme = uiState.prefs.isDarkAmoled,
        isArabic = isArabic
    ) {
        CompositionLocalProvider(
            LocalAppStrings provides strings,
            LocalLayoutDirection provides layoutDir
        ) {
            val cs = MaterialTheme.colorScheme
            Scaffold(
                containerColor = cs.background,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    MusicAndHeaderTopBar(
                        musicState = uiState.musicState,
                        isDarkAmoled = uiState.prefs.isDarkAmoled,
                        language = uiState.prefs.language,
                        onPreviousSong = { viewModel.musicManager.previous() },
                        onTogglePlayPause = { viewModel.musicManager.togglePlayPause() },
                        onNextSong = { viewModel.musicManager.next() },
                        onAddSongsClick = { audioPickerLauncher.launch(arrayOf("audio/*")) },
                        onOpenPlaylist = { viewModel.openPlaylistSheet() },
                        onToggleTheme = { viewModel.toggleTheme() },
                        onToggleLanguage = { viewModel.toggleLanguage() },
                        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = cs.surface,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        NavigationBarItem(
                            selected = uiState.activeTab == MainTab.PITCH,
                            onClick = { viewModel.setTab(MainTab.PITCH) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.activeTab == MainTab.PITCH) Icons.Filled.SportsSoccer else Icons.Outlined.SportsSoccer,
                                    contentDescription = strings.pitchTab
                                )
                            },
                            label = { Text(strings.pitchTab, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = FifOrange,
                                selectedTextColor = FifOrange,
                                indicatorColor = FifOrange.copy(alpha = 0.16f)
                            ),
                            modifier = Modifier.testTag("nav_pitch_tab")
                        )
                        NavigationBarItem(
                            selected = uiState.activeTab == MainTab.PLAYERS,
                            onClick = { viewModel.setTab(MainTab.PLAYERS) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.activeTab == MainTab.PLAYERS) Icons.Filled.Groups else Icons.Outlined.Groups,
                                    contentDescription = strings.playersTab
                                )
                            },
                            label = { Text(strings.playersTab, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = FifOrange,
                                selectedTextColor = FifOrange,
                                indicatorColor = FifOrange.copy(alpha = 0.16f)
                            ),
                            modifier = Modifier.testTag("nav_players_tab")
                        )
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (!uiState.isReady) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = FifOrange)
                        }
                    } else {
                        when (uiState.activeTab) {
                            MainTab.PITCH -> PitchLineupScreen(
                                uiState = uiState,
                                onSetMatchMode = viewModel::setMatchMode,
                                onSetActiveMatchSide = viewModel::setActiveMatchSide,
                                onSelectFormation = viewModel::setFormation,
                                onSelectPitchTheme = viewModel::setPitchTheme,
                                onSlotTap = viewModel::onSlotTapped,
                                onSwapSlots = viewModel::swapSlots,
                                onInspectSelected = viewModel::inspectSelectedSlot,
                                onCancelSelection = viewModel::cancelSlotSelection,
                                onAddSubSlot = viewModel::addSubSlot,
                                onRemoveSubSlot = viewModel::removeSubSlot,
                                onCoachClick = viewModel::openCoachEditor
                            )
                            MainTab.PLAYERS -> PlayersListScreen(
                                uiState = uiState,
                                onUpdateFilter = viewModel::updateFilter,
                                onSetGridView = viewModel::setPlayersGridView,
                                onPlayerClick = viewModel::openPlayerDetail,
                                onCreatePlayerClick = { viewModel.openCreatePlayer(null) },
                                onResetFifProClick = viewModel::askResetFifPro
                            )
                        }
                    }
                }
            }

            // Empty Slot Picker Sheet (Add New Card, Search, Famous Power, Club, Nation, League)
            val pickerTarget = uiState.pickerTargetSlot
            if (pickerTarget != null) {
                PlayerPickerSheet(
                    targetSlot = pickerTarget,
                    allPlayers = uiState.players,
                    onSelectPlayer = viewModel::assignPlayerToTargetSlot,
                    onCreateNewCard = { viewModel.openCreatePlayer(pickerTarget.slotKey) },
                    onDismiss = viewModel::closePickerSheet
                )
            }

            // Player Detail Sheet
            val inspectedSlot = uiState.inspectedPlayerSlot
            if (inspectedSlot != null) {
                PlayerDetailSheet(
                    slotContext = inspectedSlot,
                    onEditCard = viewModel::openEditPlayer,
                    onRemoveFromSlot = viewModel::clearSlot,
                    onDeleteCustomCard = viewModel::deleteCustomPlayer,
                    onDismiss = viewModel::closePlayerDetail
                )
            }

            // Player Editor / Creator Dialog
            val editingPlayer = uiState.editingPlayer
            if (editingPlayer != null) {
                PlayerEditorDialog(
                    initialPlayer = editingPlayer,
                    onPersistPhotoUri = viewModel::persistPhotoUriPermission,
                    onSave = viewModel::savePlayer,
                    onDismiss = viewModel::closePlayerEditor
                )
            }

            // Coach Editor Dialog (Only Name, Nationality & local photo customizable)
            val editingCoachSide = uiState.editingCoachSide
            if (editingCoachSide != null) {
                val coach = if (editingCoachSide == TeamSide.HOME) uiState.homeCoach else uiState.awayCoach
                CoachEditorDialog(
                    coach = coach,
                    onPersistPhotoUri = viewModel::persistPhotoUriPermission,
                    onSave = viewModel::saveCoach,
                    onDismiss = viewModel::closeCoachEditor
                )
            }

            // Music Playlist Sheet
            if (uiState.showPlaylistSheet) {
                PlaylistSheet(
                    musicState = uiState.musicState,
                    onAddSongs = { audioPickerLauncher.launch(arrayOf("audio/*")) },
                    onPlayTrack = { idx -> viewModel.musicManager.playAtIndex(idx) },
                    onRemoveTrack = viewModel::removeSong,
                    onDismiss = viewModel::closePlaylistSheet
                )
            }

            // Reset FIFPro Confirmation Dialog
            if (uiState.showResetConfirmDialog) {
                ResetFifProConfirmDialog(
                    onConfirm = viewModel::confirmResetFifPro,
                    onDismiss = viewModel::dismissResetFifPro
                )
            }

            // First-Time Opening Only Permissions & Setup Dialog
            if (uiState.isReady && !uiState.prefs.hasCompletedFirstLaunch) {
                FirstLaunchSetupDialog(
                    isDarkAmoled = uiState.prefs.isDarkAmoled,
                    language = uiState.prefs.language,
                    onSelectTheme = viewModel::setDarkAmoled,
                    onSelectLanguage = viewModel::setLanguage,
                    onComplete = viewModel::markFirstLaunchComplete
                )
            }
        }
    }
}
