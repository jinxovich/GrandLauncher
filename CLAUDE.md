# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

GrandLauncher is an Android home-screen launcher designed for elderly users. It provides a simplified 2-column grid of contacts/apps, an SOS button, battery indicator, and call screening that silently blocks unknown callers.

- **Min SDK**: 29 (Android 10) — required for `CallScreeningService` role API
- **Target SDK**: 36
- **Language**: Java (View-based UI; Compose is listed as a dependency but not used for screens)
- **Package**: `com.git_blame_mama.grandlauncher`

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build and install on connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device)
./gradlew connectedAndroidTest
```

## Architecture

### Activities
- **`MainActivity`** — Home screen. Displays a 2-column `RecyclerView` grid and battery level. The Settings button requires a **long-press** (short press shows a toast) to prevent accidental entry. Back-press is disabled (launcher behavior).
- **`SettingsActivity`** — Manages allowed contacts, main-screen grid cells, SOS number, and call screening role. Refreshes all data on every `onResume`.

### Call Screening
- **`GrandCallScreeningService`** — Extends `CallScreeningService`. On incoming calls it reads `KEY_FAST_WHITELIST` directly from SharedPreferences (`getStringSet`) without Gson, for minimal latency. Blocks calls not in the whitelist; logs the last screening result to prefs.
- The app must be granted `ROLE_CALL_SCREENING` via `RoleManager`. Status is shown as a banner in Settings.

### Data Layer
- **`PrefsManager`** — Single class for all persistence via `SharedPreferences("GrandPrefs")`. Stores:
  - `KEY_GRID_ITEMS` — JSON list of `GridItem` (Gson)
  - `KEY_ALLOWED_CONTACTS` — JSON list of `AllowedContact` (Gson); also maintains a legacy raw-number set (`KEY_WHITELIST_LEGACY`) and a fast normalized set (`KEY_FAST_WHITELIST`)
  - `KEY_LAST_SCREEN_LOG` — last call screening log line
- **`saveAllowedContacts`** uses `commit()` (synchronous) so `GrandCallScreeningService` sees changes immediately.

### Data Models
- **`GridItem`** — Three types: `CONTACT` (calls a phone number), `APP` (launches by package name), `SOS` (calls emergency number). Has `label`, `data`, `type`, `iconKey`.
- **`AllowedContact`** — Contact in the call-screening whitelist: `name`, `number`, `iconKey`.

### Phone Number Normalization
Numbers are normalized to the **last 10 digits** (strips country code and formatting) before being stored in `KEY_FAST_WHITELIST` and before comparison in `GrandCallScreeningService`. Both sides must use the same normalization — see `PrefsManager.normalizeNumber()`.

### Adapters
- **`GridAdapter`** — Main screen grid; differentiates tiles by type for color coding.
- **`AllowedContactsAdapter`** — Whitelist management list; actions: add to main screen, delete.
- **`MainMenuCellsAdapter`** — Main screen cell management list in Settings; action: delete.

### Notifications
- **`CallNotificationHelper`** — Creates notification channel `"call_screening"` and posts allowed/blocked call notifications.
