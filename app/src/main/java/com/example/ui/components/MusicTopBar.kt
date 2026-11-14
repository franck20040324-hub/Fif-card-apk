package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.MusicState
import com.example.i18n.LocalAppStrings
import com.example.model.AppLanguage
import com.example.ui.theme.FifOrange
import com.example.ui.theme.LineDefGreen

@Composable
fun MusicAndHeaderTopBar(
    musicState: MusicState,
    isDarkAmoled: Boolean,
    language: AppLanguage,
    onPreviousSong: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextSong: () -> Unit,
    onAddSongsClick: () -> Unit,
    onOpenPlaylist: () -> Unit,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val cs = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(cs.surface)
            .border(width = 0.5.dp, color = cs.outline)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // Music Player Bar on top of the app
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(cs.surfaceVariant)
                .border(1.dp, FifOrange.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = strings.playlistTitle,
                tint = FifOrange,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = musicState.currentTrack?.title ?: strings.musicNoTrack,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = cs.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenPlaylist)
                    .padding(vertical = 8.dp)
                    .testTag("music_track_title")
            )
            IconButton(
                onClick = onPreviousSong,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("music_prev_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = "Previous",
                    tint = cs.onSurface
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(FifOrange)
                    .clickable(onClick = onTogglePlayPause)
                    .testTag("music_play_pause_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (musicState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (musicState.isPlaying) "Pause" else "Play",
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onNextSong,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("music_next_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Next",
                    tint = cs.onSurface
                )
            }
            IconButton(
                onClick = onAddSongsClick,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("music_add_songs_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = strings.addSongs,
                    tint = FifOrange
                )
            }
            IconButton(
                onClick = onOpenPlaylist,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("music_playlist_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = strings.playlistTitle,
                    tint = cs.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Compact Brand + Auto-Save + Theme + Language Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = FifOrange
                ) {
                    Text(
                        text = strings.appName,
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(LineDefGreen)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = strings.autoSaveOn,
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Theme Toggle (AMOLED Dark vs Bright White)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = cs.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, cs.outline, RoundedCornerShape(8.dp))
                        .clickable(onClick = onToggleTheme)
                        .testTag("theme_toggle_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isDarkAmoled) Icons.Filled.Brightness2 else Icons.Filled.LightMode,
                            contentDescription = strings.themeLabel,
                            tint = FifOrange,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isDarkAmoled) strings.darkAmoled else strings.brightWhite,
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurface
                        )
                    }
                }

                // Language Toggle (EN / AR)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = cs.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, cs.outline, RoundedCornerShape(8.dp))
                        .clickable(onClick = onToggleLanguage)
                        .testTag("language_toggle_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Translate,
                            contentDescription = strings.languageLabel,
                            tint = FifOrange,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.ARABIC) "AR" else "EN",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurface
                        )
                    }
                }
            }
        }
    }
}
