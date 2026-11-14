package com.neonull.protocol.runtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class CompanionPythonRunnerControllerTest {
    @Test
    fun gameBindsToEmbeddedRunnerProcessAndReceivesPythonOutput() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val completed = CountDownLatch(1)
        val response = AtomicReference<String>()
        val controller = CompanionPythonRunnerController(context) {
            response.set(it)
            completed.countDown()
        }
        try {
            controller.execute("agent = \"Null-Agent\"\ncount = 3\nprint(agent)")
            assertTrue("Game could not get a response from the embedded runner", completed.await(120, TimeUnit.SECONDS))
            val result = JSONObject(response.get() ?: error("Runner returned no result"))
            assertTrue(result.toString(), result.getBoolean("ok"))
            assertEquals("Null-Agent\n", result.getString("stdout"))
            val slots = result.getJSONArray("ramSlots")
            assertTrue((0 until slots.length()).any { slots.getJSONObject(it).optString("name") == "agent" })
        } finally {
            controller.close()
        }
    }

    @Test
    fun gameGradesMissionThroughTheEmbeddedRunner() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val completed = CountDownLatch(1)
        val response = AtomicReference<String>()
        val controller = CompanionPythonRunnerController(context) {
            response.set(it)
            completed.countDown()
        }
        try {
            controller.grade(1, \"\"\"agent = "Null-Agent"
clearance = 1
print(agent)
\"\"\")
            assertTrue("Mission grading did not return", completed.await(120, TimeUnit.SECONDS))
            val result = JSONObject(response.get() ?: error("Runner returned no grade"))
            assertTrue(result.toString(), result.getBoolean("ok"))
            assertTrue(result.toString(), result.getBoolean("passed"))
            assertEquals("Null-Agent\n", result.getString("stdout"))
        } finally {
            controller.close()
        }
    }

    @Test
    fun controllerRecoversAfterWatchdogRestartsRunnerProcess() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val responses = LinkedBlockingQueue<String>()
        val controller = CompanionPythonRunnerController(context) { responses.offer(it) }
        try {
            controller.execute("while True: pass")
            val timeout = JSONObject(responses.poll(45, TimeUnit.SECONDS) ?: error("Runner watchdog did not report process death"))
            assertEquals("RunnerDisconnected", timeout.optString("errorType"))

            controller.execute("print('CONTROLLER_RECOVERED')")
            val recovered = JSONObject(responses.poll(90, TimeUnit.SECONDS) ?: error("Controller did not recover after runner restart"))
            assertTrue(recovered.toString(), recovered.getBoolean("ok"))
            assertEquals("CONTROLLER_RECOVERED\n", recovered.getString("stdout"))
        } finally {
            controller.close()
        }
    }

    @Test
    fun runnerServiceIsPackagedInsideTheHostApplication() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val service = context.packageManager.getServiceInfo(
            android.content.ComponentName(context, com.neonull.protocol.runner.PythonRunnerService::class.java),
            0
        )
        assertEquals(context.packageName, service.applicationInfo.packageName)
        assertEquals(context.packageName + ":execution", service.processName)
        assertFalse("Embedded Runner must not be exported", service.exported)
    }
}
