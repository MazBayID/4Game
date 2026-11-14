# ByteGhost Repository Audit

Audit baseline: `main` after commit `3e106461d9a1a56c7e55e6f7c56beafcecfbfe51`.

This is a static source review of the active Android application, embedded Python runtime, tests, assets, build configuration, and project documentation. It is not a claim that every runtime path has been exercised on a physical device.

## Executive summary

The repository has a coherent Android application shell and a working integration path from the terminal UI through Kotlin Binder/Messenger IPC to embedded CPython. The largest unfinished part is not APK packaging: it is the game design and gameplay model above the current module-level lesson flow.

The current playable loop is:

`Story Hub → Theory briefing → Terminal editor → CPython execution / mission grader → module-level result, reward, and dialogue → next module or Hub`

That is a useful prototype loop, but it is not yet the full intended mission experience. The six-phase curriculum exists as content data, and the world does not yet have a rich mission-scoped state model that player programs can inspect and change.

## Subsystem findings

| Subsystem | Current implementation | Assessment |
|---|---|---|
| Android shell and navigation | MainActivity routes Onboarding, Hub, Theory, and Terminal through LearningViewModel state | Functional structure; still module-centric |
| Campaign content | Seven ProtocolModule records with story, concepts, objective, starter code, hints, dialogue, rewards, and snippets | Initial content slice, not a full curriculum |
| Six-phase learning | Learn, Observe, Practice, Challenge, Operation, Mastery content in LessonSequenceRepository | Content exists; no phase navigation, persistence, or per-step grading |
| Progression | Linear unlock rules; completed modules can be replayed | Navigation arithmetic issue corrected in current batch; regression tests added |
| Python editor | Compose editor with line numbers, custom lexical highlighter, keyboard controls, snippets | Usable foundation; highlighter is not a full Python parser |
| CPython Runner | Chaquopy in the same APK; non-exported service in :execution; serialized worker and watchdog | Integrated; separate process shares app UID and is not a sandbox |
| Python execution helper | Bounded source/stdin/output, fresh globals namespace, temporary working directory | Helpful guardrails; interpreter/module state and background threads can persist |
| Mission grader | AST checks plus execution/output/state checks for seven module IDs | Narrow first version; exact AST/name assumptions reject some equivalent solutions |
| Player progression | Module completion, XP, RAM, trace integrity, language/audio preference and drafts persist | No per-phase progress or mission-world state |
| World/story response | GLITCH dialogue, status/alerts, rewards and screen effects respond to module result | Mostly presentation/reward response; no substantial simulated system state yet |
| Assets | Manifest-backed catalog, bitmap cache, sprite sheet rendering; 86 manifest entries | Asset pack is present; remaining assets are intentionally deferred |
| Localization | English, Bahasa Indonesia, Filipino UI strings and localized story content | Newer six-phase content has incomplete Filipino parity and may fall back to English |
| Audio | Procedural AudioTrack sound effects generated asynchronously | Functional design; per-event thread creation and AudioTrack cleanup on exceptional paths need lifecycle hardening |
| CI/build | One debug APK build and package identity/count verification | Fast compile/package gate; does not run full Python or Android instrumentation suites |
| Automated tests | Python runner/grader tests and Android instrumentation tests for Runner/controller, plus progression regression tests added in current batch | Useful starting coverage; full suite and physical-device tests remain release gates |

## Confirmed technical debt

### Critical — execution security

The Runner is not isolated from the app's private data. The service process shares the app UID, the Python Java bridge is available, and there is no hard memory quota. A fresh globals dictionary does not reset interpreter modules or prevent background threads from surviving. Treat arbitrary hostile code as unsupported until an actual isolation design is proven.

### High — mission and learning model

The six learning phases are not connected to the current UI flow. Their acceptance criteria are instructional text, not executable evaluation contracts. Module completion is currently the main unit of progression and persistence.

### High — evaluator correctness and extensibility

The grader is tied to seven mission IDs and checks selected AST forms and exact values/output. This is too narrow for the broader player freedom implied by writing Python tools and programs. The next evaluator should use task contracts and observable outcomes, with structural checks only when the Python concept itself is being taught.

### High — world simulation

Mission results currently update rewards, completion, trace, and dialogue. There is no general mission context/state model, controlled set of world effects, or consistent interface for a program to inspect and change a target system.

### Medium — audio lifecycle

CyberAudioEngine creates a daemon thread for each sound event. AudioTrack release is not protected by a finally block, and there is no centralized cancellation/cleanup lifecycle. This is a reliability/performance improvement, not a reason to redesign the sound system immediately.

### Medium — asset and localization verification

Manifest parsing validates keys, paths, dimensions, sprite grids, and frame counts. However, automated tests for manifest/file parity, optional missing assets, bitmap decode failures, and animation bounds are not yet part of the routine validation. Filipino translation coverage for the newer phase content also needs review.

### Low — legacy naming

The controller class/file still uses the old `CompanionPythonRunnerController` name although it now binds to an in-package service. This is a naming cleanup only; it does not change runtime behavior or security.

## Corrections in the current stabilization batch

- Replaced arithmetic next-module selection with progression-rule-based selection.
- Made the Terminal's next-mission affordance use the same accessibility decision as the ViewModel.
- Added regression tests for incomplete modules, normal unlock, replay, blocked prerequisite, and final module.
- Removed inaccurate "isolated runner" language from player-facing execution status and Python source comments.
- Added `docs/DEVELOPMENT_ROADMAP.md` to define the sequence and exit criteria for future work.

## Validation status

- GitHub Actions APK build: passed for the stabilization commit; exactly one debug APK was produced and its package ID was verified as `com.neonull.protocol`.
- Python helper/grader unit tests: not executed as part of that workflow.
- Android instrumentation tests: not executed as part of that workflow.
- Physical-device execution/security checks: not performed in this audit.

Do not mark Phase 0 complete until the Python suite and Android instrumentation suite have been run and their results reviewed. APK compilation alone is not runtime validation.
