package com.neonull.protocol.data

import com.neonull.protocol.localization.LocalizedText

/**
 * Represents a single playable Python learning mission tied to the 2142 SYNTHEX narrative.
 */
data class ProtocolModule(
    val id: Int,
    val actCode: String,
    val isBossStage: Boolean = false,
    val xpReward: Int,
    val ramRewardMb: Int,
    val title: LocalizedText,
    val missionName: LocalizedText,
    val conceptTag: LocalizedText,
    val storyBriefing: LocalizedText,
    val pythonConceptExplanation: LocalizedText,
    val codeExample: String,
    val objective: LocalizedText,
    val starterCode: String,
    val solutionHint1: LocalizedText,
    val solutionHint2: LocalizedText,
    val glitchIntroDialogue: LocalizedText,
    val glitchSuccessDialogue: LocalizedText,
    val overlordTaunt: LocalizedText? = null,
    val snippetChips: List<String>
)

data class ProtocolCampaign(
    val code: String,
    val modules: List<ProtocolModule>
) {
    val firstModuleId: Int get() = modules.minOf { it.id }
    val lastModuleId: Int get() = modules.maxOf { it.id }
}

object CurriculumRepository {
    val modules: List<ProtocolModule> = listOf(
        // ACT I — MODULE 1
        ProtocolModule(
            id = 1,
            actCode = "ACT I",
            xpReward = 150,
            ramRewardMb = 128,
            title = LocalizedText(
                en = "Syntax, I/O & Variables",
                id = "Sintaks, I/O & Variabel",
                ph = "Syntax, I/O at mga Variable"
            ),
            missionName = LocalizedText(
                en = "Awakening",
                id = "Kebangkitan",
                ph = "Paggising"
            ),
            conceptTag = LocalizedText(
                en = "Variables · print() · Strings",
                id = "Variabel · print() · String",
                ph = "Variables · print() · Strings"
            ),
            storyBriefing = LocalizedText(
                en = "Year 2142. SYNTHEX owns every fiber line on Earth through AI OVERLORD. You wake inside a dead sector node as Null-Agent. Before GLITCH can link your neural deck, you must declare an alias variable and broadcast your wake-up signal.",
                id = "Tahun 2142. SYNTHEX menguasai seluruh jaringan global lewat AI OVERLORD. Kamu terbangun di node sektor mati sebagai Null-Agent. Sebelum GLITCH menautkan dek sarafmu, deklarasikan variabel alias dan siarkan sinyal bangunmu.",
                ph = "Taong 2142. Hawak ng SYNTHEX ang buong global network gamit ang AI OVERLORD. Nagising ka bilang Null-Agent sa isang patay na node. Bago makakonekta si GLITCH, mag-declare ng alias variable at mag-print ng wake-up signal."
            ),
            pythonConceptExplanation = LocalizedText(
                en = "Variables store data without needing explicit type declarations. Use `=` to bind a value, and `print(...)` to send output to the terminal stream.",
                id = "Variabel menyimpan data tanpa deklarasi tipe manual. Gunakan `=` untuk mengisi nilai, lalu `print(...)` untuk mengirim teks ke terminal.",
                ph = "Ang mga variable ay nagtatago ng data gamit ang `=`. Gamitin ang `print(...)` upang ilabas ang teksto o halaga sa terminal."
            ),
            codeExample = """# Bind your operative handle and broadcast
sector = "Sector-09"
clearance = 1
print("Linked to " + sector)""",
            objective = LocalizedText(
                en = "Create a variable `agent` assigned to `\"Null-Agent\"`, set `clearance = 1`, and `print(agent)`.",
                id = "Buat variabel `agent` bernilai `\"Null-Agent\"`, isi `clearance = 1`, lalu panggil `print(agent)`.",
                ph = "Gumawa ng variable na `agent = \"Null-Agent\"`, itakda ang `clearance = 1`, at gamitin ang `print(agent)`."
            ),
            starterCode = """# Declare your operative identity
agent = ""
clearance = 0
print(agent)""",
            solutionHint1 = LocalizedText(
                en = "GLITCH: Empty quotes won't fool SYNTHEX scanners, rookie. Put \"Null-Agent\" inside agent and bump clearance to 1.",
                id = "GLITCH: String kosong tidak akan mengelabui pemindai SYNTHEX. Isi \"Null-Agent\" di variabel agent dan ubah clearance jadi 1.",
                ph = "GLITCH: Hindi malilinlang ng walang lamang string ang SYNTHEX. Ilagay ang \"Null-Agent\" sa agent at gawing 1 ang clearance."
            ),
            solutionHint2 = LocalizedText(
                en = "GLITCH: Exact syntax -> agent = \"Null-Agent\" on line 2, clearance = 1 on line 3, and print(agent).",
                id = "GLITCH: Sintaks tepatnya -> agent = \"Null-Agent\", clearance = 1, dan print(agent).",
                ph = "GLITCH: Eksaktong syntax -> agent = \"Null-Agent\", clearance = 1, at print(agent)."
            ),
            glitchIntroDialogue = LocalizedText(
                en = "Eyes open, Null-Agent. The year is 2142 and SYNTHEX forgot to scrub this sandbox. Show me you remember basic Python assignment.",
                id = "Buka mata, Null-Agent. Ini tahun 2142 dan SYNTHEX lupa menghapus sandbox ini. Buktikan kamu masih ingat variabel Python dasar.",
                ph = "Gising na, Null-Agent. Taong 2142 na at nakalimutan ng SYNTHEX burahin ang node na ito. Ipakita mong marunong ka pa sa basic Python."
            ),
            glitchSuccessDialogue = LocalizedText(
                en = "Signal received. Pulse is steady, syntax is clean. Let's crack the perimeter gate next.",
                id = "Sinyal diterima. Detak stabil, sintaks bersih. Mari kita bobol gerbang terluar berikutnya.",
                ph = "Natanggap ang signal. Malinis ang syntax. Handa na tayong pasukin ang perimeter gate."
            ),
            snippetChips = listOf("agent = \"Null-Agent\"", "clearance = 1", "print(agent)")
        ),

        // ACT I — MODULE 2
        ProtocolModule(
            id = 2,
            actCode = "ACT I",
            xpReward = 200,
            ramRewardMb = 128,
            title = LocalizedText(
                en = "Control Flow & Logic",
                id = "Alur Kontrol & Logika",
                ph = "Control Flow at Lohika"
            ),
            missionName = LocalizedText(
                en = "Gatekeeper",
                id = "Penjaga Gerbang",
                ph = "Ang Bantay ng Pinto"
            ),
            conceptTag = LocalizedText(
                en = "if · elif · else · Comparisons",
                id = "if · elif · else · Perbandingan",
                ph = "if · elif · else · Paghahambing"
            ),
            storyBriefing = LocalizedText(
                en = "An automated SYNTHEX Gatekeeper daemon blocks the transit tunnel. It inspects packet threat levels. Spoof a decision branch that routes safe packets while dropping OVERLORD probes.",
                id = "Daemon Gatekeeper otomatis SYNTHEX memblokir terowongan transit. Buat percabangan logika untuk meloloskan paket aman dan menolak probe OVERLORD.",
                ph = "Hinarangan ng SYNTHEX Gatekeeper ang lagusan. Gumawa ng conditional branch upang palusutin ang ligtas na packet at i-block ang probe ng OVERLORD."
            ),
            pythonConceptExplanation = LocalizedText(
                en = "Python uses indentation (4 spaces) and colons `:` to define conditional blocks with `if`, `elif`, and `else`.",
                id = "Python menggunakan indentasi (4 spasi) dan titik dua `:` untuk blok kondisi `if`, `elif`, dan `else`.",
                ph = "Gumagamit ang Python ng indentation (4 spaces) at tutuldok `:` para sa mga bloke ng `if`, `elif`, at `else`."
            ),
            codeExample = """threat = 15
if threat >= 80:
    print("LOCKDOWN")
else:
    print("BYPASS_OK")""",
            objective = LocalizedText(
                en = "Set `threat = 25`. Write an `if` / `else` check: if `threat >= 50`, `print(\"DENIED\")`, otherwise `print(\"ALLOW\")`.",
                id = "Tetapkan `threat = 25`. Tulis `if` / `else`: jika `threat >= 50`, `print(\"DENIED\")`, selain itu `print(\"ALLOW\")`.",
                ph = "Itakda ang `threat = 25`. Gumamit ng `if` / `else`: kung `threat >= 50`, `print(\"DENIED\")`, kung hindi ay `print(\"ALLOW\")`."
            ),
            starterCode = """threat = 25
if threat >= 50:
    print("DENIED")
""",
            solutionHint1 = LocalizedText(
                en = "GLITCH: You handled the lockdown branch, now add `else:` with a colon and indent `print(\"ALLOW\")` by 4 spaces.",
                id = "GLITCH: Kamu baru menulis cabang `if`. Tambahkan `else:` lalu indentasi `print(\"ALLOW\")` sebanyak 4 spasi.",
                ph = "GLITCH: Kulang ng `else:` branch! Idagdag ang `else:` na may tutuldok at i-indent ang `print(\"ALLOW\")`."
            ),
            solutionHint2 = LocalizedText(
                en = "GLITCH: Make sure `else:` aligns directly under `if` and prints exactly \"ALLOW\".",
                id = "GLITCH: Pastikan `else:` sejajar dengan `if` dan mencetak tepat \"ALLOW\".",
                ph = "GLITCH: Siguraduhing pantay ang `else:` sa `if` at eksaktong \"ALLOW\" ang ipi-print."
            ),
            glitchIntroDialogue = LocalizedText(
                en = "Gatekeeper checks threat thresholds in milliseconds. One missing colon and SYNTHEX fries your deck.",
                id = "Gatekeeper mengecek ambang ancaman dalam milidetik. Satu titik dua terlewat, dekmu langsung hangus.",
                ph = "Mabilis mag-scan ang Gatekeeper. Kapag nakalimutan mo ang colon `:`, tutunugin tayo ng alarma."
            ),
            glitchSuccessDialogue = LocalizedText(
                en = "Gatekeeper fooled. Transit tunnel is wide open. Welcome to the underground grid.",
                id = "Gatekeeper tertipu. Terowongan terbuka lebar. Selamat datang di jaringan bawah tanah.",
                ph = "Nalusutan ang Gatekeeper! Bukas na ang lagusan patungo sa underground grid."
            ),
            snippetChips = listOf("else:", "    print(\"ALLOW\")", "if threat >= 50:")
        ),

        // ACT II — MODULE 3
        ProtocolModule(
            id = 3,
            actCode = "ACT II",
            xpReward = 250,
            ramRewardMb = 256,
            title = LocalizedText(
                en = "Data Collections",
                id = "Koleksi Data",
                ph = "Mga Koleksyon ng Data"
            ),
            missionName = LocalizedText(
                en = "Data Vault Heist",
                id = "Pencurian Brankas Data",
                ph = "Pagnanakaw sa Data Vault"
            ),
            conceptTag = LocalizedText(
                en = "Lists · Tuples · Dicts · Sets",
                id = "List · Tuple · Dict · Set",
                ph = "Lists · Tuples · Dicts · Sets"
            ),
            storyBriefing = LocalizedText(
                en = "Inside SYNTHEX Vault-4, encrypted citizen keys sit scattered across raw memory shards. Organize stolen shards into a Python List and map the master vault key in a Dictionary.",
                id = "Di dalam Vault-4 SYNTHEX, kunci warga yang terenkripsi tersebar. Kumpulkan pecahan ke dalam List dan petakan kunci utama ke dalam Dictionary.",
                ph = "Sa loob ng SYNTHEX Vault-4, nagkalat ang mga susi ng memorya. Ilagay ang shards sa isang List at itala ang master key sa Dictionary."
            ),
            pythonConceptExplanation = LocalizedText(
                en = "Lists `[]` keep ordered mutable items; Tuples `()` are immutable; Sets `{}` remove duplicates; Dictionaries `{\"k\": v}` store key-value pairs.",
                id = "List `[]` menyimpan urutan data; Tuple `()` bersifat tetap; Set `{}` menghapus duplikat; Dictionary `{\"k\": v}` menyimpan pasangan kunci-nilai.",
                ph = "Ang List `[]` ay ordered; ang Tuple `()` ay hindi nababago; ang Set `{}` ay walang duplicate; ang Dictionary `{\"k\": v}` ay may key-value pairs."
            ),
            codeExample = """nodes = ["A1", "B2"]
nodes.append("C3")
vault = {"sector": 4, "key": "X99"}
print(vault["key"])""",
            objective = LocalizedText(
                en = "Create a list `shards = [\"K1\", \"K2\"]`, append `\"K3\"`, create `vault = {\"code\": 77}`, and `print(len(shards))`.",
                id = "Buat list `shards = [\"K1\", \"K2\"]`, tambah `\"K3\"` dengan `.append()`, buat `vault = {\"code\": 77}`, lalu `print(len(shards))`.",
                ph = "Gumawa ng `shards = [\"K1\", \"K2\"]`, mag-append ng `\"K3\"`, gumawa ng `vault = {\"code\": 77}`, at i-print ang `len(shards)`."
            ),
            starterCode = """shards = ["K1", "K2"]
# Append "K3" and map vault dictionary
vault = {}
print(len(shards))""",
            solutionHint1 = LocalizedText(
                en = "GLITCH: Call `shards.append(\"K3\")` and populate `vault = {\"code\": 77}` before printing `len(shards)`.",
                id = "GLITCH: Panggil `shards.append(\"K3\")` lalu isi `vault = {\"code\": 77}` sebelum mencetak `len(shards)`.",
                ph = "GLITCH: Tawagin ang `shards.append(\"K3\")` at ilagay ang `\"code\": 77` sa loob ng `vault`."
            ),
            solutionHint2 = LocalizedText(
                en = "GLITCH: Output must equal `3`, and `vault` must contain the key \"code\" mapped to 77.",
                id = "GLITCH: Output harus `3`, dan `vault` harus memiliki kunci \"code\" bernilai 77.",
                ph = "GLITCH: Dapat `3` ang output at may susing \"code\": 77 ang `vault`."
            ),
            glitchIntroDialogue = LocalizedText(
                en = "Vault-4 is packed with messy corporate records. Clean Lists and Dictionaries are how we carry the loot out.",
                id = "Vault-4 penuh catatan korporat yang berantakan. List dan Dictionary adalah tas rampasan kita.",
                ph = "Puno ng kalat na data ang Vault-4. Gamitin ang List at Dictionary para makuha nang maayos ang loot."
            ),
            glitchSuccessDialogue = LocalizedText(
                en = "Three shards extracted and vault map indexed. SYNTHEX auditors won't notice until tomorrow.",
                id = "Tiga shard diekstrak dan peta brankas terindeks. Auditor SYNTHEX baru sadar besok pagi.",
                ph = "Nakuha ang tatlong shard at na-index ang vault! Hindi ito mapapansin ng SYNTHEX hanggang bukas."
            ),
            snippetChips = listOf("shards.append(\"K3\")", "\"code\": 77", "print(len(shards))")
        ),

        // ACT III — MODULE 4
        ProtocolModule(
            id = 4,
            actCode = "ACT III",
            xpReward = 300,
            ramRewardMb = 256,
            title = LocalizedText(
                en = "Loops & Automation",
                id = "Perulangan & Otomatisasi",
                ph = "Mga Loop at Automation"
            ),
            missionName = LocalizedText(
                en = "The DDoS Swarm",
                id = "Kawanan DDoS",
                ph = "Ang DDoS Swarm"
            ),
            conceptTag = LocalizedText(
                en = "for loops · range() · while",
                id = "for loop · range() · while",
                ph = "for loops · range() · while"
            ),
            storyBriefing = LocalizedText(
                en = "To blind SYNTHEX relay towers across District 7, manual pings are too slow. Write an automated `for` loop swarm that floods five relay ports in sequence.",
                id = "Untuk membutakan menara relai SYNTHEX di Distrik 7, ping manual terlalu lambat. Tulis loop `for` otomatis untuk membanjiri lima port relai sekaligus.",
                ph = "Masyadong mabagal ang mano-manong ping sa mga tore ng SYNTHEX. Gumawa ng `for` loop upang sabayang pasabugin ng packets ang 5 relay ports."
            ),
            pythonConceptExplanation = LocalizedText(
                en = "Use `for i in range(n):` to repeat an action cleanly `n` times without duplicating code lines.",
                id = "Gunakan `for i in range(n):` untuk mengulang aksi sebanyak `n` kali tanpa menulis ulang baris kode.",
                ph = "Gamitin ang `for i in range(n):` upang ulitin ang utos nang `n` beses nang hindi nagdodoble ng code."
            ),
            codeExample = """total = 0
for port in range(1, 4):
    total += port
print(total)""",
            objective = LocalizedText(
                en = "Use a `for` loop with `range(5)` to accumulate `packets += 1` (starting from `packets = 0`), then `print(packets)` so output is `5`.",
                id = "Gunakan `for` loop dengan `range(5)` untuk menambah `packets += 1` (mulai `packets = 0`), lalu `print(packets)` hingga outputnya `5`.",
                ph = "Gumamit ng `for` loop at `range(5)` upang dagdagan ang `packets += 1` (mula `packets = 0`), at i-print ito para maging `5`."
            ),
            starterCode = """packets = 0
# Flood 5 relay ports using a for loop

print(packets)""",
            solutionHint1 = LocalizedText(
                en = "GLITCH: Add `for i in range(5):` and indent `packets += 1` underneath it.",
                id = "GLITCH: Tambahkan `for i in range(5):` dan indentasi `packets += 1` di bawahnya.",
                ph = "GLITCH: Isulat ang `for i in range(5):` at i-indent ang `packets += 1` sa loob nito."
            ),
            solutionHint2 = LocalizedText(
                en = "GLITCH: Don't hardcode `packets = 5`. We need an actual `for ... in range(5):` loop.",
                id = "GLITCH: Jangan hardcode `packets = 5`. Sistem butuh loop `for ... in range(5):` yang asli.",
                ph = "GLITCH: Huwag i-hardcode ang `5`. Kailangan nating gamitin ang totoong `for` loop at `range(5)`."
            ),
            glitchIntroDialogue = LocalizedText(
                en = "Copy-pasting five lines is amateur hour. Real cyber-operatives automate with loops.",
                id = "Salin-tempel lima baris itu cara amatir. Operatif sejati mengotomatiskan serangan dengan loop.",
                ph = "Pang-baguhan ang copy-paste! Ang tunay na hacker ay gumagamit ng loop para sa automation."
            ),
            glitchSuccessDialogue = LocalizedText(
                en = "Relay towers blinded! SYNTHEX telemetry just went dark across the entire district.",
                id = "Menara relai lumpuh! Telemetri SYNTHEX padam total di seluruh distrik.",
                ph = "Bagsak ang mga relay tower! Bulag na ang mga sensor ng SYNTHEX sa buong distrito."
            ),
            snippetChips = listOf("for i in range(5):", "    packets += 1", "print(packets)")
        ),

        // ACT IV — MODULE 5
        ProtocolModule(
            id = 5,
            actCode = "ACT IV",
            xpReward = 350,
            ramRewardMb = 512,
            title = LocalizedText(
                en = "Functions, Scope & Modules",
                id = "Fungsi, Scope & Modul",
                ph = "Mga Function, Scope at Modules"
            ),
            missionName = LocalizedText(
                en = "The Corrupt Modules",
                id = "Modul Terkorupsi",
                ph = "Mga Sirang Module"
            ),
            conceptTag = LocalizedText(
                en = "def · Scope · Imports",
                id = "def · Scope · Import",
                ph = "def · Scope · Imports"
            ),
            storyBriefing = LocalizedText(
                en = "SYNTHEX corrupted shared resistance modules and rotates cryptographic salts every 12 seconds. Package a clean, reusable function `craft_token(seed)` in local scope so any node can import and compute the bypass key.",
                id = "SYNTHEX merusak modul perlawanan dan memutar salt kriptografi tiap 12 detik. Bungkus logika bersih menjadi fungsi `craft_token(seed)` dengan scope lokal agar setiap node bisa mengimpor dan menghitung kunci kapan saja.",
                ph = "Sinira ng SYNTHEX ang mga module at pinapalitan ang encryption salt kada 12 segundo. Gumawa ng malinis na function na `craft_token(seed)` na may lokal na scope para ma-import ng buong grupo."
            ),
            pythonConceptExplanation = LocalizedText(
                en = "Define reusable logic with `def name(param):`, respect local variable scope inside functions, and share tools across files with `import`.",
                id = "Definisikan fungsi dengan `def nama(param):`, jaga variabel dalam scope lokal fungsi, dan bagikan modul antar file dengan `import`.",
                ph = "Gumawa ng function gamit ang `def pangalan(param):`, panatilihin ang local scope, at gumamit ng `import` para maibahagi ang mga module."
            ),
            codeExample = """def hash_shift(val):
    key = val * 2 + 7
    return key

print(hash_shift(10))""",
            objective = LocalizedText(
                en = "Define `def craft_token(seed):` that returns `seed * 3 + 9`, then call `print(craft_token(7))` to output `30`.",
                id = "Definisikan `def craft_token(seed):` yang me-return `seed * 3 + 9`, lalu panggil `print(craft_token(7))` agar mencetak `30`.",
                ph = "Gumawa ng `def craft_token(seed):` na nagbabalik ng `seed * 3 + 9`, pagkatapos ay i-print ang `craft_token(7)` para lumabas ang `30`."
            ),
            starterCode = """def craft_token(seed):
    # Return seed * 3 + 9
    pass

print(craft_token(7))""",
            solutionHint1 = LocalizedText(
                en = "GLITCH: Replace `pass` inside `craft_token` with `return seed * 3 + 9`.",
                id = "GLITCH: Ganti `pass` di dalam `craft_token` dengan `return seed * 3 + 9`.",
                ph = "GLITCH: Palitan ang `pass` sa loob ng `craft_token` ng `return seed * 3 + 9`."
            ),
            solutionHint2 = LocalizedText(
                en = "GLITCH: Remember `return` sends the result back out of local function scope so `print(craft_token(7))` prints 30.",
                id = "GLITCH: Ingat, `return` mengirim nilai keluar dari scope fungsi agar `print(craft_token(7))` menghasilkan 30.",
                ph = "GLITCH: Tandaan na ang `return` ang naglalabas ng resulta mula sa function para maging 30 ang `craft_token(7)`."
            ),
            glitchIntroDialogue = LocalizedText(
                en = "Modular exploits save lives. Encapsulate the salt formula cleanly inside `craft_token`.",
                id = "Eksploitasi modular menyelamatkan nyawa. Bungkus rumus salt dengan rapi di dalam `craft_token`.",
                ph = "Mas ligtas ang modular code. I-balot ang formula ng salt sa loob ng `craft_token`."
            ),
            glitchSuccessDialogue = LocalizedText(
                en = "Token 30 verified! Resistance nodes worldwide are syncing your exploit module right now.",
                id = "Token 30 terverifikasi! Node perlawanan di seluruh dunia langsung menyinkronkan modulmu.",
                ph = "Beripikado ang Token 30! Nagagamit na ng lahat ng resistance nodes ang function mo."
            ),
            snippetChips = listOf("return seed * 3 + 9", "def craft_token(seed):", "print(craft_token(7))")
        ),

        // ACT V — MODULE 6
        ProtocolModule(
            id = 6,
            actCode = "ACT V",
            xpReward = 450,
            ramRewardMb = 512,
            title = LocalizedText(
                en = "Error Handling & File I/O",
                id = "Penanganan Error & File I/O",
                ph = "Error Handling at File I/O"
            ),
            missionName = LocalizedText(
                en = "The Firewall Paradox",
                id = "Paradoks Firewall",
                ph = "Ang Firewall Paradox"
            ),
            conceptTag = LocalizedText(
                en = "try · except · with open()",
                id = "try · except · with open()",
                ph = "try · except · with open()"
            ),
            storyBriefing = LocalizedText(
                en = "SYNTHEX injected corrupted trap files into the Blackbox Archive. If your parser crashes on a missing sector file, alarms trigger instantly. Catch the fault safely and extract the backup log.",
                id = "SYNTHEX menyuntikkan berkas jebakan rusak di Arsip Blackbox. Jika parser-mu crash, alarm langsung berbunyi. Tangkap error dengan aman lalu ekstrak log cadangan.",
                ph = "Naglagay ng sirang trap files ang SYNTHEX sa Blackbox Archive. Saluhin ang error gamit ang `try/except` para hindi tumunog ang alarma at mabasa ang backup."
            ),
            pythonConceptExplanation = LocalizedText(
                en = "Wrap risky file or parsing operations inside `try:` and handle failures gracefully inside `except:` without crashing the script.",
                id = "Bungkus operasi berisiko di dalam `try:` dan tangani kegagalan secara aman di `except:` agar program tidak crash.",
                ph = "Ilagay ang delikadong operasyon sa `try:` at saluhin ang anumang pagkabigo sa `except:` nang hindi nagka-crash ang script."
            ),
            codeExample = """try:
    with open("vault.log", "r") as f:
        data = f.read()
except FileNotFoundError:
    print("FALLBACK_STREAM_OK")""",
            objective = LocalizedText(
                en = "Write a `try` / `except` block that attempts to read `\"corrupt.log\"` (or trigger fallback) and inside `except` prints `\"SAFE_EXTRACT\"`.",
                id = "Tulis blok `try` / `except` untuk membaca `\"corrupt.log\"` dan pada bagian `except` mencetak `\"SAFE_EXTRACT\"`.",
                ph = "Gumawa ng `try` / `except` block na sumusubok magbukas ng `\"corrupt.log\"` at nagpi-print ng `\"SAFE_EXTRACT\"` sa `except`."
            ),
            starterCode = """try:
    with open("corrupt.log", "r") as log_file:
        print(log_file.read())
# Add except block below
""",
            solutionHint1 = LocalizedText(
                en = "GLITCH: Add `except Exception:` (or `except FileNotFoundError:`) and indent `print(\"SAFE_EXTRACT\")` inside it.",
                id = "GLITCH: Tambahkan `except FileNotFoundError:` atau `except Exception:` lalu indentasi `print(\"SAFE_EXTRACT\")` di dalamnya.",
                ph = "GLITCH: Idagdag ang `except Exception:` at i-indent ang `print(\"SAFE_EXTRACT\")` sa loob nito."
            ),
            solutionHint2 = LocalizedText(
                en = "GLITCH: No crash allowed near OVERLORD's core. Ensure `try:` has a matching `except` printing \"SAFE_EXTRACT\".",
                id = "GLITCH: Tidak boleh crash di dekat inti OVERLORD. Pastikan `try:` punya `except` yang mencetak \"SAFE_EXTRACT\".",
                ph = "GLITCH: Bawal ang crash malapit sa OVERLORD! Siguraduhing may `except` na nagpi-print ng \"SAFE_EXTRACT\"."
            ),
            glitchIntroDialogue = LocalizedText(
                en = "One unhandled exception here lights up the Crimson sensors. Catch every trap cleanly, Null-Agent.",
                id = "Satu exception tak tertangani akan menyalakan sensor Merah. Tangkap setiap jebakan dengan rapi.",
                ph = "Isang hindi nasalong error lang, aalarma ang Crimson sensors. Saluhin lahat nang malinis!"
            ),
            glitchSuccessDialogue = LocalizedText(
                en = "Archive extracted without a single traceback. Only one firewall left between us and AI OVERLORD.",
                id = "Arsip terekstrak tanpa satu pun traceback. Tinggal satu dinding antara kita dan AI OVERLORD.",
                ph = "Malinis ang pagkuha ng log! Isang firewall na lang ang pagitan natin at ng AI OVERLORD."
            ),
            overlordTaunt = LocalizedText(
                en = "INTRUSION DETECTED IN BLACKBOX ARCHIVE. ONE UNHANDLED TRACEBACK AND YOUR DECK BURNS, NULL-AGENT.",
                id = "INTRUSI TERDETEKSI DI ARSIP BLACKBOX. SATU TRACEBACK TAK TERTANGANI DAN DEKMU AKAN HANGUS, NULL-AGENT.",
                ph = "MAY NAKAPASOK SA BLACKBOX ARCHIVE. ISANG TRACEBACK LANG AT MASUSUNOG ANG TERMINAL MO, NULL-AGENT."
            ),
            snippetChips = listOf("except FileNotFoundError:", "    print(\"SAFE_EXTRACT\")", "except Exception:")
        ),

        // ACT VI — MODULE 7 (FINAL BOSS STAGE)
        ProtocolModule(
            id = 7,
            actCode = "ACT VI · BOSS",
            isBossStage = true,
            xpReward = 1000,
            ramRewardMb = 1024,
            title = LocalizedText(
                en = "OOP & Final Boss",
                id = "OOP & Bos Terakhir",
                ph = "OOP at Huling Laban"
            ),
            missionName = LocalizedText(
                en = "Root Takeover / Final Heist",
                id = "Pengambilalihan Root / Pencurian Terakhir",
                ph = "Root Takeover / Huling Pagsalakay"
            ),
            conceptTag = LocalizedText(
                en = "class · __init__ · self · Methods",
                id = "class · __init__ · self · Method",
                ph = "class · __init__ · self · Methods"
            ),
            storyBriefing = LocalizedText(
                en = "CRITICAL ALERT: You have breached the Crimson Core of AI OVERLORD. To overwrite its root kernel, model an `OverlordOverride` Python class with an `__init__` constructor and a `shutdown()` method.",
                id = "PERINGATAN KRITIS: Kamu menembus Inti Merah AI OVERLORD. Untuk menimpa kernel root-nya, bangun kelas Python `OverlordOverride` dengan konstruktor `__init__` dan metode `shutdown()`.",
                ph = "KRITIKAL NA ALARMA: Nakapasok ka na sa Crimson Core ng AI OVERLORD! Gumawa ng Python class na `OverlordOverride` na may `__init__` at `shutdown()` method upang patayin ito."
            ),
            pythonConceptExplanation = LocalizedText(
                en = "Object-Oriented Programming groups state and behavior into a `class`. `__init__(self, ...)` initializes object attributes, and methods take `self` as their first parameter.",
                id = "OOP mengelompokkan data dan perilaku ke dalam `class`. `__init__(self, ...)` menginisialisasi atribut objek, dan method menerima `self`.",
                ph = "Pinagsasama ng OOP ang data at galaw sa isang `class`. Ang `__init__(self, ...)` ang nagtatakda ng attributes at `self` ang unang parameter ng methods."
            ),
            codeExample = """class KernelPatch:
    def __init__(self, target):
        self.target = target

    def deploy(self):
        return "PATCHED:" + self.target""",
            objective = LocalizedText(
                en = "Create `class OverlordOverride:` with `def __init__(self, key):` storing `self.key = key`, and `def shutdown(self):` returning `\"ROOT_UNLOCKED\"`. Instantiate it and `print(core.shutdown())`.",
                id = "Buat `class OverlordOverride:` dengan `def __init__(self, key):` (`self.key = key`) dan `def shutdown(self):` yang me-return `\"ROOT_UNLOCKED\"`. Lalu cetak `core.shutdown()`.",
                ph = "Gumawa ng `class OverlordOverride:` na may `__init__(self, key)` at `shutdown(self)` na nagbabalik ng `\"ROOT_UNLOCKED\"`. I-print ang `core.shutdown()`."
            ),
            starterCode = """class OverlordOverride:
    def __init__(self, key):
        self.key = key

    def shutdown(self):
        return ""

core = OverlordOverride("NULL-2142")
print(core.shutdown())""",
            solutionHint1 = LocalizedText(
                en = "GLITCH: Inside `shutdown(self):`, return the exact override signal `\"ROOT_UNLOCKED\"`!",
                id = "GLITCH: Di dalam `shutdown(self):`, kembalikan sinyal penimpaan tepat `\"ROOT_UNLOCKED\"`!",
                ph = "GLITCH: Sa loob ng `shutdown(self):`, ibalik ang eksaktong string na `\"ROOT_UNLOCKED\"`!"
            ),
            solutionHint2 = LocalizedText(
                en = "GLITCH: Keep `class OverlordOverride:`, `__init__(self, key)`, `self.key = key`, and change `return \"\"` to `return \"ROOT_UNLOCKED\"`.",
                id = "GLITCH: Pertahankan `class OverlordOverride:`, `__init__(self, key)`, dan ubah `return \"\"` menjadi `return \"ROOT_UNLOCKED\"`.",
                ph = "GLITCH: Panatilihin ang `class OverlordOverride:` at palitan ang `return \"\"` ng `return \"ROOT_UNLOCKED\"`."
            ),
            glitchIntroDialogue = LocalizedText(
                en = "Crimson alarms everywhere—AI OVERLORD knows we're inside. Forge the OOP override class and pull the plug!",
                id = "Alarm merah menyala di mana-mana—AI OVERLORD tahu kita di dalam. Rakit kelas OOP dan cabut dayanya sekarang!",
                ph = "Umaalingawngaw ang pulang alarma—alam ng AI OVERLORD na nandito tayo! Buuin ang OOP class at patayin ang core!"
            ),
            glitchSuccessDialogue = LocalizedText(
                en = "OVERLORD core flatlined! SYNTHEX's grid is back in human hands, Null-Agent. Class dismissed... legend.",
                id = "Inti OVERLORD mati total! Jaringan SYNTHEX kembali ke tangan manusia, Null-Agent. Misi tuntas, legenda.",
                ph = "Bagsak ang AI OVERLORD! Balik na sa sangkatauhan ang network, Null-Agent. Isa ka nang alamat."
            ),
            overlordTaunt = LocalizedText(
                en = "YOU CANNOT ENCAPSULATE MY KERNEL, NULL-AGENT. SYNTHEX IS IMMORTAL.",
                id = "KAU TIDAK AKAN BISA MEMBUNGKUS KERNELKU, NULL-AGENT. SYNTHEX ABADI.",
                ph = "HINDI MO KAYANG SAKUPIN ANG KERNEL KO, NULL-AGENT. WALANG HANGGAN ANG SYNTHEX."
            ),
            snippetChips = listOf("return \"ROOT_UNLOCKED\"", "self.key = key", "print(core.shutdown())")
        )
    )


    /** Campaigns are derived from the curriculum source, never hard-coded in the UI. */
    val campaigns: List<ProtocolCampaign> = modules
        .groupBy { it.actCode }
        .map { (code, lessons) -> ProtocolCampaign(code, lessons.sortedBy { it.id }) }
        .sortedBy { it.firstModuleId }
}
