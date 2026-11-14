# ByteGhost Curriculum Architecture

## Current content slice

The current campaign contains seven authored story modules. Each `ProtocolModule` contains localized story briefing, Python concept explanation, code example, objective, starter code, hints, GLITCH dialogue, optional Overlord taunt, and XP/RAM rewards.

These seven modules are the initial content slice, not a complete Python curriculum.

## Six learning phases: content exists, flow is not connected

`LessonSequenceRepository.stepsFor(module)` defines six ordered learning phases:

1. **Learn** — localized concept explanation.
2. **Observe** — code example and a prediction-before-run instruction.
3. **Practice** — guided exercise.
4. **Challenge** — less-guided transfer exercise.
5. **Operation** — story-context application.
6. **Mastery** — independent competence exercise.

The six-phase content is currently data-only. `LearningUiState.activeLessonSteps` can expose the list, but the existing Theory and Terminal screens still follow the older module-level flow. There is no persisted completion state for each phase, and the phase acceptance criteria are instructional text rather than executable evaluation contracts.

English and Indonesian content are authored for these steps. Filipino parity for the newer phase content has not been completed; the current localized-text fallback behavior must not be mistaken for a reviewed translation.

## Current evaluation boundary

The embedded Python grader supports the original seven module IDs. It combines AST structure checks with CPython execution and output/state checks. Several checks are intentionally tied to exact variable names, AST shapes, or specific outputs. This makes it a useful first assessment implementation, but not a general semantic evaluator for arbitrary equivalent solutions.

The lesson-step exercises are not yet mapped to independent executable grader contracts. Do not imply that every Practice, Challenge, Operation, or Mastery task can already be submitted and graded.

The grader is packaged locally in the APK. Its checks are inspectable by a determined player; client-side checks must not be described as secret or authoritative against a hostile user.

## Runtime architecture

CPython 3.13 is embedded in the ByteGhost APK through Chaquopy. The Messenger-based service runs in the secondary process `:execution`, but it shares the host application's Android UID. Process separation helps protect UI responsiveness and enables process recovery; it does not create a filesystem, permission, or security boundary.

See [Execution Security](EXECUTION_SECURITY.md) for the concrete limitations.

## Curriculum expansion policy

Add later curriculum only when each mission has:
- a clear learning objective and prerequisite;
- authored Learn and Observe content;
- guided and independent exercises;
- explicit acceptance criteria;
- a matching executable evaluation contract;
- story context and success/failure feedback;
- localization reviewed for every supported language.

Do not inflate campaign counts with empty lessons. Future curriculum tiers may cover foundational syntax and types, control flow, data structures, functions, software engineering, advanced Python, and systems integration.
