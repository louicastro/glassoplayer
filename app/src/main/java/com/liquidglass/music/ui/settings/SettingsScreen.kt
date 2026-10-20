package com.liquidglass.music.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidglass.music.service.SleepTimerState
import com.liquidglass.music.ui.components.LiquidGlassBox
import com.liquidglass.music.ui.components.LiquidGlassCard
import com.liquidglass.music.ui.components.LiquidGlassPill
import com.liquidglass.music.ui.theme.LiquidAccentPink
import com.liquidglass.music.ui.theme.LiquidAccentPrimary
import com.liquidglass.music.ui.theme.LiquidRadii
import com.liquidglass.music.ui.theme.LiquidSpacing
import com.liquidglass.music.ui.theme.LocalReducedMotion
import com.liquidglass.music.ui.theme.TextDarkPrimary
import com.liquidglass.music.ui.theme.TextDarkSecondary
import com.liquidglass.music.ui.theme.TextLightPrimary
import com.liquidglass.music.ui.theme.TextLightSecondary

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
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val isReducedMotion = LocalReducedMotion.current

    var showCodecDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showClearLyricsConfirm by remember { mutableStateOf(false) }
    var showClearArtworkConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = LiquidSpacing.xl)
    ) {
        Spacer(Modifier.height(LiquidSpacing.lg))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isDark) Color.White else Color.Black
                    )
                }
                Spacer(Modifier.width(LiquidSpacing.sm))
            }
            Column {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.6).sp
                    ),
                    color = if (isDark) TextDarkPrimary else TextLightPrimary
                )
                Text(
                    text = "Preferences and application controls",
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
            // 1. Appearance Section
            item {
                SectionTitle(title = "Appearance", icon = Icons.Default.Palette)
                Spacer(Modifier.height(LiquidSpacing.xs))
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(LiquidSpacing.md)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Liquid Glass Theme",
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) TextDarkPrimary else TextLightPrimary
                            )
                            LiquidGlassPill {
                                Text(
                                    if (isDark) "Dark Glass" else "Light Glass",
                                    fontSize = 12.sp,
                                    color = LiquidAccentPrimary
                                )
                            }
                        }
                        Spacer(Modifier.height(LiquidSpacing.xs))
                        Text(
                            "Theme automatically adapts to your system display mode.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) TextDarkSecondary else TextLightSecondary
                        )

                        if (isReducedMotion) {
                            Spacer(Modifier.height(LiquidSpacing.sm))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LiquidGlassPill(tintColor = Color(0x33F59E0B)) {
                                    Text("Reduced Motion Active", color = Color(0xFFF59E0B), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 2. Playback / Sleep Timer Section
            item {
                SectionTitle(title = "Playback & Sleep Timer", icon = Icons.Default.Bedtime)
                Spacer(Modifier.height(LiquidSpacing.xs))
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(LiquidSpacing.md)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Sleep Timer",
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) TextDarkPrimary else TextLightPrimary
                            )
                            if (sleepTimerState.isActive) {
                                val label = if (sleepTimerState.isEndOfTrack) "End of Track" else "${sleepTimerState.remainingSeconds / 60}m ${sleepTimerState.remainingSeconds % 60}s"
                                LiquidGlassPill(tintColor = LiquidAccentPrimary.copy(0.2f)) {
                                    Text(label, color = LiquidAccentPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            } else {
                                Text("Inactive", color = if (isDark) TextDarkSecondary else TextLightSecondary, fontSize = 13.sp)
                            }
                        }

                        Spacer(Modifier.height(LiquidSpacing.sm))

                        // Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.xs)
                        ) {
                            listOf(15, 30, 45, 60).forEach { mins ->
                                FilterChip(
                                    selected = false,
                                    onClick = { onStartSleepTimer(mins) },
                                    label = { Text("${mins}m") },
                                    colors = FilterChipDefaults.filterChipColors()
                                )
                            }
                            FilterChip(
                                selected = false,
                                onClick = onStartEndOfTrackSleep,
                                label = { Text("Track End") }
                            )
                        }

                        if (sleepTimerState.isActive) {
                            Spacer(Modifier.height(LiquidSpacing.xs))
                            TextButton(
                                onClick = onCancelSleepTimer,
                                colors = ButtonDefaults.textButtonColors(contentColor = LiquidAccentPink)
                            ) {
                                Text("Cancel Active Timer")
                            }
                        }
                    }
                }
            }

            // 3. Equalizer Section
            item {
                SectionTitle(title = "Audio & Equalizer", icon = Icons.Default.Equalizer)
                Spacer(Modifier.height(LiquidSpacing.xs))
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenEqualizer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(LiquidSpacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Graphic Equalizer & DSP Effects",
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) TextDarkPrimary else TextLightPrimary
                            )
                            Text(
                                "5-band frequency tuning, bass boost, virtualizer, and loudness gain.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) TextDarkSecondary else TextLightSecondary
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = if (isDark) Color.White.copy(0.6f) else Color.Black.copy(0.6f)
                        )
                    }
                }
            }

            // 4. Lyrics Section
            item {
                SectionTitle(title = "Synchronized Lyrics", icon = Icons.Default.Lyrics)
                Spacer(Modifier.height(LiquidSpacing.xs))
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(LiquidSpacing.md)) {
                        Text(
                            "Offline Lyric Syncing",
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) TextDarkPrimary else TextLightPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Glassoplayer reads embedded SYLT/USLT tags and matching sidecar .lrc files in the audio directory.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) TextDarkSecondary else TextLightSecondary
                        )
                        Spacer(Modifier.height(LiquidSpacing.sm))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(LiquidRadii.sm))
                                .clickable { showClearLyricsConfirm = true }
                                .padding(vertical = LiquidSpacing.xs),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Clear Cached Lyrics",
                                color = LiquidAccentPink,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(Icons.Default.Delete, contentDescription = null, tint = LiquidAccentPink, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // 5. Music Library Section
            item {
                SectionTitle(title = "Music Library Index", icon = Icons.Default.MusicNote)
                Spacer(Modifier.height(LiquidSpacing.xs))
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(LiquidSpacing.md)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onRebuildLibraryIndex),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Rebuild Audio Library Index",
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) TextDarkPrimary else TextLightPrimary
                                )
                                Text(
                                    "Rescans device storage for newly imported songs, albums, and artists.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) TextDarkSecondary else TextLightSecondary
                                )
                            }
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = LiquidAccentPrimary)
                        }

                        Spacer(Modifier.height(LiquidSpacing.md))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCodecDialog = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Supported Audio Codecs",
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) TextDarkPrimary else TextLightPrimary
                                )
                                Text(
                                    "Inspect MP3, FLAC, AAC, WAV, and OGG compatibility.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) TextDarkSecondary else TextLightSecondary
                                )
                            }
                            Icon(Icons.Default.Info, contentDescription = null, tint = LiquidAccentPrimary)
                        }
                    }
                }
            }

            // 6. Storage & Cache Section
            item {
                SectionTitle(title = "Storage & Cache", icon = Icons.Default.Storage)
                Spacer(Modifier.height(LiquidSpacing.xs))
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(LiquidSpacing.md)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showClearArtworkConfirm = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Clear Artwork Cache",
                                    fontWeight = FontWeight.SemiBold,
                                    color = LiquidAccentPink
                                )
                                Text(
                                    "Purges transient cached bitmaps. Does not delete any audio files or playlists.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) TextDarkSecondary else TextLightSecondary
                                )
                            }
                            Icon(Icons.Default.Delete, contentDescription = null, tint = LiquidAccentPink)
                        }
                    }
                }
            }

            // 7. Privacy & Data Safety Section
            item {
                SectionTitle(title = "Privacy & Data Safety", icon = Icons.Default.Security)
                Spacer(Modifier.height(LiquidSpacing.xs))
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(LiquidSpacing.md)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPrivacyDialog = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "100% Offline Guarantee",
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) TextDarkPrimary else TextLightPrimary
                                )
                                Text(
                                    "Zero analytics, trackers, or network access. Your music never leaves your device.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) TextDarkSecondary else TextLightSecondary
                                )
                            }
                            Icon(Icons.Default.Policy, contentDescription = null, tint = LiquidAccentPrimary)
                        }
                    }
                }
            }

            // 8. About & Licenses Section
            item {
                SectionTitle(title = "About Glassoplayer", icon = Icons.Default.Info)
                Spacer(Modifier.height(LiquidSpacing.xs))
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(LiquidSpacing.md)) {
                        Text(
                            "Glassoplayer v1.0.0",
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) TextDarkPrimary else TextLightPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Built with Jetpack Compose, Media3 ExoPlayer, and Room for high-fidelity offline music playback.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) TextDarkSecondary else TextLightSecondary
                        )
                    }
                }
            }
        }
    }

    // Codec Dialog
    if (showCodecDialog) {
        AlertDialog(
            onDismissRequest = { showCodecDialog = false },
            title = { Text("Supported Audio Formats", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(LiquidSpacing.xs)) {
                    Text("• MP3 (MPEG Audio Layer 3)")
                    Text("• FLAC (Free Lossless Audio Codec)")
                    Text("• AAC / M4A (Advanced Audio Coding)")
                    Text("• OGG Vorbis / Opus")
                    Text("• WAV (PCM / Waveform Audio)")
                }
            },
            confirmButton = {
                TextButton(onClick = { showCodecDialog = false }) { Text("Close") }
            }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy & Data Safety", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(LiquidSpacing.sm)) {
                    Text(
                        "Glassoplayer is an offline-first music player that does not request or possess the android.permission.INTERNET permission.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "No personal information, identifiers, usage logs, or audio files are ever collected, transmitted, or shared.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/louicastro/glassoplayer/blob/main/PRIVACY_POLICY.md")
                        )
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidAccentPrimary)
                ) {
                    Text("View Online Policy")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("Close") }
            }
        )
    }

    // Clear Lyrics Confirmation Dialog
    if (showClearLyricsConfirm) {
        AlertDialog(
            onDismissRequest = { showClearLyricsConfirm = false },
            title = { Text("Clear Lyric Cache?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This will clear the cached database copies of lyrics. Your original audio files and .lrc files will remain completely untouched.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearLyricCache()
                        showClearLyricsConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidAccentPink)
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearLyricsConfirm = false }) { Text("Cancel") }
            }
        )
    }

    // Clear Artwork Confirmation Dialog
    if (showClearArtworkConfirm) {
        AlertDialog(
            onDismissRequest = { showClearArtworkConfirm = false },
            title = { Text("Clear Artwork Cache?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This will purge locally cached artwork thumbnails. They will reload automatically from your audio files upon playback.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearArtworkCache()
                        showClearArtworkConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidAccentPink)
                ) {
                    Text("Clear Cache")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearArtworkConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SectionTitle(title: String, icon: ImageVector) {
    val isDark = isSystemInDarkTheme()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.xs)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LiquidAccentPrimary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp
            ),
            color = if (isDark) TextDarkPrimary else TextLightPrimary
        )
    }
}
