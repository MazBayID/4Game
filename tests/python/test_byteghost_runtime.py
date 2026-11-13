import json
import unittest

from byteghost_runtime import execute, MAX_CAPTURED_OUTPUT, MAX_SOURCE_CHARS


class ByteGhostRuntimeTests(unittest.TestCase):
    def run_source(self, source):
        return json.loads(execute(source))

    def test_print_output(self):
        result = self.run_source('print("ByteGhost online")')
        self.assertTrue(result["ok"])
        self.assertEqual(result["stdout"], "ByteGhost online\n")
        self.assertIsNone(result["errorType"])

    def test_stderr_capture(self):
        result = self.run_source('import sys\nprint("diagnostic", file=sys.stderr)')
        self.assertTrue(result["ok"])
        self.assertEqual(result["stderr"], "diagnostic\n")

    def test_syntax_error_is_reported(self):
        result = self.run_source("if True print('broken')")
        self.assertFalse(result["ok"])
        self.assertEqual(result["errorType"], "SyntaxError")
        self.assertIsNotNone(result["traceback"])

    def test_runtime_error_is_reported(self):
        result = self.run_source("print(missing_byteghost_name)")
        self.assertFalse(result["ok"])
        self.assertEqual(result["errorType"], "NameError")

    def test_output_is_bounded(self):
        result = self.run_source("print('X' * %d)" % (MAX_CAPTURED_OUTPUT * 2))
        self.assertTrue(result["ok"])
        self.assertEqual(len(result["stdout"]), MAX_CAPTURED_OUTPUT)
        self.assertTrue(result["stdoutTruncated"])

    def test_oversized_source_is_rejected(self):
        result = self.run_source(" " * (MAX_SOURCE_CHARS + 1))
        self.assertFalse(result["ok"])
        self.assertEqual(result["errorType"], "SourceTooLarge")

    def test_each_run_gets_a_fresh_namespace(self):
        first = self.run_source("secret_value = 42")
        second = self.run_source("print(secret_value)")
        self.assertTrue(first["ok"])
        self.assertFalse(second["ok"])
        self.assertEqual(second["errorType"], "NameError")


if __name__ == "__main__":
    unittest.main()
