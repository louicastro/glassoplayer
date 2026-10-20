package com.liquidglass.music.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.liquidglass.music.data.model.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks WHERE isAvailable = 1 ORDER BY title COLLATE NOCASE ASC")
    fun getAllAvailableTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isAvailable = 1 ORDER BY dateAdded DESC")
    fun getRecentlyAddedTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isAvailable = 1 AND playCount > 0 ORDER BY lastPlayedTimestamp DESC LIMIT :limit")
    fun getRecentlyPlayedTracks(limit: Int = 50): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isAvailable = 1 AND playCount > 0 ORDER BY playCount DESC LIMIT :limit")
    fun getFrequentlyPlayedTracks(limit: Int = 50): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isAvailable = 1 AND isFavorite = 1 ORDER BY title COLLATE NOCASE ASC")
    fun getFavoriteTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: String): TrackEntity?

    @Query("SELECT * FROM tracks WHERE fingerprint = :fingerprint LIMIT 1")
    suspend fun getTrackByFingerprint(fingerprint: String): TrackEntity?

    @Query("SELECT * FROM tracks WHERE uri = :uri LIMIT 1")
    suspend fun getTrackByUri(uri: String): TrackEntity?

    @Query("SELECT * FROM tracks WHERE path = :path LIMIT 1")
    suspend fun getTrackByPath(path: String): TrackEntity?

    @Query("SELECT * FROM tracks WHERE album = :album AND isAvailable = 1 ORDER BY discNumber ASC, trackNumber ASC, title ASC")
    fun getTracksForAlbum(album: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE artist = :artist AND isAvailable = 1 ORDER BY album ASC, discNumber ASC, trackNumber ASC")
    fun getTracksForArtist(artist: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE genre = :genre AND isAvailable = 1 ORDER BY title COLLATE NOCASE ASC")
    fun getTracksForGenre(genre: String): Flow<List<TrackEntity>>

    @Query("SELECT DISTINCT genre FROM tracks WHERE isAvailable = 1 AND genre IS NOT NULL AND genre != '' ORDER BY genre ASC")
    fun getAllGenres(): Flow<List<String>>

    @Query("SELECT * FROM tracks WHERE isAvailable = 1 AND (title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%' OR composer LIKE '%' || :query || '%' OR genre LIKE '%' || :query || '%' OR path LIKE '%' || :query || '%') ORDER BY title COLLATE NOCASE ASC")
    fun searchTracks(query: String): Flow<List<TrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTracks(tracks: List<TrackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTrack(track: TrackEntity)

    @Update
    suspend fun updateTrack(track: TrackEntity)

    @Query("UPDATE tracks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE tracks SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp WHERE id = :id")
    suspend fun recordTrackPlayed(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE tracks SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun setAvailability(id: String, isAvailable: Boolean)

    @Query("UPDATE tracks SET isAvailable = 0 WHERE path NOT IN (:activePaths)")
    suspend fun markMissingTracks(activePaths: List<String>)

    @Query("SELECT COUNT(*) FROM tracks WHERE isAvailable = 1")
    fun getTrackCount(): Flow<Int>

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrack(id: String)
}
