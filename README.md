# People DB

A mobile-first, offline Android app for keeping track of people: relationships,
places they've lived (with year ranges), birthdays, freeform tags, timestamped
notes, and photos. Everything is stored locally in a Room (SQLite) database and
can be backed up to / restored from a single `.zip` file you choose the location
of (local storage, SD card, or a synced cloud folder).

## Features

- **People**: name, birthday (with an "unknown year" option), one or more photos.
- **Relationships**: link people to each other with a free-text relationship type
  (sibling, friend, colleague, etc).
- **Places**: tag a person with a place and an optional "from year" / "to year"
  range (e.g. lived in Berlin 2015–2019). Places are just a special kind of tag,
  so they show up in the tag browser too.
- **Tags**: freeform tags for anything else you want to track.
- **Notes**: timestamped notes per person, shown as relative time
  ("3 months ago", "1 year and 4 months ago", etc), like the request asked for.
- **Search**: search people by name, and browse/search all tags & places to see
  everyone attached to a given tag.
- **Backup & Restore**: exports the database and all photos into one `.zip` file
  via the Android system file picker (so you can save it anywhere — local
  storage, SD card, Google Drive/Dropbox folder, etc), and can restore from that
  same file later (including onto a different device).

## Building with GitHub Actions (no computer required)

1. Create a new **public or private** repository on GitHub (the mobile GitHub
   app or github.com in a phone browser both work).
2. Upload every file/folder from this project into the repo, preserving the
   folder structure (the `.github/workflows/build.yml` file is what makes this
   work — make sure it ends up at that exact path).
3. Go to the repo's **Actions** tab. A workflow called "Build APK" will run
   automatically on every push to `main` (or trigger it manually with
   "Run workflow").
4. When it finishes (a few minutes), open the workflow run, scroll to
   **Artifacts**, and download `PeopleDB-debug-apk`. It's a zip containing
   `app-debug.apk`.
5. On your phone: unzip it (most file managers can do this, or use a Files app),
   tap `app-debug.apk`, and allow "install unknown apps" for your browser/files
   app when prompted. That's it — the app installs like any other.

Every time you push a change (e.g. edit a file in the GitHub web editor —
press `.` on the repo page to open github.dev), the workflow re-runs and
produces a fresh APK.

## Building locally (if you get access to a machine with Gradle)

```
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`. The Gradle
wrapper jar isn't checked into this repo (see below), so run
`gradle wrapper --gradle-version 8.9` once first if `gradlew` isn't present,
or just let the GitHub Actions workflow do it for you.

## Notes on the project

- Minimum Android version: Android 8.0 (API 26). This keeps the launcher icon
  and permission model simple; nearly all phones in use today are newer than
  this.
- No internet permission is requested and the app makes no network calls —
  it's fully offline.
- Photo picking uses the modern Android Photo Picker, which doesn't require
  any storage permission.
- The Gradle wrapper `.jar` binary is intentionally not committed; the CI
  workflow generates it fresh on every run with `gradle wrapper --gradle-version 8.9`,
  which avoids shipping an unverifiable binary in the repo.
