# ByteGhost Gameplay Architecture

## Purpose

This document separates what the current game already does from the gameplay structure that still needs to be designed and implemented. It is a working architecture contract, not a claim that every listed system is already playable.

ByteGhost is a story-driven Python-learning game in the 2142 SYNTHEX setting. Python programming is the player's primary tool for solving the learning missions. There is **no combat system** in the current design. Asset names such as `overlord_attack` or `overlord_damage` are artwork/animation labels and must not be interpreted as combat mechanics.

## Current implemented loop

1. **Onboarding** — choose a supported language and enter the game.
2. **Story Hub** — view campaign progress, player stats, available modules, Codex, and free-play entry.
3. **Theory** — read the active module's story briefing, concept explanation, code example, and objective. The Hub can also open a module directly in the Terminal.
4. **Terminal** — edit starter code, insert snippets, request hints, reset, or submit.
5. **Execution/evaluation** — free play calls the execution path; a campaign submission calls the grader for the active module.
6. **Outcome** — the UI displays output/errors and GLITCH feedback. A successful first completion grants module XP/RAM and updates completion/progression state. Failed attempts affect the current trace/attempt state.
7. **Continue** — the player can return to the Hub or advance to the next module.

This is the high-level flow represented by `MainActivity`, `LearningViewModel`, `StoryHubScreen`, `TheoryViewerScreen`, and `TerminalLearningScreen`. It is a compact mission-and-terminal loop, not yet a fully simulated world.

## Current domain model

### Mission content

`ProtocolModule` currently combines:
- stable numeric module ID and act label;
- boss-stage flag;
- XP and RAM rewards;
- localized title, mission name, concept tag, story briefing, concept explanation, objective, hints, and dialogue;
- code example, starter code, and snippet chips.

The current campaign is seven modules. `CurriculumRepository.modules` is the active content source. The separate `ProtocolCampaign` type exists as a data structure but is not yet the central runtime campaign controller.

### Progression

`ProgressionRules` currently implements a linear sequence: the first incomplete module is the next unlocked module; completed modules remain replayable. The ViewModel tracks completed module IDs, XP, RAM, trace integrity, and the active module.

Rewards are granted on first successful completion, not repeatedly on replay. This invariant must remain true when progression is refactored.

### Learning phases

`LessonSequenceRepository` authors six phases per module:
Learn → Observe → Practice → Challenge → Operation → Mastery.

These are currently content records. They are not yet the active screen state machine, do not have persisted per-phase completion, and their acceptance criteria are not connected to independent executable graders.

### Python execution and evaluation

The embedded Runner executes CPython in a secondary Android process. Campaign grading is currently coupled to the original seven module IDs and their specific AST/runtime checks. Free play does not award campaign progression.

## Target gameplay flow

The intended next step is to make the six-phase learning sequence a real part of the mission loop without redesigning the frozen visual layout:

```
Story Hub
  → Mission briefing
  → Learn
  → Observe
  → Practice
  → Challenge
  → Operation
  → Mastery
  → Mission outcome
  → Story response + progression
  → Story Hub / next unlocked mission
```

The terminal/editor remains the player's programming workspace. A phase may present a different task or constraint, but it should not require a new screen for every phase unless a later UX decision demonstrates a clear need.

## Required domain boundaries

The implementation should separate these responsibilities instead of putting all behavior into one large ViewModel or screen:

1. **Campaign catalog** — ordered mission definitions and prerequisite metadata.
2. **Mission content** — narrative, learning objective, code context, hints, and rewards.
3. **Lesson progression** — current phase and per-phase completion state.
4. **Submission** — source code, mission/phase identity, and execution mode.
5. **Evaluation contract** — explicit requirements for a particular task, independent from UI wording.
6. **Mission outcome** — pass/fail, feedback, and reward eligibility.
7. **Player progression** — persisted phase completion, mission completion, XP, RAM, and unlock state.
8. **Story presentation** — dialogue, visual response, and narrative transition based on the outcome.

The evaluator must not infer a task's entire meaning from fragile source-text matching. Each executable task needs an explicit contract. AST checks may verify required concepts where appropriate; runtime tests should verify behavior. Equivalent valid solutions should be accepted when they meet the learning objective.

## State and reward invariants

- A mission has a stable ID; display order must not become identity.
- Phase completion and mission completion are different states.
- A mission is complete only after its required evaluation succeeds.
- A phase must not be marked complete merely because code executed without an exception.
- Rewards are idempotent: replaying a completed mission cannot duplicate first-clear rewards.
- Free play never changes campaign completion or grants mission rewards.
- Story feedback reflects the evaluated outcome; it does not independently grant progression.
- Unlock rules are explicit and testable.
- No combat, damage model, weapon system, or combat balancing is implied by the current asset names.

## Work still required

- Define a stable task/evaluation contract for every lesson phase.
- Introduce persisted phase-level progress and a clear phase transition policy.
- Connect the existing six-phase content to the current Theory/Terminal flow.
- Separate mission content from progression/evaluation logic where that reduces coupling.
- Expand story/world response only after the mission loop is reliable.
- Keep the current UI layout frozen while these systems are integrated.

## Definition of done for the gameplay foundation

A player can enter a mission, progress through all required learning phases, submit code for the active task, receive accurate and useful feedback, complete the mission, receive rewards exactly once, and see the next mission unlock. Progress survives app restart. Free play remains separate from campaign progression. The experience remains a Python problem-solving game, not a combat game.
