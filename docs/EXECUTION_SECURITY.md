# ByteGhost Python Execution Security

Status: CPython is embedded in the single ByteGhost APK and is hosted by an in-package service process. Physical-device and broader adversarial validation remain required. This is not a secure sandbox for arbitrary hostile Python.

## Architecture

- Application package: `com.neonull.protocol`.
- CPython 3.13 (Chaquopy) is packaged in the app module and delivered in the same APK as the game.
- `PythonRunnerService` is declared non-exported and runs in the secondary process `:execution`.
- The service process shares the ByteGhost application's Android UID. It is not a separate Android package and does not have a distinct UID.
- The game communicates with the service using an in-app Messenger/Binder contract.
- Free-play submissions execute through `byteghost_runner.py`. Mission submissions pass through `byteghost_grader.py`, which performs AST checks and then executes the submission through CPython.
- The game process handles UI, rewards, progression, and player-facing state.

## Runtime limits implemented

- Source: 128 Ki characters maximum.
- stdin: 64 Ki characters maximum.
- stdout and stderr: 64 Ki characters each, capped while writing.
- Execution watchdog: 5 seconds from the start of request handling, including workspace setup and CPython initialization. On timeout, the Runner process is killed; Android can recreate the bound service.
- Each submission receives a fresh Python globals namespace.
- The game controller unbinds after each completed response. When the final client unbinds, the service schedules termination of the Runner process, so the next request starts a fresh CPython interpreter and does not inherit mutated `builtins`, imported-module state, or background threads from the previous request.
- This process recycling adds interpreter startup cost to each submission; performance must be measured on target devices.
- A temporary cache workspace is created for a submission and deleted after it finishes.
- RAM Inspector previews are limited to 12 variables, with bounded summaries of basic Python values and containers.

These limits reduce some accidental or abusive workloads. They do not provide a hard memory quota or a complete sandbox.

## Important security limitations

- The Runner process shares the app UID. Submitted Python must be assumed capable of accessing data and resources available to the ByteGhost app process, including app-private files.
- The Python Java bridge is available in the Runner process and has not been comprehensively audited.
- Process recycling is triggered when the final service client unbinds. Any additional client which deliberately keeps the service bound can delay recycling; the app's normal controller unbinds after every result.
- Process recycling does not protect app-private files or Android permissions because the service still shares the app UID.
- There is no hard per-process memory quota. Large allocations can cause Runner OOM or broader device memory pressure.
- Filesystem traversal, reflection/Java bridge abuse, thread abuse, memory exhaustion, and IPC misuse still require adversarial tests on supported Android versions.
- The Runner process is not an Android isolated process. Do not describe `:execution` as a security boundary.

Do not expose unrestricted execution of arbitrary hostile player code as safe. Treat the current runtime as a controlled learning prototype until the security model and adversarial validation are substantially improved.

## Installation and updates

There is one application to install and update: the ByteGhost APK with package ID `com.neonull.protocol`. Uninstalling ByteGhost removes its embedded Runner service and CPython runtime as part of the same package. Data deliberately stored outside the app's private storage may have a separate lifecycle.

## Current validation coverage

The repository contains Python helper/grader tests and Android instrumentation tests for Runner execution, mission grading, watchdog recovery, and service packaging/process declaration. The regular GitHub Actions workflow builds and verifies the single APK; it does not run the full Python and Android runtime suites on every push.

A successful APK build is not proof of runtime or sandbox safety. Full tests and physical-device validation remain release gates.

## Required release checks

1. Verify clean install, update, and uninstall behavior for the single APK on a physical Android device.
2. Verify that the non-exported service binds from the host application and is not externally bindable.
3. Verify source/stdin/output limits and watchdog behavior.
4. Verify that a timeout kills the Runner process and that a later request can recover.
5. Test large allocations, filesystem access, Java bridge/reflection, thread creation, and IPC abuse.
6. Verify that each completed request unbinds and the next request starts with a clean interpreter; include mutation tests for `builtins`, imported modules, and background threads.
7. Measure the startup/per-submission latency impact of process recycling on supported devices.
8. Decide whether arbitrary user-authored code is in scope. If it is, design and validate an actual Android isolation boundary before production release.
9. Keep player progression and reward decisions in the game process; never treat a local client-side grader as tamper-proof.
