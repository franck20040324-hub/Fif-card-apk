package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
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
import com.example.data.local.CoachEntity
import com.example.i18n.LocalAppStrings
import com.example.model.ManagerSortOption
import com.example.ui.FifCardUiState
import com.example.ui.ManagerFilterState
import com.example.ui.components.ManagerCardGrid
import com.example.ui.components.ManagerCardList
import com.example.ui.theme.FifOrange

@Composable
fun ManagersListScreen(
    uiState: FifCardUiState,
    onUpdateFilter: ((ManagerFilterState) -> ManagerFilterState) -> Unit,
    onSetGridView: (Boolean) -> Unit,
    onManagerClick: (CoachEntity) -> Unit,
    onCreateManagerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val cs = MaterialTheme.colorScheme
    val filter = uiState.managerFilterState
    val isGrid = uiState.prefs.isManagersGridView

    var showSortMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search + Grid/List Toggle + Sort Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cs.surface)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = filter.query,
                    onValueChange = { q -> onUpdateFilter { it.copy(query = q) } },
                    placeholder = {
                        Text(
                            text = strings.managersSearchPlaceholder,
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
                        .testTag("managers_search_input")
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = cs.surfaceVariant,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, cs.outline, RoundedCornerShape(10.dp))
                        .clickable { onSetGridView(!isGrid) }
                        .testTag("managers_view_toggle")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isGrid) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.GridView,
                            contentDescription = "Toggle Grid/List",
                            tint = FifOrange
                        )
                    }
                }

                Box {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = cs.surfaceVariant,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, cs.outline, RoundedCornerShape(10.dp))
                            .clickable { showSortMenu = true }
                            .testTag("managers_sort_button")
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
                            ManagerSortOption.NAME_ASC to strings.sortNameAsc,
                            ManagerSortOption.NAME_DESC to strings.sortNameDesc,
                            ManagerSortOption.NATIONALITY to strings.sortNationality
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

            if (uiState.filteredCoaches.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.noManagersFound,
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
                    items(uiState.filteredCoaches, key = { it.id }) { coach ->
                        ManagerCardGrid(
                            coach = coach,
                            isActiveOnPitch = coach.id == uiState.activeCoach.id,
                            onClick = { onManagerClick(coach) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(10.dp, 10.dp, 10.dp, 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.filteredCoaches, key = { it.id }) { coach ->
                        ManagerCardList(
                            coach = coach,
                            isActiveOnPitch = coach.id == uiState.activeCoach.id,
                            onClick = { onManagerClick(coach) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateManagerClick,
            containerColor = FifOrange,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("create_manager_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Add, contentDescription = strings.createNewManager)
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.createNewManager, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
