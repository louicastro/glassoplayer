package com.liquidglass.music.data.scanner

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.liquidglass.music.data.LiquidMusicDatabase
import com.liquidglass.music.data.model.AlbumEntity
import com.liquidglass.music.data.model.ArtistEntity
import com.liquidglass.music.data.model.LyricRecordEntity
import com.liquidglass.music.data.model.LyricSourceType
import com.liquidglass.music.data.model.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

data class ScanProgress(
    val scannedCount: Int,
    val totalCount: Int,
    val currentFile: String,
    val isComplete: Boolean = false,
    val error: String? = null
)

class LibraryScanner(
    private val context: Context,
    private val database: LiquidMusicDatabase
) {
    /**
     * Scans both MediaStore and any custom user-added folders.
     * Emits progress updates while running on the IO dispatcher.
     */
    fun scanLibrary(customFolders: List<File> = emptyList()): Flow<ScanProgress> = flow {
        emit(ScanProgress(0, 0, "Initializing scan..."))

        val discoveredFiles = mutableListOf<File>()
        val mediaStoreUris = mutableMapOf<String, Uri>()

        // 1. Scan MediaStore
        try {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.SIZE
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val queryUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            context.contentResolver.query(queryUri, projection, selection, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val path = cursor.getString(dataCol)
                    if (path != null) {
                        val file = File(path)
                        if (file.exists() && file.canRead()) {
                            discoveredFiles.add(file)
                            val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                            mediaStoreUris[file.absolutePath] = contentUri
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // MediaStore might be restricted on certain sandboxes, fallback to folder scanning
        }

        // 2. Scan custom user folders
        for (folder in customFolders) {
            if (folder.exists() && folder.isDirectory) {
                scanFolderRecursively(folder, discoveredFiles)
            }
        }

        val totalFiles = discoveredFiles.size
        emit(ScanProgress(0, totalFiles, "Found $totalFiles audio files"))

        val activePaths = mutableListOf<String>()
        val trackEntities = mutableListOf<TrackEntity>()
        val albumMap = mutableMapOf<String, MutableList<TrackEntity>>()
        val artistMap = mutableMapOf<String, MutableList<TrackEntity>>()

        // 3. Process each file
        for ((index, file) in discoveredFiles.withIndex()) {
            activePaths.add(file.absolutePath)
            emit(ScanProgress(index + 1, totalFiles, file.name))

            val existingTrack = database.trackDao().getTrackByPath(file.absolutePath)
            val isModified = existingTrack == null || existingTrack.dateModified != file.lastModified()

            val track = if (isModified) {
                val fingerprint = FingerprintGenerator.generateFingerprint(file)
                val contentUri = mediaStoreUris[file.absolutePath] ?: Uri.fromFile(file)
                val metadata = MetadataExtractor.extract(context, contentUri, file)

                // Check for sidecar lyrics (.lrc or .txt)
                val sidecarLyricFile = SidecarLyricMatcher.findSidecarLyricFile(file)
                val hasSidecarLyrics = sidecarLyricFile != null

                // Check existing lyric record to respect priority
                val existingLyric = database.lyricDao().getLyricForTrack(fingerprint)
                if (existingLyric == null ||
                    existingLyric.sourceType == LyricSourceType.NONE.name ||
                    existingLyric.sourceType == LyricSourceType.SIDECAR.name ||
                    existingLyric.sourceType == LyricSourceType.EMBEDDED.name
                ) {
                    if (hasSidecarLyrics && sidecarLyricFile != null) {
                        try {
                            val lyricContent = sidecarLyricFile.readText(Charsets.UTF_8)
                            val isSynced = lyricContent.contains(Regex("""\[\d{1,2}:\d{2}"""))
                            database.lyricDao().insertOrUpdateLyric(
                                LyricRecordEntity(
                                    trackId = fingerprint,
                                    sourceType = LyricSourceType.SIDECAR.name,
                                    rawContent = lyricContent,
                                    isSynced = isSynced,
                                    sourcePath = sidecarLyricFile.absolutePath
                                )
                            )
                        } catch (e: Exception) {
                            // Ignore lyric read error
                        }
                    } else if (metadata.embeddedLyrics != null) {
                        val isSynced = metadata.embeddedLyrics.contains(Regex("""\[\d{1,2}:\d{2}"""))
                        database.lyricDao().insertOrUpdateLyric(
                            LyricRecordEntity(
                                trackId = fingerprint,
                                sourceType = LyricSourceType.EMBEDDED.name,
                                rawContent = metadata.embeddedLyrics,
                                isSynced = isSynced
                            )
                        )
                    }
                }

                TrackEntity(
                    id = fingerprint,
                    fingerprint = fingerprint,
                    uri = contentUri.toString(),
                    path = file.absolutePath,
                    title = metadata.title,
                    artist = metadata.artist,
                    album = metadata.album,
                    albumArtist = metadata.albumArtist,
                    composer = metadata.composer,
                    genre = metadata.genre,
                    year = metadata.year,
                    trackNumber = metadata.trackNumber,
                    discNumber = metadata.discNumber,
                    durationMs = metadata.durationMs,
                    bitrate = metadata.bitrate,
                    sampleRate = metadata.sampleRate,
                    bitDepth = metadata.bitDepth,
                    channels = metadata.channels,
                    mimeType = metadata.mimeType,
                    format = metadata.format,
                    fileSizeBytes = file.length(),
                    dateAdded = System.currentTimeMillis(),
                    dateModified = file.lastModified(),
                    isFavorite = existingTrack?.isFavorite ?: false,
                    playCount = existingTrack?.playCount ?: 0,
                    lastPlayedTimestamp = existingTrack?.lastPlayedTimestamp,
                    isAvailable = true,
                    hasEmbeddedArtwork = metadata.hasArtwork,
                    artworkUri = if (metadata.hasArtwork) contentUri.toString() else null,
                    hasEmbeddedLyrics = metadata.embeddedLyrics != null || hasSidecarLyrics
                )
            } else {
                existingTrack.copy(isAvailable = true)
            }

            trackEntities.add(track)

            // Group for albums
            val albumKey = "${track.album}_${track.albumArtist ?: track.artist}"
            albumMap.getOrPut(albumKey) { mutableListOf() }.add(track)

            // Group for artists
            artistMap.getOrPut(track.artist) { mutableListOf() }.add(track)

            // Batch insert every 50 tracks to keep memory low and update UI smoothly
            if (trackEntities.size >= 50) {
                database.trackDao().insertOrUpdateTracks(trackEntities)
                trackEntities.clear()
            }
        }

        // Insert remaining tracks
        if (trackEntities.isNotEmpty()) {
            database.trackDao().insertOrUpdateTracks(trackEntities)
            trackEntities.clear()
        }

        // 4. Update Albums & Artists
        val albumEntities = albumMap.map { (key, tracks) ->
            val first = tracks.first()
            AlbumEntity(
                id = key,
                title = first.album,
                artist = first.albumArtist ?: first.artist,
                trackCount = tracks.size,
                year = tracks.mapNotNull { it.year }.maxOrNull(),
                artworkUri = tracks.firstOrNull { it.hasEmbeddedArtwork }?.artworkUri
            )
        }
        database.albumDao().insertOrUpdateAlbums(albumEntities)

        val artistEntities = artistMap.map { (artistName, tracks) ->
            val uniqueAlbums = tracks.map { it.album }.distinct().size
            ArtistEntity(
                id = artistName,
                name = artistName,
                trackCount = tracks.size,
                albumCount = uniqueAlbums
            )
        }
        database.artistDao().insertOrUpdateArtists(artistEntities)

        // 5. Mark missing tracks as unavailable instead of deleting
        database.trackDao().markMissingTracks(activePaths)

        emit(ScanProgress(totalFiles, totalFiles, "Library scan complete", isComplete = true))
    }.flowOn(Dispatchers.IO)

    private fun scanFolderRecursively(folder: File, result: MutableList<File>) {
        val files = folder.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                scanFolderRecursively(file, result)
            } else if (isSupportedAudioFile(file)) {
                result.add(file)
            }
        }
    }

    private fun isSupportedAudioFile(file: File): Boolean {
        val ext = file.extension.lowercase()
        return ext in SUPPORTED_EXTENSIONS
    }

    companion object {
        val SUPPORTED_EXTENSIONS = setOf(
            "mp3", "aac", "m4a", "flac", "wav", "ogg", "opus", "aiff", "aif",
            "wv", "ape", "tta", "wma", "ac3", "mkv"
        )
    }
}
