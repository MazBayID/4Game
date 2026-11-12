import { HighlightToken, tokenizePythonLine } from '../lib/pythonHighlightAndEval';

const TOKEN_COLOR: Record<HighlightToken['kind'], string> = {
  plain: 'text-[#E6EDF3]',
  keyword: 'text-[#00E5FF] font-semibold',
  control: 'text-[#00FF66] font-medium',
  string: 'text-[#FFD600]',
  comment: 'text-[#6B7280] italic',
  number: 'text-[#7DD3FC]',
  alert: 'text-[#DC143C] font-bold',
};

interface SyntaxCodeViewProps {
  code: string;
  errorLines?: number[];
}

export function SyntaxCodeBlock({ code, errorLines = [] }: SyntaxCodeViewProps) {
  const lines = code.split('\n');
  const errSet = new Set(errorLines);

  return (
    <div className="overflow-x-auto rounded-lg border border-[#23283D] bg-[#05050A] p-3.5 font-mono text-[13px] leading-6">
      {lines.map((line, idx) => {
        const lineNo = idx + 1;
        const isErr = errSet.has(lineNo);
        const tokens = tokenizePythonLine(line);
        return (
          <div
            key={lineNo}
            className={`flex items-start gap-3 px-1.5 ${
              isErr ? 'rounded bg-[#DC143C]/20' : ''
            }`}
          >
            <span
              className={`w-6 shrink-0 select-none text-right tabular-nums ${
                isErr ? 'font-bold text-[#DC143C]' : 'text-[#64748B]'
              }`}
            >
              {isErr ? `!${lineNo}` : lineNo}
            </span>
            <span className="whitespace-pre">
              {tokens.length === 0 ? ' ' : tokens.map((tok, tIdx) => (
                <span key={tIdx} className={TOKEN_COLOR[tok.kind]}>
                  {tok.text}
                </span>
              ))}
            </span>
          </div>
        );
      })}
    </div>
  );
}

interface SyntaxCodeEditorProps {
  code: string;
  onChange: (value: string) => void;
  errorLines?: number[];
  isBossStage?: boolean;
}

export function SyntaxCodeEditor({
  code,
  onChange,
  errorLines = [],
  isBossStage = false,
}: SyntaxCodeEditorProps) {
  const lines = code.split('\n');
  const errSet = new Set(errorLines);
  const hasErrors = errorLines.length > 0;
  const borderClass = hasErrors
    ? 'border-[#DC143C]'
    : isBossStage
      ? 'border-[#DC143C]/70'
      : 'border-[#00E5FF]/50';

  return (
    <div
      className={`relative flex overflow-hidden rounded-lg border bg-[#05050A] font-mono text-[13px] leading-6 ${borderClass}`}
    >
      {/* Line number gutter */}
      <div className="select-none border-r border-[#1E2235] bg-[#080911] px-2.5 py-3 text-right tabular-nums">
        {lines.map((_, idx) => {
          const lineNo = idx + 1;
          const isErr = errSet.has(lineNo);
          return (
            <div
              key={lineNo}
              className={isErr ? 'font-bold text-[#DC143C]' : 'text-[#64748B]'}
            >
              {isErr ? `!${lineNo}` : lineNo}
            </div>
          );
        })}
      </div>

      {/* Synchronized Highlight Overlay + Transparent Input Layer */}
      <div className="relative min-h-[210px] flex-1 overflow-x-auto">
        <div
          aria-hidden="true"
          className="pointer-events-none min-h-full whitespace-pre px-3.5 py-3"
        >
          {lines.map((line, idx) => {
            const lineNo = idx + 1;
            const isErr = errSet.has(lineNo);
            const tokens = tokenizePythonLine(line);
            return (
              <div
                key={lineNo}
                className={isErr ? 'rounded bg-[#DC143C]/20' : ''}
              >
                {tokens.length === 0 ? (
                  <span>&nbsp;</span>
                ) : (
                  tokens.map((tok, tIdx) => (
                    <span key={tIdx} className={TOKEN_COLOR[tok.kind]}>
                      {tok.text}
                    </span>
                  ))
                )}
              </div>
            );
          })}
        </div>

        <textarea
          value={code}
          onChange={(e) => onChange(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Tab') {
              e.preventDefault();
              const target = e.currentTarget;
              const start = target.selectionStart;
              const end = target.selectionEnd;
              const next = code.slice(0, start) + '    ' + code.slice(end);
              onChange(next);
              requestAnimationFrame(() => {
                target.selectionStart = target.selectionEnd = start + 4;
              });
            }
          }}
          spellCheck={false}
          autoCapitalize="off"
          autoComplete="off"
          aria-label="Python Code Editor"
          className="code-input absolute inset-0 h-full w-full resize-none bg-transparent px-3.5 py-3 font-mono text-[13px] leading-6 text-transparent caret-[#00FF66] focus:outline-none"
        />
      </div>
    </div>
  );
}
