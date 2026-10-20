package com.liquidglass.music.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liquidglass.music.data.model.AlbumEntity
import com.liquidglass.music.data.model.ArtistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums ORDER BY title COLLATE NOCASE ASC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :id LIMIT 1")
    suspend fun getAlbumById(id: String): AlbumEntity?

    @Query("SELECT * FROM albums WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' ORDER BY title COLLATE NOCASE ASC")
    fun searchAlbums(query: String): Flow<List<AlbumEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAlbums(albums: List<AlbumEntity>)

    @Query("DELETE FROM albums")
    suspend fun clearAlbums()
}

@Dao
interface ArtistDao {
    @Query("SELECT * FROM artists ORDER BY name COLLATE NOCASE ASC")
    fun getAllArtists(): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artists WHERE id = :id LIMIT 1")
    suspend fun getArtistById(id: String): ArtistEntity?

    @Query("SELECT * FROM artists WHERE name LIKE '%' || :query || '%' ORDER BY name COLLATE NOCASE ASC")
    fun searchArtists(query: String): Flow<List<ArtistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateArtists(artists: List<ArtistEntity>)

    @Query("DELETE FROM artists")
    suspend fun clearArtists()
}
