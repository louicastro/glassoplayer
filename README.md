# Glassoplayer (iOS 26-Inspired Offline Music Player for Android)

An offline-first music player engineered with an iOS 26-inspired Liquid Glass design language, native Android Media3 foreground playback, real-time synchronized lyrics (embedded, sidecar, imported, and user-edited), multi-band equalizer, and queue persistence.

---

## Target Platform & Rationale

- **Target Platform**: Android (API 26+ / Android 8.0 Oreo up to Android 16 / API 36)
- **SDK Levels**: `compileSdk = 36`, `targetSdk = 36`, `minSdk = 26`
- **Rationale**: Mandated by project specifications and Google Play compatibility standards. The Android ecosystem provides hardware audio decode capabilities, `Media3` (`ExoPlayer`) integration with system media sessions, background audio services (`MediaSessionService`), and hardware-accelerated blur via `RenderEffect` (API 31+) with graceful translucent bevel fallback on earlier versions.
- **Language & Frameworks**: Kotlin 2.0.20, Jetpack Compose with Material 3, Media3 1.4.1, Room 2.6.1 with KSP, AndroidX Palette, Coil Compose, and Kotlinx Coroutines.

---

## Architecture Overview

```
                      ┌────────────────────────────────────────┐
                      │             MainActivity               │
                      │  (Liquid Glass Shell & Navigation UI)  │
                      └──────────────────┬─────────────────────┘
                                         │
             ┌───────────────────────────┼───────────────────────────┐
             ▼                           ▼                           ▼
    ┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
    │  MiniPlayer &   │         │ Synced Lyrics   │         │ Equalizer &     │
    │ NowPlayingSheet │         │ & Editor Engine │         │ Audio Effects   │
    └────────┬────────┘         └────────┬────────┘         └────────┬────────┘
             │                           │                           │
             └───────────────────────────┼───────────────────────────┘
                                         ▼
                      ┌────────────────────────────────────────┐
                      │            MediaController             │
                      │         (UI to Service Bridge)         │
                      └──────────────────┬─────────────────────┘
                                         ▼
                      ┌────────────────────────────────────────┐
                      │    PlaybackService (Foreground Svc)    │
                      │  - MediaSession & ExoPlayer            │
                      │  - QueueManager & Room Persistence     │
                      │  - SleepTimer (Audio Fade-Out)         │
                      │  - AudioEffectsController              │
                      └──────────────────┬─────────────────────┘
                                         │
                   ┌─────────────────────┴─────────────────────┐
                   ▼                                           ▼
       ┌────────────────────────┐                  ┌────────────────────────┐
       │   Room Local Database  │                  │  Local Storage / SAF   │
       │  (Tracks, Lyrics, Q)   │                  │  (Audio & Sidecar LRC) │
       └────────────────────────┘                  └────────────────────────┘
```

---

## Build, Test, and Run Commands

The project is built with Gradle 8.7 and OpenJDK 21.

### 1. Build Debug APK
```bash
./gradlew assembleDebug
```
*Output: `app/build/outputs/apk/debug/app-debug.apk`*

### 2. Run Full Unit Test Suite
```bash
./gradlew test
```
*Runs all unit test suites (`Stage1LibraryScannerTest`, `Stage2QueuePersistenceTest`, `Stage4LyricsTest`, `Stage5to8AudioAndEffectsTest`).*

### 3. Run Debug Unit Tests Specifically
```bash
./gradlew testDebugUnitTest
```

### 4. Run Lint Checks
```bash
./gradlew lintDebug
```

### 5. Install on Connected Device / Emulator
```bash
./gradlew installDebug
```

---

## Permissions, Manifest Entries, and Background Capabilities

Declared in `app/src/main/AndroidManifest.xml`:

| Permission / Capability | API Level | Purpose |
|-------------------------|-----------|---------|
| `READ_MEDIA_AUDIO` | API 33+ | Grants permission to scan and index local audio files via MediaStore. |
| `READ_EXTERNAL_STORAGE` | API 26-32 | Grants audio read access on pre-Android 13 devices. |
| `FOREGROUND_SERVICE` | API 28+ | Required to run foreground services for background execution. |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | API 34+ | Explicit Android 14+ foreground service type for media playback. |
| `POST_NOTIFICATIONS` | API 33+ | Required to display playback controls in system notifications. |
| `WAKE_LOCK` | All | Keeps the CPU active during audio playback with screen locked. |

### Manifest Service Registration:
```xml
<service
    android:name=".service.PlaybackService"
    android:exported="true"
    android:foregroundServiceType="mediaPlayback">
    <intent-filter>
        <action android:name="androidx.media3.session.MediaSessionService" />
        <action android:name="android.media.browse.MediaBrowserService" />
    </intent-filter>
</service>
```

---

## Audio Codec Matrix

| Format / Container | Extension | Verification Status | Notes |
|--------------------|-----------|---------------------|-------|
| **MP3** | `.mp3` | **Verified** | MPEG Audio Layer III, ID3v1, ID3v2.2-v2.4 (USLT / APIC) verified by test suite. |
| **FLAC** | `.flac` | **Verified** | Free Lossless Audio Codec, native ExoPlayer stream decode verified. |
| **WAV** | `.wav` | **Verified** | RIFF PCM 16/24-bit header parsing verified. |
| **AAC / M4A** | `.m4a`, `.aac` | **Verified** | Advanced Audio Coding in MP4 container (`ftyp`) verified. |
| **ALAC** | `.m4a` | **Verified** | Apple Lossless in MP4 container natively decoded by Media3. |
| **AIFF** | `.aiff`, `.aif` | **Verified** | Audio Interchange File Format header probing verified. |
| **Ogg Vorbis** | `.ogg`, `.oga` | **Verified** | `OggS` container probing and Vorbis comment extraction verified. |
| **Opus** | `.opus` | **Verified** | Low-latency audio codec in OGG container verified. |
| **MKV / WebM** | `.mkv`, `.webm`| **Verified** | EBML container header probing verified. |
| **WavPack** | `.wv` | Supported (Platform) | Handled by Media3 Extractor where platform decoders exist. |
| **APE (Monkey's Audio)**| `.ape` | Supported (Platform) | Recognized by scanner and container parser. |
| **AC-3 / E-AC-3** | `.ac3`, `.eac3` | Supported (Platform)| Dolby Digital pass-through/decode where hardware provides decoder. |
| **Speex / AMR** | `.spx`, `.amr` | Supported (Platform)| Voice codecs supported by Android OpenCore/Stagefright decoders. |
| **DRM-Protected** | Various | Rejected Gracefully | DRM files are flagged as unplayable with clear error state; never crash. |

---

## Implemented Features

### 1. Liquid Glass Interface
- **Translucent Surfaces**: 65%-80% opacity tinting with adaptive dark and light color palettes.
- **Glass Bevel & Reflection**: Vertical specular border gradients (`0.5-1.0dp`) simulating angled glass refraction.
- **Android 12+ RenderEffect Blur**: Utilizes `RenderEffect.createBlurEffect()` on API 31+ with fallback to gradient diffusion on API < 31.
- **Dynamic Artwork Palette Extraction**: Real-time palette analysis using `androidx.palette` extracts vibrant dominant colors to dynamically illuminate the background and glass cards.
- **WCAG AA Compliance**: All text elements maintain contrast ratios $\ge 4.5:1$ against glass and artwork backgrounds.
- **Reduced Motion Support**: Automatically disables transition animations when Android system animator duration scale is 0.

### 2. Library Scanning & Local Database
- **Room Database**: Complete persistence with `TrackEntity`, `AlbumEntity`, `ArtistEntity`, `PlaylistEntity`, `PlaylistItemEntity`, `LyricRecordEntity`, and `QueueItemEntity`.
- **Stable Fingerprinting**: Computes SHA-256 fingerprint from the file content header + length to ensure tracks, playlists, favorites, and lyrics survive renames and folder moves.
- **Deduplication & Missing Files**: Rescans detect moved or missing files, marking them `isAvailable = false` rather than silently deleting user data.
- **MediaStore & SAF**: Scans system media store and user-selected custom directories.

### 3. Playback Engine & Queue Persistence
- **Single Source of Truth**: `PlaybackService` (`MediaSessionService`) owns the single `ExoPlayer` instance.
- **Queue Persistence**: The full queue and current playing index are automatically saved to `QueueDao` and restored on application restart.
- **Queue Operations**: Enqueue next, add to end, move/reorder, remove, shuffle (preserves current playing track at index 0 and restores original order when un-shuffled), and repeat modes (OFF, ALL, ONE).

### 4. Synchronized Lyrics Engine
- **LRC Parser**: Handles standard single timestamps `[mm:ss.xx]`, multiple timestamps per line `[00:12.00][00:24.00]Lyric`, and metadata tags `[ti:]`, `[ar:]`, `[al:]`, `[offset:]`.
- **Encoding Resilience**: Decodes UTF-8 with BOM, UTF-8 without BOM, and gracefully falls back to ISO-8859-1.
- **Binary Search Sync**: $O(\log n)$ active line lookup driven by audio engine playback position; updates UI only on line changes to prevent flicker.
- **Tap-to-Seek**: Tapping any lyric line instantly seeks audio playback to that timestamp.
- **Manual Scroll Hold**: Auto-scroll pauses when user manually scrolls, surfacing a floating "Sync to Now Playing" pill to resume tracking.
- **Tap-to-Stamp Editor**: Interactive live editor enabling users to play a track and stamp line timestamps in real time, with instant saving to the Room database.
- **Priority Resolution**: User-Edited > User-Imported > Embedded > Sidecar.

### 5. Audio Processing & System Controls
- **Equalizer**: Multi-band equalizer with presets (Flat, Bass Boost, Treble Boost, Vocal, Custom) connected to ExoPlayer's `audioSessionId`.
- **Audio Effects**: Android `BassBoost`, `Virtualizer`, and `LoudnessEnhancer` (ReplayGain / loudness normalization).
- **Pitch-Preserving Speed**: Variable speed from 0.5x to 2.0x via `PlaybackParameters`.
- **Sleep Timer**: Runs inside the background `PlaybackService` with presets (15m, 30m, 45m, 60m, end-of-track), remaining time display, and a 15-second gentle volume fade-out.
- **Interruption & Route Changes**: Automatically pauses on calls or audio focus loss, and pauses on headphone/Bluetooth disconnection (`ACTION_AUDIO_BECOMING_NOISY`).

### 6. Privacy & Offline Architecture
- **Zero Internet Permissions**: No network permissions are declared in `AndroidManifest.xml`.
- **No Telemetry**: No analytics, trackers, or cloud dependencies. 100% operational in Airplane Mode.
- **Storage Management**: Clear artwork cache, clear lyric cache, and rebuild library index on demand.
