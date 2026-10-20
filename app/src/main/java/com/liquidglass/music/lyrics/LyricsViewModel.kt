package com.liquidglass.music.lyrics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liquidglass.music.LiquidMusicApplication
import com.liquidglass.music.data.model.LyricRecordEntity
import com.liquidglass.music.data.model.LyricSourceType
import com.liquidglass.music.data.model.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

sealed class LyricUiState {
    data object Loading : LyricUiState()
    data class Synced(val parsed: ParsedLyrics, val activeLineIndex: Int, val offsetMs: Long) : LyricUiState()
    data class Plain(val text: String) : LyricUiState()
    data object Instrumental : LyricUiState()
    data object Missing : LyricUiState()
}

class LyricsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = (application as LiquidMusicApplication).database
    private val _lyricState = MutableStateFlow<LyricUiState>(LyricUiState.Loading)
    val lyricState: StateFlow<LyricUiState> = _lyricState.asStateFlow()

    private var currentTrackId: String? = null
    private var currentParsedLyrics: ParsedLyrics? = null
    private var currentOffsetMs: Long = 0L

    // Tap-to-stamp editor state
    private val _editorLines = MutableStateFlow<List<LyricLine>>(emptyList())
    val editorLines: StateFlow<List<LyricLine>> = _editorLines.asStateFlow()

    private val _isEditorActive = MutableStateFlow(false)
    val isEditorActive: StateFlow<Boolean> = _isEditorActive.asStateFlow()

    fun loadLyricsForTrack(track: TrackEntity?) {
        if (track == null) {
            _lyricState.value = LyricUiState.Missing
            currentTrackId = null
            return
        }

        if (currentTrackId == track.id) return
        currentTrackId = track.id

        viewModelScope.launch(Dispatchers.IO) {
            val record = database.lyricDao().getLyricForTrack(track.id)
            if (record == null) {
                _lyricState.value = LyricUiState.Missing
                currentParsedLyrics = null
            } else if (record.isInstrumental || record.sourceType == LyricSourceType.INSTRUMENTAL.name) {
                _lyricState.value = LyricUiState.Instrumental
                currentParsedLyrics = null
            } else {
                currentOffsetMs = record.offsetMs
                val parsed = LrcParser.parse(record.rawContent)
                currentParsedLyrics = parsed
                if (parsed.isSynced) {
                    _lyricState.value = LyricUiState.Synced(
                        parsed = parsed,
                        activeLineIndex = -1,
                        offsetMs = record.offsetMs
                    )
                } else {
                    _lyricState.value = LyricUiState.Plain(record.rawContent)
                }
            }
        }
    }

    /**
     * Updates the active line calculation based on playback position.
     * Only updates the state when the active line actually changes to eliminate UI flicker.
     */
    fun updatePlaybackPosition(positionMs: Long) {
        val currentState = _lyricState.value
        val parsed = currentParsedLyrics ?: return

        if (currentState is LyricUiState.Synced && parsed.isSynced) {
            val newIndex = LrcParser.findActiveLineIndex(parsed.lines, positionMs, currentOffsetMs)
            if (newIndex != currentState.activeLineIndex) {
                _lyricState.value = currentState.copy(activeLineIndex = newIndex)
            }
        }
    }

    fun adjustOffset(deltaMs: Long) {
        val trackId = currentTrackId ?: return
        val currentState = _lyricState.value
        if (currentState is LyricUiState.Synced) {
            val newOffset = currentOffsetMs + deltaMs
            currentOffsetMs = newOffset
            _lyricState.value = currentState.copy(offsetMs = newOffset)

            viewModelScope.launch(Dispatchers.IO) {
                database.lyricDao().updateLyricOffset(trackId, newOffset)
            }
        }
    }

    fun toggleInstrumental() {
        val trackId = currentTrackId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val isNowInstrumental = _lyricState.value !is LyricUiState.Instrumental
            if (isNowInstrumental) {
                database.lyricDao().insertOrUpdateLyric(
                    LyricRecordEntity(
                        trackId = trackId,
                        sourceType = LyricSourceType.INSTRUMENTAL.name,
                        rawContent = "",
                        isInstrumental = true
                    )
                )
                _lyricState.value = LyricUiState.Instrumental
            } else {
                database.lyricDao().deleteLyricForTrack(trackId)
                _lyricState.value = LyricUiState.Missing
            }
        }
    }

    // --- Tap To Stamp Editor Methods ---

    fun startEditor(initialText: String = "") {
        val parsed = currentParsedLyrics
        val lines = if (parsed != null && parsed.lines.isNotEmpty()) {
            parsed.lines
        } else {
            initialText.lines().filter { it.isNotBlank() }.map {
                LyricLine(timestampMs = 0L, text = it.trim())
            }
        }
        _editorLines.value = lines
        _isEditorActive.value = true
    }

    fun stampLine(lineIndex: Int, currentPlaybackMs: Long) {
        val current = _editorLines.value.toMutableList()
        if (lineIndex in current.indices) {
            current[lineIndex] = current[lineIndex].copy(timestampMs = currentPlaybackMs)
            _editorLines.value = current
        }
    }

    fun saveEditorEdits() {
        val trackId = currentTrackId ?: return
        val lines = _editorLines.value
        if (lines.isEmpty()) return

        val lrcContent = LrcParser.serializeToLrc(lines)
        val isSynced = lines.any { it.timestampMs > 0L }

        viewModelScope.launch(Dispatchers.IO) {
            database.lyricDao().insertOrUpdateLyric(
                LyricRecordEntity(
                    trackId = trackId,
                    sourceType = LyricSourceType.USER_EDITED.name,
                    rawContent = lrcContent,
                    isSynced = isSynced,
                    offsetMs = currentOffsetMs
                )
            )
            val parsed = LrcParser.parse(lrcContent)
            currentParsedLyrics = parsed
            _lyricState.value = if (isSynced) {
                LyricUiState.Synced(parsed, -1, currentOffsetMs)
            } else {
                LyricUiState.Plain(lrcContent)
            }
            _isEditorActive.value = false
        }
    }

    fun cancelEditor() {
        _isEditorActive.value = false
    }

    fun exportToLrcFile(destinationFile: File): Boolean {
        val parsed = currentParsedLyrics ?: return false
        return try {
            val content = LrcParser.serializeToLrc(parsed.lines, parsed.tags, currentOffsetMs)
            destinationFile.writeText(content, Charsets.UTF_8)
            true
        } catch (e: Exception) {
            false
        }
    }
}
