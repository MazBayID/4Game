import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import { loadPyodide } from 'pyodide';
import { INITIAL_VFS, MESSAGE_FRAGMENT_ASSET } from '../src/data/protocolData';
import { listDirectory, resolveVfsPath } from '../src/lib/terminalEngine';
import type { PythonExecutionResult } from '../src/types/protocol';

async function runTests() {
  console.log('1. Testing VFS path resolution and immutable evidence logs...');
  assert.equal(
    resolveVfsPath('/home/operative/practice', '../workspace/signal_check.py'),
    '/home/operative/workspace/signal_check.py'
  );
  const practiceItems = listDirectory(INITIAL_VFS, '/home/operative/practice');
  assert.equal(practiceItems.some((i) => i.name === 'practice_note.txt'), true);
  assert.equal(INITIAL_VFS['/aegis/records/rx14_aegis_receipt.log'].immutable, true);
  assert.equal(INITIAL_VFS['/aegis/archive/rx14_archive_receipt.log'].immutable, true);

  console.log('2. Loading local Pyodide 3.14 runtime with Cyber Protocol VFS...');
  const pyodide = await loadPyodide({
    indexURL: path.resolve(process.cwd(), 'node_modules/pyodide'),
  });

  const allFiles = [
    ...Object.values(INITIAL_VFS).filter((n) => n.type === 'file'),
    { path: MESSAGE_FRAGMENT_ASSET.path, content: MESSAGE_FRAGMENT_ASSET.content },
  ];
  for (const file of allFiles) {
    const dir = file.path.slice(0, file.path.lastIndexOf('/'));
    if (dir) pyodide.FS.mkdirTree(dir);
    pyodide.FS.writeFile(file.path, file.content || '', { encoding: 'utf8' });
  }

  const workerSrc = fs.readFileSync(
    path.resolve(process.cwd(), 'public/workers/python-sandbox.worker.js'),
    'utf8'
  );
  const match = workerSrc.match(/const PYTHON_BOOTSTRAP = `([\s\S]*?)`;/);
  assert.ok(match, 'Could not locate PYTHON_BOOTSTRAP in worker file');
  const bootstrapCode = match[1]
    .replace('${MAX_OUTPUT_CHARS}', '32768')
    .replace('${MAX_OUTPUT_LINES}', '400');
  await pyodide.runPythonAsync(bootstrapCode);

  const execPython = (code: string): PythonExecutionResult => {
    pyodide.globals.set('__ct_user_code__', code);
    const raw = pyodide.runPython('_cybertrace_exec(__ct_user_code__)');
    pyodide.globals.delete('__ct_user_code__');
    return { ...JSON.parse(raw), durationMs: 1 };
  };

  console.log('3. Testing Operation 01A Signal Task variable check (rejects hardcoded print)...');
  const hardcoded = execPython('print("AEGIS-774-ALPHA")');
  assert.equal(hardcoded.ok, true);
  assert.equal(hardcoded.variables.some((v) => v.name === 'signal'), false);

  const validSignal = execPython('signal = "AEGIS-774-ALPHA"\nprint(signal)');
  assert.equal(validSignal.ok, true);
  const sigVar = validSignal.variables.find((v) => v.name === 'signal');
  assert.equal(sigVar?.raw, 'AEGIS-774-ALPHA');
  assert.equal(validSignal.stdout.trim(), 'AEGIS-774-ALPHA');

  console.log('4. Testing optional RX-14 Python timestamp comparison script...');
  const rx14Res = execPython(INITIAL_VFS['/home/operative/workspace/inspect_rx14.py'].content || '');
  assert.equal(rx14Res.ok, true);
  assert.equal(rx14Res.stdout.includes('4 seconds'), true);

  console.log('5. Testing sandbox security boundaries (blocked socket + output cap)...');
  const blockedSocket = execPython('import socket');
  assert.equal(blockedSocket.ok, false);
  assert.ok(blockedSocket.error?.message.includes('SANDBOX_POLICY'));

  const spamOut = execPython('while True:\n    print("X")');
  assert.equal(spamOut.ok, false);
  assert.ok(spamOut.error?.message.includes('SANDBOX_OUTPUT_LIMIT'));

  console.log('ALL CYBER PROTOCOL PROTOTYPE TESTS PASSED!');
}

runTests().catch((err) => {
  console.error(err);
  process.exit(1);
});
