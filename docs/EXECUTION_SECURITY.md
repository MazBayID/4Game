# ByteGhost Python Execution Security

Status: isolated CPython runner is integrated with gameplay; CI validation is in progress. Physical-device and broader adversarial validation remain required.

## Architecture

- Game package: `com.neonull.protocol`.
- Companion runner package: `com.neonull.protocol.runner`.
- Each APK is a separate Android package and receives a distinct Linux UID.
- The game declares the signature-level permission `com.neonull.protocol.permission.RUN_PYTHON`.
- The runner service is exported only behind that signature permission.
- Both APKs must be signed with the same certificate. Debug variants use the shared Android debug signing identity in CI; release distribution must use the same release signing key for both APKs.
- CPython 3.13 (Chaquopy) is packaged only in the companion runner. The game APK does not embed a second Python runtime.
- The runner requests no INTERNET permission or dangerous Android permissions.
- `CompanionPythonRunnerController` sends source, optional stdin, and an operation (execute or grade) over Binder.
- Free-play submissions execute directly in CPython. Mission submissions are graded by the AST-based `byteghost_grader` inside the runner.
- The game receives bounded output, error details, and safe variable previews. It remains responsible for XP, RAM rewards, campaign progression, and saving player progress.
- The old same-UID Python PoC service and its host-side execution sources have been removed. There is no same-UID execution fallback.

## Runtime limits implemented

- Source: 128 Ki characters maximum.
- stdin: 64 Ki characters maximum.
- stdout and stderr: 64 Ki characters each, capped while writing.
- Execution watchdog: 5 seconds after CPython has initialized. On timeout, the runner process is killed; Android can restart the bound service.
- Each submission gets a fresh Python globals namespace.
- A temporary cache workspace is created for a submission and deleted after it finishes.
- RAM Inspector previews are limited to 12 variables, with bounded, non-recursive summaries of basic Python values and containers.

## Important limitations (do not overclaim)

This is a practical defense-in-depth boundary, not a perfect Python sandbox.

- There is no hard per-process memory quota. A hostile allocation can still cause runner OOM or broader device memory pressure.
- The Python Java bridge remains available inside the runner process. Its reach is limited by runner UID and granted Android permissions, but it has not been comprehensively audited.
- Python threads may outlive a submission; the runner currently isolates globals and workspace, not every possible background activity.
- Python modules and interpreter-level state may persist between submissions even though each submission receives a fresh globals namespace.
- The runner's own private files and readable system files are not all inaccessible to submitted code.
- Filesystem traversal, reflection/Java bridge abuse, thread abuse, IPC abuse, and memory exhaustion still need adversarial tests on supported Android versions.
- Do not claim perfect isolation or expose unrestricted execution to production players until these tests pass.

## Installation and updates

Install both APKs from the same build artifact and source revision:

1. `ByteGhost-The-Synthex-Files-debug.apk`
2. `ByteGhost-Python-Runner-debug.apk`

Both must be signed with the same certificate. Update both together. If the runner is missing or has an incompatible signature, the host controller returns an installation/permission error rather than silently falling back to a same-UID runtime.

## Current validation coverage

- Android compile and APK packaging.
- Seven mission grading cases, including incorrect solutions and syntax errors.
- Binder execution and mission grading from the game package.
- Host-private file access rejection.
- Separate package UID and signature permission checks.
- Output/input/source limits, concurrency rejection, watchdog termination and runner restart.
- Campaign progression, asset manifest, six learning phases, and end-to-end mission reward handling.

CI passing is not a substitute for testing both APKs on a physical Android device.

## Required release checks

1. Confirm host and runner UIDs differ on a physical Android device.
2. Confirm an unrelated app cannot bind the runner service.
3. Confirm the host app signed with the matching certificate can bind and execute Python.
4. Confirm source/stdin/output caps and the watchdog.
5. Confirm timeout kills only the runner process and a later request can restart it.
6. Test large allocations, filesystem traversal, Java bridge/reflection, thread creation, and IPC abuse.
7. Test clean install, both-APK update, uninstall, and mismatched-signature behavior.
8. Keep mission grading and hidden checks inside the runner; do not send expected answers to the player submission.
