package com.liquidglass.music.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.liquidglass.music.data.model.TrackEntity
import com.liquidglass.music.playback.RepeatMode
import com.liquidglass.music.ui.components.LiquidGlassBox
import com.liquidglass.music.ui.components.LiquidGlassButton
import com.liquidglass.music.ui.components.LiquidGlassPill
import com.liquidglass.music.ui.theme.GlassDarkBackdrop
import com.liquidglass.music.ui.theme.GlassLightBackdrop
import com.liquidglass.music.ui.theme.LiquidAccentPink
import com.liquidglass.music.ui.theme.LiquidAccentPrimary
import com.liquidglass.music.ui.theme.LiquidElevation
import com.liquidglass.music.ui.theme.LiquidMotion
import com.liquidglass.music.ui.theme.LiquidRadii
import com.liquidglass.music.ui.theme.LiquidSpacing
import com.liquidglass.music.ui.theme.LocalReducedMotion
import com.liquidglass.music.ui.theme.TextDarkPrimary
import com.liquidglass.music.ui.theme.TextDarkSecondary
import com.liquidglass.music.ui.theme.TextLightPrimary
import com.liquidglass.music.ui.theme.TextLightSecondary
import com.liquidglass.music.ui.util.ExtractedColors

@Composable
fun NowPlayingSheet(
    track: TrackEntity?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    playbackSpeed: Float,
    paletteColors: ExtractedColors,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenEqualizer: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (track == null) return

    val isDark = isSystemInDarkTheme()
    val isReducedMotion = LocalReducedMotion.current

    var isScrubbing by remember { mutableStateOf(false) }
    var scrubPositionMs by remember { mutableFloatStateOf(currentPositionMs.toFloat()) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    val displayPosition = if (isScrubbing) scrubPositionMs.toLong() else currentPositionMs
    val remainingMs = (durationMs - displayPosition).coerceAtLeast(0L)

    // Animated colors derived from artwork palette
    val animDominant by animateColorAsState(
        targetValue = paletteColors.dominant,
        animationSpec = tween(if (isReducedMotion) 0 else 450),
        label = "animDominant"
    )
    val animSurfaceTint by animateColorAsState(
        targetValue = paletteColors.surfaceTint,
        animationSpec = tween(if (isReducedMotion) 0 else 450),
        label = "animSurfaceTint"
    )

    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            animDominant.copy(alpha = if (isDark) 0.65f else 0.35f),
            animSurfaceTint.copy(alpha = if (isDark) 0.40f else 0.20f),
            if (isDark) GlassDarkBackdrop else GlassLightBackdrop
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = LiquidSpacing.xl)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(LiquidSpacing.xs))

            // Top Bar: Dismiss Chevron, Format Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = LiquidSpacing.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x26FFFFFF) else Color(0x1A000000))
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dismiss",
                        tint = if (isDark) Color.White else Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Audio Format & Quality Pill
                LiquidGlassPill(
                    tintColor = LiquidAccentPrimary.copy(alpha = if (isDark) 0.22f else 0.14f)
                ) {
                    Text(
                        text = track.format.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = LiquidAccentPrimary
                    )
                }
            }

            Spacer(Modifier.height(LiquidSpacing.md))

            // Center Hero Artwork
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(LiquidRadii.xxl),
                        ambientColor = if (isDark) animDominant.copy(alpha = 0.5f) else Color(0x33000000),
                        spotColor = if (isDark) animDominant else Color(0x4D000000)
                    )
                    .clip(RoundedCornerShape(LiquidRadii.xxl))
                    .background(if (isDark) Color(0x33FFFFFF) else Color(0x1A000000)),
                contentAlignment = Alignment.Center
            ) {
                if (track.artworkUri != null) {
                    AsyncImage(
                        model = track.artworkUri,
                        contentDescription = "Album Artwork",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = if (isDark) Color.White.copy(0.6f) else Color.Black.copy(0.6f),
                        modifier = Modifier.size(96.dp)
                    )
                }
            }

            Spacer(Modifier.height(LiquidSpacing.xxl))

            // Title, Artist & Favorite Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = if (isDark) TextDarkPrimary else TextLightPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(LiquidSpacing.xxs))
                    Text(
                        text = "${track.artist} — ${track.album}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (isDark) TextDarkSecondary else TextLightSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Favorite Toggle Button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (track.isFavorite) "Favorited" else "Not Favorited",
                        tint = if (track.isFavorite) LiquidAccentPink else (if (isDark) Color(0x99FFFFFF) else Color(0x992B3045)),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.height(LiquidSpacing.lg))

            // Precision Seek Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                val sliderValue = if (durationMs > 0) {
                    displayPosition.toFloat().coerceIn(0f, durationMs.toFloat())
                } else 0f

                Slider(
                    value = sliderValue,
                    onValueChange = {
                        isScrubbing = true
                        scrubPositionMs = it
                    },
                    onValueChangeFinished = {
                        onSeekTo(scrubPositionMs.toLong())
                        isScrubbing = false
                    },
                    valueRange = 0f..(if (durationMs > 0) durationMs.toFloat() else 1f),
                    colors = SliderDefaults.colors(
                        thumbColor = LiquidAccentPrimary,
                        activeTrackColor = LiquidAccentPrimary,
                        inactiveTrackColor = if (isDark) Color(0x33FFFFFF) else Color(0x26000000)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Time indicators (Elapsed & Remaining)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val elMin = (displayPosition / 1000) / 60
                    val elSec = (displayPosition / 1000) % 60
                    Text(
                        text = String.format("%d:%02d", elMin, elSec),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = if (isDark) TextDarkSecondary else TextLightSecondary
                    )

                    val remMin = (remainingMs / 1000) / 60
                    val remSec = (remainingMs / 1000) % 60
                    Text(
                        text = String.format("-%d:%02d", remMin, remSec),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = if (isDark) TextDarkSecondary else TextLightSecondary
                    )
                }
            }

            Spacer(Modifier.height(LiquidSpacing.lg))

            // Primary Playback Controls: Shuffle, Previous, Play/Pause, Next, Repeat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isShuffle) LiquidAccentPrimary.copy(alpha = if (isDark) 0.25f else 0.16f) else Color.Transparent)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) LiquidAccentPrimary else (if (isDark) Color(0x99FFFFFF) else Color(0x992B3045)),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous Button
                IconButton(
                    onClick = onSkipPrevious,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = if (isDark) Color.White else Color(0xFF131524),
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Center Play/Pause Floating Liquid Orb
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .shadow(12.dp, CircleShape, ambientColor = LiquidAccentPrimary.copy(0.4f), spotColor = LiquidAccentPrimary)
                        .clip(CircleShape)
                        .background(LiquidAccentPrimary)
                        .clickable(onClick = onTogglePlayPause),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = isPlaying,
                        transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                        label = "nowPlayingPlayPause"
                    ) { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Next Button
                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = if (isDark) Color.White else Color(0xFF131524),
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Repeat Button (OFF, ALL, ONE)
                IconButton(
                    onClick = onCycleRepeat,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (repeatMode != RepeatMode.OFF) LiquidAccentPrimary.copy(alpha = if (isDark) 0.25f else 0.16f) else Color.Transparent)
                ) {
                    Icon(
                        imageVector = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Repeat: $repeatMode",
                        tint = if (repeatMode != RepeatMode.OFF) LiquidAccentPrimary else (if (isDark) Color(0x99FFFFFF) else Color(0x992B3045)),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(LiquidSpacing.xxl))

            // Bottom Liquid Action Bar: Lyrics, Queue, Equalizer, Speed
            LiquidGlassBox(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(LiquidRadii.xl),
                elevation = LiquidElevation.subtle
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LiquidSpacing.md, vertical = LiquidSpacing.sm),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Lyrics Button
                    LiquidGlassButton(
                        onClick = onOpenLyrics,
                        shape = RoundedCornerShape(LiquidRadii.md),
                        tintColor = Color.Transparent
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = LiquidSpacing.sm, vertical = LiquidSpacing.xs)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lyrics,
                                contentDescription = "Lyrics",
                                tint = LiquidAccentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(LiquidSpacing.xs))
                            Text(
                                text = "Lyrics",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isDark) TextDarkPrimary else TextLightPrimary
                            )
                        }
                    }

                    // Queue Button
                    LiquidGlassButton(
                        onClick = onOpenQueue,
                        shape = RoundedCornerShape(LiquidRadii.md),
                        tintColor = Color.Transparent
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = LiquidSpacing.sm, vertical = LiquidSpacing.xs)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = "Queue",
                                tint = LiquidAccentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(LiquidSpacing.xs))
                            Text(
                                text = "Queue",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isDark) TextDarkPrimary else TextLightPrimary
                            )
                        }
                    }

                    // Equalizer Button
                    LiquidGlassButton(
                        onClick = onOpenEqualizer,
                        shape = RoundedCornerShape(LiquidRadii.md),
                        tintColor = Color.Transparent
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = LiquidSpacing.sm, vertical = LiquidSpacing.xs)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Equalizer,
                                contentDescription = "EQ",
                                tint = LiquidAccentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(LiquidSpacing.xs))
                            Text(
                                text = "EQ",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isDark) TextDarkPrimary else TextLightPrimary
                            )
                        }
                    }

                    // Speed Button
                    LiquidGlassButton(
                        onClick = { showSpeedDialog = true },
                        shape = RoundedCornerShape(LiquidRadii.md),
                        tintColor = Color.Transparent
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = LiquidSpacing.sm, vertical = LiquidSpacing.xs)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Speed",
                                tint = LiquidAccentPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(LiquidSpacing.xs))
                            Text(
                                text = "${playbackSpeed}x",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isDark) TextDarkPrimary else TextLightPrimary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(LiquidSpacing.xl))
        }

        // Playback Speed Dialog
        if (showSpeedDialog) {
            AlertDialog(
                onDismissRequest = { showSpeedDialog = false },
                title = { Text("Playback Speed", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(LiquidSpacing.xs)) {
                        listOf(0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f).forEach { speed ->
                            val isCurrent = playbackSpeed == speed
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(LiquidRadii.sm))
                                    .clickable {
                                        onSetSpeed(speed)
                                        showSpeedDialog = false
                                    }
                                    .padding(vertical = LiquidSpacing.sm, horizontal = LiquidSpacing.md),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${speed}x",
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) LiquidAccentPrimary else (if (isDark) TextDarkPrimary else TextLightPrimary)
                                )
                                if (isCurrent) {
                                    Text("Active", color = LiquidAccentPrimary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSpeedDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
