package com.liquidglass.music.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Sort
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
import com.liquidglass.music.ui.theme.LiquidAccentPrimary

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
    onAlbumClick: (AlbumEntity) -> Unit,
    onArtistClick: (ArtistEntity) -> Unit,
    onSaveTrackMetadata: (TrackEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(LibraryTab.SONGS) }
    var sortOrder by remember { mutableStateOf(SortOrder.TITLE) }
    var showSortMenu by remember { mutableStateOf(false) }
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
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Title and Sort Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Library",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
            )

            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort", tint = MaterialTheme.colorScheme.onBackground)
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SortOrder.entries.forEach { order ->
                        DropdownMenuItem(
                            text = { Text(order.displayName) },
                            onClick = {
                                sortOrder = order
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Category Tabs: Songs, Albums, Artists, Genres
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(LibraryTab.entries) { tab ->
                FilterChip(
                    selected = selectedTab == tab,
                    onClick = {
                        selectedTab = tab
                        selectedGenre = null
                    },
                    label = { Text(tab.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LiquidAccentPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Genre Sub-Filters (when in Genres or Songs mode)
        if (genres.isNotEmpty() && selectedTab == LibraryTab.SONGS) {
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = selectedGenre == null,
                        onClick = { selectedGenre = null },
                        label = { Text("All Genres", fontSize = 12.sp) }
                    )
                }
                items(genres) { g ->
                    FilterChip(
                        selected = selectedGenre == g,
                        onClick = { selectedGenre = if (selectedGenre == g) null else g },
                        label = { Text(g, fontSize = 12.sp) }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Tab Content
        when (selectedTab) {
            LibraryTab.SONGS -> {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTracks) { track ->
                        LibraryTrackRow(
                            track = track,
                            onClick = { onPlayTrack(track, filteredTracks) },
                            onEditClick = { editingTrack = track }
                        )
                    }
                }
            }

            LibraryTab.ALBUMS -> {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(albums) { album ->
                        LiquidGlassBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAlbumClick(album) },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (album.artworkUri != null) {
                                    AsyncImage(
                                        model = album.artworkUri,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0x22FFFFFF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Album, contentDescription = null, tint = Color.White)
                                    }
                                }
                                Spacer(Modifier.width(14.dp))
                                Column {
                                    Text(album.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("${album.artist} • ${album.trackCount} tracks", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)))
                                }
                            }
                        }
                    }
                }
            }

            LibraryTab.ARTISTS -> {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(artists) { artist ->
                        LiquidGlassBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onArtistClick(artist) },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(LiquidAccentPrimary.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                                }
                                Spacer(Modifier.width(14.dp))
                                Column {
                                    Text(artist.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("${artist.trackCount} tracks • ${artist.albumCount} albums", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)))
                                }
                            }
                        }
                    }
                }
            }

            LibraryTab.GENRES -> {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(genres) { genre ->
                        val genreTrackCount = tracks.count { it.genre.equals(genre, ignoreCase = true) }
                        LiquidGlassBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedGenre = genre
                                    selectedTab = LibraryTab.SONGS
                                },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(genre, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("$genreTrackCount tracks", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)))
                            }
                        }
                    }
                }
            }
        }
    }

    // Metadata Editing Dialog (Stored in App Database without modifying original file)
    editingTrack?.let { track ->
        var editTitle by remember { mutableStateOf(track.title) }
        var editArtist by remember { mutableStateOf(track.artist) }
        var editAlbum by remember { mutableStateOf(track.album) }
        var editGenre by remember { mutableStateOf(track.genre ?: "") }

        AlertDialog(
            onDismissRequest = { editingTrack = null },
            title = { Text("Edit Metadata (App Database)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Edits are saved to your offline database and do not alter the source file unless explicitly requested.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                    OutlinedTextField(value = editTitle, onValueChange = { editTitle = it }, label = { Text("Title") })
                    OutlinedTextField(value = editArtist, onValueChange = { editArtist = it }, label = { Text("Artist") })
                    OutlinedTextField(value = editAlbum, onValueChange = { editAlbum = it }, label = { Text("Album") })
                    OutlinedTextField(value = editGenre, onValueChange = { editGenre = it }, label = { Text("Genre") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = track.copy(
                            title = editTitle.trim(),
                            artist = editArtist.trim(),
                            album = editAlbum.trim(),
                            genre = editGenre.trim().ifEmpty { null }
                        )
                        onSaveTrackMetadata(updated)
                        editingTrack = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidAccentPrimary)
                ) {
                    Text("Save")
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

@Composable
fun LibraryTrackRow(
    track: TrackEntity,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    LiquidGlassBox(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (track.artworkUri != null) {
                AsyncImage(
                    model = track.artworkUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x22FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White.copy(0.6f))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} • ${track.album}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Metadata", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            }
        }
    }
}
