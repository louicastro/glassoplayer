package com.liquidglass.music.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.liquidglass.music.data.model.AlbumEntity
import com.liquidglass.music.data.model.ArtistEntity
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

enum class LibraryTab(val displayName: String) {
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    GENRES("Genres")
}

enum class SortOrder(val displayName: String) {
    TITLE("Title (A-Z)"),
    ARTIST("Artist (A-Z)"),
    DATE_ADDED("Recently Added"),
    DURATION("Duration")
}

@Composable
fun LibraryScreen(
    tracks: List<TrackEntity>,
    albums: List<AlbumEntity>,
    artists: List<ArtistEntity>,
    genres: List<String>,
    onPlayTrack: (TrackEntity, List<TrackEntity>) -> Unit,
    onAlbumClick: (AlbumEntity) -> Unit = {},
    onArtistClick: (ArtistEntity) -> Unit = {},
    onSaveTrackMetadata: (TrackEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    var selectedTab by remember { mutableStateOf(LibraryTab.SONGS) }
    var sortOrder by remember { mutableStateOf(SortOrder.TITLE) }
    var showSortMenu by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(true) }
    var selectedGenre by remember { mutableStateOf<String?>(null) }
    var editingTrack by remember { mutableStateOf<TrackEntity?>(null) }

    val filteredTracks = remember(tracks, selectedGenre, sortOrder) {
        val filtered = if (selectedGenre != null) {
            tracks.filter { it.genre.equals(selectedGenre, ignoreCase = true) }
        } else {
            tracks
        }
        when (sortOrder) {
            SortOrder.TITLE -> filtered.sortedBy { it.title.lowercase() }
            SortOrder.ARTIST -> filtered.sortedBy { it.artist.lowercase() }
            SortOrder.DATE_ADDED -> filtered.sortedByDescending { it.dateAdded }
            SortOrder.DURATION -> filtered.sortedByDescending { it.durationMs }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = LiquidSpacing.xl)
    ) {
        Spacer(Modifier.height(LiquidSpacing.lg))

        // Top Header: Title, Grid/List toggle, Sort Menu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.6).sp
                    ),
                    color = if (isDark) TextDarkPrimary else TextLightPrimary
                )
                Text(
                    text = "${tracks.size} tracks available offline",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) TextDarkSecondary else TextLightSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selectedTab == LibraryTab.ALBUMS || selectedTab == LibraryTab.ARTISTS) {
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                            contentDescription = "Toggle Grid/List",
                            tint = if (isDark) Color.White else Color.Black
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort",
                            tint = if (isDark) Color.White else Color.Black
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        order.displayName,
                                        fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal,
                                        color = if (sortOrder == order) LiquidAccentPrimary else Color.Unspecified
                                    )
                                },
                                onClick = {
                                    sortOrder = order
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(LiquidSpacing.md))

        // Segmented Liquid Pill Bar for Tab Categories
        LiquidGlassBox(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(LiquidRadii.xl)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LibraryTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(LiquidRadii.lg))
                            .background(
                                if (isSelected) LiquidAccentPrimary.copy(alpha = if (isDark) 0.25f else 0.16f)
                                else Color.Transparent
                            )
                            .clickable { selectedTab = tab }
                            .padding(vertical = LiquidSpacing.sm),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) LiquidAccentPrimary else (if (isDark) TextDarkSecondary else TextLightSecondary)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(LiquidSpacing.md))

        // Content Area based on Tab
        when (selectedTab) {
            LibraryTab.SONGS -> {
                if (filteredTracks.isEmpty()) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text(
                            "No songs in library",
                            color = if (isDark) TextDarkSecondary else TextLightSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(LiquidSpacing.sm)
                    ) {
                        items(filteredTracks, key = { it.id }) { track ->
                            var showTrackMenu by remember { mutableStateOf(false) }

                            LiquidGlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onPlayTrack(track, filteredTracks) },
                                shape = RoundedCornerShape(LiquidRadii.md)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = LiquidSpacing.md, vertical = LiquidSpacing.sm),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Artwork
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

                                    // Duration Pill
                                    val min = (track.durationMs / 1000) / 60
                                    val sec = (track.durationMs / 1000) % 60
                                    LiquidGlassPill {
                                        Text(
                                            text = String.format("%d:%02d", min, sec),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = if (isDark) TextDarkSecondary else TextLightSecondary
                                        )
                                    }

                                    // Track Context Menu
                                    Box {
                                        IconButton(onClick = { showTrackMenu = true }, modifier = Modifier.size(36.dp)) {
                                            Icon(
                                                Icons.Default.MoreVert,
                                                contentDescription = "Options",
                                                tint = if (isDark) Color.White.copy(0.7f) else Color.Black.copy(0.7f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showTrackMenu,
                                            onDismissRequest = { showTrackMenu = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Play Now") },
                                                onClick = {
                                                    onPlayTrack(track, filteredTracks)
                                                    showTrackMenu = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Edit Metadata") },
                                                onClick = {
                                                    editingTrack = track
                                                    showTrackMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LibraryTab.ALBUMS -> {
                if (albums.isEmpty()) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("No albums found", color = if (isDark) TextDarkSecondary else TextLightSecondary)
                    }
                } else if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(bottom = 140.dp),
                        horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(LiquidSpacing.md)
                    ) {
                        items(albums, key = { it.id }) { album ->
                            LiquidGlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onAlbumClick(album) },
                                shape = RoundedCornerShape(LiquidRadii.lg)
                            ) {
                                Column(modifier = Modifier.padding(LiquidSpacing.sm)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(LiquidRadii.md))
                                            .background(if (isDark) Color(0x33FFFFFF) else Color(0x1A000000)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (album.artworkUri != null) {
                                            AsyncImage(
                                                model = album.artworkUri,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Album,
                                                contentDescription = null,
                                                tint = if (isDark) Color.White.copy(0.6f) else Color.Black.copy(0.6f),
                                                modifier = Modifier.size(48.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(LiquidSpacing.xs + 2.dp))
                                    Text(
                                        text = album.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isDark) TextDarkPrimary else TextLightPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = album.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDark) TextDarkSecondary else TextLightSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(LiquidSpacing.sm)
                    ) {
                        items(albums, key = { it.id }) { album ->
                            LiquidGlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onAlbumClick(album) },
                                shape = RoundedCornerShape(LiquidRadii.md)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(LiquidSpacing.md),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(LiquidRadii.sm))
                                            .background(if (isDark) Color(0x33FFFFFF) else Color(0x1A000000)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (album.artworkUri != null) {
                                            AsyncImage(
                                                model = album.artworkUri,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(Icons.Default.Album, contentDescription = null, tint = Color.White)
                                        }
                                    }
                                    Spacer(Modifier.width(LiquidSpacing.md))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            album.title,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) TextDarkPrimary else TextLightPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            album.artist,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isDark) TextDarkSecondary else TextLightSecondary,
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        "${album.trackCount} tracks",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isDark) TextDarkSecondary else TextLightSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            LibraryTab.ARTISTS -> {
                if (artists.isEmpty()) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("No artists found", color = if (isDark) TextDarkSecondary else TextLightSecondary)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(LiquidSpacing.sm)
                    ) {
                        items(artists, key = { it.id }) { artist ->
                            LiquidGlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onArtistClick(artist) },
                                shape = RoundedCornerShape(LiquidRadii.md)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(LiquidSpacing.md),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(LiquidAccentPrimary.copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = LiquidAccentPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(LiquidSpacing.md))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            artist.name,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) TextDarkPrimary else TextLightPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            "${artist.trackCount} tracks • ${artist.albumCount} albums",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isDark) TextDarkSecondary else TextLightSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LibraryTab.GENRES -> {
                if (genres.isEmpty()) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("No genres categorized", color = if (isDark) TextDarkSecondary else TextLightSecondary)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(LiquidSpacing.sm)
                    ) {
                        items(genres) { genre ->
                            val isSelected = selectedGenre.equals(genre, ignoreCase = true)
                            LiquidGlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    selectedGenre = if (isSelected) null else genre
                                    selectedTab = LibraryTab.SONGS
                                },
                                tintColor = if (isSelected) LiquidAccentPrimary.copy(alpha = 0.22f) else null,
                                shape = RoundedCornerShape(LiquidRadii.md)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(LiquidSpacing.md),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Category,
                                        contentDescription = null,
                                        tint = LiquidAccentPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(LiquidSpacing.md))
                                    Text(
                                        genre,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) TextDarkPrimary else TextLightPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Text("Active Filter", color = LiquidAccentPrimary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Metadata Dialog
    editingTrack?.let { track ->
        var editTitle by remember { mutableStateOf(track.title) }
        var editArtist by remember { mutableStateOf(track.artist) }
        var editAlbum by remember { mutableStateOf(track.album) }
        var editGenre by remember { mutableStateOf(track.genre ?: "") }

        AlertDialog(
            onDismissRequest = { editingTrack = null },
            title = { Text("Edit Track Metadata", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(LiquidSpacing.sm)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editArtist,
                        onValueChange = { editArtist = it },
                        label = { Text("Artist") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAlbum,
                        onValueChange = { editAlbum = it },
                        label = { Text("Album") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editGenre,
                        onValueChange = { editGenre = it },
                        label = { Text("Genre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = track.copy(
                            title = editTitle,
                            artist = editArtist,
                            album = editAlbum,
                            genre = editGenre.ifEmpty { null }
                        )
                        onSaveTrackMetadata(updated)
                        editingTrack = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidAccentPrimary)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingTrack = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
