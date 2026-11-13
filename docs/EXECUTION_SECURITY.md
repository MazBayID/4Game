# ByteGhost Python Execution Security

Status: companion runner implementation added; adversarial and device validation still required.

## Architecture now in the repository

- Game package: `com.neonull.protocol`.
- Runner package: `com.neonull.protocol.runner`.
- The two APKs are separate Android packages and receive different Linux UIDs.
- The game declares the signature-level permission `com.neonull.protocol.permission.RUN_PYTHON`.
- The runner service is exported only behind that signature permission.
- Both APKs must be signed with the same certificate. Debug variants use the shared Android debug signing identity in CI; release distribution must use the same release signing key for both APKs.
- The runner requests no INTERNET or dangerous Android permissions and stores no game secrets or player progress.
- The game has a Binder client, `CompanionPythonRunnerController`. It is not yet connected to the editor/gameplay.
- The old same-UID service remains only as a runtime PoC and must not execute arbitrary player submissions.

## Runtime limits implemented

- Source: 128 Ki characters maximum.
- stdin: 64 Ki characters maximum.
- stdout and stderr: 64 Ki characters each, capped while writing.
- Execution watchdog: 5 seconds after CPython has initialized. On timeout, the runner execution process is killed; Android can restart the bound service.
- Each submission gets a fresh Python globals namespace.
- A temporary cache workspace is created for a submission and deleted after it finishes.
- Runner process is separate from the game UID, so normal Android private-file access is enforced by the OS.

## Important limitations (do not overclaim)

This is a practical defense-in-depth boundary, not a perfect Python sandbox.

- There is no hard per-process memory quota. A hostile allocation can still cause runner OOM or broader device memory pressure.
- The Python Java bridge remains available inside the runner process. Its reach is limited by runner UID and granted Android permissions, but it has not been comprehensively audited.
- Python threads may outlive a submission; the runner currently isolates globals and workspace, not every possible background activity.
- The runner's own private files and readable system files are not all inaccessible to submitted code.
- Filesystem traversal, reflection/Java bridge abuse, thread abuse, IPC abuse, and memory exhaustion still need adversarial tests on supported Android versions.
- Do not claim perfect isolation or expose unrestricted execution to production players until these tests pass.

## IPC contract

The runner accepts only bounded source and stdin strings. It does not accept arbitrary paths, host objects, hidden expected answers, game save data, or hidden test collections. The host owns grading, hidden tests, XP, mastery, and persistence. The runner returns bounded stdout/stderr and normalized error information.

## Installation and updates

Install the game APK and companion runner APK built from the same source revision and signed with the same certificate. Update both together. If the runner is missing or has an incompatible signature, the host controller returns an installation/transport error rather than silently falling back to the same-UID PoC.

## Required release checks

1. Confirm host and runner UIDs differ on a physical Android device.
2. Confirm an unrelated app cannot bind the runner service.
3. Confirm the host app signed with the matching certificate can bind and execute Python.
4. Confirm source/stdin/output caps and the watchdog.
5. Confirm timeout kills only the runner process and a later request can restart it.
6. Test large allocations, filesystem traversal, Java bridge/reflection, thread creation, and IPC abuse.
7. Test clean install, both-APK update, uninstall, and mismatched-signature behavior.
8. Do not send hidden test expectations to the runner.
