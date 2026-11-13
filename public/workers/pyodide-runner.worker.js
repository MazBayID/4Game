/* eslint-disable no-restricted-globals */
// Isolated Web Worker running CPython 3.14 via Pyodide for NEO-NULL

const originalFetch = self.fetch ? self.fetch.bind(self) : null;
let pyodideInitialized = false;

self.fetch = (input, init) => {
  const url = typeof input === 'string' ? input : (input && input.url) || '';
  const isLocalPyodide =
    !pyodideInitialized &&
    (url.startsWith('/pyodide/') ||
      (self.location && url.startsWith(self.location.origin + '/pyodide/')));
  if (isLocalPyodide && originalFetch) {
    return originalFetch(input, init);
  }
  return Promise.reject(new Error('Network disabled in NEO-NULL sandbox'));
};
self.XMLHttpRequest = function () {
  throw new Error('Network disabled in NEO-NULL sandbox');
};
self.WebSocket = function () {
  throw new Error('Network disabled in NEO-NULL sandbox');
};

importScripts('/pyodide/pyodide.js');

let pyodideReadyPromise = null;

async function ensurePyodide() {
  if (!pyodideReadyPromise) {
    pyodideReadyPromise = loadPyodide({ indexURL: '/pyodide/' }).then(async (py) => {
      pyodideInitialized = true;
      await py.runPythonAsync(`
import sys, io, json, traceback, re

def _neonull_serialize_value(val):
    tname = type(val).__name__
    if isinstance(val, (int, float, bool)) or val is None:
        return {"type": tname, "preview": repr(val), "items": None}
    if isinstance(val, str):
        return {"type": "str", "preview": repr(val), "items": None}
    if isinstance(val, (list, tuple, set)):
        seq = list(val)[:12]
        return {"type": tname, "preview": repr(val), "items": [repr(x) for x in seq]}
    if isinstance(val, dict):
        pairs = list(val.items())[:12]
        return {"type": "dict", "preview": repr(val), "items": [f"{repr(k)}: {repr(v)}" for k, v in pairs]}
    if callable(val):
        return {"type": "function", "preview": f"<fn {getattr(val, '__name__', 'obj')}()>", "items": None}
    if hasattr(val, "__dict__"):
        attrs = [f"{k}={repr(v)}" for k, v in list(val.__dict__.items())[:8]]
        return {"type": f"instance:{tname}", "preview": f"{tname}({', '.join(attrs)})", "items": attrs}
    return {"type": tname, "preview": repr(val), "items": None}

def _neonull_run(user_code, module_id):
    stdout_buf = io.StringIO()
    old_stdout = sys.stdout
    sys.stdout = stdout_buf
    ns = {"__name__": "__main__"}
    error_msg = None
    error_lines = []
    is_syntax_error = False
    passed = False
    check_detail = ""
    ram_snapshot = []

    try:
        compiled = compile(user_code, "<neonull>", "exec")
        exec(compiled, ns, ns)

        # Snapshot user variables for RAM Inspector
        for k, v in ns.items():
            if k.startswith("_") or k == "__builtins__":
                continue
            info = _neonull_serialize_value(v)
            ram_snapshot.append({
                "name": k,
                "type": info["type"],
                "preview": info["preview"],
                "items": info["items"],
            })

        output_str = stdout_buf.getvalue().strip()

        if module_id == 0:
            # Free-play sandbox mode
            passed = True
            check_detail = "SANDBOX_OK"
        elif module_id == 1:
            ok_agent = ns.get("agent") == "Null-Agent"
            ok_clear = ns.get("clearance") == 1
            ok_out = "Null-Agent" in output_str.splitlines()
            passed = bool(ok_agent and ok_clear and ok_out)
            if not passed:
                check_detail = 'Expected agent = "Null-Agent", clearance = 1, and print(agent).'
        elif module_id == 2:
            threat_val = ns.get("threat")
            has_else = bool(re.search(r"\belse\s*:", user_code))
            has_if = bool(re.search(r"\bif\s+threat\s*>=\s*50\s*:", user_code))
            passed = bool(isinstance(threat_val, int) and 0 <= threat_val < 50 and has_if and has_else and output_str == "ALLOW")
            if not passed:
                check_detail = 'Expected threat < 50, if threat >= 50 / else branch, and output "ALLOW".'
        elif module_id == 3:
            shards = ns.get("shards")
            vault = ns.get("vault")
            passed = bool(isinstance(shards, list) and shards == ["K1", "K2", "K3"] and isinstance(vault, dict) and vault.get("code") == 77 and output_str == "3")
            if not passed:
                check_detail = 'Ensure shards == ["K1", "K2", "K3"], vault == {"code": 77}, and output == 3.'
        elif module_id == 4:
            has_for = bool(re.search(r"for\s+\w+\s+in\s+range\s*\(\s*5\s*\)\s*:", user_code))
            passed = bool(has_for and ns.get("packets") == 5 and output_str == "5")
            if not passed:
                check_detail = 'Use a real for i in range(5): loop incrementing packets to 5 and printing it.'
        elif module_id == 5:
            fn = ns.get("craft_token")
            if callable(fn):
                passed = bool(fn(7) == 30 and fn(10) == 39 and output_str == "30")
            if not passed:
                check_detail = 'Define craft_token(seed) returning seed * 3 + 9 and print craft_token(7).'
        elif module_id == 6:
            has_try = bool(re.search(r"\btry\s*:", user_code))
            has_except = bool(re.search(r"\bexcept\b.*:", user_code))
            passed = bool(has_try and has_except and output_str == "SAFE_EXTRACT")
            if not passed:
                check_detail = 'Catch the missing file exception with try / except and print "SAFE_EXTRACT".'
        elif module_id == 7:
            cls = ns.get("OverlordOverride")
            core = ns.get("core")
            if isinstance(cls, type) and core is not None and isinstance(core, cls):
                probe = cls("PROBE-X")
                passed = bool(getattr(probe, "key", None) == "PROBE-X" and probe.shutdown() == "ROOT_UNLOCKED" and output_str == "ROOT_UNLOCKED")
            if not passed:
                check_detail = 'Implement OverlordOverride(__init__ + shutdown returning "ROOT_UNLOCKED") and print core.shutdown().'

    except SyntaxError as se:
        is_syntax_error = True
        line_no = se.lineno or 1
        error_lines = [line_no]
        error_msg = f"SyntaxError [Line {line_no}]: {se.msg}"
    except Exception as ex:
        tb_lines = traceback.extract_tb(ex.__traceback__)
        user_lines = [f.lineno for f in tb_lines if f.filename == "<neonull>"]
        error_lines = user_lines if user_lines else [1]
        error_msg = f"{type(ex).__name__} [Line {error_lines[-1]}]: {ex}"
    finally:
        sys.stdout = old_stdout

    return json.dumps({
        "stdout": stdout_buf.getvalue().strip(),
        "passed": passed,
        "isSyntaxError": is_syntax_error,
        "errorMsg": error_msg,
        "errorLines": error_lines,
        "checkDetail": check_detail,
        "ramSnapshot": ram_snapshot,
    })
      `);
      return py;
    });
  }
  return pyodideReadyPromise;
}

self.onmessage = async (event) => {
  const { id, code, moduleId } = event.data;
  try {
    const py = await ensurePyodide();
    py.globals.set('_user_code_arg', code);
    py.globals.set('_module_id_arg', Number(moduleId));
    const jsonResult = await py.runPythonAsync('_neonull_run(_user_code_arg, _module_id_arg)');
    self.postMessage({ id, ok: true, result: JSON.parse(jsonResult) });
  } catch (err) {
    self.postMessage({ id, ok: false, error: err && err.message ? err.message : String(err) });
  }
};
