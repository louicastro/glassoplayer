# Privacy Policy for Glassoplayer

**Effective Date:** October 20, 2026  
**Developer:** louicastro  
**Repository:** [https://github.com/louicastro/glassoplayer](https://github.com/louicastro/glassoplayer)  
**Hosted Policy URL:** [https://github.com/louicastro/glassoplayer/blob/main/PRIVACY_POLICY.md](https://github.com/louicastro/glassoplayer/blob/main/PRIVACY_POLICY.md)

---

## 1. Introduction & Core Principle

Glassoplayer is an **offline-first local music player** for Android engineered with a strict privacy-first architecture. Our core privacy commitment is simple: **we do not collect, transmit, store on servers, track, monetize, or share your personal data, audio files, or usage habits.**

---

## 2. Zero Network Connectivity

Glassoplayer does not request or declare the `android.permission.INTERNET` permission in its Android manifest.
- The application cannot communicate with the internet, cloud servers, analytics providers, or remote endpoints.
- Glassoplayer operates completely and reliably in Airplane Mode without requiring accounts, sign-ins, subscriptions, or telemetry.

---

## 3. Permissions Requested & Operational Purpose

Glassoplayer requests only the minimal operating system permissions strictly necessary to play local audio:

| Permission | Android API Level | Exact Purpose |
|---|---|---|
| `READ_MEDIA_AUDIO` | Android 13+ (API 33+) | Scans and accesses local audio files and album artwork stored on your device to build your offline music library. |
| `READ_EXTERNAL_STORAGE` | Android 8.0–12 (API 26–32) | Legacy storage access to discover audio files on devices running older Android versions (`maxSdkVersion=32`). |
| `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Android 9+ / Android 14+ (API 34+) | Required by Android OS to maintain continuous audio playback and system media controls when the screen is turned off or when switching apps. |
| `POST_NOTIFICATIONS` | Android 13+ (API 33+) | Displays playback notification transport controls (play, pause, skip, seek, track metadata, album art) in your notification shade and lock screen. |
| `WAKE_LOCK` | All API Levels | Prevents CPU sleep while an audio track is streaming, ensuring playback does not stutter when the screen locks. |

**No unnecessary permissions**: Glassoplayer does **not** request `MANAGE_EXTERNAL_STORAGE`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `READ_CONTACTS`, `CAMERA`, or `RECORD_AUDIO`.

---

## 4. Local Data Storage & Processing

All data processing is performed 100% on-device:
1. **Audio Index & Metadata**: Extracted directly from your audio headers (`ID3v2`, `FLAC`, `MP4`, `RIFF`) and cached in an internal SQLite Room database on your device (`liquid_music_database`).
2. **Playlists & Favorites**: Stored purely in your local database.
3. **Synchronized Lyrics**: Parsed from embedded metadata (`USLT`) or sidecar files (`.lrc`). Any edits made via the in-app Tap-to-Stamp editor are saved locally to your device database.
4. **Audio Effects & Equalizer Settings**: Stored locally in app shared preferences.

---

## 5. Android System Backup Disclosures

- **Operating System Cloud Backup**: If you have enabled Android System Cloud Backup (Google Drive Backup) on your device in your Android OS Settings, Android OS will encrypt and include app preferences, playlists, and user-edited lyrics in your personal Google Drive storage according to your Google account settings.
- **Exclusions**: Raw audio files, cached album artwork, and temporary files are explicitly excluded from cloud backup via `data_extraction_rules.xml` and `backup_rules.xml`.
- **Zero Third-Party Cloud Servers**: Glassoplayer does not manage, operate, own, or access any external cloud servers.

---

## 6. Third-Party SDKs & Analytics

Glassoplayer contains **no third-party advertising SDKs, tracking libraries, or analytics frameworks**:
- No Google Firebase Analytics
- No Crashlytics
- No Facebook SDK
- No ad networks or monetization brokers

---

## 7. Children’s Privacy

Because Glassoplayer does not collect personal data from any user, it does not collect information from children under the age of 13 (or under applicable age limits in your jurisdiction).

---

## 8. Changes to This Privacy Policy

If this Privacy Policy is updated, the revision will be committed directly to this repository with an updated effective date.

---

## 9. Contact & Support

For questions, audit reports, or feedback regarding privacy or security in Glassoplayer, please open an issue on GitHub:
[https://github.com/louicastro/glassoplayer/issues](https://github.com/louicastro/glassoplayer/issues)
