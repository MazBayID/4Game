package com.neonull.protocol.runtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.ViewModelStore
import com.neonull.protocol.viewmodel.LearningViewModel
import com.neonull.protocol.assets.GameAssetCatalog
import com.neonull.protocol.data.CurriculumRepository
import com.neonull.protocol.data.LessonPhase
import com.neonull.protocol.data.LessonSequenceRepository
import com.neonull.protocol.data.ProgressionRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameRegressionInstrumentedTest {
    @Test
    fun loadsAssetManifestAndChecksPlannedEntries() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val catalog = GameAssetCatalog.load(context)
        assertTrue("Asset manifest should contain planned entries", catalog.all.size >= 50)
        assertNotNull(catalog.find("ic_dashboard"))
        assertNotNull(catalog.find("glitch_idle"))
        assertNotNull(catalog.find("bg_server_room"))
        assertEquals(256, catalog.find("glitch_idle")?.frameWidth)
        assertEquals(16, catalog.find("glitch_idle")?.frameCount)
    }


    @Test
    fun gameplaySubmissionUsesIsolatedRunnerAndAwardsMissionProgress() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("neo_null_progress", 0).edit().clear().commit()
        val viewModel = LearningViewModel(context.applicationContext as Application)
        val store = ViewModelStore().apply { put("learning", viewModel) }
        try {
            viewModel.openModuleTerminal(1)
            viewModel.updateCode("""agent = "Null-Agent"
clearance = 1
print(agent)
""")
            viewModel.runExploit()

            val deadline = SystemClock.elapsedRealtime() + 120_000L
            while (viewModel.uiState.value.isExecuting && SystemClock.elapsedRealtime() < deadline) {
                SystemClock.sleep(50)
            }

            val state = viewModel.uiState.value
            assertTrue("CPython execution did not finish", !state.isExecuting)
            assertTrue("Mission 1 was not completed: ${state.evaluationResult}", 1 in state.completedModuleIds)
            assertEquals(state.modules.first { it.id == 1 }.xpReward + 50, state.xp)
            assertEquals(128 + state.modules.first { it.id == 1 }.ramRewardMb, state.ramMb)
            assertTrue(state.evaluationResult?.outputText?.contains("Null-Agent") == true)
            assertTrue(
                "RAM Inspector must receive live CPython variables",
                state.evaluationResult?.ramSlots?.any { it.name == "agent" && it.type == "str" } == true
            )
        } finally {
            store.clear()
        }
    }

    @Test
    fun sandboxExecutesRealPythonAndPopulatesRamInspectorWithoutMissionRewards() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("neo_null_progress", 0).edit().clear().commit()
        val viewModel = LearningViewModel(context.applicationContext as Application)
        val store = ViewModelStore().apply { put("learning", viewModel) }
        try {
            viewModel.openSandbox()
            val xpBefore = viewModel.uiState.value.xp
            val ramBefore = viewModel.uiState.value.ramMb
            viewModel.updateCode("""name = "Zero"
numbers = [value * value for value in range(4)]
def double(value):
    return value * 2
print(name)
print(numbers)
print(double(6))
""")
            viewModel.runExploit()

            val deadline = SystemClock.elapsedRealtime() + 120_000L
            while (viewModel.uiState.value.isExecuting && SystemClock.elapsedRealtime() < deadline) {
                SystemClock.sleep(50)
            }

            val state = viewModel.uiState.value
            val result = state.evaluationResult
            assertTrue("Sandbox CPython execution did not finish", !state.isExecuting)
            assertTrue("Sandbox execution failed: $result", result?.isSuccess == true)
            assertTrue(result?.outputText?.contains("Zero") == true)
            assertTrue(result?.outputText?.contains("[0, 1, 4, 9]") == true)
            assertTrue(result?.outputText?.contains("12") == true)
            assertTrue(result?.ramSlots?.any { it.name == "numbers" && it.type == "list" } == true)
            assertEquals(xpBefore, state.xp)
            assertEquals(ramBefore, state.ramMb)
        } finally {
            store.clear()
        }
    }

    @Test
    fun progressionRequiresContiguousPrerequisites() {
        val modules = CurriculumRepository.modules
        assertEquals(1, ProgressionRules.nextUnlockedModuleId(modules, setOf(3)))
        assertEquals(3, ProgressionRules.nextUnlockedModuleId(modules, setOf(1, 2)))
        assertEquals(2, ProgressionRules.nextUnlockedModuleId(modules, setOf(1, 3)))
        assertTrue(ProgressionRules.isAccessible(3, modules, setOf(1, 2, 3)))
        assertEquals(false, ProgressionRules.isAccessible(4, modules, setOf(1, 3)))
        val campaigns = CurriculumRepository.campaigns
        assertEquals(modules.size, campaigns.sumOf { it.modules.size })
        assertTrue(campaigns.all { it.modules.isNotEmpty() })
        assertTrue(campaigns.zipWithNext().all { (left, right) ->
            left.lastModuleId < right.firstModuleId
        })
    }

    @Test
    fun everyMissionHasSixCompleteLearningPhases() {
        val modules = CurriculumRepository.modules
        val expected = LessonPhase.values().toList()
        val sequences = LessonSequenceRepository.allSteps(modules)
        assertEquals(modules.map { it.id }.toSet(), sequences.keys)
        modules.forEach { module ->
            val steps = sequences.getValue(module.id)
            assertEquals("Mission ${module.id} must have six phases", expected, steps.map { it.phase })
            assertTrue("Mission ${module.id} has a blank instruction", steps.all {
                it.instruction.en.isNotBlank() && it.instruction.id.isNotBlank()
            })
            assertTrue("Mission ${module.id} has a blank success criterion", steps.all {
                it.acceptanceCriteria.en.isNotBlank() && it.acceptanceCriteria.id.isNotBlank()
            })
            assertTrue("Mission ${module.id} has no observation example", steps[1].code.isNotBlank())
            assertTrue("Mission ${module.id} has no practice starter", steps[2].code.isNotBlank())
            assertTrue("Mission ${module.id} has no mastery starter", steps[5].code.isNotBlank())
        }
    }
}
