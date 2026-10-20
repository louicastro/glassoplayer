package com.liquidglass.music.data.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.charset.Charset

data class ExtractedMetadata(
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
    val bitrate: Int = 0,
    val sampleRate: Int = 0,
    val bitDepth: Int = 16,
    val channels: Int = 2,
    val mimeType: String,
    val format: String,
    val hasArtwork: Boolean = false,
    val artworkBytes: ByteArray? = null,
    val embeddedLyrics: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ExtractedMetadata
        return title == other.title && artist == other.artist && album == other.album &&
                durationMs == other.durationMs && format == other.format
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + album.hashCode()
        return result
    }
}

object MetadataExtractor {

    /**
     * Probes audio file content header to identify the format by magic numbers,
     * not solely relying on the file extension.
     */
    fun probeFormat(file: File): String {
        if (!file.exists() || file.length() < 12) {
            return guessFormatFromExtension(file.extension)
        }
        return try {
            file.inputStream().use { probeFormat(it) }
        } catch (e: Exception) {
            guessFormatFromExtension(file.extension)
        }
    }

    fun probeFormat(inputStream: InputStream): String {
        val header = ByteArray(16)
        val read = inputStream.read(header)
        if (read < 4) return "UNKNOWN"

        // FLAC: "fLaC"
        if (header[0] == 0x66.toByte() && header[1] == 0x4C.toByte() &&
            header[2] == 0x61.toByte() && header[3] == 0x43.toByte()
        ) {
            return "FLAC"
        }

        // MP3 with ID3 header: "ID3"
        if (header[0] == 0x49.toByte() && header[1] == 0x44.toByte() && header[2] == 0x33.toByte()) {
            return "MP3"
        }

        // OGG container (Ogg Vorbis or Opus): "OggS"
        if (header[0] == 0x4F.toByte() && header[1] == 0x67.toByte() &&
            header[2] == 0x67.toByte() && header[3] == 0x53.toByte()
        ) {
            return "OGG"
        }

        // RIFF WAV: "RIFF" .... "WAVE"
        if (header[0] == 0x52.toByte() && header[1] == 0x49.toByte() &&
            header[2] == 0x46.toByte() && header[3] == 0x46.toByte()
        ) {
            return "WAV"
        }

        // AIFF: "FORM" .... "AIFF"
        if (header[0] == 0x46.toByte() && header[1] == 0x4F.toByte() &&
            header[2] == 0x52.toByte() && header[3] == 0x4D.toByte()
        ) {
            return "AIFF"
        }

        // MP4 / M4A / ALAC: "....ftyp"
        if (read >= 8 && header[4] == 0x66.toByte() && header[5] == 0x74.toByte() &&
            header[6] == 0x79.toByte() && header[7] == 0x70.toByte()
        ) {
            return "M4A"
        }

        // MKV / WebM: EBML header 0x1A 0x45 0xDF 0xA3
        if (header[0] == 0x1A.toByte() && header[1] == 0x45.toByte() &&
            header[2] == 0xDF.toByte() && header[3] == 0xA3.toByte()
        ) {
            return "MKV"
        }

        // MPEG Audio Sync Frame (MP3 without ID3 header): 0xFF followed by 0xFB, 0xFA, 0xF3, or 0xF2
        if (header[0] == 0xFF.toByte() && (header[1].toInt() and 0xE0) == 0xE0) {
            return "MP3"
        }

        return "UNKNOWN"
    }

    fun guessFormatFromExtension(ext: String): String {
        return when (ext.lowercase()) {
            "mp3" -> "MP3"
            "flac" -> "FLAC"
            "m4a", "mp4" -> "M4A"
            "aac" -> "AAC"
            "wav" -> "WAV"
            "ogg", "oga" -> "OGG"
            "opus" -> "OPUS"
            "aiff", "aif" -> "AIFF"
            "wv" -> "WAVPACK"
            "ape" -> "APE"
            "wma" -> "WMA"
            "ac3" -> "AC3"
            "mkv" -> "MKV"
            else -> ext.uppercase()
        }
    }

    fun guessMimeType(format: String): String {
        return when (format) {
            "MP3" -> "audio/mpeg"
            "FLAC" -> "audio/flac"
            "M4A", "AAC" -> "audio/mp4"
            "WAV" -> "audio/wav"
            "OGG" -> "audio/ogg"
            "OPUS" -> "audio/opus"
            "AIFF" -> "audio/x-aiff"
            "WAVPACK" -> "audio/x-wavpack"
            "APE" -> "audio/x-ape"
            "MKV" -> "audio/x-matroska"
            else -> "audio/*"
        }
    }

    /**
     * Extracts full metadata from a local audio file or content URI using MediaMetadataRetriever
     * and fallback stream tag parsers for embedded lyrics and tags.
     */
    fun extract(context: Context, uri: Uri, file: File? = null): ExtractedMetadata {
        val probedFormat = file?.let { probeFormat(it) } ?: "MP3"
        val fallbackName = file?.let { SidecarLyricMatcher.getBaseNameWithoutExtension(it.name) } ?: "Track"

        val retriever = MediaMetadataRetriever()
        return try {
            if (file != null && file.exists()) {
                retriever.setDataSource(file.absolutePath)
            } else {
                retriever.setDataSource(context, uri)
            }

            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?.takeIf { it.isNotBlank() } ?: fallbackName
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?.takeIf { it.isNotBlank() } ?: "Unknown Album"
            val albumArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
                ?.takeIf { it.isNotBlank() }
            val composer = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER)
                ?.takeIf { it.isNotBlank() }
            val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                ?.takeIf { it.isNotBlank() }
            val year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull()
            val trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                ?.split("/")?.firstOrNull()?.toIntOrNull()
            val discNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)
                ?.split("/")?.firstOrNull()?.toIntOrNull()
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                ?.toIntOrNull()?.let { it / 1000 } ?: 0

            val artworkBytes = try {
                retriever.embeddedPicture
            } catch (e: Exception) {
                null
            }

            // Extract embedded lyrics if present in ID3 / Vorbis comments
            val embeddedLyrics = file?.let { extractEmbeddedLyrics(it) }

            ExtractedMetadata(
                title = title,
                artist = artist,
                album = album,
                albumArtist = albumArtist,
                composer = composer,
                genre = genre,
                year = year,
                trackNumber = trackNumber,
                discNumber = discNumber,
                durationMs = durationMs,
                bitrate = bitrate,
                sampleRate = 44100, // standard default
                bitDepth = 16,
                channels = 2,
                mimeType = guessMimeType(probedFormat),
                format = probedFormat,
                hasArtwork = artworkBytes != null,
                artworkBytes = artworkBytes,
                embeddedLyrics = embeddedLyrics
            )
        } catch (e: Exception) {
            // Return fallback metadata for unreadable/corrupt files without crashing
            ExtractedMetadata(
                title = fallbackName,
                artist = "Unknown Artist",
                album = "Unknown Album",
                mimeType = guessMimeType(probedFormat),
                format = probedFormat
            )
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // Ignore release errors
            }
        }
    }

    /**
     * Searches for ID3 USLT/SYLT frames, Vorbis comment LYRICS, or MP4 ©lyr in the file header
     * without loading entire multi-megabyte audio stream into memory.
     */
    fun extractEmbeddedLyrics(file: File): String? {
        if (!file.exists() || file.length() < 10) return null
        return try {
            FileInputStream(file).use { fis ->
                val header = ByteArray(10)
                if (fis.read(header) < 10) return null

                // Check for ID3v2 tag: "ID3"
                if (header[0] == 0x49.toByte() && header[1] == 0x44.toByte() && header[2] == 0x33.toByte()) {
                    val majorVersion = header[3].toInt()
                    val tagSize = ((header[6].toInt() and 0x7F) shl 21) or
                            ((header[7].toInt() and 0x7F) shl 14) or
                            ((header[8].toInt() and 0x7F) shl 7) or
                            (header[9].toInt() and 0x7F)

                    // Bound scan to max 512 KB to avoid excessive memory on malformed tags
                    val maxScan = minOf(tagSize, 524288)
                    val tagData = ByteArray(maxScan)
                    val readTotal = fis.read(tagData)
                    if (readTotal > 0) {
                        return parseId3Lyrics(tagData, readTotal, majorVersion)
                    }
                }
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseId3Lyrics(data: ByteArray, length: Int, majorVersion: Int): String? {
        // Look for USLT frame (ID3v2.3/ID3v2.4) or ULT frame (ID3v2.2)
        val targetFrame = if (majorVersion == 2) "ULT" else "USLT"
        val targetBytes = targetFrame.toByteArray(Charsets.US_ASCII)

        var idx = 0
        while (idx < length - 10) {
            var match = true
            for (k in targetBytes.indices) {
                if (data[idx + k] != targetBytes[k]) {
                    match = false
                    break
                }
            }
            if (match) {
                // Found USLT frame
                val frameHeaderSize = if (majorVersion == 2) 6 else 10
                val frameSize = if (majorVersion == 2) {
                    ((data[idx + 3].toInt() and 0xFF) shl 16) or
                            ((data[idx + 4].toInt() and 0xFF) shl 8) or
                            (data[idx + 5].toInt() and 0xFF)
                } else {
                    ((data[idx + 4].toInt() and 0xFF) shl 24) or
                            ((data[idx + 5].toInt() and 0xFF) shl 16) or
                            ((data[idx + 6].toInt() and 0xFF) shl 8) or
                            (data[idx + 7].toInt() and 0xFF)
                }

                if (frameSize in 1..262144 && idx + frameHeaderSize + frameSize <= length) {
                    val frameContentStart = idx + frameHeaderSize
                    // First byte is encoding: 0 = ISO-8859-1, 1 = UTF-16 with BOM, 2 = UTF-16BE, 3 = UTF-8
                    val encodingByte = data[frameContentStart].toInt() and 0xFF
                    val charset: Charset = when (encodingByte) {
                        1 -> Charsets.UTF_16
                        2 -> Charsets.UTF_16BE
                        3 -> Charsets.UTF_8
                        else -> Charsets.ISO_8859_1
                    }

                    // Skip language (3 bytes) and description (up to null terminator)
                    var textStart = frameContentStart + 4 // 1 byte encoding + 3 bytes language
                    while (textStart < frameContentStart + frameSize && data[textStart].toInt() != 0) {
                        textStart++
                    }
                    textStart++ // Skip null delimiter

                    if (textStart < frameContentStart + frameSize) {
                        val textLength = (frameContentStart + frameSize) - textStart
                        val lyrics = String(data, textStart, textLength, charset).trim()
                        if (lyrics.isNotBlank()) {
                            return lyrics
                        }
                    }
                }
            }
            idx++
        }
        return null
    }
}
