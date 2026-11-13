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
import sys, io, json, traceback, re, tokenize, ast

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

def _neonull_strip_comments(source):
    try:
        tokens = tokenize.generate_tokens(io.StringIO(source).readline)
        chars = [list(line) for line in source.splitlines(keepends=True)]
        for token in tokens:
            if token.type == tokenize.COMMENT:
                row, col = token.start
                end_row, end_col = token.end
                if row == end_row:
                    for index in range(col, end_col):
                        if chars[row - 1][index] not in '\\r\\n':
                            chars[row - 1][index] = ' '
        return ''.join(''.join(line) for line in chars)
    except (tokenize.TokenError, IndentationError):
        return source


def _neonull_has_mission_structure(module_id, source):
    try:
        tree = ast.parse(source)
    except (SyntaxError, IndentationError):
        return False

    def is_print_string(node, value):
        return (
            isinstance(node, ast.Expr)
            and isinstance(node.value, ast.Call)
            and isinstance(node.value.func, ast.Name)
            and node.value.func.id == "print"
            and len(node.value.args) == 1
            and isinstance(node.value.args[0], ast.Constant)
            and node.value.args[0].value == value
        )

    def contains_open_corrupt(nodes):
        for node in ast.walk(ast.Module(body=nodes, type_ignores=[])):
            call = node if isinstance(node, ast.Call) else None
            if call and isinstance(call.func, ast.Name) and call.func.id == "open":
                if call.args and isinstance(call.args[0], ast.Constant) and call.args[0].value == "corrupt.log":
                    return True
        return False

    if module_id == 2:
        for node in ast.walk(tree):
            if not isinstance(node, ast.If):
                continue
            test = node.test
            valid_test = (
                isinstance(test, ast.Compare)
                and isinstance(test.left, ast.Name) and test.left.id == "threat"
                and len(test.ops) == 1 and isinstance(test.ops[0], ast.GtE)
                and len(test.comparators) == 1
                and isinstance(test.comparators[0], ast.Constant)
                and test.comparators[0].value == 50
            )
            if valid_test and any(is_print_string(item, "DENIED") for item in node.body) and any(is_print_string(item, "ALLOW") for item in node.orelse):
                return True
        return False

    if module_id == 4:
        for node in ast.walk(tree):
            if not isinstance(node, ast.For) or not isinstance(node.iter, ast.Call):
                continue
            if not isinstance(node.iter.func, ast.Name) or node.iter.func.id != "range":
                continue
            if len(node.iter.args) != 1 or not isinstance(node.iter.args[0], ast.Constant) or node.iter.args[0].value != 5:
                continue
            for statement in node.body:
                if isinstance(statement, ast.AugAssign) and isinstance(statement.target, ast.Name) and statement.target.id == "packets" and isinstance(statement.op, ast.Add) and isinstance(statement.value, ast.Constant) and statement.value.value == 1:
                    return True
                if isinstance(statement, ast.Assign) and len(statement.targets) == 1 and isinstance(statement.targets[0], ast.Name) and statement.targets[0].id == "packets":
                    value = statement.value
                    if isinstance(value, ast.BinOp) and isinstance(value.left, ast.Name) and value.left.id == "packets" and isinstance(value.op, ast.Add) and isinstance(value.right, ast.Constant) and value.right.value == 1:
                        return True
        return False

    if module_id == 5:
        for node in ast.walk(tree):
            if not isinstance(node, (ast.FunctionDef, ast.AsyncFunctionDef)) or node.name != "craft_token":
                continue
            if len(node.args.args) != 1 or node.args.args[0].arg != "seed":
                continue
            for statement in node.body:
                if not isinstance(statement, ast.Return):
                    continue
                value = statement.value
                if (
                    isinstance(value, ast.BinOp) and isinstance(value.op, ast.Add)
                    and isinstance(value.left, ast.BinOp) and isinstance(value.left.op, ast.Mult)
                    and isinstance(value.left.left, ast.Name) and value.left.left.id == "seed"
                    and isinstance(value.left.right, ast.Constant) and value.left.right.value == 3
                    and isinstance(value.right, ast.Constant) and value.right.value == 9
                ):
                    return True
        return False

    if module_id == 6:
        for node in ast.walk(tree):
            if not isinstance(node, ast.Try):
                continue
            if not contains_open_corrupt(node.body):
                continue
            if any(is_print_string(statement, "SAFE_EXTRACT") for handler in node.handlers for statement in handler.body):
                return True
        return False

    if module_id == 7:
        for node in ast.walk(tree):
            if not isinstance(node, ast.ClassDef) or node.name != "OverlordOverride":
                continue
            methods = {item.name: item for item in node.body if isinstance(item, ast.FunctionDef)}
            init = methods.get("__init__")
            shutdown = methods.get("shutdown")
            if init is None or shutdown is None:
                continue
            init_ok = any(
                isinstance(item, ast.Assign) and len(item.targets) == 1
                and isinstance(item.targets[0], ast.Attribute)
                and isinstance(item.targets[0].value, ast.Name) and item.targets[0].value.id == "self"
                and item.targets[0].attr == "key"
                and isinstance(item.value, ast.Name) and item.value.id == "key"
                for item in init.body
            )
            shutdown_ok = any(
                isinstance(item, ast.Return) and isinstance(item.value, ast.Constant)
                and item.value.value == "ROOT_UNLOCKED"
                for item in shutdown.body
            )
            if init_ok and shutdown_ok:
                return True
        return False

    return True

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
        normalized_source = _neonull_strip_comments(user_code)

        if module_id == 0:
            # Free-play sandbox mode
            passed = True
            check_detail = "SANDBOX_OK"
        elif module_id == 1:
            user_code = normalized_source
            ok_agent = ns.get("agent") == "Null-Agent"
            ok_clear = ns.get("clearance") == 1
            ok_out = "Null-Agent" in output_str.splitlines()
            passed = bool(ok_agent and ok_clear and ok_out)
            if not passed:
                check_detail = 'Expected agent = "Null-Agent", clearance = 1, and print(agent).'
        elif module_id == 2:
            user_code = normalized_source
            structure_ok = _neonull_has_mission_structure(module_id, user_code)
            threat_val = ns.get("threat")
            has_else = bool(re.search(r"\belse\s*:", user_code))
            has_if = bool(re.search(r"\bif\s+threat\s*>=\s*50\s*:", user_code))
            passed = bool(structure_ok and isinstance(threat_val, int) and 0 <= threat_val < 50 and has_if and has_else and output_str == "ALLOW")
            if not passed:
                check_detail = 'Expected threat < 50, if threat >= 50 / else branch, and output "ALLOW".'
        elif module_id == 3:
            user_code = normalized_source
            shards = ns.get("shards")
            vault = ns.get("vault")
            passed = bool(isinstance(shards, list) and shards == ["K1", "K2", "K3"] and isinstance(vault, dict) and vault.get("code") == 77 and output_str == "3")
            if not passed:
                check_detail = 'Ensure shards == ["K1", "K2", "K3"], vault == {"code": 77}, and output == 3.'
        elif module_id == 4:
            user_code = normalized_source
            structure_ok = _neonull_has_mission_structure(module_id, user_code)
            has_for = bool(re.search(r"for\s+\w+\s+in\s+range\s*\(\s*5\s*\)\s*:", user_code))
            passed = bool(structure_ok and has_for and ns.get("packets") == 5 and output_str == "5")
            if not passed:
                check_detail = 'Use a real for i in range(5): loop incrementing packets to 5 and printing it.'
        elif module_id == 5:
            user_code = normalized_source
            structure_ok = _neonull_has_mission_structure(module_id, user_code)
            fn = ns.get("craft_token")
            if callable(fn):
                passed = bool(structure_ok and fn(7) == 30 and fn(10) == 39 and output_str == "30")
            if not passed:
                check_detail = 'Define craft_token(seed) returning seed * 3 + 9 and print craft_token(7).'
        elif module_id == 6:
            user_code = normalized_source
            structure_ok = _neonull_has_mission_structure(module_id, user_code)
            has_try = bool(re.search(r"\btry\s*:", user_code))
            has_except = bool(re.search(r"\bexcept\b.*:", user_code))
            passed = bool(structure_ok and has_try and has_except and output_str == "SAFE_EXTRACT")
            if not passed:
                check_detail = 'Catch the missing file exception with try / except and print "SAFE_EXTRACT".'
        elif module_id == 7:
            user_code = normalized_source
            structure_ok = _neonull_has_mission_structure(module_id, user_code)
            cls = ns.get("OverlordOverride")
            core = ns.get("core")
            if isinstance(cls, type) and core is not None and isinstance(core, cls):
                probe = cls("PROBE-X")
                passed = bool(structure_ok and getattr(probe, "key", None) == "PROBE-X" and probe.shutdown() == "ROOT_UNLOCKED" and output_str == "ROOT_UNLOCKED")
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
