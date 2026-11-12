package com.neonull.protocol.viewmodel

import androidx.lifecycle.ViewModel
import com.neonull.protocol.data.CurriculumRepository
import com.neonull.protocol.data.ProtocolModule
import com.neonull.protocol.localization.AppLanguage
import com.neonull.protocol.localization.LanguageManager
import com.neonull.protocol.localization.LocalizedText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class NeoScreen {
    ONBOARDING,
    HUB,
    THEORY,
    TERMINAL
}

data class EvaluationResult(
    val isSuccess: Boolean,
    val isSyntaxError: Boolean,
    val outputText: String,
    val errorLines: Set<Int> = emptySet(),
    val glitchFeedback: String
)

data class LearningUiState(
    val currentScreen: NeoScreen = NeoScreen.ONBOARDING,
    val language: AppLanguage = AppLanguage.EN,
    val modules: List<ProtocolModule> = CurriculumRepository.modules,
    val activeModuleId: Int = 1,
    val completedModuleIds: Set<Int> = emptySet(),
    val xp: Int = 0,
    val ramMb: Int = 128,
    val codeBuffer: String = CurriculumRepository.modules.first().starterCode,
    val hintLevel: Int = 0,
    val glitchDialogue: String = CurriculumRepository.modules.first().glitchIntroDialogue.en,
    val isWarningState: Boolean = false,
    val evaluationResult: EvaluationResult? = null
) {
    val activeModule: ProtocolModule
        get() = modules.firstOrNull { it.id == activeModuleId } ?: modules.first()

    val isCampaignComplete: Boolean
        get() = completedModuleIds.size >= modules.size

    fun highestUnlockedModuleId(): Int {
        val maxCompleted = completedModuleIds.maxOrNull() ?: 0
        return (maxCompleted + 1).coerceAtMost(modules.size)
    }

    fun rankTitle(lang: AppLanguage): String {
        return when {
            completedModuleIds.size >= 7 -> LocalizedText(
                en = "Zero-Day Legend",
                id = "Legenda Zero-Day",
                ph = "Alamat ng Zero-Day"
            ).resolve(lang)
            completedModuleIds.size >= 5 -> LocalizedText(
                en = "Rootbreaker",
                id = "Pembobol Root",
                ph = "Tagawasak ng Root"
            ).resolve(lang)
            completedModuleIds.size >= 3 -> LocalizedText(
                en = "Cipher Runner",
                id = "Pelari Sandi",
                ph = "Tagapagbukas ng Cipher"
            ).resolve(lang)
            else -> LocalizedText(
                en = "Null-Agent Initiate",
                id = "Inisiat Null-Agent",
                ph = "Baguhang Null-Agent"
            ).resolve(lang)
        }
    }
}

class LearningViewModel(
    private val languageManager: LanguageManager = LanguageManager()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LearningUiState())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    fun getStrings(language: AppLanguage = _uiState.value.language) =
        languageManager.getUiStrings(language)

    fun selectLanguage(language: AppLanguage) {
        languageManager.setLanguage(language)
        _uiState.update { state ->
            val mod = state.activeModule
            val refreshedDialogue = when {
                state.evaluationResult?.isSuccess == true -> mod.glitchSuccessDialogue.resolve(language)
                state.hintLevel == 1 -> mod.solutionHint1.resolve(language)
                state.hintLevel >= 2 -> mod.solutionHint2.resolve(language)
                else -> mod.glitchIntroDialogue.resolve(language)
            }
            state.copy(
                language = language,
                glitchDialogue = refreshedDialogue
            )
        }
    }

    fun enterHub() {
        _uiState.update { it.copy(currentScreen = NeoScreen.HUB) }
    }

    fun backToOnboarding() {
        _uiState.update { it.copy(currentScreen = NeoScreen.ONBOARDING) }
    }

    fun openModuleTheory(moduleId: Int) {
        selectModule(moduleId, NeoScreen.THEORY)
    }

    fun openModuleTerminal(moduleId: Int) {
        selectModule(moduleId, NeoScreen.TERMINAL)
    }

    fun proceedFromTheoryToTerminal() {
        _uiState.update { it.copy(currentScreen = NeoScreen.TERMINAL) }
    }

    private fun selectModule(moduleId: Int, targetScreen: NeoScreen) {
        _uiState.update { state ->
            if (moduleId > state.highestUnlockedModuleId() && moduleId !in state.completedModuleIds) {
                return@update state
            }
            val module = state.modules.firstOrNull { it.id == moduleId } ?: return@update state
            state.copy(
                currentScreen = targetScreen,
                activeModuleId = module.id,
                codeBuffer = module.starterCode,
                hintLevel = 0,
                glitchDialogue = module.glitchIntroDialogue.resolve(state.language),
                isWarningState = module.isBossStage,
                evaluationResult = null
            )
        }
    }

    fun updateCode(newCode: String) {
        _uiState.update { it.copy(codeBuffer = newCode) }
    }

    fun insertSnippet(snippet: String) {
        _uiState.update { state ->
            val current = state.codeBuffer
            val updated = if (current.isBlank()) snippet else "$current\n$snippet"
            state.copy(codeBuffer = updated)
        }
    }

    fun resetCode() {
        _uiState.update { state ->
            val mod = state.activeModule
            state.copy(
                codeBuffer = mod.starterCode,
                hintLevel = 0,
                isWarningState = mod.isBossStage,
                evaluationResult = null,
                glitchDialogue = mod.glitchIntroDialogue.resolve(state.language)
            )
        }
    }

    fun requestHint() {
        _uiState.update { state ->
            val mod = state.activeModule
            val nextLevel = (state.hintLevel + 1).coerceAtMost(2)
            val hintText = if (nextLevel == 1) {
                mod.solutionHint1.resolve(state.language)
            } else {
                mod.solutionHint2.resolve(state.language)
            }
            state.copy(
                hintLevel = nextLevel,
                glitchDialogue = hintText
            )
        }
    }

    fun advanceToNextModule() {
        _uiState.update { state ->
            val nextId = (state.activeModuleId + 1).coerceAtMost(state.modules.size)
            val nextMod = state.modules.firstOrNull { it.id == nextId } ?: state.activeModule
            state.copy(
                activeModuleId = nextMod.id,
                currentScreen = NeoScreen.THEORY,
                codeBuffer = nextMod.starterCode,
                hintLevel = 0,
                isWarningState = nextMod.isBossStage,
                evaluationResult = null,
                glitchDialogue = nextMod.glitchIntroDialogue.resolve(state.language)
            )
        }
    }

    fun runExploit() {
        _uiState.update { state ->
            val module = state.activeModule
            val result = evaluatePythonSubmission(
                module = module,
                code = state.codeBuffer,
                language = state.language
            )
            val alreadyCleared = module.id in state.completedModuleIds
            val newCompleted = if (result.isSuccess) {
                state.completedModuleIds + module.id
            } else {
                state.completedModuleIds
            }
            val newXp = if (result.isSuccess && !alreadyCleared) {
                state.xp + module.xpReward
            } else {
                state.xp
            }
            val newRam = if (result.isSuccess && !alreadyCleared) {
                state.ramMb + module.ramRewardMb
            } else {
                state.ramMb
            }

            state.copy(
                completedModuleIds = newCompleted,
                xp = newXp,
                ramMb = newRam,
                evaluationResult = result,
                isWarningState = !result.isSuccess || module.isBossStage,
                glitchDialogue = result.glitchFeedback
            )
        }
    }

    /**
     * Offline Python structural + semantic evaluator tailored to each of the 7 modules.
     * Detects syntax errors (missing colons, unclosed strings, bracket mismatch, indentation)
     * and highlights faulty lines in Crimson Red.
     */
    private fun evaluatePythonSubmission(
        module: ProtocolModule,
        code: String,
        language: AppLanguage
    ): EvaluationResult {
        val strings = languageManager.getUiStrings(language)
        val syntaxIssue = detectPythonSyntaxIssue(code)
        if (syntaxIssue != null) {
            val (lineNum, reason) = syntaxIssue
            val glitchMsg = LocalizedText(
                en = "SYNTAX ALERT on line $lineNum: $reason. Crimson trace triggered—fix it before SYNTHEX locks this node!",
                id = "PERINGATAN SINTAKS di baris $lineNum: $reason. Pelacak merah aktif—perbaiki sebelum SYNTHEX mengunci node!",
                ph = "BABALA SA SYNTAX sa linya $lineNum: $reason. Umaalarma ang Crimson trace—ayusin agad!"
            ).resolve(language)

            return EvaluationResult(
                isSuccess = false,
                isSyntaxError = true,
                outputText = "${strings.syntaxErrorPrefix} [Line $lineNum]: $reason",
                errorLines = setOf(lineNum),
                glitchFeedback = glitchMsg
            )
        }

        val normalized = code.lines()
            .filterNot { it.trim().startsWith("#") }
            .joinToString("\n")

        return when (module.id) {
            1 -> evaluateModule1(normalized, language, module, strings.testFailedPrefix)
            2 -> evaluateModule2(normalized, language, module, strings.testFailedPrefix)
            3 -> evaluateModule3(normalized, language, module, strings.testFailedPrefix)
            4 -> evaluateModule4(normalized, language, module, strings.testFailedPrefix)
            5 -> evaluateModule5(normalized, language, module, strings.testFailedPrefix)
            6 -> evaluateModule6(normalized, language, module, strings.testFailedPrefix)
            7 -> evaluateModule7(normalized, language, module, strings.testFailedPrefix)
            else -> EvaluationResult(false, false, "Unknown module", emptySet(), "")
        }
    }

    private fun detectPythonSyntaxIssue(code: String): Pair<Int, String>? {
        val lines = code.lines()
        val blockHeaders = Regex("""^\s*(if|elif|else|for|while|def|class|try|except|finally|with)\b.*""")

        var parenDepth = 0
        var bracketDepth = 0
        var braceDepth = 0
        var expectIndent = false

        lines.forEachIndexed { idx, rawLine ->
            val lineNum = idx + 1
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEachIndexed

            // Strip comment outside quotes to inspect code portion
            val codeOnly = stripTrailingComment(rawLine)
            val trimmedCode = codeOnly.trim()

            // Check unclosed quotes on single line
            var inSingle = false
            var inDouble = false
            var escaped = false
            for (ch in codeOnly) {
                if (escaped) {
                    escaped = false
                    continue
                }
                if (ch == '\\') {
                    escaped = true
                    continue
                }
                if (ch == '\'' && !inDouble) inSingle = !inSingle
                else if (ch == '"' && !inSingle) inDouble = !inDouble
                else if (!inSingle && !inDouble) {
                    when (ch) {
                        '(' -> parenDepth++
                        ')' -> parenDepth--
                        '[' -> bracketDepth++
                        ']' -> bracketDepth--
                        '{' -> braceDepth++
                        '}' -> braceDepth--
                    }
                }
            }
            if (inSingle || inDouble) {
                return lineNum to "Unterminated string literal"
            }
            if (parenDepth < 0 || bracketDepth < 0 || braceDepth < 0) {
                return lineNum to "Unmatched closing delimiter"
            }

            if (expectIndent && !rawLine.startsWith("    ") && !rawLine.startsWith("\t")) {
                return lineNum to "Expected an indented block (4 spaces)"
            }
            expectIndent = false

            if (blockHeaders.matches(trimmedCode)) {
                if (!trimmedCode.endsWith(":")) {
                    return lineNum to "Missing ':' at end of block header"
                }
                expectIndent = true
            }
        }

        if (parenDepth != 0 || bracketDepth != 0 || braceDepth != 0) {
            return lines.size to "Unclosed parentheses or brackets"
        }
        if (expectIndent) {
            return lines.size to "Block header requires at least one indented line"
        }
        return null
    }

    private fun stripTrailingComment(line: String): String {
        var inSingle = false
        var inDouble = false
        var escaped = false
        for (i in line.indices) {
            val ch = line[i]
            if (escaped) {
                escaped = false
                continue
            }
            if (ch == '\\') {
                escaped = true
                continue
            }
            if (ch == '\'' && !inDouble) inSingle = !inSingle
            else if (ch == '"' && !inSingle) inDouble = !inDouble
            else if (ch == '#' && !inSingle && !inDouble) return line.substring(0, i)
        }
        return line
    }

    private fun evaluateModule1(
        code: String,
        lang: AppLanguage,
        module: ProtocolModule,
        failPrefix: String
    ): EvaluationResult {
        val hasAgent = Regex("""agent\s*=\s*["']Null-Agent["']""").containsMatchIn(code)
        val hasClearance = Regex("""clearance\s*=\s*1\b""").containsMatchIn(code)
        val hasPrintAgent = Regex("""print\s*\(\s*agent\s*\)""").containsMatchIn(code)

        return if (hasAgent && hasClearance && hasPrintAgent) {
            EvaluationResult(
                isSuccess = true,
                isSyntaxError = false,
                outputText = "Null-Agent\n[EXIT 0] Identity & clearance verified.",
                glitchFeedback = module.glitchSuccessDialogue.resolve(lang)
            )
        } else {
            val missing = buildList {
                if (!hasAgent) add("agent = \"Null-Agent\"")
                if (!hasClearance) add("clearance = 1")
                if (!hasPrintAgent) add("print(agent)")
            }.joinToString(", ")
            EvaluationResult(
                isSuccess = false,
                isSyntaxError = false,
                outputText = "$failPrefix: Missing or mismatched -> $missing",
                errorLines = setOf(2),
                glitchFeedback = module.solutionHint1.resolve(lang)
            )
        }
    }

    private fun evaluateModule2(
        code: String,
        lang: AppLanguage,
        module: ProtocolModule,
        failPrefix: String
    ): EvaluationResult {
        val hasThreat = Regex("""threat\s*=\s*\d+""").containsMatchIn(code)
        val hasIf = Regex("""if\s+threat\s*>=\s*50\s*:""").containsMatchIn(code)
        val hasDenied = Regex("""print\s*\(\s*["']DENIED["']\s*\)""").containsMatchIn(code)
        val hasElse = Regex("""\belse\s*:""").containsMatchIn(code)
        val hasAllow = Regex("""print\s*\(\s*["']ALLOW["']\s*\)""").containsMatchIn(code)

        val threatMatch = Regex("""threat\s*=\s*(\d+)""").find(code)
        val threatVal = threatMatch?.groupValues?.get(1)?.toIntOrNull() ?: -1

        return if (hasThreat && hasIf && hasDenied && hasElse && hasAllow && threatVal in 0..49) {
            EvaluationResult(
                isSuccess = true,
                isSyntaxError = false,
                outputText = "ALLOW\n[EXIT 0] Gatekeeper branch routed cleanly.",
                glitchFeedback = module.glitchSuccessDialogue.resolve(lang)
            )
        } else {
            EvaluationResult(
                isSuccess = false,
                isSyntaxError = false,
                outputText = "$failPrefix: Expected threat < 50 with if/else printing DENIED or ALLOW.",
                errorLines = setOf(4),
                glitchFeedback = module.solutionHint1.resolve(lang)
            )
        }
    }

    private fun evaluateModule3(
        code: String,
        lang: AppLanguage,
        module: ProtocolModule,
        failPrefix: String
    ): EvaluationResult {
        val hasList = Regex("""shards\s*=\s*\[\s*["']K1["']\s*,\s*["']K2["']\s*\]""").containsMatchIn(code)
        val hasAppend = Regex("""shards\.append\s*\(\s*["']K3["']\s*\)""").containsMatchIn(code)
        val hasVault = Regex("""vault\s*=\s*\{\s*["']code["']\s*:\s*77\s*\}""").containsMatchIn(code)
        val hasPrintLen = Regex("""print\s*\(\s*len\s*\(\s*shards\s*\)\s*\)""").containsMatchIn(code)

        return if (hasList && hasAppend && hasVault && hasPrintLen) {
            EvaluationResult(
                isSuccess = true,
                isSyntaxError = false,
                outputText = "3\n[EXIT 0] Shards list & Vault-4 dictionary verified.",
                glitchFeedback = module.glitchSuccessDialogue.resolve(lang)
            )
        } else {
            EvaluationResult(
                isSuccess = false,
                isSyntaxError = false,
                outputText = "$failPrefix: Ensure shards.append(\"K3\"), vault = {\"code\": 77}, and print(len(shards)).",
                errorLines = setOf(3),
                glitchFeedback = module.solutionHint1.resolve(lang)
            )
        }
    }

    private fun evaluateModule4(
        code: String,
        lang: AppLanguage,
        module: ProtocolModule,
        failPrefix: String
    ): EvaluationResult {
        val hasZero = Regex("""packets\s*=\s*0\b""").containsMatchIn(code)
        val hasForLoop = Regex("""for\s+\w+\s+in\s+range\s*\(\s*5\s*\)\s*:""").containsMatchIn(code)
        val hasIncrement = Regex("""packets\s*(\+=|=\s*packets\s*\+)\s*1\b""").containsMatchIn(code)
        val hasPrint = Regex("""print\s*\(\s*packets\s*\)""").containsMatchIn(code)

        return if (hasZero && hasForLoop && hasIncrement && hasPrint) {
            EvaluationResult(
                isSuccess = true,
                isSyntaxError = false,
                outputText = "5\n[EXIT 0] 5/5 relay ports flooded via for-loop.",
                glitchFeedback = module.glitchSuccessDialogue.resolve(lang)
            )
        } else {
            EvaluationResult(
                isSuccess = false,
                isSyntaxError = false,
                outputText = "$failPrefix: Use `for i in range(5):` with `packets += 1` and `print(packets)`.",
                errorLines = setOf(2),
                glitchFeedback = module.solutionHint2.resolve(lang)
            )
        }
    }

    private fun evaluateModule5(
        code: String,
        lang: AppLanguage,
        module: ProtocolModule,
        failPrefix: String
    ): EvaluationResult {
        val hasDef = Regex("""def\s+craft_token\s*\(\s*seed\s*\)\s*:""").containsMatchIn(code)
        val hasReturn = Regex("""return\s+seed\s*\*\s*3\s*\+\s*9""").containsMatchIn(code)
        val hasCall = Regex("""print\s*\(\s*craft_token\s*\(\s*7\s*\)\s*\)""").containsMatchIn(code)

        return if (hasDef && hasReturn && hasCall) {
            EvaluationResult(
                isSuccess = true,
                isSyntaxError = false,
                outputText = "30\n[EXIT 0] craft_token(7) returned 30.",
                glitchFeedback = module.glitchSuccessDialogue.resolve(lang)
            )
        } else {
            EvaluationResult(
                isSuccess = false,
                isSyntaxError = false,
                outputText = "$failPrefix: Define `craft_token(seed)` returning `seed * 3 + 9` and print `craft_token(7)`.",
                errorLines = setOf(2),
                glitchFeedback = module.solutionHint1.resolve(lang)
            )
        }
    }

    private fun evaluateModule6(
        code: String,
        lang: AppLanguage,
        module: ProtocolModule,
        failPrefix: String
    ): EvaluationResult {
        val hasTry = Regex("""\btry\s*:""").containsMatchIn(code)
        val hasOpen = Regex("""open\s*\(\s*["']corrupt\.log["']""").containsMatchIn(code)
        val hasExcept = Regex("""\bexcept\b.*:""").containsMatchIn(code)
        val hasSafeExtract = Regex("""print\s*\(\s*["']SAFE_EXTRACT["']\s*\)""").containsMatchIn(code)

        return if (hasTry && hasOpen && hasExcept && hasSafeExtract) {
            EvaluationResult(
                isSuccess = true,
                isSyntaxError = false,
                outputText = "SAFE_EXTRACT\n[EXIT 0] Trap exception caught cleanly without crash.",
                glitchFeedback = module.glitchSuccessDialogue.resolve(lang)
            )
        } else {
            EvaluationResult(
                isSuccess = false,
                isSyntaxError = false,
                outputText = "$failPrefix: Handle the trap with `except` and print `\"SAFE_EXTRACT\"`.",
                errorLines = setOf(4),
                glitchFeedback = module.solutionHint1.resolve(lang)
            )
        }
    }

    private fun evaluateModule7(
        code: String,
        lang: AppLanguage,
        module: ProtocolModule,
        failPrefix: String
    ): EvaluationResult {
        val hasClass = Regex("""class\s+OverlordOverride\b\s*:""").containsMatchIn(code)
        val hasInit = Regex("""def\s+__init__\s*\(\s*self\s*,\s*key\s*\)\s*:""").containsMatchIn(code)
        val hasSelfKey = Regex("""self\.key\s*=\s*key""").containsMatchIn(code)
        val hasShutdown = Regex("""def\s+shutdown\s*\(\s*self\s*\)\s*:""").containsMatchIn(code)
        val hasReturnRoot = Regex("""return\s+["']ROOT_UNLOCKED["']""").containsMatchIn(code)
        val hasInstance = Regex("""core\s*=\s*OverlordOverride\s*\(""").containsMatchIn(code)
        val hasPrintCall = Regex("""print\s*\(\s*core\.shutdown\s*\(\s*\)\s*\)""").containsMatchIn(code)

        return if (hasClass && hasInit && hasSelfKey && hasShutdown && hasReturnRoot && hasInstance && hasPrintCall) {
            EvaluationResult(
                isSuccess = true,
                isSyntaxError = false,
                outputText = "ROOT_UNLOCKED\n[EXIT 0] AI OVERLORD KERNEL OVERWRITTEN.",
                glitchFeedback = module.glitchSuccessDialogue.resolve(lang)
            )
        } else {
            EvaluationResult(
                isSuccess = false,
                isSyntaxError = false,
                outputText = "$failPrefix: OverlordOverride.shutdown() must return \"ROOT_UNLOCKED\" and be printed.",
                errorLines = setOf(6),
                glitchFeedback = module.solutionHint1.resolve(lang)
            )
        }
    }
}
