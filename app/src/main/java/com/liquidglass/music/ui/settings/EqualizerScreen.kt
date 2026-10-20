package com.liquidglass.music.ui.settings

import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.width
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
import com.liquidglass.music.ui.components.LiquidGlassCard
import com.liquidglass.music.ui.theme.LiquidAccentPrimary
import com.liquidglass.music.ui.theme.LiquidRadii
import com.liquidglass.music.ui.theme.LiquidSpacing
import com.liquidglass.music.ui.theme.TextDarkPrimary
import com.liquidglass.music.ui.theme.TextDarkSecondary
import com.liquidglass.music.ui.theme.TextLightPrimary
import com.liquidglass.music.ui.theme.TextLightSecondary

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
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = LiquidSpacing.xl)
    ) {
        Spacer(Modifier.height(LiquidSpacing.lg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (isDark) Color.White else Color.Black
                )
            }
            Spacer(Modifier.width(LiquidSpacing.sm))
            Column {
                Text(
                    text = "Equalizer & Effects",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.6).sp
                    ),
                    color = if (isDark) TextDarkPrimary else TextLightPrimary
                )
                Text(
                    text = "DSP audio shaping and frequency tuning",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) TextDarkSecondary else TextLightSecondary
                )
            }
        }

        Spacer(Modifier.height(LiquidSpacing.md))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(LiquidSpacing.lg)
        ) {
            // Master Toggle
            item {
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(LiquidSpacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Enable Audio Effects",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) TextDarkPrimary else TextLightPrimary
                            )
                            Text(
                                text = "Applies real-time DSP equalization to audio session",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) TextDarkSecondary else TextLightSecondary
                            )
                        }
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
                Text(
                    "Presets",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isDark) TextDarkPrimary else TextLightPrimary
                )
                Spacer(Modifier.height(LiquidSpacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.sm)
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
                Text(
                    "Frequency Bands",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isDark) TextDarkPrimary else TextLightPrimary
                )
                Spacer(Modifier.height(LiquidSpacing.xs))
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(LiquidSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(LiquidSpacing.md)
                    ) {
                        effectsState.bands.forEach { band ->
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val freqLabel = if (band.centerFreqHz >= 1000) "${band.centerFreqHz / 1000} kHz" else "${band.centerFreqHz} Hz"
                                    Text(
                                        freqLabel,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isDark) TextDarkPrimary else TextLightPrimary
                                    )
                                    val dB = band.currentLevelMilliBels / 100
                                    Text(
                                        "${if (dB > 0) "+" else ""}$dB dB",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = LiquidAccentPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
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
                Text(
                    "Acoustic Enhancements",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isDark) TextDarkPrimary else TextLightPrimary
                )
                Spacer(Modifier.height(LiquidSpacing.xs))
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(LiquidSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(LiquidSpacing.md)
                    ) {
                        // Bass Boost
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Bass Boost",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isDark) TextDarkPrimary else TextLightPrimary
                                )
                                Text(
                                    "${effectsState.bassBoostStrength / 10}%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = LiquidAccentPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Slider(
                                value = effectsState.bassBoostStrength.toFloat(),
                                onValueChange = { onBassBoostChange(it.toInt()) },
                                valueRange = 0f..1000f,
                                enabled = effectsState.isEnabled,
                                colors = SliderDefaults.colors(
                                    thumbColor = LiquidAccentPrimary,
                                    activeTrackColor = LiquidAccentPrimary
                                )
                            )
                        }

                        // Virtualizer
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Spatial Virtualizer",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isDark) TextDarkPrimary else TextLightPrimary
                                )
                                Text(
                                    "${effectsState.virtualizerStrength / 10}%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = LiquidAccentPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Slider(
                                value = effectsState.virtualizerStrength.toFloat(),
                                onValueChange = { onVirtualizerChange(it.toInt()) },
                                valueRange = 0f..1000f,
                                enabled = effectsState.isEnabled,
                                colors = SliderDefaults.colors(
                                    thumbColor = LiquidAccentPrimary,
                                    activeTrackColor = LiquidAccentPrimary
                                )
                            )
                        }

                        // Loudness Gain
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Loudness Enhancer",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isDark) TextDarkPrimary else TextLightPrimary
                                )
                                Text(
                                    "+${effectsState.loudnessEnhancerGainMb / 100} dB",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = LiquidAccentPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Slider(
                                value = effectsState.loudnessEnhancerGainMb.toFloat(),
                                onValueChange = { onLoudnessGainChange(it.toInt()) },
                                valueRange = 0f..1000f,
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
    }
}
