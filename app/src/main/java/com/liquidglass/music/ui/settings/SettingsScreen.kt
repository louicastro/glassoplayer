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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidglass.music.service.SleepTimerState
import com.liquidglass.music.ui.components.LiquidGlassBox
import com.liquidglass.music.ui.theme.LiquidAccentPrimary

@Composable
fun SettingsScreen(
    sleepTimerState: SleepTimerState,
    onStartSleepTimer: (Int) -> Unit,
    onStartEndOfTrackSleep: () -> Unit,
    onCancelSleepTimer: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onClearArtworkCache: () -> Unit,
    onClearLyricCache: () -> Unit,
    onRebuildLibraryIndex: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCodecDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sleep Timer Section
            item {
                Text("Sleep Timer", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(8.dp))
                LiquidGlassBox(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bedtime, contentDescription = null, tint = LiquidAccentPrimary)
                                Spacer(Modifier.size(10.dp))
                                Text("Sleep Timer Status", fontWeight = FontWeight.SemiBold)
                            }
                            if (sleepTimerState.isActive) {
                                val label = if (sleepTimerState.isEndOfTrack) "End of track" else "${sleepTimerState.remainingSeconds / 60}m ${sleepTimerState.remainingSeconds % 60}s"
                                Text(label, color = LiquidAccentPrimary, fontWeight = FontWeight.Bold)
                            } else {
                                Text("Off", color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(15, 30, 45, 60).forEach { mins ->
                                FilterChip(
                                    selected = false,
                                    onClick = { onStartSleepTimer(mins) },
                                    label = { Text("${mins}m") }
                                )
                            }
                            FilterChip(
                                selected = false,
                                onClick = onStartEndOfTrackSleep,
                                label = { Text("Track End") }
                            )
                        }

                        if (sleepTimerState.isActive) {
                            Spacer(Modifier.height(8.dp))
                            TextButton(
                                onClick = onCancelSleepTimer,
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF4E6E))
                            ) {
                                Text("Cancel Timer")
                            }
                        }
                    }
                }
            }

            // Audio Effects Link
            item {
                Text("Audio & Playback", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(8.dp))
                LiquidGlassBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenEqualizer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Equalizer, contentDescription = null, tint = LiquidAccentPrimary)
                            Spacer(Modifier.size(12.dp))
                            Text("Equalizer & Audio Effects", fontWeight = FontWeight.SemiBold)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }

            // Storage Management
            item {
                Text("Storage Management", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(8.dp))
                LiquidGlassBox(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onClearArtworkCache)
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Clear Artwork Cache")
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onClearLyricCache)
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Clear Lyrics Cache")
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onRebuildLibraryIndex)
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Rebuild Library Index")
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = LiquidAccentPrimary)
                        }
                    }
                }
            }

            // Privacy & Airplane Mode Compliance
            item {
                Text("Privacy & Offline Architecture", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(8.dp))
                LiquidGlassBox(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF4ECCA3))
                            Spacer(Modifier.size(10.dp))
                            Text("100% Offline by Design", fontWeight = FontWeight.Bold, color = Color(0xFF4ECCA3))
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "No internet permissions declared in AndroidManifest. No tracking, telemetry, or analytics. Works in Airplane Mode without accounts or subscription.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(0.75f))
                        )
                    }
                }
            }

            // Codec Information
            item {
                Text("Audio Codecs", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(8.dp))
                LiquidGlassBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCodecDialog = true },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = LiquidAccentPrimary)
                            Spacer(Modifier.size(12.dp))
                            Text("Codec Support Matrix", fontWeight = FontWeight.SemiBold)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }
        }

        // Codec Support Dialog
        if (showCodecDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showCodecDialog = false },
                title = { Text("Audio Codec Matrix") },
                text = {
                    Column(modifier = Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Tier 1 (Verified Native Support):", fontWeight = FontWeight.Bold, color = Color(0xFF4ECCA3))
                        Text("• MP3 (MPEG-1/2 Audio Layer 3)\n• AAC / M4A (Advanced Audio Coding)\n• FLAC (Free Lossless Audio Codec)\n• ALAC (Apple Lossless Audio Codec)\n• WAV (PCM 16/24-bit)\n• AIFF (Audio Interchange File Format)\n• Ogg Vorbis\n• Opus\n• MP4 & MKV audio containers", fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Tier 2 (Supported via Media3/Platform):", fontWeight = FontWeight.Bold)
                        Text("• AC-3 / E-AC-3\n• DTS\n• AMR\n• Speex", fontSize = 13.sp)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCodecDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
