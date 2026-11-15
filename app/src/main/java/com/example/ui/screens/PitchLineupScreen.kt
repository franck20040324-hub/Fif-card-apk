package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SavedFormationEntity
import com.example.i18n.LocalAppStrings
import com.example.model.AppLanguage
import com.example.model.FormationCatalog
import com.example.model.PitchTheme
import com.example.ui.FifCardUiState
import com.example.ui.SlotContext
import com.example.ui.components.CoachPitchCard
import com.example.ui.components.EmptyPitchSlot
import com.example.ui.components.FootballPitchCanvas
import com.example.ui.components.VERTICAL_CARD_RATIO
import com.example.ui.components.VerticalPitchPlayerCard
import com.example.ui.theme.FifOrange
import kotlin.math.hypot
import kotlin.math.roundToInt

private data class ActiveDragState(
    val sourceSlot: SlotContext,
    val cardCenterRootOffset: Offset,
    val draggedCardWidth: Dp,
    val draggedCardHeight: Dp,
    val hoveredTargetKey: String?
)

@Composable
fun PitchLineupScreen(
    uiState: FifCardUiState,
    onSelectFormation: (String) -> Unit,
    onLoadSavedFormation: (SavedFormationEntity) -> Unit,
    onOpenSaveFormation: () -> Unit,
    onOpenSavedFormations: () -> Unit,
    onOpenCreateFormation: () -> Unit,
    onSelectPitchTheme: (PitchTheme) -> Unit,
    onSlotTap: (SlotContext) -> Unit,
    onHoldForArrows: (SlotContext) -> Unit,
    onNudgeSlot: (String, Float, Float) -> Unit,
    onResetSlotPosition: (String) -> Unit,
    onFinishRepositioning: () -> Unit,
    onSwapSlots: (String, String) -> Unit,
    onInspectSelected: () -> Unit,
    onCancelSelection: () -> Unit,
    onAddSubSlot: () -> Unit,
    onRemoveSubSlot: (String) -> Unit,
    onCoachClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val cs = MaterialTheme.colorScheme

    var rootTopLeft by remember { mutableStateOf(Offset.Zero) }
    val slotBounds = remember { mutableStateMapOf<String, Rect>() }
    var activeDrag by remember { mutableStateOf<ActiveDragState?>(null) }

    var showFormationMenu by remember { mutableStateOf(false) }
    var showPitchMenu by remember { mutableStateOf(false) }

    val activeFormation = uiState.homeFormation
    val activeFormationTitle = uiState.prefs.activeFormationLabel.ifBlank { activeFormation.name }
    val activeSubs = uiState.homeSubSlots
    val density = LocalDensity.current

    fun findHoveredSlotKey(cardCenterRoot: Offset, cardW: Dp, cardH: Dp, sourceKey: String): String? {
        val halfW = with(density) { (cardW / 2).toPx() }
        val halfH = with(density) { (cardH / 2).toPx() }
        val dragRect = Rect(
            left = cardCenterRoot.x - halfW,
            top = cardCenterRoot.y - halfH,
            right = cardCenterRoot.x + halfW,
            bottom = cardCenterRoot.y + halfH
        )
        // Prefer slot directly under dragged card center, otherwise maximum overlap area
        val directHit = slotBounds.entries.firstOrNull { (key, rect) ->
            key != sourceKey && rect.contains(cardCenterRoot)
        }?.key
        if (directHit != null) return directHit

        var bestKey: String? = null
        var bestArea = 0f
        for ((key, rect) in slotBounds) {
            if (key == sourceKey) continue
            val interLeft = maxOf(dragRect.left, rect.left)
            val interTop = maxOf(dragRect.top, rect.top)
            val interRight = minOf(dragRect.right, rect.right)
            val interBottom = minOf(dragRect.bottom, rect.bottom)
            if (interRight > interLeft && interBottom > interTop) {
                val area = (interRight - interLeft) * (interBottom - interTop)
                if (area > bestArea) {
                    bestArea = area
                    bestKey = key
                }
            }
        }
        return bestKey
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
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // Formation Dropdown (presets + saved custom formations)
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
                                    text = activeFormationTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = cs.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Filled.ExpandMore,
                                    contentDescription = strings.formation,
                                    tint = FifOrange,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showFormationMenu,
                            onDismissRequest = { showFormationMenu = false },
                            modifier = Modifier.height(360.dp)
                        ) {
                            if (uiState.savedFormations.isNotEmpty()) {
                                uiState.savedFormations.forEach { saved ->
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(Icons.Filled.Bookmark, contentDescription = null, tint = FifOrange, modifier = Modifier.size(16.dp))
                                        },
                                        text = {
                                            Text(
                                                text = saved.name,
                                                fontWeight = if (saved.name == activeFormationTitle) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                color = if (saved.name == activeFormationTitle) FifOrange else cs.onSurface
                                            )
                                        },
                                        onClick = {
                                            showFormationMenu = false
                                            onLoadSavedFormation(saved)
                                        }
                                    )
                                }
                                HorizontalDivider()
                            }
                            FormationCatalog.all.forEach { form ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = form.name,
                                            fontWeight = if (form.name == activeFormationTitle) FontWeight.ExtraBold else FontWeight.Normal,
                                            color = if (form.name == activeFormationTitle) FifOrange else cs.onSurface
                                        )
                                    },
                                    onClick = {
                                        showFormationMenu = false
                                        onSelectFormation(form.id)
                                    }
                                )
                            }
                        }
                    }

                    // Save Formation locally
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = cs.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, cs.outline, RoundedCornerShape(8.dp))
                            .clickable(onClick = onOpenSaveFormation)
                            .testTag("save_formation_button")
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BookmarkAdd,
                                contentDescription = strings.saveFormation,
                                tint = FifOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Load Saved Formations from local storage
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = cs.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, cs.outline, RoundedCornerShape(8.dp))
                            .clickable(onClick = onOpenSavedFormations)
                            .testTag("saved_formations_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bookmark,
                                contentDescription = strings.savedFormations,
                                tint = FifOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${uiState.savedFormations.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = cs.primaryContainer
                    ) {
                        Text(
                            text = "${strings.teamPower} ${uiState.homeAveragePower}",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                        )
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
                            Box(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
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

                // Active Mode Banner (Repositioning with Arrows / Tap-to-Swap / Tip)
                val repoKey = uiState.repositioningSlotKey
                if (repoKey != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FifOrange)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.OpenWith, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.repositionHint, color = Color.White, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                        }
                        Row {
                            TextButton(
                                onClick = { onResetSlotPosition(repoKey) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Filled.RestartAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(strings.resetPosition, color = Color.White, fontSize = 11.sp)
                            }
                            TextButton(
                                onClick = onFinishRepositioning,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(strings.done, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (uiState.selectedSlotKey != null) {
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

                // Responsive Vertical Rectangular Pitch + Infinite Substitutions Layout
                if (isWideScreen) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.7f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            PitchArea(
                                uiState = uiState,
                                activeDrag = activeDrag,
                                onSlotPositioned = { key, rect -> slotBounds[key] = rect },
                                onSlotTap = onSlotTap,
                                onHoldForArrows = onHoldForArrows,
                                onNudgeSlot = onNudgeSlot,
                                onDragStart = { slot, centerRoot, w, h ->
                                    activeDrag = ActiveDragState(
                                        sourceSlot = slot,
                                        cardCenterRootOffset = centerRoot,
                                        draggedCardWidth = w,
                                        draggedCardHeight = h,
                                        hoveredTargetKey = findHoveredSlotKey(centerRoot, w, h, slot.slotKey)
                                    )
                                },
                                onDragMove = { centerRoot ->
                                    activeDrag?.let { curr ->
                                        activeDrag = curr.copy(
                                            cardCenterRootOffset = centerRoot,
                                            hoveredTargetKey = findHoveredSlotKey(
                                                centerRoot,
                                                curr.draggedCardWidth,
                                                curr.draggedCardHeight,
                                                curr.sourceSlot.slotKey
                                            )
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
                                    .fillMaxHeight()
                                    .aspectRatio(0.62f, matchHeightConstraintsFirst = true)
                            )

                            FloatingActionButton(
                                onClick = onOpenCreateFormation,
                                containerColor = FifOrange,
                                contentColor = Color.White,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                                    .testTag("create_formation_fab")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = strings.createNewFormation)
                            }
                        }

                        SubstitutionsBenchPane(
                            subs = activeSubs,
                            selectedSlotKey = uiState.selectedSlotKey,
                            activeDrag = activeDrag,
                            isVertical = true,
                            onSlotPositioned = { key, rect -> slotBounds[key] = rect },
                            onSlotTap = onSlotTap,
                            onDragStart = { slot, centerRoot, w, h ->
                                activeDrag = ActiveDragState(
                                    sourceSlot = slot,
                                    cardCenterRootOffset = centerRoot,
                                    draggedCardWidth = w,
                                    draggedCardHeight = h,
                                    hoveredTargetKey = findHoveredSlotKey(centerRoot, w, h, slot.slotKey)
                                )
                            },
                            onDragMove = { centerRoot ->
                                activeDrag?.let { curr ->
                                    activeDrag = curr.copy(
                                        cardCenterRootOffset = centerRoot,
                                        hoveredTargetKey = findHoveredSlotKey(
                                            centerRoot,
                                            curr.draggedCardWidth,
                                            curr.draggedCardHeight,
                                            curr.sourceSlot.slotKey
                                        )
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
                            onAddSubSlot = onAddSubSlot,
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
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            PitchArea(
                                uiState = uiState,
                                activeDrag = activeDrag,
                                onSlotPositioned = { key, rect -> slotBounds[key] = rect },
                                onSlotTap = onSlotTap,
                                onHoldForArrows = onHoldForArrows,
                                onNudgeSlot = onNudgeSlot,
                                onDragStart = { slot, centerRoot, w, h ->
                                    activeDrag = ActiveDragState(
                                        sourceSlot = slot,
                                        cardCenterRootOffset = centerRoot,
                                        draggedCardWidth = w,
                                        draggedCardHeight = h,
                                        hoveredTargetKey = findHoveredSlotKey(centerRoot, w, h, slot.slotKey)
                                    )
                                },
                                onDragMove = { centerRoot ->
                                    activeDrag?.let { curr ->
                                        activeDrag = curr.copy(
                                            cardCenterRootOffset = centerRoot,
                                            hoveredTargetKey = findHoveredSlotKey(
                                                centerRoot,
                                                curr.draggedCardWidth,
                                                curr.draggedCardHeight,
                                                curr.sourceSlot.slotKey
                                            )
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
                                    .fillMaxHeight()
                                    .aspectRatio(0.64f, matchHeightConstraintsFirst = true)
                            )

                            FloatingActionButton(
                                onClick = onOpenCreateFormation,
                                containerColor = FifOrange,
                                contentColor = Color.White,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(6.dp)
                                    .testTag("create_formation_fab")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = strings.createNewFormation)
                            }
                        }

                        SubstitutionsBenchPane(
                            subs = activeSubs,
                            selectedSlotKey = uiState.selectedSlotKey,
                            activeDrag = activeDrag,
                            isVertical = false,
                            onSlotPositioned = { key, rect -> slotBounds[key] = rect },
                            onSlotTap = onSlotTap,
                            onDragStart = { slot, centerRoot, w, h ->
                                activeDrag = ActiveDragState(
                                    sourceSlot = slot,
                                    cardCenterRootOffset = centerRoot,
                                    draggedCardWidth = w,
                                    draggedCardHeight = h,
                                    hoveredTargetKey = findHoveredSlotKey(centerRoot, w, h, slot.slotKey)
                                )
                            },
                            onDragMove = { centerRoot ->
                                activeDrag?.let { curr ->
                                    activeDrag = curr.copy(
                                        cardCenterRootOffset = centerRoot,
                                        hoveredTargetKey = findHoveredSlotKey(
                                            centerRoot,
                                            curr.draggedCardWidth,
                                            curr.draggedCardHeight,
                                            curr.sourceSlot.slotKey
                                        )
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
                            onAddSubSlot = onAddSubSlot,
                            onRemoveSubSlot = onRemoveSubSlot,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Floating Ghost Vertical Card following directly under finger for drop-over switching
            val drag = activeDrag
            val draggedPlayer = drag?.sourceSlot?.player
            if (drag != null && draggedPlayer != null) {
                val localPt = drag.cardCenterRootOffset - rootTopLeft
                val halfWPx = with(density) { (drag.draggedCardWidth / 2).toPx() }
                val halfHPx = with(density) { (drag.draggedCardHeight / 2).toPx() }
                VerticalPitchPlayerCard(
                    player = draggedPlayer,
                    slotPosition = drag.sourceSlot.slotPosition,
                    cardWidth = drag.draggedCardWidth,
                    cardHeight = drag.draggedCardHeight,
                    isSelected = true,
                    isDropTarget = false,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (localPt.x - halfWPx).roundToInt(),
                                (localPt.y - halfHPx).roundToInt()
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
    activeDrag: ActiveDragState?,
    onSlotPositioned: (String, Rect) -> Unit,
    onSlotTap: (SlotContext) -> Unit,
    onHoldForArrows: (SlotContext) -> Unit,
    onNudgeSlot: (String, Float, Float) -> Unit,
    onDragStart: (SlotContext, Offset, Dp, Dp) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onCoachClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
    ) {
        val pitchWidthPx = constraints.maxWidth.toFloat()
        val pitchHeightPx = constraints.maxHeight.toFloat()

        // Vertical rectangular card sizing fitted cleanly inside vertical pitch
        val cardWidthDp: Dp = minOf(maxWidth / 5.7f, maxHeight / 8.8f).coerceIn(40.dp, 60.dp)
        val cardHeightDp: Dp = cardWidthDp / VERTICAL_CARD_RATIO
        val density = LocalDensity.current
        val cardWpx = with(density) { cardWidthDp.toPx() }
        val cardHpx = with(density) { cardHeightDp.toPx() }

        FootballPitchCanvas(
            pitchTheme = uiState.prefs.pitchTheme
        )

        // Single Team XI Slots
        uiState.homeStarterSlots.forEach { slot ->
            val nx = slot.x
            val ny = slot.y
            val px = (nx * pitchWidthPx - cardWpx / 2f).coerceIn(4f, pitchWidthPx - cardWpx - 4f)
            val py = (ny * pitchHeightPx - cardHpx / 2f).coerceIn(4f, pitchHeightPx - cardHpx - 4f)

            PitchSlotItem(
                slot = slot,
                cardWidth = cardWidthDp,
                cardHeight = cardHeightDp,
                isSelected = uiState.selectedSlotKey == slot.slotKey,
                isRepositioning = uiState.repositioningSlotKey == slot.slotKey,
                isDropTarget = activeDrag?.hoveredTargetKey == slot.slotKey,
                isDraggedSource = activeDrag?.sourceSlot?.slotKey == slot.slotKey,
                isAwayTeam = false,
                onPositioned = { rect -> onSlotPositioned(slot.slotKey, rect) },
                onTap = { onSlotTap(slot) },
                onHoldForArrows = { onHoldForArrows(slot) },
                onDragStart = { centerRoot -> onDragStart(slot, centerRoot, cardWidthDp, cardHeightDp) },
                onDragMove = onDragMove,
                onDragEnd = onDragEnd,
                modifier = Modifier.offset { IntOffset(px.roundToInt(), py.roundToInt()) }
            )

            // Directional Arrows overlay around held card for manual repositioning
            if (uiState.repositioningSlotKey == slot.slotKey) {
                DirectionalArrowsOverlay(
                    cardLeftPx = px,
                    cardTopPx = py,
                    cardWPx = cardWpx,
                    cardHPx = cardHpx,
                    pitchWPx = pitchWidthPx,
                    pitchHPx = pitchHeightPx,
                    onUp = { onNudgeSlot(slot.slotKey, 0f, -0.045f) },
                    onDown = { onNudgeSlot(slot.slotKey, 0f, 0.045f) },
                    onLeft = { onNudgeSlot(slot.slotKey, -0.045f, 0f) },
                    onRight = { onNudgeSlot(slot.slotKey, 0.045f, 0f) }
                )
            }
        }

        // Compact Manager Card on bottom-left of formation
        CoachPitchCard(
            coach = uiState.activeCoach,
            onClick = onCoachClick,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(5.dp)
        )
    }
}

@Composable
private fun DirectionalArrowsOverlay(
    cardLeftPx: Float,
    cardTopPx: Float,
    cardWPx: Float,
    cardHPx: Float,
    pitchWPx: Float,
    pitchHPx: Float,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit
) {
    val btnDp = 30.dp
    val btnPx = with(LocalDensity.current) { btnDp.toPx() }
    val cx = cardLeftPx + cardWPx / 2f
    val cy = cardTopPx + cardHPx / 2f

    // Up Arrow
    ArrowButton(
        icon = Icons.Filled.KeyboardArrowUp,
        contentDescription = "Move Up",
        onClick = onUp,
        modifier = Modifier
            .offset {
                IntOffset(
                    (cx - btnPx / 2f).coerceIn(2f, pitchWPx - btnPx - 2f).roundToInt(),
                    (cardTopPx - btnPx - 2f).coerceIn(2f, pitchHPx - btnPx - 2f).roundToInt()
                )
            }
            .testTag("arrow_move_up")
    )
    // Down Arrow
    ArrowButton(
        icon = Icons.Filled.KeyboardArrowDown,
        contentDescription = "Move Down",
        onClick = onDown,
        modifier = Modifier
            .offset {
                IntOffset(
                    (cx - btnPx / 2f).coerceIn(2f, pitchWPx - btnPx - 2f).roundToInt(),
                    (cardTopPx + cardHPx + 2f).coerceIn(2f, pitchHPx - btnPx - 2f).roundToInt()
                )
            }
            .testTag("arrow_move_down")
    )
    // Left Arrow
    ArrowButton(
        icon = Icons.Filled.KeyboardArrowLeft,
        contentDescription = "Move Left",
        onClick = onLeft,
        modifier = Modifier
            .offset {
                IntOffset(
                    (cardLeftPx - btnPx - 2f).coerceIn(2f, pitchWPx - btnPx - 2f).roundToInt(),
                    (cy - btnPx / 2f).coerceIn(2f, pitchHPx - btnPx - 2f).roundToInt()
                )
            }
            .testTag("arrow_move_left")
    )
    // Right Arrow
    ArrowButton(
        icon = Icons.Filled.KeyboardArrowRight,
        contentDescription = "Move Right",
        onClick = onRight,
        modifier = Modifier
            .offset {
                IntOffset(
                    (cardLeftPx + cardWPx + 2f).coerceIn(2f, pitchWPx - btnPx - 2f).roundToInt(),
                    (cy - btnPx / 2f).coerceIn(2f, pitchHPx - btnPx - 2f).roundToInt()
                )
            }
            .testTag("arrow_move_right")
    )
}

@Composable
private fun ArrowButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(FifOrange)
            .border(1.5.dp, Color.White, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PitchSlotItem(
    slot: SlotContext,
    cardWidth: Dp,
    cardHeight: Dp,
    isSelected: Boolean,
    isRepositioning: Boolean,
    isDropTarget: Boolean,
    isDraggedSource: Boolean,
    isAwayTeam: Boolean,
    onPositioned: (Rect) -> Unit,
    onTap: () -> Unit,
    onHoldForArrows: (() -> Unit)?,
    onDragStart: (Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    var slotTopLeft by remember { mutableStateOf(Offset.Zero) }
    val haptic = LocalHapticFeedback.current

    val gestureMod = Modifier
        .onGloballyPositioned { coords ->
            val bounds = coords.boundsInRoot()
            slotTopLeft = bounds.topLeft
            onPositioned(bounds)
        }
        .pointerInput(slot.slotKey, slot.player?.id, onHoldForArrows != null) {
            val slop = viewConfiguration.touchSlop
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val startLocal = down.position
                var dragging = false
                var totalDx = 0f
                var totalDy = 0f

                if (slot.player == null) {
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
                } else if (onHoldForArrows != null) {
                    var released = false
                    val withinHold = withTimeoutOrNull(420L) {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) {
                                released = true
                                break
                            }
                            val delta = change.positionChange()
                            totalDx += delta.x
                            totalDy += delta.y
                            if (hypot(totalDx, totalDy) > slop) {
                                dragging = true
                                change.consume()
                                onDragStart(slotTopLeft + change.position)
                                break
                            }
                        }
                        true
                    }

                    if (withinHold == null && !dragging && !released) {
                        // Long-press hold completed without dragging -> show directional arrows
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onHoldForArrows()
                    } else if (released) {
                        onTap()
                    } else if (dragging) {
                        drag(down.id) { dragChange ->
                            dragChange.consume()
                            onDragMove(slotTopLeft + dragChange.position)
                        }
                        onDragEnd()
                    }
                } else {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            onTap()
                            break
                        }
                        val delta = change.positionChange()
                        totalDx += delta.x
                        totalDy += delta.y
                        if (hypot(totalDx, totalDy) > slop) {
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
        VerticalPitchPlayerCard(
            player = slot.player,
            slotPosition = slot.slotPosition,
            cardWidth = cardWidth,
            cardHeight = cardHeight,
            isSelected = isSelected,
            isDropTarget = isDropTarget,
            isRepositioning = isRepositioning,
            isTeamAwayBadge = isAwayTeam,
            modifier = modifier.then(gestureMod)
        )
    } else {
        EmptyPitchSlot(
            slotPosition = slot.slotPosition,
            cardWidth = cardWidth,
            cardHeight = cardHeight,
            isDropTarget = isDropTarget,
            modifier = modifier.then(gestureMod)
        )
    }
}

@Composable
private fun SubstitutionsBenchPane(
    subs: List<SlotContext>,
    selectedSlotKey: String?,
    activeDrag: ActiveDragState?,
    isVertical: Boolean,
    onSlotPositioned: (String, Rect) -> Unit,
    onSlotTap: (SlotContext) -> Unit,
    onDragStart: (SlotContext, Offset, Dp, Dp) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onAddSubSlot: () -> Unit,
    onRemoveSubSlot: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val cs = MaterialTheme.colorScheme
    val subW = 46.dp
    val subH = subW / VERTICAL_CARD_RATIO

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
                            cardWidth = subW,
                            cardHeight = subH,
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
                AddSubButton(onClick = onAddSubSlot, label = strings.addSubSlot, width = subW, height = subH)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 5.dp),
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
                            cardWidth = subW,
                            cardHeight = subH,
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
                        AddSubButton(onClick = onAddSubSlot, label = strings.addSubSlot, width = subW, height = subH)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubSlotWrapper(
    slot: SlotContext,
    cardWidth: Dp,
    cardHeight: Dp,
    canDelete: Boolean,
    selectedSlotKey: String?,
    activeDrag: ActiveDragState?,
    onSlotPositioned: (String, Rect) -> Unit,
    onSlotTap: (SlotContext) -> Unit,
    onDragStart: (SlotContext, Offset, Dp, Dp) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onRemove: () -> Unit
) {
    Box {
        PitchSlotItem(
            slot = slot,
            cardWidth = cardWidth,
            cardHeight = cardHeight,
            isSelected = selectedSlotKey == slot.slotKey,
            isRepositioning = false,
            isDropTarget = activeDrag?.hoveredTargetKey == slot.slotKey,
            isDraggedSource = activeDrag?.sourceSlot?.slotKey == slot.slotKey,
            isAwayTeam = false,
            onPositioned = { rect -> onSlotPositioned(slot.slotKey, rect) },
            onTap = { onSlotTap(slot) },
            onHoldForArrows = null,
            onDragStart = { centerRoot -> onDragStart(slot, centerRoot, cardWidth, cardHeight) },
            onDragMove = onDragMove,
            onDragEnd = onDragEnd
        )
        if (canDelete) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.78f))
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove sub slot",
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

@Composable
private fun AddSubButton(
    onClick: () -> Unit,
    label: String,
    width: Dp,
    height: Dp
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = FifOrange.copy(alpha = 0.16f),
        modifier = Modifier
            .size(width = width, height = height)
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
