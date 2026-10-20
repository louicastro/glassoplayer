package com.liquidglass.music

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import com.liquidglass.music.data.model.AlbumEntity
import com.liquidglass.music.data.model.PlaylistEntity
import com.liquidglass.music.data.model.TrackEntity
import com.liquidglass.music.data.scanner.LibraryScanner
import com.liquidglass.music.lyrics.LyricsEditorSheet
import com.liquidglass.music.lyrics.LyricsScreen
import com.liquidglass.music.lyrics.LyricsViewModel
import com.liquidglass.music.playback.AudioEffectsController
import com.liquidglass.music.playback.RepeatMode
import com.liquidglass.music.service.PlaybackService
import com.liquidglass.music.service.SleepTimer
import com.liquidglass.music.service.SleepTimerState
import com.liquidglass.music.ui.components.LiquidGlassBox
import com.liquidglass.music.ui.home.HomeScreen
import com.liquidglass.music.ui.library.LibraryScreen
import com.liquidglass.music.ui.player.MiniPlayer
import com.liquidglass.music.ui.player.NowPlayingSheet
import com.liquidglass.music.ui.player.PlaybackViewModel
import com.liquidglass.music.ui.playlist.PlaylistScreen
import com.liquidglass.music.ui.search.SearchScreen
import com.liquidglass.music.ui.settings.EqualizerScreen
import com.liquidglass.music.ui.settings.SettingsScreen
import com.liquidglass.music.ui.theme.LiquidAccentPrimary
import com.liquidglass.music.ui.theme.LiquidMusicTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

enum class NavigationItem(val label: String, val icon: ImageVector) {
    LISTEN_NOW("Listen Now", Icons.Default.Home),
    LIBRARY("Library", Icons.Default.LibraryMusic),
    SEARCH("Search", Icons.Default.Search),
    PLAYLISTS("Playlists", Icons.Default.PlaylistPlay),
    SETTINGS("Settings", Icons.Default.Settings)
}

@UnstableApi
class MainActivity : ComponentActivity() {

    private val playbackViewModel: PlaybackViewModel by viewModels()
    private val lyricsViewModel: LyricsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Ensure playback foreground service is started
        val serviceIntent = Intent(this, PlaybackService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            // Service startup fallback
        }

        setContent {
            LiquidMusicTheme {
                MainAppScreen(
                    playbackViewModel = playbackViewModel,
                    lyricsViewModel = lyricsViewModel
                )
            }
        }
    }
}

@UnstableApi
@Composable
fun MainAppScreen(
    playbackViewModel: PlaybackViewModel,
    lyricsViewModel: LyricsViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = (context.applicationContext as LiquidMusicApplication).database

    // Permissions State
    var hasAudioPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasAudioPermission = perms.values.any { it }
    }

    // Trigger initial scan if permission granted
    LaunchedEffect(hasAudioPermission) {
        if (hasAudioPermission) {
            coroutineScope.launch(Dispatchers.IO) {
                LibraryScanner(context, database).scanLibrary().collectLatest {}
            }
        }
    }

    // UI States
    var currentNav by remember { mutableStateOf(NavigationItem.LISTEN_NOW) }
    var isNowPlayingExpanded by remember { mutableStateOf(false) }
    var isLyricsScreenOpen by remember { mutableStateOf(false) }
    var isEqualizerScreenOpen by remember { mutableStateOf(false) }

    // Database reactive streams
    val allTracks by database.trackDao().getAllAvailableTracks().collectAsState(initial = emptyList())
    val recentlyAdded by database.trackDao().getRecentlyAddedTracks().collectAsState(initial = emptyList())
    val recentlyPlayed by database.trackDao().getRecentlyPlayedTracks().collectAsState(initial = emptyList())
    val frequentlyPlayed by database.trackDao().getFrequentlyPlayedTracks().collectAsState(initial = emptyList())
    val favorites by database.trackDao().getFavoriteTracks().collectAsState(initial = emptyList())
    val albums by database.albumDao().getAllAlbums().collectAsState(initial = emptyList())
    val artists by database.artistDao().getAllArtists().collectAsState(initial = emptyList())
    val genres by database.trackDao().getAllGenres().collectAsState(initial = emptyList())
    val playlists by database.playlistDao().getAllPlaylists().collectAsState(initial = emptyList())

    // Playback state
    val currentTrack by playbackViewModel.currentTrack.collectAsState()
    val isPlaying by playbackViewModel.isPlaying.collectAsState()
    val currentPosMs by playbackViewModel.currentPositionMs.collectAsState()
    val durationMs by playbackViewModel.durationMs.collectAsState()
    val isShuffle by playbackViewModel.isShuffleEnabled.collectAsState()
    val repeatMode by playbackViewModel.repeatMode.collectAsState()
    val playbackSpeed by playbackViewModel.playbackSpeed.collectAsState()
    val paletteColors by playbackViewModel.paletteColors.collectAsState()

    // Lyrics state
    val lyricUiState by lyricsViewModel.lyricState.collectAsState()
    val isEditorActive by lyricsViewModel.isEditorActive.collectAsState()
    val editorLines by lyricsViewModel.editorLines.collectAsState()

    // Update lyrics state when track or position changes
    LaunchedEffect(currentTrack) {
        lyricsViewModel.loadLyricsForTrack(currentTrack)
    }

    LaunchedEffect(currentPosMs) {
        lyricsViewModel.updatePlaybackPosition(currentPosMs)
    }

    val audioEffectsController = remember { AudioEffectsController() }
    val effectsState by audioEffectsController.effectsState.collectAsState()
    var sleepTimerState by remember { mutableStateOf(SleepTimerState()) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                // Floating MiniPlayer (appears when track is loaded and full player is collapsed)
                if (currentTrack != null && !isNowPlayingExpanded && !isLyricsScreenOpen) {
                    MiniPlayer(
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        currentPositionMs = currentPosMs,
                        durationMs = durationMs,
                        onTogglePlayPause = { playbackViewModel.togglePlayPause() },
                        onSkipNext = { playbackViewModel.skipToNext() },
                        onExpand = { isNowPlayingExpanded = true }
                    )
                }

                // Floating Liquid Glass Navigation Bar
                if (!isNowPlayingExpanded && !isLyricsScreenOpen) {
                    LiquidGlassBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(24.dp),
                        blurRadius = 24.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NavigationItem.entries.forEach { item ->
                                val isSelected = currentNav == item
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { currentNav = item }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = if (isSelected) LiquidAccentPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) LiquidAccentPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!hasAudioPermission) {
                // Clear Permission Explanation Screen
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = LiquidAccentPrimary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Storage Permission Required",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Liquid Music Player is 100% offline and requires access to your device's audio files to scan and play your local library.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground.copy(0.7f)),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = {
                                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                                }
                                permissionLauncher.launch(permissions)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LiquidAccentPrimary)
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            } else {
                when (currentNav) {
                    NavigationItem.LISTEN_NOW -> {
                        HomeScreen(
                            recentlyPlayed = recentlyPlayed,
                            frequentlyPlayed = frequentlyPlayed,
                            recentlyAdded = recentlyAdded,
                            favorites = favorites,
                            albums = albums,
                            onPlayTrack = { track, list -> playbackViewModel.playTrack(track, list) },
                            onAlbumClick = { /* Handle album click */ }
                        )
                    }

                    NavigationItem.LIBRARY -> {
                        LibraryScreen(
                            tracks = allTracks,
                            albums = albums,
                            artists = artists,
                            genres = genres,
                            onPlayTrack = { track, list -> playbackViewModel.playTrack(track, list) },
                            onAlbumClick = {},
                            onArtistClick = {},
                            onSaveTrackMetadata = { updatedTrack ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    database.trackDao().updateTrack(updatedTrack)
                                }
                            }
                        )
                    }

                    NavigationItem.SEARCH -> {
                        SearchScreen(
                            tracks = allTracks,
                            onPlayTrack = { track, list -> playbackViewModel.playTrack(track, list) }
                        )
                    }

                    NavigationItem.PLAYLISTS -> {
                        PlaylistScreen(
                            playlists = playlists,
                            onCreatePlaylist = { name ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    database.playlistDao().insertPlaylist(PlaylistEntity(name = name))
                                }
                            },
                            onRenamePlaylist = { id, newName ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    database.playlistDao().renamePlaylist(id, newName)
                                }
                            },
                            onDeletePlaylist = { id ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    database.playlistDao().deletePlaylist(id)
                                }
                            },
                            onSelectPlaylist = {}
                        )
                    }

                    NavigationItem.SETTINGS -> {
                        SettingsScreen(
                            sleepTimerState = sleepTimerState,
                            onStartSleepTimer = { mins ->
                                sleepTimerState = SleepTimerState(isActive = true, remainingSeconds = mins * 60L)
                            },
                            onStartEndOfTrackSleep = {
                                sleepTimerState = SleepTimerState(isActive = true, isEndOfTrack = true)
                            },
                            onCancelSleepTimer = {
                                sleepTimerState = SleepTimerState(isActive = false)
                            },
                            onOpenEqualizer = { isEqualizerScreenOpen = true },
                            onClearArtworkCache = {},
                            onClearLyricCache = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    database.lyricDao().clearAllLyrics()
                                }
                            },
                            onRebuildLibraryIndex = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    LibraryScanner(context, database).scanLibrary().collectLatest {}
                                }
                            }
                        )
                    }
                }
            }

            // Full Now Playing Sheet Overlay
            AnimatedVisibility(
                visible = isNowPlayingExpanded,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NowPlayingSheet(
                    track = currentTrack,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPosMs,
                    durationMs = durationMs,
                    isShuffle = isShuffle,
                    repeatMode = repeatMode,
                    playbackSpeed = playbackSpeed,
                    paletteColors = paletteColors,
                    onTogglePlayPause = { playbackViewModel.togglePlayPause() },
                    onSeekTo = { pos -> playbackViewModel.seekTo(pos) },
                    onSkipNext = { playbackViewModel.skipToNext() },
                    onSkipPrevious = { playbackViewModel.skipToPrevious() },
                    onToggleShuffle = { /* toggle shuffle */ },
                    onCycleRepeat = { /* cycle repeat */ },
                    onToggleFavorite = { playbackViewModel.toggleFavorite() },
                    onSetSpeed = { sp -> playbackViewModel.setSpeed(sp) },
                    onOpenLyrics = {
                        isNowPlayingExpanded = false
                        isLyricsScreenOpen = true
                    },
                    onOpenQueue = { /* open queue */ },
                    onDismiss = { isNowPlayingExpanded = false }
                )
            }

            // Full Screen Lyrics Overlay
            AnimatedVisibility(
                visible = isLyricsScreenOpen,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                LyricsScreen(
                    track = currentTrack,
                    uiState = lyricUiState,
                    currentPlaybackPositionMs = currentPosMs,
                    paletteColors = paletteColors,
                    onSeekToTimestamp = { ts -> playbackViewModel.seekTo(ts) },
                    onAdjustOffset = { delta -> lyricsViewModel.adjustOffset(delta) },
                    onToggleInstrumental = { lyricsViewModel.toggleInstrumental() },
                    onStartEditor = { initial -> lyricsViewModel.startEditor(initial) },
                    onClose = { isLyricsScreenOpen = false }
                )
            }

            // Tap-to-Stamp Editor Sheet
            AnimatedVisibility(
                visible = isEditorActive,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                LyricsEditorSheet(
                    editorLines = editorLines,
                    currentPlaybackPositionMs = currentPosMs,
                    onStampLine = { idx, pos -> lyricsViewModel.stampLine(idx, pos) },
                    onSaveEdits = { lyricsViewModel.saveEditorEdits() },
                    onCancel = { lyricsViewModel.cancelEditor() }
                )
            }

            // Equalizer Screen Overlay
            AnimatedVisibility(
                visible = isEqualizerScreenOpen,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                EqualizerScreen(
                    effectsState = effectsState,
                    onToggleEnabled = { audioEffectsController.setEnabled(it) },
                    onApplyPreset = { audioEffectsController.applyPreset(it) },
                    onBandChange = { band, lvl -> audioEffectsController.setBandLevel(band, lvl) },
                    onBassBoostChange = { audioEffectsController.setBassBoostStrength(it) },
                    onVirtualizerChange = { audioEffectsController.setVirtualizerStrength(it) },
                    onLoudnessGainChange = { audioEffectsController.setLoudnessGain(it) },
                    onBack = { isEqualizerScreenOpen = false }
                )
            }
        }
    }
}
