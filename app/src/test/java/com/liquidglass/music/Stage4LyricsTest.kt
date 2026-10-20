package com.liquidglass.music

import com.liquidglass.music.lyrics.LyricLine
import com.liquidglass.music.lyrics.LrcParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class Stage4LyricsTest {

    @Test
    fun testParseStandardLrc() {
        val lrc = """
            [ti:Test Title]
            [ar:Test Artist]
            [al:Test Album]
            [offset:500]
            [00:01.00]Line 1
            [00:05.50]Line 2
            [00:10.250]Line 3
        """.trimIndent()

        val parsed = LrcParser.parse(lrc)
        assertTrue(parsed.isSynced)
        assertEquals(3, parsed.lines.size)
        assertEquals("Test Title", parsed.tags["ti"])
        assertEquals("Test Artist", parsed.tags["ar"])
        assertEquals(500L, parsed.fileOffsetMs)

        assertEquals(1000L, parsed.lines[0].timestampMs)
        assertEquals("Line 1", parsed.lines[0].text)

        assertEquals(5500L, parsed.lines[1].timestampMs)
        assertEquals("Line 2", parsed.lines[1].text)

        assertEquals(10250L, parsed.lines[2].timestampMs)
        assertEquals("Line 3", parsed.lines[2].text)
    }

    @Test
    fun testParseMultipleTimestampsPerLine() {
        val lrc = """
            [00:02.00][00:10.00]Repeated Chorus
            [00:06.00]Verse Line
        """.trimIndent()

        val parsed = LrcParser.parse(lrc)
        assertEquals(3, parsed.lines.size)

        // Must be sorted by timestamp
        assertEquals(2000L, parsed.lines[0].timestampMs)
        assertEquals("Repeated Chorus", parsed.lines[0].text)

        assertEquals(6000L, parsed.lines[1].timestampMs)
        assertEquals("Verse Line", parsed.lines[1].text)

        assertEquals(10000L, parsed.lines[2].timestampMs)
        assertEquals("Repeated Chorus", parsed.lines[2].text)
    }

    @Test
    fun testMalformedLinesHandledGracefully() {
        val lrc = """
            This is an invalid header line without bracket
            [invalid:tag without bracket closing
            [00:02.00]Valid line 1
            [999:999] Invalid timestamp
            [00:04.00]Valid line 2
        """.trimIndent()

        val parsed = LrcParser.parse(lrc)
        assertTrue(parsed.isSynced)
        assertEquals(2, parsed.lines.size)
        assertEquals("Valid line 1", parsed.lines[0].text)
        assertEquals("Valid line 2", parsed.lines[1].text)
    }

    @Test
    fun testUtf8WithBomAndWithoutBom() {
        val bomBytes = byteArrayOf(
            0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte(),
            '['.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), ':'.code.toByte(),
            '0'.code.toByte(), '1'.code.toByte(), '.'.code.toByte(), '0'.code.toByte(),
            '0'.code.toByte(), ']'.code.toByte(), 'B'.code.toByte(), 'O'.code.toByte(), 'M'.code.toByte()
        )

        val parsedBom = LrcParser.parseFromStream(ByteArrayInputStream(bomBytes))
        assertTrue(parsedBom.isSynced)
        assertEquals(1, parsedBom.lines.size)
        assertEquals("BOM", parsedBom.lines[0].text)
        assertEquals(1000L, parsedBom.lines[0].timestampMs)
    }

    @Test
    fun testBinarySearchActiveLine() {
        val lines = listOf(
            LyricLine(1000L, "First"),
            LyricLine(3000L, "Second"),
            LyricLine(6000L, "Third"),
            LyricLine(10000L, "Fourth")
        )

        // Before first line -> -1
        assertEquals(-1, LrcParser.findActiveLineIndex(lines, 500L))

        // Exactly at first line
        assertEquals(0, LrcParser.findActiveLineIndex(lines, 1000L))

        // Between first and second
        assertEquals(0, LrcParser.findActiveLineIndex(lines, 2500L))

        // Exactly at second line
        assertEquals(1, LrcParser.findActiveLineIndex(lines, 3000L))

        // Between third and fourth
        assertEquals(2, LrcParser.findActiveLineIndex(lines, 8000L))

        // Exactly at fourth line
        assertEquals(3, LrcParser.findActiveLineIndex(lines, 10000L))

        // After last line
        assertEquals(3, LrcParser.findActiveLineIndex(lines, 25000L))
    }

    @Test
    fun testBinarySearchWithUserOffset() {
        val lines = listOf(
            LyricLine(2000L, "Line A"),
            LyricLine(5000L, "Line B")
        )

        // Current position 1800ms, user offset +300ms -> adjusted 2100ms -> active line is index 0
        assertEquals(0, LrcParser.findActiveLineIndex(lines, currentPositionMs = 1800L, userOffsetMs = 300L))

        // Current position 2100ms, user offset -200ms -> adjusted 1900ms -> before line A (-1)
        assertEquals(-1, LrcParser.findActiveLineIndex(lines, currentPositionMs = 2100L, userOffsetMs = -200L))
    }

    @Test
    fun testSerializeToLrcRoundTrip() {
        val original = listOf(
            LyricLine(1500L, "Intro"),
            LyricLine(4200L, "Verse"),
            LyricLine(9850L, "Chorus")
        )

        val serialized = LrcParser.serializeToLrc(original)
        val roundTrip = LrcParser.parse(serialized)

        assertEquals(3, roundTrip.lines.size)
        assertEquals("Intro", roundTrip.lines[0].text)
        assertEquals(1500L, roundTrip.lines[0].timestampMs)
        assertEquals("Verse", roundTrip.lines[1].text)
        assertEquals(4200L, roundTrip.lines[1].timestampMs)
        assertEquals("Chorus", roundTrip.lines[2].text)
        assertEquals(9850L, roundTrip.lines[2].timestampMs)
    }

    @Test
    fun testPlainUnsynchronizedText() {
        val plain = """
            Just a poem
            without any timestamps
            or tags
        """.trimIndent()

        val parsed = LrcParser.parse(plain)
        assertFalse("Lyrics with no timestamps must have isSynced=false", parsed.isSynced)
        assertEquals(0, parsed.lines.size)
        assertEquals(plain, parsed.rawText)
    }
}
