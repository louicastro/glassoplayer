package com.liquidglass.music.ui.home

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.liquidglass.music.data.model.AlbumEntity
import com.liquidglass.music.data.model.PlaylistEntity
import com.liquidglass.music.data.model.TrackEntity
import com.liquidglass.music.ui.components.LiquidGlassBox
import com.liquidglass.music.ui.components.LiquidGlassCard
import com.liquidglass.music.ui.components.LiquidGlassPill
import com.liquidglass.music.ui.theme.LiquidAccentPink
import com.liquidglass.music.ui.theme.LiquidAccentPrimary
import com.liquidglass.music.ui.theme.LiquidElevation
import com.liquidglass.music.ui.theme.LiquidRadii
import com.liquidglass.music.ui.theme.LiquidSpacing
import com.liquidglass.music.ui.theme.TextDarkPrimary
import com.liquidglass.music.ui.theme.TextDarkSecondary
import com.liquidglass.music.ui.theme.TextLightPrimary
import com.liquidglass.music.ui.theme.TextLightSecondary

@Composable
fun HomeScreen(
    recentlyPlayed: List<TrackEntity>,
    frequentlyPlayed: List<TrackEntity>,
    recentlyAdded: List<TrackEntity>,
    favorites: List<TrackEntity>,
    albums: List<AlbumEntity>,
    playlists: List<PlaylistEntity> = emptyList(),
    onPlayTrack: (TrackEntity, List<TrackEntity>) -> Unit,
    onAlbumClick: (AlbumEntity) -> Unit = {},
    onPlaylistClick: (PlaylistEntity) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    onScanLibrary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val isLibraryEmpty = recentlyPlayed.isEmpty() && recentlyAdded.isEmpty() && favorites.isEmpty() && albums.isEmpty()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = LiquidSpacing.lg, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(LiquidSpacing.xxl)
    ) {
        // Top Glass Navigation & Header Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LiquidSpacing.xl),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Glassoplayer",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.6).sp
                        ),
                        color = if (isDark) TextDarkPrimary else TextLightPrimary
                    )
                    Text(
                        text = "Curated Offline Audio",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (isDark) TextDarkSecondary else TextLightSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Library Access Button
                    IconButton(
                        onClick = onNavigateToLibrary,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x26FFFFFF) else Color(0x14000000))
                    ) {
                        Icon(
                            imageVector = Icons.Default.LibraryMusic,
                            contentDescription = "Library",
                            tint = if (isDark) Color.White else Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(Modifier.width(LiquidSpacing.sm))

                    // Settings Button
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x26FFFFFF) else Color(0x14000000))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (isDark) Color.White else Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        if (isLibraryEmpty) {
            // Elegant Liquid Empty State
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LiquidSpacing.xl, vertical = LiquidSpacing.xxxl),
                    contentAlignment = Alignment.Center
                ) {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(LiquidRadii.xxl)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(LiquidSpacing.xxxl),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(LiquidAccentPrimary.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = LiquidAccentPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(Modifier.height(LiquidSpacing.lg))
                            Text(
                                text = "Your Library is Empty",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.3).sp
                                ),
                                color = if (isDark) TextDarkPrimary else TextLightPrimary
                            )
                            Spacer(Modifier.height(LiquidSpacing.xs))
                            Text(
                                text = "No local audio files detected yet. Ensure your device has audio files or rescan to index them.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDark) TextDarkSecondary else TextLightSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(LiquidSpacing.xl))
                            Button(
                                onClick = onScanLibrary,
                                colors = ButtonDefaults.buttonColors(containerColor = LiquidAccentPrimary),
                                shape = RoundedCornerShape(LiquidRadii.pill),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(LiquidSpacing.xs))
                                Text("Scan Device Audio", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // Prominent Hero: Recently Played (Curated Card)
            if (recentlyPlayed.isNotEmpty()) {
                item {
                    val heroTrack = recentlyPlayed.first()
                    Column(modifier = Modifier.padding(horizontal = LiquidSpacing.xl)) {
                        SectionHeader(title = "Jump Back In", icon = Icons.Default.History)
                        Spacer(Modifier.height(LiquidSpacing.sm))

                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onPlayTrack(heroTrack, recentlyPlayed) },
                            shape = RoundedCornerShape(LiquidRadii.xxl),
                            elevation = LiquidElevation.medium
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(LiquidSpacing.md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Large Artwork
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .shadow(8.dp, RoundedCornerShape(LiquidRadii.md))
                                        .clip(RoundedCornerShape(LiquidRadii.md))
                                        .background(if (isDark) Color(0x33FFFFFF) else Color(0x1A000000)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (heroTrack.artworkUri != null) {
                                        AsyncImage(
                                            model = heroTrack.artworkUri,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = if (isDark) Color.White.copy(0.6f) else Color.Black.copy(0.6f),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.width(LiquidSpacing.md))

                                Column(modifier = Modifier.weight(1f)) {
                                    LiquidGlassPill(
                                        tintColor = LiquidAccentPrimary.copy(alpha = 0.16f)
                                    ) {
                                        Text(
                                            text = "LAST PLAYED",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = LiquidAccentPrimary
                                        )
                                    }
                                    Spacer(Modifier.height(LiquidSpacing.xs))
                                    Text(
                                        text = heroTrack.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        color = if (isDark) TextDarkPrimary else TextLightPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${heroTrack.artist} — ${heroTrack.album}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDark) TextDarkSecondary else TextLightSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(Modifier.width(LiquidSpacing.sm))

                                // Play Button Orb
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(LiquidAccentPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Favorites Row
            if (favorites.isNotEmpty()) {
                item {
                    Column {
                        Box(modifier = Modifier.padding(horizontal = LiquidSpacing.xl)) {
                            SectionHeader(title = "Favorites", icon = Icons.Default.Favorite, tint = LiquidAccentPink)
                        }
                        Spacer(Modifier.height(LiquidSpacing.sm))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = LiquidSpacing.xl),
                            horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.md)
                        ) {
                            items(favorites) { track ->
                                CompactTrackCard(track = track, onClick = { onPlayTrack(track, favorites) })
                            }
                        }
                    }
                }
            }

            // Albums Carousel
            if (albums.isNotEmpty()) {
                item {
                    Column {
                        Box(modifier = Modifier.padding(horizontal = LiquidSpacing.xl)) {
                            SectionHeader(title = "Albums", icon = Icons.Default.Album)
                        }
                        Spacer(Modifier.height(LiquidSpacing.sm))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = LiquidSpacing.xl),
                            horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.md)
                        ) {
                            items(albums) { album ->
                                AlbumCard(album = album, onClick = { onAlbumClick(album) })
                            }
                        }
                    }
                }
            }

            // Playlists Carousel
            if (playlists.isNotEmpty()) {
                item {
                    Column {
                        Box(modifier = Modifier.padding(horizontal = LiquidSpacing.xl)) {
                            SectionHeader(title = "Playlists", icon = Icons.AutoMirrored.Filled.PlaylistPlay)
                        }
                        Spacer(Modifier.height(LiquidSpacing.sm))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = LiquidSpacing.xl),
                            horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.md)
                        ) {
                            items(playlists) { playlist ->
                                PlaylistCard(playlist = playlist, onClick = { onPlaylistClick(playlist) })
                            }
                        }
                    }
                }
            }

            // Frequently Played / Top Tracks
            if (frequentlyPlayed.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = LiquidSpacing.xl)) {
                        SectionHeader(title = "Frequently Played", icon = Icons.Default.MusicNote)
                        Spacer(Modifier.height(LiquidSpacing.sm))
                        Column(verticalArrangement = Arrangement.spacedBy(LiquidSpacing.sm)) {
                            frequentlyPlayed.take(5).forEachIndexed { index, track ->
                                TrackRowItem(
                                    track = track,
                                    rank = index + 1,
                                    onClick = { onPlayTrack(track, frequentlyPlayed) }
                                )
                            }
                        }
                    }
                }
            }

            // Recently Added
            if (recentlyAdded.isNotEmpty()) {
                item {
                    Column {
                        Box(modifier = Modifier.padding(horizontal = LiquidSpacing.xl)) {
                            SectionHeader(title = "Recently Added", icon = Icons.Default.MusicNote)
                        }
                        Spacer(Modifier.height(LiquidSpacing.sm))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = LiquidSpacing.xl),
                            horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.md)
                        ) {
                            items(recentlyAdded) { track ->
                                TrackCard(track = track, onClick = { onPlayTrack(track, recentlyAdded) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    tint: Color = LiquidAccentPrimary
) {
    val isDark = isSystemInDarkTheme()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LiquidSpacing.xs + 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
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

@Composable
private fun CompactTrackCard(track: TrackEntity, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    LiquidGlassCard(
        modifier = Modifier.width(138.dp),
        onClick = onClick,
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
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(Modifier.height(LiquidSpacing.xs + 2.dp))

            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = if (isDark) TextDarkPrimary else TextLightPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                color = if (isDark) TextDarkSecondary else TextLightSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TrackCard(track: TrackEntity, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    LiquidGlassCard(
        modifier = Modifier.width(152.dp),
        onClick = onClick,
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
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(Modifier.height(LiquidSpacing.sm))

            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp
                ),
                color = if (isDark) TextDarkPrimary else TextLightPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = if (isDark) TextDarkSecondary else TextLightSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AlbumCard(album: AlbumEntity, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    LiquidGlassCard(
        modifier = Modifier.width(148.dp),
        onClick = onClick,
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
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(Modifier.height(LiquidSpacing.sm))

            Text(
                text = album.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                ),
                color = if (isDark) TextDarkPrimary else TextLightPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = album.artist,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = if (isDark) TextDarkSecondary else TextLightSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PlaylistCard(playlist: PlaylistEntity, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    LiquidGlassCard(
        modifier = Modifier.width(148.dp),
        onClick = onClick,
        shape = RoundedCornerShape(LiquidRadii.lg)
    ) {
        Column(modifier = Modifier.padding(LiquidSpacing.sm)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(LiquidRadii.md))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                LiquidAccentPrimary.copy(alpha = 0.8f),
                                LiquidAccentPink.copy(alpha = 0.8f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(LiquidSpacing.sm))

            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                ),
                color = if (isDark) TextDarkPrimary else TextLightPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Offline Playlist",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = if (isDark) TextDarkSecondary else TextLightSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TrackRowItem(
    track: TrackEntity,
    rank: Int,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(LiquidRadii.md)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LiquidSpacing.md, vertical = LiquidSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = LiquidAccentPrimary,
                modifier = Modifier.width(24.dp)
            )

            Box(
                modifier = Modifier
                    .size(44.dp)
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
                        modifier = Modifier.size(22.dp)
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
                    text = "${track.artist} • ${track.playCount} plays",
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
