# Google Play Data Safety Questionnaire Guide for Glassoplayer

This document provides exact, verified answers for completing the **Data safety** questionnaire in the Google Play Console for **Glassoplayer**.

---

## 1. Data Collection and Sharing Overview

| Question in Play Console | Verified Answer | Explanation |
|---|---|---|
| **Does your app collect or share any of the required user data types?** | **No** | Glassoplayer is 100% offline. `android.permission.INTERNET` is not requested. No telemetry or analytics exist. |
| **Is all of the user data collected by your app encrypted in transit?** | **N/A** (No data collected or transmitted) | No data leaves the device; zero network calls. |
| **Do you provide a way for users to request that their data be deleted?** | **Yes** | Users can delete all playlists, lyric edits, and database indices directly within the app's Settings screen via "Clear Cache & Rebuild Index" or via Android System Settings -> Clear Data. |

---

## 2. Category-by-Category Declaration

### Location
- **Approximate location**: Not collected / Not shared.
- **Precise location**: Not collected / Not shared.

### Personal info
- **Name, Email, User IDs, Address, Phone number, Race, Political/Religious beliefs, Sexual orientation, Other info**: Not collected / Not shared.

### Financial info
- **Payment info, Purchase history, Credit score, Other financial info**: Not collected / Not shared.

### Health and fitness
- **Health info, Fitness info**: Not collected / Not shared.

### Messages
- **Emails, SMS, Other in-app messages**: Not collected / Not shared.

### Photos and videos
- **Photos, Videos**: Not collected / Not shared.

### Audio files
- **Voice or sound recordings, Music files, Other audio files**:
  - **Shared with third parties**: **No**.
  - **Collected (transmitted off-device)**: **No**.
  - *Note for Play Console*: While the app accesses local audio files via `READ_MEDIA_AUDIO` / `READ_EXTERNAL_STORAGE` to decode and play them locally on device, this is strictly **on-device processing** and does not constitute "Data Collection" under Google Play's definition (which requires data to be sent off the user's device).

### Files and docs
- **Files and docs**: Not collected / Not shared.

### Calendar
- **Calendar events**: Not collected / Not shared.

### Contacts
- **Contacts**: Not collected / Not shared.

### App activity
- **App interactions, In-app search history, Installed apps, Other user-generated content, Other actions**: Not collected / Not shared.

### Web browsing
- **Web browsing history**: Not collected / Not shared.

### App info and performance
- **Crash logs, Diagnostics, Other app performance data**: Not collected / Not shared.

### Device or other IDs
- **Device or other IDs**: Not collected / Not shared.

---

## 3. System Cloud Backups Note

Under Google Play policies, standard Android OS cloud backups (encrypted by Android OS into the user's personal Google Drive account) do not need to be declared as developer data collection, because the data is processed solely by the Android operating system and is governed by the user's personal Google account settings.

---

## 4. Privacy Policy URL

Submit the hosted HTTPS Privacy Policy URL in the Play Console under **Store presence > App content > Privacy policy**:
```
https://github.com/louicastro/glassoplayer/blob/main/PRIVACY_POLICY.md
```
