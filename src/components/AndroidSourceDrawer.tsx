import { useEffect, useState } from 'react';
import { Check, Copy, FileCode2, X } from 'lucide-react';
import { UiStrings } from '../data/neoNullData';

interface AndroidSourceFile {
  path: string;
  name: string;
  content: string;
}

interface AndroidSourceDrawerProps {
  isOpen: boolean;
  strings: UiStrings;
  onClose: () => void;
}

export function AndroidSourceDrawer({ isOpen, strings, onClose }: AndroidSourceDrawerProps) {
  const [files, setFiles] = useState<AndroidSourceFile[]>([]);
  const [selectedPath, setSelectedPath] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    if (!isOpen || files.length > 0) return;
    setLoading(true);
    fetch('/api/android-sources')
      .then((r) => r.json())
      .then((data) => {
        const list: AndroidSourceFile[] = Array.isArray(data.files) ? data.files : [];
        setFiles(list);
        if (list.length > 0) {
          const mainAct = list.find((f) => f.name === 'MainActivity.kt') ?? list[0];
          setSelectedPath(mainAct.path);
        }
      })
      .catch(() => setFiles([]))
      .finally(() => setLoading(false));
  }, [isOpen, files.length]);

  if (!isOpen) return null;

  const activeFile = files.find((f) => f.path === selectedPath) ?? files[0];

  const handleCopy = async () => {
    if (!activeFile) return;
    try {
      await navigator.clipboard.writeText(activeFile.content);
      setCopied(true);
      setTimeout(() => setCopied(false), 1800);
    } catch {
      // Clipboard blocked in non-HTTPS contexts; ignore silently
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-label={strings.androidSourceTitle}
    >
      <div className="flex h-[85vh] w-full max-w-6xl flex-col overflow-hidden rounded-xl border border-[#23283D] bg-[#0A0A12] shadow-2xl">
        <div className="flex items-center justify-between border-b border-[#23283D] bg-[#10121E] px-5 py-3.5">
          <div className="flex items-center gap-3">
            <FileCode2 className="h-5 w-5 text-[#00E5FF]" />
            <h2 className="font-mono text-sm font-bold text-[#E6EDF3]">
              {strings.androidSourceTitle}
            </h2>
          </div>
          <div className="flex items-center gap-3">
            {activeFile && (
              <button
                type="button"
                onClick={handleCopy}
                className="inline-flex min-h-[40px] items-center gap-2 rounded-md border border-[#00FF66]/50 bg-[#00FF66]/10 px-3.5 py-1.5 font-mono text-xs font-semibold text-[#00FF66] transition-colors hover:bg-[#00FF66]/20 whitespace-nowrap"
              >
                {copied ? <Check className="h-4 w-4" /> : <Copy className="h-4 w-4" />}
                <span>{copied ? strings.copiedBtn : strings.copyCodeBtn}</span>
              </button>
            )}
            <button
              type="button"
              onClick={onClose}
              aria-label="Close"
              className="inline-flex min-h-[40px] min-w-[40px] items-center justify-center rounded-md border border-[#23283D] bg-[#161928] text-[#94A3B8] transition-colors hover:border-[#DC143C] hover:text-[#E6EDF3]"
            >
              <X className="h-4 w-4" />
            </button>
          </div>
        </div>

        <div className="grid min-h-0 flex-1 grid-cols-1 md:grid-cols-[280px_1fr]">
          <aside className="overflow-y-auto border-b border-[#23283D] bg-[#0D0F1A] p-3 md:border-r md:border-b-0">
            {loading ? (
              <p className="p-3 font-mono text-xs text-[#94A3B8]">Loading Kotlin source tree...</p>
            ) : (
              <div className="flex flex-col gap-1">
                {files.map((file) => {
                  const selected = file.path === activeFile?.path;
                  return (
                    <button
                      key={file.path}
                      type="button"
                      onClick={() => setSelectedPath(file.path)}
                      className={`w-full rounded-md px-3 py-2 text-left font-mono text-xs transition-colors ${
                        selected
                          ? 'border border-[#00E5FF]/60 bg-[#00E5FF]/10 text-[#00E5FF]'
                          : 'border border-transparent text-[#94A3B8] hover:bg-[#161928] hover:text-[#E6EDF3]'
                      }`}
                    >
                      <div className="truncate font-semibold">{file.name}</div>
                      <div className="truncate text-[10px] text-[#64748B]">{file.path}</div>
                    </button>
                  );
                })}
              </div>
            )}
          </aside>

          <div className="min-h-0 overflow-auto bg-[#05050A] p-5">
            {activeFile ? (
              <pre className="font-mono text-xs leading-6 text-[#E6EDF3]">
                <code>{activeFile.content}</code>
              </pre>
            ) : (
              <p className="font-mono text-xs text-[#94A3B8]">No Kotlin files found.</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
