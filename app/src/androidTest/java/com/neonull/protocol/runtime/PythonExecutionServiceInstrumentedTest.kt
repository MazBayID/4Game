package com.neonull.protocol.runtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
class PythonExecutionServiceInstrumentedTest {
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
