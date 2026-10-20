package com.liquidglass.music.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.liquidglass.music.data.model.LyricRecordEntity
import com.liquidglass.music.data.model.PlaylistEntity
import com.liquidglass.music.data.model.PlaylistItemEntity
import com.liquidglass.music.data.model.QueueItemEntity
import com.liquidglass.music.data.model.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY name COLLATE NOCASE ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?

    @Query("SELECT * FROM playlists WHERE name = :name LIMIT 1")
    suspend fun getPlaylistByName(name: String): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :newName, modifiedAt = :timestamp WHERE id = :playlistId")
    suspend fun renamePlaylist(playlistId: Long, newName: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistItems(items: List<PlaylistItemEntity>)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun clearPlaylistItems(playlistId: Long)

    @Query("""
        SELECT t.* FROM tracks t
        INNER JOIN playlist_items pi ON t.id = pi.trackId
        WHERE pi.playlistId = :playlistId AND t.isAvailable = 1
        ORDER BY pi.sortOrder ASC
    """)
    fun getTracksForPlaylist(playlistId: Long): Flow<List<TrackEntity>>

    @Query("SELECT COUNT(*) FROM playlist_items WHERE playlistId = :playlistId")
    fun getTrackCountForPlaylist(playlistId: Long): Flow<Int>
}

@Dao
interface LyricDao {
    @Query("SELECT * FROM lyrics WHERE trackId = :trackId LIMIT 1")
    suspend fun getLyricForTrack(trackId: String): LyricRecordEntity?

    @Query("SELECT * FROM lyrics WHERE trackId = :trackId LIMIT 1")
    fun getLyricFlowForTrack(trackId: String): Flow<LyricRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLyric(lyric: LyricRecordEntity)

    @Query("UPDATE lyrics SET offsetMs = :offsetMs, updatedAt = :timestamp WHERE trackId = :trackId")
    suspend fun updateLyricOffset(trackId: String, offsetMs: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM lyrics WHERE trackId = :trackId")
    suspend fun deleteLyricForTrack(trackId: String)

    @Query("DELETE FROM lyrics")
    suspend fun clearAllLyrics()
}

@Dao
interface QueueDao {
    @Query("SELECT * FROM playback_queue ORDER BY positionInQueue ASC")
    fun getQueueItems(): Flow<List<QueueItemEntity>>

    @Query("SELECT * FROM playback_queue ORDER BY positionInQueue ASC")
    suspend fun getQueueItemsSync(): List<QueueItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueItems(items: List<QueueItemEntity>)

    @Query("DELETE FROM playback_queue")
    suspend fun clearQueue()

    @Query("UPDATE playback_queue SET isCurrent = 0")
    suspend fun clearCurrentFlags()

    @Query("UPDATE playback_queue SET isCurrent = 1 WHERE trackId = :trackId")
    suspend fun setCurrentTrack(trackId: String)

    @Transaction
    suspend fun replaceQueue(items: List<QueueItemEntity>) {
        clearQueue()
        insertQueueItems(items)
    }
}
