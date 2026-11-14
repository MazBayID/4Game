import json
import unittest

from byteghost_grader import grade


VALID_SUBMISSIONS = {
    1: '''agent = "Null-Agent"
clearance = 1
print(agent)
''',
    2: '''threat = 25
if threat >= 50:
    print("DENIED")
else:
    print("ALLOW")
''',
    3: '''shards = ["K1", "K2"]
shards.append("K3")
vault = {"code": 77}
print(len(shards))
''',
    4: '''packets = 0
for packet in range(5):
    packets += 1
print(packets)
''',
    5: '''def craft_token(seed):
    return seed * 3 + 9

print(craft_token(7))
''',
    6: '''try:
    with open("corrupt.log", "r") as log_file:
        print(log_file.read())
except FileNotFoundError:
    print("SAFE_EXTRACT")
''',
    7: '''class OverlordOverride:
    def __init__(self, key):
        self.key = key

    def shutdown(self):
        return "ROOT_UNLOCKED"

core = OverlordOverride("NULL-2142")
print(core.shutdown())
''',
}


class ByteGhostGraderTests(unittest.TestCase):
    def test_all_seven_valid_missions_pass(self):
        for module_id, source in VALID_SUBMISSIONS.items():
            with self.subTest(module_id=module_id):
                result = json.loads(grade(module_id, source))
                self.assertTrue(result["ok"], result)
                self.assertTrue(result["passed"], result)
                self.assertEqual(result["feedback"], "")

    def test_wrong_variable_value_fails_module_one(self):
        result = json.loads(grade(1, VALID_SUBMISSIONS[1].replace("clearance = 1", "clearance = 0")))
        self.assertFalse(result["passed"])

    def test_wrong_branch_output_fails_module_two(self):
        source = VALID_SUBMISSIONS[2].replace('print("ALLOW")', 'print("BLOCK")')
        result = json.loads(grade(2, source))
        self.assertFalse(result["passed"])

    def test_missing_append_fails_module_three(self):
        source = VALID_SUBMISSIONS[3].replace('shards.append("K3")', 'shards.append("K4")')
        result = json.loads(grade(3, source))
        self.assertFalse(result["passed"])

    def test_printing_list_instead_of_its_length_fails_module_three(self):
        source = VALID_SUBMISSIONS[3].replace("print(len(shards))", "print(shards)")
        result = json.loads(grade(3, source))
        self.assertFalse(result["passed"])

    def test_wrong_loop_count_fails_module_four(self):
        source = VALID_SUBMISSIONS[4].replace("range(5)", "range(4)")
        result = json.loads(grade(4, source))
        self.assertFalse(result["passed"])

    def test_extra_output_does_not_pass_module_four(self):
        source = VALID_SUBMISSIONS[4] + '\nprint("5")\n'
        result = json.loads(grade(4, source))
        self.assertFalse(result["passed"])

    def test_wrong_function_formula_fails_module_five(self):
        source = VALID_SUBMISSIONS[5].replace("seed * 3 + 9", "seed * 3 + 8")
        result = json.loads(grade(5, source))
        self.assertFalse(result["passed"])

    def test_missing_exception_handler_fails_module_six(self):
        source = """with open("corrupt.log") as log_file:
    print(log_file.read())"""
        result = json.loads(grade(6, source))
        self.assertFalse(result["passed"])

    def test_wrong_class_return_fails_module_seven(self):
        source = VALID_SUBMISSIONS[7].replace('"ROOT_UNLOCKED"', '"NOPE"')
        result = json.loads(grade(7, source))
        self.assertFalse(result["passed"])

    def test_syntax_error_is_reported_without_execution(self):
        result = json.loads(grade(1, 'if True print("broken")'))
        self.assertFalse(result["ok"])
        self.assertFalse(result["passed"])
        self.assertEqual(result["errorType"], "SyntaxError")


if __name__ == "__main__":
    unittest.main()
