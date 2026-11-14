package com.neonull.protocol.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.neonull.protocol.data.CurriculumRepository
import com.neonull.protocol.data.LessonSequenceRepository
import com.neonull.protocol.data.LessonStep
import com.neonull.protocol.data.ProtocolModule
import com.neonull.protocol.data.ProgressionRules
import com.neonull.protocol.localization.AppLanguage
import com.neonull.protocol.localization.LanguageManager
import com.neonull.protocol.localization.LocalizedText
import com.neonull.protocol.runtime.CompanionPythonRunnerController
import org.json.JSONObject
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
    val evaluationResult: EvaluationResult? = null,
    val isExecuting: Boolean = false
) {
    val activeModule: ProtocolModule
        get() = modules.firstOrNull { it.id == activeModuleId } ?: modules.first()

    val activeLessonSteps: List<LessonStep>
        get() = LessonSequenceRepository.stepsFor(activeModule)

    val firewallHealth: Int
        get() = traceIntegrity

    val isCampaignComplete: Boolean
        get() = completedModuleIds.size >= modules.size

    fun highestUnlockedModuleId(): Int =
        ProgressionRules.nextUnlockedModuleId(modules, completedModuleIds)

    fun rankTitle(lang: AppLanguage): String {
        val totalLessons = modules.size.coerceAtLeast(1)
        val rootbreakerThreshold = (totalLessons * 7 + 9) / 10
        val cipherRunnerThreshold = (totalLessons * 4 + 9) / 10
        return when {
            completedModuleIds.size >= totalLessons -> LocalizedText(
                en = "Zero-Day Legend",
                id = "Legenda Zero-Day",
                ph = "Alamat ng Zero-Day"
            ).resolve(lang)
            completedModuleIds.size >= rootbreakerThreshold -> LocalizedText(
                en = "Rootbreaker",
                id = "Pembobol Root",
                ph = "Tagawasak ng Root"
            ).resolve(lang)
            completedModuleIds.size >= cipherRunnerThreshold -> LocalizedText(
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

    private data class PendingSubmission(val moduleId: Int?, val source: String, val language: AppLanguage, val sandbox: Boolean)

    private val languageManager = LanguageManager()
    private val progressPreferences = application.getSharedPreferences(
        "neo_null_progress",
        Context.MODE_PRIVATE
    )

    private val draftSaveJobs = mutableMapOf<String, Job>()

    private val _uiState = MutableStateFlow(restoreProgress())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()
    private var pendingSubmission: PendingSubmission? = null
    private val runnerController = CompanionPythonRunnerController(application.applicationContext) { handleRunnerResult(it) }

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
        val highestUnlockedId = ProgressionRules.nextUnlockedModuleId(
            CurriculumRepository.modules,
            completedIds
        )
        val storedActiveModuleId = progressPreferences.getInt("active_module_id", 1)
        val activeModuleId = storedActiveModuleId
            .takeIf { it in validModuleIds && (it <= highestUnlockedId || it in completedIds) }
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
        val state = _uiState.value
        if (state.isExecuting || state.currentScreen != NeoScreen.TERMINAL) return
        val request = PendingSubmission(
            if (state.isSandboxMode) null else state.activeModuleId,
            state.codeBuffer,
            state.language,
            state.isSandboxMode
        )
        pendingSubmission = request
        _uiState.update { it.copy(isExecuting = true, evaluationResult = null) }
        if (request.sandbox) runnerController.execute(request.source)
        else runnerController.grade(request.moduleId ?: return, request.source)
    }

    private fun handleRunnerResult(raw: String) {
        val request = pendingSubmission ?: return
        pendingSubmission = null
        val data = runCatching { JSONObject(raw) }.getOrElse {
            JSONObject().put("ok", false).put("passed", false)
                .put("errorType", "InvalidRunnerResponse").put("error", it.message ?: "Invalid runner response")
        }
        val state = _uiState.value
        if (state.isSandboxMode != request.sandbox || state.codeBuffer != request.source ||
            (!request.sandbox && state.activeModuleId != request.moduleId)) {
            _uiState.update { it.copy(isExecuting = false) }
            return
        }

        val module = state.activeModule
        val ok = data.optBoolean("ok", false)
        val success = if (request.sandbox) {
            ok && !data.optBoolean("stdoutTruncated") && !data.optBoolean("stderrTruncated")
        } else data.optBoolean("passed", false)
        val errorType = data.optString("errorType")
        val feedback = data.optString("feedback").takeIf { it.isNotBlank() && it != "null" }
        val stdout = data.optString("stdout")
        val stderr = data.optString("stderr")
        val traceback = data.optString("traceback").takeIf { it.isNotBlank() && it != "null" }
        val line = Regex("""(?:line |Line )(\d+)""")
            .find((feedback ?: "") + "\n" + (traceback ?: ""))?.groupValues?.get(1)?.toIntOrNull()
        val syntaxError = errorType == "SyntaxError"
        val dialogue = when {
            success && request.sandbox -> LocalizedText(
                en = "CPython execution completed in the isolated runner.",
                id = "Eksekusi CPython selesai di runner terisolasi.",
                ph = "Natapos ang CPython execution sa isolated runner."
            ).resolve(request.language)
            success -> module.glitchSuccessDialogue.resolve(request.language)
            request.sandbox -> LocalizedText(
                en = "Python execution failed. Check the console output.",
                id = "Eksekusi Python gagal. Periksa output konsol.",
                ph = "Nabigo ang Python execution. Tingnan ang console."
            ).resolve(request.language)
            syntaxError && line != null -> LocalizedText(
                en = "SYNTAX ALERT on line " + line + ". Check the Python error details.",
                id = "PERINGATAN SINTAKS di baris " + line + ". Periksa detail kesalahan Python.",
                ph = "BABALA SA SYNTAX sa linya " + line + ". Tingnan ang detalye ng Python error."
            ).resolve(request.language)
            else -> feedback ?: module.solutionHint1.resolve(request.language)
        }
        val output = buildString {
            append(stdout)
            if (stderr.isNotBlank()) append(if (isEmpty()) stderr else "\n" + stderr)
            if (!success) {
                val detail = feedback ?: data.optString("error").takeIf { it.isNotBlank() && it != "null" }
                if (!detail.isNullOrBlank()) append(if (isEmpty()) detail else "\n" + detail)
                if (!traceback.isNullOrBlank() && traceback != detail) {
                    append(if (isEmpty()) traceback else "\n" + traceback)
                }
            } else if (!request.sandbox) append(if (isEmpty()) "[EXIT 0]" else "\n[EXIT 0] Mission checks passed.")
            if (isEmpty()) append(if (success) "[EXIT 0]" else "Python execution failed.")
        }
        val ramSlots = runCatching {
            val slots = data.optJSONArray("ramSlots") ?: return@runCatching emptyList()
            (0 until slots.length()).mapNotNull { index ->
                val slot = slots.optJSONObject(index) ?: return@mapNotNull null
                RamVariableSlot(
                    name = slot.optString("name"),
                    type = slot.optString("type"),
                    preview = slot.optString("preview")
                )
            }
        }.getOrDefault(emptyList())
        val result = EvaluationResult(
            isSuccess = success,
            isSyntaxError = syntaxError,
            outputText = output,
            errorLines = if (syntaxError && line != null) setOf(line) else emptySet(),
            glitchFeedback = dialogue,
            ramSlots = ramSlots
        )
        if (request.sandbox) {
            _uiState.update { current ->
                if (!current.isSandboxMode || current.codeBuffer != request.source) current.copy(isExecuting = false)
                else current.copy(isExecuting = false, evaluationResult = result,
                    isWarningState = !success, glitchDialogue = dialogue)
            }
        } else {
            applyMissionResult(request.moduleId ?: return, request.source, result)
        }
    }

    private fun applyMissionResult(moduleId: Int, source: String, result: EvaluationResult) {
        _uiState.update { state ->
            if (state.isSandboxMode || state.activeModuleId != moduleId || state.codeBuffer != source) {
                return@update state.copy(isExecuting = false)
            }
            val module = state.modules.firstOrNull { it.id == moduleId }
                ?: return@update state.copy(isExecuting = false)
            val alreadyCleared = module.id in state.completedModuleIds
            val completed = if (result.isSuccess) state.completedModuleIds + module.id else state.completedModuleIds
            val cleanBonus = if (result.isSuccess && !alreadyCleared && !state.moduleAttemptFailed) 50 else 0
            val xp = if (result.isSuccess && !alreadyCleared) state.xp + module.xpReward + cleanBonus else state.xp
            val ram = if (result.isSuccess && !alreadyCleared) state.ramMb + module.ramRewardMb else state.ramMb
            val trace = if (result.isSuccess) (state.traceIntegrity + 10).coerceAtMost(100)
                else (state.traceIntegrity - 20).coerceAtLeast(20)
            state.copy(
                isExecuting = false,
                completedModuleIds = completed,
                xp = xp,
                ramMb = ram,
                traceIntegrity = trace,
                moduleAttemptFailed = state.moduleAttemptFailed || !result.isSuccess,
                evaluationResult = result,
                isWarningState = !result.isSuccess || module.isBossStage,
                glitchDialogue = result.glitchFeedback
            )
        }
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

    override fun onCleared() {
        pendingSubmission = null
        runnerController.close()
        super.onCleared()
    }
}
