export type SavedLanguage = 'EN' | 'ID' | 'PH';

export interface SavedProgress {
  completedIds: number[];
  xp: number;
  ramMb: number;
  traceIntegrity: number;
  language: SavedLanguage;
  drafts?: Record<string, string>;
}

interface StorageLike {
  getItem(key: string): string | null;
  setItem(key: string, value: string): void;
}

const STORAGE_KEY = 'neo-null.progress.v1';
const DEFAULT_PROGRESS: SavedProgress = {
  completedIds: [],
  xp: 0,
  ramMb: 128,
  traceIntegrity: 100,
  language: 'EN',
  drafts: {},
};

function getStorage(storage?: StorageLike): StorageLike | null {
  if (storage) return storage;
  try {
    return typeof window !== 'undefined' ? window.localStorage : null;
  } catch {
    return null;
  }
}

export function loadProgress(storage?: StorageLike): SavedProgress {
  try {
    const target = getStorage(storage);
    const raw = target?.getItem(STORAGE_KEY);
    if (!raw) return { ...DEFAULT_PROGRESS };

    const parsed: unknown = JSON.parse(raw);
    if (!parsed || typeof parsed !== 'object') return { ...DEFAULT_PROGRESS };

    const data = parsed as Record<string, unknown>;
    if (data.version !== 1) return { ...DEFAULT_PROGRESS };

    const completedIds = Array.isArray(data.completedIds)
      ? [...new Set(data.completedIds.filter(
          (id): id is number => typeof id === 'number' && Number.isInteger(id) && id >= 1 && id <= 7
        ))]
      : [];
    const xp = typeof data.xp === 'number' && Number.isFinite(data.xp)
      ? Math.max(0, Math.floor(data.xp))
      : DEFAULT_PROGRESS.xp;
    const ramMb = typeof data.ramMb === 'number' && Number.isFinite(data.ramMb)
      ? Math.min(4096, Math.max(128, Math.floor(data.ramMb)))
      : DEFAULT_PROGRESS.ramMb;
    const traceIntegrity = typeof data.traceIntegrity === 'number' && Number.isFinite(data.traceIntegrity)
      ? Math.min(100, Math.max(20, Math.floor(data.traceIntegrity)))
      : DEFAULT_PROGRESS.traceIntegrity;
    const language: SavedLanguage =
      data.language === 'ID' || data.language === 'PH' ? data.language : 'EN';

    const drafts: Record<string, string> = {};
    if (data.drafts && typeof data.drafts === 'object' && !Array.isArray(data.drafts)) {
      for (const [key, value] of Object.entries(data.drafts as Record<string, unknown>)) {
        if ((/^[1-7]$/.test(key) || key === 'sandbox') && typeof value === 'string' && value.length <= 32768) {
          drafts[key] = value;
        }
      }
    }

    return { completedIds, xp, ramMb, traceIntegrity, language, drafts };
  } catch {
    return { ...DEFAULT_PROGRESS };
  }
}

export function saveProgress(progress: SavedProgress, storage?: StorageLike): boolean {
  try {
    const target = getStorage(storage);
    if (!target) return false;
    target.setItem(STORAGE_KEY, JSON.stringify({ version: 1, ...progress }));
    return true;
  } catch {
    // Storage can be disabled or full; gameplay must continue without persistence.
    return false;
  }
}
