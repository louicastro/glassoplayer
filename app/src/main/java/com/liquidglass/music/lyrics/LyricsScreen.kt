package com.liquidglass.music.lyrics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidglass.music.data.model.TrackEntity
import com.liquidglass.music.ui.components.LiquidGlassBox
import com.liquidglass.music.ui.components.LiquidGlassButton
import com.liquidglass.music.ui.components.LiquidGlassCard
import com.liquidglass.music.ui.components.LiquidGlassPill
import com.liquidglass.music.ui.theme.GlassDarkBackdrop
import com.liquidglass.music.ui.theme.GlassLightBackdrop
import com.liquidglass.music.ui.theme.LiquidAccentPrimary
import com.liquidglass.music.ui.theme.LiquidElevation
import com.liquidglass.music.ui.theme.LiquidMotion
import com.liquidglass.music.ui.theme.LiquidRadii
import com.liquidglass.music.ui.theme.LiquidSpacing
import com.liquidglass.music.ui.theme.LocalReducedMotion
import com.liquidglass.music.ui.util.ExtractedColors
import kotlinx.coroutines.launch

@Composable
fun LyricsScreen(
    track: TrackEntity?,
    uiState: LyricUiState,
    currentPlaybackPositionMs: Long,
    paletteColors: ExtractedColors,
    onSeekToTimestamp: (Long) -> Unit,
    onAdjustOffset: (Long) -> Unit,
    onToggleInstrumental: () -> Unit,
    onStartEditor: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fontSizeSp by remember { mutableFloatStateOf(26f) }
    var lineSpacingSp by remember { mutableFloatStateOf(16f) }
    var showTimingControls by remember { mutableStateOf(false) }
    var showSizeControls by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()
    val isReducedMotion = LocalReducedMotion.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Detect user manual dragging
    val isUserScrolling by listState.interactionSource.collectIsDraggedAsState()
    var userScrolledAway by remember { mutableStateOf(false) }

    LaunchedEffect(isUserScrolling) {
        if (isUserScrolling) {
            userScrolledAway = true
        }
    }

    // Auto-scroll when playing
    if (uiState is LyricUiState.Synced) {
        val activeIndex = uiState.activeLineIndex
        LaunchedEffect(activeIndex, userScrolledAway) {
            if (activeIndex >= 0 && !userScrolledAway) {
                val targetIndex = (activeIndex - 2).coerceAtLeast(0)
                if (isReducedMotion) {
                    listState.scrollToItem(targetIndex)
                } else {
                    listState.animateScrollToItem(targetIndex)
                }
            }
        }
    }

    val animDominant by animateColorAsState(
        targetValue = paletteColors.dominant,
        animationSpec = tween(if (isReducedMotion) 0 else 400),
        label = "lyricsDominant"
    )

    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            animDominant.copy(alpha = if (isDark) 0.70f else 0.40f),
            paletteColors.surfaceTint.copy(alpha = if (isDark) 0.50f else 0.25f),
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
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Dismiss Chevron, Track Info, and Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LiquidSpacing.lg, vertical = LiquidSpacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x26FFFFFF) else Color(0x1A000000))
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Close Lyrics",
                        tint = if (isDark) Color.White else Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).padding(horizontal = LiquidSpacing.sm)
                ) {
                    Text(
                        text = track?.title ?: "Lyrics",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isDark) Color.White else Color.Black,
                        maxLines = 1
                    )
                    Text(
                        text = track?.artist ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (isDark) Color(0xB3FFFFFF) else Color(0xB32B3045),
                        maxLines = 1
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Size adjuster toggle
                    IconButton(
                        onClick = {
                            showSizeControls = !showSizeControls
                            if (showSizeControls) showTimingControls = false
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "Text Size",
                            tint = if (showSizeControls) LiquidAccentPrimary else (if (isDark) Color.White else Color.Black)
                        )
                    }

                    // Timing offset button
                    IconButton(
                        onClick = {
                            showTimingControls = !showTimingControls
                            if (showTimingControls) showSizeControls = false
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Lyric Offset",
                            tint = if (showTimingControls) LiquidAccentPrimary else (if (isDark) Color.White else Color.Black)
                        )
                    }

                    // Edit button
                    IconButton(
                        onClick = {
                            val initial = when (uiState) {
                                is LyricUiState.Synced -> uiState.parsed.rawText
                                is LyricUiState.Plain -> uiState.text
                                else -> ""
                            }
                            onStartEditor(initial)
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Lyrics",
                            tint = if (isDark) Color.White else Color.Black
                        )
                    }
                }
            }

            // Offset adjustment bar
            AnimatedVisibility(
                visible = showTimingControls && uiState is LyricUiState.Synced,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val currentOffset = (uiState as? LyricUiState.Synced)?.offsetMs ?: 0L
                LiquidGlassBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LiquidSpacing.xl, vertical = LiquidSpacing.xs),
                    shape = RoundedCornerShape(LiquidRadii.md),
                    elevation = LiquidElevation.subtle
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = LiquidSpacing.md, vertical = LiquidSpacing.xs),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sync Offset: ${currentOffset}ms",
                            color = if (isDark) Color.White else Color.Black,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Row {
                            IconButton(onClick = { onAdjustOffset(-50L) }) {
                                Icon(Icons.Default.Remove, contentDescription = "-50ms", tint = LiquidAccentPrimary)
                            }
                            IconButton(onClick = { onAdjustOffset(50L) }) {
                                Icon(Icons.Default.Add, contentDescription = "+50ms", tint = LiquidAccentPrimary)
                            }
                        }
                    }
                }
            }

            // Text size adjuster bar
            AnimatedVisibility(
                visible = showSizeControls,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LiquidGlassBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LiquidSpacing.xl, vertical = LiquidSpacing.xs),
                    shape = RoundedCornerShape(LiquidRadii.md),
                    elevation = LiquidElevation.subtle
                ) {
                    Column(modifier = Modifier.padding(LiquidSpacing.md)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Lyric Font Size: ${fontSizeSp.toInt()}sp",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isDark) Color.White else Color.Black
                            )
                        }
                        Slider(
                            value = fontSizeSp,
                            onValueChange = { fontSizeSp = it },
                            valueRange = 18f..38f,
                            colors = SliderDefaults.colors(
                                thumbColor = LiquidAccentPrimary,
                                activeTrackColor = LiquidAccentPrimary
                            )
                        )
                    }
                }
            }

            // Main Lyrics View depending on state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (uiState) {
                    is LyricUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            Text(
                                "Loading lyrics...",
                                color = if (isDark) Color.White.copy(0.7f) else Color.Black.copy(0.7f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    is LyricUiState.Synced -> {
                        val lines = uiState.parsed.lines
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = LiquidSpacing.xl, vertical = 72.dp),
                            verticalArrangement = Arrangement.spacedBy(lineSpacingSp.dp)
                        ) {
                            itemsIndexed(lines) { index, line ->
                                val isActive = index == uiState.activeLineIndex
                                val alpha by animateFloatAsState(
                                    targetValue = if (isActive) 1.0f else (if (isDark) 0.38f else 0.45f),
                                    animationSpec = tween(if (isReducedMotion) 0 else 200),
                                    label = "lyricAlpha"
                                )
                                val scale by animateFloatAsState(
                                    targetValue = if (isActive && !isReducedMotion) 1.06f else 1.0f,
                                    animationSpec = tween(if (isReducedMotion) 0 else 200),
                                    label = "lyricScale"
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .scale(scale)
                                        .clip(RoundedCornerShape(LiquidRadii.sm))
                                        .clickable { onSeekToTimestamp(line.timestampMs) }
                                        .padding(vertical = LiquidSpacing.xs, horizontal = LiquidSpacing.xs)
                                ) {
                                    Text(
                                        text = line.text,
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontSize = fontSizeSp.sp,
                                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            lineHeight = (fontSizeSp * 1.35f).sp,
                                            letterSpacing = (-0.3).sp
                                        ),
                                        color = if (isActive) (if (isDark) Color.White else Color(0xFF0F111E)) else (if (isDark) Color.White.copy(alpha) else Color.Black.copy(alpha)),
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                        }
                    }

                    is LyricUiState.Plain -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = LiquidSpacing.xl, vertical = LiquidSpacing.xl)
                        ) {
                            LiquidGlassPill(
                                modifier = Modifier.padding(bottom = LiquidSpacing.md),
                                tintColor = LiquidAccentPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Plain Text Lyrics",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LiquidAccentPrimary
                                )
                            }
                            Text(
                                text = uiState.text,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = fontSizeSp.sp,
                                    lineHeight = (fontSizeSp * 1.4f).sp,
                                    color = if (isDark) Color.White.copy(0.9f) else Color.Black.copy(0.9f)
                                )
                            )
                        }
                    }

                    is LyricUiState.Instrumental -> {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            LiquidGlassCard(
                                modifier = Modifier.padding(LiquidSpacing.xxl),
                                shape = RoundedCornerShape(LiquidRadii.xl)
                            ) {
                                Column(
                                    modifier = Modifier.padding(LiquidSpacing.xxl),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicOff,
                                        contentDescription = null,
                                        tint = LiquidAccentPrimary,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(Modifier.height(LiquidSpacing.md))
                                    Text(
                                        text = "Instrumental Track",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = if (isDark) Color.White else Color.Black
                                    )
                                }
                            }
                        }
                    }

                    is LyricUiState.Missing -> {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            LiquidGlassCard(
                                modifier = Modifier.padding(LiquidSpacing.xxl),
                                shape = RoundedCornerShape(LiquidRadii.xl)
                            ) {
                                Column(
                                    modifier = Modifier.padding(LiquidSpacing.xxl),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No lyrics found for this song",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                        color = if (isDark) Color.White.copy(0.7f) else Color.Black.copy(0.7f)
                                    )
                                    Spacer(Modifier.height(LiquidSpacing.lg))
                                    ElevatedButton(
                                        onClick = onToggleInstrumental,
                                        colors = ButtonDefaults.elevatedButtonColors(
                                            containerColor = LiquidAccentPrimary,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text("Mark as Instrumental")
                                    }
                                }
                            }
                        }
                    }
                }

                // Floating "Sync to Now Playing" button when user scrolls away
                if (userScrolledAway && uiState is LyricUiState.Synced) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = LiquidSpacing.xxl)
                    ) {
                        LiquidGlassButton(
                            onClick = {
                                userScrolledAway = false
                                coroutineScope.launch {
                                    val idx = (uiState.activeLineIndex - 2).coerceAtLeast(0)
                                    listState.animateScrollToItem(idx)
                                }
                            },
                            shape = RoundedCornerShape(LiquidRadii.pill),
                            tintColor = LiquidAccentPrimary
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = LiquidSpacing.lg, vertical = LiquidSpacing.sm + 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(LiquidSpacing.sm))
                                Text(
                                    text = "Sync to Now Playing",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
