package com.neonull.protocol.runtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class CompanionPythonRunnerControllerTest {
    @Test
    fun gameBindsToSeparateUidRunnerAndReceivesPythonOutput() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val completed = CountDownLatch(1)
        val response = AtomicReference<String>()
        val controller = CompanionPythonRunnerController(context) {
            response.set(it)
            completed.countDown()
        }
        try {
            controller.execute("print('HOST_TO_RUNNER_OK')")
            assertTrue("Game could not get a response from the companion runner", completed.await(120, TimeUnit.SECONDS))
            val result = JSONObject(response.get() ?: error("Runner returned no result"))
            assertTrue(result.toString(), result.getBoolean("ok"))
            assertEquals("HOST_TO_RUNNER_OK\n", result.getString("stdout"))
        } finally {
            controller.close()
        }
    }
}
