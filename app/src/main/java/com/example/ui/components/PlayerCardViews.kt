package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.local.CoachEntity
import com.example.data.local.PlayerEntity
import com.example.i18n.LocalAppStrings
import com.example.model.AvatarGender
import com.example.model.ClubCatalog
import com.example.model.PlayingLine
import com.example.model.PositionCode
import com.example.ui.theme.FifOrange

const val VERTICAL_CARD_RATIO = 0.68f // width / height -> tall vertical card

@Composable
fun PlayerPortrait(
    customImageUri: String?,
    avatarGender: AvatarGender,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val fallbackRes = if (avatarGender == AvatarGender.FEMALE) {
        R.drawable.img_player_female
    } else {
        R.drawable.img_player_male
    }

    if (!customImageUri.isNullOrBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(customImageUri)
                .size(256)
                .crossfade(false)
                .build(),
            placeholder = painterResource(id = fallbackRes),
            error = painterResource(id = fallbackRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Image(
            painter = painterResource(id = fallbackRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}

@Composable
fun CoachPortrait(
    customImageUri: String?,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    if (!customImageUri.isNullOrBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(customImageUri)
                .size(256)
                .crossfade(false)
                .build(),
            placeholder = painterResource(id = R.drawable.img_coach_neutral),
            error = painterResource(id = R.drawable.img_coach_neutral),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.img_coach_neutral),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}

@Composable
fun PositionRoleLogo(
    line: PlayingLine,
    tint: Color = line.color,
    size: Dp = 12.dp
) {
    val icon = when (line) {
        PlayingLine.GK -> Icons.Filled.PanTool
        PlayingLine.DEF -> Icons.Filled.Shield
        PlayingLine.MID -> Icons.Filled.Explore
        PlayingLine.ATT -> Icons.Filled.Bolt
    }
    Icon(
        imageVector = icon,
        contentDescription = line.code,
        tint = tint,
        modifier = Modifier.size(size)
    )
}

@Composable
fun ClubCrestBadge(
    clubName: String,
    customClubLogoUri: String? = null,
    size: Dp = 18.dp
) {
    if (!customClubLogoUri.isNullOrBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(customClubLogoUri)
                .size(128)
                .crossfade(false)
                .build(),
            contentDescription = clubName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(4.dp))
        )
        return
    }

    val visual = ClubCatalog.findClub(clubName)
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val shield = Path().apply {
                moveTo(w * 0.1f, h * 0.08f)
                lineTo(w * 0.9f, h * 0.08f)
                lineTo(w * 0.9f, h * 0.56f)
                quadraticTo(w * 0.9f, h * 0.85f, w * 0.5f, h * 0.96f)
                quadraticTo(w * 0.1f, h * 0.85f, w * 0.1f, h * 0.56f)
                close()
            }
            drawPath(shield, color = visual.primaryColor)
            drawPath(
                shield,
                color = visual.secondaryColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
            )
        }
        Text(
            text = visual.shortCode.take(2),
            fontSize = (size.value * 0.34f).sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (visual.primaryColor == Color(0xFFF8FAFC) || visual.primaryColor == Color(0xFFF1F5F9) || visual.primaryColor == Color(0xFFFACC15)) {
                Color(0xFF0F172A)
            } else {
                Color.White
            },
            maxLines = 1
        )
    }
}

/**
 * Vertical rectangular pitch player card with playing-line border color,
 * unobstructed vertical long-rectangle portrait on the top right,
 * tactical power/position/extra-positions column on the left,
 * and written nationality + club crest + name at the bottom.
 */
@Composable
fun VerticalPitchPlayerCard(
    player: PlayerEntity,
    slotPosition: PositionCode,
    cardWidth: Dp,
    cardHeight: Dp,
    isSelected: Boolean,
    isDropTarget: Boolean,
    isRepositioning: Boolean = false,
    isTeamAwayBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    val lineColor = slotPosition.line.color
    val borderColor = when {
        isRepositioning -> FifOrange
        isDropTarget -> FifOrange
        isSelected -> Color.White
        else -> lineColor
    }
    val borderWidth = if (isSelected || isDropTarget || isRepositioning) 3.dp else 2.2.dp
    val effectivePower = player.powerForSlot(slotPosition)
    val cs = MaterialTheme.colorScheme
    val compact = cardWidth < 46.dp

    Column(
        modifier = modifier
            .size(width = cardWidth, height = cardHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(cs.surface)
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            .padding(if (compact) 2.dp else 2.5.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 1.5.dp else 2.dp)
    ) {
        // Top Zone: Left tactical column + Top-Right vertical long rectangle portrait
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 1.5.dp else 2.5.dp)
        ) {
            // Left tactical column: Power + Main Position + Role Icon + Extra Positions
            Column(
                modifier = Modifier
                    .width(cardWidth * 0.34f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isTeamAwayBadge) Color(0xFF0F172A) else lineColor)
                        .padding(vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = effectivePower.toString(),
                        color = if (isTeamAwayBadge || slotPosition.line != PlayingLine.MID) Color.White else Color.Black,
                        fontSize = if (compact) 8.sp else 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(1.5.dp))

                Text(
                    text = player.mainPosition,
                    color = cs.onSurface,
                    fontSize = if (compact) 6.5.sp else 7.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )

                PositionRoleLogo(
                    line = slotPosition.line,
                    tint = lineColor,
                    size = if (compact) 7.dp else 9.dp
                )

                if (player.extraPositionList.isNotEmpty() && !compact) {
                    Spacer(modifier = Modifier.height(1.5.dp))
                    player.extraPositionList.take(2).forEach { extra ->
                        Box(
                            modifier = Modifier
                                .padding(top = 1.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(cs.surfaceVariant)
                                .border(0.6.dp, extra.line.color, RoundedCornerShape(3.dp))
                                .padding(horizontal = 2.dp, vertical = 0.5.dp)
                        ) {
                            Text(
                                text = extra.code,
                                color = cs.onSurface,
                                fontSize = 5.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Top-Right Vertical Long Rectangle Portrait (completely unobstructed)
            PlayerPortrait(
                customImageUri = player.customImageUri,
                avatarGender = AvatarGender.fromName(player.avatarGender),
                contentDescription = player.name,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .border(1.dp, lineColor.copy(alpha = 0.75f), RoundedCornerShape(5.dp))
            )
        }

        // Bottom info strip: Player Name + Written Nationality + Club Crest
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(cs.surfaceVariant.copy(alpha = 0.85f))
                .padding(horizontal = 2.dp, vertical = if (compact) 1.dp else 1.5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = player.nickname.ifBlank { player.name.substringAfterLast(" ") },
                color = cs.onSurface,
                fontSize = if (compact) 7.sp else 8.5.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = player.nationality,
                    color = cs.onSurfaceVariant,
                    fontSize = if (compact) 5.5.sp else 6.8.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(2.dp))
                ClubCrestBadge(
                    clubName = player.club,
                    customClubLogoUri = player.customClubLogoUri,
                    size = if (compact) 8.dp else 10.dp
                )
            }
        }
    }
}

@Composable
fun EmptyPitchSlot(
    slotPosition: PositionCode,
    cardWidth: Dp,
    cardHeight: Dp,
    isDropTarget: Boolean,
    modifier: Modifier = Modifier
) {
    val lineColor = slotPosition.line.color
    val borderColor = if (isDropTarget) FifOrange else lineColor.copy(alpha = 0.85f)
    Box(
        modifier = modifier
            .size(width = cardWidth, height = cardHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDropTarget) FifOrange.copy(alpha = 0.30f) else Color.Black.copy(alpha = 0.44f))
            .border(if (isDropTarget) 3.dp else 1.8.dp, borderColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = slotPosition.code,
                tint = Color.White,
                modifier = Modifier.size(if (cardWidth < 46.dp) 13.dp else 18.dp)
            )
            Text(
                text = slotPosition.code,
                color = lineColor,
                fontSize = if (cardWidth < 46.dp) 8.sp else 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

/**
 * Coach Card placed on bottom-left of formation; only Name, Nationality & photo customizable.
 */
@Composable
fun CoachPitchCard(
    coach: CoachEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = cs.surface.copy(alpha = 0.94f),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.5.dp, FifOrange, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag("coach_card_${coach.teamSide}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoachPortrait(
                customImageUri = coach.customImageUri,
                contentDescription = coach.name,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, FifOrange, RoundedCornerShape(6.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = strings.coach.uppercase(),
                    color = FifOrange,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = coach.name,
                    color = cs.onSurface,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = coach.nationality,
                    color = cs.onSurfaceVariant,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Vertical rectangular player card for the Players Library Grid view.
 * Places the portrait as an unobstructed vertical long rectangle in the top-right,
 * with written nationality (no flags).
 */
@Composable
fun LibraryPlayerCardGrid(
    player: PlayerEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val cs = MaterialTheme.colorScheme
    val lineColor = player.mainLine.color

    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(VERTICAL_CARD_RATIO)
            .clip(RoundedCornerShape(12.dp))
            .background(cs.surface)
            .border(2.5.dp, lineColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(6.dp)
            .testTag("player_grid_card_${player.id}"),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Top Area: Tactical Stats/Positions on left + Vertical Long Rectangle Portrait on top right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(lineColor)
                            .padding(vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = player.naturalPower.toString(),
                            color = if (player.mainLine == PlayingLine.MID) Color.Black else Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PositionRoleLogo(line = player.mainLine, tint = lineColor, size = 12.dp)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = player.mainPosition,
                            color = cs.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    }

                    if (player.extraPositionList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            player.extraPositionList.forEach { extra ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(cs.surfaceVariant)
                                        .border(1.dp, extra.line.color, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = extra.code,
                                        color = cs.onSurface,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (player.isBuiltIn) cs.surfaceVariant else FifOrange)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = if (player.isBuiltIn) strings.fifproBadge else strings.customBadge,
                        color = if (player.isBuiltIn) cs.onSurfaceVariant else Color.White,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Vertical Long Rectangle Portrait on Top Right
            PlayerPortrait(
                customImageUri = player.customImageUri,
                avatarGender = AvatarGender.fromName(player.avatarGender),
                contentDescription = player.name,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.5.dp, lineColor.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            )
        }

        // Bottom Card Details: Name, Nickname, Written Nationality, Club & Foot
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(cs.surfaceVariant.copy(alpha = 0.65f))
                .padding(horizontal = 6.dp, vertical = 5.dp)
        ) {
            Text(
                text = player.name,
                style = MaterialTheme.typography.titleSmall,
                color = cs.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (player.nickname.isNotBlank()) {
                Text(
                    text = "\"${player.nickname}\"",
                    style = MaterialTheme.typography.labelSmall,
                    color = FifOrange,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = player.nationality,
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = player.preferredFoot,
                    style = MaterialTheme.typography.labelSmall,
                    color = FifOrange,
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClubCrestBadge(
                    clubName = player.club,
                    customClubLogoUri = player.customClubLogoUri,
                    size = 14.dp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = player.club,
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun LibraryPlayerCardList(
    player: PlayerEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val lineColor = player.mainLine.color

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cs.surface)
            .border(2.dp, lineColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag("player_list_card_${player.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Power Badge + Main Position + Info + Written Nationality
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(lineColor)
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = player.naturalPower.toString(),
                        color = if (player.mainLine == PlayingLine.MID) Color.Black else Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                PositionRoleLogo(line = player.mainLine, tint = lineColor, size = 14.dp)
                Spacer(modifier = Modifier.width(3.dp))
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
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

            if (player.extraPositionList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(3.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    player.extraPositionList.forEach { extra ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(cs.surfaceVariant)
                                .border(0.8.dp, extra.line.color, RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = extra.code,
                                color = cs.onSurface,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (player.nickname.isNotBlank()) {
                    Text(
                        text = "\"${player.nickname}\" • ",
                        style = MaterialTheme.typography.labelSmall,
                        color = FifOrange
                    )
                }
                Text(
                    text = player.nationality,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = cs.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "HEA:${player.heading} STA:${player.stamina}",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant,
                    fontSize = 9.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Top-Right Vertical Long Rectangle Portrait
        PlayerPortrait(
            customImageUri = player.customImageUri,
            avatarGender = AvatarGender.fromName(player.avatarGender),
            contentDescription = player.name,
            modifier = Modifier
                .size(width = 48.dp, height = 74.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.5.dp, lineColor, RoundedCornerShape(8.dp))
        )
    }
}
