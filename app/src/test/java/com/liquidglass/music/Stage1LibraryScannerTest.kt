package com.liquidglass.music

import com.liquidglass.music.data.model.LyricSourceType
import com.liquidglass.music.data.scanner.FingerprintGenerator
import com.liquidglass.music.data.scanner.MetadataExtractor
import com.liquidglass.music.data.scanner.SidecarLyricMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.File

class Stage1LibraryScannerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testSidecarLyricMatcher_exactMatch() {
        val folder = tempFolder.newFolder("music")
        val audio = File(folder, "Song.mp3").apply { writeText("audio content") }
        val lrc = File(folder, "Song.lrc").apply { writeText("[00:01.00]Hello") }

        val matched = SidecarLyricMatcher.findSidecarLyricFile(audio)
        assertNotNull(matched)
        assertEquals(lrc.absolutePath, matched?.absolutePath)
    }

    @Test
    fun testSidecarLyricMatcher_multipleDotsInFilename() {
        val folder = tempFolder.newFolder("music_dots")
        val audio = File(folder, "Artist.Song.Live.2019.flac").apply { writeText("audio") }
        val lrc = File(folder, "Artist.Song.Live.2019.lrc").apply { writeText("[00:10.00]Line") }

        val matched = SidecarLyricMatcher.findSidecarLyricFile(audio)
        assertNotNull(matched)
        assertEquals(lrc.absolutePath, matched?.absolutePath)
    }

    @Test
    fun testSidecarLyricMatcher_caseInsensitive() {
        val folder = tempFolder.newFolder("music_case")
        val audio = File(folder, "track01.MP3").apply { writeText("audio") }
        val lrc = File(folder, "track01.LRC").apply { writeText("[00:05.00]Upper case test") }

        val matched = SidecarLyricMatcher.findSidecarLyricFile(audio)
        assertNotNull(matched)
        assertEquals(lrc.absolutePath, matched?.absolutePath)
    }

    @Test
    fun testSidecarLyricMatcher_unicodeCharacters() {
        val folder = tempFolder.newFolder("music_unicode")
        val audio = File(folder, "01. 晴天 (Jay Chou).m4a").apply { writeText("audio") }
        val lrc = File(folder, "01. 晴天 (Jay Chou).lrc").apply { writeText("[00:01.00]故事的小黄花") }

        val matched = SidecarLyricMatcher.findSidecarLyricFile(audio)
        assertNotNull(matched)
        assertEquals(lrc.absolutePath, matched?.absolutePath)
    }

    @Test
    fun testSidecarLyricMatcher_prefersLrcOverTxt() {
        val folder = tempFolder.newFolder("music_priority")
        val audio = File(folder, "Track.wav").apply { writeText("audio") }
        val txt = File(folder, "Track.txt").apply { writeText("Plain lyrics") }
        val lrc = File(folder, "Track.lrc").apply { writeText("[00:02.00]Synced lyrics") }

        val matched = SidecarLyricMatcher.findSidecarLyricFile(audio)
        assertNotNull(matched)
        assertEquals(lrc.absolutePath, matched?.absolutePath)
    }

    @Test
    fun testSidecarLyricMatcher_fallsBackToTxtWhenNoLrc() {
        val folder = tempFolder.newFolder("music_txt")
        val audio = File(folder, "Song.ogg").apply { writeText("audio") }
        val txt = File(folder, "Song.txt").apply { writeText("Plain lyrics") }

        val matched = SidecarLyricMatcher.findSidecarLyricFile(audio)
        assertNotNull(matched)
        assertEquals(txt.absolutePath, matched?.absolutePath)
    }

    @Test
    fun testSidecarLyricMatcher_noMatch() {
        val folder = tempFolder.newFolder("music_none")
        val audio = File(folder, "Solo.mp3").apply { writeText("audio") }
        File(folder, "Other.lrc").apply { writeText("other") }

        val matched = SidecarLyricMatcher.findSidecarLyricFile(audio)
        assertNull(matched)
    }

    @Test
    fun testFingerprintGenerator_stabilityAcrossRenames() {
        val folder = tempFolder.newFolder("fingerprint")
        val content = "Sample audio bytes repeated to simulate file content: ".repeat(100).toByteArray()
        val file1 = File(folder, "Original.mp3").apply { writeBytes(content) }
        val fp1 = FingerprintGenerator.generateFingerprint(file1)

        val file2 = File(folder, "Renamed_Song.mp3").apply { writeBytes(content) }
        val fp2 = FingerprintGenerator.generateFingerprint(file2)

        assertEquals("Fingerprint must match when file content is identical regardless of filename", fp1, fp2)
    }

    @Test
    fun testFingerprintGenerator_differentContentYieldsDifferentFingerprint() {
        val folder = tempFolder.newFolder("fp_diff")
        val file1 = File(folder, "A.mp3").apply { writeBytes("Content A".toByteArray()) }
        val file2 = File(folder, "B.mp3").apply { writeBytes("Content B".toByteArray()) }

        val fp1 = FingerprintGenerator.generateFingerprint(file1)
        val fp2 = FingerprintGenerator.generateFingerprint(file2)

        assertFalse("Different contents must produce distinct fingerprints", fp1 == fp2)
    }

    @Test
    fun testMetadataExtractor_probesFormatsByMagicNumbers() {
        // FLAC: "fLaC"
        val flacHeader = byteArrayOf('f'.code.toByte(), 'L'.code.toByte(), 'a'.code.toByte(), 'C'.code.toByte(), 0, 0, 0, 0)
        assertEquals("FLAC", MetadataExtractor.probeFormat(ByteArrayInputStream(flacHeader)))

        // MP3 ID3: "ID3"
        val id3Header = byteArrayOf('I'.code.toByte(), 'D'.code.toByte(), '3'.code.toByte(), 3, 0, 0, 0, 0)
        assertEquals("MP3", MetadataExtractor.probeFormat(ByteArrayInputStream(id3Header)))

        // OGG: "OggS"
        val oggHeader = byteArrayOf('O'.code.toByte(), 'g'.code.toByte(), 'g'.code.toByte(), 'S'.code.toByte(), 0, 0, 0, 0)
        assertEquals("OGG", MetadataExtractor.probeFormat(ByteArrayInputStream(oggHeader)))

        // WAV: "RIFF....WAVE"
        val wavHeader = byteArrayOf('R'.code.toByte(), 'I'.code.toByte(), 'F'.code.toByte(), 'F'.code.toByte(), 0, 0, 0, 0)
        assertEquals("WAV", MetadataExtractor.probeFormat(ByteArrayInputStream(wavHeader)))

        // AIFF: "FORM....AIFF"
        val aiffHeader = byteArrayOf('F'.code.toByte(), 'O'.code.toByte(), 'R'.code.toByte(), 'M'.code.toByte(), 0, 0, 0, 0)
        assertEquals("AIFF", MetadataExtractor.probeFormat(ByteArrayInputStream(aiffHeader)))

        // M4A: "....ftyp"
        val m4aHeader = byteArrayOf(0, 0, 0, 0x20, 'f'.code.toByte(), 't'.code.toByte(), 'y'.code.toByte(), 'p'.code.toByte())
        assertEquals("M4A", MetadataExtractor.probeFormat(ByteArrayInputStream(m4aHeader)))

        // MKV: EBML header 0x1A 0x45 0xDF 0xA3
        val mkvHeader = byteArrayOf(0x1A.toByte(), 0x45.toByte(), 0xDF.toByte(), 0xA3.toByte(), 0, 0, 0, 0)
        assertEquals("MKV", MetadataExtractor.probeFormat(ByteArrayInputStream(mkvHeader)))

        // MP3 Sync Frame: 0xFF 0xFB
        val mp3Sync = byteArrayOf(0xFF.toByte(), 0xFB.toByte(), 0x90.toByte(), 0x64.toByte(), 0, 0, 0, 0)
        assertEquals("MP3", MetadataExtractor.probeFormat(ByteArrayInputStream(mp3Sync)))
    }

    @Test
    fun testLyricSourcePriorityOrder() {
        assertTrue(LyricSourceType.USER_EDITED.hasHigherPriorityThan(LyricSourceType.USER_IMPORTED))
        assertTrue(LyricSourceType.USER_IMPORTED.hasHigherPriorityThan(LyricSourceType.EMBEDDED))
        assertTrue(LyricSourceType.EMBEDDED.hasHigherPriorityThan(LyricSourceType.SIDECAR))
        assertTrue(LyricSourceType.SIDECAR.hasHigherPriorityThan(LyricSourceType.NONE))
    }
}
