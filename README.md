# ByteGhost: The Synthex Files — Native Android Game

ByteGhost: The Synthex Files is a story-driven cyberpunk Python-learning game set in 2142. The production application is built natively for Android using Kotlin and Jetpack Compose.

## Android application

- **Launcher title:** ByteGhost: The Synthex Files
- **Application ID:** `com.neonull.protocol` (kept unchanged for update compatibility)
- **Entry point:** `app/src/main/java/com/neonull/protocol/MainActivity.kt`
- **Campaign and curriculum:** `data/CurriculumData.kt`
- **Gameplay state, mission evaluation, progress and drafts:** `viewmodel/LearningViewModel.kt`
- **Python syntax highlighting:** `syntax/SyntaxHighlighter.kt`
- **Screens:** onboarding, Story Hub, theory viewer and terminal learning
- **Audio:** `audio/CyberAudioEngine.kt`
- **Localization:** English, Bahasa Indonesia and Filipino
- **UI:** Jetpack Compose / Material 3

## Build APK

GitHub Actions automatically builds a debug APK when changes are pushed to `main`.

To build manually, run **Actions → Build Android APK → Run workflow** and choose `debug` or `release`. The workflow uses JDK 17, Android SDK 35 and Gradle 8.11.1. The APK file and workflow artifact use the `ByteGhost-The-Synthex-Files` name prefix.

## Project scope

This repository is dedicated to the native Android application. The former React/Vite web prototype, Pyodide web runtime, Node server and web-only tests have been removed. Android is the source of truth for gameplay and runtime development.
