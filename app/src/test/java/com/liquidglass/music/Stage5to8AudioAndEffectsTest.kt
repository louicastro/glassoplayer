package com.liquidglass.music

import com.liquidglass.music.playback.AudioEffectsController
import com.liquidglass.music.playback.EqualizerPreset
import com.liquidglass.music.service.SleepTimer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class Stage5to8AudioAndEffectsTest {

    @Test
    fun testSleepTimer_countdownAndCancellation() = runTest {
        val timer = SleepTimer(getPlayer = { null }, scope = this)

        // Start 30-second timer
        timer.startTimerSeconds(30L)
        assertTrue(timer.timerState.value.isActive)
        assertEquals(30L, timer.timerState.value.remainingSeconds)

        // Advance 10 seconds
        testScheduler.advanceTimeBy(10001L)
        assertEquals(20L, timer.timerState.value.remainingSeconds)

        // Cancel timer
        timer.cancelTimer()
        assertFalse(timer.timerState.value.isActive)
        assertEquals(0L, timer.timerState.value.remainingSeconds)
    }

    @Test
    fun testSleepTimer_endOfTrackMode() = runTest {
        val timer = SleepTimer(getPlayer = { null }, scope = this)

        timer.startEndOfTrack()
        assertTrue(timer.timerState.value.isActive)
        assertTrue(timer.timerState.value.isEndOfTrack)

        // Trigger track ended
        timer.onTrackEnded()
        assertFalse(timer.timerState.value.isActive)
    }

    @Test
    fun testAudioEffectsController_initialStateAndEnable() {
        val controller = AudioEffectsController()
        assertFalse(controller.effectsState.value.isEnabled)
        assertEquals(EqualizerPreset.FLAT, controller.effectsState.value.currentPreset)

        controller.setEnabled(true)
        assertTrue(controller.effectsState.value.isEnabled)

        controller.setBassBoostStrength(500)
        assertEquals(500, controller.effectsState.value.bassBoostStrength)

        controller.setVirtualizerStrength(800)
        assertEquals(800, controller.effectsState.value.virtualizerStrength)

        controller.setLoudnessGain(600)
        assertEquals(600, controller.effectsState.value.loudnessEnhancerGainMb)

        controller.setCrossfadeDuration(5)
        assertEquals(5, controller.effectsState.value.crossfadeDurationSeconds)

        controller.release()
    }

    @Test
    fun testAudioEffectsController_boundsChecking() {
        val controller = AudioEffectsController()

        // Negative values should clamp to 0
        controller.setBassBoostStrength(-100)
        assertEquals(0, controller.effectsState.value.bassBoostStrength)

        // Values over 1000 should clamp to 1000
        controller.setBassBoostStrength(2500)
        assertEquals(1000, controller.effectsState.value.bassBoostStrength)

        // Crossfade duration clamped to [0, 10]
        controller.setCrossfadeDuration(15)
        assertEquals(10, controller.effectsState.value.crossfadeDurationSeconds)

        controller.release()
    }
}
