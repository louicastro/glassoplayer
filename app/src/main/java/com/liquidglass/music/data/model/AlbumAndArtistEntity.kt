package com.liquidglass.music.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey
    val id: String, // Normalized "album_artist" key
    val title: String,
    val artist: String,
    val trackCount: Int = 0,
    val year: Int? = null,
    val artworkUri: String? = null
)

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey
    val id: String, // Normalized artist name key
    val name: String,
    val trackCount: Int = 0,
    val albumCount: Int = 0
)
