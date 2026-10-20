package com.liquidglass.music.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidglass.music.playback.AudioEffectsState
import com.liquidglass.music.playback.EqualizerPreset
import com.liquidglass.music.ui.components.LiquidGlassBox
import com.liquidglass.music.ui.theme.LiquidAccentPrimary

@Composable
fun EqualizerScreen(
    effectsState: AudioEffectsState,
    onToggleEnabled: (Boolean) -> Unit,
    onApplyPreset: (EqualizerPreset) -> Unit,
    onBandChange: (Int, Int) -> Unit,
    onBassBoostChange: (Int) -> Unit,
    onVirtualizerChange: (Int) -> Unit,
    onLoudnessGainChange: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Equalizer & Effects",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Master Toggle
            item {
                LiquidGlassBox(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable Audio Effects",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Switch(
                            checked = effectsState.isEnabled,
                            onCheckedChange = onToggleEnabled,
                            colors = SwitchDefaults.colors(checkedThumbColor = LiquidAccentPrimary)
                        )
                    }
                }
            }

            // Presets
            item {
                Text("Presets", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EqualizerPreset.entries.forEach { preset ->
                        FilterChip(
                            selected = effectsState.currentPreset == preset,
                            onClick = { onApplyPreset(preset) },
                            label = { Text(preset.displayName, fontSize = 12.sp) },
                            enabled = effectsState.isEnabled,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LiquidAccentPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Multi-band Equalizer Sliders
            item {
                Text("Equalizer Bands", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(8.dp))
                LiquidGlassBox(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        effectsState.bands.forEach { band ->
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val freqLabel = if (band.centerFreqHz >= 1000) "${band.centerFreqHz / 1000} kHz" else "${band.centerFreqHz} Hz"
                                    Text(freqLabel, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                    val dB = band.currentLevelMilliBels / 100
                                    Text("${if (dB > 0) "+" else ""}$dB dB", style = MaterialTheme.typography.labelSmall.copy(color = LiquidAccentPrimary))
                                }
                                Slider(
                                    value = band.currentLevelMilliBels.toFloat(),
                                    onValueChange = { onBandChange(band.index, it.toInt()) },
                                    valueRange = band.minLevelMilliBels.toFloat()..band.maxLevelMilliBels.toFloat(),
                                    enabled = effectsState.isEnabled,
                                    colors = SliderDefaults.colors(
                                        thumbColor = LiquidAccentPrimary,
                                        activeTrackColor = LiquidAccentPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Bass Boost & Virtualizer & Loudness Enhancer
            item {
                Text("Enhancements", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(8.dp))
                LiquidGlassBox(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Bass Boost
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Bass Boost", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                Text("${effectsState.bassBoostStrength / 10}%", style = MaterialTheme.typography.labelSmall.copy(color = LiquidAccentPrimary))
                            }
                            Slider(
                                value = effectsState.bassBoostStrength.toFloat(),
                                onValueChange = { onBassBoostChange(it.toInt()) },
                                valueRange = 0f..1000f,
                                enabled = effectsState.isEnabled,
                                colors = SliderDefaults.colors(thumbColor = LiquidAccentPrimary, activeTrackColor = LiquidAccentPrimary)
                            )
                        }

                        // Virtualizer
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Virtualizer (3D Audio)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                Text("${effectsState.virtualizerStrength / 10}%", style = MaterialTheme.typography.labelSmall.copy(color = LiquidAccentPrimary))
                            }
                            Slider(
                                value = effectsState.virtualizerStrength.toFloat(),
                                onValueChange = { onVirtualizerChange(it.toInt()) },
                                valueRange = 0f..1000f,
                                enabled = effectsState.isEnabled,
                                colors = SliderDefaults.colors(thumbColor = LiquidAccentPrimary, activeTrackColor = LiquidAccentPrimary)
                            )
                        }

                        // Loudness Enhancer
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Loudness Enhancer (ReplayGain)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                Text("${effectsState.loudnessEnhancerGainMb / 100} dB", style = MaterialTheme.typography.labelSmall.copy(color = LiquidAccentPrimary))
                            }
                            Slider(
                                value = effectsState.loudnessEnhancerGainMb.toFloat(),
                                onValueChange = { onLoudnessGainChange(it.toInt()) },
                                valueRange = 0f..1000f,
                                enabled = effectsState.isEnabled,
                                colors = SliderDefaults.colors(thumbColor = LiquidAccentPrimary, activeTrackColor = LiquidAccentPrimary)
                            )
                        }
                    }
                }
            }
        }
    }
}
