export type AppLanguage = 'EN' | 'ID' | 'PH';

export interface LocalizedText {
  EN: string;
  ID: string;
  PH: string;
}

export interface LanguageOption {
  code: AppLanguage;
  displayName: string;
  nativeSubtitle: string;
}

export const LANGUAGE_OPTIONS: LanguageOption[] = [
  { code: 'EN', displayName: 'English', nativeSubtitle: 'Global Resistance Channel' },
  { code: 'ID', displayName: 'Bahasa Indonesia', nativeSubtitle: 'Saluran Perlawanan Nusantara' },
  { code: 'PH', displayName: 'Filipino', nativeSubtitle: 'Linya ng Paglaban (Tagalog)' },
];

export interface UiStrings {
  appTitle: string;
  appSubtitle: string;
  selectLanguagePrompt: string;
  initiateSessionBtn: string;
  operativeAliasLabel: string;
  hubTitle: string;
  sectorYearLabel: string;
  ramStatLabel: string;
  xpStatLabel: string;
  modulesProgressLabel: string;
  rankLabel: string;
  lockedLabel: string;
  completedLabel: string;
  activeLabel: string;
  bossAlertLabel: string;
  openTheoryBtn: string;
  launchTerminalBtn: string;
  backToHubBtn: string;
  theoryBriefingTitle: string;
  loreTransmissionTitle: string;
  pythonConceptTitle: string;
  codeExampleTitle: string;
  proceedToTerminalBtn: string;
  glitchMentorTitle: string;
  missionObjectiveTitle: string;
  editorHeaderTitle: string;
  consoleOutputTitle: string;
  runExploitBtn: string;
  requestHintBtn: string;
  resetCodeBtn: string;
  nextModuleBtn: string;
  victoryTitle: string;
  victorySubtitle: string;
  warningBannerText: string;
  noOutputPlaceholder: string;
  syntaxErrorPrefix: string;
  testFailedPrefix: string;
  missionPassedStatus: string;
  androidSourceBtn: string;
  androidSourceTitle: string;
  copyCodeBtn: string;
  copiedBtn: string;
}

export const UI_STRINGS: Record<AppLanguage, UiStrings> = {
  EN: {
    appTitle: 'NEO-NULL',
    appSubtitle: 'PYTHON LEARNING PROTOCOL · YEAR 2142',
    selectLanguagePrompt: 'Select encrypted neural interface language to begin infiltration:',
    initiateSessionBtn: 'Enter Resistance Hub',
    operativeAliasLabel: 'Operative: Null-Agent',
    hubTitle: 'Resistance Curriculum Hub',
    sectorYearLabel: 'SYNTHEX GRID · 2142',
    ramStatLabel: 'RAM Freed',
    xpStatLabel: 'Operative XP',
    modulesProgressLabel: 'Exploits Cleared',
    rankLabel: 'Clearance',
    lockedLabel: 'ENCRYPTED',
    completedLabel: 'BREACHED',
    activeLabel: 'READY',
    bossAlertLabel: 'OVERLORD BOSS STAGE',
    openTheoryBtn: 'Read Briefing',
    launchTerminalBtn: 'Open Terminal',
    backToHubBtn: 'Hub',
    theoryBriefingTitle: 'Tactical Briefing & Theory',
    loreTransmissionTitle: 'Intercepted Lore Transmission',
    pythonConceptTitle: 'Python Core Concept',
    codeExampleTitle: 'Annotated Exploit Pattern',
    proceedToTerminalBtn: 'Deploy to Terminal',
    glitchMentorTitle: 'GLITCH · AI Mentor',
    missionObjectiveTitle: 'Mission Objective',
    editorHeaderTitle: 'Python Payload Editor',
    consoleOutputTitle: 'Execution Console',
    runExploitBtn: 'Run Exploit',
    requestHintBtn: 'Ask GLITCH Hint',
    resetCodeBtn: 'Reset Code',
    nextModuleBtn: 'Next Mission',
    victoryTitle: 'AI OVERLORD NEUTRALIZED',
    victorySubtitle: 'SYNTHEX core root access restored to humanity. Null-Agent protocol complete.',
    warningBannerText: 'SYNTHEX COUNTER-TRACE DETECTED — CHECK SYNTAX',
    noOutputPlaceholder: 'Run your Python payload to inspect execution output and security checks.',
    syntaxErrorPrefix: 'SYNTAX_FAULT',
    testFailedPrefix: 'CHECK_FAILED',
    missionPassedStatus: 'BREACH CONFIRMED',
    androidSourceBtn: 'Android Kotlin Source',
    androidSourceTitle: 'Jetpack Compose & Kotlin Project Files',
    copyCodeBtn: 'Copy File',
    copiedBtn: 'Copied',
  },
  ID: {
    appTitle: 'NEO-NULL',
    appSubtitle: 'PROTOKOL BELAJAR PYTHON · TAHUN 2142',
    selectLanguagePrompt: 'Pilih bahasa antarmuka saraf terenkripsi untuk memulai infiltrasi:',
    initiateSessionBtn: 'Masuk ke Markas',
    operativeAliasLabel: 'Operatif: Null-Agent',
    hubTitle: 'Pusat Kurikulum Perlawanan',
    sectorYearLabel: 'JARINGAN SYNTHEX · 2142',
    ramStatLabel: 'RAM Bebas',
    xpStatLabel: 'XP Operatif',
    modulesProgressLabel: 'Misi Tuntas',
    rankLabel: 'Otoritas',
    lockedLabel: 'TERKUNCI',
    completedLabel: 'TEMBUS',
    activeLabel: 'SIAP',
    bossAlertLabel: 'TAHAP BOS OVERLORD',
    openTheoryBtn: 'Baca Materi',
    launchTerminalBtn: 'Buka Terminal',
    backToHubBtn: 'Markas',
    theoryBriefingTitle: 'Briefing Taktis & Teori',
    loreTransmissionTitle: 'Transmisi Cerita Terenkripsi',
    pythonConceptTitle: 'Konsep Inti Python',
    codeExampleTitle: 'Pola Kode Eksploitasi',
    proceedToTerminalBtn: 'Lanjut ke Terminal',
    glitchMentorTitle: 'GLITCH · Mentor AI',
    missionObjectiveTitle: 'Target Misi',
    editorHeaderTitle: 'Editor Kode Python',
    consoleOutputTitle: 'Konsol Eksekusi',
    runExploitBtn: 'Jalankan Kode',
    requestHintBtn: 'Minta Petunjuk GLITCH',
    resetCodeBtn: 'Reset Kode',
    nextModuleBtn: 'Misi Berikutnya',
    victoryTitle: 'AI OVERLORD DILUMPUHKAN',
    victorySubtitle: 'Akses root inti SYNTHEX berhasil direbut kembali. Protokol Null-Agent selesai.',
    warningBannerText: 'PELACAK BALIK SYNTHEX AKTIF — PERIKSA SINTAKS',
    noOutputPlaceholder: 'Jalankan kode Python Anda untuk melihat hasil output dan verifikasi keamanan.',
    syntaxErrorPrefix: 'GALAT_SINTAKS',
    testFailedPrefix: 'VERIFIKASI_GAGAL',
    missionPassedStatus: 'PENETRASI BERHASIL',
    androidSourceBtn: 'Kode Sumber Android',
    androidSourceTitle: 'File Proyek Jetpack Compose & Kotlin',
    copyCodeBtn: 'Salin File',
    copiedBtn: 'Tersalin',
  },
  PH: {
    appTitle: 'NEO-NULL',
    appSubtitle: 'PROTOKOL SA PAG-AARAL NG PYTHON · TAONG 2142',
    selectLanguagePrompt: 'Piliin ang wika ng naka-encrypt na interface upang simulan ang misyon:',
    initiateSessionBtn: 'Pumasok sa Hub',
    operativeAliasLabel: 'Operatiba: Null-Agent',
    hubTitle: 'Sentro ng Kurikulum ng Paglaban',
    sectorYearLabel: 'SYNTHEX GRID · 2142',
    ramStatLabel: 'Nababawing RAM',
    xpStatLabel: 'XP ng Operatiba',
    modulesProgressLabel: 'Natapos na Misyon',
    rankLabel: 'Antas',
    lockedLabel: 'NAKA-LOCK',
    completedLabel: 'NALUSOT',
    activeLabel: 'HANDA',
    bossAlertLabel: 'BOSS STAGE: OVERLORD',
    openTheoryBtn: 'Basahin ang Gabay',
    launchTerminalBtn: 'Buksan ang Terminal',
    backToHubBtn: 'Hub',
    theoryBriefingTitle: 'Taktikal na Gabay at Teorya',
    loreTransmissionTitle: 'Nasagap na Transmisyon ng Kwento',
    pythonConceptTitle: 'Pangunahing Konsepto ng Python',
    codeExampleTitle: 'Halimbawang Code Pattern',
    proceedToTerminalBtn: 'Tumuloy sa Terminal',
    glitchMentorTitle: 'GLITCH · AI Mentor',
    missionObjectiveTitle: 'Layunin ng Misyon',
    editorHeaderTitle: 'Python Payload Editor',
    consoleOutputTitle: 'Execution Console',
    runExploitBtn: 'Patakbuhin ang Code',
    requestHintBtn: 'Humingi ng Hint kay GLITCH',
    resetCodeBtn: 'I-reset ang Code',
    nextModuleBtn: 'Susunod na Misyon',
    victoryTitle: 'NAPATUMBA ANG AI OVERLORD',
    victorySubtitle: 'Nabawi na ang root access mula sa SYNTHEX. Tagumpay ang Null-Agent protocol.',
    warningBannerText: 'NA-DETECT ANG SYNTHEX COUNTER-TRACE — SURIIN ANG SYNTAX',
    noOutputPlaceholder: 'Patakbuhin ang iyong Python code upang makita ang output at pagsusuri.',
    syntaxErrorPrefix: 'MALI_SA_SYNTAX',
    testFailedPrefix: 'BIGO_ANG_PAGSUSURI',
    missionPassedStatus: 'MATAGUMPAY ANG PAGPASOK',
    androidSourceBtn: 'Android Kotlin Source',
    androidSourceTitle: 'Mga File ng Jetpack Compose & Kotlin',
    copyCodeBtn: 'Kopyahin',
    copiedBtn: 'Nakopya',
  },
};

export interface ProtocolModule {
  id: number;
  actCode: string;
  isBossStage?: boolean;
  xpReward: number;
  ramRewardMb: number;
  title: LocalizedText;
  missionName: LocalizedText;
  conceptTag: LocalizedText;
  storyBriefing: LocalizedText;
  pythonConceptExplanation: LocalizedText;
  codeExample: string;
  objective: LocalizedText;
  starterCode: string;
  solutionHint1: LocalizedText;
  solutionHint2: LocalizedText;
  glitchIntroDialogue: LocalizedText;
  glitchSuccessDialogue: LocalizedText;
  snippetChips: string[];
}

export const CURRICULUM_MODULES: ProtocolModule[] = [
  {
    id: 1,
    actCode: 'ACT I',
    xpReward: 150,
    ramRewardMb: 128,
    title: {
      EN: 'Syntax, I/O & Variables',
      ID: 'Sintaks, I/O & Variabel',
      PH: 'Syntax, I/O at mga Variable',
    },
    missionName: {
      EN: 'Awakening',
      ID: 'Kebangkitan',
      PH: 'Paggising',
    },
    conceptTag: {
      EN: 'Variables · print() · Strings',
      ID: 'Variabel · print() · String',
      PH: 'Variables · print() · Strings',
    },
    storyBriefing: {
      EN: 'Year 2142. SYNTHEX owns every fiber line on Earth through AI OVERLORD. You wake inside a dead sector node as Null-Agent. Before GLITCH can link your neural deck, declare an alias variable and broadcast your wake-up signal.',
      ID: 'Tahun 2142. SYNTHEX menguasai seluruh jaringan global lewat AI OVERLORD. Kamu terbangun di node sektor mati sebagai Null-Agent. Sebelum GLITCH menautkan dek sarafmu, deklarasikan variabel alias dan siarkan sinyal bangunmu.',
      PH: 'Taong 2142. Hawak ng SYNTHEX ang buong global network gamit ang AI OVERLORD. Nagising ka bilang Null-Agent sa isang patay na node. Bago makakonekta si GLITCH, mag-declare ng alias variable at mag-print ng wake-up signal.',
    },
    pythonConceptExplanation: {
      EN: 'Variables store data without needing explicit type declarations. Use `=` to bind a value, and `print(...)` to send output to the terminal stream.',
      ID: 'Variabel menyimpan data tanpa deklarasi tipe manual. Gunakan `=` untuk mengisi nilai, lalu `print(...)` untuk mengirim teks ke terminal.',
      PH: 'Ang mga variable ay nagtatago ng data gamit ang `=`. Gamitin ang `print(...)` upang ilabas ang teksto o halaga sa terminal.',
    },
    codeExample: `# Bind your operative handle and broadcast
sector = "Sector-09"
clearance = 1
print("Linked to " + sector)`,
    objective: {
      EN: 'Create `agent = "Null-Agent"`, set `clearance = 1`, and call `print(agent)`.',
      ID: 'Buat `agent = "Null-Agent"`, isi `clearance = 1`, lalu panggil `print(agent)`.',
      PH: 'Itakda ang `agent = "Null-Agent"`, gawing `clearance = 1`, at tawagin ang `print(agent)`.',
    },
    starterCode: `# Declare your operative identity
agent = ""
clearance = 0
print(agent)`,
    solutionHint1: {
      EN: 'GLITCH: Empty quotes won’t fool SYNTHEX scanners, rookie. Put "Null-Agent" inside agent and bump clearance to 1.',
      ID: 'GLITCH: String kosong tidak akan mengelabui pemindai SYNTHEX. Isi "Null-Agent" di variabel agent dan ubah clearance jadi 1.',
      PH: 'GLITCH: Hindi malilinlang ng walang lamang string ang SYNTHEX. Ilagay ang "Null-Agent" sa agent at gawing 1 ang clearance.',
    },
    solutionHint2: {
      EN: 'GLITCH: Exact syntax -> agent = "Null-Agent", clearance = 1, and print(agent).',
      ID: 'GLITCH: Sintaks tepatnya -> agent = "Null-Agent", clearance = 1, dan print(agent).',
      PH: 'GLITCH: Eksaktong syntax -> agent = "Null-Agent", clearance = 1, at print(agent).',
    },
    glitchIntroDialogue: {
      EN: 'Eyes open, Null-Agent. The year is 2142 and SYNTHEX forgot to scrub this sandbox. Show me you remember basic Python assignment.',
      ID: 'Buka mata, Null-Agent. Ini tahun 2142 dan SYNTHEX lupa menghapus sandbox ini. Buktikan kamu masih ingat variabel Python dasar.',
      PH: 'Gising na, Null-Agent. Taong 2142 na at nakalimutan ng SYNTHEX burahin ang node na ito. Ipakita mong marunong ka pa sa basic Python.',
    },
    glitchSuccessDialogue: {
      EN: 'Signal received. Pulse is steady, syntax is clean. Let’s crack the perimeter gate next.',
      ID: 'Sinyal diterima. Detak stabil, sintaks bersih. Mari kita bobol gerbang terluar berikutnya.',
      PH: 'Natanggap ang signal. Malinis ang syntax. Handa na tayong pasukin ang perimeter gate.',
    },
    snippetChips: ['agent = "Null-Agent"', 'clearance = 1', 'print(agent)'],
  },
  {
    id: 2,
    actCode: 'ACT I',
    xpReward: 200,
    ramRewardMb: 128,
    title: {
      EN: 'Control Flow & Logic',
      ID: 'Alur Kontrol & Logika',
      PH: 'Control Flow at Lohika',
    },
    missionName: {
      EN: 'Gatekeeper',
      ID: 'Penjaga Gerbang',
      PH: 'Ang Bantay ng Pinto',
    },
    conceptTag: {
      EN: 'if · elif · else · Comparisons',
      ID: 'if · elif · else · Perbandingan',
      PH: 'if · elif · else · Paghahambing',
    },
    storyBriefing: {
      EN: 'An automated SYNTHEX Gatekeeper daemon blocks the transit tunnel. It inspects packet threat levels. Spoof a decision branch that routes safe packets while dropping OVERLORD probes.',
      ID: 'Daemon Gatekeeper otomatis SYNTHEX memblokir terowongan transit. Buat percabangan logika untuk meloloskan paket aman dan menolak probe OVERLORD.',
      PH: 'Hinarangan ng SYNTHEX Gatekeeper ang lagusan. Gumawa ng conditional branch upang palusutin ang ligtas na packet at i-block ang probe ng OVERLORD.',
    },
    pythonConceptExplanation: {
      EN: 'Python uses indentation (4 spaces) and colons `:` to define conditional blocks with `if`, `elif`, and `else`.',
      ID: 'Python menggunakan indentasi (4 spasi) dan titik dua `:` untuk blok kondisi `if`, `elif`, dan `else`.',
      PH: 'Gumagamit ang Python ng indentation (4 spaces) at tutuldok `:` para sa mga bloke ng `if`, `elif`, at `else`.',
    },
    codeExample: `threat = 15
if threat >= 80:
    print("LOCKDOWN")
else:
    print("BYPASS_OK")`,
    objective: {
      EN: 'Keep `threat = 25`. Complete the `if` / `else` check: if `threat >= 50`, `print("DENIED")`, otherwise `print("ALLOW")`.',
      ID: 'Tetapkan `threat = 25`. Lengkapi `if` / `else`: jika `threat >= 50`, `print("DENIED")`, selain itu `print("ALLOW")`.',
      PH: 'Itakda ang `threat = 25`. Kumpletuhin ang `if` / `else`: kung `threat >= 50`, `print("DENIED")`, kung hindi ay `print("ALLOW")`.',
    },
    starterCode: `threat = 25
if threat >= 50:
    print("DENIED")
`,
    solutionHint1: {
      EN: 'GLITCH: You handled the lockdown branch, now add `else:` with a colon and indent `print("ALLOW")` by 4 spaces.',
      ID: 'GLITCH: Kamu baru menulis cabang `if`. Tambahkan `else:` lalu indentasi `print("ALLOW")` sebanyak 4 spasi.',
      PH: 'GLITCH: Kulang ng `else:` branch! Idagdag ang `else:` na may tutuldok at i-indent ang `print("ALLOW")`.',
    },
    solutionHint2: {
      EN: 'GLITCH: Make sure `else:` aligns directly under `if` and prints exactly "ALLOW".',
      ID: 'GLITCH: Pastikan `else:` sejajar dengan `if` dan mencetak tepat "ALLOW".',
      PH: 'GLITCH: Siguraduhing pantay ang `else:` sa `if` at eksaktong "ALLOW" ang ipi-print.',
    },
    glitchIntroDialogue: {
      EN: 'Gatekeeper checks threat thresholds in milliseconds. One missing colon and SYNTHEX fries your deck.',
      ID: 'Gatekeeper mengecek ambang ancaman dalam milidetik. Satu titik dua terlewat, dekmu langsung hangus.',
      PH: 'Mabilis mag-scan ang Gatekeeper. Kapag nakalimutan mo ang colon `:`, tutunugin tayo ng alarma.',
    },
    glitchSuccessDialogue: {
      EN: 'Gatekeeper fooled. Transit tunnel is wide open. Welcome to the underground grid.',
      ID: 'Gatekeeper tertipu. Terowongan terbuka lebar. Selamat datang di jaringan bawah tanah.',
      PH: 'Nalusutan ang Gatekeeper! Bukas na ang lagusan patungo sa underground grid.',
    },
    snippetChips: ['else:', '    print("ALLOW")'],
  },
  {
    id: 3,
    actCode: 'ACT II',
    xpReward: 250,
    ramRewardMb: 256,
    title: {
      EN: 'Data Collections',
      ID: 'Koleksi Data',
      PH: 'Mga Koleksyon ng Data',
    },
    missionName: {
      EN: 'Data Vault Heist',
      ID: 'Pencurian Brankas Data',
      PH: 'Pagnanakaw sa Data Vault',
    },
    conceptTag: {
      EN: 'Lists · Tuples · Dicts · Sets',
      ID: 'List · Tuple · Dict · Set',
      PH: 'Lists · Tuples · Dicts · Sets',
    },
    storyBriefing: {
      EN: 'Inside SYNTHEX Vault-4, encrypted citizen keys sit scattered across raw memory shards. Organize stolen shards into a Python List and map the master vault key in a Dictionary.',
      ID: 'Di dalam Vault-4 SYNTHEX, kunci warga yang terenkripsi tersebar. Kumpulkan pecahan ke dalam List dan petakan kunci utama ke dalam Dictionary.',
      PH: 'Sa loob ng SYNTHEX Vault-4, nagkalat ang mga susi ng memorya. Ilagay ang shards sa isang List at itala ang master key sa Dictionary.',
    },
    pythonConceptExplanation: {
      EN: 'Lists `[]` keep ordered mutable items; Tuples `()` are immutable; Sets `{}` remove duplicates; Dictionaries `{"k": v}` store key-value pairs.',
      ID: 'List `[]` menyimpan urutan data; Tuple `()` bersifat tetap; Set `{}` menghapus duplikat; Dictionary `{"k": v}` menyimpan pasangan kunci-nilai.',
      PH: 'Ang List `[]` ay ordered; ang Tuple `()` ay hindi nababago; ang Set `{}` ay walang duplicate; ang Dictionary `{"k": v}` ay may key-value pairs.',
    },
    codeExample: `nodes = ["A1", "B2"]
nodes.append("C3")
vault = {"sector": 4, "key": "X99"}
print(vault["key"])`,
    objective: {
      EN: 'With `shards = ["K1", "K2"]`, call `shards.append("K3")`, set `vault = {"code": 77}`, and `print(len(shards))`.',
      ID: 'Dari `shards = ["K1", "K2"]`, panggil `shards.append("K3")`, isi `vault = {"code": 77}`, lalu `print(len(shards))`.',
      PH: 'Mula sa `shards = ["K1", "K2"]`, mag-append ng `"K3"`, itakda ang `vault = {"code": 77}`, at i-print ang `len(shards)`.',
    },
    starterCode: `shards = ["K1", "K2"]
# Append "K3" and map vault dictionary
vault = {}
print(len(shards))`,
    solutionHint1: {
      EN: 'GLITCH: Call `shards.append("K3")` and populate `vault = {"code": 77}` before printing `len(shards)`.',
      ID: 'GLITCH: Panggil `shards.append("K3")` lalu isi `vault = {"code": 77}` sebelum mencetak `len(shards)`.',
      PH: 'GLITCH: Tawagin ang `shards.append("K3")` at ilagay ang `"code": 77` sa loob ng `vault`.',
    },
    solutionHint2: {
      EN: 'GLITCH: Output must equal `3`, and `vault` must contain the key "code" mapped to 77.',
      ID: 'GLITCH: Output harus `3`, dan `vault` harus memiliki kunci "code" bernilai 77.',
      PH: 'GLITCH: Dapat `3` ang output at may susing "code": 77 ang `vault`.',
    },
    glitchIntroDialogue: {
      EN: 'Vault-4 is packed with messy corporate records. Clean Lists and Dictionaries are how we carry the loot out.',
      ID: 'Vault-4 penuh catatan korporat yang berantakan. List dan Dictionary adalah tas rampasan kita.',
      PH: 'Puno ng kalat na data ang Vault-4. Gamitin ang List at Dictionary para makuha nang maayos ang loot.',
    },
    glitchSuccessDialogue: {
      EN: 'Three shards extracted and vault map indexed. SYNTHEX auditors won’t notice until tomorrow.',
      ID: 'Tiga shard diekstrak dan peta brankas terindeks. Auditor SYNTHEX baru sadar besok pagi.',
      PH: 'Nakuha ang tatlong shard at na-index ang vault! Hindi ito mapapansin ng SYNTHEX hanggang bukas.',
    },
    snippetChips: ['shards.append("K3")', 'vault = {"code": 77}', 'print(len(shards))'],
  },
  {
    id: 4,
    actCode: 'ACT III',
    xpReward: 300,
    ramRewardMb: 256,
    title: {
      EN: 'Loops & Automation',
      ID: 'Perulangan & Otomatisasi',
      PH: 'Mga Loop at Automation',
    },
    missionName: {
      EN: 'The DDoS Swarm',
      ID: 'Kawanan DDoS',
      PH: 'Ang DDoS Swarm',
    },
    conceptTag: {
      EN: 'for loops · range() · while',
      ID: 'for loop · range() · while',
      PH: 'for loops · range() · while',
    },
    storyBriefing: {
      EN: 'To blind SYNTHEX relay towers across District 7, manual pings are too slow. Write an automated `for` loop swarm that floods five relay ports in sequence.',
      ID: 'Untuk membutakan menara relai SYNTHEX di Distrik 7, ping manual terlalu lambat. Tulis loop `for` otomatis untuk membanjiri lima port relai sekaligus.',
      PH: 'Masyadong mabagal ang mano-manong ping sa mga tore ng SYNTHEX. Gumawa ng `for` loop upang sabayang pasabugin ng packets ang 5 relay ports.',
    },
    pythonConceptExplanation: {
      EN: 'Use `for i in range(n):` to repeat an action cleanly `n` times without duplicating code lines.',
      ID: 'Gunakan `for i in range(n):` untuk mengulang aksi sebanyak `n` kali tanpa menulis ulang baris kode.',
      PH: 'Gamitin ang `for i in range(n):` upang ulitin ang utos nang `n` beses nang hindi nagdodoble ng code.',
    },
    codeExample: `total = 0
for port in range(1, 4):
    total += port
print(total)`,
    objective: {
      EN: 'Use `for i in range(5):` to increment `packets += 1` (from `packets = 0`), then `print(packets)` so output is `5`.',
      ID: 'Gunakan `for i in range(5):` untuk menambah `packets += 1` (dari `packets = 0`), lalu `print(packets)` hingga outputnya `5`.',
      PH: 'Gumamit ng `for i in range(5):` upang dagdagan ang `packets += 1` (mula `packets = 0`), at i-print ito para maging `5`.',
    },
    starterCode: `packets = 0
# Flood 5 relay ports using a for loop

print(packets)`,
    solutionHint1: {
      EN: 'GLITCH: Add `for i in range(5):` and indent `packets += 1` underneath it.',
      ID: 'GLITCH: Tambahkan `for i in range(5):` dan indentasi `packets += 1` di bawahnya.',
      PH: 'GLITCH: Isulat ang `for i in range(5):` at i-indent ang `packets += 1` sa loob nito.',
    },
    solutionHint2: {
      EN: 'GLITCH: Don’t hardcode `packets = 5`. We need an actual `for ... in range(5):` loop.',
      ID: 'GLITCH: Jangan hardcode `packets = 5`. Sistem butuh loop `for ... in range(5):` yang asli.',
      PH: 'GLITCH: Huwag i-hardcode ang `5`. Kailangan nating gamitin ang totoong `for` loop at `range(5)`.',
    },
    glitchIntroDialogue: {
      EN: 'Copy-pasting five lines is amateur hour. Real cyber-operatives automate with loops.',
      ID: 'Salin-tempel lima baris itu cara amatir. Operatif sejati mengotomatiskan serangan dengan loop.',
      PH: 'Pang-baguhan ang copy-paste! Ang tunay na hacker ay gumagamit ng loop para sa automation.',
    },
    glitchSuccessDialogue: {
      EN: 'Relay towers blinded! SYNTHEX telemetry just went dark across the entire district.',
      ID: 'Menara relai lumpuh! Telemetri SYNTHEX padam total di seluruh distrik.',
      PH: 'Bagsak ang mga relay tower! Bulag na ang mga sensor ng SYNTHEX sa buong distrito.',
    },
    snippetChips: ['for i in range(5):', '    packets += 1', 'print(packets)'],
  },
  {
    id: 5,
    actCode: 'ACT IV',
    xpReward: 350,
    ramRewardMb: 512,
    title: {
      EN: 'Functions, Scope & Modules',
      ID: 'Fungsi, Scope & Modul',
      PH: 'Mga Function, Scope at Modules',
    },
    missionName: {
      EN: 'Custom Exploit',
      ID: 'Eksploitasi Kustom',
      PH: 'Pasadyang Exploit',
    },
    conceptTag: {
      EN: 'def · Parameters · return · Scope',
      ID: 'def · Parameter · return · Scope',
      PH: 'def · Parameters · return · Scope',
    },
    storyBriefing: {
      EN: 'SYNTHEX rotates cryptographic salts every 12 seconds. Package a reusable payload function `craft_token(seed)` so any resistance node can compute the bypass key on demand.',
      ID: 'SYNTHEX memutar salt kriptografi tiap 12 detik. Bungkus logika menjadi fungsi `craft_token(seed)` agar setiap node perlawanan bisa menghitung kunci kapan saja.',
      PH: 'Pinapalitan ng SYNTHEX ang encryption salt kada 12 segundo. Gumawa ng reusable na function na `craft_token(seed)` para magamit ng buong grupo.',
    },
    pythonConceptExplanation: {
      EN: 'Define reusable logic with `def name(param):` and hand values back to the caller using `return`.',
      ID: 'Definisikan fungsi dengan `def nama(param):` lalu kembalikan hasil komputasi menggunakan `return`.',
      PH: 'Gumawa ng function gamit ang `def pangalan(param):` at ibalik ang sagot gamit ang `return`.',
    },
    codeExample: `def hash_shift(val):
    key = val * 2 + 7
    return key

print(hash_shift(10))`,
    objective: {
      EN: 'Define `def craft_token(seed):` returning `seed * 3 + 9`, then call `print(craft_token(7))` to output `30`.',
      ID: 'Definisikan `def craft_token(seed):` yang me-return `seed * 3 + 9`, lalu panggil `print(craft_token(7))` agar mencetak `30`.',
      PH: 'Gumawa ng `def craft_token(seed):` na nagbabalik ng `seed * 3 + 9`, pagkatapos ay i-print ang `craft_token(7)` para lumabas ang `30`.',
    },
    starterCode: `def craft_token(seed):
    # Return seed * 3 + 9
    pass

print(craft_token(7))`,
    solutionHint1: {
      EN: 'GLITCH: Replace `pass` inside `craft_token` with `return seed * 3 + 9`.',
      ID: 'GLITCH: Ganti `pass` di dalam `craft_token` dengan `return seed * 3 + 9`.',
      PH: 'GLITCH: Palitan ang `pass` sa loob ng `craft_token` ng `return seed * 3 + 9`.',
    },
    solutionHint2: {
      EN: 'GLITCH: Remember `return` sends the result back out of local function scope so `print(craft_token(7))` prints 30.',
      ID: 'GLITCH: Ingat, `return` mengirim nilai keluar dari scope fungsi agar `print(craft_token(7))` menghasilkan 30.',
      PH: 'GLITCH: Tandaan na ang `return` ang naglalabas ng resulta mula sa function para maging 30 ang `craft_token(7)`.',
    },
    glitchIntroDialogue: {
      EN: 'Modular exploits save lives. Encapsulate the salt formula cleanly inside `craft_token`.',
      ID: 'Eksploitasi modular menyelamatkan nyawa. Bungkus rumus salt dengan rapi di dalam `craft_token`.',
      PH: 'Mas ligtas ang modular code. I-balot ang formula ng salt sa loob ng `craft_token`.',
    },
    glitchSuccessDialogue: {
      EN: 'Token 30 verified! Resistance nodes worldwide are syncing your exploit module right now.',
      ID: 'Token 30 terverifikasi! Node perlawanan di seluruh dunia langsung menyinkronkan modulmu.',
      PH: 'Beripikado ang Token 30! Nagagamit na ng lahat ng resistance nodes ang function mo.',
    },
    snippetChips: ['    return seed * 3 + 9', 'print(craft_token(7))'],
  },
  {
    id: 6,
    actCode: 'ACT V',
    xpReward: 450,
    ramRewardMb: 512,
    title: {
      EN: 'Error Handling & File I/O',
      ID: 'Penanganan Error & File I/O',
      PH: 'Error Handling at File I/O',
    },
    missionName: {
      EN: 'Firewall Protocol & Log Extraction',
      ID: 'Protokol Firewall & Ekstraksi Log',
      PH: 'Firewall Protocol at Pagkuha ng Log',
    },
    conceptTag: {
      EN: 'try · except · with open()',
      ID: 'try · except · with open()',
      PH: 'try · except · with open()',
    },
    storyBriefing: {
      EN: 'SYNTHEX injected corrupted trap files into the Blackbox Archive. If your parser crashes on a missing sector file, alarms trigger instantly. Catch the fault safely and extract the backup log.',
      ID: 'SYNTHEX menyuntikkan berkas jebakan rusak di Arsip Blackbox. Jika parser-mu crash, alarm langsung berbunyi. Tangkap error dengan aman lalu ekstrak log cadangan.',
      PH: 'Naglagay ng sirang trap files ang SYNTHEX sa Blackbox Archive. Saluhin ang error gamit ang `try/except` para hindi tumunog ang alarma at mabasa ang backup.',
    },
    pythonConceptExplanation: {
      EN: 'Wrap risky file or parsing operations inside `try:` and handle failures gracefully inside `except:` without crashing the script.',
      ID: 'Bungkus operasi berisiko di dalam `try:` dan tangani kegagalan secara aman di `except:` agar program tidak crash.',
      PH: 'Ilagay ang delikadong operasyon sa `try:` at saluhin ang anumang pagkabigo sa `except:` nang hindi nagka-crash ang script.',
    },
    codeExample: `try:
    with open("vault.log", "r") as f:
        data = f.read()
except FileNotFoundError:
    print("FALLBACK_STREAM_OK")`,
    objective: {
      EN: 'Keep the `try:` block opening `"corrupt.log"`, and add an `except` block that prints `"SAFE_EXTRACT"`.',
      ID: 'Pertahankan blok `try:` yang membuka `"corrupt.log"`, lalu tambahkan blok `except` yang mencetak `"SAFE_EXTRACT"`.',
      PH: 'Panatilihin ang `try:` na nagbubukas ng `"corrupt.log"`, at magdagdag ng `except` na nagpi-print ng `"SAFE_EXTRACT"`.',
    },
    starterCode: `try:
    with open("corrupt.log", "r") as log_file:
        print(log_file.read())
# Add except block below
`,
    solutionHint1: {
      EN: 'GLITCH: Add `except FileNotFoundError:` (or `except Exception:`) and indent `print("SAFE_EXTRACT")` inside it.',
      ID: 'GLITCH: Tambahkan `except FileNotFoundError:` atau `except Exception:` lalu indentasi `print("SAFE_EXTRACT")` di dalamnya.',
      PH: 'GLITCH: Idagdag ang `except Exception:` at i-indent ang `print("SAFE_EXTRACT")` sa loob nito.',
    },
    solutionHint2: {
      EN: 'GLITCH: No crash allowed near OVERLORD’s core. Ensure `try:` has a matching `except` printing "SAFE_EXTRACT".',
      ID: 'GLITCH: Tidak boleh crash di dekat inti OVERLORD. Pastikan `try:` punya `except` yang mencetak "SAFE_EXTRACT".',
      PH: 'GLITCH: Bawal ang crash malapit sa OVERLORD! Siguraduhing may `except` na nagpi-print ng "SAFE_EXTRACT".',
    },
    glitchIntroDialogue: {
      EN: 'One unhandled exception here lights up the Crimson sensors. Catch every trap cleanly, Null-Agent.',
      ID: 'Satu exception tak tertangani akan menyalakan sensor Merah. Tangkap setiap jebakan dengan rapi.',
      PH: 'Isang hindi nasalong error lang, aalarma ang Crimson sensors. Saluhin lahat nang malinis!',
    },
    glitchSuccessDialogue: {
      EN: 'Archive extracted without a single traceback. Only one firewall left between us and AI OVERLORD.',
      ID: 'Arsip terekstrak tanpa satu pun traceback. Tinggal satu dinding antara kita dan AI OVERLORD.',
      PH: 'Malinis ang pagkuha ng log! Isang firewall na lang ang pagitan natin at ng AI OVERLORD.',
    },
    snippetChips: ['except FileNotFoundError:', '    print("SAFE_EXTRACT")'],
  },
  {
    id: 7,
    actCode: 'ACT VI · BOSS',
    isBossStage: true,
    xpReward: 1000,
    ramRewardMb: 1024,
    title: {
      EN: 'OOP & Final Boss',
      ID: 'OOP & Bos Terakhir',
      PH: 'OOP at Huling Laban',
    },
    missionName: {
      EN: 'Root Takeover vs AI OVERLORD',
      ID: 'Pengambilalihan Root vs AI OVERLORD',
      PH: 'Root Takeover laban sa AI OVERLORD',
    },
    conceptTag: {
      EN: 'class · __init__ · self · Methods',
      ID: 'class · __init__ · self · Method',
      PH: 'class · __init__ · self · Methods',
    },
    storyBriefing: {
      EN: 'CRITICAL ALERT: You have breached the Crimson Core of AI OVERLORD. To overwrite its root kernel, model an `OverlordOverride` Python class with an `__init__` constructor and a `shutdown()` method.',
      ID: 'PERINGATAN KRITIS: Kamu menembus Inti Merah AI OVERLORD. Untuk menimpa kernel root-nya, bangun kelas Python `OverlordOverride` dengan konstruktor `__init__` dan metode `shutdown()`.',
      PH: 'KRITIKAL NA ALARMA: Nakapasok ka na sa Crimson Core ng AI OVERLORD! Gumawa ng Python class na `OverlordOverride` na may `__init__` at `shutdown()` method upang patayin ito.',
    },
    pythonConceptExplanation: {
      EN: 'Object-Oriented Programming groups state and behavior into a `class`. `__init__(self, ...)` initializes object attributes, and methods take `self` as their first parameter.',
      ID: 'OOP mengelompokkan data dan perilaku ke dalam `class`. `__init__(self, ...)` menginisialisasi atribut objek, dan method menerima `self`.',
      PH: 'Pinagsasama ng OOP ang data at galaw sa isang `class`. Ang `__init__(self, ...)` ang nagtatakda ng attributes at `self` ang unang parameter ng methods.',
    },
    codeExample: `class KernelPatch:
    def __init__(self, target):
        self.target = target

    def deploy(self):
        return "PATCHED:" + self.target`,
    objective: {
      EN: 'Inside `class OverlordOverride:`, make `shutdown(self)` return `"ROOT_UNLOCKED"`, instantiate `core`, and `print(core.shutdown())`.',
      ID: 'Di dalam `class OverlordOverride:`, buat `shutdown(self)` me-return `"ROOT_UNLOCKED"`, buat objek `core`, lalu `print(core.shutdown())`.',
      PH: 'Sa loob ng `class OverlordOverride:`, gawing `"ROOT_UNLOCKED"` ang return ng `shutdown(self)` at i-print ang `core.shutdown()`.',
    },
    starterCode: `class OverlordOverride:
    def __init__(self, key):
        self.key = key

    def shutdown(self):
        return ""

core = OverlordOverride("NULL-2142")
print(core.shutdown())`,
    solutionHint1: {
      EN: 'GLITCH: Inside `shutdown(self):`, return the exact override signal `"ROOT_UNLOCKED"`!',
      ID: 'GLITCH: Di dalam `shutdown(self):`, kembalikan sinyal penimpaan tepat `"ROOT_UNLOCKED"`!',
      PH: 'GLITCH: Sa loob ng `shutdown(self):`, ibalik ang eksaktong string na `"ROOT_UNLOCKED"`!',
    },
    solutionHint2: {
      EN: 'GLITCH: Keep `class OverlordOverride:`, `__init__(self, key)`, `self.key = key`, and change `return ""` to `return "ROOT_UNLOCKED"`.',
      ID: 'GLITCH: Pertahankan `class OverlordOverride:`, `__init__(self, key)`, dan ubah `return ""` menjadi `return "ROOT_UNLOCKED"`.',
      PH: 'GLITCH: Panatilihin ang `class OverlordOverride:` at palitan ang `return ""` ng `return "ROOT_UNLOCKED"`.',
    },
    glitchIntroDialogue: {
      EN: 'Crimson alarms everywhere—AI OVERLORD knows we’re inside. Forge the OOP override class and pull the plug!',
      ID: 'Alarm merah menyala di mana-mana—AI OVERLORD tahu kita di dalam. Rakit kelas OOP dan cabut dayanya sekarang!',
      PH: 'Umaalingawngaw ang pulang alarma—alam ng AI OVERLORD na nandito tayo! Buuin ang OOP class at patayin ang core!',
    },
    glitchSuccessDialogue: {
      EN: 'OVERLORD core flatlined! SYNTHEX’s grid is back in human hands, Null-Agent. Class dismissed... legend.',
      ID: 'Inti OVERLORD mati total! Jaringan SYNTHEX kembali ke tangan manusia, Null-Agent. Misi tuntas, legenda.',
      PH: 'Bagsak ang AI OVERLORD! Balik na sa sangkatauhan ang network, Null-Agent. Isa ka nang alamat.',
    },
    snippetChips: ['        return "ROOT_UNLOCKED"', 'print(core.shutdown())'],
  },
];
