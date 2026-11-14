# ByteGhost Development Roadmap

Baseline: `main` after the single-APK CPython integration. This document is the working source of truth for the next development sequence. Update it when a phase is completed or a decision changes.

## Product direction

ByteGhost is a story-driven Python learning game. The player learns by building and using programs to solve problems inside the game world. Python execution is a core interaction, but the product is not merely a code editor with lessons attached: missions, player decisions, feedback, progression, and the world response must form one coherent gameplay loop.

Preserve the existing visual identity and screen layout unless a gameplay requirement proves a change is necessary. Do not force unused art into screens. The current asset pack is a resource library, not a checklist that must be exhausted.

## Verified baseline

### Application and runtime
- One Android application is shipped: `com.neonull.protocol`.
- Kotlin, Jetpack Compose, Material 3, and CPython 3.13 (Chaquopy) are in the app module.
- `PythonRunnerService` runs in secondary process `:execution`, is non-exported, and shares the app UID.
- Kotlin communicates with the embedded service for free-play and mission submissions.
- Source, stdin, output, and execution-time limits exist. The Runner is not a security sandbox; there is no hard memory quota, and interpreter/module state may persist between submissions.
- Current screens are Onboarding, Story Hub, Theory, and Terminal, with a separate free-play sandbox mode.

### Gameplay and learning content
- Seven authored `ProtocolModule` records contain story briefings, concept explanations, objectives, starter code, hints, dialogue, snippets, and rewards.
- `ProgressionRules` implements linear prerequisites; completed modules remain replayable.
- `LessonSequenceRepository` contains six instructional phases for the seven original modules: Learn, Observe, Practice, Challenge, Operation, and Mastery.
- Those phases remain data-only: no persisted per-phase completion, no phase navigation, and no executable grader contract per exercise.
- The current playable path is module-level: Theory, then Terminal, then mission-level success or retry.

### Evaluation and player state
- `byteghost_grader.py` handles the original seven module IDs using AST checks plus CPython execution and output/state checks.
- Some checks depend on specific names or AST shapes, so equivalent solutions may be rejected. This is an initial grader, not a general semantic evaluator.
- Module success updates completion, XP, RAM, trace integrity, and dialogue in `LearningViewModel`.
- Module completion, XP, RAM, trace integrity, language, audio preference, and code drafts persist. Per-phase progress and richer mission-world state do not.
- The grader is client-side and inspectable; it is not tamper-proof against a hostile player.

### Assets and validation
- The asset manifest declares 86 assets and all declared files were present in the audited tree. Only a subset is directly referenced by current screens; the rest remain intentionally deferred.
- Execution-security and asset-pipeline docs already describe the single-APK architecture and deferred-asset policy.
- Normal GitHub Actions builds the single debug APK and checks package identity. Python and Android instrumentation suites are separate validation steps and should run at the end of an implementation batch, not after every small edit.

## Verified audit findings

1. Six-phase learning content is disconnected from Theory/Terminal navigation and lacks per-phase persistence and grader contracts.
2. The mission grader is narrow and tied to seven module IDs. It needs task-level contracts before it can support all phase exercises.
3. Mission success currently yields module-level rewards and dialogue; it does not yet drive a richer simulation of world/system state.
4. Runner process separation supports responsiveness and recovery, not isolation. Arbitrary hostile Python cannot be described as sandboxed.
5. Module navigation previously used arithmetic ID increments and compared IDs with list size. This is brittle for non-contiguous IDs and replay states; the current batch moves this logic into progression rules and adds regression coverage.
6. Several comments/player-facing strings still used old companion/isolated-runner terminology, which could imply a security guarantee that does not exist.

## Phase 0 — Stabilize the current repository

**Goal:** make the existing playable slice internally consistent before expanding it.

- Route next-module selection through campaign ordering and accessibility rules.
- Add regression coverage for incomplete, unlocked, completed, replayed, and final-module cases.
- Remove stale companion/isolated-runner wording from source comments and player-facing status text.
- Keep architecture/security documentation aligned with the actual single-APK, shared-UID design.
- Remove remaining legacy names only where they cause confusion; avoid broad cosmetic refactors.
- Run Python helper/grader and Android instrumentation suites after this stabilization batch.

**Exit criteria:** navigation cannot skip locked content or advance an incomplete module; progression cases are covered; source and documentation describe the actual runtime boundary.

## Phase 1 — Define and implement the core gameplay loop

**Goal:** make each mission a playable problem-solving experience rather than a briefing plus code submission.

Define one stable mission lifecycle:

1. **Receive a contract:** story context, target/system, constraints, and a clear objective.
2. **Inspect:** reveal relevant system state and available information/tools.
3. **Build:** player writes or modifies a Python program/tool.
4. **Execute:** the program runs against a controlled mission context.
5. **Evaluate:** check the required outcome, not merely whether Python ran without an exception.
6. **Respond:** target/world changes, narrative reacts, and useful feedback is shown.
7. **Progress:** rewards, unlocks, and persistent mission state update exactly once.

Design domain contracts before expanding UI: campaign, mission, objective, execution context, evaluation contract, mission outcome, and world effects. Separate authored content from execution mechanics. Specify what state a mission reads, what it may change, and what counts as success or failure.

**Exit criteria:** one representative mission has explicit input state, player action, expected effect, evaluation rules, failure feedback, success response, and persistence behavior.

## Phase 2 — Connect the six learning phases

- Add current phase and per-phase status to active mission state.
- Connect Learn and Observe to instructional content and prediction tasks.
- Connect Practice and Challenge to editable, runnable submissions.
- Connect Operation to the mission's story/system context.
- Connect Mastery to an independent assessment.
- Persist phase completion and drafts without losing existing module progress.
- Define retry, revisit, and skip behavior.

**Exit criteria:** at least one module completes all six phases, and state restores after app restart.

## Phase 3 — Build task-based evaluation contracts

- Replace mission-ID-only assumptions with explicit task contracts.
- Separate syntax/runtime errors, structural learning objectives, and observable mission outcomes.
- Prefer behavior/state assertions over exact source shapes, except when syntax itself is the learning objective.
- Test valid alternatives, boundaries, wrong results, extra output, runtime errors, and timeouts.
- Prevent a submission from contaminating later submissions or the grader; use a fresh controlled execution context.
- Keep client-side evaluator limitations explicit.

**Exit criteria:** every playable phase has a documented contract and tests for accepted alternatives and rejected failures.

## Phase 4 — Make the world react to programs

- Add mission-scoped system state and controlled world effects.
- Drive target responses, GLITCH dialogue, alerts, and story progression from evaluated effects.
- Define failure consequences and recovery; do not arbitrarily punish syntax mistakes.
- Derive XP, RAM, trace, unlocks, and completion from one mission outcome; do not award repeatedly on replay.

**Exit criteria:** a program changes mission state visibly and testably, with deterministic rewards and progression.

## Phase 5 — Expand campaign and content

- Expand Python concepts in prerequisite order.
- Give every mission a learning objective, story purpose, exercises, acceptance criteria, and evaluation tests.
- Review English, Bahasa Indonesia, and Filipino translations instead of relying on fallback text.
- Integrate remaining assets only when a real screen or gameplay mechanic needs them.
- Avoid empty modules and cosmetic systems that do not strengthen gameplay.

**Exit criteria:** each campaign slice is playable end-to-end and meets the same content/evaluation bar as the initial slice.

## Phase 6 — Release validation

- Run Python helper/grader and Android instrumentation suites.
- Build and inspect the single APK.
- Test install, update, uninstall, service binding, Runner recovery, output limits, watchdog, and persistence on physical devices.
- Perform adversarial tests for filesystem access, Java bridge/reflection, threads, memory pressure, and interpreter state.
- Do not claim arbitrary hostile-code isolation unless a real isolation boundary is implemented and validated.

## Working rules

- Work in practical batches; do not run the full suite after every small edit.
- Preserve frozen UI unless gameplay requires a change.
- Do not force unused assets into the build.
- Do not delete tests to shorten CI. Keep routine APK CI fast and run full suites at planned checkpoints.
- A phase is complete only when behavior works and tests support the claim, not merely because files exist.
- Update this roadmap as the verified baseline changes.
