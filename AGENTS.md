# AGENTS.md

## Build Commands

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease
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

Clean Architecture with:
- `ui/` - Composables, ViewModels, navigation
- `domain/` - Models, repository interfaces
- `data/` - Room entities/DAOs, Retrofit API
- `di/` - Hilt modules

## No Tests

Project has no test sources. Run with `--continue` to bypass test tasks if needed:
```bash
./gradlew assembleDebug -x test -x testDebugUnitTest
```

## Implementation Rules

- **Compose:** Use `@Preview` for all UI components. Keep Composables stateless by hoisting state.
- **Error Handling:** Use `Result` wrapper or sealed `UiState` class for network/disk operations.
- **DI:** Use `@Inject` constructor injection. Avoid `EntryPoint` unless necessary.
- **Performance:** Always use `remember` and `derivedStateOf` in Compose.

## Code Quality

- **Static Analysis:** Code should follow detekt/ktlint standards (not currently enforced).
- **Documentation:** Add KDoc comments to public functions/classes.
- **Workarounds:** Mark hacks with `// TODO:` or `// FIXME:` explaining why.