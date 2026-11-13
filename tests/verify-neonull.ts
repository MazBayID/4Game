import { readFileSync } from 'node:fs';
import { loadProgress, saveProgress } from '../src/lib/progressStorage';
import { CURRICULUM_MODULES, UI_STRINGS } from '../src/data/neoNullData';
import { evaluatePythonSubmission, tokenizePythonLine } from '../src/lib/pythonHighlightAndEval';

function assert(cond: boolean, msg: string) {
  if (!cond) throw new Error(`Assertion failed: ${msg}`);
}

// 1. Verify all 3 languages exist on every module and UI string set
for (const lang of ['EN', 'ID', 'PH'] as const) {
  assert(Boolean(UI_STRINGS[lang].victoryTitle), `Missing victoryTitle for ${lang}`);
  for (const mod of CURRICULUM_MODULES) {
    assert(Boolean(mod.title[lang]), `Missing title for module ${mod.id} (${lang})`);
    assert(Boolean(mod.storyBriefing[lang]), `Missing briefing for module ${mod.id} (${lang})`);
  }
}

// 2. Verify syntax tokenizer colors keywords, strings, comments, and OVERLORD alerts
const tokens = tokenizePythonLine('def hack(): # OVERLORD "ok"');
assert(tokens.some((t) => t.kind === 'keyword' && t.text === 'def'), 'Expected def keyword token');
assert(tokens.some((t) => t.kind === 'comment'), 'Expected comment token');

const alertTokens = tokenizePythonLine('OVERLORD = 1');
assert(alertTokens.some((t) => t.kind === 'alert' && t.text === 'OVERLORD'), 'Expected Crimson OVERLORD token');

// 3. Verify syntax error detection triggers Crimson Red errorLines
const brokenCode = 'if threat >= 50\n    print("DENIED")';
const brokenRes = evaluatePythonSubmission(CURRICULUM_MODULES[1], brokenCode, 'EN');
assert(!brokenRes.isSuccess && brokenRes.isSyntaxError, 'Missing colon should fail with syntax error');
assert(brokenRes.errorLines.includes(1), 'Line 1 should be flagged in errorLines');

const twoSpaceIndent = evaluatePythonSubmission(
  CURRICULUM_MODULES[1],
  'threat = 25\nif threat >= 50:\n  print("DENIED")\nelse:\n  print("ALLOW")',
  'EN'
);
assert(twoSpaceIndent.isSuccess, 'Valid Python blocks using two-space indentation must be accepted');

const missingIndent = evaluatePythonSubmission(
  CURRICULUM_MODULES[1],
  'threat = 25\nif threat >= 50:\nprint("DENIED")\nelse:\n  print("ALLOW")',
  'EN'
);
assert(missingIndent.isSyntaxError, 'A Python block without indentation must be rejected');


// 4. Verify passing solutions across all 7 modules
const solutions: Record<number, string> = {
  1: 'agent = "Null-Agent"\nclearance = 1\nprint(agent)',
  2: 'threat = 25\nif threat >= 50:\n    print("DENIED")\nelse:\n    print("ALLOW")',
  3: 'shards = ["K1", "K2"]\nshards.append("K3")\nvault = {"code": 77}\nprint(len(shards))',
  4: 'packets = 0\nfor i in range(5):\n    packets += 1\nprint(packets)',
  5: 'def craft_token(seed):\n    return seed * 3 + 9\nprint(craft_token(7))',
  6: 'try:\n    with open("corrupt.log", "r") as f:\n        print(f.read())\nexcept FileNotFoundError:\n    print("SAFE_EXTRACT")',
  7: 'class OverlordOverride:\n    def __init__(self, key):\n        self.key = key\n    def shutdown(self):\n        return "ROOT_UNLOCKED"\ncore = OverlordOverride("NULL-2142")\nprint(core.shutdown())',
};

for (const mod of CURRICULUM_MODULES) {
  const res = evaluatePythonSubmission(mod, solutions[mod.id], 'ID');
  assert(res.isSuccess, `Module ${mod.id} solution failed: ${res.outputText}`);
}


// 5. Verify mission evaluation reports the first missing learning checkpoint.
const firstCheckpointMessages = [
  'Create the agent variable',
  'Assign a numeric value to threat',
  'Initialize shards',
  'Initialize packets',
  'Define the craft_token',
  'Start exception handling',
  'Declare the OverlordOverride class',
];
for (const mod of CURRICULUM_MODULES) {
  const result = evaluatePythonSubmission(mod, '', 'EN');
  assert(!result.isSuccess, `Empty submission must fail mission ${mod.id}`);
  assert(result.outputText.includes(firstCheckpointMessages[mod.id - 1]), `Mission ${mod.id} should explain its first missing checkpoint`);
  assert(result.errorLines.length === 1 && result.errorLines[0] === 1, `Mission ${mod.id} should flag the first checkpoint line`);
}

const commentOnlyClearance = evaluatePythonSubmission(
  CURRICULUM_MODULES[0],
  'agent = "Null-Agent"\n# clearance = 1\nprint(agent)',
  'EN'
);
assert(commentOnlyClearance.outputText.includes('Set clearance to 1'), 'Commented-out requirements must not satisfy mission checks');
assert(commentOnlyClearance.errorLines.includes(2), 'Missing clearance should flag its expected line');

const invalidThreat = evaluatePythonSubmission(
  CURRICULUM_MODULES[1],
  'threat = 75\nif threat >= 50:\n    print("DENIED")\nelse:\n    print("ALLOW")',
  'EN'
);
assert(invalidThreat.outputText.includes('Set threat below 50'), 'Out-of-range threat values need targeted feedback');

// 6. Verify the production Pyodide worker uses valid Python regex patterns
const workerSource = readFileSync('public/workers/pyodide-runner.worker.js', 'utf8');
assert(workerSource.includes('r"\\belse\\s*:"'), 'Worker else-branch regex must use Python word-boundary syntax');
assert(workerSource.includes('r"\\bif\\s+threat\\s*>=\\s*50\\s*:"'), 'Worker threat-if regex must be valid');
assert(workerSource.includes('r"for\\s+\\w+\\s+in\\s+range\\s*\\(\\s*5\\s*\\)\\s*:"'), 'Worker for-loop regex must be valid');
assert(workerSource.includes('r"\\btry\\s*:"'), 'Worker try regex must be valid');
assert(workerSource.includes('r"\\bexcept\\b.*:"'), 'Worker except regex must be valid');
assert(workerSource.includes('import sys, io, json, traceback, re, tokenize'), 'Worker must tokenize Python comments safely');
assert(workerSource.includes('def _neonull_strip_comments(source):'), 'Worker must strip comments without altering string literals');
assert(workerSource.includes('normalized_source = _neonull_strip_comments(user_code)'), 'Runtime mission checks must use comment-free source');
assert(workerSource.includes('import sys, io, json, traceback, re, tokenize, ast'), 'Worker must import AST for structural mission checks');
assert(workerSource.includes('def _neonull_has_mission_structure(module_id, source):'), 'Worker must validate mission block structure with Python AST');
for (const moduleId of [2, 4, 5, 6, 7]) {
  const expectedStructureCheck = `elif module_id == ${moduleId}:` + String.fromCharCode(10) +
    '            user_code = normalized_source' + String.fromCharCode(10) +
    '            structure_ok = _neonull_has_mission_structure(module_id, user_code)';
  assert(workerSource.includes(expectedStructureCheck), `Mission ${moduleId} must use AST structure validation`);
}

for (const moduleId of [1, 2, 3, 4, 5, 6, 7]) {
  const expectedBranch = `elif module_id == ${moduleId}:` + String.fromCharCode(10) + '            user_code = normalized_source';
  assert(workerSource.includes(expectedBranch), `Module ${moduleId} must ignore commented-out requirements`);
}

const invalidConditionalScope = evaluatePythonSubmission(
  CURRICULUM_MODULES[1],
  'threat = 25\nif threat >= 50:\n    pass\nelse:\n    pass\nprint("DENIED")\nprint("ALLOW")',
  'EN'
);
assert(!invalidConditionalScope.isSuccess, 'Mission 2 must require DENIED and ALLOW inside their matching branches');

const invalidLoopScope = evaluatePythonSubmission(
  CURRICULUM_MODULES[3],
  'packets = 0\nfor i in range(5):\n    print(i)\npackets += 1\nprint(packets)',
  'EN'
);
assert(!invalidLoopScope.isSuccess, 'Mission 4 must reject increments placed outside the loop');

const nestedFakeLoopIncrement = evaluatePythonSubmission(
  CURRICULUM_MODULES[3],
  'packets = 0\\nfor i in range(5):\\n    def fake():\\n        packets += 1\\nprint(packets)',
  'EN'
);
assert(!nestedFakeLoopIncrement.isSuccess, 'Mission 4 must not accept an increment hidden in a nested function');

const invalidFunctionScope = evaluatePythonSubmission(
  CURRICULUM_MODULES[4],
  'def craft_token(seed):\n    print(seed)\nreturn seed * 3 + 9\nprint(craft_token(7))',
  'EN'
);
assert(!invalidFunctionScope.isSuccess, 'Mission 5 must reject return statements outside the function');

const invalidExceptScope = evaluatePythonSubmission(
  CURRICULUM_MODULES[5],
  'try:\n    open("corrupt.log", "r")\nexcept FileNotFoundError:\n    pass\nprint("SAFE_EXTRACT")',
  'EN'
);
assert(!invalidExceptScope.isSuccess, 'Mission 6 must require SAFE_EXTRACT inside the except block');

const nestedFakeExceptOutput = evaluatePythonSubmission(
  CURRICULUM_MODULES[5],
  'try:\\n    open("corrupt.log", "r")\\nexcept FileNotFoundError:\\n    def fake():\\n        print("SAFE_EXTRACT")\\nprint("SAFE_EXTRACT")',
  'EN'
);
assert(!nestedFakeExceptOutput.isSuccess, 'Mission 6 must not accept output hidden inside a nested function');


const invalidMethodScope = evaluatePythonSubmission(
  CURRICULUM_MODULES[6],
  'class OverlordOverride:\n    def __init__(self, key):\n        self.key = key\n    def shutdown(self):\n        pass\nreturn "ROOT_UNLOCKED"\ncore = OverlordOverride("NULL-2142")\nprint(core.shutdown())',
  'EN'
);
assert(!invalidMethodScope.isSuccess, 'Mission 7 must require ROOT_UNLOCKED return inside shutdown');

const nestedFakeMethodReturn = evaluatePythonSubmission(
  CURRICULUM_MODULES[6],
  'class OverlordOverride:\\n    def __init__(self, key):\\n        self.key = key\\n    def shutdown(self):\\n        def fake():\\n            return "ROOT_UNLOCKED"\\ncore = OverlordOverride("NULL-2142")\\nprint(core.shutdown())',
  'EN'
);
assert(!nestedFakeMethodReturn.isSuccess, 'Mission 7 must not accept a return hidden inside a nested function');


console.log('Production Pyodide worker regex checks passed.');

 // 7. Verify versioned progress persistence and defensive recovery
const memoryStorage = (() => {
  let value: string | null = null;
  return {
    getItem: (_key: string) => value,
    setItem: (_key: string, next: string) => { value = next; },
    setRaw: (next: string | null) => { value = next; },
  };
})();
const defaultProgress = loadProgress(memoryStorage);
assert(defaultProgress.completedIds.length === 0 && defaultProgress.xp === 0, 'Expected clean progress defaults');
const saved = {
  completedIds: [1, 2, 2, 99, -1],
  xp: 350,
  ramMb: 256,
  traceIntegrity: 80,
  language: 'ID' as const,
  drafts: { '1': 'agent = "Zero"', '7': 'print("boss")', sandbox: 'print("free")', '8': 'invalid', '2': 'x'.repeat(32769) },
  activeModuleId: 2,
  hasEnteredHub: true,
  audioEnabled: false,
};
assert(saveProgress(saved, memoryStorage), 'Expected progress save to succeed');
const restored = loadProgress(memoryStorage);
assert(restored.completedIds.join(',') === '1,2', 'Progress loader must filter invalid and duplicate mission IDs');
assert(restored.xp === 350 && restored.ramMb === 256 && restored.language === 'ID', 'Progress values should survive save/load');
assert(restored.drafts?.['1'] === 'agent = "Zero"' && restored.drafts?.['7'] === 'print("boss")', 'Valid mission drafts should survive save/load');
assert(restored.drafts?.sandbox === 'print("free")', 'Sandbox draft should survive save/load');
assert(restored.activeModuleId === 2 && restored.hasEnteredHub === true, 'Campaign session should resume the saved mission and hub');
assert(restored.audioEnabled === false, 'Audio preference should survive save/load');
assert(!('8' in (restored.drafts ?? {})) && !('2' in (restored.drafts ?? {})), 'Invalid or oversized drafts must be filtered');
memoryStorage.setRaw('{invalid-json');
assert(loadProgress(memoryStorage).xp === 0, 'Corrupt save data must safely reset to defaults');
memoryStorage.setRaw(JSON.stringify({ version: 99, completedIds: [1], xp: 999 }));
assert(loadProgress(memoryStorage).completedIds.length === 0, 'Unknown save versions must not be loaded');
memoryStorage.setRaw(JSON.stringify({ version: 1, completedIds: [1], activeModuleId: 7, hasEnteredHub: true }));
assert(loadProgress(memoryStorage).activeModuleId === 2, 'Resume state must not unlock missions that have not been completed');


console.log('All NEO-NULL verification checks passed (7/7 modules, EN/ID/PH, syntax, worker regex & Crimson error checks).');
