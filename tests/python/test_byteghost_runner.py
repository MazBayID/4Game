import io
import json
import os
import tempfile
import unittest
from contextlib import redirect_stdout
from pathlib import Path

import byteghost_runner


class ByteGhostRunnerTests(unittest.TestCase):
    def test_executes_source_and_captures_stdout(self):
        result = json.loads(byteghost_runner.execute("print('RUNNER_TEST_OK')"))
        self.assertTrue(result["ok"])
        self.assertEqual("RUNNER_TEST_OK\n", result["stdout"])

    def test_caps_output_while_writing(self):
        result = json.loads(byteghost_runner.execute("print('X' * 70000)"))
        self.assertTrue(result["ok"])
        self.assertEqual(64 * 1024, len(result["stdout"]))
        self.assertTrue(result["stdoutTruncated"])

    def test_stdin_and_workspace_are_scoped(self):
        original_cwd = os.getcwd()
        with tempfile.TemporaryDirectory() as workspace:
            result = json.loads(byteghost_runner.execute(
                "name = input()\nwith open('temporary.txt', 'w') as f: f.write(name)\nprint(name)",
                "Zero\n",
                workspace,
            ))
            self.assertTrue(result["ok"])
            self.assertEqual("Zero\n", result["stdout"])
            self.assertEqual("Zero", Path(workspace, "temporary.txt").read_text())
        self.assertEqual(original_cwd, os.getcwd())

    def test_reports_python_errors(self):
        result = json.loads(byteghost_runner.execute("raise ValueError('runner failure')"))
        self.assertFalse(result["ok"])
        self.assertEqual("ValueError", result["errorType"])
        self.assertIn("runner failure", result["traceback"])

    def test_rejects_oversized_source_before_execution(self):
        result = json.loads(byteghost_runner.execute(" " * (128 * 1024 + 1)))
        self.assertFalse(result["ok"])
        self.assertEqual("SourceTooLarge", result["errorType"])


if __name__ == "__main__":
    unittest.main()
