package com.liquidglass.music.ui.search

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.liquidglass.music.ui.components.LiquidGlassPill
import com.liquidglass.music.ui.theme.LiquidAccentPrimary
import com.liquidglass.music.ui.theme.LiquidRadii
import com.liquidglass.music.ui.theme.LiquidSpacing
import com.liquidglass.music.ui.theme.TextDarkPrimary
import com.liquidglass.music.ui.theme.TextDarkSecondary
import com.liquidglass.music.ui.theme.TextLightPrimary
import com.liquidglass.music.ui.theme.TextLightSecondary

enum class SearchCategory {
    ALL, TRACKS, ARTISTS, ALBUMS
}

@Composable
fun SearchScreen(
    tracks: List<TrackEntity>,
    onPlayTrack: (TrackEntity, List<TrackEntity>) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(SearchCategory.ALL) }

    val recentSuggestions = remember(tracks) {
        tracks.take(5).map { it.artist }.distinct().take(4)
    }

    val results = remember(tracks, searchQuery, selectedCategory) {
        val query = searchQuery.trim().lowercase()
        if (query.isEmpty()) {
            emptyList()
        } else {
            tracks.filter { track ->
                when (selectedCategory) {
                    SearchCategory.ALL -> {
                        track.title.lowercase().contains(query) ||
                                track.artist.lowercase().contains(query) ||
                                track.album.lowercase().contains(query) ||
                                (track.genre?.lowercase()?.contains(query) == true)
                    }
                    SearchCategory.TRACKS -> track.title.lowercase().contains(query)
                    SearchCategory.ARTISTS -> track.artist.lowercase().contains(query)
                    SearchCategory.ALBUMS -> track.album.lowercase().contains(query)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = LiquidSpacing.xl)
    ) {
        Spacer(Modifier.height(LiquidSpacing.lg))

        Text(
            text = "Search",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.6).sp
            ),
            color = if (isDark) TextDarkPrimary else TextLightPrimary
        )

        Spacer(Modifier.height(LiquidSpacing.md))

        // Prominent Liquid Glass Search Bar
        LiquidGlassBox(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(LiquidRadii.xl)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search titles, artists, albums...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isDark) Color(0x80FFFFFF) else Color(0x802B3045)
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = LiquidAccentPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear Search",
                                tint = if (isDark) Color.White.copy(0.7f) else Color.Black.copy(0.7f)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = if (isDark) TextDarkPrimary else TextLightPrimary,
                    unfocusedTextColor = if (isDark) TextDarkPrimary else TextLightPrimary
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(LiquidSpacing.sm))

        // Filter Chips: All, Tracks, Artists, Albums
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.sm),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(SearchCategory.entries) { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LiquidAccentPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(Modifier.height(LiquidSpacing.md))

        if (searchQuery.isEmpty()) {
            // Empty / Suggestions State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = LiquidSpacing.xl)
            ) {
                if (recentSuggestions.isNotEmpty()) {
                    Text(
                        text = "Quick Searches",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (isDark) TextDarkSecondary else TextLightSecondary
                    )
                    Spacer(Modifier.height(LiquidSpacing.sm))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.sm)) {
                        items(recentSuggestions) { suggestion ->
                            LiquidGlassCard(
                                onClick = { searchQuery = suggestion },
                                shape = RoundedCornerShape(LiquidRadii.pill)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = LiquidSpacing.md, vertical = LiquidSpacing.sm),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = LiquidAccentPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(LiquidSpacing.xs))
                                    Text(
                                        text = suggestion,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = if (isDark) TextDarkPrimary else TextLightPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(LiquidSpacing.xxxl))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Search across ${tracks.size} tracks stored on your device completely offline.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) TextDarkSecondary else TextLightSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = LiquidSpacing.xxl)
                    )
                }
            }
        } else if (results.isEmpty()) {
            // No Results Found State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(LiquidSpacing.xxl),
                contentAlignment = Alignment.Center
            ) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(LiquidRadii.xxl)
                ) {
                    Column(
                        modifier = Modifier.padding(LiquidSpacing.xxl),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No results found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) TextDarkPrimary else TextLightPrimary
                        )
                        Spacer(Modifier.height(LiquidSpacing.xs))
                        Text(
                            text = "No tracks matching \"$searchQuery\" in ${selectedCategory.name.lowercase()}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) TextDarkSecondary else TextLightSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Instant Results List
            LazyColumn(
                contentPadding = PaddingValues(bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(LiquidSpacing.sm)
            ) {
                items(results, key = { it.id }) { track ->
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onPlayTrack(track, results) },
                        shape = RoundedCornerShape(LiquidRadii.md)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = LiquidSpacing.md, vertical = LiquidSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(LiquidRadii.sm))
                                    .background(if (isDark) Color(0x33FFFFFF) else Color(0x1A000000)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (track.artworkUri != null) {
                                    AsyncImage(
                                        model = track.artworkUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = if (isDark) Color.White.copy(0.6f) else Color.Black.copy(0.6f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.width(LiquidSpacing.md))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    ),
                                    color = if (isDark) TextDarkPrimary else TextLightPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${track.artist} — ${track.album}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = if (isDark) TextDarkSecondary else TextLightSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(Modifier.width(LiquidSpacing.sm))

                            val min = (track.durationMs / 1000) / 60
                            val sec = (track.durationMs / 1000) % 60
                            LiquidGlassPill {
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
