# ByteGhost: The Synthex Files

ByteGhost: The Synthex Files is a story-driven cyberpunk Python-learning game set in 2142. It is a native Android application built with Kotlin, Jetpack Compose, and CPython 3.13.

## Android application

- **Launcher title:** ByteGhost: The Synthex Files
- **Application ID:** `com.neonull.protocol`
- **Entry point:** `app/src/main/java/com/neonull/protocol/MainActivity.kt`
- **Campaign and curriculum:** `app/src/main/java/com/neonull/protocol/data/CurriculumData.kt`
- **Gameplay state, mission grading results, progress and drafts:** `app/src/main/java/com/neonull/protocol/viewmodel/LearningViewModel.kt`
- **Learning-phase content:** `app/src/main/java/com/neonull/protocol/data/LessonSequence.kt`
- **Python syntax highlighting:** `app/src/main/java/com/neonull/protocol/syntax/SyntaxHighlighter.kt`
- **Screens:** onboarding, Story Hub, theory viewer and terminal learning
- **Audio:** `app/src/main/java/com/neonull/protocol/audio/CyberAudioEngine.kt`
- **Localization:** English, Bahasa Indonesia and Filipino
- **UI:** Jetpack Compose / Material 3

## Embedded CPython runner

- CPython 3.13 (Chaquopy) is packaged in the **same APK** as the game.
- The application package is `com.neonull.protocol`; there is no separately installable Runner APK.
- `PythonRunnerService` runs in the secondary process `:execution` to keep execution work away from the UI process and allow process recovery.
- The service shares the application's Android UID. A separate process is **not** a security sandbox and does not isolate Python from the app's private files or permissions.
- The service is non-exported and the game communicates with it through an in-app Messenger/Binder contract.
- Source, stdin, stdout/stderr and execution time have limits. These controls are defense-in-depth, not a guarantee that arbitrary hostile Python is safe.

See [Execution Security](docs/EXECUTION_SECURITY.md) for the actual boundary and outstanding risks.

## Build

The project builds one debug APK:

```bash
gradle :app:assembleDebug
```

The APK is emitted under `app/build/outputs/apk/debug/`. GitHub Actions verifies that exactly one APK is produced and that its package ID is `com.neonull.protocol`.

## Tests

Python helper and grader tests:

```bash
PYTHONPATH=app/src/main/python python -m unittest discover -s tests/python -v
```

Android instrumentation tests require a connected device or emulator:

```bash
gradle :app:connectedDebugAndroidTest
```

The combined helper, instrumentation, and log-diagnostics workflow is available at `.github/scripts/run-runtime-instrumentation.sh`. The regular push workflow intentionally performs the faster APK build and identity check; it does not claim that the full runtime suite passed.

## Project scope

This repository contains the native Android game and its embedded CPython runtime. Android is the source of truth for gameplay and runtime development. The former web prototype and its web-only runtime are not part of the active application.
