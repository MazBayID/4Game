package com.neonull.protocol.viewmodel

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LearningViewModelRunnerIntegrationTest {
    private fun newViewModel(): LearningViewModel {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("neo_null_progress", 0).edit().clear().commit()
        return LearningViewModel(context.applicationContext as Application)
    }

    private fun awaitResult(viewModel: LearningViewModel): LearningUiState {
        val deadline = System.currentTimeMillis() + 45_000L
        while (System.currentTimeMillis() < deadline) {
            val state = viewModel.uiState.value
            if (!state.isExecuting && state.evaluationResult != null) return state
            Thread.sleep(100)
        }
        throw AssertionError("Runner result did not reach LearningViewModel")
    }

    @Test
    fun missionPassUsesRunnerResultAndAwardsProgressionOnce() {
        val viewModel = newViewModel()
        try {
            viewModel.enterHub()
            viewModel.openModuleTerminal(1)
            viewModel.updateCode(
                """agent = "Null-Agent"
clearance = 1
print(agent)
"""
            )
            viewModel.runExploit()

            val passed = awaitResult(viewModel)
            assertTrue("Mission should pass through CPython", passed.evaluationResult!!.isSuccess)
            assertTrue("Mission 1 should be marked complete", 1 in passed.completedModuleIds)
            assertTrue("Mission reward should increase XP", passed.xp > 0)
            val rewardedXp = passed.xp

            viewModel.runExploit()
            val repeated = awaitResult(viewModel)
            assertEquals("A cleared mission must not award XP twice", rewardedXp, repeated.xp)
        } finally {
            viewModel.onCleared()
        }
    }

    @Test
    fun sandboxExecutesPythonButDoesNotAwardMissionRewards() {
        val viewModel = newViewModel()
        try {
            viewModel.enterHub()
            viewModel.openSandbox()
            viewModel.updateCode("value = 6 * 7\nprint(value)")
            viewModel.runExploit()

            val result = awaitResult(viewModel)
            assertTrue("Sandbox should execute real Python", result.evaluationResult!!.isSuccess)
            assertTrue(result.evaluationResult!!.outputText.contains("42"))
            assertTrue("Sandbox should expose Python variables", result.evaluationResult!!.ramSlots.any {
                it.name == "value" && it.preview == "42"
            })
            assertEquals("Sandbox must not award XP", 0, result.xp)
            assertTrue("Sandbox must not complete a campaign mission", result.completedModuleIds.isEmpty())
        } finally {
            viewModel.onCleared()
        }
    }

    @Test
    fun failedMissionDoesNotAwardXpOrUnlockProgression() {
        val viewModel = newViewModel()
        try {
            viewModel.enterHub()
            viewModel.openModuleTerminal(1)
            viewModel.updateCode("agent = 'wrong'\nclearance = 0\nprint(agent)")
            viewModel.runExploit()

            val failed = awaitResult(viewModel)
            assertFalse("Incorrect mission must fail", failed.evaluationResult!!.isSuccess)
            assertEquals("Failed mission must not award XP", 0, failed.xp)
            assertFalse("Failed mission must not be marked complete", 1 in failed.completedModuleIds)
        } finally {
            viewModel.onCleared()
        }
    }
}
