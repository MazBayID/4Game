import { useState } from 'react';
import {
  AlertTriangle,
  ArrowLeft,
  BookMarked,
  BookOpen,
  CheckCircle2,
  Code2,
  Cpu,
  Database,
  Lock,
  Play,
  RotateCcw,
  ShieldAlert,
  Sparkles,
  Terminal,
  Volume2,
  VolumeX,
} from 'lucide-react';
import glitchAvatarImg from './assets/images/glitch_mentor_avatar_1794515712678.jpg';
import sectorBackdropImg from './assets/images/neonull_sector_backdrop_1794515714492.jpg';
import nullAgentImg from './assets/images/null_agent_portrait_1794516681535.jpg';
import overlordCoreImg from './assets/images/overlord_boss_core_1794515713761.jpg';
import neonullCrestPng from './assets/images/neonull_crest_emblem.png';
import synthexGatePng from './assets/images/synthex_gate_badge.png';
import cyberHudBannerPng from './assets/images/cyber_hud_header_banner.png';
import { AndroidSourceDrawer } from './components/AndroidSourceDrawer';
import { CipherCodexModal } from './components/CipherCodexModal';
import { SyntaxCodeBlock, SyntaxCodeEditor } from './components/SyntaxCodeEditor';
import { TacticalBreachCanvas } from './components/TacticalBreachCanvas';
import {
  AppLanguage,
  CURRICULUM_MODULES,
  LANGUAGE_OPTIONS,
  UI_STRINGS,
} from './data/neoNullData';
import { RamVariableItem, RichExecutionResult, pyodideClient } from './lib/pyodideRuntime';
import { getRankTitle } from './lib/pythonHighlightAndEval';
import { soundEngine } from './lib/soundEngine';

type NeoScreen = 'ONBOARDING' | 'HUB' | 'THEORY' | 'TERMINAL';

const SANDBOX_STARTER = `# NEO-NULL CPython 3.14 Free-Play Sandbox
agents = ["Null-Agent", "GLITCH"]
power = {name: (idx + 1) * 42 for idx, name in enumerate(agents)}

for name, score in power.items():
    print(f"{name} -> {score} MHz")`;

export function App() {
  const [screen, setScreen] = useState<NeoScreen>('ONBOARDING');
  const [language, setLanguage] = useState<AppLanguage>('EN');
  const [activeModuleId, setActiveModuleId] = useState<number>(1);
  const [isSandboxMode, setIsSandboxMode] = useState<boolean>(false);
  const [completedIds, setCompletedIds] = useState<number[]>([]);
  const [xp, setXp] = useState<number>(0);
  const [ramMb, setRamMb] = useState<number>(128);
  const [traceIntegrity, setTraceIntegrity] = useState<number>(100);
  const [moduleAttemptFailed, setModuleAttemptFailed] = useState<boolean>(false);
  const [codeBuffer, setCodeBuffer] = useState<string>(CURRICULUM_MODULES[0].starterCode);
  const [hintLevel, setHintLevel] = useState<number>(0);
  const [glitchDialogue, setGlitchDialogue] = useState<string>(
    CURRICULUM_MODULES[0].glitchIntroDialogue.EN
  );
  const [isWarningState, setIsWarningState] = useState<boolean>(false);
  const [evaluationResult, setEvaluationResult] = useState<RichExecutionResult | null>(null);
  const [ramSnapshot, setRamSnapshot] = useState<RamVariableItem[]>([]);
  const [isRunning, setIsRunning] = useState<boolean>(false);
  const [pulseMode, setPulseMode] = useState<'idle' | 'deploy' | 'success' | 'alert'>('idle');
  const [audioEnabled, setAudioEnabled] = useState<boolean>(true);
  const [sourceDrawerOpen, setSourceDrawerOpen] = useState<boolean>(false);
  const [codexOpen, setCodexOpen] = useState<boolean>(false);

  const strings = UI_STRINGS[language];
  const activeModule =
    CURRICULUM_MODULES.find((m) => m.id === activeModuleId) ?? CURRICULUM_MODULES[0];
  const highestUnlockedId = Math.min(
    CURRICULUM_MODULES.length,
    (completedIds.length > 0 ? Math.max(...completedIds) : 0) + 1
  );
  const isCampaignComplete = completedIds.length >= CURRICULUM_MODULES.length;

  const toggleAudio = () => {
    const next = !audioEnabled;
    soundEngine.enabled = next;
    setAudioEnabled(next);
    if (next) soundEngine.playKeyTick();
  };

  const handleLanguageChange = (nextLang: AppLanguage) => {
    soundEngine.playKeyTick();
    setLanguage(nextLang);
    if (isSandboxMode) {
      setGlitchDialogue(UI_STRINGS[nextLang].sandboxSubtitle);
      return;
    }
    if (evaluationResult?.isSuccess) {
      setGlitchDialogue(activeModule.glitchSuccessDialogue[nextLang]);
    } else if (hintLevel === 1) {
      setGlitchDialogue(activeModule.solutionHint1[nextLang]);
    } else if (hintLevel >= 2) {
      setGlitchDialogue(activeModule.solutionHint2[nextLang]);
    } else {
      setGlitchDialogue(activeModule.glitchIntroDialogue[nextLang]);
    }
  };

  const openModule = (moduleId: number, target: 'THEORY' | 'TERMINAL') => {
    if (moduleId > highestUnlockedId && !completedIds.includes(moduleId)) return;
    const mod = CURRICULUM_MODULES.find((m) => m.id === moduleId);
    if (!mod) return;
    soundEngine.playKeyTick();
    setIsSandboxMode(false);
    setActiveModuleId(mod.id);
    setCodeBuffer(mod.starterCode);
    setHintLevel(0);
    setModuleAttemptFailed(false);
    setEvaluationResult(null);
    setRamSnapshot([]);
    setPulseMode('idle');
    setIsWarningState(Boolean(mod.isBossStage));
    setGlitchDialogue(mod.glitchIntroDialogue[language]);
    setScreen(target);
  };

  const openSandbox = () => {
    soundEngine.playKeyTick();
    setIsSandboxMode(true);
    setCodeBuffer(SANDBOX_STARTER);
    setEvaluationResult(null);
    setRamSnapshot([]);
    setIsWarningState(false);
    setPulseMode('idle');
    setGlitchDialogue(strings.sandboxSubtitle);
    setScreen('TERMINAL');
  };

  const handleRunExploit = async () => {
    if (isRunning) return;
    setIsRunning(true);
    setPulseMode('deploy');
    soundEngine.playDeployBeam();

    const targetMod = isSandboxMode ? null : activeModule;
    const res = await pyodideClient.execute(targetMod, codeBuffer, language);

    setEvaluationResult(res);
    setRamSnapshot(res.ramSnapshot);
    setGlitchDialogue(res.glitchFeedback);
    setIsRunning(false);

    if (res.isSuccess) {
      setPulseMode('success');
      setIsWarningState(false);
      soundEngine.playBreachSuccess(Boolean(targetMod?.isBossStage));
      setTraceIntegrity((prev) => Math.min(100, prev + 10));

      if (targetMod && !completedIds.includes(targetMod.id)) {
        const cleanBonus = !moduleAttemptFailed ? 50 : 0;
        setCompletedIds((prev) => [...prev, targetMod.id]);
        setXp((prev) => prev + targetMod.xpReward + cleanBonus);
        setRamMb((prev) => prev + targetMod.ramRewardMb);
      }
    } else {
      setPulseMode('alert');
      setIsWarningState(true);
      setModuleAttemptFailed(true);
      setTraceIntegrity((prev) => Math.max(20, prev - 20));
      soundEngine.playCrimsonAlert();
    }
  };

  const handleRequestHint = () => {
    soundEngine.playKeyTick();
    const next = Math.min(2, hintLevel + 1);
    setHintLevel(next);
    setGlitchDialogue(
      next === 1 ? activeModule.solutionHint1[language] : activeModule.solutionHint2[language]
    );
  };

  const handleResetCode = () => {
    soundEngine.playKeyTick();
    setCodeBuffer(isSandboxMode ? SANDBOX_STARTER : activeModule.starterCode);
    setHintLevel(0);
    setEvaluationResult(null);
    setRamSnapshot([]);
    setPulseMode('idle');
    setTraceIntegrity(100);
    setIsWarningState(!isSandboxMode && Boolean(activeModule.isBossStage));
    setGlitchDialogue(
      isSandboxMode ? strings.sandboxSubtitle : activeModule.glitchIntroDialogue[language]
    );
  };

  const handleNextMission = () => {
    const nextId = Math.min(CURRICULUM_MODULES.length, activeModule.id + 1);
    openModule(nextId, 'THEORY');
  };

  return (
    <div className="min-h-screen bg-[#0A0A12] text-[#E6EDF3]">
      {/* Top Bar */}
      <header className="sticky top-0 z-30 border-b border-[#1E2235] bg-[#0A0A12]/90 backdrop-blur-md">
        <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-3 sm:px-6">
          <button
            type="button"
            onClick={() => setScreen('ONBOARDING')}
            className="text-left font-mono text-base font-bold tracking-wider text-[#00FF66] whitespace-nowrap shrink-0"
          >
            NEO-NULL
          </button>

          <div className="hidden items-center gap-4 font-mono text-xs text-[#94A3B8] md:flex">
            <span className="tabular-nums text-[#00E5FF]">{ramMb} MB RAM</span>
            <span aria-hidden="true">·</span>
            <span className="tabular-nums text-[#00FF66]">{xp} XP</span>
            <span aria-hidden="true">·</span>
            <span
              className={`tabular-nums ${
                traceIntegrity <= 40 ? 'font-bold text-[#DC143C]' : 'text-[#E6EDF3]'
              }`}
            >
              {traceIntegrity}% {strings.traceStatLabel}
            </span>
            <span aria-hidden="true">·</span>
            <span className="tabular-nums">
              {completedIds.length}/{CURRICULUM_MODULES.length}
            </span>
          </div>

          <div className="flex items-center gap-2 shrink-0">
            <div className="flex items-center rounded-md border border-[#23283D] bg-[#10121E] p-0.5">
              {(['EN', 'ID', 'PH'] as AppLanguage[]).map((code) => {
                const active = language === code;
                return (
                  <button
                    key={code}
                    type="button"
                    onClick={() => handleLanguageChange(code)}
                    className={`min-h-[34px] rounded px-2.5 font-mono text-xs font-bold transition-colors whitespace-nowrap ${
                      active
                        ? 'bg-[#00FF66]/15 text-[#00FF66]'
                        : 'text-[#94A3B8] hover:text-[#E6EDF3]'
                    }`}
                  >
                    {code}
                  </button>
                );
              })}
            </div>

            <button
              type="button"
              onClick={toggleAudio}
              aria-label={audioEnabled ? 'Mute Audio' : 'Enable Audio'}
              className="inline-flex min-h-[38px] min-w-[38px] items-center justify-center rounded-md border border-[#23283D] bg-[#161928] text-[#94A3B8] transition-colors hover:border-[#00E5FF]/60 hover:text-[#E6EDF3]"
            >
              {audioEnabled ? <Volume2 className="h-4 w-4 text-[#00FF66]" /> : <VolumeX className="h-4 w-4" />}
            </button>

            <button
              type="button"
              onClick={() => setCodexOpen(true)}
              className="inline-flex min-h-[38px] items-center gap-1.5 rounded-md border border-[#23283D] bg-[#161928] px-3 py-1.5 font-mono text-xs font-semibold text-[#00FF66] transition-colors hover:border-[#00FF66]/60 whitespace-nowrap"
            >
              <BookMarked className="h-4 w-4" />
              <span className="hidden lg:inline">{strings.codexBtn}</span>
            </button>

            <button
              type="button"
              onClick={() => setSourceDrawerOpen(true)}
              className="inline-flex min-h-[38px] items-center gap-1.5 rounded-md border border-[#23283D] bg-[#161928] px-3 py-1.5 font-mono text-xs font-semibold text-[#00E5FF] transition-colors hover:border-[#00E5FF]/60 whitespace-nowrap"
            >
              <Code2 className="h-4 w-4" />
              <span className="hidden lg:inline">{strings.androidSourceBtn}</span>
            </button>
          </div>
        </div>
      </header>

      {/* SCREEN 1: ONBOARDING */}
      {screen === 'ONBOARDING' && (
        <main className="relative overflow-hidden">
          <img
            src={sectorBackdropImg}
            alt="SYNTHEX 2142 Server Grid"
            referrerPolicy="no-referrer"
            className="pointer-events-none absolute inset-0 h-full w-full object-cover opacity-22"
          />
          <div className="pointer-events-none absolute inset-0 bg-gradient-to-b from-[#0A0A12]/65 via-[#0A0A12]/90 to-[#0A0A12]" />

          <div className="relative mx-auto grid max-w-6xl grid-cols-1 gap-10 px-4 py-12 sm:px-6 lg:grid-cols-12 lg:items-center lg:py-20">
            <div className="lg:col-span-7">
              <div className="mb-5 flex items-center gap-4">
                <img
                  src={neonullCrestPng}
                  alt="NEO-NULL Emblem"
                  referrerPolicy="no-referrer"
                  className="h-16 w-16 shrink-0 object-contain"
                />
                <img
                  src={nullAgentImg}
                  alt="Null-Agent Operative"
                  referrerPolicy="no-referrer"
                  className="h-14 w-14 rounded-xl border border-[#00FF66]/70 object-cover"
                />
                <img
                  src={glitchAvatarImg}
                  alt="GLITCH AI Mentor"
                  referrerPolicy="no-referrer"
                  className="h-14 w-14 rounded-xl border border-[#00E5FF]/60 object-cover"
                />
                <div>
                  <p className="font-mono text-xs font-bold tracking-wider text-[#DC143C]">
                    {strings.appSubtitle}
                  </p>
                  <h1 className="font-mono text-3xl font-extrabold tracking-tight text-[#00FF66] sm:text-5xl">
                    {strings.appTitle}
                  </h1>
                </div>
              </div>

              <div className="cyber-chamfer mt-6 border border-[#DC143C]/65 bg-[#10121E]/95 p-6">
                <p className="font-mono text-xs font-bold text-[#00E5FF]">
                  {strings.operativeAliasLabel}
                </p>
                <p className="mt-2 text-base leading-relaxed text-[#E6EDF3]">
                  {CURRICULUM_MODULES[0].storyBriefing[language]}
                </p>
              </div>
            </div>

            <div className="lg:col-span-5">
              <div className="rounded-xl border border-[#23283D] bg-[#10121E]/95 p-6">
                <p className="mb-4 text-sm font-medium text-[#94A3B8]">
                  {strings.selectLanguagePrompt}
                </p>

                <div className="flex flex-col gap-3">
                  {LANGUAGE_OPTIONS.map((opt) => {
                    const selected = opt.code === language;
                    return (
                      <button
                        key={opt.code}
                        type="button"
                        onClick={() => handleLanguageChange(opt.code)}
                        className={`flex min-h-[56px] w-full items-center justify-between rounded-lg border px-4 py-3 text-left transition-colors ${
                          selected
                            ? 'border-[#00FF66] bg-[#161928]'
                            : 'border-[#23283D] bg-[#0A0A12] hover:border-[#00E5FF]/50'
                        }`}
                      >
                        <div>
                          <div
                            className={`text-sm font-semibold ${
                              selected ? 'text-[#00FF66]' : 'text-[#E6EDF3]'
                            }`}
                          >
                            {opt.displayName}
                          </div>
                          <div className="text-xs text-[#94A3B8]">{opt.nativeSubtitle}</div>
                        </div>
                        <span
                          className={`font-mono text-sm font-bold ${
                            selected ? 'text-[#00E5FF]' : 'text-[#64748B]'
                          }`}
                        >
                          {opt.code}
                        </span>
                      </button>
                    );
                  })}
                </div>

                <button
                  type="button"
                  onClick={() => {
                    soundEngine.playDeployBeam();
                    setScreen('HUB');
                  }}
                  className="mt-6 flex min-h-[48px] w-full items-center justify-center gap-2 rounded-lg bg-[#00FF66] px-5 py-3 font-mono text-sm font-bold text-[#05050A] transition-colors hover:bg-[#33FF85]"
                >
                  <Terminal className="h-4 w-4" />
                  <span>{strings.initiateSessionBtn}</span>
                </button>
              </div>
            </div>
          </div>
        </main>
      )}

      {/* SCREEN 2: STORY & CURRICULUM HUB */}
      {screen === 'HUB' && (
        <main className="mx-auto max-w-6xl px-4 py-8 sm:px-6">
          <div className="mb-6 flex flex-wrap items-end justify-between gap-4">
            <div>
              <p className="font-mono text-xs font-bold text-[#DC143C]">
                {strings.sectorYearLabel}
              </p>
              <h1 className="mt-1 font-mono text-2xl font-bold text-[#E6EDF3] sm:text-3xl">
                {strings.hubTitle}
              </h1>
            </div>
            <div className="flex flex-wrap items-center gap-3">
              <span className="font-mono text-xs text-[#94A3B8]">
                {strings.rankLabel}:{' '}
                <span className="font-semibold text-[#00E5FF]">
                  {getRankTitle(completedIds.length, language)}
                </span>
              </span>
              <button
                type="button"
                onClick={openSandbox}
                className="inline-flex min-h-[40px] items-center gap-2 rounded-lg border border-[#00E5FF]/50 bg-[#10121E] px-3.5 py-2 font-mono text-xs font-semibold text-[#00E5FF] transition-colors hover:bg-[#00E5FF]/10"
              >
                <Terminal className="h-4 w-4" />
                <span>{strings.sandboxModeBtn}</span>
              </button>
            </div>
          </div>

          {/* Stats Overview Bar */}
          <div className="relative mb-6 grid grid-cols-2 gap-4 overflow-hidden rounded-xl border border-[#23283D] bg-[#10121E] p-5 sm:grid-cols-4">
            <img
              src={cyberHudBannerPng}
              alt=""
              referrerPolicy="no-referrer"
              className="pointer-events-none absolute inset-0 h-full w-full object-cover opacity-20"
            />
            <div className="relative">
              <div className="font-mono text-xs text-[#64748B]">{strings.ramStatLabel}</div>
              <div className="mt-1 font-mono text-2xl font-bold tabular-nums text-[#00E5FF]">
                {ramMb} MB
              </div>
            </div>
            <div className="relative">
              <div className="font-mono text-xs text-[#64748B]">{strings.xpStatLabel}</div>
              <div className="mt-1 font-mono text-2xl font-bold tabular-nums text-[#00FF66]">
                {xp} XP
              </div>
            </div>
            <div className="relative">
              <div className="font-mono text-xs text-[#64748B]">{strings.traceStatLabel}</div>
              <div
                className={`mt-1 font-mono text-2xl font-bold tabular-nums ${
                  traceIntegrity <= 40 ? 'text-[#DC143C]' : 'text-[#E6EDF3]'
                }`}
              >
                {traceIntegrity}%
              </div>
            </div>
            <div className="relative">
              <div className="font-mono text-xs text-[#64748B]">{strings.modulesProgressLabel}</div>
              <div className="mt-1 flex items-center gap-3">
                <span className="font-mono text-2xl font-bold tabular-nums text-[#E6EDF3]">
                  {completedIds.length}/{CURRICULUM_MODULES.length}
                </span>
                <div className="h-2 flex-1 overflow-hidden rounded-full bg-[#23283D]">
                  <div
                    className="h-full bg-[#00FF66] transition-all duration-300"
                    style={{
                      width: `${(completedIds.length / CURRICULUM_MODULES.length) * 100}%`,
                    }}
                  />
                </div>
              </div>
            </div>
          </div>

          {/* Interactive Tactical Breach Network Canvas */}
          <section className="mb-6">
            <div className="mb-2 font-mono text-xs text-[#94A3B8]">
              {strings.networkMapTitle}
            </div>
            <TacticalBreachCanvas
              completedIds={completedIds}
              activeModuleId={highestUnlockedId}
              language={language}
              pulseMode="idle"
              onSelectNode={(id) => openModule(id, 'THEORY')}
            />
          </section>

          {isCampaignComplete && (
            <div className="mb-6 flex items-center gap-4 rounded-xl border border-[#00FF66] bg-[#00FF66]/10 p-5">
              <Sparkles className="h-6 w-6 shrink-0 text-[#00FF66]" />
              <div>
                <h2 className="font-mono text-base font-bold text-[#00FF66]">
                  {strings.victoryTitle}
                </h2>
                <p className="mt-0.5 text-sm text-[#E6EDF3]">{strings.victorySubtitle}</p>
              </div>
            </div>
          )}

          {/* 7 Modules Grid */}
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            {CURRICULUM_MODULES.map((mod) => {
              const completed = completedIds.includes(mod.id);
              const unlocked = mod.id <= highestUnlockedId || completed;
              const isBoss = Boolean(mod.isBossStage);

              const cardBorder =
                isBoss && unlocked
                  ? 'border-[#DC143C] bg-[#240912]/60'
                  : completed
                    ? 'border-[#00FF66]/60 bg-[#10121E]'
                    : unlocked
                      ? 'border-[#00E5FF]/50 bg-[#10121E]'
                      : 'border-[#23283D] bg-[#0D0F18] opacity-65';

              const statusLabel = completed
                ? strings.completedLabel
                : isBoss && unlocked
                  ? strings.bossAlertLabel
                  : unlocked
                    ? strings.activeLabel
                    : strings.lockedLabel;

              return (
                <article
                  key={mod.id}
                  className={`flex flex-col justify-between rounded-xl border p-5 transition-colors ${cardBorder}`}
                >
                  <div>
                    <div className="flex items-center justify-between gap-2 font-mono text-xs">
                      <span
                        className={`font-bold ${
                          isBoss && unlocked
                            ? 'text-[#DC143C]'
                            : completed
                              ? 'text-[#00FF66]'
                              : unlocked
                                ? 'text-[#00E5FF]'
                                : 'text-[#64748B]'
                        }`}
                      >
                        {mod.actCode} · M0{mod.id} · {statusLabel}
                      </span>
                      <span className="tabular-nums text-[#94A3B8]">
                        +{mod.xpReward} XP / {mod.ramRewardMb} MB
                      </span>
                    </div>

                    <div className="mt-3 flex items-start gap-3.5">
                      {isBoss ? (
                        <img
                          src={overlordCoreImg}
                          alt="AI OVERLORD"
                          referrerPolicy="no-referrer"
                          className="h-12 w-12 shrink-0 rounded-lg border border-[#DC143C] object-cover"
                        />
                      ) : !unlocked ? (
                        <img
                          src={synthexGatePng}
                          alt={strings.lockedLabel}
                          referrerPolicy="no-referrer"
                          className="h-11 w-11 shrink-0 object-contain"
                        />
                      ) : null}
                      <div>
                        <h3 className="text-lg font-bold text-[#E6EDF3]">
                          {mod.missionName[language]}
                        </h3>
                        <p className="mt-0.5 text-sm text-[#94A3B8]">
                          {mod.title[language]} · {mod.conceptTag[language]}
                        </p>
                      </div>
                    </div>
                  </div>

                  <div className="mt-5 flex items-center gap-3">
                    {unlocked ? (
                      <>
                        <button
                          type="button"
                          onClick={() => openModule(mod.id, 'THEORY')}
                          className="inline-flex min-h-[42px] flex-1 items-center justify-center gap-2 rounded-lg border border-[#23283D] bg-[#161928] px-3.5 py-2 font-mono text-xs font-semibold text-[#00E5FF] transition-colors hover:border-[#00E5FF]"
                        >
                          <BookOpen className="h-4 w-4" />
                          <span>{strings.openTheoryBtn}</span>
                        </button>
                        <button
                          type="button"
                          onClick={() => openModule(mod.id, 'TERMINAL')}
                          className={`inline-flex min-h-[42px] flex-1 items-center justify-center gap-2 rounded-lg px-3.5 py-2 font-mono text-xs font-bold transition-colors ${
                            isBoss
                              ? 'bg-[#DC143C] text-[#E6EDF3] hover:bg-[#e8224a]'
                              : 'bg-[#00FF66] text-[#05050A] hover:bg-[#33FF85]'
                          }`}
                        >
                          <Terminal className="h-4 w-4" />
                          <span>{strings.launchTerminalBtn}</span>
                        </button>
                      </>
                    ) : (
                      <div className="flex min-h-[42px] items-center gap-2 font-mono text-xs text-[#64748B]">
                        <Lock className="h-4 w-4" />
                        <span>{strings.lockedLabel}</span>
                      </div>
                    )}
                  </div>
                </article>
              );
            })}
          </div>
        </main>
      )}

      {/* SCREEN 3: STORY & THEORY VIEWER */}
      {screen === 'THEORY' && (
        <main className="mx-auto max-w-4xl px-4 py-8 sm:px-6">
          <button
            type="button"
            onClick={() => setScreen('HUB')}
            className="mb-6 inline-flex min-h-[40px] items-center gap-2 rounded-md border border-[#23283D] bg-[#10121E] px-3.5 py-1.5 font-mono text-xs text-[#E6EDF3] hover:border-[#00E5FF]"
          >
            <ArrowLeft className="h-4 w-4" />
            <span>{strings.backToHubBtn}</span>
          </button>

          <div className="mb-6">
            <p
              className={`font-mono text-xs font-bold ${
                activeModule.isBossStage ? 'text-[#DC143C]' : 'text-[#00E5FF]'
              }`}
            >
              {activeModule.actCode} · {strings.theoryBriefingTitle}
            </p>
            <h1 className="mt-1 font-mono text-2xl font-bold text-[#E6EDF3] sm:text-3xl">
              {activeModule.missionName[language]} — {activeModule.title[language]}
            </h1>
          </div>

          <div className="flex flex-col gap-5">
            {/* Operative + Lore Transmission */}
            <section
              className={`rounded-xl border p-5 ${
                activeModule.isBossStage
                  ? 'border-[#DC143C] bg-[#240912]/70'
                  : 'border-[#23283D] bg-[#10121E]'
              }`}
            >
              <div className="flex items-start gap-4">
                <img
                  src={activeModule.isBossStage ? overlordCoreImg : nullAgentImg}
                  alt="Mission Portrait"
                  referrerPolicy="no-referrer"
                  className={`h-14 w-14 shrink-0 rounded-lg border object-cover ${
                    activeModule.isBossStage ? 'border-[#DC143C]' : 'border-[#00FF66]/60'
                  }`}
                />
                <div>
                  <h2
                    className={`font-mono text-xs font-bold ${
                      activeModule.isBossStage ? 'text-[#DC143C]' : 'text-[#00E5FF]'
                    }`}
                  >
                    {strings.loreTransmissionTitle}
                  </h2>
                  <p className="mt-2 text-base leading-relaxed text-[#E6EDF3]">
                    {activeModule.storyBriefing[language]}
                  </p>
                </div>
              </div>
            </section>

            {activeModule.overlordTaunt && (
              <section className="rounded-xl border border-[#DC143C]/70 bg-[#240912]/60 p-4">
                <div className="flex items-center gap-2 font-mono text-xs font-bold text-[#DC143C]">
                  <ShieldAlert className="h-4 w-4" />
                  <span>{strings.overlordTauntTitle}</span>
                </div>
                <p className="mt-1.5 font-mono text-sm text-[#E6EDF3]">
                  &ldquo;{activeModule.overlordTaunt[language]}&rdquo;
                </p>
              </section>
            )}

            <section className="rounded-xl border border-[#00FF66]/50 bg-[#10121E] p-5">
              <h2 className="font-mono text-xs font-bold text-[#00FF66]">
                {strings.pythonConceptTitle}
              </h2>
              <p className="mt-2 text-base leading-relaxed text-[#E6EDF3]">
                {activeModule.pythonConceptExplanation[language]}
              </p>
            </section>

            <section>
              <h2 className="mb-2 font-mono text-xs font-bold text-[#00E5FF]">
                {strings.codeExampleTitle}
              </h2>
              <SyntaxCodeBlock code={activeModule.codeExample} />
            </section>

            <button
              type="button"
              onClick={() => {
                soundEngine.playDeployBeam();
                setScreen('TERMINAL');
              }}
              className={`mt-2 flex min-h-[48px] w-full items-center justify-center gap-2 rounded-lg px-5 py-3 font-mono text-sm font-bold transition-colors ${
                activeModule.isBossStage
                  ? 'bg-[#DC143C] text-[#E6EDF3] hover:bg-[#e8224a]'
                  : 'bg-[#00FF66] text-[#05050A] hover:bg-[#33FF85]'
              }`}
            >
              <Terminal className="h-4 w-4" />
              <span>{strings.proceedToTerminalBtn}</span>
            </button>
          </div>
        </main>
      )}

      {/* SCREEN 4: INTERACTIVE SPLIT-SCREEN TERMINAL DECK */}
      {screen === 'TERMINAL' && (
        <main className="cyber-terminal-grid min-h-[calc(100vh-64px)]">
          <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6">
          <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
            <button
              type="button"
              onClick={() => setScreen('HUB')}
              className="inline-flex min-h-[40px] items-center gap-2 rounded-md border border-[#23283D] bg-[#10121E] px-3.5 py-1.5 font-mono text-xs text-[#E6EDF3] hover:border-[#00E5FF]"
            >
              <ArrowLeft className="h-4 w-4" />
              <span>{strings.backToHubBtn}</span>
            </button>

            <div className="flex flex-wrap items-center gap-4 font-mono text-xs">
              <span className="inline-flex items-center gap-1.5 text-[#00E5FF]">
                <Cpu className="h-4 w-4" />
                {isSandboxMode
                  ? strings.sandboxTitle
                  : `${activeModule.actCode} · ${activeModule.missionName[language]}`}
              </span>
              {!isSandboxMode && !moduleAttemptFailed && (
                <span className="text-[#00FF66]">{strings.zeroTraceBonusLabel}</span>
              )}
            </div>
          </div>

          <div className="grid grid-cols-1 gap-6 lg:grid-cols-12">
            {/* Left Zone: Network Breach Radar + GLITCH Mentor + Objective + Live RAM Inspector */}
            <div className="flex flex-col gap-4 lg:col-span-5">
              {!isSandboxMode && (
                <TacticalBreachCanvas
                  completedIds={completedIds}
                  activeModuleId={activeModule.id}
                  language={language}
                  pulseMode={pulseMode}
                  compact
                />
              )}

              <section
                className={`rounded-xl border p-4 transition-colors ${
                  isWarningState || (!isSandboxMode && activeModule.isBossStage)
                    ? 'border-[#DC143C] bg-[#240912]/80'
                    : 'border-[#00E5FF]/60 bg-[#10121E]'
                }`}
              >
                {(isWarningState || (!isSandboxMode && activeModule.isBossStage)) && (
                  <div className="mb-2.5 flex items-center gap-2 font-mono text-[11px] font-bold text-[#DC143C]">
                    <ShieldAlert className="h-4 w-4 shrink-0" />
                    <span>{strings.warningBannerText}</span>
                  </div>
                )}

                <div className="flex items-start gap-3.5">
                  <img
                    src={
                      !isSandboxMode && activeModule.isBossStage && isWarningState
                        ? overlordCoreImg
                        : glitchAvatarImg
                    }
                    alt="GLITCH AI Mentor"
                    referrerPolicy="no-referrer"
                    className={`h-14 w-14 shrink-0 rounded-lg border object-cover ${
                      isWarningState ? 'border-[#DC143C]' : 'border-[#00E5FF]'
                    }`}
                  />
                  <div>
                    <div
                      className={`font-mono text-xs font-bold ${
                        isWarningState ? 'text-[#DC143C]' : 'text-[#00E5FF]'
                      }`}
                    >
                      {strings.glitchMentorTitle}
                    </div>
                    <p className="mt-1 text-sm leading-relaxed text-[#E6EDF3]">
                      {glitchDialogue}
                    </p>
                  </div>
                </div>
              </section>

              {!isSandboxMode && (
                <section className="rounded-xl border border-[#23283D] bg-[#10121E] p-4">
                  <h2 className="font-mono text-xs font-bold text-[#00FF66]">
                    {strings.missionObjectiveTitle}
                  </h2>
                  <p className="mt-2 text-sm leading-relaxed text-[#E6EDF3]">
                    {activeModule.objective[language]}
                  </p>
                </section>
              )}

              {/* Live RAM Variable Inspector */}
              <section className="rounded-xl border border-[#23283D] bg-[#10121E] p-4">
                <div className="flex items-center gap-2 font-mono text-xs font-bold text-[#00E5FF]">
                  <Database className="h-4 w-4" />
                  <span>{strings.ramInspectorTitle}</span>
                </div>

                {ramSnapshot.length === 0 ? (
                  <p className="mt-2 text-xs leading-relaxed text-[#64748B]">
                    {strings.ramEmptyText}
                  </p>
                ) : (
                  <div className="mt-3 flex flex-col gap-2">
                    {ramSnapshot.map((item) => (
                      <div
                        key={item.name}
                        className="rounded-lg border border-[#1E2235] bg-[#05050A] px-3 py-2 font-mono text-xs"
                      >
                        <div className="flex items-center justify-between gap-2">
                          <span className="font-bold text-[#00FF66]">{item.name}</span>
                          <span className="text-[11px] text-[#00E5FF]">{item.type}</span>
                        </div>
                        {item.items && item.items.length > 0 ? (
                          <div className="mt-1.5 flex flex-wrap gap-1.5">
                            {item.items.map((cell, idx) => (
                              <span
                                key={idx}
                                className="rounded border border-[#23283D] bg-[#10121E] px-2 py-0.5 text-[11px] text-[#FFD600]"
                              >
                                {cell}
                              </span>
                            ))}
                          </div>
                        ) : (
                          <div className="mt-1 truncate text-[#E6EDF3]">{item.preview}</div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </section>
            </div>

            {/* Right Zone: Syntax-Highlighted Code Editor & CPython 3.14 Console */}
            <div className="flex flex-col gap-4 lg:col-span-7">
              <div>
                <div className="mb-2 flex items-center justify-between">
                  <span className="font-mono text-xs font-bold text-[#00E5FF]">
                    {strings.editorHeaderTitle}
                  </span>
                  <button
                    type="button"
                    onClick={handleResetCode}
                    className="inline-flex min-h-[34px] items-center gap-1.5 rounded px-2.5 font-mono text-xs text-[#94A3B8] hover:bg-[#161928] hover:text-[#E6EDF3]"
                  >
                    <RotateCcw className="h-3.5 w-3.5" />
                    <span>{strings.resetCodeBtn}</span>
                  </button>
                </div>

                <SyntaxCodeEditor
                  code={codeBuffer}
                  onChange={setCodeBuffer}
                  errorLines={evaluationResult?.errorLines ?? []}
                  isBossStage={!isSandboxMode && Boolean(activeModule.isBossStage)}
                />
              </div>

              {!isSandboxMode && (
                <div className="flex flex-wrap gap-2">
                  {activeModule.snippetChips.map((chip) => (
                    <button
                      key={chip}
                      type="button"
                      onClick={() => {
                        soundEngine.playKeyTick();
                        setCodeBuffer((prev) => (prev.trim() ? `${prev}\n${chip}` : chip));
                      }}
                      className="min-h-[36px] rounded-md border border-[#23283D] bg-[#161928] px-3 py-1 font-mono text-xs text-[#00E5FF] transition-colors hover:border-[#00E5FF]/60"
                    >
                      + {chip.trim()}
                    </button>
                  ))}
                </div>
              )}

              <div
                className={`grid gap-3 ${
                  isSandboxMode ? 'grid-cols-1' : 'grid-cols-1 sm:grid-cols-2'
                }`}
              >
                {!isSandboxMode && (
                  <button
                    type="button"
                    onClick={handleRequestHint}
                    className="inline-flex min-h-[46px] items-center justify-center gap-2 rounded-lg border border-[#00E5FF]/50 bg-[#10121E] px-4 py-2.5 font-mono text-xs font-semibold text-[#00E5FF] transition-colors hover:bg-[#00E5FF]/10"
                  >
                    <span>{strings.requestHintBtn}</span>
                  </button>
                )}
                <button
                  type="button"
                  disabled={isRunning}
                  onClick={handleRunExploit}
                  className="inline-flex min-h-[48px] items-center justify-center gap-2 rounded-lg border border-[#DC143C] bg-[#DC143C] px-4 py-2.5 font-mono text-sm font-extrabold tracking-wide text-[#E6EDF3] shadow-sm transition-colors hover:bg-[#e8224a] disabled:opacity-60"
                >
                  <Play className="h-4 w-4" />
                  <span>{isRunning ? strings.runningBtn : strings.runExploitBtn}</span>
                </button>
              </div>

              {/* Execution Console */}
              <div
                className={`rounded-xl border bg-[#05050A] p-4 ${
                  evaluationResult == null
                    ? 'border-[#23283D]'
                    : evaluationResult.isSuccess
                      ? 'border-[#00FF66]'
                      : 'border-[#DC143C]'
                }`}
              >
                <div className="flex items-center justify-between gap-2 font-mono text-xs font-bold">
                  <div className="flex items-center gap-2">
                    {evaluationResult?.isSuccess ? (
                      <>
                        <CheckCircle2 className="h-4 w-4 text-[#00FF66]" />
                        <span className="text-[#00FF66]">
                          {strings.consoleOutputTitle} · {strings.missionPassedStatus}
                        </span>
                      </>
                    ) : evaluationResult ? (
                      <>
                        <AlertTriangle className="h-4 w-4 text-[#DC143C]" />
                        <span className="text-[#DC143C]">{strings.consoleOutputTitle}</span>
                      </>
                    ) : (
                      <span className="text-[#94A3B8]">{strings.consoleOutputTitle}</span>
                    )}
                  </div>
                  {evaluationResult && (
                    <span className="text-[11px] font-normal text-[#64748B]">
                      {evaluationResult.engineUsed}
                    </span>
                  )}
                </div>

                <pre
                  className={`mt-2 whitespace-pre-wrap font-mono text-[13px] leading-6 ${
                    evaluationResult == null
                      ? 'text-[#64748B]'
                      : evaluationResult.isSuccess
                        ? 'text-[#00FF66]'
                        : 'text-[#DC143C]'
                  }`}
                >
                  {evaluationResult?.outputText ?? strings.noOutputPlaceholder}
                </pre>
              </div>

              {!isSandboxMode && evaluationResult?.isSuccess && (
                <button
                  type="button"
                  onClick={
                    activeModule.id < CURRICULUM_MODULES.length
                      ? handleNextMission
                      : () => setScreen('HUB')
                  }
                  className="flex min-h-[48px] w-full items-center justify-center rounded-lg bg-[#00E5FF] px-5 py-3 font-mono text-sm font-bold text-[#05050A] transition-colors hover:bg-[#47EEFF]"
                >
                  {activeModule.id < CURRICULUM_MODULES.length
                    ? strings.nextModuleBtn
                    : strings.backToHubBtn}
                </button>
              )}
            </div>
          </div>
          </div>
        </main>
      )}

      <CipherCodexModal
        isOpen={codexOpen}
        language={language}
        strings={strings}
        completedIds={completedIds}
        onClose={() => setCodexOpen(false)}
      />

      <AndroidSourceDrawer
        isOpen={sourceDrawerOpen}
        strings={strings}
        onClose={() => setSourceDrawerOpen(false)}
      />

      {/* Global animated CRT scanline overlay */}
      <div className="crt-scanlines" aria-hidden="true" />
    </div>
  );
}

export default App;
