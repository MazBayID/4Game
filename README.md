# ByteGhost: The Synthex Files — Native Android Game

ByteGhost: The Synthex Files is a story-driven cyberpunk Python-learning game set in 2142. The game is built natively for Android using Kotlin and Jetpack Compose, with CPython 3.13 packaged in a separate companion APK.

## Android application

- **Launcher title:** ByteGhost: The Synthex Files
- **Application ID:** `com.neonull.protocol` (kept unchanged for update compatibility)
- **Entry point:** `app/src/main/java/com/neonull/protocol/MainActivity.kt`
- **Campaign and curriculum:** `app/src/main/java/com/neonull/protocol/data/CurriculumData.kt`
- **Gameplay state, mission grading results, progress and drafts:** `app/src/main/java/com/neonull/protocol/viewmodel/LearningViewModel.kt`
- **Python syntax highlighting:** `app/src/main/java/com/neonull/protocol/syntax/SyntaxHighlighter.kt`
- **Screens:** onboarding, Story Hub, theory viewer and terminal learning
- **Audio:** `app/src/main/java/com/neonull/protocol/audio/CyberAudioEngine.kt`
- **Localization:** English, Bahasa Indonesia and Filipino
- **UI:** Jetpack Compose / Material 3

## Isolated CPython runner

- Game APK: `com.neonull.protocol`
- Runner APK: `com.neonull.protocol.runner`
- CPython 3.13 is packaged in the runner APK, not the game APK.
- Mission grading and free-play execution run through the companion service under a separate Android UID.
- Install and update both APKs from the same build artifact. They must share the same signing certificate.
- The runner has bounded source, stdin, stdout/stderr, a temporary workspace and a five-second execution watchdog.

This is a practical isolation boundary, not a perfect sandbox. See [Execution Security](docs/EXECUTION_SECURITY.md) for architecture, limits and validation status.

## Build APKs

GitHub Actions builds both debug APKs and publishes them together as one artifact. Download both files from the same workflow run:

- `ByteGhost-The-Synthex-Files-debug.apk`
- `ByteGhost-Python-Runner-debug.apk`

Install both on the Android device. Updating only one APK can leave incompatible versions installed.

To build manually, use JDK 17, Android SDK 35 and Gradle 8.11.1, then run:

```bash
gradle assembleDebug
```

The game and runner APKs are emitted under their respective `app/build/outputs/apk/debug` and `runner/build/outputs/apk/debug` directories.

## Project scope

This repository is dedicated to the native Android application and its companion CPython runner. The former React/Vite web prototype, Pyodide web runtime, Node server and web-only tests have been removed. Android is the source of truth for gameplay and runtime development.
