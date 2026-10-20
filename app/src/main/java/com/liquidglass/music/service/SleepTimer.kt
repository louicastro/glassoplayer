package com.liquidglass.music.service

import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SleepTimerState(
    val isActive: Boolean = false,
    val remainingSeconds: Long = 0L,
    val isEndOfTrack: Boolean = false
)

class SleepTimer(
    private val getPlayer: () -> ExoPlayer?,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _timerState = MutableStateFlow(SleepTimerState())
    val timerState: StateFlow<SleepTimerState> = _timerState.asStateFlow()

    private var timerJob: Job? = null
    private var originalVolume = 1.0f

    fun startTimerMinutes(minutes: Int) {
        startTimerSeconds(minutes * 60L)
    }

    fun startTimerSeconds(seconds: Long) {
        cancelTimer()
        val player = getPlayer()
        if (player != null) {
            originalVolume = player.volume
        }

        _timerState.value = SleepTimerState(
            isActive = true,
            remainingSeconds = seconds,
            isEndOfTrack = false
        )

        timerJob = scope.launch {
            var remaining = seconds
            val fadeOutStartSec = 15L // Start gradual fade-out in the last 15 seconds

            while (isActive && remaining > 0) {
                delay(1000L)
                remaining--
                _timerState.value = _timerState.value.copy(remainingSeconds = remaining)

                // Gentle audio fade-out to prevent abrupt silence
                if (remaining <= fadeOutStartSec) {
                    val fadeFraction = (remaining.toFloat() / fadeOutStartSec).coerceIn(0f, 1f)
                    getPlayer()?.volume = originalVolume * fadeFraction
                }
            }

            // Time's up: pause playback and restore normal volume
            val p = getPlayer()
            p?.pause()
            p?.volume = originalVolume
            _timerState.value = SleepTimerState(isActive = false, remainingSeconds = 0L)
        }
    }

    fun startEndOfTrack() {
        cancelTimer()
        _timerState.value = SleepTimerState(
            isActive = true,
            remainingSeconds = 0L,
            isEndOfTrack = true
        )
    }

    fun onTrackEnded() {
        if (_timerState.value.isActive && _timerState.value.isEndOfTrack) {
            getPlayer()?.pause()
            _timerState.value = SleepTimerState(isActive = false)
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        getPlayer()?.volume = originalVolume
        _timerState.value = SleepTimerState(isActive = false, remainingSeconds = 0L)
    }
}
