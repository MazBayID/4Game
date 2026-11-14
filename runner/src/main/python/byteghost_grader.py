"""AST-assisted semantic checks for ByteGhost's original seven missions.

This is an educational grader, not a security boundary. It executes submitted
Python through the isolated companion runner and must only be used in the isolated runner
once that runner is implemented and adversarially tested.
"""
import ast
import json

from byteghost_runner import MAX_SOURCE_CHARS, execute


def _assigned_value(tree, name):
    for node in ast.walk(tree):
        if isinstance(node, (ast.Assign, ast.AnnAssign)):
            targets = node.targets if isinstance(node, ast.Assign) else [node.target]
            if any(isinstance(target, ast.Name) and target.id == name for target in targets):
                return node.value
    return None


def _is_int(node, value):
    return isinstance(node, ast.Constant) and type(node.value) is int and node.value == value


def _has_print(tree, expected):
    for node in ast.walk(tree):
        if not isinstance(node, ast.Call) or not isinstance(node.func, ast.Name) or node.func.id != "print":
            continue
        if len(node.args) != 1:
            continue
        arg = node.args[0]
        if isinstance(arg, ast.Constant) and arg.value == expected:
            return True
        if isinstance(arg, ast.Name) and arg.id == expected:
            return True
    return False


def _has_len_print(tree, name):
    return any(
        isinstance(node, ast.Call)
        and isinstance(node.func, ast.Name)
        and node.func.id == "print"
        and len(node.args) == 1
        and isinstance(node.args[0], ast.Call)
        and isinstance(node.args[0].func, ast.Name)
        and node.args[0].func.id == "len"
        and len(node.args[0].args) == 1
        and isinstance(node.args[0].args[0], ast.Name)
        and node.args[0].args[0].id == name
        for node in ast.walk(tree)
    )


def _grade_structure(module_id, tree):
    if module_id == 1:
        agent = _assigned_value(tree, "agent")
        clearance = _assigned_value(tree, "clearance")
        return (
            isinstance(agent, ast.Constant) and agent.value == "Null-Agent"
            and _is_int(clearance, 1)
            and _has_print(tree, "agent"),
            "Create agent='Null-Agent', clearance=1, and print(agent).",
        )

    if module_id == 2:
        threat = _assigned_value(tree, "threat")
        if not _is_int(threat, 25):
            return False, "Set threat to 25."
        for node in ast.walk(tree):
            if not isinstance(node, ast.If) or not isinstance(node.test, ast.Compare):
                continue
            test = node.test
            if not (
                isinstance(test.left, ast.Name) and test.left.id == "threat"
                and len(test.ops) == 1 and isinstance(test.ops[0], ast.GtE)
                and len(test.comparators) == 1 and _is_int(test.comparators[0], 50)
            ):
                continue
            if _has_print(ast.Module(body=node.body, type_ignores=[]), "DENIED") and _has_print(
                ast.Module(body=node.orelse, type_ignores=[]), "ALLOW"
            ):
                return True, ""
        return False, "Use if threat >= 50 with DENIED in the if branch and ALLOW in else."

    if module_id == 3:
        shards = _assigned_value(tree, "shards")
        vault = _assigned_value(tree, "vault")
        has_initial_shards = (
            isinstance(shards, ast.List)
            and [item.value for item in shards.elts if isinstance(item, ast.Constant)]
            == ["K1", "K2"]
            and len(shards.elts) == 2
        )
        has_vault = (
            isinstance(vault, ast.Dict)
            and any(
                isinstance(key, ast.Constant) and key.value == "code" and _is_int(value, 77)
                for key, value in zip(vault.keys, vault.values)
            )
        )
        has_append = any(
            isinstance(node, ast.Call)
            and isinstance(node.func, ast.Attribute)
            and isinstance(node.func.value, ast.Name)
            and node.func.value.id == "shards"
            and node.func.attr == "append"
            and len(node.args) == 1
            and isinstance(node.args[0], ast.Constant)
            and node.args[0].value == "K3"
            for node in ast.walk(tree)
        )
        return has_initial_shards and has_vault and has_append and _has_len_print(tree, "shards"), (
            "Build shards with K1/K2, append K3, create vault={'code': 77}, and print len(shards)."
        )

    if module_id == 4:
        packets = _assigned_value(tree, "packets")
        has_loop = any(
            isinstance(node, ast.For)
            and isinstance(node.iter, ast.Call)
            and isinstance(node.iter.func, ast.Name)
            and node.iter.func.id == "range"
            and len(node.iter.args) == 1
            and _is_int(node.iter.args[0], 5)
            and any(
                isinstance(child, ast.AugAssign)
                and isinstance(child.target, ast.Name)
                and child.target.id == "packets"
                and isinstance(child.op, ast.Add)
                and _is_int(child.value, 1)
                for statement in node.body for child in ast.walk(statement)
            )
            for node in ast.walk(tree)
        )
        return _is_int(packets, 0) and has_loop and _has_print(tree, "packets"), (
            "Initialize packets=0, increment it inside a for-loop over range(5), then print packets."
        )

    if module_id == 5:
        function = next(
            (node for node in tree.body if isinstance(node, (ast.FunctionDef, ast.AsyncFunctionDef))
             and node.name == "craft_token"),
            None,
        )
        if function is None or len(function.args.args) != 1 or function.args.args[0].arg != "seed":
            return False, "Define craft_token(seed)."
        returned = next((node for node in ast.walk(function) if isinstance(node, ast.Return)), None)
        valid_return = (
            returned is not None
            and isinstance(returned.value, ast.BinOp)
            and isinstance(returned.value.op, ast.Add)
            and _is_int(returned.value.right, 9)
            and isinstance(returned.value.left, ast.BinOp)
            and isinstance(returned.value.left.op, ast.Mult)
            and isinstance(returned.value.left.left, ast.Name)
            and returned.value.left.left.id == "seed"
            and _is_int(returned.value.left.right, 3)
        )
        return valid_return, "Return seed * 3 + 9 from craft_token(seed)."

    if module_id == 6:
        has_try_open = any(
            isinstance(node, ast.Try)
            and any(
                isinstance(child, ast.Call)
                and isinstance(child.func, ast.Name)
                and child.func.id == "open"
                and child.args
                and isinstance(child.args[0], ast.Constant)
                and child.args[0].value == "corrupt.log"
                for statement in node.body for child in ast.walk(statement)
            )
            and bool(node.handlers)
            and any(
                isinstance(child, ast.Call)
                and isinstance(child.func, ast.Name)
                and child.func.id == "print"
                and any(isinstance(arg, ast.Constant) and arg.value == "SAFE_EXTRACT" for arg in child.args)
                for handler in node.handlers for statement in handler.body for child in ast.walk(statement)
            )
            for node in ast.walk(tree)
        )
        return has_try_open, "Open corrupt.log inside try and print SAFE_EXTRACT from an except handler."

    if module_id == 7:
        cls = next((node for node in tree.body if isinstance(node, ast.ClassDef) and node.name == "OverlordOverride"), None)
        if cls is None:
            return False, "Declare class OverlordOverride."
        init = next((node for node in cls.body if isinstance(node, ast.FunctionDef) and node.name == "__init__"), None)
        shutdown = next((node for node in cls.body if isinstance(node, ast.FunctionDef) and node.name == "shutdown"), None)
        if init is None or shutdown is None:
            return False, "Define __init__(self, key) and shutdown(self)."
        init_ok = (
            len(init.args.args) == 2
            and init.args.args[0].arg == "self"
            and init.args.args[1].arg == "key"
            and any(
                isinstance(node, ast.Assign)
                and any(
                    isinstance(target, ast.Attribute)
                    and isinstance(target.value, ast.Name)
                    and target.value.id == "self"
                    and target.attr == "key"
                    for target in node.targets
                )
                and isinstance(node.value, ast.Name)
                and node.value.id == "key"
                for node in ast.walk(init)
            )
        )
        shutdown_ok = (
            len(shutdown.args.args) == 1
            and shutdown.args.args[0].arg == "self"
            and any(
                isinstance(node, ast.Return)
                and isinstance(node.value, ast.Constant)
                and node.value.value == "ROOT_UNLOCKED"
                for node in ast.walk(shutdown)
            )
        )
        return init_ok and shutdown_ok, "Store key on self.key and return ROOT_UNLOCKED from shutdown."

    return False, "Unknown mission."


def grade(module_id, source, stdin_text="", workspace="."):
    """Run a mission and return JSON with CPython output and AST-based checks."""
    try:
        module_id = int(module_id)
    except (TypeError, ValueError):
        module_id = -1
    if module_id not in range(1, 8):
        return json.dumps({
            "ok": False, "passed": False, "stdout": "", "stderr": "",
            "errorType": "UnknownMission", "feedback": "Unknown mission.",
        })
    if not isinstance(source, str):
        source = str(source)
    if len(source) > MAX_SOURCE_CHARS:
        return json.dumps({
            "ok": False, "passed": False, "stdout": "", "stderr": "",
            "errorType": "SourceTooLarge", "feedback": "Source exceeds the mission limit.",
        })

    try:
        tree = ast.parse(source)
    except SyntaxError as error:
        return json.dumps({
            "ok": False,
            "passed": False,
            "stdout": "",
            "stderr": "",
            "errorType": "SyntaxError",
            "feedback": f"Syntax error on line {error.lineno}: {error.msg}",
        })

    passed_structure, feedback = _grade_structure(module_id, tree)
    execution_source = source
    if module_id == 5:
        execution_source += "\nprint(craft_token(7))\nprint(craft_token(-2))"
    elif module_id == 7:
        execution_source += '\nprint(OverlordOverride("TEST-KEY").shutdown())'

    execution = json.loads(execute(execution_source, stdin_text, workspace))
    output_complete = not execution.get("stdoutTruncated", False) and not execution.get("stderrTruncated", False)
    passed = bool(execution.get("ok")) and output_complete and passed_structure

    if not output_complete:
        feedback = "Output limit reached; remove excessive output and retry."
    if module_id == 4 and execution.get("stdout", "").splitlines() != ["5"]:
        passed = False
        feedback = "The loop must finish with packets equal to 5."
    if module_id == 5:
        lines = execution.get("stdout", "").splitlines()
        if lines[-2:] != ["30", "3"]:
            passed = False
            feedback = "craft_token must return 30 for seed 7 and 3 for seed -2."
    if module_id == 6 and "SAFE_EXTRACT" not in execution.get("stdout", ""):
        passed = False
        feedback = "The except handler must print SAFE_EXTRACT when corrupt.log is missing."
    if module_id == 7 and execution.get("stdout", "").splitlines()[-1:] != ["ROOT_UNLOCKED"]:
        passed = False
        feedback = "shutdown() must return ROOT_UNLOCKED for a different instance key."

    return json.dumps({
        "ok": bool(execution.get("ok")),
        "passed": passed,
        "stdout": execution.get("stdout", ""),
        "stderr": execution.get("stderr", ""),
        "errorType": execution.get("errorType"),
        "traceback": execution.get("traceback"),
        "feedback": "" if passed else (feedback or "The mission checks were not satisfied."),
    })
