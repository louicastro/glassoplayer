package com.liquidglass.music.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.liquidglass.music.data.dao.AlbumDao
import com.liquidglass.music.data.dao.ArtistDao
import com.liquidglass.music.data.dao.LyricDao
import com.liquidglass.music.data.dao.PlaylistDao
import com.liquidglass.music.data.dao.QueueDao
import com.liquidglass.music.data.dao.TrackDao
import com.liquidglass.music.data.model.AlbumEntity
import com.liquidglass.music.data.model.ArtistEntity
import com.liquidglass.music.data.model.LyricRecordEntity
import com.liquidglass.music.data.model.PlaylistEntity
import com.liquidglass.music.data.model.PlaylistItemEntity
import com.liquidglass.music.data.model.QueueItemEntity
import com.liquidglass.music.data.model.TrackEntity

@Database(
    entities = [
        TrackEntity::class,
        AlbumEntity::class,
        ArtistEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        LyricRecordEntity::class,
        QueueItemEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class LiquidMusicDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun lyricDao(): LyricDao
    abstract fun queueDao(): QueueDao

    companion object {
        const val DATABASE_NAME = "liquid_music.db"

        @Volatile
        private var instance: LiquidMusicDatabase? = null

        fun getInstance(context: Context): LiquidMusicDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LiquidMusicDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(*MIGRATIONS)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                    .also { instance = it }
            }
        }

        /**
         * List of explicit Room migrations for safe schema evolution without data loss.
         */
        val MIGRATIONS: Array<Migration> = arrayOf(
            // Future schema migrations will be added here
        )
    }
}
