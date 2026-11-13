package com.neonull.protocol.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.neonull.protocol.data.CurriculumRepository
import com.neonull.protocol.data.ProtocolModule
import com.neonull.protocol.localization.AppLanguage
import com.neonull.protocol.localization.LanguageManager
import com.neonull.protocol.localization.LocalizedText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NeoScreen {
    ONBOARDING,
    HUB,
    THEORY,
    TERMINAL
}

data class RamVariableSlot(
    val name: String,
    val type: String,
    val preview: String
)

data class EvaluationResult(
    val isSuccess: Boolean,
    val isSyntaxError: Boolean,
    val outputText: String,
    val errorLines: Set<Int> = emptySet(),
    val glitchFeedback: String,
    val ramSlots: List<RamVariableSlot> = emptyList()
)

data class LearningUiState(
    val currentScreen: NeoScreen = NeoScreen.ONBOARDING,
    val language: AppLanguage = AppLanguage.EN,
    val modules: List<ProtocolModule> = CurriculumRepository.modules,
    val activeModuleId: Int = 1,
    val isSandboxMode: Boolean = false,
    val codexOpen: Boolean = false,
    val audioEnabled: Boolean = true,
    val completedModuleIds: Set<Int> = emptySet(),
    val xp: Int = 0,
    val ramMb: Int = 128,
    val traceIntegrity: Int = 100,
    val moduleAttemptFailed: Boolean = false,
    val codeBuffer: String = CurriculumRepository.modules.first().starterCode,
    val hintLevel: Int = 0,
    val glitchDialogue: String = CurriculumRepository.modules.first().glitchIntroDialogue.en,
    val isWarningState: Boolean = false,
    val evaluationResult: EvaluationResult? = null
) {
    val activeModule: ProtocolModule
        get() = modules.firstOrNull { it.id == activeModuleId } ?: modules.first()

    val firewallHealth: Int
        get() = traceIntegrity

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
    application: Application
) : AndroidViewModel(application) {

    private val languageManager = LanguageManager()
    private val progressPreferences = application.getSharedPreferences(
        "neo_null_progress",
        Context.MODE_PRIVATE
    )

    private val draftSaveJobs = mutableMapOf<String, Job>()

    private val _uiState = MutableStateFlow(restoreProgress())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.drop(1).collect { state ->
                persistProgress(state)
            }
        }
    }

    private fun restoreProgress(): LearningUiState {
        val language = runCatching {
            AppLanguage.valueOf(progressPreferences.getString("language", AppLanguage.EN.name) ?: AppLanguage.EN.name)
        }.getOrDefault(AppLanguage.EN)
        val validModuleIds = CurriculumRepository.modules.map { it.id }.toSet()
        val completedIds = progressPreferences.getStringSet("completed_modules", emptySet())
            .orEmpty()
            .mapNotNull { it.toIntOrNull() }
            .filter { it in validModuleIds }
            .toSet()
        val highestUnlockedId = (completedIds.maxOrNull() ?: 0)
            .plus(1)
            .coerceAtMost(CurriculumRepository.modules.size)
        val storedActiveModuleId = progressPreferences.getInt("active_module_id", 1)
        val activeModuleId = storedActiveModuleId
            .takeIf { it in validModuleIds && it <= highestUnlockedId }
            ?: highestUnlockedId
        val hasEnteredHub = progressPreferences.getBoolean("has_entered_hub", false)

        return LearningUiState(
            currentScreen = if (hasEnteredHub) NeoScreen.HUB else NeoScreen.ONBOARDING,
            language = language,
            activeModuleId = activeModuleId,
            audioEnabled = progressPreferences.getBoolean("audio_enabled", true),
            completedModuleIds = completedIds,
            xp = progressPreferences.getInt("xp", 0).coerceAtLeast(0),
            ramMb = progressPreferences.getInt("ram_mb", 128).coerceIn(128, 4096),
            traceIntegrity = progressPreferences.getInt("trace_integrity", 100).coerceIn(20, 100),
            codeBuffer = loadDraft(activeModuleId)
                ?: CurriculumRepository.modules.first { it.id == activeModuleId }.starterCode
        )
    }

    private fun persistProgress(state: LearningUiState) {
        progressPreferences.edit()
            .putString("language", state.language.name)
            .putBoolean("audio_enabled", state.audioEnabled)
            .putBoolean("has_entered_hub", state.currentScreen != NeoScreen.ONBOARDING)
            .putInt("active_module_id", state.activeModuleId)
            .putStringSet("completed_modules", state.completedModuleIds.map { it.toString() }.toSet())
            .putInt("xp", state.xp.coerceAtLeast(0))
            .putInt("ram_mb", state.ramMb.coerceIn(128, 4096))
            .putInt("trace_integrity", state.traceIntegrity.coerceIn(20, 100))
            .apply()
    }

    private fun loadDraft(moduleId: Int, sandbox: Boolean = false): String? {
        val key = if (sandbox) "draft_sandbox" else "draft_module_$moduleId"
        return progressPreferences.getString(key, null)
            ?.takeIf { it.length <= MAX_DRAFT_LENGTH }
    }

    private fun scheduleDraftSave(code: String, moduleId: Int, sandbox: Boolean = false) {
        val key = if (sandbox) "draft_sandbox" else "draft_module_$moduleId"
        draftSaveJobs[key]?.cancel()
        draftSaveJobs[key] = viewModelScope.launch {
            delay(DRAFT_SAVE_DEBOUNCE_MS)
            progressPreferences.edit().putString(key, code.take(MAX_DRAFT_LENGTH)).apply()
            draftSaveJobs.remove(key)
        }
    }

    fun getStrings(language: AppLanguage = _uiState.value.language) =
        languageManager.getUiStrings(language)

    fun selectLanguage(language: AppLanguage) {
        languageManager.setLanguage(language)
        _uiState.update { state ->
            val mod = state.activeModule
            val refreshedDialogue = when {
                state.isSandboxMode -> languageManager.getUiStrings(language).sandboxSubtitle
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

    fun toggleAudio() {
        _uiState.update { it.copy(audioEnabled = !it.audioEnabled) }
    }

    fun openCodex() {
        _uiState.update { it.copy(codexOpen = true) }
    }

    fun closeCodex() {
        _uiState.update { it.copy(codexOpen = false) }
    }

    fun enterHub() {
        _uiState.update { it.copy(currentScreen = NeoScreen.HUB, isSandboxMode = false) }
    }

    fun openSandbox() {
        _uiState.update { state ->
            val strings = languageManager.getUiStrings(state.language)
            state.copy(
                currentScreen = NeoScreen.TERMINAL,
                isSandboxMode = true,
                codeBuffer = loadDraft(state.activeModuleId, sandbox = true) ?: SANDBOX_STARTER_CODE,
                hintLevel = 0,
                moduleAttemptFailed = false,
                isWarningState = false,
                evaluationResult = null,
                glitchDialogue = strings.sandboxSubtitle
            )
        }
    }

    fun openModuleTheory(moduleId: Int) {
        selectModule(moduleId, NeoScreen.THEORY)
    }

    fun openModuleTerminal(moduleId: Int) {
        selectModule(moduleId, NeoScreen.TERMINAL)
    }

    fun proceedFromTheoryToTerminal() {
        _uiState.update { it.copy(currentScreen = NeoScreen.TERMINAL, isSandboxMode = false) }
    }

    private fun selectModule(moduleId: Int, targetScreen: NeoScreen) {
        _uiState.update { state ->
            if (moduleId > state.highestUnlockedModuleId() && moduleId !in state.completedModuleIds) {
                return@update state
            }
            val module = state.modules.firstOrNull { it.id == moduleId } ?: return@update state
            state.copy(
                currentScreen = targetScreen,
                isSandboxMode = false,
                activeModuleId = module.id,
                codeBuffer = loadDraft(module.id) ?: module.starterCode,
                hintLevel = 0,
                moduleAttemptFailed = false,
                glitchDialogue = module.glitchIntroDialogue.resolve(state.language),
                isWarningState = module.isBossStage,
                evaluationResult = null
            )
        }
    }

    fun updateCode(newCode: String) {
        val state = _uiState.value
        scheduleDraftSave(newCode, state.activeModuleId, state.isSandboxMode)
        _uiState.update { it.copy(codeBuffer = newCode) }
    }

    fun insertSnippet(snippet: String) {
        _uiState.update { state ->
            val current = state.codeBuffer
            val updated = if (current.isBlank()) snippet else "$current\n$snippet"
            scheduleDraftSave(updated, state.activeModuleId, state.isSandboxMode)
            state.copy(codeBuffer = updated)
        }
    }

    fun resetCode() {
        _uiState.update { state ->
            if (state.isSandboxMode) {
                val strings = languageManager.getUiStrings(state.language)
                scheduleDraftSave(SANDBOX_STARTER_CODE, state.activeModuleId, sandbox = true)
                return@update state.copy(
                    codeBuffer = SANDBOX_STARTER_CODE,
                    hintLevel = 0,
                    isWarningState = false,
                    evaluationResult = null,
                    glitchDialogue = strings.sandboxSubtitle
                )
            }
            val mod = state.activeModule
            scheduleDraftSave(mod.starterCode, mod.id)
            state.copy(
                codeBuffer = mod.starterCode,
                hintLevel = 0,
                traceIntegrity = 100,
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
                codeBuffer = loadDraft(nextMod.id) ?: nextMod.starterCode,
                hintLevel = 0,
                moduleAttemptFailed = false,
                isWarningState = nextMod.isBossStage,
                evaluationResult = null,
                glitchDialogue = nextMod.glitchIntroDialogue.resolve(state.language)
            )
        }
    }

    fun runExploit() {
        _uiState.update { state ->
            if (state.isSandboxMode) {
                val sandboxRes = evaluateSandboxSubmission(state.codeBuffer, state.language)
                return@update state.copy(
                    evaluationResult = sandboxRes,
                    isWarningState = !sandboxRes.isSuccess,
                    glitchDialogue = sandboxRes.glitchFeedback
                )
            }

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
            val cleanBonus = if (result.isSuccess && !alreadyCleared && !state.moduleAttemptFailed) 50 else 0
            val newXp = if (result.isSuccess && !alreadyCleared) {
                state.xp + module.xpReward + cleanBonus
            } else {
                state.xp
            }
            val newRam = if (result.isSuccess && !alreadyCleared) {
                state.ramMb + module.ramRewardMb
            } else {
                state.ramMb
            }
            val nextTrace = if (result.isSuccess) {
                (state.traceIntegrity + 10).coerceAtMost(100)
            } else {
                (state.traceIntegrity - 20).coerceAtLeast(20)
            }

            state.copy(
                completedModuleIds = newCompleted,
                xp = newXp,
                ramMb = newRam,
                traceIntegrity = nextTrace,
                moduleAttemptFailed = state.moduleAttemptFailed || !result.isSuccess,
                evaluationResult = result,
                isWarningState = !result.isSuccess || module.isBossStage,
                glitchDialogue = result.glitchFeedback
            )
        }
    }

    private fun evaluateSandboxSubmission(
        code: String,
        language: AppLanguage
    ): EvaluationResult {
        val strings = languageManager.getUiStrings(language)
        val syntaxIssue = detectPythonSyntaxIssue(code)
        if (syntaxIssue != null) {
            val (lineNum, reason) = syntaxIssue
            val glitchMsg = LocalizedText(
                en = "SYNTAX ALERT on line $lineNum: $reason. Check indentation, quotes, or colons.",
                id = "PERINGATAN SINTAKS di baris $lineNum: $reason. Periksa indentasi, tanda kutip, atau titik dua.",
                ph = "BABALA SA SYNTAX sa linya $lineNum: $reason. Suriin ang indentation, quotes, o colon."
            ).resolve(language)
            return EvaluationResult(
                isSuccess = false,
                isSyntaxError = true,
                outputText = "${strings.syntaxErrorPrefix} [Line $lineNum]: $reason",
                errorLines = setOf(lineNum),
                glitchFeedback = glitchMsg
            )
        }

        val env = linkedMapOf<String, String>()
        val types = linkedMapOf<String, String>()
        val prints = mutableListOf<String>()
        val assignRegex = Regex("""^([A-Za-z_]\w*)\s*=\s*(.+)$""")
        val printRegex = Regex("""^print\s*\((.+)\)$""")
        val defRegex = Regex("""^def\s+([A-Za-z_]\w*)\s*\(([^)]*)\)\s*:$""")
        val classRegex = Regex("""^class\s+([A-Za-z_]\w*)\b.*:$""")

        code.lines().forEach { raw ->
            val cleaned = stripTrailingComment(raw).trim()
            if (cleaned.isEmpty()) return@forEach

            defRegex.matchEntire(cleaned)?.let { m ->
                val name = m.groupValues[1]
                val params = m.groupValues[2].trim()
                types[name] = "function"
                env[name] = "<fn $name($params)>"
                return@forEach
            }

            classRegex.matchEntire(cleaned)?.let { m ->
                val name = m.groupValues[1]
                types[name] = "class"
                env[name] = "<class '$name'>"
                return@forEach
            }

            assignRegex.matchEntire(cleaned)?.let { m ->
                val name = m.groupValues[1]
                val expr = m.groupValues[2].trim()
                val inferred = inferValuePreview(expr, env)
                types[name] = inferred.first
                env[name] = inferred.second
                return@forEach
            }

            printRegex.matchEntire(cleaned)?.let { m ->
                val inner = m.groupValues[1].trim()
                val rendered = resolvePrintExpression(inner, env)
                prints.add(rendered)
            }
        }

        val ramSlots = env.entries.take(12).map { (k, v) ->
            RamVariableSlot(name = k, type = types[k] ?: "obj", preview = v)
        }
        val stdout = if (prints.isNotEmpty()) {
            prints.joinToString("\n")
        } else {
            "[Sandbox] Syntax verified (${ramSlots.size} symbols allocated)."
        }

        val okFeedback = LocalizedText(
            en = "Free-Play Sandbox clean. Inspect allocated variables in your RAM Inspector below.",
            id = "Sandbox Bebas bersih. Periksa variabel aktif di panel RAM Inspector di bawah.",
            ph = "Malinis ang Free-Play Sandbox. Tingnan ang live variables sa RAM Inspector sa ibaba."
        ).resolve(language)

        return EvaluationResult(
            isSuccess = true,
            isSyntaxError = false,
            outputText = stdout,
            glitchFeedback = okFeedback,
            ramSlots = ramSlots
        )
    }

    private fun inferValuePreview(expr: String, env: Map<String, String>): Pair<String, String> {
        return when {
            (expr.startsWith("\"") && expr.endsWith("\"")) || (expr.startsWith("'") && expr.endsWith("'")) ->
                "str" to expr
            expr.startsWith("[") && expr.endsWith("]") -> "list" to expr
            expr.startsWith("{") && expr.endsWith("}") && expr.contains(":") -> "dict" to expr
            expr.startsWith("{") && expr.endsWith("}") -> "set" to expr
            expr == "True" || expr == "False" -> "bool" to expr
            expr.toIntOrNull() != null -> "int" to expr
            expr.toDoubleOrNull() != null -> "float" to expr
            env.containsKey(expr) -> "ref" to (env[expr] ?: expr)
            else -> "expr" to expr
        }
    }

    private fun resolvePrintExpression(expr: String, env: Map<String, String>): String {
        if ((expr.startsWith("\"") && expr.endsWith("\"")) || (expr.startsWith("'") && expr.endsWith("'"))) {
            return expr.substring(1, expr.length - 1)
        }
        if (env.containsKey(expr)) {
            val raw = env[expr] ?: expr
            return if ((raw.startsWith("\"") && raw.endsWith("\"")) || (raw.startsWith("'") && raw.endsWith("'"))) {
                raw.substring(1, raw.length - 1)
            } else {
                raw
            }
        }
        return expr
    }

    private fun hasIndentedStatementAfter(
        code: String,
        headerPattern: Regex,
        statementPattern: Regex
    ): Boolean {
        val lines = code.lines()
        for (headerIndex in lines.indices) {
            val header = stripTrailingComment(lines[headerIndex]).trim()
            if (!headerPattern.matches(header)) continue
            val headerIndent = lines[headerIndex].takeWhile { it == ' ' || it == '\t' }.length
            for (bodyIndex in headerIndex + 1 until lines.size) {
                val body = stripTrailingComment(lines[bodyIndex])
                if (body.isBlank()) continue
                val bodyIndent = body.takeWhile { it == ' ' || it == '\t' }.length
                if (bodyIndent <= headerIndent) break
                if (statementPattern.matches(body.trim())) return true
            }
        }
        return false
    }

    private fun hasMissionBlockStructure(moduleId: Int, code: String): Boolean {
        return when (moduleId) {
            2 -> hasIndentedStatementAfter(
                code, Regex("""if\s+threat\s*>=\s*50\s*:"""), Regex("""print\s*\(\s*["']DENIED["']\s*\)""")
            ) && hasIndentedStatementAfter(
                code, Regex("""else\s*:"""), Regex("""print\s*\(\s*["']ALLOW["']\s*\)""")
            )
            4 -> hasIndentedStatementAfter(
                code,
                Regex("""for\s+\w+\s+in\s+range\s*\(\s*5\s*\)\s*:"""),
                Regex("""packets\s*(?:\+=|=\s*packets\s*\+)\s*1\b""")
            )
            5 -> hasIndentedStatementAfter(
                code,
                Regex("""def\s+craft_token\s*\(\s*seed\s*\)\s*:"""),
                Regex("""return\s+seed\s*\*\s*3\s*\+\s*9\b""")
            )
            6 -> hasIndentedStatementAfter(
                code, Regex("""try\s*:"""), Regex("""(?:with\s+)?open\s*\(\s*["']corrupt\.log["']""")
            ) && hasIndentedStatementAfter(
                code, Regex("""except\b.*:"""), Regex("""print\s*\(\s*["']SAFE_EXTRACT["']\s*\)""")
            )
            7 -> hasIndentedStatementAfter(
                code, Regex("""class\s+OverlordOverride\s*:"""), Regex("""def\s+__init__\s*\(\s*self\s*,\s*key\s*\)\s*:""")
            ) && hasIndentedStatementAfter(
                code, Regex("""def\s+__init__\s*\(\s*self\s*,\s*key\s*\)\s*:"""), Regex("""self\.key\s*=\s*key""")
            ) && hasIndentedStatementAfter(
                code, Regex("""class\s+OverlordOverride\s*:"""), Regex("""def\s+shutdown\s*\(\s*self\s*\)\s*:""")
            ) && hasIndentedStatementAfter(
                code, Regex("""def\s+shutdown\s*\(\s*self\s*\)\s*:"""), Regex("""return\s*["']ROOT_UNLOCKED["']""")
            )
            else -> true
        }
    }

    private fun firstFailedMissionCheckpoint(moduleId: Int, code: String): Pair<String, Int>? {
        val checks: List<Pair<Regex, Pair<String, Int>>> = when (moduleId) {
            1 -> listOf(
                Regex("""agent\s*=\s*["']Null-Agent["']""") to ("Create agent = \"Null-Agent\"." to 1),
                Regex("""clearance\s*=\s*1\b""") to ("Set clearance to 1." to 2),
                Regex("""print\s*\(\s*agent\s*\)""") to ("Display the agent variable with print(agent)." to 3)
            )
            2 -> listOf(
                Regex("""threat\s*=\s*\d+""") to ("Assign a numeric value to threat." to 1),
                Regex("""if\s+threat\s*>=\s*50\s*:""") to ("Add the if threat >= 50: condition." to 2),
                Regex("""print\s*\(\s*["']DENIED["']\s*\)""") to ("Print DENIED inside the if branch." to 3),
                Regex("""\belse\s*:""") to ("Add an else: branch." to 4),
                Regex("""print\s*\(\s*["']ALLOW["']\s*\)""") to ("Print ALLOW inside the else branch." to 5)
            )
            3 -> listOf(
                Regex("""shards\s*=\s*\[\s*["']K1["']\s*,\s*["']K2["']\s*\]""") to ("Initialize shards with K1 and K2." to 1),
                Regex("""shards\.append\s*\(\s*["']K3["']\s*\)""") to ("Append K3 using shards.append(\"K3\")." to 2),
                Regex("""vault\s*=\s*\{\s*["']code["']\s*:\s*77\s*\}""") to ("Set vault code to 77." to 3),
                Regex("""print\s*\(\s*len\s*\(\s*shards\s*\)\s*\)""") to ("Print len(shards)." to 4)
            )
            4 -> listOf(
                Regex("""packets\s*=\s*0\b""") to ("Initialize packets = 0." to 1),
                Regex("""for\s+\w+\s+in\s+range\s*\(\s*5\s*\)\s*:""") to ("Create a for-loop using range(5)." to 2),
                Regex("""packets\s*(?:\+=|=\s*packets\s*\+)\s*1\b""") to ("Increase packets by one inside the loop." to 3),
                Regex("""print\s*\(\s*packets\s*\)""") to ("Print the final packets value." to 4)
            )
            5 -> listOf(
                Regex("""def\s+craft_token\s*\(\s*seed\s*\)\s*:""") to ("Define craft_token(seed)." to 1),
                Regex("""return\s+seed\s*\*\s*3\s*\+\s*9""") to ("Return seed * 3 + 9." to 2),
                Regex("""print\s*\(\s*craft_token\s*\(\s*7\s*\)\s*\)""") to ("Call and print craft_token(7)." to 3)
            )
            6 -> listOf(
                Regex("""\btry\s*:""") to ("Start exception handling with try:." to 1),
                Regex("""open\s*\(\s*["']corrupt\.log["']""") to ("Open corrupt.log inside try." to 2),
                Regex("""\bexcept\b.*:""") to ("Add an except handler." to 4),
                Regex("""print\s*\(\s*["']SAFE_EXTRACT["']\s*\)""") to ("Print SAFE_EXTRACT in the handler." to 5)
            )
            7 -> listOf(
                Regex("""class\s+OverlordOverride\b\s*:""") to ("Declare the OverlordOverride class." to 1),
                Regex("""def\s+__init__\s*\(\s*self\s*,\s*key\s*\)\s*:""") to ("Create __init__(self, key)." to 2),
                Regex("""self\.key\s*=\s*key""") to ("Store the key with self.key = key." to 3),
                Regex("""def\s+shutdown\s*\(\s*self\s*\)\s*:""") to ("Define shutdown(self)." to 4),
                Regex("""return\s*["']ROOT_UNLOCKED["']""") to ("Return ROOT_UNLOCKED." to 5),
                Regex("""core\s*=\s*OverlordOverride\s*\(""") to ("Create an OverlordOverride instance named core." to 6),
                Regex("""print\s*\(\s*core\.shutdown\s*\(\s*\)\s*\)""") to ("Print core.shutdown()." to 7)
            )
            else -> return "Unknown mission." to 1
        }
        if (moduleId == 2) {
            val threat = Regex("""threat\s*=\s*(\d+)""").find(code)?.groupValues?.get(1)?.toIntOrNull()
            if (threat != null && threat !in 0..49) {
                return "Set threat below 50 so the ALLOW branch can be tested." to 1
            }
        }
        return checks.firstOrNull { (pattern, _) -> !pattern.containsMatchIn(code) }?.second
    }

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
            .joinToString("\n") { stripTrailingComment(it) }

        val result = when (module.id) {
            1 -> evaluateModule1(normalized, language, module, strings.testFailedPrefix)
            2 -> evaluateModule2(normalized, language, module, strings.testFailedPrefix)
            3 -> evaluateModule3(normalized, language, module, strings.testFailedPrefix)
            4 -> evaluateModule4(normalized, language, module, strings.testFailedPrefix)
            5 -> evaluateModule5(normalized, language, module, strings.testFailedPrefix)
            6 -> evaluateModule6(normalized, language, module, strings.testFailedPrefix)
            7 -> evaluateModule7(normalized, language, module, strings.testFailedPrefix)
            else -> EvaluationResult(false, false, "Unknown module", emptySet(), "")
        }
        if (result.isSuccess || result.isSyntaxError) return result
        val checkpoint = firstFailedMissionCheckpoint(module.id, normalized) ?: return result
        return result.copy(
            outputText = "${strings.testFailedPrefix}: ${checkpoint.first}",
            errorLines = setOf(checkpoint.second)
        )
    }

    companion object {
        private const val MAX_DRAFT_LENGTH = 32_768
        private const val DRAFT_SAVE_DEBOUNCE_MS = 400L
        val SANDBOX_STARTER_CODE = """
            # NEO-NULL Python Free-Play Sandbox
            agent = "Null-Agent"
            shards = ["K1", "K2", "K3"]
            vault = {"sector": 9, "active": True}

            def boost(mhz):
                return mhz * 2

            print(agent)
            print(shards)
        """.trimIndent()
    }

    private fun detectPythonSyntaxIssue(code: String): Pair<Int, String>? {
        val lines = code.lines()
        val blockHeaders = Regex("""^\s*(if|elif|else|for|while|def|class|try|except|finally|with)\b.*""")

        var parenDepth = 0
        var bracketDepth = 0
        var braceDepth = 0
        var expectedIndentLevel: Int? = null

        lines.forEachIndexed { idx, rawLine ->
            val lineNum = idx + 1
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEachIndexed

            val codeOnly = stripTrailingComment(rawLine)
            val trimmedCode = codeOnly.trim()

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
            if (inSingle || inDouble) return lineNum to "Unterminated string literal"
            if (parenDepth < 0 || bracketDepth < 0 || braceDepth < 0) {
                return lineNum to "Unmatched closing delimiter"
            }
            val indentLevel = rawLine.takeWhile { it == ' ' || it == '\t' }.length
            if (expectedIndentLevel != null && indentLevel <= expectedIndentLevel) {
                return lineNum to "Expected an indented block"
            }
            expectedIndentLevel = null

            if (blockHeaders.matches(trimmedCode)) {
                if (!trimmedCode.endsWith(":")) return lineNum to "Missing ':' at end of block header"
                expectedIndentLevel = rawLine.takeWhile { it == ' ' || it == '\t' }.length
            }
        }

        if (parenDepth != 0 || bracketDepth != 0 || braceDepth != 0) {
            return lines.size to "Unclosed parentheses or brackets"
        }
        if (expectedIndentLevel != null) return lines.size to "Block header requires at least one indented line"
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
                glitchFeedback = module.glitchSuccessDialogue.resolve(lang),
                ramSlots = listOf(
                    RamVariableSlot("agent", "str", "'Null-Agent'"),
                    RamVariableSlot("clearance", "int", "1")
                )
            )
        } else {
            EvaluationResult(
                isSuccess = false,
                isSyntaxError = false,
                outputText = "$failPrefix: Expected agent = \"Null-Agent\", clearance = 1, and print(agent).",
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
        val threatMatch = Regex("""threat\s*=\s*(\d+)""").find(code)
        val threatVal = threatMatch?.groupValues?.get(1)?.toIntOrNull() ?: -1
        val ok = threatVal in 0..49 &&
            Regex("""if\s+threat\s*>=\s*50\s*:""").containsMatchIn(code) &&
            Regex("""print\s*\(\s*["']DENIED["']\s*\)""").containsMatchIn(code) &&
            Regex("""\belse\s*:""").containsMatchIn(code) &&
            Regex("""print\s*\(\s*["']ALLOW["']\s*\)""").containsMatchIn(code)

        val accepted = ok && hasMissionBlockStructure(2, code)

        return if (accepted) {
            EvaluationResult(
                isSuccess = true,
                isSyntaxError = false,
                outputText = "ALLOW\n[EXIT 0] Gatekeeper branch routed cleanly.",
                glitchFeedback = module.glitchSuccessDialogue.resolve(lang),
                ramSlots = listOf(RamVariableSlot("threat", "int", threatVal.toString()))
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
        val ok = Regex("""shards\s*=\s*\[\s*["']K1["']\s*,\s*["']K2["']\s*\]""").containsMatchIn(code) &&
            Regex("""shards\.append\s*\(\s*["']K3["']\s*\)""").containsMatchIn(code) &&
            Regex("""vault\s*=\s*\{\s*["']code["']\s*:\s*77\s*\}""").containsMatchIn(code) &&
            Regex("""print\s*\(\s*len\s*\(\s*shards\s*\)\s*\)""").containsMatchIn(code)

        return if (ok) {
            EvaluationResult(
                isSuccess = true,
                isSyntaxError = false,
                outputText = "3\n[EXIT 0] Shards list & Vault-4 dictionary verified.",
                glitchFeedback = module.glitchSuccessDialogue.resolve(lang),
                ramSlots = listOf(
                    RamVariableSlot("shards", "list", "['K1', 'K2', 'K3']"),
                    RamVariableSlot("vault", "dict", "{'code': 77}")
                )
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
        val ok = Regex("""packets\s*=\s*0\b""").containsMatchIn(code) &&
            Regex("""for\s+\w+\s+in\s+range\s*\(\s*5\s*\)\s*:""").containsMatchIn(code) &&
            Regex("""packets\s*(\+=|=\s*packets\s*\+)\s*1\b""").containsMatchIn(code) &&
            Regex("""print\s*\(\s*packets\s*\)""").containsMatchIn(code)

        val structurallyValid = hasMissionBlockStructure(4, code)
        val accepted = ok && structurallyValid

        return if (accepted) {
            EvaluationResult(
                isSuccess = true,
                isSyntaxError = false,
                outputText = "5\n[EXIT 0] 5/5 relay ports flooded via for-loop.",
                glitchFeedback = module.glitchSuccessDialogue.resolve(lang),
                ramSlots = listOf(RamVariableSlot("packets", "int", "5"))
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
        val ok = Regex("""def\s+craft_token\s*\(\s*seed\s*\)\s*:""").containsMatchIn(code) &&
            Regex("""return\s+seed\s*\*\s*3\s*\+\s*9""").containsMatchIn(code) &&
            Regex("""print\s*\(\s*craft_token\s*\(\s*7\s*\)\s*\)""").containsMatchIn(code)

        val structurallyValid = hasMissionBlockStructure(5, code)
        val accepted = ok && structurallyValid

        return if (accepted) {
            EvaluationResult(
                isSuccess = true,
                isSyntaxError = false,
                outputText = "30\n[EXIT 0] craft_token(7) returned 30.",
                glitchFeedback = module.glitchSuccessDialogue.resolve(lang),
                ramSlots = listOf(RamVariableSlot("craft_token", "function", "<fn craft_token(seed)>"))
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
        val ok = Regex("""\btry\s*:""").containsMatchIn(code) &&
            Regex("""open\s*\(\s*["']corrupt\.log["']""").containsMatchIn(code) &&
            Regex("""\bexcept\b.*:""").containsMatchIn(code) &&
            Regex("""print\s*\(\s*["']SAFE_EXTRACT["']\s*\)""").containsMatchIn(code)

        val structurallyValid = hasMissionBlockStructure(6, code)
        val accepted = ok && structurallyValid

        return if (accepted) {
            EvaluationResult(
                isSuccess = true,
                isSyntaxError = false,
                outputText = "SAFE_EXTRACT\n[EXIT 0] Trap exception caught cleanly without crash.",
                glitchFeedback = module.glitchSuccessDialogue.resolve(lang),
                ramSlots = listOf(RamVariableSlot("exception_guard", "handler", "FileNotFoundError -> SAFE_EXTRACT"))
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
        val ok = Regex("""class\s+OverlordOverride\b\s*:""").containsMatchIn(code) &&
            Regex("""def\s+__init__\s*\(\s*self\s*,\s*key\s*\)\s*:""").containsMatchIn(code) &&
            Regex("""self\.key\s*=\s*key""").containsMatchIn(code) &&
            Regex("""def\s+shutdown\s*\(\s*self\s*\)\s*:""").containsMatchIn(code) &&
            Regex("""return\s+["']ROOT_UNLOCKED["']""").containsMatchIn(code) &&
            Regex("""core\s*=\s*OverlordOverride\s*\(""").containsMatchIn(code) &&
            Regex("""print\s*\(\s*core\.shutdown\s*\(\s*\)\s*\)""").containsMatchIn(code)

        val structurallyValid = hasMissionBlockStructure(7, code)
        val accepted = ok && structurallyValid

        return if (accepted) {
            EvaluationResult(
                isSuccess = true,
                isSyntaxError = false,
                outputText = "ROOT_UNLOCKED\n[EXIT 0] AI OVERLORD KERNEL OVERWRITTEN.",
                glitchFeedback = module.glitchSuccessDialogue.resolve(lang),
                ramSlots = listOf(
                    RamVariableSlot("OverlordOverride", "class", "shutdown() -> 'ROOT_UNLOCKED'"),
                    RamVariableSlot("core", "instance:OverlordOverride", "key='NULL-2142'")
                )
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
