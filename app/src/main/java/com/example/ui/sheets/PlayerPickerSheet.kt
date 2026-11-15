package com.example.ui.sheets

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PlayerEntity
import com.example.i18n.LocalAppStrings
import com.example.model.AbilityNicknameCatalog
import com.example.model.AppLanguage
import com.example.model.AvatarGender
import com.example.model.PlayingLine
import com.example.ui.SlotContext
import com.example.ui.components.ClubCrestBadge
import com.example.ui.components.PlayerPortrait
import com.example.ui.components.PositionRoleLogo
import com.example.ui.theme.FifOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerPickerSheet(
    targetSlot: SlotContext,
    allPlayers: List<PlayerEntity>,
    onSelectPlayer: (Long) -> Unit,
    onCreateNewCard: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val cs = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var searchQuery by remember { mutableStateOf("") }
    var famousOnly by remember { mutableStateOf(false) }
    var selectedClub by remember { mutableStateOf<String?>(null) }
    var selectedNation by remember { mutableStateOf<String?>(null) }
    var selectedLeague by remember { mutableStateOf<String?>(null) }

    var showClubMenu by remember { mutableStateOf(false) }
    var showNationMenu by remember { mutableStateOf(false) }
    var showLeagueMenu by remember { mutableStateOf(false) }

    val clubs = remember(allPlayers) { allPlayers.map { it.club }.distinct().sorted() }
    val nations = remember(allPlayers) { allPlayers.map { it.nationality }.distinct().sorted() }
    val leagues = remember(allPlayers) { allPlayers.map { it.league }.distinct().sorted() }

    val appLang = if (LocalLayoutDirection.current == LayoutDirection.Rtl) AppLanguage.ARABIC else AppLanguage.ENGLISH
    val results = remember(
        allPlayers,
        searchQuery,
        famousOnly,
        selectedClub,
        selectedNation,
        selectedLeague,
        targetSlot.slotPosition
    ) {
        val q = searchQuery.trim().lowercase()
        allPlayers.filter { p ->
            val matchQ = q.isEmpty() ||
                p.name.lowercase().contains(q) ||
                p.nickname.lowercase().contains(q) ||
                p.abilityNicknames.lowercase().contains(q) ||
                p.club.lowercase().contains(q) ||
                p.nationality.lowercase().contains(q) ||
                p.league.lowercase().contains(q) ||
                p.mainPosition.lowercase().contains(q) ||
                p.secondaryPositions.lowercase().contains(q)
            val matchFamous = !famousOnly || p.naturalPower >= 86
            val matchClub = selectedClub == null || p.club == selectedClub
            val matchNation = selectedNation == null || p.nationality == selectedNation
            val matchLeague = selectedLeague == null || p.league == selectedLeague
            matchQ && matchFamous && matchClub && matchNation && matchLeague
        }.sortedByDescending { it.powerForSlot(targetSlot.slotPosition) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = cs.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.86f)
                .padding(horizontal = 14.dp)
        ) {
            // Header + Create New Creation Card button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${strings.addPlayerToSlot} ${targetSlot.slotPosition.code}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = cs.onBackground
                )
                Button(
                    onClick = onCreateNewCard,
                    colors = ButtonDefaults.buttonColors(containerColor = FifOrange),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("picker_create_new_card")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.createNewCard, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.searchPlaceholder, maxLines = 1) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = FifOrange) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("picker_search_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Famous Power Cards, By Club, By Nationality, By League
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PickerFilterChip(
                    label = strings.famousCards,
                    selected = famousOnly,
                    leadingStar = true,
                    onClick = { famousOnly = !famousOnly }
                )

                Box {
                    PickerFilterChip(
                        label = selectedClub ?: strings.byClub,
                        selected = selectedClub != null,
                        onClick = { showClubMenu = true }
                    )
                    DropdownMenu(
                        expanded = showClubMenu,
                        onDismissRequest = { showClubMenu = false },
                        modifier = Modifier.height(280.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text(strings.all) },
                            onClick = {
                                showClubMenu = false
                                selectedClub = null
                            }
                        )
                        clubs.forEach { club ->
                            DropdownMenuItem(
                                text = { Text(club) },
                                onClick = {
                                    showClubMenu = false
                                    selectedClub = club
                                }
                            )
                        }
                    }
                }

                Box {
                    PickerFilterChip(
                        label = selectedNation ?: strings.byNationality,
                        selected = selectedNation != null,
                        onClick = { showNationMenu = true }
                    )
                    DropdownMenu(
                        expanded = showNationMenu,
                        onDismissRequest = { showNationMenu = false },
                        modifier = Modifier.height(280.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text(strings.all) },
                            onClick = {
                                showNationMenu = false
                                selectedNation = null
                            }
                        )
                        nations.forEach { nation ->
                            DropdownMenuItem(
                                text = { Text(nation) },
                                onClick = {
                                    showNationMenu = false
                                    selectedNation = nation
                                }
                            )
                        }
                    }
                }

                Box {
                    PickerFilterChip(
                        label = selectedLeague ?: strings.byLeague,
                        selected = selectedLeague != null,
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
                                selectedLeague = null
                            }
                        )
                        leagues.forEach { league ->
                            DropdownMenuItem(
                                text = { Text(league) },
                                onClick = {
                                    showLeagueMenu = false
                                    selectedLeague = league
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(results, key = { it.id }) { player ->
                    val slotPower = player.powerForSlot(targetSlot.slotPosition)
                    val lineColor = player.mainLine.color
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(cs.surface)
                            .border(1.8.dp, lineColor, RoundedCornerShape(10.dp))
                            .clickable { onSelectPlayer(player.id) }
                            .padding(8.dp)
                            .testTag("picker_player_${player.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(targetSlot.slotPosition.line.color)
                                .padding(horizontal = 9.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = slotPower.toString(),
                                color = if (targetSlot.slotPosition.line == PlayingLine.MID) Color.Black else Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PositionRoleLogo(line = player.mainLine, tint = lineColor, size = 13.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = player.mainPosition,
                                    color = lineColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = player.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = cs.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = player.nationality,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = cs.onSurface
                                )
                                Text(
                                    text = " • ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = cs.onSurfaceVariant
                                )
                                ClubCrestBadge(
                                    clubName = player.club,
                                    customClubLogoUri = player.customClubLogoUri,
                                    size = 14.dp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${player.club} • ${player.league}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = cs.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            val abilities = player.abilityNicknameList.take(2)
                            if (abilities.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = abilities.joinToString(" • ") { AbilityNicknameCatalog.displayName(it, appLang) },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FifOrange,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        PlayerPortrait(
                            customImageUri = player.customImageUri,
                            avatarGender = AvatarGender.fromName(player.avatarGender),
                            contentDescription = player.name,
                            modifier = Modifier
                                .size(width = 40.dp, height = 60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.5.dp, lineColor, RoundedCornerShape(8.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerFilterChip(
    label: String,
    selected: Boolean,
    leadingStar: Boolean = false,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) FifOrange else cs.surfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, if (selected) FifOrange else cs.outline, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingStar) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = if (selected) Color.White else FifOrange,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Color.White else cs.onSurface
            )
        }
    }
}
