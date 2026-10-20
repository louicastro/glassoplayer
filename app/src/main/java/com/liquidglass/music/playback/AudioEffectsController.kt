@file:Suppress("DEPRECATION")

package com.liquidglass.music.playback

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EqualizerBand(
    val index: Int,
    val centerFreqHz: Int,
    val minLevelMilliBels: Int,
    val maxLevelMilliBels: Int,
    val currentLevelMilliBels: Int
)

enum class EqualizerPreset(val displayName: String) {
    FLAT("Flat"),
    BASS_BOOST("Bass Boost"),
    TREBLE_BOOST("Treble Boost"),
    VOCAL("Vocal"),
    CUSTOM("Custom")
}

data class AudioEffectsState(
    val isEnabled: Boolean = false,
    val currentPreset: EqualizerPreset = EqualizerPreset.FLAT,
    val bands: List<EqualizerBand> = emptyList(),
    val bassBoostStrength: Int = 0, // 0 to 1000
    val virtualizerStrength: Int = 0, // 0 to 1000
    val loudnessEnhancerGainMb: Int = 0, // In millibels (e.g. 0 to 1000 mb = 0 to 10 dB)
    val crossfadeDurationSeconds: Int = 0 // 0 = off, 1-10s
)

@Suppress("DEPRECATION")
class AudioEffectsController {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private val _effectsState = MutableStateFlow(AudioEffectsState())
    val effectsState: StateFlow<AudioEffectsState> = _effectsState.asStateFlow()

    fun attachAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0) return
        release()

        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _effectsState.value.isEnabled
            }
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = _effectsState.value.isEnabled
            }
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = _effectsState.value.isEnabled
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                    enabled = _effectsState.value.isEnabled
                }
            }

            readBandsFromDevice()
            applyStateToEffects()
        } catch (e: Exception) {
            // Audio effect initialization might fail on unsupported devices/emulators
        }
    }

    private fun readBandsFromDevice() {
        val eq = equalizer ?: return
        val numBands = eq.numberOfBands.toInt()
        val range = eq.bandLevelRange // [min, max] in millibels (usually -1500 to +1500)
        val minLevel = range.getOrNull(0)?.toInt() ?: -1500
        val maxLevel = range.getOrNull(1)?.toInt() ?: 1500

        val bandList = mutableListOf<EqualizerBand>()
        for (i in 0 until numBands) {
            val centerFreq = eq.getCenterFreq(i.toShort()) / 1000 // Convert mHz to Hz
            val currentLevel = eq.getBandLevel(i.toShort()).toInt()
            bandList.add(
                EqualizerBand(
                    index = i,
                    centerFreqHz = centerFreq,
                    minLevelMilliBels = minLevel,
                    maxLevelMilliBels = maxLevel,
                    currentLevelMilliBels = currentLevel
                )
            )
        }
        _effectsState.value = _effectsState.value.copy(bands = bandList)
    }

    fun setEnabled(enabled: Boolean) {
        _effectsState.value = _effectsState.value.copy(isEnabled = enabled)
        equalizer?.enabled = enabled
        bassBoost?.enabled = enabled
        virtualizer?.enabled = enabled
        loudnessEnhancer?.enabled = enabled
    }

    fun setBandLevel(bandIndex: Int, levelMilliBels: Int) {
        val eq = equalizer ?: return
        try {
            eq.setBandLevel(bandIndex.toShort(), levelMilliBels.toShort())
            val updatedBands = _effectsState.value.bands.map { band ->
                if (band.index == bandIndex) band.copy(currentLevelMilliBels = levelMilliBels) else band
            }
            _effectsState.value = _effectsState.value.copy(
                bands = updatedBands,
                currentPreset = EqualizerPreset.CUSTOM
            )
        } catch (e: Exception) {
            // Ignore band error
        }
    }

    fun applyPreset(preset: EqualizerPreset) {
        val eq = equalizer ?: return
        val bands = _effectsState.value.bands
        if (bands.isEmpty()) return

        val targetLevels = when (preset) {
            EqualizerPreset.FLAT -> bands.map { 0 }
            EqualizerPreset.BASS_BOOST -> {
                // Boost lower frequencies, keep mids flat, slight treble cut
                bands.mapIndexed { idx, _ ->
                    when (idx) {
                        0 -> 600
                        1 -> 400
                        2 -> 200
                        else -> 0
                    }
                }
            }
            EqualizerPreset.TREBLE_BOOST -> {
                // Keep bass flat, boost upper bands
                bands.mapIndexed { idx, _ ->
                    when (idx) {
                        bands.lastIndex -> 600
                        bands.lastIndex - 1 -> 400
                        else -> 0
                    }
                }
            }
            EqualizerPreset.VOCAL -> {
                // Cut sub-bass, boost vocal frequencies (around 1kHz - 3kHz)
                bands.mapIndexed { idx, _ ->
                    when (idx) {
                        0 -> -200
                        1 -> 0
                        2 -> 400
                        3 -> 500
                        else -> 200
                    }
                }
            }
            EqualizerPreset.CUSTOM -> bands.map { it.currentLevelMilliBels }
        }

        targetLevels.forEachIndexed { idx, level ->
            try {
                eq.setBandLevel(idx.toShort(), level.toShort())
            } catch (e: Exception) {
                // Ignore
            }
        }

        val updatedBands = bands.mapIndexed { idx, band ->
            band.copy(currentLevelMilliBels = targetLevels.getOrElse(idx) { 0 })
        }

        _effectsState.value = _effectsState.value.copy(
            bands = updatedBands,
            currentPreset = preset
        )
    }

    fun setBassBoostStrength(strength: Int) {
        val safeStrength = strength.coerceIn(0, 1000)
        try {
            bassBoost?.setStrength(safeStrength.toShort())
            _effectsState.value = _effectsState.value.copy(bassBoostStrength = safeStrength)
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun setVirtualizerStrength(strength: Int) {
        val safeStrength = strength.coerceIn(0, 1000)
        try {
            virtualizer?.setStrength(safeStrength.toShort())
            _effectsState.value = _effectsState.value.copy(virtualizerStrength = safeStrength)
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun setLoudnessGain(gainMilliBels: Int) {
        val safeGain = gainMilliBels.coerceIn(0, 1000)
        try {
            loudnessEnhancer?.setTargetGain(safeGain)
            _effectsState.value = _effectsState.value.copy(loudnessEnhancerGainMb = safeGain)
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun setCrossfadeDuration(seconds: Int) {
        _effectsState.value = _effectsState.value.copy(crossfadeDurationSeconds = seconds.coerceIn(0, 10))
    }

    private fun applyStateToEffects() {
        val state = _effectsState.value
        setEnabled(state.isEnabled)
        setBassBoostStrength(state.bassBoostStrength)
        setVirtualizerStrength(state.virtualizerStrength)
        setLoudnessGain(state.loudnessEnhancerGainMb)
    }

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
            loudnessEnhancer?.release()
        } catch (e: Exception) {
            // Ignore
        }
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
    }
}
