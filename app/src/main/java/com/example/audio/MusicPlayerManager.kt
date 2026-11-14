package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.example.model.LocalTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MusicState(
    val tracks: List<LocalTrack> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false
) {
    val currentTrack: LocalTrack?
        get() = tracks.getOrNull(currentIndex)
}

class MusicPlayerManager(private val context: Context) {

    private val _state = MutableStateFlow(MusicState())
    val state: StateFlow<MusicState> = _state.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    fun setTracks(tracks: List<LocalTrack>) {
        val currentTrackId = _state.value.currentTrack?.id
        val newIndex = when {
            tracks.isEmpty() -> -1
            currentTrackId != null -> tracks.indexOfFirst { it.id == currentTrackId }.let { if (it >= 0) it else 0 }
            else -> 0
        }
        _state.update { it.copy(tracks = tracks, currentIndex = newIndex) }
        if (tracks.isEmpty()) {
            stopAndRelease()
        }
    }

    fun togglePlayPause() {
        val st = _state.value
        if (st.tracks.isEmpty()) return
        if (st.isPlaying) {
            pause()
        } else {
            val player = mediaPlayer
            if (player != null) {
                try {
                    player.start()
                    _state.update { it.copy(isPlaying = true) }
                } catch (_: IllegalStateException) {
                    playAtIndex(st.currentIndex.coerceAtLeast(0))
                }
            } else {
                playAtIndex(st.currentIndex.coerceAtLeast(0))
            }
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (_: IllegalStateException) {
        }
        _state.update { it.copy(isPlaying = false) }
    }

    fun onAppBackgrounded() {
        // Strictly stop playback when the app leaves foreground
        pause()
    }

    fun next() {
        val tracks = _state.value.tracks
        if (tracks.isEmpty()) return
        val nextIdx = (_state.value.currentIndex + 1) % tracks.size
        playAtIndex(nextIdx)
    }

    fun previous() {
        val tracks = _state.value.tracks
        if (tracks.isEmpty()) return
        val prevIdx = if (_state.value.currentIndex <= 0) tracks.lastIndex else _state.value.currentIndex - 1
        playAtIndex(prevIdx)
    }

    fun playAtIndex(index: Int) {
        val tracks = _state.value.tracks
        if (index !in tracks.indices) return
        val track = tracks[index]
        stopAndRelease()
        try {
            val uri = Uri.parse(track.uriString)
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, uri)
                setOnPreparedListener { mp ->
                    mp.start()
                    _state.update { it.copy(currentIndex = index, isPlaying = true) }
                }
                setOnCompletionListener {
                    next()
                }
                setOnErrorListener { _, _, _ ->
                    _state.update { it.copy(isPlaying = false) }
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
            _state.update { it.copy(currentIndex = index) }
        } catch (_: Exception) {
            _state.update { it.copy(isPlaying = false) }
        }
    }

    fun stopAndRelease() {
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
        _state.update { it.copy(isPlaying = false) }
    }
}
