package com.example.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.CoachEntity
import com.example.i18n.LocalAppStrings
import com.example.model.AvatarGender
import com.example.model.FormationCatalog
import com.example.model.PlayingLine
import com.example.model.PositionCode
import com.example.model.TeamSide
import com.example.ui.SlotContext
import com.example.ui.components.CoachPortrait
import com.example.ui.components.ManagerCardList
import com.example.ui.components.PlayerPortrait
import com.example.ui.components.PositionRoleLogo
import com.example.ui.theme.FifOrange
import com.example.ui.theme.LineAttRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotActionSheet(
    slotContext: SlotContext,
    onCreateNewCard: () -> Unit,
    onSelectExistingCard: () -> Unit,
    onInspectOrEditCard: () -> Unit,
    onStartSwap: () -> Unit,
    onRemoveFromSlot: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val cs = MaterialTheme.colorScheme
    val currentPlayer = slotContext.player
    val lineColor = slotContext.slotPosition.line.color

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = cs.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(lineColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = slotContext.slotPosition.code,
                        color = if (slotContext.slotPosition.line == PlayingLine.MID) Color.Black else Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.slotOptionsTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.onBackground
                )
            }

            if (currentPlayer != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(cs.surface)
                        .border(1.dp, lineColor.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlayerPortrait(
                        customImageUri = currentPlayer.customImageUri,
                        avatarGender = AvatarGender.fromName(currentPlayer.avatarGender),
                        contentDescription = currentPlayer.name,
                        modifier = Modifier
                            .size(width = 36.dp, height = 52.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, lineColor, RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentPlayer.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = cs.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${currentPlayer.nationality} • ${currentPlayer.club}",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = currentPlayer.powerForSlot(slotContext.slotPosition).toString(),
                        color = FifOrange,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }

            // Primary Option 1: Create New Card
            Button(
                onClick = onCreateNewCard,
                colors = ButtonDefaults.buttonColors(containerColor = FifOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("slot_action_create_new")
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.createNewCard, fontWeight = FontWeight.Bold)
            }

            // Primary Option 2: Select Card from Existing Cards List
            OutlinedButton(
                onClick = onSelectExistingCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("slot_action_select_existing")
            ) {
                Icon(Icons.Filled.Groups, contentDescription = null, tint = FifOrange, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.selectExistingCard, fontWeight = FontWeight.Bold)
            }

            // Secondary options when slot currently holds a player
            if (currentPlayer != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onInspectOrEditCard,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("slot_action_edit_view")
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.editCurrentCard, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    OutlinedButton(
                        onClick = onStartSwap,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("slot_action_swap")
                    ) {
                        Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.swapPlayer, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                TextButton(
                    onClick = onRemoveFromSlot,
                    colors = ButtonDefaults.textButtonColors(contentColor = LineAttRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slot_action_remove")
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.removeFromSlot)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerPitchActionSheet(
    activeCoach: CoachEntity,
    onCreateNewManager: () -> Unit,
    onSelectFromManagersList: () -> Unit,
    onEditCurrentManager: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val cs = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = cs.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = strings.managerOptionsTitle,
                style = MaterialTheme.typography.titleMedium,
                color = cs.onBackground
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(cs.surface)
                    .border(1.5.dp, FifOrange, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoachPortrait(
                    customImageUri = activeCoach.customImageUri,
                    contentDescription = activeCoach.name,
                    modifier = Modifier
                        .size(width = 40.dp, height = 56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, FifOrange, RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeCoach.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = cs.onSurface
                    )
                    Text(
                        text = activeCoach.nationality,
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onCreateNewManager,
                colors = ButtonDefaults.buttonColors(containerColor = FifOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("manager_action_create")
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.createNewManager, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onSelectFromManagersList,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("manager_action_select")
            ) {
                Icon(Icons.Filled.Groups, contentDescription = null, tint = FifOrange, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.selectFromManagersList, fontWeight = FontWeight.Bold)
            }

            TextButton(
                onClick = onEditCurrentManager,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manager_action_edit_current")
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null, tint = FifOrange, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.editCoach, color = FifOrange)
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerPickerSheet(
    coaches: List<CoachEntity>,
    activeCoachId: Long,
    onSelectCoach: (Long) -> Unit,
    onCreateNewManager: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val cs = MaterialTheme.colorScheme
    var query by remember { mutableStateOf("") }

    val filtered = remember(coaches, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) coaches
        else coaches.filter {
            it.name.lowercase().contains(q) || it.nationality.lowercase().contains(q)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = cs.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.84f)
                .padding(horizontal = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = strings.managersTab,
                    style = MaterialTheme.typography.headlineSmall,
                    color = cs.onBackground
                )
                Button(
                    onClick = onCreateNewManager,
                    colors = ButtonDefaults.buttonColors(containerColor = FifOrange),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("manager_picker_create_new")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.createNewManager, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(strings.managersSearchPlaceholder, maxLines = 1) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = FifOrange) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filtered, key = { it.id }) { coach ->
                    ManagerCardList(
                        coach = coach,
                        isActiveOnPitch = coach.id == activeCoachId,
                        onClick = { onSelectCoach(coach.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerDetailSheet(
    coach: CoachEntity,
    isActiveOnPitch: Boolean,
    onAssignToPitch: (Long) -> Unit,
    onEditManager: (CoachEntity) -> Unit,
    onDeleteCustomManager: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val cs = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = cs.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(FifOrange)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = strings.coach.uppercase(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        if (isActiveOnPitch) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = FifOrange, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(strings.managerOnPitch, color = FifOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = coach.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = cs.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${strings.nationalityLabel}: ${coach.nationality}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                CoachPortrait(
                    customImageUri = coach.customImageUri,
                    contentDescription = coach.name,
                    modifier = Modifier
                        .size(width = 74.dp, height = 112.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.5.dp, FifOrange, RoundedCornerShape(12.dp))
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isActiveOnPitch) {
                    Button(
                        onClick = { onAssignToPitch(coach.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = FifOrange),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manager_detail_assign")
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.assignToPitch, fontWeight = FontWeight.Bold)
                    }
                }
                OutlinedButton(
                    onClick = { onEditManager(coach) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("manager_detail_edit")
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.editCoach, fontWeight = FontWeight.Bold)
                }
            }

            if (!coach.isBuiltIn && coach.id != 0L) {
                OutlinedButton(
                    onClick = { onDeleteCustomManager(coach.id) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LineAttRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.delete)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateFormationDialog(
    currentSlots: List<SlotContext>,
    currentBaseFormationId: String,
    onCreateAndApply: (name: String, baseFormationId: String, customSlots: List<SlotContext>) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val cs = MaterialTheme.colorScheme

    var formationName by remember { mutableStateOf("") }
    var selectedBaseId by remember { mutableStateOf(currentBaseFormationId) }
    var useCurrentPitch by remember { mutableStateOf(true) }
    var editableSlots by remember { mutableStateOf(currentSlots) }
    var editingSlotIndex by remember { mutableStateOf<Int?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = cs.background,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, cs.outline, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = strings.createNewFormation,
                    style = MaterialTheme.typography.headlineSmall,
                    color = cs.onBackground
                )

                OutlinedTextField(
                    value = formationName,
                    onValueChange = { formationName = it },
                    label = { Text(strings.formationNameLabel) },
                    placeholder = { Text("4-2-3-1 Counter") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_formation_name_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (useCurrentPitch) FifOrange else cs.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                useCurrentPitch = true
                                editableSlots = currentSlots
                            }
                    ) {
                        Text(
                            text = strings.formationSourceCurrent,
                            color = if (useCurrentPitch) Color.White else cs.onSurface,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (!useCurrentPitch) FifOrange else cs.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                useCurrentPitch = false
                                val template = FormationCatalog.byId(selectedBaseId)
                                val byIdx = currentSlots.associateBy { it.slotIndex }
                                editableSlots = template.slots.map { ts ->
                                    val prev = byIdx[ts.index]
                                    SlotContext(
                                        slotKey = prev?.slotKey ?: "HOME_S_${ts.index}",
                                        teamSide = TeamSide.HOME,
                                        isStarter = true,
                                        slotIndex = ts.index,
                                        slotPosition = ts.position,
                                        x = ts.x,
                                        y = ts.y,
                                        hasCustomPosition = true,
                                        player = prev?.player
                                    )
                                }
                            }
                    ) {
                        Text(
                            text = strings.formationSourcePreset,
                            color = if (!useCurrentPitch) Color.White else cs.onSurface,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    }
                }

                if (!useCurrentPitch) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FormationCatalog.all.forEach { preset ->
                            val selected = preset.id == selectedBaseId
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (selected) FifOrange else cs.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        selectedBaseId = preset.id
                                        val byIdx = currentSlots.associateBy { it.slotIndex }
                                        editableSlots = preset.slots.map { ts ->
                                            val prev = byIdx[ts.index]
                                            SlotContext(
                                                slotKey = prev?.slotKey ?: "HOME_S_${ts.index}",
                                                teamSide = TeamSide.HOME,
                                                isStarter = true,
                                                slotIndex = ts.index,
                                                slotPosition = ts.position,
                                                x = ts.x,
                                                y = ts.y,
                                                hasCustomPosition = true,
                                                player = prev?.player
                                            )
                                        }
                                    }
                            ) {
                                Text(
                                    text = preset.name,
                                    color = if (selected) Color.White else cs.onSurface,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = strings.slotPositionsLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = cs.onBackground
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    editableSlots.forEach { slot ->
                        val posColor = slot.slotPosition.line.color
                        Box {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = cs.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.2.dp, posColor, RoundedCornerShape(8.dp))
                                    .clickable { editingSlotIndex = slot.slotIndex }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PositionRoleLogo(line = slot.slotPosition.line, tint = posColor, size = 12.dp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${slot.slotIndex + 1}. ${slot.slotPosition.code}",
                                        color = cs.onSurface,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = editingSlotIndex == slot.slotIndex,
                                onDismissRequest = { editingSlotIndex = null },
                                modifier = Modifier.height(260.dp)
                            ) {
                                PositionCode.entries.forEach { candidate ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = candidate.code,
                                                color = candidate.line.color,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        onClick = {
                                            editingSlotIndex = null
                                            editableSlots = editableSlots.map { item ->
                                                if (item.slotIndex == slot.slotIndex) {
                                                    item.copy(slotPosition = candidate, hasCustomPosition = true)
                                                } else item
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(strings.cancel)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val finalName = formationName.trim().ifEmpty {
                                "${FormationCatalog.byId(selectedBaseId).name} Custom"
                            }
                            onCreateAndApply(finalName, selectedBaseId, editableSlots)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FifOrange),
                        modifier = Modifier.testTag("confirm_create_formation")
                    ) {
                        Text(strings.saveAndApply, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
