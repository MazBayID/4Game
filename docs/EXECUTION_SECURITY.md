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
- Execution watchdog: 5 seconds after CPython has initialized. On timeout, the Runner process is killed; Android may restart the bound service.
- Each submission receives a fresh Python globals namespace.
- A temporary cache workspace is created for a submission and deleted after it finishes.
- RAM Inspector previews are limited to 12 variables, with bounded summaries of basic Python values and containers.

These limits reduce some accidental or abusive workloads. They do not provide a hard memory quota or a complete sandbox.

## Important security limitations

- The Runner process shares the app UID. Submitted Python must be assumed capable of accessing data and resources available to the ByteGhost app process, including app-private files.
- The Python Java bridge is available in the Runner process and has not been comprehensively audited.
- Python modules and interpreter-level state can persist between submissions even though each submission receives a fresh globals namespace.
- Python threads or other background activity may outlive a submission.
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
6. Test whether interpreter/module state can influence later submissions or the grader.
7. Decide whether arbitrary user-authored code is in scope. If it is, design and validate an actual isolation boundary before production release.
8. Keep player progression and reward decisions in the game process; never treat a local client-side grader as tamper-proof.
