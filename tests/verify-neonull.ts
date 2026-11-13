import { readFileSync } from 'node:fs';
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


// 5. Verify the production Pyodide worker uses valid Python regex patterns
const workerSource = readFileSync('public/workers/pyodide-runner.worker.js', 'utf8');
assert(workerSource.includes('r"\\belse\\s*:"'), 'Worker else-branch regex must use Python word-boundary syntax');
assert(workerSource.includes('r"\\bif\\s+threat\\s*>=\\s*50\\s*:"'), 'Worker threat-if regex must be valid');
assert(workerSource.includes('r"for\\s+\\w+\\s+in\\s+range\\s*\\(\\s*5\\s*\\)\\s*:"'), 'Worker for-loop regex must be valid');
assert(workerSource.includes('r"\\btry\\s*:"'), 'Worker try regex must be valid');
assert(workerSource.includes('r"\\bexcept\\b.*:"'), 'Worker except regex must be valid');

console.log('Production Pyodide worker regex checks passed.');

console.log('All NEO-NULL verification checks passed (7/7 modules, EN/ID/PH, syntax, worker regex & Crimson error checks).');
