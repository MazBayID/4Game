package com.neonull.protocol.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LearningViewModelRunnerIntegrationTest {
    private data class Fixture(val viewModel: LearningViewModel, val store: ViewModelStore)

    private fun newFixture(): Fixture {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("neo_null_progress", 0).edit().clear().commit()
        val application = context.applicationContext as Application
        val store = ViewModelStore()
        val provider = ViewModelProvider(
            store,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )
        return Fixture(provider[LearningViewModel::class.java], store)
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
        val fixture = newFixture()
        val viewModel = fixture.viewModel
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
            fixture.store.clear()
        }
    }

    @Test
    fun sandboxExecutesPythonButDoesNotAwardMissionRewards() {
        val fixture = newFixture()
        val viewModel = fixture.viewModel
        try {
            viewModel.enterHub()
            viewModel.openSandbox()
            viewModel.updateCode("value = 6 * 7\\nprint(value)")
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
            fixture.store.clear()
        }
    }

    @Test
    fun failedMissionDoesNotAwardXpOrUnlockProgression() {
        val fixture = newFixture()
        val viewModel = fixture.viewModel
        try {
            viewModel.enterHub()
            viewModel.openModuleTerminal(1)
            viewModel.updateCode("agent = 'wrong'\\nclearance = 0\\nprint(agent)")
            viewModel.runExploit()

            val failed = awaitResult(viewModel)
            assertFalse("Incorrect mission must fail", failed.evaluationResult!!.isSuccess)
            assertEquals("Failed mission must not award XP", 0, failed.xp)
            assertFalse("Failed mission must not be marked complete", 1 in failed.completedModuleIds)
        } finally {
            fixture.store.clear()
        }
    }
}
