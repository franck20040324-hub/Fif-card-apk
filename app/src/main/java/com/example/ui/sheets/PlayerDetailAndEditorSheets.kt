package com.example.ui.sheets

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.MusicState
import com.example.data.local.CoachEntity
import com.example.data.local.PlayerEntity
import com.example.data.local.SavedFormationEntity
import com.example.i18n.LocalAppStrings
import com.example.model.AppLanguage
import com.example.model.AvatarGender
import com.example.model.ClubCatalog
import com.example.model.NationalityCatalog
import com.example.model.PlayingLine
import com.example.model.PositionCode
import com.example.model.PreferredFoot
import com.example.ui.SlotContext
import com.example.ui.components.ClubCrestBadge
import com.example.ui.components.CoachPortrait
import com.example.ui.components.PlayerPortrait
import com.example.ui.components.PositionRoleLogo
import com.example.ui.theme.FifOrange
import com.example.ui.theme.LineAttRed
import com.example.ui.theme.LineDefGreen
import com.example.ui.theme.LineGkBlue
import com.example.ui.theme.LineMidYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailSheet(
    slotContext: SlotContext,
    onEditCard: (PlayerEntity) -> Unit,
    onRemoveFromSlot: (String) -> Unit,
    onDeleteCustomCard: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val player = slotContext.player ?: return
    val strings = LocalAppStrings.current
    val cs = MaterialTheme.colorScheme
    val lineColor = player.mainLine.color

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = cs.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vertical Rectangular Card Portrait
                Box(
                    modifier = Modifier
                        .size(width = 78.dp, height = 108.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.5.dp, lineColor, RoundedCornerShape(12.dp))
                ) {
                    PlayerPortrait(
                        customImageUri = player.customImageUri,
                        avatarGender = AvatarGender.fromName(player.avatarGender),
                        contentDescription = player.name,
                        modifier = Modifier.matchParentSize()
                    )
                    if (player.extraPositionList.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 3.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            player.extraPositionList.forEach { extra ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Black.copy(alpha = 0.78f))
                                        .border(1.dp, extra.line.color, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 3.dp)
                                ) {
                                    Text(extra.code, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PositionRoleLogo(line = player.mainLine, tint = lineColor, size = 16.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = player.mainPosition,
                            color = lineColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${player.naturalPower}",
                            color = FifOrange,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    }
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = cs.onBackground
                    )
                    if (player.nickname.isNotBlank()) {
                        Text(
                            text = "\"${player.nickname}\"",
                            style = MaterialTheme.typography.titleSmall,
                            color = FifOrange
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = player.flagEmoji, fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(player.nationality, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(8.dp))
                        ClubCrestBadge(
                            clubName = player.club,
                            customClubLogoUri = player.customClubLogoUri,
                            size = 16.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(player.club, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val footStr = when (PreferredFoot.fromCode(player.preferredFoot)) {
                        PreferredFoot.LEFT -> strings.footLeft
                        PreferredFoot.BOTH -> strings.footBoth
                        PreferredFoot.RIGHT -> strings.footRight
                    }
                    Text(
                        text = "${strings.preferredFootLabel}: $footStr • ${player.league}",
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant
                    )
                }
            }

            // Position-Based Powers
            Text(
                text = strings.positionPowersTitle,
                style = MaterialTheme.typography.titleSmall,
                color = cs.onBackground
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinePowerBox("GK", player.powerGk, LineGkBlue, Modifier.weight(1f))
                LinePowerBox("DEF", player.powerDef, LineDefGreen, Modifier.weight(1f))
                LinePowerBox("MID", player.powerMid, LineMidYellow, Modifier.weight(1f))
                LinePowerBox("ATT", player.powerAtt, LineAttRed, Modifier.weight(1f))
            }

            // Individual Skill Ratings including Heading, Crossing, Tackling
            Text(
                text = strings.skillsTitle,
                style = MaterialTheme.typography.titleSmall,
                color = cs.onBackground
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(cs.surface)
                    .border(1.dp, cs.outline, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SkillPill(strings.pace, player.pace)
                    SkillPill(strings.shooting, player.shooting)
                    SkillPill(strings.passing, player.passing)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SkillPill(strings.dribbling, player.dribbling)
                    SkillPill(strings.defending, player.defending)
                    SkillPill(strings.physical, player.physical)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SkillPill(strings.heading, player.heading)
                    SkillPill(strings.crossing, player.crossing)
                    SkillPill(strings.tackling, player.tackling)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    SkillPill(strings.stamina, player.stamina)
                    SkillPill(strings.gkSkill, player.gkStat)
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onEditCard(player) },
                    colors = ButtonDefaults.buttonColors(containerColor = FifOrange),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("detail_edit_card_button")
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.editCard, fontWeight = FontWeight.Bold)
                }

                if (slotContext.slotKey.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { onRemoveFromSlot(slotContext.slotKey) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(strings.removeFromSlot, maxLines = 1)
                    }
                }
            }

            if (!player.isBuiltIn && player.id != 0L) {
                OutlinedButton(
                    onClick = { onDeleteCustomCard(player.id) },
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

@Composable
private fun LinePowerBox(code: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.16f))
            .border(1.5.dp, color, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = code, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = value.toString(), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun SkillPill(label: String, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$label ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value.toString(), style = MaterialTheme.typography.titleSmall, color = FifOrange)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerEditorDialog(
    initialPlayer: PlayerEntity,
    onPersistPhotoUri: (Uri) -> Unit,
    onSave: (PlayerEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val cs = MaterialTheme.colorScheme

    var name by remember { mutableStateOf(initialPlayer.name) }
    var nickname by remember { mutableStateOf(initialPlayer.nickname) }
    var nationality by remember { mutableStateOf(initialPlayer.nationality) }
    var flagEmoji by remember { mutableStateOf(initialPlayer.flagEmoji) }
    var club by remember { mutableStateOf(initialPlayer.club) }
    var league by remember { mutableStateOf(initialPlayer.league) }
    var mainPos by remember { mutableStateOf(initialPlayer.mainPositionCode) }
    var extraPositions by remember { mutableStateOf(initialPlayer.extraPositionList.toSet()) }
    var avatarGender by remember { mutableStateOf(AvatarGender.fromName(initialPlayer.avatarGender)) }
    var customImageUri by remember { mutableStateOf(initialPlayer.customImageUri) }
    var customClubLogoUri by remember { mutableStateOf(initialPlayer.customClubLogoUri) }
    var preferredFoot by remember { mutableStateOf(PreferredFoot.fromCode(initialPlayer.preferredFoot)) }

    var pace by remember { mutableIntStateOf(initialPlayer.pace) }
    var shooting by remember { mutableIntStateOf(initialPlayer.shooting) }
    var passing by remember { mutableIntStateOf(initialPlayer.passing) }
    var dribbling by remember { mutableIntStateOf(initialPlayer.dribbling) }
    var defending by remember { mutableIntStateOf(initialPlayer.defending) }
    var physical by remember { mutableIntStateOf(initialPlayer.physical) }
    var heading by remember { mutableIntStateOf(initialPlayer.heading) }
    var crossing by remember { mutableIntStateOf(initialPlayer.crossing) }
    var tackling by remember { mutableIntStateOf(initialPlayer.tackling) }
    var stamina by remember { mutableIntStateOf(initialPlayer.stamina) }
    var gkStat by remember { mutableIntStateOf(initialPlayer.gkStat) }

    var powerGk by remember { mutableIntStateOf(initialPlayer.powerGk) }
    var powerDef by remember { mutableIntStateOf(initialPlayer.powerDef) }
    var powerMid by remember { mutableIntStateOf(initialPlayer.powerMid) }
    var powerAtt by remember { mutableIntStateOf(initialPlayer.powerAtt) }

    var showNationDropdown by remember { mutableStateOf(false) }
    var showClubDropdown by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPersistPhotoUri(uri)
            customImageUri = uri.toString()
        }
    }

    val clubLogoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPersistPhotoUri(uri)
            customClubLogoUri = uri.toString()
        }
    }

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
                    text = if (initialPlayer.id == 0L) strings.createNewCard else strings.editCard,
                    style = MaterialTheme.typography.headlineSmall,
                    color = cs.onBackground
                )

                // Vertical Rectangular Portrait Preview + Blank Neutral Male/Female Avatar Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PlayerPortrait(
                        customImageUri = customImageUri,
                        avatarGender = avatarGender,
                        contentDescription = name,
                        modifier = Modifier
                            .size(width = 62.dp, height = 86.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(2.dp, mainPos.line.color, RoundedCornerShape(10.dp))
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AvatarGender.entries.forEach { g ->
                                val selected = avatarGender == g && customImageUri == null
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selected) FifOrange else cs.surfaceVariant,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            avatarGender = g
                                            customImageUri = null
                                        }
                                ) {
                                    Text(
                                        text = if (g == AvatarGender.MALE) strings.neutralMale else strings.neutralFemale,
                                        color = if (selected) Color.White else cs.onSurface,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.pickFromPhone, fontSize = 11.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.nameLabel) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("editor_name_input")
                )

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text(strings.nicknameLabel) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("editor_nickname_input")
                )

                // Nationality Picker
                Box {
                    OutlinedButton(
                        onClick = { showNationDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${strings.nationalityLabel}: $flagEmoji $nationality")
                    }
                    DropdownMenu(
                        expanded = showNationDropdown,
                        onDismissRequest = { showNationDropdown = false },
                        modifier = Modifier.height(260.dp)
                    ) {
                        NationalityCatalog.all.forEach { n ->
                            DropdownMenuItem(
                                text = { Text("${n.flag} ${n.nameEn}") },
                                onClick = {
                                    nationality = n.nameEn
                                    flagEmoji = n.flag
                                    showNationDropdown = false
                                }
                            )
                        }
                    }
                }

                // Club & League Quick Picker + Custom Text + Custom Club Logo from Internal Storage
                Box {
                    OutlinedButton(
                        onClick = { showClubDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ClubCrestBadge(clubName = club, customClubLogoUri = customClubLogoUri, size = 16.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${strings.clubLabel}: $club ($league)")
                    }
                    DropdownMenu(
                        expanded = showClubDropdown,
                        onDismissRequest = { showClubDropdown = false },
                        modifier = Modifier.height(260.dp)
                    ) {
                        ClubCatalog.clubs.forEach { c ->
                            DropdownMenuItem(
                                text = { Text("${c.name} (${c.league})") },
                                onClick = {
                                    club = c.name
                                    league = c.league
                                    showClubDropdown = false
                                }
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = club,
                        onValueChange = { club = it },
                        label = { Text(strings.clubLabel) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = league,
                        onValueChange = { league = it },
                        label = { Text(strings.leagueLabel) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClubCrestBadge(
                        clubName = club,
                        customClubLogoUri = customClubLogoUri,
                        size = 34.dp
                    )
                    OutlinedButton(
                        onClick = {
                            clubLogoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("pick_club_logo_button")
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.pickClubLogo, fontSize = 11.sp)
                    }
                    if (!customClubLogoUri.isNullOrBlank()) {
                        TextButton(onClick = { customClubLogoUri = null }) {
                            Text(strings.clearClubLogo, fontSize = 11.sp)
                        }
                    }
                }

                // Main Position
                Text(strings.mainPositionLabel, style = MaterialTheme.typography.titleSmall, color = cs.onBackground)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    PositionCode.entries.forEach { pos ->
                        val selected = mainPos == pos
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selected) pos.line.color else cs.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, pos.line.color, RoundedCornerShape(6.dp))
                                .clickable { mainPos = pos }
                        ) {
                            Text(
                                text = pos.code,
                                color = if (selected && pos.line == PlayingLine.MID) Color.Black else if (selected) Color.White else cs.onSurface,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Left-Edge Extra Positions
                Text(strings.extraPositionsLabel, style = MaterialTheme.typography.titleSmall, color = cs.onBackground)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    PositionCode.entries.filter { it != mainPos }.forEach { pos ->
                        val selected = extraPositions.contains(pos)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selected) pos.line.color else cs.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    extraPositions = if (selected) {
                                        extraPositions - pos
                                    } else if (extraPositions.size < 3) {
                                        extraPositions + pos
                                    } else extraPositions
                                }
                        ) {
                            Text(
                                text = pos.code,
                                color = if (selected && pos.line == PlayingLine.MID) Color.Black else if (selected) Color.White else cs.onSurfaceVariant,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Preferred Foot (Height & Weight removed)
                Text(strings.preferredFootLabel, style = MaterialTheme.typography.titleSmall, color = cs.onBackground)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        PreferredFoot.RIGHT to strings.footRight,
                        PreferredFoot.LEFT to strings.footLeft,
                        PreferredFoot.BOTH to strings.footBoth
                    ).forEach { (foot, label) ->
                        val selected = preferredFoot == foot
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) FifOrange else cs.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { preferredFoot = foot }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(label, color = if (selected) Color.White else cs.onSurface, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                // Customizable Powers by Pitch Line using + and - Tapping Controls
                Text(strings.positionPowersTitle, style = MaterialTheme.typography.titleSmall, color = cs.onBackground)
                PowerStepperRow("GK", powerGk, LineGkBlue) { powerGk = it }
                PowerStepperRow("DEF", powerDef, LineDefGreen) { powerDef = it }
                PowerStepperRow("MID", powerMid, LineMidYellow) { powerMid = it }
                PowerStepperRow("ATT", powerAtt, LineAttRed) { powerAtt = it }

                // Individual Skills using + and - Tapping Controls (includes Heading, Crossing, Tackling)
                Text(strings.skillsTitle, style = MaterialTheme.typography.titleSmall, color = cs.onBackground)
                PowerStepperRow(strings.pace, pace) { pace = it }
                PowerStepperRow(strings.shooting, shooting) { shooting = it }
                PowerStepperRow(strings.passing, passing) { passing = it }
                PowerStepperRow(strings.dribbling, dribbling) { dribbling = it }
                PowerStepperRow(strings.defending, defending) { defending = it }
                PowerStepperRow(strings.physical, physical) { physical = it }
                PowerStepperRow(strings.heading, heading) { heading = it }
                PowerStepperRow(strings.crossing, crossing) { crossing = it }
                PowerStepperRow(strings.tackling, tackling) { tackling = it }
                PowerStepperRow(strings.stamina, stamina) { stamina = it }
                PowerStepperRow(strings.gkSkill, gkStat) { gkStat = it }

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
                            val cleanName = name.trim().ifEmpty { "Player" }
                            onSave(
                                initialPlayer.copy(
                                    name = cleanName,
                                    nickname = nickname.trim(),
                                    nationality = nationality,
                                    flagEmoji = flagEmoji,
                                    club = club.trim().ifEmpty { "FC" },
                                    league = league.trim().ifEmpty { "Custom" },
                                    mainPosition = mainPos.code,
                                    secondaryPositions = extraPositions.joinToString(",") { it.code },
                                    avatarGender = avatarGender.name,
                                    customImageUri = customImageUri,
                                    customClubLogoUri = customClubLogoUri,
                                    preferredFoot = preferredFoot.code,
                                    pace = pace,
                                    shooting = shooting,
                                    passing = passing,
                                    dribbling = dribbling,
                                    defending = defending,
                                    physical = physical,
                                    heading = heading,
                                    crossing = crossing,
                                    tackling = tackling,
                                    stamina = stamina,
                                    gkStat = gkStat,
                                    powerGk = powerGk,
                                    powerDef = powerDef,
                                    powerMid = powerMid,
                                    powerAtt = powerAtt
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FifOrange),
                        modifier = Modifier.testTag("editor_save_button")
                    ) {
                        Text(strings.save, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Tap + and - Stepper Row for adjusting powers (no sliding).
 */
@Composable
private fun PowerStepperRow(
    label: String,
    value: Int,
    accent: Color = FifOrange,
    onChange: (Int) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(cs.surface)
            .border(1.dp, cs.outline, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = cs.onSurface,
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(cs.surfaceVariant)
                    .border(1.dp, accent, CircleShape)
                    .clickable { onChange((value - 1).coerceIn(1, 99)) }
                    .testTag("stepper_minus_$label"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Decrease $label",
                    tint = cs.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = value.toString(),
                color = accent,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(30.dp)
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent)
                    .clickable { onChange((value + 1).coerceIn(1, 99)) }
                    .testTag("stepper_plus_$label"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Increase $label",
                    tint = if (accent == LineMidYellow) Color.Black else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SaveFormationDialog(
    defaultName: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf(defaultName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.saveFormation) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(strings.formationNameLabel) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_formation_name_input")
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(name) },
                colors = ButtonDefaults.buttonColors(containerColor = FifOrange),
                modifier = Modifier.testTag("confirm_save_formation")
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedFormationsSheet(
    savedFormations: List<SavedFormationEntity>,
    onLoadFormation: (SavedFormationEntity) -> Unit,
    onDeleteFormation: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val cs = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = cs.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = strings.savedFormations,
                style = MaterialTheme.typography.headlineSmall,
                color = cs.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (savedFormations.isEmpty()) {
                Text(
                    text = strings.noSavedFormations,
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(savedFormations, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(cs.surface)
                                .border(1.dp, cs.outline, RoundedCornerShape(10.dp))
                                .clickable { onLoadFormation(item) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bookmark,
                                contentDescription = null,
                                tint = FifOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = cs.onSurface
                                )
                                Text(
                                    text = item.baseFormationId.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = cs.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDeleteFormation(item.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = strings.delete, tint = cs.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Coach Card Editor: ONLY Name, Nationality & local Photo customizable.
 */
@Composable
fun CoachEditorDialog(
    coach: CoachEntity,
    onPersistPhotoUri: (Uri) -> Unit,
    onSave: (CoachEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val cs = MaterialTheme.colorScheme
    var name by remember { mutableStateOf(coach.name) }
    var nationality by remember { mutableStateOf(coach.nationality) }
    var flagEmoji by remember { mutableStateOf(coach.flagEmoji) }
    var customImageUri by remember { mutableStateOf(coach.customImageUri) }
    var showNationMenu by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPersistPhotoUri(uri)
            customImageUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.editCoach) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(strings.coachNote, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CoachPortrait(
                        customImageUri = customImageUri,
                        contentDescription = name,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(2.dp, FifOrange, RoundedCornerShape(10.dp))
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton(onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) {
                            Text(strings.pickFromPhone, fontSize = 11.sp)
                        }
                        if (customImageUri != null) {
                            TextButton(onClick = { customImageUri = null }) {
                                Text(strings.clearPhoto, fontSize = 11.sp)
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.nameLabel) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("coach_name_input")
                )
                Box {
                    OutlinedButton(
                        onClick = { showNationMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${strings.nationalityLabel}: $flagEmoji $nationality")
                    }
                    DropdownMenu(
                        expanded = showNationMenu,
                        onDismissRequest = { showNationMenu = false },
                        modifier = Modifier.height(260.dp)
                    ) {
                        NationalityCatalog.all.forEach { n ->
                            DropdownMenuItem(
                                text = { Text("${n.flag} ${n.nameEn}") },
                                onClick = {
                                    nationality = n.nameEn
                                    flagEmoji = n.flag
                                    showNationMenu = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        coach.copy(
                            name = name.trim().ifEmpty { "Coach" },
                            nationality = nationality,
                            flagEmoji = flagEmoji,
                            customImageUri = customImageUri
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = FifOrange),
                modifier = Modifier.testTag("coach_save_button")
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistSheet(
    musicState: MusicState,
    onAddSongs: () -> Unit,
    onPlayTrack: (Int) -> Unit,
    onRemoveTrack: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val cs = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = cs.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(strings.playlistTitle, style = MaterialTheme.typography.titleMedium, color = cs.onBackground)
                Button(
                    onClick = onAddSongs,
                    colors = ButtonDefaults.buttonColors(containerColor = FifOrange)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.addSongs, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (musicState.tracks.isEmpty()) {
                Text(
                    text = strings.playlistEmpty,
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(musicState.tracks, key = { _, item -> item.id }) { idx, track ->
                        val active = idx == musicState.currentIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) FifOrange.copy(alpha = 0.16f) else cs.surface)
                                .border(
                                    1.dp,
                                    if (active) FifOrange else cs.outline,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onPlayTrack(idx) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = if (active) FifOrange else cs.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                color = cs.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onRemoveTrack(track.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = strings.delete, tint = cs.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ResetFifProConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.resetConfirmTitle) },
        text = { Text(strings.resetConfirmMessage) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = FifOrange),
                modifier = Modifier.testTag("confirm_reset_fifpro")
            ) {
                Text(strings.resetConfirmAction)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

@Composable
fun FirstLaunchSetupDialog(
    isDarkAmoled: Boolean,
    language: AppLanguage,
    onSelectTheme: (Boolean) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onComplete: () -> Unit
) {
    val strings = LocalAppStrings.current
    val cs = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onComplete,
        title = { Text(strings.firstLaunchTitle, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(strings.firstLaunchSubtitle, style = MaterialTheme.typography.labelMedium, color = FifOrange)
                Text(strings.firstLaunchMediaInfo, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)

                Text(strings.languageLabel, style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(AppLanguage.ENGLISH to "English", AppLanguage.ARABIC to "العربية").forEach { (lang, label) ->
                        val selected = language == lang
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) FifOrange else cs.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectLanguage(lang) }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(label, color = if (selected) Color.White else cs.onSurface, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Text(strings.themeLabel, style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(true to strings.darkAmoled, false to strings.brightWhite).forEach { (dark, label) ->
                        val selected = isDarkAmoled == dark
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) FifOrange else cs.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectTheme(dark) }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(label, color = if (selected) Color.White else cs.onSurface, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onComplete,
                colors = ButtonDefaults.buttonColors(containerColor = FifOrange),
                modifier = Modifier.testTag("first_launch_start_button")
            ) {
                Text(strings.firstLaunchStart, fontWeight = FontWeight.Bold)
            }
        }
    )
}
