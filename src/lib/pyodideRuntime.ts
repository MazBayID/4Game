import { AppLanguage, ProtocolModule, UI_STRINGS } from '../data/neoNullData';
import { EvaluationResult, evaluatePythonSubmission } from './pythonHighlightAndEval';

export interface RamVariableItem {
  name: string;
  type: string;
  preview: string;
  items: string[] | null;
}

export interface RichExecutionResult extends EvaluationResult {
  engineUsed: 'cpython-3.14' | 'offline-fallback';
  stdout: string;
  ramSnapshot: RamVariableItem[];
}

interface WorkerPayload {
  stdout: string;
  passed: boolean;
  isSyntaxError: boolean;
  errorMsg: string | null;
  errorLines: number[];
  checkDetail: string;
  ramSnapshot: RamVariableItem[];
}

class PyodideSandboxClient {
  private worker: Worker | null = null;
  private reqCounter = 0;

  private getWorker(): Worker {
    if (!this.worker) {
      this.worker = new Worker('/workers/pyodide-runner.worker.js');
    }
    return this.worker;
  }

  private resetWorker() {
    if (this.worker) {
      this.worker.terminate();
      this.worker = null;
    }
  }

  public async execute(
    module: ProtocolModule | null,
    code: string,
    language: AppLanguage
  ): Promise<RichExecutionResult> {
    const moduleId = module ? module.id : 0;
    const strings = UI_STRINGS[language];

    // Guard for non-worker test environments
    if (typeof Worker === 'undefined') {
      if (!module) {
        return {
          isSuccess: true,
          isSyntaxError: false,
          outputText: 'Sandbox unavailable outside browser.',
          errorLines: [],
          glitchFeedback: '',
          engineUsed: 'offline-fallback',
          stdout: '',
          ramSnapshot: [],
        };
      }
      const fallback = evaluatePythonSubmission(module, code, language);
      return {
        ...fallback,
        engineUsed: 'offline-fallback',
        stdout: fallback.outputText,
        ramSnapshot: [],
      };
    }

    try {
      const worker = this.getWorker();
      const id = ++this.reqCounter;

      const payload = await new Promise<WorkerPayload>((resolve, reject) => {
        const timeout = window.setTimeout(() => {
          this.resetWorker();
          reject(new Error('Execution timed out (3000ms) — infinite loop terminated.'));
        }, 8000);

        const onMsg = (ev: MessageEvent) => {
          if (ev.data?.id !== id) return;
          window.clearTimeout(timeout);
          worker.removeEventListener('message', onMsg);
          if (ev.data.ok) {
            resolve(ev.data.result as WorkerPayload);
          } else {
            reject(new Error(ev.data.error || 'Worker execution failed'));
          }
        };

        worker.addEventListener('message', onMsg);
        worker.postMessage({ id, code, moduleId });
      });

      if (payload.errorMsg) {
        const line = payload.errorLines[0] ?? 1;
        const glitchAlert =
          language === 'EN'
            ? `TRACEBACK on line ${line}: ${payload.errorMsg}. Crimson sensors locked onto your syntax!`
            : language === 'ID'
              ? `TRACEBACK di baris ${line}: ${payload.errorMsg}. Sensor merah mengunci kesalahan kodemu!`
              : `TRACEBACK sa linya ${line}: ${payload.errorMsg}. Nakita ng Crimson sensors ang mali sa code!`;

        return {
          isSuccess: false,
          isSyntaxError: payload.isSyntaxError,
          outputText: `${strings.syntaxErrorPrefix} · ${payload.errorMsg}`,
          errorLines: payload.errorLines,
          glitchFeedback: glitchAlert,
          engineUsed: 'cpython-3.14',
          stdout: payload.stdout,
          ramSnapshot: payload.ramSnapshot,
        };
      }

      if (!module) {
        return {
          isSuccess: true,
          isSyntaxError: false,
          outputText: payload.stdout || '[CPython 3.14] Executed cleanly (no stdout).',
          errorLines: [],
          glitchFeedback:
            language === 'EN'
              ? 'Free-Play Sandbox clear. Inspect live objects in your RAM slots on the left.'
              : language === 'ID'
                ? 'Sandbox Bebas bersih. Periksa objek aktif di panel RAM Inspector.'
                : 'Malinis ang Free-Play Sandbox. Tingnan ang mga live variable sa RAM Inspector.',
          engineUsed: 'cpython-3.14',
          stdout: payload.stdout,
          ramSnapshot: payload.ramSnapshot,
        };
      }

      if (payload.passed) {
        const finalOut = payload.stdout
          ? `${payload.stdout}\n[CPython 3.14 · EXIT 0] ${strings.missionPassedStatus}`
          : `[CPython 3.14 · EXIT 0] ${strings.missionPassedStatus}`;
        return {
          isSuccess: true,
          isSyntaxError: false,
          outputText: finalOut,
          errorLines: [],
          glitchFeedback: module.glitchSuccessDialogue[language],
          engineUsed: 'cpython-3.14',
          stdout: payload.stdout,
          ramSnapshot: payload.ramSnapshot,
        };
      }

      const failOut = payload.stdout
        ? `stdout: ${payload.stdout}\n${strings.testFailedPrefix}: ${payload.checkDetail}`
        : `${strings.testFailedPrefix}: ${payload.checkDetail}`;

      return {
        isSuccess: false,
        isSyntaxError: false,
        outputText: failOut,
        errorLines: [1],
        glitchFeedback: module.solutionHint1[language],
        engineUsed: 'cpython-3.14',
        stdout: payload.stdout,
        ramSnapshot: payload.ramSnapshot,
      };
    } catch (err) {
      if (!module) {
        return {
          isSuccess: false,
          isSyntaxError: false,
          outputText: err instanceof Error ? err.message : String(err),
          errorLines: [1],
          glitchFeedback: 'Runtime interrupted.',
          engineUsed: 'offline-fallback',
          stdout: '',
          ramSnapshot: [],
        };
      }
      const fallback = evaluatePythonSubmission(module, code, language);
      return {
        ...fallback,
        engineUsed: 'offline-fallback',
        stdout: fallback.outputText,
        ramSnapshot: [],
      };
    }
  }
}

export const pyodideClient = new PyodideSandboxClient();
