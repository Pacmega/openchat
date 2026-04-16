# CLAUDE.md

## Build Commands

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease

# Run tests (with API key)
source .env && ./gradlew test
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Requirements

- JDK 17+ (configured in `gradle.properties` via `org.gradle.java.home`)
- Android SDK (set in `local.properties` as `sdk.dir=/path/to/android-sdk`)
- Minimum Android API 29 (Android 10)

## Tech Stack

- Kotlin 2.0.21 with Compose compiler plugin (not kapt)
- Jetpack Compose (Material 3)
- Hilt for DI (using KSP, not kapt)
- Room for local storage
- Retrofit + OkHttp for networking with SSE

## Architecture

Clean Architecture:
- `ui/` — Composables, ViewModels, navigation
- `domain/` — Models, repository interfaces
- `data/` — Room entities/DAOs, Retrofit API
- `di/` — Hilt modules

## Tests

Tests live in `src/test/` mirroring the main package structure. Some tests require a real OpenRouter API key stored in `.env` (never commit this file).

## Implementation Rules

- **TDD strictly enforced:** Write failing tests first, then implement. This applies to all changes — bug fixes, refactors, new features. Before any change, check if tests exist; if not, write them first and confirm they fail before proceeding.
- **Compose:** Add `@Preview` to all UI components. Keep Composables stateless (hoist state).
- **Error handling:** Use `Result` wrapper or sealed `UiState` for network/disk operations.
- **DI:** Use `@Inject` constructor injection. Avoid `EntryPoint` unless necessary.
- **Performance:** Use `remember` and `derivedStateOf` in Compose.
- **Workarounds:** Mark hacks with `// TODO:` or `// FIXME:` explaining why.
- **Docs:** Add KDoc to public functions and classes.
