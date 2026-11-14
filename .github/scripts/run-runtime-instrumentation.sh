#!/usr/bin/env bash
set -euo pipefail

echo "=== ByteGhost Python helper tests ==="
PYTHONPATH=app/src/main/python python -m unittest discover -s tests/python -v

echo "=== Unified ByteGhost APK instrumentation suite ==="
gradle :app:connectedDebugAndroidTest

echo "=== ByteGhost runtime diagnostics ==="
adb logcat -b all -d -v threadtime | grep -Ei \
  "ByteGhostRunner|Chaquopy|python|AndroidRuntime|FATAL EXCEPTION|am_crash|am_anr|lowmemorykiller|lmkd|ActivityManager|Process .* died|Killing .*com\\.neonull\\.protocol|tombstoned|DEBUG|libc|linker|SecurityException|Permission Denial" || true
