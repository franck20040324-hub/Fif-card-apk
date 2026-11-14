package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PlayerEntity
import com.example.i18n.LocalAppStrings
import com.example.model.PlayerSortOption
import com.example.model.PlayingLine
import com.example.ui.FifCardUiState
import com.example.ui.PlayerFilterState
import com.example.ui.components.LibraryPlayerCardGrid
import com.example.ui.components.LibraryPlayerCardList
import com.example.ui.theme.FifOrange

@Composable
fun PlayersListScreen(
    uiState: FifCardUiState,
    onUpdateFilter: ((PlayerFilterState) -> PlayerFilterState) -> Unit,
    onSetGridView: (Boolean) -> Unit,
    onPlayerClick: (PlayerEntity) -> Unit,
    onCreatePlayerClick: () -> Unit,
    onResetFifProClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val cs = MaterialTheme.colorScheme
    val filter = uiState.filterState
    val isGrid = uiState.prefs.isPlayersGridView

    var showSortMenu by remember { mutableStateOf(false) }
    var showLeagueMenu by remember { mutableStateOf(false) }
    var showClubMenu by remember { mutableStateOf(false) }
    var showNationMenu by remember { mutableStateOf(false) }

    val availableLeagues = remember(uiState.players) {
        uiState.players.map { it.league }.distinct().sorted()
    }
    val availableClubs = remember(uiState.players) {
        uiState.players.map { it.club }.distinct().sorted()
    }
    val availableNations = remember(uiState.players) {
        uiState.players.map { it.nationality }.distinct().sorted()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search + View Toggle + Sort + Reset Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cs.surface)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = filter.query,
                        onValueChange = { q -> onUpdateFilter { it.copy(query = q) } },
                        placeholder = {
                            Text(
                                text = strings.searchPlaceholder,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = FifOrange)
                        },
                        trailingIcon = {
                            if (filter.query.isNotEmpty()) {
                                IconButton(onClick = { onUpdateFilter { it.copy(query = "") } }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FifOrange,
                            unfocusedBorderColor = cs.outline
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("players_search_input")
                    )

                    // Grid / List View Switch
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = cs.surfaceVariant,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, cs.outline, RoundedCornerShape(10.dp))
                            .clickable { onSetGridView(!isGrid) }
                            .testTag("players_view_toggle")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isGrid) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.GridView,
                                contentDescription = "Toggle Grid/List",
                                tint = FifOrange
                            )
                        }
                    }

                    // Sort Menu Button
                    Box {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = cs.surfaceVariant,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, cs.outline, RoundedCornerShape(10.dp))
                                .clickable { showSortMenu = true }
                                .testTag("players_sort_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = strings.sortBy,
                                    tint = FifOrange
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            val options = listOf(
                                PlayerSortOption.POWER_DESC to strings.sortPowerDesc,
                                PlayerSortOption.POWER_ASC to strings.sortPowerAsc,
                                PlayerSortOption.POSITION to strings.sortPosition,
                                PlayerSortOption.NAME_ASC to strings.sortNameAsc,
                                PlayerSortOption.NAME_DESC to strings.sortNameDesc,
                                PlayerSortOption.NATIONALITY to strings.sortNationality,
                                PlayerSortOption.CLUB to strings.sortClub,
                                PlayerSortOption.LEAGUE to strings.sortLeague
                            )
                            options.forEach { (option, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = label,
                                            fontWeight = if (filter.sortOption == option) FontWeight.ExtraBold else FontWeight.Normal,
                                            color = if (filter.sortOption == option) FifOrange else cs.onSurface
                                        )
                                    },
                                    onClick = {
                                        showSortMenu = false
                                        onUpdateFilter { it.copy(sortOption = option) }
                                    }
                                )
                            }
                        }
                    }
                }

                // Filter Chips Row (Line Colors + League + Club + Nation + Reset FIFPro)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterPill(
                        label = strings.all,
                        selected = filter.lineFilter == null && filter.leagueFilter == null && filter.clubFilter == null && filter.nationalityFilter == null && !filter.famousOnly,
                        accentColor = FifOrange,
                        onClick = {
                            onUpdateFilter {
                                it.copy(
                                    lineFilter = null,
                                    leagueFilter = null,
                                    clubFilter = null,
                                    nationalityFilter = null,
                                    famousOnly = false
                                )
                            }
                        }
                    )

                    PlayingLine.entries.forEach { line ->
                        val label = when (line) {
                            PlayingLine.GK -> strings.gkLine
                            PlayingLine.DEF -> strings.defLine
                            PlayingLine.MID -> strings.midLine
                            PlayingLine.ATT -> strings.attLine
                        }
                        FilterPill(
                            label = label,
                            selected = filter.lineFilter == line,
                            accentColor = line.color,
                            onClick = {
                                onUpdateFilter {
                                    it.copy(lineFilter = if (it.lineFilter == line) null else line)
                                }
                            }
                        )
                    }

                    // League Filter
                    Box {
                        FilterPill(
                            label = filter.leagueFilter ?: strings.byLeague,
                            selected = filter.leagueFilter != null,
                            accentColor = FifOrange,
                            onClick = { showLeagueMenu = true }
                        )
                        DropdownMenu(
                            expanded = showLeagueMenu,
                            onDismissRequest = { showLeagueMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(strings.all) },
                                onClick = {
                                    showLeagueMenu = false
                                    onUpdateFilter { it.copy(leagueFilter = null) }
                                }
                            )
                            availableLeagues.forEach { league ->
                                DropdownMenuItem(
                                    text = { Text(league) },
                                    onClick = {
                                        showLeagueMenu = false
                                        onUpdateFilter { it.copy(leagueFilter = league) }
                                    }
                                )
                            }
                        }
                    }

                    // Club Filter
                    Box {
                        FilterPill(
                            label = filter.clubFilter ?: strings.byClub,
                            selected = filter.clubFilter != null,
                            accentColor = FifOrange,
                            onClick = { showClubMenu = true }
                        )
                        DropdownMenu(
                            expanded = showClubMenu,
                            onDismissRequest = { showClubMenu = false },
                            modifier = Modifier.height(300.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text(strings.all) },
                                onClick = {
                                    showClubMenu = false
                                    onUpdateFilter { it.copy(clubFilter = null) }
                                }
                            )
                            availableClubs.forEach { club ->
                                DropdownMenuItem(
                                    text = { Text(club) },
                                    onClick = {
                                        showClubMenu = false
                                        onUpdateFilter { it.copy(clubFilter = club) }
                                    }
                                )
                            }
                        }
                    }

                    // Nationality Filter
                    Box {
                        FilterPill(
                            label = filter.nationalityFilter ?: strings.byNationality,
                            selected = filter.nationalityFilter != null,
                            accentColor = FifOrange,
                            onClick = { showNationMenu = true }
                        )
                        DropdownMenu(
                            expanded = showNationMenu,
                            onDismissRequest = { showNationMenu = false },
                            modifier = Modifier.height(300.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text(strings.all) },
                                onClick = {
                                    showNationMenu = false
                                    onUpdateFilter { it.copy(nationalityFilter = null) }
                                }
                            )
                            availableNations.forEach { nation ->
                                DropdownMenuItem(
                                    text = { Text(nation) },
                                    onClick = {
                                        showNationMenu = false
                                        onUpdateFilter { it.copy(nationalityFilter = nation) }
                                    }
                                )
                            }
                        }
                    }

                    // Reset FIFPro Default Cards Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = cs.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, cs.outline, RoundedCornerShape(8.dp))
                            .clickable(onClick = onResetFifProClick)
                            .testTag("reset_fifpro_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = strings.resetFifpro,
                                tint = FifOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.resetFifpro,
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurface
                            )
                        }
                    }
                }
            }

            if (uiState.filteredPlayers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.noPlayersFound,
                        style = MaterialTheme.typography.bodyLarge,
                        color = cs.onSurfaceVariant
                    )
                }
            } else if (isGrid) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 152.dp),
                    contentPadding = PaddingValues(10.dp, 10.dp, 10.dp, 88.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.filteredPlayers, key = { it.id }) { player ->
                        LibraryPlayerCardGrid(
                            player = player,
                            onClick = { onPlayerClick(player) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(10.dp, 10.dp, 10.dp, 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.filteredPlayers, key = { it.id }) { player ->
                        LibraryPlayerCardList(
                            player = player,
                            onClick = { onPlayerClick(player) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onCreatePlayerClick,
            containerColor = FifOrange,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("create_player_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Add, contentDescription = strings.createNewCard)
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.createNewCard, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) accentColor else cs.surfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, if (selected) accentColor else cs.outline, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else cs.onSurface,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
        )
    }
}
