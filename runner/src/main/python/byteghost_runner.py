"""Bounded CPython execution for the ByteGhost companion runner."""
import contextlib
import io
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
        }
    except BaseException as error:
        result = {
            "ok": False, "stdout": stdout.getvalue(), "stderr": stderr.getvalue(),
            "stdoutTruncated": stdout.truncated, "stderrTruncated": stderr.truncated,
            "errorType": type(error).__name__,
            "traceback": traceback.format_exc()[-MAX_OUTPUT_CHARS:],
        }
    finally:
        sys.stdin = original_stdin
        os.chdir(original_cwd)
    return json.dumps(result)
