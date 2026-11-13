"""Minimal CPython execution PoC; not a security sandbox."""
import contextlib
import io
import json
import traceback

MAX_CAPTURED_OUTPUT = 64 * 1024

def execute(source: str) -> str:
    stdout = io.StringIO()
    stderr = io.StringIO()
    namespace = {"__name__": "__byteghost_submission__"}
    try:
        compiled = compile(source, "<byteghost-submission>", "exec")
        with contextlib.redirect_stdout(stdout), contextlib.redirect_stderr(stderr):
            exec(compiled, namespace, namespace)
        result = {
            "ok": True,
            "stdout": stdout.getvalue()[:MAX_CAPTURED_OUTPUT],
            "stderr": stderr.getvalue()[:MAX_CAPTURED_OUTPUT],
            "errorType": None,
            "traceback": None,
        }
    except BaseException as error:
        result = {
            "ok": False,
            "stdout": stdout.getvalue()[:MAX_CAPTURED_OUTPUT],
            "stderr": stderr.getvalue()[:MAX_CAPTURED_OUTPUT],
            "errorType": type(error).__name__,
            "traceback": traceback.format_exc()[-MAX_CAPTURED_OUTPUT:],
        }
    return json.dumps(result)
