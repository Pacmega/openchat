---
description: Build & test — runs all tests (including live API tests) then builds a debug APK ready for manual installation
allowed-tools: Bash(source .env*), Bash(./gradlew*), Bash(ls*), Bash(du*)
---

# Ship

Run the full test suite, then build a debug APK. Stop and report clearly if any stage fails.

## Steps

### 1. Run all tests
```
source .env && ./gradlew testDebugUnitTest
```
- `.env` must be sourced so `OPENROUTER_API_KEY` is available for the live validation tests.
- If any test fails, print the failing test names and stop. Do not build a broken APK.

### 2. Build the debug APK
```
./gradlew assembleDebug
```
- If the build fails, print the error and stop.

### 3. Report
Print a short summary:
- How many tests ran and passed
- Full APK path: `app/build/outputs/apk/debug/app-debug.apk`
- APK file size
- Remind the user to install it manually with `adb install -r <path>`
