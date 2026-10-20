package com.liquidglass.music.ui.player

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.liquidglass.music.data.model.TrackEntity
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
fun QueueSheet(
    queue: List<TrackEntity>,
    currentTrack: TrackEntity?,
    onTrackSelected: (Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xEB0E101A) else Color(0xF5F6F8FC))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = LiquidSpacing.lg)
        ) {
            Spacer(Modifier.height(LiquidSpacing.md))

            // Header: Title, Track count, Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Playing Next",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = if (isDark) TextDarkPrimary else TextLightPrimary
                    )
                    Text(
                        text = "${queue.size} songs in queue",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) TextDarkSecondary else TextLightSecondary
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x26FFFFFF) else Color(0x1A000000))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Queue",
                        tint = if (isDark) Color.White else Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(LiquidSpacing.lg))

            if (queue.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Queue is empty",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) TextDarkSecondary else TextLightSecondary
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 60.dp),
                    verticalArrangement = Arrangement.spacedBy(LiquidSpacing.sm)
                ) {
                    itemsIndexed(queue) { index, track ->
                        val isCurrent = track.id == currentTrack?.id

                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onTrackSelected(index) },
                            tintColor = if (isCurrent) LiquidAccentPrimary.copy(alpha = if (isDark) 0.22f else 0.14f) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = LiquidSpacing.md, vertical = LiquidSpacing.sm + 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Track number or Playing indicator
                                Box(
                                    modifier = Modifier.size(28.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCurrent) {
                                        Icon(
                                            imageVector = Icons.Default.Equalizer,
                                            contentDescription = "Now Playing",
                                            tint = LiquidAccentPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                            color = if (isDark) TextDarkSecondary else TextLightSecondary
                                        )
                                    }
                                }

                                Spacer(Modifier.width(LiquidSpacing.sm))

                                // Artwork
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(LiquidRadii.sm))
                                        .background(if (isDark) Color(0x26FFFFFF) else Color(0x1A000000)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (track.artworkUri != null) {
                                        AsyncImage(
                                            model = track.artworkUri,
                                            contentDescription = null,
                                            modifier = Modifier.size(42.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = if (isDark) Color.White.copy(0.6f) else Color.Black.copy(0.6f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.width(LiquidSpacing.md))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 14.5.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isCurrent) LiquidAccentPrimary else (if (isDark) TextDarkPrimary else TextLightPrimary),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = track.artist,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = if (isDark) TextDarkSecondary else TextLightSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(Modifier.width(LiquidSpacing.sm))

                                val min = (track.durationMs / 1000) / 60
                                val sec = (track.durationMs / 1000) % 60
                                Text(
                                    text = String.format("%d:%02d", min, sec),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDark) TextDarkSecondary else TextLightSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
