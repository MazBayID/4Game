package com.neonull.protocol.localization

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Supported languages for NEO-NULL: PYTHON LEARNING PROTOCOL.
 * EN = English, ID = Bahasa Indonesia, PH = Filipino / Tagalog.
 */
enum class AppLanguage(val code: String, val displayName: String, val nativeSubtitle: String) {
    EN("EN", "English", "Global Resistance Channel"),
    ID("ID", "Bahasa Indonesia", "Saluran Perlawanan Nusantara"),
    PH("PH", "Filipino", "Linya ng Paglaban (Tagalog)")
}

data class LocalizedText(
    val en: String,
    val id: String,
    val ph: String
) {
    fun resolve(language: AppLanguage): String = when (language) {
        AppLanguage.EN -> en
        AppLanguage.ID -> id
        AppLanguage.PH -> ph
    }
}

data class UiStrings(
    val appTitle: String,
    val appSubtitle: String,
    val selectLanguagePrompt: String,
    val initiateSessionBtn: String,
    val operativeAliasLabel: String,
    val hubTitle: String,
    val sectorYearLabel: String,
    val ramStatLabel: String,
    val xpStatLabel: String,
    val firewallHealthLabel: String,
    val modulesProgressLabel: String,
    val rankLabel: String,
    val lockedLabel: String,
    val completedLabel: String,
    val activeLabel: String,
    val bossAlertLabel: String,
    val openTheoryBtn: String,
    val launchTerminalBtn: String,
    val backToHubBtn: String,
    val theoryBriefingTitle: String,
    val loreTransmissionTitle: String,
    val pythonConceptTitle: String,
    val codeExampleTitle: String,
    val proceedToTerminalBtn: String,
    val glitchMentorTitle: String,
    val overlordTauntTitle: String,
    val missionObjectiveTitle: String,
    val editorHeaderTitle: String,
    val consoleOutputTitle: String,
    val consoleTabLabel: String,
    val ramTabLabel: String,
    val ramInspectorTitle: String,
    val ramEmptyText: String,
    val runExploitBtn: String,
    val requestHintBtn: String,
    val resetCodeBtn: String,
    val nextModuleBtn: String,
    val sandboxModeBtn: String,
    val sandboxTitle: String,
    val sandboxSubtitle: String,
    val codexBtn: String,
    val codexTitle: String,
    val zeroTraceBonusLabel: String,
    val victoryTitle: String,
    val victorySubtitle: String,
    val warningBannerText: String,
    val noOutputPlaceholder: String,
    val syntaxErrorPrefix: String,
    val testFailedPrefix: String,
    val missionPassedStatus: String,
    val languageSwitchLabel: String
)

/**
 * Centralized reactive localization controller backed by StateFlow.
 */
class LanguageManager(initialLanguage: AppLanguage = AppLanguage.EN) {
    private val _currentLanguage = MutableStateFlow(initialLanguage)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
    }

    fun getUiStrings(language: AppLanguage = _currentLanguage.value): UiStrings {
        return when (language) {
            AppLanguage.EN -> UiStrings(
                appTitle = "NEO-NULL",
                appSubtitle = "PYTHON LEARNING PROTOCOL · YEAR 2142",
                selectLanguagePrompt = "Select encrypted neural interface language to begin infiltration:",
                initiateSessionBtn = "Enter Resistance Hub",
                operativeAliasLabel = "Operative: Null-Agent",
                hubTitle = "Resistance Curriculum Hub",
                sectorYearLabel = "SYNTHEX GRID · 2142",
                ramStatLabel = "RAM",
                xpStatLabel = "XP",
                firewallHealthLabel = "FIREWALL",
                modulesProgressLabel = "Exploits Cleared",
                rankLabel = "Clearance",
                lockedLabel = "ENCRYPTED",
                completedLabel = "BREACHED",
                activeLabel = "READY",
                bossAlertLabel = "OVERLORD BOSS STAGE",
                openTheoryBtn = "Read Briefing",
                launchTerminalBtn = "Open Terminal",
                backToHubBtn = "Hub",
                theoryBriefingTitle = "Tactical Briefing & Theory",
                loreTransmissionTitle = "Intercepted Lore Transmission",
                pythonConceptTitle = "Python Core Concept",
                codeExampleTitle = "Annotated Exploit Pattern",
                proceedToTerminalBtn = "Deploy to Terminal",
                glitchMentorTitle = "GLITCH · AI Mentor",
                overlordTauntTitle = "AI OVERLORD · Crimson Intercept",
                missionObjectiveTitle = "Mission Objective",
                editorHeaderTitle = "TERMINAL DECK · PYTHON",
                consoleOutputTitle = "Execution Console",
                consoleTabLabel = "CONSOLE",
                ramTabLabel = "RAM MEMORY",
                ramInspectorTitle = "Live RAM Variable Inspector",
                ramEmptyText = "Execute Python code to inspect live variables, lists, dicts, and class objects in memory.",
                runExploitBtn = "[ RUN EXPLOIT ]",
                requestHintBtn = "GLITCH HINT",
                resetCodeBtn = "RESET",
                nextModuleBtn = "NEXT MISSION",
                sandboxModeBtn = "Free-Play Sandbox",
                sandboxTitle = "Python Free-Play Sandbox",
                sandboxSubtitle = "Experiment freely with variables, loops, functions, and inspect live memory objects offline.",
                codexBtn = "Cipher Codex",
                codexTitle = "Unlocked Python Cipher Codex",
                zeroTraceBonusLabel = "+50 XP Clean Breach Bonus",
                victoryTitle = "AI OVERLORD NEUTRALIZED",
                victorySubtitle = "SYNTHEX core root access restored to humanity. Null-Agent protocol complete.",
                warningBannerText = "SYNTHEX COUNTER-TRACE DETECTED — CHECK SYNTAX",
                noOutputPlaceholder = "Run your Python payload to inspect execution output and security checks.",
                syntaxErrorPrefix = "SYNTAX_FAULT",
                testFailedPrefix = "CHECK_FAILED",
                missionPassedStatus = "BREACH CONFIRMED",
                languageSwitchLabel = "Language"
            )

            AppLanguage.ID -> UiStrings(
                appTitle = "NEO-NULL",
                appSubtitle = "PROTOKOL BELAJAR PYTHON · TAHUN 2142",
                selectLanguagePrompt = "Pilih bahasa antarmuka saraf terenkripsi untuk memulai infiltrasi:",
                initiateSessionBtn = "Masuk ke Markas",
                operativeAliasLabel = "Operatif: Null-Agent",
                hubTitle = "Pusat Kurikulum Perlawanan",
                sectorYearLabel = "JARINGAN SYNTHEX · 2142",
                ramStatLabel = "RAM",
                xpStatLabel = "XP",
                firewallHealthLabel = "FIREWALL",
                modulesProgressLabel = "Misi Tuntas",
                rankLabel = "Otoritas",
                lockedLabel = "TERKUNCI",
                completedLabel = "TEMBUS",
                activeLabel = "SIAP",
                bossAlertLabel = "TAHAP BOS OVERLORD",
                openTheoryBtn = "Baca Materi",
                launchTerminalBtn = "Buka Terminal",
                backToHubBtn = "Markas",
                theoryBriefingTitle = "Briefing Taktis & Teori",
                loreTransmissionTitle = "Transmisi Cerita Terenkripsi",
                pythonConceptTitle = "Konsep Inti Python",
                codeExampleTitle = "Pola Kode Eksploitasi",
                proceedToTerminalBtn = "Lanjut ke Terminal",
                glitchMentorTitle = "GLITCH · Mentor AI",
                overlordTauntTitle = "AI OVERLORD · Intersepsi Merah",
                missionObjectiveTitle = "Target Misi",
                editorHeaderTitle = "DEK TERMINAL · PYTHON",
                consoleOutputTitle = "Konsol Eksekusi",
                consoleTabLabel = "KONSOL",
                ramTabLabel = "MEMORI RAM",
                ramInspectorTitle = "Inspektor Variabel RAM Langsung",
                ramEmptyText = "Jalankan kode Python untuk memantau variabel, list, dict, dan objek di memori.",
                runExploitBtn = "[ JALANKAN EXPLOIT ]",
                requestHintBtn = "HINT GLITCH",
                resetCodeBtn = "RESET",
                nextModuleBtn = "MISI BERIKUTNYA",
                sandboxModeBtn = "Sandbox Bebas",
                sandboxTitle = "Sandbox Bebas Python",
                sandboxSubtitle = "Bereksperimen bebas dengan variabel, loop, fungsi, dan pantau objek memori secara offline.",
                codexBtn = "Kodeks Python",
                codexTitle = "Kodeks Referensi Python Terbuka",
                zeroTraceBonusLabel = "+50 XP Bonus Tanpa Jejak",
                victoryTitle = "AI OVERLORD DILUMPUHKAN",
                victorySubtitle = "Akses root inti SYNTHEX berhasil direbut kembali. Protokol Null-Agent selesai.",
                warningBannerText = "PELACAK BALIK SYNTHEX AKTIF — PERIKSA SINTAKS",
                noOutputPlaceholder = "Jalankan kode Python Anda untuk melihat hasil output dan verifikasi keamanan.",
                syntaxErrorPrefix = "GALAT_SINTAKS",
                testFailedPrefix = "VERIFIKASI_GAGAL",
                missionPassedStatus = "PENETRASI BERHASIL",
                languageSwitchLabel = "Bahasa"
            )

            AppLanguage.PH -> UiStrings(
                appTitle = "NEO-NULL",
                appSubtitle = "PROTOKOL SA PAG-AARAL NG PYTHON · TAONG 2142",
                selectLanguagePrompt = "Piliin ang wika ng naka-encrypt na interface upang simulan ang misyon:",
                initiateSessionBtn = "Pumasok sa Hub",
                operativeAliasLabel = "Operatiba: Null-Agent",
                hubTitle = "Sentro ng Kurikulum ng Paglaban",
                sectorYearLabel = "SYNTHEX GRID · 2142",
                ramStatLabel = "RAM",
                xpStatLabel = "XP",
                firewallHealthLabel = "FIREWALL",
                modulesProgressLabel = "Natapos na Misyon",
                rankLabel = "Antas",
                lockedLabel = "NAKA-LOCK",
                completedLabel = "NALUSOT",
                activeLabel = "HANDA",
                bossAlertLabel = "BOSS STAGE: OVERLORD",
                openTheoryBtn = "Basahin ang Gabay",
                launchTerminalBtn = "Buksan ang Terminal",
                backToHubBtn = "Hub",
                theoryBriefingTitle = "Taktikal na Gabay at Teorya",
                loreTransmissionTitle = "Nasagap na Transmisyon ng Kwento",
                pythonConceptTitle = "Pangunahing Konsepto ng Python",
                codeExampleTitle = "Halimbawang Code Pattern",
                proceedToTerminalBtn = "Tumuloy sa Terminal",
                glitchMentorTitle = "GLITCH · AI Mentor",
                overlordTauntTitle = "AI OVERLORD · Crimson Intercept",
                missionObjectiveTitle = "Layunin ng Misyon",
                editorHeaderTitle = "TERMINAL DECK · PYTHON",
                consoleOutputTitle = "Execution Console",
                consoleTabLabel = "CONSOLE",
                ramTabLabel = "RAM MEMORY",
                ramInspectorTitle = "Live RAM Variable Inspector",
                ramEmptyText = "Patakbuhin ang Python code upang makita ang live variables, lists, dicts, at objects.",
                runExploitBtn = "[ RUN EXPLOIT ]",
                requestHintBtn = "GLITCH HINT",
                resetCodeBtn = "RESET",
                nextModuleBtn = "SUSUNOD NA MISYON",
                sandboxModeBtn = "Free-Play Sandbox",
                sandboxTitle = "Python Free-Play Sandbox",
                sandboxSubtitle = "Mag-eksperimento nang malaya gamit ang Python at suriin ang mga memory object.",
                codexBtn = "Cipher Codex",
                codexTitle = "Nabuksang Python Cipher Codex",
                zeroTraceBonusLabel = "+50 XP Clean Breach Bonus",
                victoryTitle = "NAPATUMBA ANG AI OVERLORD",
                victorySubtitle = "Nabawi na ang root access mula sa SYNTHEX. Tagumpay ang Null-Agent protocol.",
                warningBannerText = "NA-DETECT ANG SYNTHEX COUNTER-TRACE — SURIIN ANG SYNTAX",
                noOutputPlaceholder = "Patakbuhin ang iyong Python code upang makita ang output at pagsusuri.",
                syntaxErrorPrefix = "MALI_SA_SYNTAX",
                testFailedPrefix = "BIGO_ANG_PAGSUSURI",
                missionPassedStatus = "MATAGUMPAY ANG PAGPASOK",
                languageSwitchLabel = "Wika"
            )
        }
    }
}
