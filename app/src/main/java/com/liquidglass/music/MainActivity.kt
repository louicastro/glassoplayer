package com.liquidglass.music

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.liquidglass.music.data.model.PlaylistEntity
import com.liquidglass.music.data.model.TrackEntity
import com.liquidglass.music.data.scanner.LibraryScanner
import com.liquidglass.music.lyrics.LyricsEditorSheet
import com.liquidglass.music.lyrics.LyricsScreen
import com.liquidglass.music.lyrics.LyricsViewModel
import com.liquidglass.music.playback.AudioEffectsController
import com.liquidglass.music.service.PlaybackService
import com.liquidglass.music.service.SleepTimerState
import com.liquidglass.music.ui.home.HomeScreen
import com.liquidglass.music.ui.library.LibraryScreen
import com.liquidglass.music.ui.navigation.LiquidNavigationBar
import com.liquidglass.music.ui.navigation.NavigationDestination
import com.liquidglass.music.ui.player.MiniPlayer
import com.liquidglass.music.ui.player.NowPlayingSheet
import com.liquidglass.music.ui.player.PlaybackViewModel
import com.liquidglass.music.ui.player.QueueSheet
import com.liquidglass.music.ui.playlist.PlaylistScreen
import com.liquidglass.music.ui.search.SearchScreen
import com.liquidglass.music.ui.settings.EqualizerScreen
import com.liquidglass.music.ui.settings.SettingsScreen
import com.liquidglass.music.ui.theme.LiquidAccentPrimary
import com.liquidglass.music.ui.theme.LiquidMusicTheme
import com.liquidglass.music.ui.theme.LiquidRadii
import com.liquidglass.music.ui.theme.LiquidSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

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

        handleIncomingIntent(intent)

        setContent {
            LiquidMusicTheme {
                MainAppScreen(
                    playbackViewModel = playbackViewModel,
                    lyricsViewModel = lyricsViewModel
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null || intent.action != Intent.ACTION_VIEW) return
        val uri = intent.data ?: return

        // Security check: validate URI scheme strictly
        val scheme = uri.scheme?.lowercase()
        if (scheme != "content" && scheme != "file") {
            return
        }

        // Additional validation for file URIs: prevent path traversal or null bytes
        if (scheme == "file") {
            val path = uri.path ?: return
            if (path.contains("\u0000") || path.contains("..")) {
                return
            }
            try {
                val canonical = File(path).canonicalPath
                val appDataDir = applicationInfo.dataDir
                if (canonical.startsWith(appDataDir) && !canonical.startsWith("$appDataDir/cache")) {
                    return
                }
            } catch (e: Exception) {
                return
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(this@MainActivity, uri)
                val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    ?: uri.lastPathSegment?.substringAfterLast('/') ?: "Imported Track"
                val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
                val album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Unknown Album"
                val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val durationMs = durationStr?.toLongOrNull() ?: 0L
                mmr.release()

                val trackId = "intent_${uri.toString().hashCode()}"
                val importedTrack = TrackEntity(
                    id = trackId,
                    fingerprint = trackId,
                    uri = uri.toString(),
                    path = uri.path ?: uri.toString(),
                    title = title,
                    artist = artist,
                    album = album,
                    durationMs = durationMs,
                    mimeType = intent.type ?: "audio/*",
                    format = uri.lastPathSegment?.substringAfterLast('.', "audio")?.uppercase() ?: "AUDIO",
                    fileSizeBytes = 0L,
                    dateAdded = System.currentTimeMillis(),
                    dateModified = System.currentTimeMillis(),
                    isAvailable = true
                )

                val database = (applicationContext as LiquidMusicApplication).database
                database.trackDao().insertOrUpdateTracks(listOf(importedTrack))
                playbackViewModel.playTrack(importedTrack)
            } catch (e: Exception) {
                // Graceful fallback for unreadable audio files
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
    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    var permissionDeniedOnce by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasAudioPermission = perms[audioPermission] == true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = perms[Manifest.permission.POST_NOTIFICATIONS] == true
        }
        if (!hasAudioPermission) {
            permissionDeniedOnce = true
        }
    }

    // Trigger initial scan if permission granted
    LaunchedEffect(hasAudioPermission) {
        if (hasAudioPermission) {
            coroutineScope.launch(Dispatchers.IO) {
                LibraryScanner(context, database).scanLibrary().collectLatest {}
            }
        }
    }

    // UI Navigation & Sheet States
    var currentNav by remember { mutableStateOf(NavigationDestination.HOME) }
    var isNowPlayingExpanded by remember { mutableStateOf(false) }
    var isLyricsScreenOpen by remember { mutableStateOf(false) }
    var isEqualizerScreenOpen by remember { mutableStateOf(false) }
    var isQueueSheetOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }

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
    val queue by playbackViewModel.queue.collectAsState()

    // Lyrics state
    val lyricUiState by lyricsViewModel.lyricState.collectAsState()
    val isEditorActive by lyricsViewModel.isEditorActive.collectAsState()
    val editorLines by lyricsViewModel.editorLines.collectAsState()

    // Predictive back navigation: safely dismiss topmost sheet/overlay in reverse order
    BackHandler(enabled = isEditorActive) {
        lyricsViewModel.cancelEditor()
    }
    BackHandler(enabled = !isEditorActive && isQueueSheetOpen) {
        isQueueSheetOpen = false
    }
    BackHandler(enabled = !isEditorActive && !isQueueSheetOpen && isLyricsScreenOpen) {
        isLyricsScreenOpen = false
    }
    BackHandler(enabled = !isEditorActive && !isQueueSheetOpen && !isLyricsScreenOpen && isNowPlayingExpanded) {
        isNowPlayingExpanded = false
    }
    BackHandler(enabled = !isEditorActive && !isQueueSheetOpen && !isLyricsScreenOpen && !isNowPlayingExpanded && isEqualizerScreenOpen) {
        isEqualizerScreenOpen = false
    }
    BackHandler(enabled = !isEditorActive && !isQueueSheetOpen && !isLyricsScreenOpen && !isNowPlayingExpanded && !isEqualizerScreenOpen && isSettingsOpen) {
        isSettingsOpen = false
    }

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
                // Persistent MiniPlayer (visible when track is loaded and full screens are closed)
                if (currentTrack != null && !isNowPlayingExpanded && !isLyricsScreenOpen && !isSettingsOpen) {
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

                // Floating iOS 26 Liquid Glass 4-Destination Navigation Bar
                if (!isNowPlayingExpanded && !isLyricsScreenOpen && !isSettingsOpen) {
                    LiquidNavigationBar(
                        currentDestination = currentNav,
                        onNavigate = { currentNav = it }
                    )
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
                        modifier = Modifier.padding(LiquidSpacing.xxxl),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = LiquidAccentPrimary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(LiquidSpacing.lg))
                        Text(
                            text = "Audio Permission Required",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(LiquidSpacing.sm))
                        Text(
                            text = "Glassoplayer is 100% offline and requires access to your device's audio files to scan and play your local music library. Your files are never uploaded or shared.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground.copy(0.7f)),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(LiquidSpacing.xl))
                        Button(
                            onClick = {
                                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    arrayOf(audioPermission, Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    arrayOf(audioPermission)
                                }
                                permissionLauncher.launch(permissions)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LiquidAccentPrimary),
                            shape = RoundedCornerShape(LiquidRadii.pill)
                        ) {
                            Text("Grant Permission", fontWeight = FontWeight.Bold)
                        }
                        if (permissionDeniedOnce) {
                            Spacer(Modifier.height(LiquidSpacing.md))
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(LiquidRadii.pill)
                            ) {
                                Text("Open App Settings")
                            }
                        }
                    }
                }
            } else {
                when (currentNav) {
                    NavigationDestination.HOME -> {
                        HomeScreen(
                            recentlyPlayed = recentlyPlayed,
                            frequentlyPlayed = frequentlyPlayed,
                            recentlyAdded = recentlyAdded,
                            favorites = favorites,
                            albums = albums,
                            playlists = playlists,
                            onPlayTrack = { track, list -> playbackViewModel.playTrack(track, list) },
                            onAlbumClick = { /* Handle album click */ },
                            onPlaylistClick = { /* Handle playlist click */ },
                            onOpenSettings = { isSettingsOpen = true },
                            onNavigateToLibrary = { currentNav = NavigationDestination.LIBRARY },
                            onScanLibrary = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    LibraryScanner(context, database).scanLibrary().collectLatest {}
                                }
                            }
                        )
                    }

                    NavigationDestination.LIBRARY -> {
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

                    NavigationDestination.SEARCH -> {
                        SearchScreen(
                            tracks = allTracks,
                            onPlayTrack = { track, list -> playbackViewModel.playTrack(track, list) }
                        )
                    }

                    NavigationDestination.PLAYLISTS -> {
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
                    onToggleShuffle = { playbackViewModel.toggleShuffle() },
                    onCycleRepeat = { playbackViewModel.cycleRepeatMode() },
                    onToggleFavorite = { playbackViewModel.toggleFavorite() },
                    onSetSpeed = { sp -> playbackViewModel.setSpeed(sp) },
                    onOpenLyrics = {
                        isNowPlayingExpanded = false
                        isLyricsScreenOpen = true
                    },
                    onOpenQueue = { isQueueSheetOpen = true },
                    onOpenEqualizer = { isEqualizerScreenOpen = true },
                    onDismiss = { isNowPlayingExpanded = false }
                )
            }

            // Queue Sheet Overlay
            AnimatedVisibility(
                visible = isQueueSheetOpen,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                QueueSheet(
                    queue = queue,
                    currentTrack = currentTrack,
                    onTrackSelected = { idx -> playbackViewModel.skipToQueueIndex(idx) },
                    onClose = { isQueueSheetOpen = false }
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

            // Settings Screen Overlay
            AnimatedVisibility(
                visible = isSettingsOpen,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
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
                    onClearArtworkCache = {
                        try {
                            context.cacheDir.resolve("image_cache").deleteRecursively()
                        } catch (e: Exception) {
                            // Cache cleanup fallback
                        }
                    },
                    onClearLyricCache = {
                        coroutineScope.launch(Dispatchers.IO) {
                            database.lyricDao().clearAllLyrics()
                        }
                    },
                    onRebuildLibraryIndex = {
                        coroutineScope.launch(Dispatchers.IO) {
                            LibraryScanner(context, database).scanLibrary().collectLatest {}
                        }
                    },
                    onBack = { isSettingsOpen = false }
                )
            }
        }
    }
}
