# ByteGhost Curriculum Architecture

## Current content contract

Every existing story mission exposes six ordered learning phases through
`LessonSequenceRepository.stepsFor(module)`:

1. **Learn** — the module's localized concept explanation.
2. **Observe** — the module's code example, with a prediction-before-run instruction.
3. **Practice** — a guided task with starter code and observable success criteria.
4. **Challenge** — a less guided task which transfers the concept to a new input or constraint.
5. **Operation** — a story-context task that applies the concept to the SYNTHEX mission.
6. **Mastery** — a more independent task with explicit competence criteria.

The current seven modules each have authored Practice, Challenge, Operation, and
Mastery prompts. The content is localized in English and Indonesian. Filipino
text currently falls back to the English strings for these new lesson steps;
translation is still required before claiming full three-language parity.

## Evaluation boundary

A lesson step's acceptance criteria describe what competence means; they are
not yet a complete executable grader. The original Kotlin mission evaluators
still use source-pattern checks and must not be treated as authoritative Python
semantics.

A new Python-side AST-assisted grader has been added for the seven original
missions. It parses Python syntax, inspects structural requirements, executes
the submission through CPython, and checks runtime output. Its checks include
branch structure, loop behavior, function results on additional inputs, file
exception handling, and class method behavior. This is a testable foundation,
not a finished assessment system: AST checks can still be gamed, some task
contracts remain tightly coupled to the original seven missions, and the grader
currently runs in the same process as the execution helper.

Do not infer success by searching source text with regular expressions. Do not
ship hidden expected answers in the client APK and call them secret: local APK
contents can be inspected.

Before real graded submissions are enabled:
- execute submissions only in a separately packaged runner with a distinct
  Android UID and a narrowly scoped Binder contract;
- keep authoritative progression and any private grading data outside the
  untrusted runner;
- send bounded, individual test cases to the runner rather than exposing a
  complete hidden test suite;
- add execution deadlines, output caps, process recovery, and adversarial
  tests for filesystem, Java bridge/reflection, threads, memory pressure,
  and IPC misuse;
- verify runner installation, update, signing, and failure recovery on real
  Android devices.

The current same-UID Chaquopy service remains a proof of concept. It is not
safe for arbitrary player code and is not connected to the editor.

## Curriculum expansion policy

The existing seven missions are the first content slice, not the full Python
curriculum. Future campaigns should be added only with complete, reviewed
lesson content and explicit prerequisite relationships. Do not inflate
campaign counts with empty lessons. Proposed later tiers include foundational
syntax and types, core control flow, data structures, functions, software
engineering, advanced Python, and systems integration.
