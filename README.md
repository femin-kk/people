# PeopleApp 👥

A mobile-first Android app to store, manage, and explore your personal network — fully offline-first with optional server sync.

## Features

- **People Management** — Add, edit, delete people with name, nickname, email, phone, notes
- **Facts & Notes** — Store arbitrary facts (occupation, hobbies, where you met, etc.) per person
- **Dates & Events** — Track birthdays, last contact, when you met — displayed in relative time ("3 months ago")
- **Relationships** — Connect people with typed relationships (friend, spouse, colleague, introduced_by, etc.)
- **Search** — Fast full-text search across people, facts, and notes
- **Offline-First** — All data stored locally via Room (SQLite)
- **Background Sync** — WorkManager-powered sync to a server whenever connectivity is available

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Local DB | Room (SQLite) |
| Navigation | Compose Navigation |
| Images | Coil |
| Background Work | WorkManager |
| Networking | Retrofit + OkHttp |
| Language | Kotlin |

## Building via GitHub Actions (Recommended)

Every push to `main` or `master` automatically triggers a build.

### Steps:
1. Push this project to a GitHub repository
2. Go to **Actions** tab in your repository
3. The **"Build Android APK"** workflow runs automatically
4. When complete, click the workflow run → scroll to **Artifacts** section
5. Download:
   - `people-app-debug` — installable debug APK (ready to sideload)
   - `people-app-release-unsigned` — release APK (needs signing for Play Store)

### Manual trigger:
Go to Actions → Build Android APK → **Run workflow**

## Building Locally

### Prerequisites
- JDK 17+
- Android SDK (API 34)
- Android Studio Hedgehog or newer (recommended)

### Steps
```bash
git clone <your-repo-url>
cd PeopleApp
chmod +x gradlew
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Project Structure

```
app/src/main/java/com/peopleapp/
├── data/
│   ├── local/
│   │   ├── dao/          # Room DAOs (PersonDao, FactDao, EventDao, RelationshipDao, PhotoDao)
│   │   ├── entity/       # Room entities + mappers
│   │   └── PeopleDatabase.kt
│   ├── model/            # Domain models (Person, Fact, Event, Relationship, Photo)
│   ├── repository/       # PeopleRepository (single source of truth)
│   └── sync/             # SyncWorker (WorkManager background sync)
├── di/                   # Hilt modules (DatabaseModule, WorkManagerModule)
├── ui/
│   ├── navigation/       # NavHost + Screen routes
│   ├── screens/
│   │   ├── home/         # People list + search
│   │   ├── addperson/    # Add / edit person form
│   │   ├── persondetail/ # Detail view with Facts / Events / Relationships tabs
│   │   └── search/       # Global search screen
│   └── theme/            # Material 3 theme
├── MainActivity.kt
└── PeopleApplication.kt
```

## Server Sync (Optional)

The app is ready for sync. To connect to a server:

1. Open `app/src/main/java/com/peopleapp/data/sync/SyncWorker.kt`
2. Replace the `// TODO` comment with your API calls using Retrofit
3. On HTTP 409 conflict, store the payload on the server for manual resolution
4. Call `repository.markPersonSynced(id)` on success

## Signing a Release APK

To publish to the Play Store, sign the release APK:

```bash
# Generate a keystore (one-time)
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias

# Sign the APK
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore my-release-key.jks \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  my-key-alias
```

Or add signing config to `app/build.gradle` and use `./gradlew assembleRelease`.

## Minimum Requirements

- Android 8.0 (API 26) or higher
- ~10 MB storage for the app
- Camera permission (optional, for photos)
- Internet permission (optional, for sync)
