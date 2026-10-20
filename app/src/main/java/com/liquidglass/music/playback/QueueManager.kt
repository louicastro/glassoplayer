package com.liquidglass.music.playback

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import android.net.Uri
import com.liquidglass.music.data.dao.QueueDao
import com.liquidglass.music.data.dao.TrackDao
import com.liquidglass.music.data.model.QueueItemEntity
import com.liquidglass.music.data.model.TrackEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class RepeatMode {
    OFF, ALL, ONE
}

data class QueueState(
    val tracks: List<TrackEntity> = emptyList(),
    val currentIndex: Int = -1,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF
) {
    val currentTrack: TrackEntity?
        get() = tracks.getOrNull(currentIndex)

    val hasNext: Boolean
        get() = when (repeatMode) {
            RepeatMode.ONE -> currentIndex in tracks.indices
            RepeatMode.ALL -> tracks.isNotEmpty()
            RepeatMode.OFF -> currentIndex + 1 < tracks.size
        }

    val hasPrevious: Boolean
        get() = when (repeatMode) {
            RepeatMode.ONE -> currentIndex in tracks.indices
            RepeatMode.ALL -> tracks.isNotEmpty()
            RepeatMode.OFF -> currentIndex > 0
        }
}

class QueueManager(
    private val queueDao: QueueDao,
    private val trackDao: TrackDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _queueState = MutableStateFlow(QueueState())
    val queueState: StateFlow<QueueState> = _queueState.asStateFlow()

    // Preserved original order before shuffle was toggled on
    private var uncompressedOrder: List<TrackEntity> = emptyList()

    init {
        restoreQueue()
    }

    fun restoreQueue() {
        scope.launch {
            val queueEntities = queueDao.getQueueItemsSync()
            if (queueEntities.isNotEmpty()) {
                val restoredTracks = mutableListOf<TrackEntity>()
                var restoredCurrentIndex = -1

                for ((idx, entity) in queueEntities.withIndex()) {
                    val track = trackDao.getTrackById(entity.trackId)
                    if (track != null) {
                        restoredTracks.add(track)
                        if (entity.isCurrent) {
                            restoredCurrentIndex = idx
                        }
                    }
                }

                if (restoredTracks.isNotEmpty()) {
                    if (restoredCurrentIndex == -1) restoredCurrentIndex = 0
                    uncompressedOrder = restoredTracks.toList()
                    _queueState.value = QueueState(
                        tracks = restoredTracks,
                        currentIndex = restoredCurrentIndex
                    )
                }
            }
        }
    }

    suspend fun saveQueue() = withContext(Dispatchers.IO) {
        val state = _queueState.value
        val entities = state.tracks.mapIndexed { index, track ->
            QueueItemEntity(
                trackId = track.id,
                positionInQueue = index,
                isCurrent = (index == state.currentIndex)
            )
        }
        queueDao.replaceQueue(entities)
    }

    fun setQueue(tracks: List<TrackEntity>, startIndex: Int = 0) {
        if (tracks.isEmpty()) {
            clearQueue()
            return
        }
        val safeIndex = startIndex.coerceIn(0, tracks.lastIndex)
        uncompressedOrder = tracks.toList()
        _queueState.value = _queueState.value.copy(
            tracks = tracks,
            currentIndex = safeIndex
        )
        scope.launch { saveQueue() }
    }

    fun playTrack(track: TrackEntity) {
        val currentTracks = _queueState.value.tracks.toMutableList()
        val existingIndex = currentTracks.indexOfFirst { it.id == track.id }
        if (existingIndex >= 0) {
            _queueState.value = _queueState.value.copy(currentIndex = existingIndex)
        } else {
            currentTracks.add(track)
            uncompressedOrder = currentTracks.toList()
            _queueState.value = _queueState.value.copy(
                tracks = currentTracks,
                currentIndex = currentTracks.lastIndex
            )
        }
        scope.launch { saveQueue() }
    }

    fun enqueueNext(track: TrackEntity) {
        val currentTracks = _queueState.value.tracks.toMutableList()
        val curIdx = _queueState.value.currentIndex
        val insertIndex = if (curIdx in currentTracks.indices) curIdx + 1 else currentTracks.size
        currentTracks.add(insertIndex, track)
        uncompressedOrder = currentTracks.toList()
        _queueState.value = _queueState.value.copy(tracks = currentTracks)
        scope.launch { saveQueue() }
    }

    fun enqueueToEnd(tracks: List<TrackEntity>) {
        if (tracks.isEmpty()) return
        val currentTracks = _queueState.value.tracks.toMutableList()
        val wasEmpty = currentTracks.isEmpty()
        currentTracks.addAll(tracks)
        uncompressedOrder = currentTracks.toList()
        _queueState.value = _queueState.value.copy(
            tracks = currentTracks,
            currentIndex = if (wasEmpty) 0 else _queueState.value.currentIndex
        )
        scope.launch { saveQueue() }
    }

    fun removeTrackAt(index: Int) {
        val currentTracks = _queueState.value.tracks.toMutableList()
        if (index !in currentTracks.indices) return
        currentTracks.removeAt(index)
        uncompressedOrder = uncompressedOrder.filterIndexed { i, _ -> i != index }

        val oldIndex = _queueState.value.currentIndex
        val newIndex = when {
            currentTracks.isEmpty() -> -1
            oldIndex > index -> oldIndex - 1
            oldIndex == index -> if (oldIndex >= currentTracks.size) currentTracks.lastIndex else oldIndex
            else -> oldIndex
        }

        _queueState.value = _queueState.value.copy(
            tracks = currentTracks,
            currentIndex = newIndex
        )
        scope.launch { saveQueue() }
    }

    fun moveTrack(fromIndex: Int, toIndex: Int) {
        val currentTracks = _queueState.value.tracks.toMutableList()
        if (fromIndex !in currentTracks.indices || toIndex !in currentTracks.indices) return

        val item = currentTracks.removeAt(fromIndex)
        currentTracks.add(toIndex, item)

        val oldCurIdx = _queueState.value.currentIndex
        val newCurIdx = when (oldCurIdx) {
            fromIndex -> toIndex
            in (fromIndex + 1)..toIndex -> oldCurIdx - 1
            in toIndex until fromIndex -> oldCurIdx + 1
            else -> oldCurIdx
        }

        _queueState.value = _queueState.value.copy(
            tracks = currentTracks,
            currentIndex = newCurIdx
        )
        scope.launch { saveQueue() }
    }

    fun skipToNext(): TrackEntity? {
        val state = _queueState.value
        if (state.tracks.isEmpty()) return null

        val nextIndex = when (state.repeatMode) {
            RepeatMode.ONE -> state.currentIndex
            RepeatMode.ALL -> (state.currentIndex + 1) % state.tracks.size
            RepeatMode.OFF -> {
                if (state.currentIndex + 1 < state.tracks.size) state.currentIndex + 1 else return null
            }
        }

        _queueState.value = state.copy(currentIndex = nextIndex)
        scope.launch { saveQueue() }
        return state.tracks.getOrNull(nextIndex)
    }

    fun skipToPrevious(): TrackEntity? {
        val state = _queueState.value
        if (state.tracks.isEmpty()) return null

        val prevIndex = when (state.repeatMode) {
            RepeatMode.ONE -> state.currentIndex
            RepeatMode.ALL -> if (state.currentIndex - 1 < 0) state.tracks.lastIndex else state.currentIndex - 1
            RepeatMode.OFF -> {
                if (state.currentIndex > 0) state.currentIndex - 1 else return null
            }
        }

        _queueState.value = state.copy(currentIndex = prevIndex)
        scope.launch { saveQueue() }
        return state.tracks.getOrNull(prevIndex)
    }

    fun skipToIndex(index: Int): TrackEntity? {
        val state = _queueState.value
        if (index !in state.tracks.indices) return null
        _queueState.value = state.copy(currentIndex = index)
        scope.launch { saveQueue() }
        return state.tracks.getOrNull(index)
    }

    fun toggleShuffle() {
        val state = _queueState.value
        val newShuffle = !state.isShuffleEnabled
        if (newShuffle) {
            val currentTrack = state.currentTrack
            val otherTracks = state.tracks.filter { it.id != currentTrack?.id }.shuffled()
            val newTracks = if (currentTrack != null) listOf(currentTrack) + otherTracks else otherTracks
            _queueState.value = state.copy(
                tracks = newTracks,
                currentIndex = if (currentTrack != null) 0 else -1,
                isShuffleEnabled = true
            )
        } else {
            val currentTrack = state.currentTrack
            val restoredIndex = if (currentTrack != null) uncompressedOrder.indexOfFirst { it.id == currentTrack.id } else 0
            _queueState.value = state.copy(
                tracks = uncompressedOrder,
                currentIndex = if (restoredIndex >= 0) restoredIndex else 0,
                isShuffleEnabled = false
            )
        }
        scope.launch { saveQueue() }
    }

    fun cycleRepeatMode(): RepeatMode {
        val nextMode = when (_queueState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _queueState.value = _queueState.value.copy(repeatMode = nextMode)
        return nextMode
    }

    fun clearQueue() {
        uncompressedOrder = emptyList()
        _queueState.value = QueueState()
        scope.launch {
            queueDao.clearQueue()
        }
    }

    companion object {
        fun trackToMediaItem(track: TrackEntity): MediaItem {
            val metadata = MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .setAlbumTitle(track.album)
                .setArtworkUri(track.artworkUri?.let { Uri.parse(it) })
                .build()

            return MediaItem.Builder()
                .setMediaId(track.id)
                .setUri(Uri.parse(track.uri))
                .setMediaMetadata(metadata)
                .build()
        }
    }
}
