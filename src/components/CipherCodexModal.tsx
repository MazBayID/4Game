import { X } from 'lucide-react';
import cipherCodexImg from '../assets/images/cipher_codex_artifact.png';
import synthexBadgeImg from '../assets/images/synthex_gate_badge.png';
import { AppLanguage, CURRICULUM_MODULES, UiStrings } from '../data/neoNullData';
import { SyntaxCodeBlock } from './SyntaxCodeEditor';

interface CipherCodexModalProps {
  isOpen: boolean;
  language: AppLanguage;
  strings: UiStrings;
  completedIds: number[];
  onClose: () => void;
}

export function CipherCodexModal({
  isOpen,
  language,
  strings,
  completedIds,
  onClose,
}: CipherCodexModalProps) {
  if (!isOpen) return null;

  const completedSet = new Set(completedIds);

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-label={strings.codexTitle}
    >
      <div className="flex max-h-[85vh] w-full max-w-4xl flex-col overflow-hidden rounded-xl border border-[#23283D] bg-[#0A0A12] shadow-2xl">
        <div className="flex items-center justify-between border-b border-[#23283D] bg-[#10121E] px-5 py-3.5">
          <div className="flex items-center gap-3">
            <img
              src={cipherCodexImg}
              alt={strings.codexTitle}
              referrerPolicy="no-referrer"
              className="h-8 w-8 rounded-md border border-[#00FF66]/60 object-cover"
            />
            <h2 className="font-mono text-sm font-bold text-[#E6EDF3]">{strings.codexTitle}</h2>
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close"
            className="inline-flex min-h-[40px] min-w-[40px] items-center justify-center rounded-md border border-[#23283D] bg-[#161928] text-[#94A3B8] hover:border-[#DC143C] hover:text-[#E6EDF3]"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        <div className="grid grid-cols-1 gap-4 overflow-y-auto p-5 md:grid-cols-2">
          {CURRICULUM_MODULES.map((mod) => {
            const unlocked = completedSet.has(mod.id);
            return (
              <div
                key={mod.id}
                className={`rounded-xl border p-4 ${
                  unlocked
                    ? 'border-[#00FF66]/45 bg-[#10121E]'
                    : 'border-[#23283D] bg-[#0D0F18] opacity-70'
                }`}
              >
                <div className="font-mono text-xs font-bold text-[#00E5FF]">
                  {mod.actCode} · {mod.title[language]}
                </div>
                {unlocked ? (
                  <>
                    <p className="mt-2 text-sm leading-relaxed text-[#E6EDF3]">
                      {mod.pythonConceptExplanation[language]}
                    </p>
                    <div className="mt-3">
                      <SyntaxCodeBlock code={mod.codeExample} />
                    </div>
                  </>
                ) : (
                  <div className="mt-3 flex items-center gap-2.5 font-mono text-xs text-[#64748B]">
                    <img
                      src={synthexBadgeImg}
                      alt={strings.lockedLabel}
                      referrerPolicy="no-referrer"
                      className="h-7 w-7 object-contain"
                    />
                    <span>{strings.lockedLabel}</span>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
