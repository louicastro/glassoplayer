package com.liquidglass.music.lyrics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidglass.music.data.model.TrackEntity
import com.liquidglass.music.ui.components.LiquidGlassBox
import com.liquidglass.music.ui.theme.LiquidAccentPrimary
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
    var fontSizeSp by remember { mutableFloatStateOf(24f) }
    var lineSpacingSp by remember { mutableFloatStateOf(16f) }
    var showTimingControls by remember { mutableStateOf(false) }

    val isReducedMotion = LocalReducedMotion.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Detect when the user manually drags the lyrics list
    val isUserScrolling by listState.interactionSource.collectIsDraggedAsState()
    var userScrolledAway by remember { mutableStateOf(false) }

    LaunchedEffect(isUserScrolling) {
        if (isUserScrolling) {
            userScrolledAway = true
        }
    }

    // Auto-scroll to active line if user has not scrolled away
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

    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            paletteColors.dominant,
            paletteColors.surfaceTint,
            Color(0xFF0A0B10)
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
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Controls: Close, Title, Text Size, Offset, Instrumental
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Close Lyrics",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = track?.title ?: "Lyrics",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = track?.artist ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.7f)
                        ),
                        maxLines = 1
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Timing offset button
                    IconButton(
                        onClick = { showTimingControls = !showTimingControls },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Lyric Offset",
                            tint = if (showTimingControls) LiquidAccentPrimary else Color.White
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
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Lyrics",
                            tint = Color.White
                        )
                    }
                }
            }

            // Offset adjustment bar (when enabled)
            AnimatedVisibility(
                visible = showTimingControls && uiState is LyricUiState.Synced,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val currentOffset = (uiState as? LyricUiState.Synced)?.offsetMs ?: 0L
                LiquidGlassBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Offset: ${currentOffset}ms",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row {
                            IconButton(onClick = { onAdjustOffset(-50L) }) {
                                Icon(Icons.Default.Remove, contentDescription = "-50ms", tint = Color.White)
                            }
                            IconButton(onClick = { onAdjustOffset(50L) }) {
                                Icon(Icons.Default.Add, contentDescription = "+50ms", tint = Color.White)
                            }
                        }
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
                            Text("Loading lyrics...", color = Color.White.copy(alpha = 0.7f))
                        }
                    }

                    is LyricUiState.Synced -> {
                        val lines = uiState.parsed.lines
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 64.dp),
                            verticalArrangement = Arrangement.spacedBy(lineSpacingSp.dp)
                        ) {
                            itemsIndexed(lines) { index, line ->
                                val isActive = index == uiState.activeLineIndex
                                val alpha = if (isActive) 1.0f else 0.45f
                                val scale = if (isActive) 1.05f else 1.0f

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onSeekToTimestamp(line.timestampMs) }
                                        .padding(vertical = 4.dp, horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = line.text,
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontSize = (fontSizeSp * scale).sp,
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                            color = Color.White.copy(alpha = alpha),
                                            lineHeight = (fontSizeSp * 1.35f).sp
                                        ),
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
                                .padding(horizontal = 24.dp, vertical = 32.dp)
                        ) {
                            Text(
                                text = "Unsynchronized Lyrics",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Text(
                                text = uiState.text,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 18.sp,
                                    lineHeight = 28.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            )
                        }
                    }

                    is LyricUiState.Instrumental -> {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.MusicOff,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "Instrumental Track",
                                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                                )
                            }
                        }
                    }

                    is LyricUiState.Missing -> {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No lyrics found for this track",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                )
                                Spacer(Modifier.height(16.dp))
                                ElevatedButton(
                                    onClick = onToggleInstrumental,
                                    colors = ButtonDefaults.elevatedButtonColors(
                                        containerColor = Color(0x33FFFFFF),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Mark as Instrumental")
                                }
                            }
                        }
                    }
                }

                // Floating "Return to current line" pill if user manually scrolled away
                if (userScrolledAway && uiState is LyricUiState.Synced) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                    ) {
                        LiquidGlassBox(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    userScrolledAway = false
                                    coroutineScope.launch {
                                        val idx = (uiState.activeLineIndex - 2).coerceAtLeast(0)
                                        listState.animateScrollToItem(idx)
                                    }
                                },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = null,
                                    tint = LiquidAccentPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Sync to Now Playing",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
