package com.liquidglass.music.data.scanner

import java.io.File

object SidecarLyricMatcher {

    /**
     * Finds a matching sidecar lyric file (.lrc or .txt) in the same directory as the given audio file.
     * Follows priority:
     * 1. Exact match with .lrc (case-insensitive)
     * 2. Exact match with .txt (case-insensitive)
     *
     * Handles:
     * - Unicode filenames
     * - Multiple dots in filename (e.g., "Song.Live.2019.mp3" -> looks for "Song.Live.2019.lrc")
     * - Case differences in extension (.lrc, .LRC, .Lrc, .txt, .TXT)
     */
    fun findSidecarLyricFile(audioFile: File): File? {
        val parentDir = audioFile.parentFile ?: return null
        if (!parentDir.exists() || !parentDir.isDirectory) return null

        val baseName = getBaseNameWithoutExtension(audioFile.name)
        val filesInDir = parentDir.listFiles() ?: return null

        var bestLrcFile: File? = null
        var bestTxtFile: File? = null

        for (file in filesInDir) {
            if (!file.isFile) continue
            val fileName = file.name
            val fileBase = getBaseNameWithoutExtension(fileName)

            if (fileBase.equals(baseName, ignoreCase = true)) {
                val ext = file.extension.lowercase()
                if (ext == "lrc") {
                    bestLrcFile = file
                    break // LRC takes top priority over TXT
                } else if (ext == "txt" && bestTxtFile == null) {
                    bestTxtFile = file
                }
            }
        }

        return bestLrcFile ?: bestTxtFile
    }

    /**
     * Strips only the last extension from a filename.
     * E.g. "Song.Live.2019.mp3" -> "Song.Live.2019"
     */
    fun getBaseNameWithoutExtension(fileName: String): String {
        val dotIndex = fileName.lastIndexOf('.')
        return if (dotIndex > 0) {
            fileName.substring(0, dotIndex)
        } else {
            fileName
        }
    }
}
