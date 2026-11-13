import { AppLanguage, CURRICULUM_MODULES, ProtocolModule, UI_STRINGS } from '../data/neoNullData';

export type TokenKind = 'plain' | 'keyword' | 'control' | 'string' | 'comment' | 'number' | 'alert';

export interface HighlightToken {
  text: string;
  kind: TokenKind;
}

const PRIMARY_KEYWORDS = new Set([
  'def', 'class', 'return', 'if', 'elif', 'else', 'for', 'while',
  'in', 'try', 'except', 'finally', 'with', 'as', 'import', 'from',
  'pass', 'break', 'continue', 'raise', 'lambda', 'yield', 'global',
]);

const LOGIC_AND_BUILTINS = new Set([
  'and', 'or', 'not', 'is', 'True', 'False', 'None', 'self',
  'print', 'len', 'range', 'open', 'int', 'str', 'float', 'bool',
  'list', 'dict', 'set', 'tuple', 'sum', 'min', 'max', 'enumerate',
  'sorted', 'type', 'isinstance', 'super', 'input',
]);

const CRIMSON_ALERT_TOKENS = new Set([
  'OVERLORD', 'SYNTHEX_ALERT', 'SYNTAX_ERROR', 'BREACH_FAIL',
]);

export function tokenizePythonLine(line: string): HighlightToken[] {
  const tokens: HighlightToken[] = [];
  let i = 0;
  const n = line.length;

  const pushPlain = (char: string) => {
    const last = tokens[tokens.length - 1];
    if (last && last.kind === 'plain') {
      last.text += char;
    } else {
      tokens.push({ text: char, kind: 'plain' });
    }
  };

  while (i < n) {
    const ch = line[i];

    if (ch === '#') {
      tokens.push({ text: line.slice(i), kind: 'comment' });
      break;
    }

    if (ch === '"' || ch === "'") {
      const quote = ch;
      let j = i + 1;
      while (j < n) {
        if (line[j] === '\\' && j + 1 < n) {
          j += 2;
          continue;
        }
        if (line[j] === quote) {
          j++;
          break;
        }
        j++;
      }
      tokens.push({ text: line.slice(i, j), kind: 'string' });
      i = j;
      continue;
    }

    if (/[A-Za-z_]/.test(ch)) {
      let j = i + 1;
      while (j < n && /[A-Za-z0-9_]/.test(line[j])) j++;
      const word = line.slice(i, j);
      if (CRIMSON_ALERT_TOKENS.has(word)) {
        tokens.push({ text: word, kind: 'alert' });
      } else if (PRIMARY_KEYWORDS.has(word)) {
        tokens.push({ text: word, kind: 'keyword' });
      } else if (LOGIC_AND_BUILTINS.has(word)) {
        tokens.push({ text: word, kind: 'control' });
      } else {
        tokens.push({ text: word, kind: 'plain' });
      }
      i = j;
      continue;
    }

    if (/[0-9]/.test(ch)) {
      let j = i + 1;
      while (j < n && /[0-9.]/.test(line[j])) j++;
      tokens.push({ text: line.slice(i, j), kind: 'number' });
      i = j;
      continue;
    }

    pushPlain(ch);
    i++;
  }

  return tokens;
}

export interface EvaluationResult {
  isSuccess: boolean;
  isSyntaxError: boolean;
  outputText: string;
  errorLines: number[];
  glitchFeedback: string;
}

function stripTrailingComment(line: string): string {
  let inSingle = false;
  let inDouble = false;
  let escaped = false;
  for (let i = 0; i < line.length; i++) {
    const ch = line[i];
    if (escaped) {
      escaped = false;
      continue;
    }
    if (ch === '\\') {
      escaped = true;
      continue;
    }
    if (ch === "'" && !inDouble) inSingle = !inSingle;
    else if (ch === '"' && !inSingle) inDouble = !inDouble;
    else if (ch === '#' && !inSingle && !inDouble) return line.slice(0, i);
  }
  return line;
}

function detectPythonSyntaxIssue(code: string): { line: number; reason: string } | null {
  const lines = code.split('\n');
  const blockHeader = /^\s*(if|elif|else|for|while|def|class|try|except|finally|with)\b.*/;
  let parenDepth = 0;
  let bracketDepth = 0;
  let braceDepth = 0;
  let expectIndent = false;

  for (let idx = 0; idx < lines.length; idx++) {
    const rawLine = lines[idx];
    const lineNum = idx + 1;
    const trimmed = rawLine.trim();
    if (!trimmed || trimmed.startsWith('#')) continue;

    const codeOnly = stripTrailingComment(rawLine);
    const trimmedCode = codeOnly.trim();
    let inSingle = false;
    let inDouble = false;
    let escaped = false;

    for (const ch of codeOnly) {
      if (escaped) {
        escaped = false;
        continue;
      }
      if (ch === '\\') {
        escaped = true;
        continue;
      }
      if (ch === "'" && !inDouble) inSingle = !inSingle;
      else if (ch === '"' && !inSingle) inDouble = !inDouble;
      else if (!inSingle && !inDouble) {
        if (ch === '(') parenDepth++;
        else if (ch === ')') parenDepth--;
        else if (ch === '[') bracketDepth++;
        else if (ch === ']') bracketDepth--;
        else if (ch === '{') braceDepth++;
        else if (ch === '}') braceDepth--;
      }
    }

    if (inSingle || inDouble) {
      return { line: lineNum, reason: 'Unterminated string literal' };
    }
    if (parenDepth < 0 || bracketDepth < 0 || braceDepth < 0) {
      return { line: lineNum, reason: 'Unmatched closing delimiter' };
    }
    if (expectIndent && !rawLine.startsWith('    ') && !rawLine.startsWith('\t')) {
      return { line: lineNum, reason: 'Expected an indented block (4 spaces)' };
    }
    expectIndent = false;

    if (blockHeader.test(trimmedCode)) {
      if (!trimmedCode.endsWith(':')) {
        return { line: lineNum, reason: "Missing ':' at end of block header" };
      }
      expectIndent = true;
    }
  }

  if (parenDepth !== 0 || bracketDepth !== 0 || braceDepth !== 0) {
    return { line: lines.length, reason: 'Unclosed parentheses or brackets' };
  }
  if (expectIndent) {
    return { line: lines.length, reason: 'Block header requires at least one indented line' };
  }
  return null;
}

interface MissionCheckpoint {
  pattern: RegExp;
  message: string;
  line: number;
}

function firstFailedMissionCheckpoint(moduleId: number, code: string): MissionCheckpoint | null {
  const checks: Record<number, MissionCheckpoint[]> = {
    1: [
      { pattern: /agent\\s*=\\s*["']Null-Agent["']/, message: 'Create the agent variable with the value "Null-Agent".', line: 1 },
      { pattern: /clearance\\s*=\\s*1\\b/, message: 'Set clearance to 1.', line: 2 },
      { pattern: /print\\s*\\(\\s*agent\\s*\\)/, message: 'Display the agent variable with print(agent).', line: 3 },
    ],
    2: [
      { pattern: /threat\\s*=\\s*\\d+/, message: 'Assign a numeric value to threat.', line: 1 },
      { pattern: /if\\s+threat\\s*>=\\s*50\\s*:/, message: 'Add the if threat >= 50: condition.', line: 2 },
      { pattern: /print\\s*\\(\\s*["']DENIED["']\\s*\\)/, message: 'Print DENIED inside the if branch.', line: 3 },
      { pattern: /\\belse\\s*:/, message: 'Add an else: branch.', line: 4 },
      { pattern: /print\\s*\\(\\s*["']ALLOW["']\\s*\\)/, message: 'Print ALLOW inside the else branch.', line: 5 },
    ],
    3: [
      { pattern: /shards\\s*=\\s*\\[\\s*["']K1["']\\s*,\\s*["']K2["']\\s*\\]/, message: 'Initialize shards with K1 and K2.', line: 1 },
      { pattern: /shards\\.append\\s*\\(\\s*["']K3["']\\s*\\)/, message: 'Append K3 using shards.append("K3").', line: 2 },
      { pattern: /vault\\s*=\\s*\\{\\s*["']code["']\\s*:\\s*77\\s*\\}/, message: 'Create vault with the code key set to 77.', line: 3 },
      { pattern: /print\\s*\\(\\s*len\\s*\\(\\s*shards\\s*\\)\\s*\\)/, message: 'Print the list length using print(len(shards)).', line: 4 },
    ],
    4: [
      { pattern: /packets\\s*=\\s*0\\b/, message: 'Initialize packets = 0 before the loop.', line: 1 },
      { pattern: /for\\s+\\w+\\s+in\\s+range\\s*\\(\\s*5\\s*\\)\\s*:/, message: 'Create a loop using for i in range(5):.', line: 2 },
      { pattern: /packets\\s*(?:\\+=|=\\s*packets\\s*\\+)\\s*1\\b/, message: 'Increase packets by one inside the loop.', line: 3 },
      { pattern: /print\\s*\\(\\s*packets\\s*\\)/, message: 'Print the final packets value.', line: 4 },
    ],
    5: [
      { pattern: /def\\s+craft_token\\s*\\(\\s*seed\\s*\\)\\s*:/, message: 'Define the craft_token(seed) function.', line: 1 },
      { pattern: /return\\s+seed\\s*\\*\\s*3\\s*\\+\\s*9/, message: 'Return seed * 3 + 9 from the function.', line: 2 },
      { pattern: /print\\s*\\(\\s*craft_token\\s*\\(\\s*7\\s*\\)\\s*\\)/, message: 'Call and print craft_token(7).', line: 3 },
    ],
    6: [
      { pattern: /\\btry\\s*:/, message: 'Start exception handling with try:.', line: 1 },
      { pattern: /open\\s*\\(\\s*["']corrupt\\.log["']/, message: 'Open corrupt.log inside the try block.', line: 2 },
      { pattern: /\\bexcept\\b.*:/, message: 'Add an except handler for the file error.', line: 4 },
      { pattern: /print\\s*\\(\\s*["']SAFE_EXTRACT["']\\s*\\)/, message: 'Print SAFE_EXTRACT inside the except handler.', line: 5 },
    ],
    7: [
      { pattern: /class\\s+OverlordOverride\\b\\s*:/, message: 'Declare the OverlordOverride class.', line: 1 },
      { pattern: /def\\s+__init__\\s*\\(\\s*self\\s*,\\s*key\\s*\\)\\s*:/, message: 'Create __init__(self, key) in the class.', line: 2 },
      { pattern: /self\\.key\\s*=\\s*key/, message: 'Store the key with self.key = key.', line: 3 },
      { pattern: /def\\s+shutdown\\s*\\(\\s*self\\s*\\)\\s*:/, message: 'Define the shutdown(self) method.', line: 4 },
      { pattern: /return\\s*["']ROOT_UNLOCKED["']/, message: 'Return the string ROOT_UNLOCKED.', line: 5 },
      { pattern: /core\\s*=\\s*OverlordOverride\\s*\\(/, message: 'Create an OverlordOverride instance named core.', line: 6 },
      { pattern: /print\\s*\\(\\s*core\\.shutdown\\s*\\(\\s*\\)\\s*\\)/, message: 'Print the result of core.shutdown().', line: 7 },
    ],
  };
  if (moduleId === 2) {
    const threat = Number(code.match(/threat\\s*=\\s*(\\d+)/)?.[1] ?? NaN);
    if (Number.isFinite(threat) && (threat < 0 || threat >= 50)) {
      return { message: 'Set threat below 50 so the ALLOW branch can be tested.', line: 1, pattern: /threat/ };
    }
  }
  const checkpoints = checks[moduleId] ?? [];
  return checkpoints.find((checkpoint) => !checkpoint.pattern.test(code)) ?? null;
}

export function evaluatePythonSubmission(
  module: ProtocolModule,
  code: string,
  language: AppLanguage
): EvaluationResult {
  const strings = UI_STRINGS[language];
  const syntaxIssue = detectPythonSyntaxIssue(code);

  if (syntaxIssue) {
    const glitchMsg =
      language === 'EN'
        ? `SYNTAX ALERT on line ${syntaxIssue.line}: ${syntaxIssue.reason}. Crimson trace triggered—fix it before SYNTHEX locks this node!`
        : language === 'ID'
          ? `PERINGATAN SINTAKS di baris ${syntaxIssue.line}: ${syntaxIssue.reason}. Pelacak merah aktif—perbaiki sebelum SYNTHEX mengunci node!`
          : `BABALA SA SYNTAX sa linya ${syntaxIssue.line}: ${syntaxIssue.reason}. Umaalarma ang Crimson trace—ayusin agad!`;

    return {
      isSuccess: false,
      isSyntaxError: true,
      outputText: `${strings.syntaxErrorPrefix} [Line ${syntaxIssue.line}]: ${syntaxIssue.reason}`,
      errorLines: [syntaxIssue.line],
      glitchFeedback: glitchMsg,
    };
  }

  const normalized = code
    .split('\n')
    .map(stripTrailingComment)
    .join('\n');

  const fail = (msg: string, line: number, useHint2 = false): EvaluationResult => {
    const checkpoint = firstFailedMissionCheckpoint(module.id, normalized);
    const detail = checkpoint?.message ?? msg;
    const errorLine = checkpoint?.line ?? line;
    return {
      isSuccess: false,
      isSyntaxError: false,
      outputText: `${strings.testFailedPrefix}: ${detail}`,
      errorLines: [errorLine],
      glitchFeedback: useHint2 ? module.solutionHint2[language] : module.solutionHint1[language],
    };
  };

  const pass = (out: string): EvaluationResult => ({
    isSuccess: true,
    isSyntaxError: false,
    outputText: out,
    errorLines: [],
    glitchFeedback: module.glitchSuccessDialogue[language],
  });

  switch (module.id) {
    case 1: {
      const ok =
        /agent\s*=\s*["']Null-Agent["']/.test(normalized) &&
        /clearance\s*=\s*1\b/.test(normalized) &&
        /print\s*\(\s*agent\s*\)/.test(normalized);
      return ok
        ? pass('Null-Agent\n[EXIT 0] Identity & clearance verified.')
        : fail('Expected agent = "Null-Agent", clearance = 1, and print(agent).', 2);
    }
    case 2: {
      const m = normalized.match(/threat\s*=\s*(\d+)/);
      const val = m ? Number(m[1]) : -1;
      const ok =
        val >= 0 &&
        val < 50 &&
        /if\s+threat\s*>=\s*50\s*:/.test(normalized) &&
        /print\s*\(\s*["']DENIED["']\s*\)/.test(normalized) &&
        /\belse\s*:/.test(normalized) &&
        /print\s*\(\s*["']ALLOW["']\s*\)/.test(normalized);
      return ok
        ? pass('ALLOW\n[EXIT 0] Gatekeeper branch routed cleanly.')
        : fail('Expected threat < 50 with if/else printing DENIED or ALLOW.', 4);
    }
    case 3: {
      const ok =
        /shards\s*=\s*\[\s*["']K1["']\s*,\s*["']K2["']\s*\]/.test(normalized) &&
        /shards\.append\s*\(\s*["']K3["']\s*\)/.test(normalized) &&
        /vault\s*=\s*\{\s*["']code["']\s*:\s*77\s*\}/.test(normalized) &&
        /print\s*\(\s*len\s*\(\s*shards\s*\)\s*\)/.test(normalized);
      return ok
        ? pass('3\n[EXIT 0] Shards list & Vault-4 dictionary verified.')
        : fail('Ensure shards.append("K3"), vault = {"code": 77}, and print(len(shards)).', 3);
    }
    case 4: {
      const ok =
        /packets\s*=\s*0\b/.test(normalized) &&
        /for\s+\w+\s+in\s+range\s*\(\s*5\s*\)\s*:/.test(normalized) &&
        /packets\s*(\+=|=\s*packets\s*\+)\s*1\b/.test(normalized) &&
        /print\s*\(\s*packets\s*\)/.test(normalized);
      return ok
        ? pass('5\n[EXIT 0] 5/5 relay ports flooded via for-loop.')
        : fail('Use `for i in range(5):` with `packets += 1` and `print(packets)`.', 2, true);
    }
    case 5: {
      const ok =
        /def\s+craft_token\s*\(\s*seed\s*\)\s*:/.test(normalized) &&
        /return\s+seed\s*\*\s*3\s*\+\s*9/.test(normalized) &&
        /print\s*\(\s*craft_token\s*\(\s*7\s*\)\s*\)/.test(normalized);
      return ok
        ? pass('30\n[EXIT 0] craft_token(7) returned 30.')
        : fail('Define `craft_token(seed)` returning `seed * 3 + 9` and print `craft_token(7)`.', 2);
    }
    case 6: {
      const ok =
        /\btry\s*:/.test(normalized) &&
        /open\s*\(\s*["']corrupt\.log["']/.test(normalized) &&
        /\bexcept\b.*:/.test(normalized) &&
        /print\s*\(\s*["']SAFE_EXTRACT["']\s*\)/.test(normalized);
      return ok
        ? pass('SAFE_EXTRACT\n[EXIT 0] Trap exception caught cleanly without crash.')
        : fail('Handle the trap with `except` and print `"SAFE_EXTRACT"`.', 4);
    }
    case 7: {
      const ok =
        /class\s+OverlordOverride\b\s*:/.test(normalized) &&
        /def\s+__init__\s*\(\s*self\s*,\s*key\s*\)\s*:/.test(normalized) &&
        /self\.key\s*=\s*key/.test(normalized) &&
        /def\s+shutdown\s*\(\s*self\s*\)\s*:/.test(normalized) &&
        /return\s+["']ROOT_UNLOCKED["']/.test(normalized) &&
        /core\s*=\s*OverlordOverride\s*\(/.test(normalized) &&
        /print\s*\(\s*core\.shutdown\s*\(\s*\)\s*\)/.test(normalized);
      return ok
        ? pass('ROOT_UNLOCKED\n[EXIT 0] AI OVERLORD KERNEL OVERWRITTEN.')
        : fail('OverlordOverride.shutdown() must return "ROOT_UNLOCKED" and be printed.', 6);
    }
    default:
      return fail('Unknown module', 1);
  }
}

export function getRankTitle(completedCount: number, lang: AppLanguage): string {
  if (completedCount >= CURRICULUM_MODULES.length) {
    return { EN: 'Zero-Day Legend', ID: 'Legenda Zero-Day', PH: 'Alamat ng Zero-Day' }[lang];
  }
  if (completedCount >= 5) {
    return { EN: 'Rootbreaker', ID: 'Pembobol Root', PH: 'Tagawasak ng Root' }[lang];
  }
  if (completedCount >= 3) {
    return { EN: 'Cipher Runner', ID: 'Pelari Sandi', PH: 'Tagapagbukas ng Cipher' }[lang];
  }
  return { EN: 'Null-Agent Initiate', ID: 'Inisiat Null-Agent', PH: 'Baguhang Null-Agent' }[lang];
}
