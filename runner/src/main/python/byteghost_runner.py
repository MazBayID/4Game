"""Bounded CPython execution for the ByteGhost companion runner."""
import contextlib
import io
import itertools
import json
import os
import sys
import traceback

MAX_SOURCE_CHARS = 128 * 1024
MAX_STDIN_CHARS = 64 * 1024
MAX_OUTPUT_CHARS = 64 * 1024


class BoundedTextBuffer:
    def __init__(self, limit):
        self.limit = limit
        self.parts = []
        self.length = 0
        self.truncated = False

    def write(self, value):
        if not isinstance(value, str):
            raise TypeError("write() argument must be str")
        remaining = max(self.limit - self.length, 0)
        accepted = value[:remaining]
        if accepted:
            self.parts.append(accepted)
            self.length += len(accepted)
        if len(value) > remaining:
            self.truncated = True
        return len(value)

    def flush(self):
        pass

    def getvalue(self):
        return "".join(self.parts)


def _failure(error_type, message=None):
    return json.dumps({
        "ok": False, "stdout": "", "stderr": "",
        "stdoutTruncated": False, "stderrTruncated": False,
        "errorType": error_type, "error": message or error_type,
        "traceback": None,
    })



def _safe_preview(value, depth=0):
    if value is None:
        return "None"
    if type(value) is bool:
        return "True" if value else "False"
    if type(value) in (int, float):
        return str(value)[:80]
    if type(value) is str:
        return repr(value[:60])[:80]
    if depth >= 2:
        return "..."
    if type(value) is list:
        items = [_safe_preview(item, depth + 1) for item in itertools.islice(value, 4)]
        suffix = ", ..." if len(value) > 4 else ""
        return "[" + ", ".join(items) + suffix + "]"
    if type(value) is tuple:
        items = [_safe_preview(item, depth + 1) for item in itertools.islice(value, 4)]
        suffix = ", ..." if len(value) > 4 else ""
        return "(" + ", ".join(items) + suffix + ")"
    if type(value) is dict:
        items = [
            _safe_preview(key, depth + 1) + ": " + _safe_preview(item, depth + 1)
            for key, item in itertools.islice(value.items(), 4)
        ]
        suffix = ", ..." if len(value) > 4 else ""
        return "{" + ", ".join(items) + suffix + "}"
    if type(value) is set:
        items = [_safe_preview(item, depth + 1) for item in itertools.islice(value, 4)]
        suffix = ", ..." if len(value) > 4 else ""
        return "{" + ", ".join(items) + suffix + "}"
    return "<" + type(value).__name__ + ">"


def _ram_slots(namespace):
    slots = []
    for name, value in namespace.items():
        if not isinstance(name, str) or name.startswith("__"):
            continue
        if callable(value):
            value_type = "function"
        elif type(value) is bool:
            value_type = "bool"
        elif type(value) is int:
            value_type = "int"
        elif type(value) is float:
            value_type = "float"
        elif type(value) is str:
            value_type = "str"
        elif type(value) is list:
            value_type = "list"
        elif type(value) is tuple:
            value_type = "tuple"
        elif type(value) is dict:
            value_type = "dict"
        elif type(value) is set:
            value_type = "set"
        elif value is None:
            value_type = "NoneType"
        else:
            value_type = type(value).__name__
        slots.append({"name": name[:40], "type": value_type[:32], "preview": _safe_preview(value)[:80]})
        if len(slots) >= 12:
            break
    return slots

def execute(source, stdin_text="", workspace="."):
    if not isinstance(source, str):
        source = str(source)
    if not isinstance(stdin_text, str):
        stdin_text = str(stdin_text)
    if len(source) > MAX_SOURCE_CHARS:
        return _failure("SourceTooLarge")
    if len(stdin_text) > MAX_STDIN_CHARS:
        return _failure("InputTooLarge")

    stdout = BoundedTextBuffer(MAX_OUTPUT_CHARS)
    stderr = BoundedTextBuffer(MAX_OUTPUT_CHARS)
    namespace = {"__name__": "__byteghost_submission__"}
    original_stdin = sys.stdin
    original_cwd = os.getcwd()
    try:
        os.chdir(workspace)
        compiled = compile(source, "<byteghost-submission>", "exec")
        sys.stdin = io.StringIO(stdin_text)
        with contextlib.redirect_stdout(stdout), contextlib.redirect_stderr(stderr):
            exec(compiled, namespace, namespace)
        result = {
            "ok": True, "stdout": stdout.getvalue(), "stderr": stderr.getvalue(),
            "stdoutTruncated": stdout.truncated, "stderrTruncated": stderr.truncated,
            "errorType": None, "traceback": None,
            "ramSlots": _ram_slots(namespace),
        }
    except BaseException as error:
        result = {
            "ok": False, "stdout": stdout.getvalue(), "stderr": stderr.getvalue(),
            "stdoutTruncated": stdout.truncated, "stderrTruncated": stderr.truncated,
            "errorType": type(error).__name__,
            "traceback": traceback.format_exc()[-MAX_OUTPUT_CHARS:],
            "ramSlots": _ram_slots(namespace),
        }
    finally:
        sys.stdin = original_stdin
        os.chdir(original_cwd)
    return json.dumps(result)
