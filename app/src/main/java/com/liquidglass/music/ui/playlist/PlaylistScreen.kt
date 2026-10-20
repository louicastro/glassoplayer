package com.liquidglass.music.ui.playlist

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidglass.music.data.model.PlaylistEntity
import com.liquidglass.music.ui.components.LiquidGlassBox
import com.liquidglass.music.ui.components.LiquidGlassCard
import com.liquidglass.music.ui.components.LiquidGlassPill
import com.liquidglass.music.ui.theme.LiquidAccentPink
import com.liquidglass.music.ui.theme.LiquidAccentPrimary
import com.liquidglass.music.ui.theme.LiquidRadii
import com.liquidglass.music.ui.theme.LiquidSpacing
import com.liquidglass.music.ui.theme.TextDarkPrimary
import com.liquidglass.music.ui.theme.TextDarkSecondary
import com.liquidglass.music.ui.theme.TextLightPrimary
import com.liquidglass.music.ui.theme.TextLightSecondary

@Composable
fun PlaylistScreen(
    playlists: List<PlaylistEntity>,
    onCreatePlaylist: (String) -> Unit,
    onRenamePlaylist: (Long, String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onSelectPlaylist: (PlaylistEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    var showCreateDialog by remember { mutableStateOf(false) }
    var playlistToRename by remember { mutableStateOf<PlaylistEntity?>(null) }
    var playlistToDelete by remember { mutableStateOf<PlaylistEntity?>(null) }
    var newPlaylistName by remember { mutableStateOf("") }
    var renameValue by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = LiquidSpacing.xl)
        ) {
            Spacer(Modifier.height(LiquidSpacing.lg))

            // Header: Title and Create Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Playlists",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.6).sp
                        ),
                        color = if (isDark) TextDarkPrimary else TextLightPrimary
                    )
                    Text(
                        text = "${playlists.size} playlists saved",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) TextDarkSecondary else TextLightSecondary
                    )
                }

                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(LiquidAccentPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Playlist",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(LiquidSpacing.lg))

            if (playlists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(LiquidAccentPrimary.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                                    contentDescription = null,
                                    tint = LiquidAccentPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(Modifier.height(LiquidSpacing.md))
                            Text(
                                text = "No Playlists Yet",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) TextDarkPrimary else TextLightPrimary
                            )
                            Spacer(Modifier.height(LiquidSpacing.xs))
                            Text(
                                text = "Create playlists to group and organize your offline tracks.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDark) TextDarkSecondary else TextLightSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(LiquidSpacing.xl))
                            Button(
                                onClick = { showCreateDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = LiquidAccentPrimary),
                                shape = RoundedCornerShape(LiquidRadii.pill),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(LiquidSpacing.xs))
                                Text("Create First Playlist", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(LiquidSpacing.sm)
                ) {
                    items(playlists, key = { it.id }) { pl ->
                        var showMenu by remember { mutableStateOf(false) }

                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onSelectPlaylist(pl) },
                            shape = RoundedCornerShape(LiquidRadii.md)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = LiquidSpacing.md, vertical = LiquidSpacing.sm + 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(LiquidRadii.sm))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(
                                                        LiquidAccentPrimary.copy(alpha = 0.85f),
                                                        LiquidAccentPink.copy(alpha = 0.85f)
                                                    )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    Spacer(Modifier.width(LiquidSpacing.md))

                                    Column {
                                        Text(
                                            text = pl.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            ),
                                            color = if (isDark) TextDarkPrimary else TextLightPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = "Offline Playlist",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isDark) TextDarkSecondary else TextLightSecondary
                                        )
                                    }
                                }

                                Box {
                                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = "Options",
                                            tint = if (isDark) Color.White.copy(0.7f) else Color.Black.copy(0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Rename") },
                                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                            onClick = {
                                                renameValue = pl.name
                                                playlistToRename = pl
                                                showMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete", color = Color(0xFFFF4E6E)) },
                                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF4E6E)) },
                                            onClick = {
                                                playlistToDelete = pl
                                                showMenu = false
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

        // Create Playlist Dialog
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("New Playlist", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        placeholder = { Text("Playlist name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPlaylistName.isNotBlank()) {
                                onCreatePlaylist(newPlaylistName.trim())
                                newPlaylistName = ""
                                showCreateDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidAccentPrimary)
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Rename Playlist Dialog
        playlistToRename?.let { pl ->
            AlertDialog(
                onDismissRequest = { playlistToRename = null },
                title = { Text("Rename Playlist", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = renameValue,
                        onValueChange = { renameValue = it },
                        placeholder = { Text("New name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (renameValue.isNotBlank()) {
                                onRenamePlaylist(pl.id, renameValue.trim())
                                playlistToRename = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidAccentPrimary)
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { playlistToRename = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        playlistToDelete?.let { pl ->
            AlertDialog(
                onDismissRequest = { playlistToDelete = null },
                title = { Text("Delete Playlist?", fontWeight = FontWeight.Bold) },
                text = {
                    Text("Are you sure you want to delete \"${pl.name}\"? Your songs will remain safe in your library.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeletePlaylist(pl.id)
                            playlistToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4E6E))
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { playlistToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
