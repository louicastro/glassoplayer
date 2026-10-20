package com.liquidglass.music.service

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.liquidglass.music.LiquidMusicApplication
import com.liquidglass.music.MainActivity
import com.liquidglass.music.data.model.TrackEntity
import com.liquidglass.music.playback.AudioEffectsController
import com.liquidglass.music.playback.QueueManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@UnstableApi
class PlaybackService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    lateinit var queueManager: QueueManager
        private set

    lateinit var sleepTimer: SleepTimer
        private set

    lateinit var audioEffectsController: AudioEffectsController
        private set

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var isUpdatingFromQueue = false

    override fun onCreate() {
        super.onCreate()
        val app = application as LiquidMusicApplication
        val database = app.database

        queueManager = QueueManager(
            queueDao = database.queueDao(),
            trackDao = database.trackDao()
        )

        sleepTimer = SleepTimer(getPlayer = { player })
        audioEffectsController = AudioEffectsController()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true) // Handles audio focus automatically
            .setHandleAudioBecomingNoisy(true) // Pauses on headphone disconnect automatically
            .build()

        player = exoPlayer

        // Attach audio effects to player audio session
        audioEffectsController.attachAudioSession(exoPlayer.audioSessionId)

        val sessionActivityIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            sessionActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setSessionActivity(pendingIntent)
            .setCallback(MediaSessionCallback())
            .build()

        // Sync player events with QueueManager and track database
        exoPlayer.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                audioEffectsController.attachAudioSession(audioSessionId)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (isUpdatingFromQueue) return
                mediaItem?.mediaId?.let { trackId ->
                    val tracks = queueManager.queueState.value.tracks
                    val index = tracks.indexOfFirst { it.id == trackId }
                    if (index >= 0 && index != queueManager.queueState.value.currentIndex) {
                        queueManager.skipToIndex(index)
                    }
                    // Record play count in database
                    serviceScope.launch(Dispatchers.IO) {
                        database.trackDao().recordTrackPlayed(trackId)
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    sleepTimer.onTrackEnded()
                    val nextTrack = queueManager.skipToNext()
                    if (nextTrack != null) {
                        playTrack(nextTrack)
                    }
                }
            }
        })

        // Observe queue changes to update ExoPlayer playlist
        serviceScope.launch {
            queueManager.queueState.collectLatest { state ->
                val track = state.currentTrack
                if (track != null) {
                    val currentExoMediaId = exoPlayer.currentMediaItem?.mediaId
                    if (currentExoMediaId != track.id) {
                        playTrack(track)
                    }
                }
            }
        }
    }

    private fun playTrack(track: TrackEntity) {
        val p = player ?: return
        isUpdatingFromQueue = true
        val mediaItem = QueueManager.trackToMediaItem(track)
        p.setMediaItem(mediaItem)
        p.prepare()
        p.play()
        isUpdatingFromQueue = false
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    private inner class MediaSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val isOurApp = controller.packageName == packageName

            val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                .build()

            val sessionCommands = if (isOurApp) {
                MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon().build()
            } else {
                MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                    .remove(androidx.media3.session.SessionCommand.COMMAND_CODE_SESSION_SET_RATING)
                    .build()
            }

            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailablePlayerCommands(playerCommands)
                .setAvailableSessionCommands(sessionCommands)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: androidx.media3.session.SessionCommand,
            args: android.os.Bundle
        ): com.google.common.util.concurrent.ListenableFuture<androidx.media3.session.SessionResult> {
            return com.google.common.util.concurrent.Futures.immediateFuture(
                androidx.media3.session.SessionResult(androidx.media3.session.SessionError.ERROR_NOT_SUPPORTED)
            )
        }
    }

    override fun onDestroy() {
        sleepTimer.cancelTimer()
        audioEffectsController.release()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        player = null
        super.onDestroy()
    }
}
