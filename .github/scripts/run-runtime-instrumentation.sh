#!/usr/bin/env bash
set -uo pipefail

echo "=== ByteGhost host instrumentation suite ==="
gradle assembleDebug
gradle :app:connectedDebugAndroidTest
host_test_status=$?

echo "=== ByteGhost companion runner instrumentation suite ==="
if [ "$host_test_status" -eq 0 ]; then
  gradle :runner:connectedDebugAndroidTest
  runner_test_status=$?
else
  runner_test_status=0
fi

echo "=== Android runtime diagnostics ==="
adb logcat -b all -d -v threadtime | grep -Ei \
  "ByteGhostRuntimeTest|ByteGhostPythonService|ByteGhostRunner|Chaquopy|python|AndroidRuntime|FATAL EXCEPTION|am_crash|am_anr|lowmemorykiller|lmkd|ActivityManager|Process .* died|Killing .*com\\.neonull\\.protocol|tombstoned|DEBUG|libc|linker|SecurityException|Permission Denial" || true

if [ "$host_test_status" -ne 0 ] || [ "$runner_test_status" -ne 0 ]; then
  echo "=== Relevant service state ==="
  adb shell dumpsys activity services com.neonull.protocol || true
  adb shell dumpsys activity services com.neonull.protocol.runner || true
  echo "=== Relevant process state ==="
  adb shell dumpsys activity processes | grep -A 12 -B 4 -E "com\\.neonull\\.protocol" || true
  adb shell dumpsys meminfo com.neonull.protocol.runner || true
  exit 1
fi

python3 - <<'PY'
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

result_roots = [
    Path("app/build/outputs/androidTest-results"),
    Path("app/build/reports/androidTests"),
    Path("runner/build/outputs/androidTest-results"),
    Path("runner/build/reports/androidTests"),
]
reports = []
for root in result_roots:
    if root.exists():
        reports.extend(root.rglob("*.xml"))

total = failures = errors = 0
for report in reports:
    try:
        root = ET.parse(report).getroot()
    except ET.ParseError:
        continue
    suites = list(root) if root.tag == "testsuites" else [root]
    if root.tag not in ("testsuite", "testsuites"):
        continue
    for suite in suites:
        total += int(suite.attrib.get("tests", 0))
        failures += int(suite.attrib.get("failures", 0))
        errors += int(suite.attrib.get("errors", 0))

print(f"Instrumentation report: tests={total}, failures={failures}, errors={errors}")
if total < 14:
    print("ERROR: expected ten host tests and four companion-runner tests.")
    sys.exit(2)
if failures or errors:
    sys.exit(1)
PY
