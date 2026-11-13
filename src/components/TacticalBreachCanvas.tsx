import { useEffect, useRef } from 'react';
import { AppLanguage, CURRICULUM_MODULES } from '../data/neoNullData';

interface TacticalBreachCanvasProps {
  completedIds: number[];
  activeModuleId: number;
  language: AppLanguage;
  pulseMode: 'idle' | 'deploy' | 'success' | 'alert';
  onSelectNode?: (moduleId: number) => void;
  compact?: boolean;
}

interface NodePos {
  id: number;
  rx: number; // normalized 0..1
  ry: number;
}

const NODE_LAYOUT: NodePos[] = [
  { id: 1, rx: 0.10, ry: 0.54 },
  { id: 2, rx: 0.23, ry: 0.30 },
  { id: 3, rx: 0.37, ry: 0.68 },
  { id: 4, rx: 0.51, ry: 0.34 },
  { id: 5, rx: 0.65, ry: 0.66 },
  { id: 6, rx: 0.78, ry: 0.36 },
  { id: 7, rx: 0.90, ry: 0.52 },
];

export function TacticalBreachCanvas({
  completedIds,
  activeModuleId,
  language,
  pulseMode,
  onSelectNode,
  compact = false,
}: TacticalBreachCanvasProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const completedSet = new Set(completedIds);
  const highestUnlocked = Math.min(
    CURRICULUM_MODULES.length,
    (completedIds.length > 0 ? Math.max(...completedIds) : 0) + 1
  );

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let rafId = 0;
    const startTime = performance.now();

    const draw = (now: number) => {
      const dpr = window.devicePixelRatio || 1;
      const rect = canvas.getBoundingClientRect();
      const w = Math.max(300, Math.floor(rect.width));
      const h = Math.max(120, Math.floor(rect.height));

      if (canvas.width !== Math.floor(w * dpr) || canvas.height !== Math.floor(h * dpr)) {
        canvas.width = Math.floor(w * dpr);
        canvas.height = Math.floor(h * dpr);
      }

      ctx.save();
      ctx.scale(dpr, dpr);
      const t = (now - startTime) * 0.001;

      // Obsidian grid background
      ctx.fillStyle = '#05050A';
      ctx.fillRect(0, 0, w, h);

      ctx.strokeStyle = 'rgba(35, 40, 61, 0.38)';
      ctx.lineWidth = 1;
      const step = 28;
      for (let gx = 0; gx < w; gx += step) {
        ctx.beginPath();
        ctx.moveTo(gx, 0);
        ctx.lineTo(gx, h);
        ctx.stroke();
      }
      for (let gy = 0; gy < h; gy += step) {
        ctx.beginPath();
        ctx.moveTo(0, gy);
        ctx.lineTo(w, gy);
        ctx.stroke();
      }

      // Edges + animated optical packets
      for (let i = 0; i < NODE_LAYOUT.length - 1; i++) {
        const a = NODE_LAYOUT[i];
        const b = NODE_LAYOUT[i + 1];
        const ax = a.rx * w;
        const ay = a.ry * h;
        const bx = b.rx * w;
        const by = b.ry * h;

        const aDone = completedSet.has(a.id);
        const bUnlocked = b.id <= highestUnlocked || completedSet.has(b.id);

        ctx.beginPath();
        ctx.moveTo(ax, ay);
        ctx.lineTo(bx, by);
        if (pulseMode === 'alert' && b.id === activeModuleId) {
          ctx.strokeStyle = 'rgba(220, 20, 60, 0.85)';
          ctx.lineWidth = 2.2;
        } else if (aDone && bUnlocked) {
          ctx.strokeStyle = 'rgba(0, 255, 102, 0.55)';
          ctx.lineWidth = 2;
        } else {
          ctx.strokeStyle = 'rgba(35, 40, 61, 0.85)';
          ctx.lineWidth = 1.2;
        }
        ctx.stroke();

        // Flowing data packet along active edges
        if (aDone || b.id <= highestUnlocked) {
          const speed = pulseMode === 'deploy' ? 1.8 : 0.65;
          const prog = (t * speed + i * 0.23) % 1;
          const px = ax + (bx - ax) * prog;
          const py = ay + (by - ay) * prog;
          ctx.beginPath();
          ctx.arc(px, py, pulseMode === 'deploy' ? 3.5 : 2.5, 0, Math.PI * 2);
          ctx.fillStyle =
            pulseMode === 'alert'
              ? '#DC143C'
              : b.id === 7
                ? '#DC143C'
                : '#00E5FF';
          ctx.fill();
        }
      }

      // Render Sector Nodes
      NODE_LAYOUT.forEach((node) => {
        const mod = CURRICULUM_MODULES.find((m) => m.id === node.id);
        if (!mod) return;
        const nx = node.rx * w;
        const ny = node.ry * h;
        const isDone = completedSet.has(node.id);
        const isCurrent = node.id === activeModuleId;
        const isUnlocked = node.id <= highestUnlocked || isDone;
        const isBoss = Boolean(mod.isBossStage);

        const primaryColor = isBoss
          ? isDone
            ? '#00FF66'
            : '#DC143C'
          : isDone
            ? '#00FF66'
            : isUnlocked
              ? '#00E5FF'
              : '#475569';

        // Rotating outer security ring for active or boss node
        if (isCurrent || isBoss) {
          const ringRadius = isBoss ? 22 : 18;
          ctx.save();
          ctx.translate(nx, ny);
          ctx.rotate(t * (isBoss ? -1.4 : 1.1));
          ctx.beginPath();
          ctx.arc(0, 0, ringRadius, 0, Math.PI * 1.4);
          ctx.strokeStyle =
            pulseMode === 'alert' && isCurrent ? '#DC143C' : primaryColor;
          ctx.lineWidth = 1.8;
          ctx.stroke();
          ctx.restore();
        }

        // Shockwave ring on deploy / alert / success
        if (isCurrent && pulseMode !== 'idle') {
          const waveProg = (t * 2.2) % 1;
          ctx.beginPath();
          ctx.arc(nx, ny, 14 + waveProg * 20, 0, Math.PI * 2);
          ctx.strokeStyle =
            pulseMode === 'alert'
              ? `rgba(220, 20, 60, ${1 - waveProg})`
              : pulseMode === 'success'
                ? `rgba(0, 255, 102, ${1 - waveProg})`
                : `rgba(0, 229, 255, ${1 - waveProg})`;
          ctx.lineWidth = 2;
          ctx.stroke();
        }

        // Core circle
        ctx.beginPath();
        ctx.arc(nx, ny, isBoss ? 13 : 10, 0, Math.PI * 2);
        ctx.fillStyle = isDone
          ? 'rgba(0, 255, 102, 0.22)'
          : isBoss
            ? 'rgba(220, 20, 60, 0.22)'
            : isUnlocked
              ? 'rgba(0, 229, 255, 0.18)'
              : '#10121E';
        ctx.fill();
        ctx.strokeStyle = primaryColor;
        ctx.lineWidth = isCurrent ? 2.4 : 1.5;
        ctx.stroke();

        // Node code inside circle
        ctx.fillStyle = '#E6EDF3';
        ctx.font = '700 10px "JetBrains Mono", monospace';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(`0${node.id}`, nx, ny);

        // Node label
        if (!compact) {
          ctx.fillStyle = isUnlocked ? '#E6EDF3' : '#64748B';
          ctx.font = '600 10px "JetBrains Mono", monospace';
          const labelY = ny > h * 0.5 ? ny + 26 : ny - 24;
          const shortName = mod.missionName[language].split(' ').slice(0, 2).join(' ');
          ctx.fillText(shortName, nx, labelY);
        }
      });

      ctx.restore();
      rafId = requestAnimationFrame(draw);
    };

    rafId = requestAnimationFrame(draw);
    return () => cancelAnimationFrame(rafId);
  }, [completedIds, activeModuleId, language, pulseMode, compact, highestUnlocked]);

  const handleCanvasClick = (e: React.MouseEvent<HTMLCanvasElement>) => {
    if (!onSelectNode || !canvasRef.current) return;
    const rect = canvasRef.current.getBoundingClientRect();
    const cx = e.clientX - rect.left;
    const cy = e.clientY - rect.top;

    for (const node of NODE_LAYOUT) {
      const nx = node.rx * rect.width;
      const ny = node.ry * rect.height;
      const dist = Math.hypot(cx - nx, cy - ny);
      if (dist <= 26 && (node.id <= highestUnlocked || completedSet.has(node.id))) {
        onSelectNode(node.id);
        break;
      }
    }
  };

  return (
    <canvas
      ref={canvasRef}
      onClick={handleCanvasClick}
      aria-label="SYNTHEX Neural Grid Breach Map"
      className={`block w-full rounded-lg border border-[#23283D] bg-[#05050A] ${
        onSelectNode ? 'cursor-pointer' : ''
      } ${compact ? 'h-[136px]' : 'h-[188px]'}`}
    />
  );
}
