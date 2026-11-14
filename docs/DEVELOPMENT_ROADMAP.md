# ByteGhost Development Roadmap

This roadmap is based on the repository state audited on 2026-11-14. It prioritizes finishing and connecting existing foundations before expanding the feature set.

## Working rules

- Keep the existing Compose visual layout frozen unless a gameplay requirement cannot be met otherwise.
- Do not add features merely to use every imported asset.
- Do not treat a screen, data class, or documentation entry as proof that a feature is playable.
- Keep CPython 3.13 as the only Python runtime.
- Preserve the single-APK decision. The `:execution` process is not a security sandbox because it shares the app UID.
- Keep tests. Run the complete Python and Android runtime suite at the end of a planned implementation batch, rather than after every small edit.
- The regular CI workflow remains a fast single-APK build/identity check. Runtime tests remain available through the instrumentation script.

## Phase 0 — Repository stabilization

**Status: initial cleanup applied; full validation still pending.**

- [x] Update README for the single-APK architecture.
- [x] Correct curriculum and execution-security documentation.
- [x] Correct asset documentation to reflect the imported pack and deferred assets.
- [x] Fix the missing Android test assertion import.
- [x] Make the extra-output Python grader test use real newline characters.
- [x] Record the actual gameplay loop and known gaps.
- [ ] Run Python unit tests and Android instrumentation together at the end of the next implementation batch.
- [ ] Perform physical-device regression after the runtime test suite.

## Phase 1 — Gameplay architecture

**Current focus.** The goal is to turn the existing mission-and-terminal prototype into a coherent learning gameplay loop, without starting visual redesign.

Deliverables:
1. Finalize the mission, lesson-phase, submission, evaluation, outcome, and progression boundaries described in [Gameplay Architecture](GAMEPLAY_ARCHITECTURE.md).
2. Define a stable executable evaluation contract for each task; do not reuse one mission-level grader for unrelated phase exercises.
3. Define phase state transitions and what counts as completion for Learn, Observe, Practice, Challenge, Operation, and Mastery.
4. Define persistence and replay rules. Preserve first-clear reward idempotency.
5. Define how success/failure outcomes affect GLITCH dialogue, story state, XP, RAM, and trace integrity.
6. Keep free play outside campaign progression.
7. Do not introduce combat mechanics.

Exit criteria:
- A written state model and task contract are agreed.
- Every current module maps to its six learning phases.
- Every phase that expects code has a testable acceptance contract.
- No ambiguous reward or unlock behavior remains.

## Phase 2 — Mission and learning progression implementation

- Add persisted per-phase progress.
- Connect the six authored phases to the existing Theory and Terminal experience.
- Add explicit phase transitions and resume behavior.
- Keep the current screen layout; prefer state-driven content over six separate screens.
- Ensure incomplete missions cannot be skipped accidentally and completed missions remain replayable according to the agreed rules.

## Phase 3 — Evaluation system

- Replace brittle module-only assumptions with task-level evaluation contracts.
- Combine syntax/AST checks and runtime behavior tests where they teach the intended concept.
- Accept equivalent solutions when appropriate.
- Add positive, negative, edge-case, and adversarial tests.
- Keep evaluator state separate from untrusted submission globals as far as the runtime architecture permits.
- Document client-side grading limitations; local APK checks are not tamper-proof.

## Phase 4 — Story and world response

- Make story responses depend on mission outcomes.
- Expand GLITCH and SYNTHEX reactions where they reinforce the player's actions.
- Give boss-stage labels and visual effects clear narrative purposes.
- Do not turn animation names into combat systems.
- Connect XP, RAM, trace, rank, and unlocks to understandable gameplay consequences.

## Phase 5 — Curriculum expansion

- Expand beyond the initial seven missions only after the foundation is complete.
- Add new Python concepts with prerequisites and complete phase content.
- Review English, Indonesian, and Filipino localization rather than relying on fallback text.
- Add assets only when a real screen or gameplay event needs them.

## Phase 6 — Release validation

- Run all Python tests.
- Run Android instrumentation tests on an emulator/device.
- Verify install, update, uninstall, persistence, Runner recovery, and low-memory behavior on a physical device.
- Conduct adversarial runtime testing for memory, filesystem, Java bridge, threads, and IPC.
- Recheck the single-APK artifact and package identity.
- Do not call the runtime secure for arbitrary hostile Python unless a real isolation boundary has been implemented and validated.
