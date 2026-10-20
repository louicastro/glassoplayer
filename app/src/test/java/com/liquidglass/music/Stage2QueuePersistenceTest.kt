package com.liquidglass.music

import com.liquidglass.music.data.dao.QueueDao
import com.liquidglass.music.data.dao.TrackDao
import com.liquidglass.music.data.model.QueueItemEntity
import com.liquidglass.music.data.model.TrackEntity
import com.liquidglass.music.playback.QueueManager
import com.liquidglass.music.playback.RepeatMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class Stage2QueuePersistenceTest {

    private val storedQueue = mutableListOf<QueueItemEntity>()
    private val tracksDatabase = mutableMapOf<String, TrackEntity>()

    private val fakeQueueDao = object : QueueDao {
        override fun getQueueItems(): Flow<List<QueueItemEntity>> = emptyFlow()

        override suspend fun getQueueItemsSync(): List<QueueItemEntity> {
            return storedQueue.toList()
        }

        override suspend fun insertQueueItems(items: List<QueueItemEntity>) {
            storedQueue.addAll(items)
        }

        override suspend fun clearQueue() {
            storedQueue.clear()
        }

        override suspend fun clearCurrentFlags() {
            for (i in storedQueue.indices) {
                storedQueue[i] = storedQueue[i].copy(isCurrent = false)
            }
        }

        override suspend fun setCurrentTrack(trackId: String) {
            for (i in storedQueue.indices) {
                storedQueue[i] = storedQueue[i].copy(isCurrent = storedQueue[i].trackId == trackId)
            }
        }

        override suspend fun replaceQueue(items: List<QueueItemEntity>) {
            storedQueue.clear()
            storedQueue.addAll(items)
        }
    }

    private val fakeTrackDao = object : TrackDao {
        override fun getAllAvailableTracks(): Flow<List<TrackEntity>> = emptyFlow()
        override fun getRecentlyAddedTracks(): Flow<List<TrackEntity>> = emptyFlow()
        override fun getRecentlyPlayedTracks(limit: Int): Flow<List<TrackEntity>> = emptyFlow()
        override fun getFrequentlyPlayedTracks(limit: Int): Flow<List<TrackEntity>> = emptyFlow()
        override fun getFavoriteTracks(): Flow<List<TrackEntity>> = emptyFlow()
        override suspend fun getTrackById(id: String): TrackEntity? = tracksDatabase[id]
        override suspend fun getTrackByFingerprint(fingerprint: String): TrackEntity? = tracksDatabase[fingerprint]
        override suspend fun getTrackByUri(uri: String): TrackEntity? = null
        override suspend fun getTrackByPath(path: String): TrackEntity? = null
        override fun getTracksForAlbum(album: String): Flow<List<TrackEntity>> = emptyFlow()
        override fun getTracksForArtist(artist: String): Flow<List<TrackEntity>> = emptyFlow()
        override fun getTracksForGenre(genre: String): Flow<List<TrackEntity>> = emptyFlow()
        override fun getAllGenres(): Flow<List<String>> = emptyFlow()
        override fun searchTracks(query: String): Flow<List<TrackEntity>> = emptyFlow()
        override suspend fun insertOrUpdateTracks(tracks: List<TrackEntity>) {}
        override suspend fun insertOrUpdateTrack(track: TrackEntity) {}
        override suspend fun updateTrack(track: TrackEntity) {}
        override suspend fun setFavorite(id: String, isFavorite: Boolean) {}
        override suspend fun recordTrackPlayed(id: String, timestamp: Long) {}
        override suspend fun setAvailability(id: String, isAvailable: Boolean) {}
        override suspend fun markMissingTracks(activePaths: List<String>) {}
        override fun getTrackCount(): Flow<Int> = emptyFlow()
        override suspend fun deleteTrack(id: String) {}
    }

    private fun createSampleTrack(id: String, title: String, artist: String): TrackEntity {
        val track = TrackEntity(
            id = id,
            fingerprint = id,
            uri = "content://media/$id",
            path = "/storage/emulated/0/Music/$title.mp3",
            title = title,
            artist = artist,
            album = "Sample Album",
            mimeType = "audio/mpeg",
            format = "MP3",
            durationMs = 180000L
        )
        tracksDatabase[id] = track
        return track
    }

    @Before
    fun setup() {
        storedQueue.clear()
        tracksDatabase.clear()
    }

    @Test
    fun testSetQueue_and_QueueNavigation() = runTest {
        val manager = QueueManager(fakeQueueDao, fakeTrackDao, this)
        val track1 = createSampleTrack("t1", "Track 1", "Artist 1")
        val track2 = createSampleTrack("t2", "Track 2", "Artist 2")
        val track3 = createSampleTrack("t3", "Track 3", "Artist 3")

        manager.setQueue(listOf(track1, track2, track3), startIndex = 0)
        assertEquals(3, manager.queueState.value.tracks.size)
        assertEquals(0, manager.queueState.value.currentIndex)
        assertEquals(track1, manager.queueState.value.currentTrack)

        // Skip to next
        val next = manager.skipToNext()
        assertEquals(track2, next)
        assertEquals(1, manager.queueState.value.currentIndex)

        // Skip to next again
        val next2 = manager.skipToNext()
        assertEquals(track3, next2)
        assertEquals(2, manager.queueState.value.currentIndex)

        // At end with repeat off, hasNext is false and skipToNext returns null
        assertFalse(manager.queueState.value.hasNext)
        val end = manager.skipToNext()
        assertNull(end)

        // Skip to previous
        val prev = manager.skipToPrevious()
        assertEquals(track2, prev)
        assertEquals(1, manager.queueState.value.currentIndex)
    }

    @Test
    fun testQueuePersistence_saveAndRestoreRoundTrip() = runTest {
        val manager1 = QueueManager(fakeQueueDao, fakeTrackDao, this)
        val t1 = createSampleTrack("p1", "Song A", "Band 1")
        val t2 = createSampleTrack("p2", "Song B", "Band 2")
        val t3 = createSampleTrack("p3", "Song C", "Band 3")

        manager1.setQueue(listOf(t1, t2, t3), startIndex = 1)
        manager1.saveQueue()

        assertEquals("Queue should have 3 items persisted", 3, storedQueue.size)
        assertTrue("Item 1 should be marked as current", storedQueue[1].isCurrent)
        assertFalse("Item 0 should not be current", storedQueue[0].isCurrent)

        // Create a new manager instance simulating application restart
        val manager2 = QueueManager(fakeQueueDao, fakeTrackDao, this)
        testScheduler.advanceUntilIdle()

        assertEquals(3, manager2.queueState.value.tracks.size)
        assertEquals(1, manager2.queueState.value.currentIndex)
        assertEquals("Song B", manager2.queueState.value.currentTrack?.title)
    }

    @Test
    fun testEnqueueNext_and_EnqueueToEnd() = runTest {
        val manager = QueueManager(fakeQueueDao, fakeTrackDao, this)
        val t1 = createSampleTrack("1", "Song 1", "Artist")
        val t2 = createSampleTrack("2", "Song 2", "Artist")
        val tNext = createSampleTrack("next", "Up Next Song", "Artist")
        val tEnd = createSampleTrack("end", "End Song", "Artist")

        manager.setQueue(listOf(t1, t2), startIndex = 0)
        manager.enqueueNext(tNext)

        // tNext must be at index 1 (directly after current index 0)
        assertEquals(3, manager.queueState.value.tracks.size)
        assertEquals("next", manager.queueState.value.tracks[1].id)
        assertEquals("2", manager.queueState.value.tracks[2].id)

        // Enqueue to end
        manager.enqueueToEnd(listOf(tEnd))
        assertEquals(4, manager.queueState.value.tracks.size)
        assertEquals("end", manager.queueState.value.tracks.last().id)
    }

    @Test
    fun testMoveTrack_and_RemoveTrack() = runTest {
        val manager = QueueManager(fakeQueueDao, fakeTrackDao, this)
        val t1 = createSampleTrack("1", "Song 1", "Artist")
        val t2 = createSampleTrack("2", "Song 2", "Artist")
        val t3 = createSampleTrack("3", "Song 3", "Artist")

        manager.setQueue(listOf(t1, t2, t3), startIndex = 0)

        // Move item from 2 to 0
        manager.moveTrack(fromIndex = 2, toIndex = 0)
        assertEquals("3", manager.queueState.value.tracks[0].id)
        assertEquals("1", manager.queueState.value.tracks[1].id)
        assertEquals("2", manager.queueState.value.tracks[2].id)
        // Current index was 0 (Song 1), now Song 1 is at index 1
        assertEquals(1, manager.queueState.value.currentIndex)

        // Remove item at index 0 (Song 3)
        manager.removeTrackAt(0)
        assertEquals(2, manager.queueState.value.tracks.size)
        assertEquals("1", manager.queueState.value.tracks[0].id)
        assertEquals(0, manager.queueState.value.currentIndex)
    }

    @Test
    fun testShuffleToggle_preservesCurrentTrackAndRestoresOrder() = runTest {
        val manager = QueueManager(fakeQueueDao, fakeTrackDao, this)
        val tracks = (1..5).map { createSampleTrack("id_$it", "Song $it", "Artist") }
        manager.setQueue(tracks, startIndex = 2) // Playing Song 3

        assertEquals("Song 3", manager.queueState.value.currentTrack?.title)

        // Toggle shuffle on
        manager.toggleShuffle()
        assertTrue(manager.queueState.value.isShuffleEnabled)
        assertEquals(0, manager.queueState.value.currentIndex)
        assertEquals("Current playing track must stay at head of shuffled queue", "Song 3", manager.queueState.value.currentTrack?.title)

        // Toggle shuffle off
        manager.toggleShuffle()
        assertFalse(manager.queueState.value.isShuffleEnabled)
        assertEquals("Original order must be restored", tracks.map { it.id }, manager.queueState.value.tracks.map { it.id })
        assertEquals("Index must point to Song 3 in original list", 2, manager.queueState.value.currentIndex)
    }

    @Test
    fun testRepeatModeCycle() = runTest {
        val manager = QueueManager(fakeQueueDao, fakeTrackDao, this)
        assertEquals(RepeatMode.OFF, manager.queueState.value.repeatMode)

        manager.cycleRepeatMode()
        assertEquals(RepeatMode.ALL, manager.queueState.value.repeatMode)

        manager.cycleRepeatMode()
        assertEquals(RepeatMode.ONE, manager.queueState.value.repeatMode)

        manager.cycleRepeatMode()
        assertEquals(RepeatMode.OFF, manager.queueState.value.repeatMode)
    }
}
