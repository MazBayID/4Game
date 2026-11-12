# NEO-NULL: PYTHON LEARNING PROTOCOL

Story-driven cyberpunk Python learning platform and Android Jetpack Compose IDE simulation set in Year 2142 against **SYNTHEX** and **AI OVERLORD**.

## Native Android Architecture (Kotlin + Jetpack Compose)

- `app/src/main/java/com/neonull/protocol/MainActivity.kt` — Edge-to-edge Compose entry point & screen router
- `app/src/main/java/com/neonull/protocol/localization/LanguageManager.kt` — Reactive `StateFlow` localization (`EN`, `ID`, `PH`)
- `app/src/main/java/com/neonull/protocol/ui/theme/Color.kt` & `Theme.kt` — Deep navy/black (`#0A0A12` / `#05050A`), Matrix Green (`#00FF66`), Cyan (`#00E5FF`), Crimson Red (`#DC143C`), monospace typography
- `app/src/main/java/com/neonull/protocol/syntax/SyntaxHighlighter.kt` — Real-time Python lexical highlighter + `VisualTransformation` + Crimson error line marking
- `app/src/main/java/com/neonull/protocol/data/CurriculumData.kt` — 6 Acts, 7 Missions localized in English, Bahasa Indonesia, and Filipino
- `app/src/main/java/com/neonull/protocol/viewmodel/LearningViewModel.kt` — Progress, RAM/XP stats, offline Python evaluator, and GLITCH hints
- `app/src/main/java/com/neonull/protocol/ui/screens/` — `OnboardingScreen.kt`, `StoryHubScreen.kt`, `TheoryViewerScreen.kt`, `TerminalLearningScreen.kt`
- `app/src/main/java/com/neonull/protocol/ui/components/CyberHudComponents.kt` — Original Compose `Canvas` chamfered HUD frames

## Building Android APK via GitHub Actions

Run `.github/workflows/build-apk.yml` manually from GitHub **Actions -> Build Android APK -> Run workflow** (`debug` or `release`) to compile the native Kotlin project with Gradle 8.11.1 + Android SDK 35 and download the `.apk` artifact.

## Interactive Web Preview & Source Inspector

```bash
npm install
npm run dev
npm run lint
npm run build
npx tsx tests/verify-neonull.ts
```
