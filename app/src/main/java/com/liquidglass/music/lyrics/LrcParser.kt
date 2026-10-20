package com.liquidglass.music.lyrics

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.util.regex.Pattern

data class LyricLine(
    val timestampMs: Long,
    val text: String
) : Comparable<LyricLine> {
    override fun compareTo(other: LyricLine): Int = timestampMs.compareTo(other.timestampMs)
}

data class ParsedLyrics(
    val lines: List<LyricLine>,
    val tags: Map<String, String> = emptyMap(),
    val isSynced: Boolean = false,
    val fileOffsetMs: Long = 0L,
    val rawText: String = ""
)

object LrcParser {

    // Regex for standard LRC timestamps: [mm:ss.xx] or [mm:ss.xxx]
    private val TIMESTAMP_PATTERN = Pattern.compile("""\[(\d{1,2}):(\d{2})(?:\.(\d{2,3}))?\]""")
    private val TAG_PATTERN = Pattern.compile("""^\[([a-zA-Z]+):(.*)\]$""")

    /**
     * Parses raw LRC or plain text content.
     * Handles:
     * - Standard and multi-timestamp lines: [00:12.00][00:24.00]Hello world
     * - Tags: [ti:], [ar:], [al:], [offset:+500]
     * - Malformed lines (skips with graceful recovery)
     * - Non-timestamped plain lyrics
     */
    fun parse(content: String): ParsedLyrics {
        val cleanContent = stripBom(content)
        val lines = cleanContent.lines()
        val tags = mutableMapOf<String, String>()
        val parsedLines = mutableListOf<LyricLine>()
        var fileOffsetMs = 0L

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // 1. Check for metadata tags: [offset:+500], [ti:Title], [ar:Artist], etc.
            val tagMatcher = TAG_PATTERN.matcher(trimmed)
            if (tagMatcher.matches()) {
                val key = tagMatcher.group(1)?.lowercase() ?: ""
                val value = tagMatcher.group(2)?.trim() ?: ""
                tags[key] = value
                if (key == "offset") {
                    fileOffsetMs = value.toLongOrNull() ?: 0L
                }
                continue
            }

            // 2. Check for timestamps (can be multiple on one line)
            val matcher = TIMESTAMP_PATTERN.matcher(trimmed)
            val timestamps = mutableListOf<Long>()
            var lastMatchEnd = 0

            while (matcher.find()) {
                val minutes = matcher.group(1)?.toLongOrNull() ?: 0L
                val seconds = matcher.group(2)?.toLongOrNull() ?: 0L
                val fractionStr = matcher.group(3)
                val millis = when {
                    fractionStr == null -> 0L
                    fractionStr.length == 2 -> fractionStr.toLong() * 10
                    fractionStr.length == 3 -> fractionStr.toLong()
                    else -> fractionStr.take(3).toLong()
                }
                val totalMs = minutes * 60000L + seconds * 1000L + millis
                timestamps.add(totalMs)
                lastMatchEnd = matcher.end()
            }

            if (timestamps.isNotEmpty()) {
                // The remaining string after all timestamps is the lyric text
                val lyricText = trimmed.substring(lastMatchEnd).trim()
                for (ts in timestamps) {
                    parsedLines.add(LyricLine(ts, lyricText))
                }
            }
        }

        // Sort by timestamp
        parsedLines.sort()

        val isSynced = parsedLines.isNotEmpty()
        return ParsedLyrics(
            lines = parsedLines,
            tags = tags,
            isSynced = isSynced,
            fileOffsetMs = fileOffsetMs,
            rawText = cleanContent
        )
    }

    /**
     * Reads an input stream with automatic encoding detection:
     * Handles UTF-8 with BOM, UTF-8 without BOM, UTF-16, and falls back to ISO-8859-1.
     */
    fun parseFromStream(inputStream: InputStream): ParsedLyrics {
        val bytes = inputStream.readBytes()
        val text = decodeWithFallback(bytes)
        return parse(text)
    }

    fun decodeWithFallback(bytes: ByteArray): String {
        if (bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() &&
            bytes[1] == 0xBB.toByte() &&
            bytes[2] == 0xBF.toByte()
        ) {
            // UTF-8 with BOM
            return String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
        }

        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
        }

        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
        }

        // Try UTF-8 first
        return try {
            val decoder = Charsets.UTF_8.newDecoder()
            decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString()
        } catch (e: Exception) {
            // Graceful fallback to ISO-8859-1 (Latin-1) which never throws MalformedInputException
            String(bytes, Charsets.ISO_8859_1)
        }
    }

    fun stripBom(str: String): String {
        return if (str.startsWith("\uFEFF")) {
            str.substring(1)
        } else {
            str
        }
    }

    /**
     * Efficient binary search to find the active line index for a given playback position.
     * Takes into account user offset.
     * Returns -1 if playback position is before the first lyric line.
     */
    fun findActiveLineIndex(lines: List<LyricLine>, currentPositionMs: Long, userOffsetMs: Long = 0L): Int {
        if (lines.isEmpty()) return -1
        val adjustedPosition = currentPositionMs + userOffsetMs
        if (adjustedPosition < lines.first().timestampMs) return -1
        if (adjustedPosition >= lines.last().timestampMs) return lines.lastIndex

        var low = 0
        var high = lines.lastIndex
        var result = 0

        while (low <= high) {
            val mid = (low + high) ushr 1
            if (lines[mid].timestampMs <= adjustedPosition) {
                result = mid
                low = mid + 1 // try to find a later line that also satisfies condition
            } else {
                high = mid - 1
            }
        }

        return result
    }

    /**
     * Serializes a list of LyricLine objects into a standard LRC string.
     */
    fun serializeToLrc(lines: List<LyricLine>, tags: Map<String, String> = emptyMap(), offsetMs: Long = 0L): String {
        return buildString {
            tags.forEach { (k, v) ->
                appendLine("[$k:$v]")
            }
            if (offsetMs != 0L) {
                appendLine("[offset:$offsetMs]")
            }
            lines.sorted().forEach { line ->
                val totalSec = line.timestampMs / 1000
                val min = totalSec / 60
                val sec = totalSec % 60
                val ms = (line.timestampMs % 1000) / 10
                appendLine("[%02d:%02d.%02d]%s".format(min, sec, ms, line.text))
            }
        }
    }
}
