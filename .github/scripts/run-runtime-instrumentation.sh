#!/usr/bin/env bash
set -uo pipefail

echo "=== ByteGhost Android instrumentation suite ==="
echo "Tests are ordered: Binder ping, pre-start source limit, CPython stdout, exception recovery, output cap, asset manifest, progression prerequisites, input() stdin, oversized stdin rejection, six-phase curriculum completeness."
gradle connectedDebugAndroidTest
test_status=$?

echo "=== Android runtime diagnostics ==="
adb logcat -d -v threadtime | grep -E "ByteGhostRuntimeTest|ByteGhostPythonService|AndroidRuntime|SecurityException|Permission Denial" || true

if [ "$test_status" -ne 0 ]; then
  exit "$test_status"
fi

python3 - <<'PY'
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

result_roots = [
    Path("app/build/outputs/androidTest-results"),
    Path("app/build/reports/androidTests"),
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
    if root.tag not in ("testsuite", "testsuites"):
        continue
    if root.tag == "testsuites":
        suites = list(root)
    else:
        suites = [root]
    for suite in suites:
        total += int(suite.attrib.get("tests", 0))
        failures += int(suite.attrib.get("failures", 0))
        errors += int(suite.attrib.get("errors", 0))

print(f"Instrumentation report: tests={total}, failures={failures}, errors={errors}")
if total < 10:
    print("ERROR: expected all ten instrumentation tests to be discovered and executed.")
    sys.exit(2)
if failures or errors:
    sys.exit(1)
PY
