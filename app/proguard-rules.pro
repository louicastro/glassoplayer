# ProGuard rules for Glassoplayer

# AndroidX Media3 / ExoPlayer
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.session.** { *; }
-keep class androidx.media3.common.** { *; }
-keep class androidx.media3.extractor.** { *; }
-dontwarn androidx.media3.**

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep class com.liquidglass.music.data.dao.** { *; }
-keep class com.liquidglass.music.data.model.** { *; }
-dontwarn androidx.room.paging.**

# Kotlin Coroutines
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# Keep Application and Service components
-keep class com.liquidglass.music.LiquidMusicApplication { *; }
-keep class com.liquidglass.music.service.PlaybackService { *; }
-keep class com.liquidglass.music.MainActivity { *; }
