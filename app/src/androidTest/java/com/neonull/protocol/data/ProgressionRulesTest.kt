package com.neonull.protocol.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgressionRulesTest {
    private val modules = CurriculumRepository.modules

    @Test
    fun cannotAdvanceBeforeCurrentModuleIsCompleted() {
        assertNull(ProgressionRules.nextAccessibleModuleIdAfter(1, modules, emptySet()))
    }

    @Test
    fun completionUnlocksTheNextOrderedModule() {
        assertEquals(2, ProgressionRules.nextAccessibleModuleIdAfter(1, modules, setOf(1)))
    }

    @Test
    fun replayedCompletedModuleCanAdvanceToNextCompletedModule() {
        assertEquals(2, ProgressionRules.nextAccessibleModuleIdAfter(1, modules, setOf(1, 2, 3)))
    }

    @Test
    fun replayDoesNotSkipAnEarlierIncompletePrerequisite() {
        assertNull(ProgressionRules.nextAccessibleModuleIdAfter(3, modules, setOf(1, 3)))
    }

    @Test
    fun finalModuleHasNoNextModule() {
        assertNull(ProgressionRules.nextAccessibleModuleIdAfter(7, modules, (1..7).toSet()))
    }
}
