"""Minimal CPython execution PoC; not a security sandbox."""
import contextlib
import json
import traceback

MAX_SOURCE_CHARS = 128 * 1024
MAX_CAPTURED_OUTPUT = 64 * 1024


class BoundedTextBuffer:
    """Keep captured output bounded even if submitted code prints repeatedly."""

    def __init__(self, limit: int):
        self.limit = limit
        self.parts = []
        self.length = 0
        self.truncated = False

    def write(self, value: str) -> int:
        if not isinstance(value, str):
            raise TypeError("write() argument must be str")
        remaining = self.limit - self.length
        if remaining > 0:
            accepted = value[:remaining]
            if accepted:
                self.parts.append(accepted)
                self.length += len(accepted)
        if len(value) > max(remaining, 0):
            self.truncated = True
        return len(value)

    def flush(self) -> None:
        return None

    def getvalue(self) -> str:
        return "".join(self.parts)


def execute(source: str) -> str:
    if not isinstance(source, str):
        source = str(source)

    if len(source) > MAX_SOURCE_CHARS:
        return json.dumps({
            "ok": False,
            "stdout": "",
            "stderr": "",
            "stdoutTruncated": False,
            "stderrTruncated": False,
            "errorType": "SourceTooLarge",
            "traceback": None,
        })

    stdout = BoundedTextBuffer(MAX_CAPTURED_OUTPUT)
    stderr = BoundedTextBuffer(MAX_CAPTURED_OUTPUT)
    namespace = {"__name__": "__byteghost_submission__"}

    try:
        compiled = compile(source, "<byteghost-submission>", "exec")
        with contextlib.redirect_stdout(stdout), contextlib.redirect_stderr(stderr):
            exec(compiled, namespace, namespace)
        result = {
            "ok": True,
            "stdout": stdout.getvalue(),
            "stderr": stderr.getvalue(),
            "stdoutTruncated": stdout.truncated,
            "stderrTruncated": stderr.truncated,
            "errorType": None,
            "traceback": None,
        }
    except BaseException as error:
        result = {
            "ok": False,
            "stdout": stdout.getvalue(),
            "stderr": stderr.getvalue(),
            "stdoutTruncated": stdout.truncated,
            "stderrTruncated": stderr.truncated,
            "errorType": type(error).__name__,
            "traceback": traceback.format_exc()[-MAX_CAPTURED_OUTPUT:],
        }

    return json.dumps(result)
