package com.liquidglass.music.ui.player

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.liquidglass.music.LiquidMusicApplication
import com.liquidglass.music.data.model.TrackEntity
import com.liquidglass.music.playback.RepeatMode
import com.liquidglass.music.service.PlaybackService
import com.liquidglass.music.ui.util.ArtworkPaletteExtractor
import com.liquidglass.music.ui.util.ExtractedColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@UnstableApi
class PlaybackViewModel(application: Application) : AndroidViewModel(application) {

    private val database = (application as LiquidMusicApplication).database
    private var controllerFuture: ListenableFuture<MediaController>? = null
    var mediaController: MediaController? = null
        private set

    private val _currentTrack = MutableStateFlow<TrackEntity?>(null)
    val currentTrack: StateFlow<TrackEntity?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _queue = MutableStateFlow<List<TrackEntity>>(emptyList())
    val queue: StateFlow<List<TrackEntity>> = _queue.asStateFlow()

    private val _paletteColors = MutableStateFlow(
        ExtractedColors(
            dominant = androidx.compose.ui.graphics.Color(0xFF131524),
            surfaceTint = androidx.compose.ui.graphics.Color(0x332B304C),
            accent = androidx.compose.ui.graphics.Color(0xFF7282FF)
        )
    )
    val paletteColors: StateFlow<ExtractedColors> = _paletteColors.asStateFlow()

    init {
        initializeMediaController()
        startPositionTracker()
        observeDatabaseQueue()
    }

    private fun initializeMediaController() {
        val sessionToken = SessionToken(
            getApplication(),
            ComponentName(getApplication(), PlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                val controller = controllerFuture?.get()
                mediaController = controller
                setupControllerListener(controller)
            } catch (e: Exception) {
                // Service may still be initializing
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupControllerListener(controller: MediaController?) {
        controller ?: return

        _isPlaying.value = controller.isPlaying
        _playbackSpeed.value = controller.playbackParameters.speed
        updateCurrentTrack(controller.currentMediaItem)

        controller.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentTrack(mediaItem)
            }

            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                _playbackSpeed.value = playbackParameters.speed
            }
        })
    }

    private fun updateCurrentTrack(mediaItem: MediaItem?) {
        val trackId = mediaItem?.mediaId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val track = database.trackDao().getTrackById(trackId)
            _currentTrack.value = track
            track?.let {
                _durationMs.value = if (it.durationMs > 0) it.durationMs else 1L
                val colors = ArtworkPaletteExtractor.extractColorsFromUri(getApplication(), it.artworkUri)
                _paletteColors.value = colors
            }
        }
    }

    private fun startPositionTracker() {
        viewModelScope.launch {
            while (isActive) {
                val controller = mediaController
                if (controller != null && controller.isPlaying) {
                    val pos = controller.currentPosition.coerceAtLeast(0L)
                    _currentPositionMs.value = pos
                    val dur = controller.duration
                    if (dur != C.TIME_UNSET && dur > 0) {
                        _durationMs.value = dur
                    }
                }
                delay(100) // 100ms update frequency for smooth seek bar & synchronized lyrics
            }
        }
    }

    private fun observeDatabaseQueue() {
        viewModelScope.launch {
            database.queueDao().getQueueItems().collectLatest { entities ->
                val trackList = entities.mapNotNull { database.trackDao().getTrackById(it.trackId) }
                _queue.value = trackList
            }
        }
    }

    fun playTrack(track: TrackEntity, allTracks: List<TrackEntity> = listOf(track)) {
        viewModelScope.launch(Dispatchers.IO) {
            val startIndex = allTracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
            val queueEntities = allTracks.mapIndexed { index, item ->
                com.liquidglass.music.data.model.QueueItemEntity(
                    trackId = item.id,
                    positionInQueue = index,
                    isCurrent = index == startIndex
                )
            }
            database.queueDao().replaceQueue(queueEntities)

            val controller = mediaController
            if (controller != null) {
                val mediaItems = allTracks.map { com.liquidglass.music.playback.QueueManager.trackToMediaItem(it) }
                controller.setMediaItems(mediaItems, startIndex, 0L)
                controller.prepare()
                controller.play()
            }
            updateCurrentTrack(controller?.currentMediaItem)
        }
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun seekTo(positionMs: Long) {
        val controller = mediaController ?: return
        val safePos = positionMs.coerceIn(0L, _durationMs.value)
        _currentPositionMs.value = safePos
        controller.seekTo(safePos)
    }

    fun skipToNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        }
    }

    fun skipToPrevious() {
        val controller = mediaController ?: return
        if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
        } else {
            controller.seekTo(0L)
        }
    }

    fun setSpeed(speed: Float) {
        val controller = mediaController ?: return
        controller.playbackParameters = PlaybackParameters(speed)
        _playbackSpeed.value = speed
    }

    fun toggleFavorite() {
        val track = _currentTrack.value ?: return
        val newFavorite = !track.isFavorite
        viewModelScope.launch(Dispatchers.IO) {
            database.trackDao().setFavorite(track.id, newFavorite)
            _currentTrack.value = track.copy(isFavorite = newFavorite)
        }
    }

    override fun onCleared() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
        super.onCleared()
    }
}
