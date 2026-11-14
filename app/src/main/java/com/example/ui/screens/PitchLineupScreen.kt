package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.LocalAppStrings
import com.example.model.AppLanguage
import com.example.model.FormationCatalog
import com.example.model.PitchTheme
import com.example.model.TeamSide
import com.example.ui.FifCardUiState
import com.example.ui.SlotContext
import com.example.ui.components.CoachPitchCard
import com.example.ui.components.EmptyPitchSlot
import com.example.ui.components.FootballPitchCanvas
import com.example.ui.components.SquarePitchPlayerCard
import com.example.ui.theme.FifOrange
import kotlin.math.hypot
import kotlin.math.roundToInt

private data class ActiveDragState(
    val sourceSlot: SlotContext,
    val pointerRootOffset: Offset,
    val hoveredTargetKey: String?
)

@Composable
fun PitchLineupScreen(
    uiState: FifCardUiState,
    onSetMatchMode: (Boolean) -> Unit,
    onSetActiveMatchSide: (TeamSide) -> Unit,
    onSelectFormation: (TeamSide, String) -> Unit,
    onSelectPitchTheme: (PitchTheme) -> Unit,
    onSlotTap: (SlotContext) -> Unit,
    onSwapSlots: (String, String) -> Unit,
    onInspectSelected: () -> Unit,
    onCancelSelection: () -> Unit,
    onAddSubSlot: (TeamSide) -> Unit,
    onRemoveSubSlot: (String) -> Unit,
    onCoachClick: (TeamSide) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val cs = MaterialTheme.colorScheme
    val isMatchMode = uiState.prefs.isMatchMode
    val activeSide = if (isMatchMode) uiState.activeMatchSide else TeamSide.HOME

    // Root bounds & slot hit-boxes for fast zero-allocation Drag-and-Drop
    var rootTopLeft by remember { mutableStateOf(Offset.Zero) }
    val slotBounds = remember { mutableStateMapOf<String, Rect>() }
    var activeDrag by remember { mutableStateOf<ActiveDragState?>(null) }

    var showFormationMenu by remember { mutableStateOf(false) }
    var showPitchMenu by remember { mutableStateOf(false) }

    val activeFormation = if (activeSide == TeamSide.HOME) uiState.homeFormation else uiState.awayFormation
    val activeSubs = if (activeSide == TeamSide.HOME) uiState.homeSubSlots else uiState.awaySubSlots

    fun findHoveredSlotKey(rootOffset: Offset, sourceKey: String): String? {
        return slotBounds.entries.firstOrNull { (key, rect) ->
            key != sourceKey && rect.contains(rootOffset)
        }?.key
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                rootTopLeft = coords.boundsInRoot().topLeft
            }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 700.dp

            Column(modifier = Modifier.fillMaxSize()) {
                // Tactical Controls Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cs.surface)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 1 Team vs 2 Teams Match Mode Toggle
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isMatchMode) FifOrange else cs.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                1.dp,
                                if (isMatchMode) FifOrange else cs.outline,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSetMatchMode(!isMatchMode) }
                            .testTag("match_mode_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SportsSoccer,
                                contentDescription = null,
                                tint = if (isMatchMode) Color.White else FifOrange,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isMatchMode) strings.matchMode else strings.singleTeamMode,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isMatchMode) Color.White else cs.onSurface
                            )
                        }
                    }

                    if (isMatchMode) {
                        // Home / Away active team pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(cs.surfaceVariant)
                                .border(1.dp, cs.outline, RoundedCornerShape(8.dp))
                        ) {
                            listOf(TeamSide.HOME, TeamSide.AWAY).forEach { side ->
                                val selected = activeSide == side
                                val avg = if (side == TeamSide.HOME) uiState.homeAveragePower else uiState.awayAveragePower
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(if (selected) FifOrange else Color.Transparent)
                                        .clickable { onSetActiveMatchSide(side) }
                                        .padding(horizontal = 7.dp, vertical = 6.dp)
                                        .testTag("match_side_${side.name}")
                                ) {
                                    Text(
                                        text = if (side == TeamSide.HOME) "H:$avg" else "A:$avg",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) Color.White else cs.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Formation Dropdown
                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = cs.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, cs.outline, RoundedCornerShape(8.dp))
                                .clickable { showFormationMenu = true }
                                .testTag("formation_picker_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = activeFormation.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = cs.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Filled.ExpandMore,
                                    contentDescription = strings.formation,
                                    tint = FifOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showFormationMenu,
                            onDismissRequest = { showFormationMenu = false },
                            modifier = Modifier.height(340.dp)
                        ) {
                            FormationCatalog.all.forEach { form ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = form.name,
                                            fontWeight = if (form.id == activeFormation.id) FontWeight.ExtraBold else FontWeight.Normal,
                                            color = if (form.id == activeFormation.id) FifOrange else cs.onSurface
                                        )
                                    },
                                    onClick = {
                                        showFormationMenu = false
                                        onSelectFormation(activeSide, form.id)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Team OVR badge (in single team mode)
                    if (!isMatchMode) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = cs.primaryContainer
                        ) {
                            Text(
                                text = "${strings.teamPower} ${uiState.homeAveragePower}",
                                style = MaterialTheme.typography.labelMedium,
                                color = cs.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Pitch Color Selector
                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = uiState.prefs.pitchTheme.baseColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.5.dp, cs.outline, RoundedCornerShape(8.dp))
                                .clickable { showPitchMenu = true }
                                .testTag("pitch_color_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Palette,
                                    contentDescription = strings.pitchColor,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showPitchMenu,
                            onDismissRequest = { showPitchMenu = false }
                        ) {
                            PitchTheme.entries.forEach { theme ->
                                val isArabic = uiState.prefs.language == AppLanguage.ARABIC
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(theme.baseColor)
                                                .border(1.5.dp, theme.lineColor, CircleShape)
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = if (isArabic) theme.nameAr else theme.nameEn,
                                            fontWeight = if (theme == uiState.prefs.pitchTheme) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        showPitchMenu = false
                                        onSelectPitchTheme(theme)
                                    }
                                )
                            }
                        }
                    }
                }

                // Selection / Swap Action Banner OR Tip Strip
                if (uiState.selectedSlotKey != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FifOrange)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SwapHoriz,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.tapToSwapHint,
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1
                            )
                        }
                        Row {
                            TextButton(
                                onClick = onInspectSelected,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = strings.editCard,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(strings.editCard, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            TextButton(
                                onClick = onCancelSelection,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(strings.cancelSelection, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    Text(
                        text = strings.dragOrTapTip,
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cs.surfaceVariant.copy(alpha = 0.6f))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }

                // Responsive Pitch + Infinite Substitutions Layout
                if (isWideScreen) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PitchArea(
                            uiState = uiState,
                            isMatchMode = isMatchMode,
                            activeDrag = activeDrag,
                            onSlotPositioned = { key, rect -> slotBounds[key] = rect },
                            onSlotTap = onSlotTap,
                            onDragStart = { slot, rootPt ->
                                activeDrag = ActiveDragState(slot, rootPt, findHoveredSlotKey(rootPt, slot.slotKey))
                            },
                            onDragMove = { rootPt ->
                                activeDrag?.let { curr ->
                                    activeDrag = curr.copy(
                                        pointerRootOffset = rootPt,
                                        hoveredTargetKey = findHoveredSlotKey(rootPt, curr.sourceSlot.slotKey)
                                    )
                                }
                            },
                            onDragEnd = {
                                val d = activeDrag
                                activeDrag = null
                                if (d?.hoveredTargetKey != null) {
                                    onSwapSlots(d.sourceSlot.slotKey, d.hoveredTargetKey)
                                }
                            },
                            onCoachClick = onCoachClick,
                            modifier = Modifier
                                .weight(1.7f)
                                .fillMaxHeight()
                        )

                        SubstitutionsBenchPane(
                            subs = activeSubs,
                            side = activeSide,
                            selectedSlotKey = uiState.selectedSlotKey,
                            activeDrag = activeDrag,
                            isVertical = true,
                            onSlotPositioned = { key, rect -> slotBounds[key] = rect },
                            onSlotTap = onSlotTap,
                            onDragStart = { slot, rootPt ->
                                activeDrag = ActiveDragState(slot, rootPt, findHoveredSlotKey(rootPt, slot.slotKey))
                            },
                            onDragMove = { rootPt ->
                                activeDrag?.let { curr ->
                                    activeDrag = curr.copy(
                                        pointerRootOffset = rootPt,
                                        hoveredTargetKey = findHoveredSlotKey(rootPt, curr.sourceSlot.slotKey)
                                    )
                                }
                            },
                            onDragEnd = {
                                val d = activeDrag
                                activeDrag = null
                                if (d?.hoveredTargetKey != null) {
                                    onSwapSlots(d.sourceSlot.slotKey, d.hoveredTargetKey)
                                }
                            },
                            onAddSubSlot = { onAddSubSlot(activeSide) },
                            onRemoveSubSlot = onRemoveSubSlot,
                            modifier = Modifier
                                .width(130.dp)
                                .fillMaxHeight()
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        PitchArea(
                            uiState = uiState,
                            isMatchMode = isMatchMode,
                            activeDrag = activeDrag,
                            onSlotPositioned = { key, rect -> slotBounds[key] = rect },
                            onSlotTap = onSlotTap,
                            onDragStart = { slot, rootPt ->
                                activeDrag = ActiveDragState(slot, rootPt, findHoveredSlotKey(rootPt, slot.slotKey))
                            },
                            onDragMove = { rootPt ->
                                activeDrag?.let { curr ->
                                    activeDrag = curr.copy(
                                        pointerRootOffset = rootPt,
                                        hoveredTargetKey = findHoveredSlotKey(rootPt, curr.sourceSlot.slotKey)
                                    )
                                }
                            },
                            onDragEnd = {
                                val d = activeDrag
                                activeDrag = null
                                if (d?.hoveredTargetKey != null) {
                                    onSwapSlots(d.sourceSlot.slotKey, d.hoveredTargetKey)
                                }
                            },
                            onCoachClick = onCoachClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )

                        SubstitutionsBenchPane(
                            subs = activeSubs,
                            side = activeSide,
                            selectedSlotKey = uiState.selectedSlotKey,
                            activeDrag = activeDrag,
                            isVertical = false,
                            onSlotPositioned = { key, rect -> slotBounds[key] = rect },
                            onSlotTap = onSlotTap,
                            onDragStart = { slot, rootPt ->
                                activeDrag = ActiveDragState(slot, rootPt, findHoveredSlotKey(rootPt, slot.slotKey))
                            },
                            onDragMove = { rootPt ->
                                activeDrag?.let { curr ->
                                    activeDrag = curr.copy(
                                        pointerRootOffset = rootPt,
                                        hoveredTargetKey = findHoveredSlotKey(rootPt, curr.sourceSlot.slotKey)
                                    )
                                }
                            },
                            onDragEnd = {
                                val d = activeDrag
                                activeDrag = null
                                if (d?.hoveredTargetKey != null) {
                                    onSwapSlots(d.sourceSlot.slotKey, d.hoveredTargetKey)
                                }
                            },
                            onAddSubSlot = { onAddSubSlot(activeSide) },
                            onRemoveSubSlot = onRemoveSubSlot,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Floating Ghost Card following finger during Drag-and-Drop
            val drag = activeDrag
            val draggedPlayer = drag?.sourceSlot?.player
            if (drag != null && draggedPlayer != null) {
                val localPt = drag.pointerRootOffset - rootTopLeft
                val cardDp = 64.dp
                val halfPx = with(LocalDensity.current) { (cardDp / 2).toPx() }
                SquarePitchPlayerCard(
                    player = draggedPlayer,
                    slotPosition = drag.sourceSlot.slotPosition,
                    cardSize = cardDp,
                    isSelected = true,
                    isDropTarget = false,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (localPt.x - halfPx).roundToInt(),
                                (localPt.y - halfPx).roundToInt()
                            )
                        }
                        .alpha(0.92f)
                )
            }
        }
    }
}

@Composable
private fun PitchArea(
    uiState: FifCardUiState,
    isMatchMode: Boolean,
    activeDrag: ActiveDragState?,
    onSlotPositioned: (String, Rect) -> Unit,
    onSlotTap: (SlotContext) -> Unit,
    onDragStart: (SlotContext, Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onCoachClick: (TeamSide) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
    ) {
        val pitchWidthPx = constraints.maxWidth.toFloat()
        val pitchHeightPx = constraints.maxHeight.toFloat()

        // Responsive card size for Snapdragon 662 phones & tablets across aspect ratios
        val cardDp: Dp = if (isMatchMode) {
            minOf(maxWidth / 7.4f, maxHeight / 9.2f).coerceIn(38.dp, 58.dp)
        } else {
            minOf(maxWidth / 5.7f, maxHeight / 6.3f).coerceIn(48.dp, 76.dp)
        }
        val cardSizePx = with(LocalDensity.current) { cardDp.toPx() }

        FootballPitchCanvas(
            pitchTheme = uiState.prefs.pitchTheme,
            isMatchMode = isMatchMode
        )

        // Home XI Slots
        val homeFormation = uiState.homeFormation
        uiState.homeStarterSlots.forEachIndexed { idx, slot ->
            val fSlot = homeFormation.slots.getOrNull(idx) ?: return@forEachIndexed
            val nx = fSlot.x
            // Single team: 0.08..0.92; Match mode: bottom half 0.54..0.93
            val ny = if (isMatchMode) 0.54f + fSlot.y * 0.40f else fSlot.y
            val px = (nx * pitchWidthPx - cardSizePx / 2f).coerceIn(4f, pitchWidthPx - cardSizePx - 4f)
            val py = (ny * pitchHeightPx - cardSizePx / 2f).coerceIn(4f, pitchHeightPx - cardSizePx - 4f)

            PitchSlotItem(
                slot = slot,
                cardSize = cardDp,
                isSelected = uiState.selectedSlotKey == slot.slotKey,
                isDropTarget = activeDrag?.hoveredTargetKey == slot.slotKey,
                isDraggedSource = activeDrag?.sourceSlot?.slotKey == slot.slotKey,
                isAwayTeam = false,
                onPositioned = { rect -> onSlotPositioned(slot.slotKey, rect) },
                onTap = { onSlotTap(slot) },
                onDragStart = { rootPt -> onDragStart(slot, rootPt) },
                onDragMove = onDragMove,
                onDragEnd = onDragEnd,
                modifier = Modifier.offset { IntOffset(px.roundToInt(), py.roundToInt()) }
            )
        }

        // Away XI Slots (Match Mode: 2 teams on one field pitch!)
        if (isMatchMode) {
            val awayFormation = uiState.awayFormation
            uiState.awayStarterSlots.forEachIndexed { idx, slot ->
                val fSlot = awayFormation.slots.getOrNull(idx) ?: return@forEachIndexed
                val nx = 1f - fSlot.x
                // Away GK at top (y=0.07), Away strikers near halfway line (y=0.45)
                val ny = 0.46f - fSlot.y * 0.40f
                val px = (nx * pitchWidthPx - cardSizePx / 2f).coerceIn(4f, pitchWidthPx - cardSizePx - 4f)
                val py = (ny * pitchHeightPx - cardSizePx / 2f).coerceIn(4f, pitchHeightPx - cardSizePx - 4f)

                PitchSlotItem(
                    slot = slot,
                    cardSize = cardDp,
                    isSelected = uiState.selectedSlotKey == slot.slotKey,
                    isDropTarget = activeDrag?.hoveredTargetKey == slot.slotKey,
                    isDraggedSource = activeDrag?.sourceSlot?.slotKey == slot.slotKey,
                    isAwayTeam = true,
                    onPositioned = { rect -> onSlotPositioned(slot.slotKey, rect) },
                    onTap = { onSlotTap(slot) },
                    onDragStart = { rootPt -> onDragStart(slot, rootPt) },
                    onDragMove = onDragMove,
                    onDragEnd = onDragEnd,
                    modifier = Modifier.offset { IntOffset(px.roundToInt(), py.roundToInt()) }
                )
            }

            // Away Coach Card at top-left in Match Mode
            CoachPitchCard(
                coach = uiState.awayCoach,
                onClick = { onCoachClick(TeamSide.AWAY) },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            )
        }

        // Home Coach Card on bottom-left of formation (Required by spec!)
        CoachPitchCard(
            coach = uiState.homeCoach,
            onClick = { onCoachClick(TeamSide.HOME) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        )
    }
}

@Composable
private fun PitchSlotItem(
    slot: SlotContext,
    cardSize: Dp,
    isSelected: Boolean,
    isDropTarget: Boolean,
    isDraggedSource: Boolean,
    isAwayTeam: Boolean,
    onPositioned: (Rect) -> Unit,
    onTap: () -> Unit,
    onDragStart: (Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    var slotTopLeft by remember { mutableStateOf(Offset.Zero) }

    val gestureMod = Modifier
        .onGloballyPositioned { coords ->
            val bounds = coords.boundsInRoot()
            slotTopLeft = bounds.topLeft
            onPositioned(bounds)
        }
        .pointerInput(slot.slotKey, slot.player?.id) {
            val slop = viewConfiguration.touchSlop
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val startLocal = down.position
                var dragging = false
                var totalDx = 0f
                var totalDy = 0f

                if (slot.player == null) {
                    // Empty slot: tap immediately on release
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            onTap()
                            break
                        }
                        val d = change.position - startLocal
                        if (hypot(d.x, d.y) > slop * 2f) break
                    }
                } else {
                    // Occupied slot: either tap (for tap-to-swap / edit) or immediate drag after slop!
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            if (dragging) {
                                onDragEnd()
                            } else {
                                onTap()
                            }
                            break
                        }
                        val delta = change.positionChange()
                        totalDx += delta.x
                        totalDy += delta.y
                        if (!dragging && hypot(totalDx, totalDy) > slop) {
                            dragging = true
                            change.consume()
                            onDragStart(slotTopLeft + change.position)
                            drag(down.id) { dragChange ->
                                dragChange.consume()
                                onDragMove(slotTopLeft + dragChange.position)
                            }
                            onDragEnd()
                            break
                        }
                    }
                }
            }
        }
        .alpha(if (isDraggedSource) 0.35f else 1f)
        .testTag("slot_${slot.slotKey}")

    if (slot.player != null) {
        SquarePitchPlayerCard(
            player = slot.player,
            slotPosition = slot.slotPosition,
            cardSize = cardSize,
            isSelected = isSelected,
            isDropTarget = isDropTarget,
            isTeamAwayBadge = isAwayTeam,
            modifier = modifier.then(gestureMod)
        )
    } else {
        EmptyPitchSlot(
            slotPosition = slot.slotPosition,
            cardSize = cardSize,
            isDropTarget = isDropTarget,
            modifier = modifier.then(gestureMod)
        )
    }
}

@Composable
private fun SubstitutionsBenchPane(
    subs: List<SlotContext>,
    side: TeamSide,
    selectedSlotKey: String?,
    activeDrag: ActiveDragState?,
    isVertical: Boolean,
    onSlotPositioned: (String, Rect) -> Unit,
    onSlotTap: (SlotContext) -> Unit,
    onDragStart: (SlotContext, Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onAddSubSlot: () -> Unit,
    onRemoveSubSlot: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val cs = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cs.surface,
        modifier = modifier.border(1.dp, cs.outline, RoundedCornerShape(12.dp))
    ) {
        if (isVertical) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${strings.substitutions} (${subs.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    subs.forEach { slot ->
                        SubSlotWrapper(
                            slot = slot,
                            canDelete = subs.size > 1 && slot.player == null,
                            selectedSlotKey = selectedSlotKey,
                            activeDrag = activeDrag,
                            onSlotPositioned = onSlotPositioned,
                            onSlotTap = onSlotTap,
                            onDragStart = onDragStart,
                            onDragMove = onDragMove,
                            onDragEnd = onDragEnd,
                            onRemove = { onRemoveSubSlot(slot.slotKey) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                AddSubButton(onClick = onAddSubSlot, label = strings.addSubSlot)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.padding(end = 8.dp)) {
                    Text(
                        text = strings.substitutions,
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant
                    )
                    Text(
                        text = "${subs.count { it.player != null }}/${subs.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = FifOrange
                    )
                }

                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(subs, key = { it.slotKey }) { slot ->
                        SubSlotWrapper(
                            slot = slot,
                            canDelete = subs.size > 1 && slot.player == null,
                            selectedSlotKey = selectedSlotKey,
                            activeDrag = activeDrag,
                            onSlotPositioned = onSlotPositioned,
                            onSlotTap = onSlotTap,
                            onDragStart = onDragStart,
                            onDragMove = onDragMove,
                            onDragEnd = onDragEnd,
                            onRemove = { onRemoveSubSlot(slot.slotKey) }
                        )
                    }
                    item {
                        AddSubButton(onClick = onAddSubSlot, label = strings.addSubSlot)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubSlotWrapper(
    slot: SlotContext,
    canDelete: Boolean,
    selectedSlotKey: String?,
    activeDrag: ActiveDragState?,
    onSlotPositioned: (String, Rect) -> Unit,
    onSlotTap: (SlotContext) -> Unit,
    onDragStart: (SlotContext, Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onRemove: () -> Unit
) {
    Box {
        PitchSlotItem(
            slot = slot,
            cardSize = 56.dp,
            isSelected = selectedSlotKey == slot.slotKey,
            isDropTarget = activeDrag?.hoveredTargetKey == slot.slotKey,
            isDraggedSource = activeDrag?.sourceSlot?.slotKey == slot.slotKey,
            isAwayTeam = slot.teamSide == TeamSide.AWAY,
            onPositioned = { rect -> onSlotPositioned(slot.slotKey, rect) },
            onTap = { onSlotTap(slot) },
            onDragStart = { rootPt -> onDragStart(slot, rootPt) },
            onDragMove = onDragMove,
            onDragEnd = onDragEnd
        )
        if (canDelete) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.78f))
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove sub slot",
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}

@Composable
private fun AddSubButton(
    onClick: () -> Unit,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = FifOrange.copy(alpha = 0.16f),
        modifier = Modifier
            .size(width = 58.dp, height = 56.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.5.dp, FifOrange, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag("add_infinite_sub_button")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = label,
                tint = FifOrange,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                color = FifOrange,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
