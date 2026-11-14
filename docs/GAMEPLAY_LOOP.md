# ByteGhost Core Gameplay Loop

Status: design baseline for Phase 1. This document describes the intended gameplay contract; it does not claim that the complete loop is already implemented.

## 1. Current playable loop

The current app provides:

`Story Hub → Theory briefing → Terminal editor → CPython execution / module grader → module result, rewards, and dialogue → next module or Hub`

This proves the editor-to-CPython-to-result path. It does not yet provide a mission simulation in which the player's program changes a persistent target system.

## 2. Intended player loop

Every mission should use the same understandable cycle:

1. **Receive a contract** — the player learns why the operation matters, what system is involved, and what outcome is required.
2. **Inspect the target** — the player receives relevant state, logs, constraints, and available information. Inspection should teach players to reason about a system before writing code.
3. **Learn and experiment** — the player studies the Python concept, predicts example behavior, and practices with feedback.
4. **Build a tool or program** — the player writes Python to solve the current problem. The game should not require one exact source layout unless the syntax being taught is itself the objective.
5. **Execute against the operation** — the program runs with a controlled mission context, not against arbitrary real services or the device.
6. **Evaluate the outcome** — the evaluator checks required state changes and constraints, not only whether the interpreter returned without an exception.
7. **Show the world response** — the target system, mission status, narrative, and GLITCH response reflect the evaluated result.
8. **Debrief and progress** — explain what worked or failed, award eligible rewards once, and unlock the next accessible content.

The operation is the gameplay payoff. The learning phases prepare the player to perform it; they should not feel like six unrelated menu pages.

## 3. Relationship to the six learning phases

| Phase | Player activity | What the game should verify |
|---|---|---|
| Learn | Understand the concept and its role in the operation | The concept and objective were presented clearly |
| Observe | Read an example and predict what it will do | Prediction or explanation is captured before revealing the result |
| Practice | Complete a guided code task | The targeted concept is used correctly |
| Challenge | Solve a related problem with less guidance | The player can transfer the concept to a new input/problem |
| Operation | Apply the skill to the story mission | The mission's required system outcome is achieved |
| Mastery | Complete an independent assessment | The player can demonstrate the skill without the guided solution |

A module is complete only after the Operation and Mastery requirements are satisfied. Replaying a completed module may remain available, but it must not award first-clear rewards again.

## 4. Representative vertical slice: Module 1 — Awakening

### Narrative contract

- **Context:** Null-Agent wakes in a damaged Sector-09 node. GLITCH needs a valid identity signal before the access beacon can be restored.
- **Target:** the Sector-09 identity beacon.
- **Player objective:** provide the required agent identity and clearance, then broadcast the identity.
- **Python concepts:** assignment, strings, integers, and `print()`.

### Required input and expected result

| Field | Required value |
|---|---|
| Agent identity | `Null-Agent` |
| Clearance | `1` |
| Broadcast | The submitted program prints the agent identity |

The existing module-level grader checks these values and output. This is sufficient for the first instructional checkpoint, but the future mission evaluator should return structured observations rather than only a boolean pass/fail.

### Intended world response

- On success, the beacon changes from unregistered to identity-verified.
- The mission debrief confirms that the node recognizes Null-Agent.
- The game records the Operation result and makes the next accessible learning content available.
- On syntax/runtime failure, show actionable Python feedback. Do not describe a syntax error as a failed real-world attack.
- On a logically incorrect result, identify the unmet requirement and let the player revise the program.

This first mission should prove the full cycle from contract to a visible, persistent system response. It does not need combat, a large simulation, or a new screen layout.

## 5. Domain responsibilities

Keep the responsibilities separate:

- **Campaign definition:** ordering, prerequisites, narrative grouping, and unlock rules.
- **Mission definition:** story, target, objective, learning concept, and available constraints.
- **Learning step:** phase content, task prompt, starter code, hints, and phase acceptance criteria.
- **Execution context:** bounded input data and temporary workspace made available to one submission.
- **Evaluation contract:** required observations, output constraints, and success/failure rules for one task.
- **Mission outcome:** normalized result, feedback, observations, and eligible rewards.
- **World effect:** an allow-listed state transition derived by game logic from a successful outcome.
- **Progress store:** completed phases/missions, drafts, unlocks, and one-time reward claims.

Player code must not be allowed to directly award XP, unlock content, or write arbitrary game state. The game process should validate an outcome and apply explicitly defined effects.

## 6. Evaluation response contract

The evaluator should evolve from a bare `passed` flag toward a structured response with these logical fields:

- execution status: completed, syntax error, runtime error, timeout, or rejected input;
- task status: passed or not passed;
- feedback: concise, actionable explanation;
- observations: bounded values the evaluator is permitted to inspect;
- criteria results: individual required checks and their outcomes;
- proposed effect identifiers: a constrained list that Kotlin validates against the mission definition.

The evaluator must not return arbitrary commands for the game to execute. Kotlin owns the authoritative state transition and reward calculation.

## 7. Failure and reward rules

- Syntax and runtime errors should explain how to fix the code.
- A program that runs but misses the objective should identify the unmet criterion.
- Timeouts and output-limit failures should be distinguishable from incorrect answers.
- A retry must not duplicate rewards or mission effects.
- First-clear rewards are granted once. Replays may provide practice feedback but do not farm XP/RAM.
- The current trace penalty and reward balance should be reviewed during gameplay implementation; do not change balance as an incidental part of the architecture refactor.

## 8. Phase 1 acceptance criteria

The first vertical slice is ready when:

1. Module 1 has a complete contract, target state, and explicit success/failure outcomes.
2. A valid submission changes the beacon's game state through a Kotlin-owned effect.
3. An invalid submission leaves the target state unchanged and provides useful feedback.
4. The same successful submission cannot award the first-clear reward twice.
5. Mission state and completion survive an app restart.
6. The existing visual style is preserved unless a demonstrated interaction problem requires a small targeted change.
7. Tests cover the valid path, invalid path, repeated submission, and persisted result.

Only after this vertical slice works should the same architecture be generalized to the remaining six modules.
