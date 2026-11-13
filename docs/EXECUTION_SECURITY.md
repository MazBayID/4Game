# ByteGhost Python Execution Security Plan

Status: design decision for the runtime and evaluator phases.

## Decision

Use Chaquopy + CPython as the language runtime. Keep the current in-app secondary process only as a Stage 1 runtime proof of concept.

Do not treat `android:process=":python_runtime"` as a security boundary: it runs under the same application UID and permissions as the main app.

Do not ship arbitrary player-code execution through the same-UID process.

For production execution of untrusted player submissions, the preferred offline architecture is a small companion Android runner package:

- The game and runner are separate Android packages and therefore receive distinct Android UIDs.
- The runner has no INTERNET permission and requests no dangerous Android permissions.
- The runner stores no game credentials, account tokens, or player secrets.
- The runner exposes only a narrow Binder service protected by a signature-level permission shared with the game.
- IPC accepts bounded source text and structured inputs; it never accepts arbitrary file paths or host-app objects.
- The game owns curriculum definitions, hidden test cases, XP, mastery, and save data.
- The runner returns bounded stdout, stderr, structured return values, and normalized exceptions.
- The runner uses an ephemeral workspace which is cleared between submissions.

This costs more packaging and installation work than a single APK. It is preferable to falsely claiming that a same-UID process is a sandbox. Before production, validate the companion installation/update flow on real Android devices.

## Why not use isolatedProcess with Chaquopy immediately?

Android's isolated service model gives a process a distinct isolated UID and removes normal app permissions. That is the right kind of OS boundary in principle, but Chaquopy's documented startup path uses an Activity, Service, or Application context and its FAQ discusses a normal separate process as an option. Compatibility with an isolated-UID service and its storage/runtime bootstrap has not been demonstrated in this project.

Therefore:
1. First prove CPython starts in a normal secondary process.
2. Then prototype the separate-UID runner package.
3. Test Python bootstrap, package imports, IPC, filesystem behavior, and process restart on physical devices.
4. Only enable arbitrary user submissions after the runner passes adversarial tests.

## Grading architecture

Keep hidden test cases in the trusted game process. Never send expected outputs or the complete hidden test suite to the runner.

For function-contract exercises:
1. The runner loads the submitted source in its own process.
2. The game sends one bounded function invocation at a time.
3. The runner returns a bounded, serializable result or normalized exception.
4. The game compares the result against the hidden expected value.
5. Use a fresh runner session/process between test batches when practical.

For stdin/stdout exercises, send one test input at a time and compare captured output in the game process.

This reduces accidental leakage of hidden tests and keeps reward/progress decisions outside the untrusted interpreter.

## Required defense-in-depth controls

- Maximum source size and IPC payload size.
- Maximum stdout and stderr bytes, enforced while writing (not by slicing an already-unbounded StringIO afterwards).
- Wall-clock timeout with a watchdog able to terminate the runner process.
- Per-run fresh workspace and cleanup.
- No INTERNET permission in the runner package.
- No shared external storage access.
- No game secrets or persistent player progress in runner storage.
- Bounded concurrency and queue length.
- Structured error categories: syntax, name, type, runtime, timeout, output limit, transport, and runner crash.
- Adversarial tests for infinite loops, huge output, huge allocations, filesystem traversal, Java bridge access, reflection, thread creation, and IPC abuse.
- No claim of perfect sandboxing: document residual risks and test on supported Android versions.

## Current PoC scope

The current PoC only proves that the app can package Chaquopy and communicate with a secondary process. It is not connected to the editor and is not a secure execution environment.
