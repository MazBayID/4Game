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
    val missionObjectiveTitle: String,
    val editorHeaderTitle: String,
    val consoleOutputTitle: String,
    val runExploitBtn: String,
    val requestHintBtn: String,
    val resetCodeBtn: String,
    val nextModuleBtn: String,
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
                ramStatLabel = "RAM Freed",
                xpStatLabel = "Operative XP",
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
                glitchMentorTitle = "GLITCH // AI Mentor",
                missionObjectiveTitle = "Mission Objective",
                editorHeaderTitle = "Python Payload Editor",
                consoleOutputTitle = "Execution Console",
                runExploitBtn = "Run Exploit",
                requestHintBtn = "Ask GLITCH Hint",
                resetCodeBtn = "Reset Code",
                nextModuleBtn = "Next Mission",
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
                ramStatLabel = "RAM Bebas",
                xpStatLabel = "XP Operatif",
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
                glitchMentorTitle = "GLITCH // Mentor AI",
                missionObjectiveTitle = "Target Misi",
                editorHeaderTitle = "Editor Kode Python",
                consoleOutputTitle = "Konsol Eksekusi",
                runExploitBtn = "Jalankan Kode",
                requestHintBtn = "Minta Petunjuk GLITCH",
                resetCodeBtn = "Reset Kode",
                nextModuleBtn = "Misi Berikutnya",
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
                ramStatLabel = "Nababawing RAM",
                xpStatLabel = "XP ng Operatiba",
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
                glitchMentorTitle = "GLITCH // AI Mentor",
                missionObjectiveTitle = "Layunin ng Misyon",
                editorHeaderTitle = "Python Payload Editor",
                consoleOutputTitle = "Execution Console",
                runExploitBtn = "Patakbuhin ang Code",
                requestHintBtn = "Humingi ng Hint kay GLITCH",
                resetCodeBtn = "I-reset ang Code",
                nextModuleBtn = "Susunod na Misyon",
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
