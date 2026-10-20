package com.liquidglass.music.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class LyricSourceType(val priority: Int) {
    USER_EDITED(1),
    USER_IMPORTED(2),
    EMBEDDED(3),
    SIDECAR(4),
    INSTRUMENTAL(5),
    NONE(6);

    fun hasHigherPriorityThan(other: LyricSourceType): Boolean {
        return this.priority < other.priority
    }
}

@Entity(
    tableName = "lyrics",
    indices = [Index("trackId", unique = true)]
)
data class LyricRecordEntity(
    @PrimaryKey
    val trackId: String,
    val sourceType: String, // String representation of LyricSourceType
    val rawContent: String, // Full LRC format or plain text
    val isSynced: Boolean = false,
    val isInstrumental: Boolean = false,
    val offsetMs: Long = 0L, // User custom timing offset
    val updatedAt: Long = System.currentTimeMillis(),
    val sourcePath: String? = null // For sidecar or user-imported files
)

@Entity(
    tableName = "playback_queue",
    indices = [Index("positionInQueue")]
)
data class QueueItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackId: String,
    val positionInQueue: Int,
    val isCurrent: Boolean = false
)
