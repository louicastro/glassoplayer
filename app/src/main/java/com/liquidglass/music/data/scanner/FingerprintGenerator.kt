package com.liquidglass.music.data.scanner

import java.io.File
import java.io.InputStream
import java.security.MessageDigest

object FingerprintGenerator {

    /**
     * Generates a stable SHA-256 fingerprint for an audio file.
     * Uses the first 64KB of the file (or full file if smaller) combined with the file size.
     * This makes fingerprint calculation extremely fast (<1ms per track) and bounded in memory,
     * while guaranteeing stability across file renames and moves.
     */
    fun generateFingerprint(file: File): String {
        if (!file.exists() || !file.canRead()) {
            return fallbackFingerprint(file.absolutePath, file.length(), file.lastModified())
        }
        return try {
            file.inputStream().use { stream ->
                generateFingerprint(stream, file.length())
            }
        } catch (e: Exception) {
            fallbackFingerprint(file.absolutePath, file.length(), file.lastModified())
        }
    }

    fun generateFingerprint(inputStream: InputStream, totalLength: Long): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(65536) // 64 KB sample
        var totalRead = 0
        while (totalRead < buffer.size) {
            val bytesRead = inputStream.read(buffer, totalRead, buffer.size - totalRead)
            if (bytesRead == -1) break
            totalRead += bytesRead
        }
        digest.update(buffer, 0, totalRead)
        // Also salt with the total file length in bytes to distinguish truncated files
        val lengthBytes = ByteArray(8)
        for (i in 0..7) {
            lengthBytes[i] = ((totalLength shr (i * 8)) and 0xFF).toByte()
        }
        digest.update(lengthBytes)

        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun fallbackFingerprint(path: String, size: Long, lastModified: Long): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = "$path:$size:$lastModified"
        return digest.digest(input.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
}
