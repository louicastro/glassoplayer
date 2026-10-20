package com.liquidglass.music.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Stable audio track entity representing an indexed local audio file.
 */
@Entity(
    tableName = "tracks",
    indices = [
        Index(value = ["fingerprint"], unique = true),
        Index(value = ["uri"]),
        Index(value = ["artist"]),
        Index(value = ["album"]),
        Index(value = ["isFavorite"]),
        Index(value = ["lastPlayedTimestamp"])
    ]
)
data class TrackEntity(
    @PrimaryKey
    val id: String, // Stable ID based on fingerprint or path hash
    val fingerprint: String, // Content/metadata fingerprint
    val uri: String, // Content or File URI
    val path: String, // File path on disk
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String? = null,
    val composer: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val durationMs: Long = 0L,
    val bitrate: Int = 0, // in kbps
    val sampleRate: Int = 0, // in Hz
    val bitDepth: Int = 16,
    val channels: Int = 2,
    val mimeType: String,
    val format: String, // "MP3", "FLAC", "AAC", "WAV", "OGG", "OPUS", etc.
    val fileSizeBytes: Long = 0L,
    val dateAdded: Long = 0L,
    val dateModified: Long = 0L,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long? = null,
    val isAvailable: Boolean = true, // Set to false if file is missing/moved rather than deleting
    val hasEmbeddedArtwork: Boolean = false,
    val artworkUri: String? = null,
    val hasEmbeddedLyrics: Boolean = false
)
