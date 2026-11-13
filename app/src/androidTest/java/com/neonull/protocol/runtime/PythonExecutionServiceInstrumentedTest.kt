package com.neonull.protocol.runtime

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Process
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.neonull.protocol.assets.GameAssetCatalog
import com.neonull.protocol.data.CurriculumRepository
import com.neonull.protocol.data.ProgressionRules
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class PythonExecutionServiceInstrumentedTest {

    @Test
    fun test01BinderPingReturnsWithoutStartingPython() {
        assertEquals("BINDER_OK", sendMessageThroughService(
            PythonExecutionService.MSG_PING, null, PythonExecutionService.MSG_PING_RESULT
        ))
    }

    @Test
    fun test03ExecutesPythonAndReturnsStdoutOverBinder() {
        val result = JSONObject(executeThroughService("print('BYTEGHOST_RUNTIME_OK')"))
        assertTrue("Python execution should succeed: $result", result.getBoolean("ok"))
        assertEquals("BYTEGHOST_RUNTIME_OK\n", result.getString("stdout"))
        assertEquals("", result.getString("stderr"))
    }

    @Test
    fun test04ReturnsPythonExceptionAndKeepsServiceAlive() {
        val result = JSONObject(executeThroughService("raise ValueError('BYTEGHOST_EXPECTED_ERROR')"))
        assertEquals(false, result.getBoolean("ok"))
        assertEquals("ValueError", result.getString("errorType"))
        assertTrue(result.getString("traceback").contains("BYTEGHOST_EXPECTED_ERROR"))
        val followUp = JSONObject(executeThroughService("print('SERVICE_STILL_ALIVE')"))
        assertTrue("Service should accept a request after an exception", followUp.getBoolean("ok"))
        assertEquals("SERVICE_STILL_ALIVE\n", followUp.getString("stdout"))
    }

    @Test
    fun test05CapsLargeOutputAndKeepsServiceResponsive() {
        val largeOutput = JSONObject(executeThroughService("print('X' * 70000)"))
        assertTrue("Large output execution should complete", largeOutput.getBoolean("ok"))
        assertEquals(64 * 1024, largeOutput.getString("stdout").length)
        assertTrue(largeOutput.getBoolean("stdoutTruncated"))
        val followUp = JSONObject(executeThroughService("print('AFTER_STRESS_OK')"))
        assertTrue(followUp.getBoolean("ok"))
        assertEquals("AFTER_STRESS_OK\n", followUp.getString("stdout"))
    }

    @Test
    fun test02RejectsOversizedSourceBeforePythonStartup() {
        val oversized = JSONObject(executeThroughService(" ".repeat(128 * 1024 + 1)))
        assertEquals(false, oversized.getBoolean("ok"))
        assertEquals("SourceTooLarge", oversized.getString("errorType"))
    }

    @Test
    fun test06LoadsAssetManifestAndChecksPlannedEntries() {
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
    fun test08PythonInputUsesProvidedStdin() {
        val result = JSONObject(executeThroughService("name = input('Alias? ')\nprint('Hello', name)", "Zero\n"))
        assertTrue(result.getBoolean("ok"))
        assertEquals("Alias? Hello Zero\n", result.getString("stdout"))
    }

    @Test
    fun test07ProgressionRequiresContiguousPrerequisites() {
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

    private fun executeThroughService(source: String, stdinText: String? = null): String =
        sendMessageThroughService(
            PythonExecutionService.MSG_EXECUTE,
            source,
            PythonExecutionService.MSG_RESULT,
            stdinText
        )

    private fun sendMessageThroughService(
        messageWhat: Int,
        source: String?,
        expectedReplyWhat: Int,
        stdinText: String? = null
    ): String {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val component = ComponentName(context, PythonExecutionService::class.java)
        val intent = Intent().setComponent(component)
        val serviceInfo = runCatching {
            context.packageManager.getServiceInfo(component, 0)
        }.getOrNull()
        val diagnostics = "targetPackage=${context.packageName}, " +
            "targetUid=${context.applicationInfo.uid}, processUid=${Process.myUid()}, " +
            "serviceFound=${serviceInfo != null}, exported=${serviceInfo?.exported}, " +
            "serviceProcess=${serviceInfo?.processName}, permission=${serviceInfo?.permission}"

        Log.i(TAG, "Binding Python service: $diagnostics")
        val connected = CountDownLatch(1)
        val resultReady = CountDownLatch(1)
        val result = AtomicReference<String>()
        val replyMessenger = Messenger(Handler(Looper.getMainLooper()) { message ->
            if (message.what == expectedReplyWhat) {
                result.set(message.data.getString(PythonExecutionService.KEY_RESULT).orEmpty())
                resultReady.countDown()
            }
            true
        })

        var remoteMessenger: Messenger? = null
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: android.os.IBinder?) {
                Log.i(TAG, "onServiceConnected: component=$name binderPresent=${binder != null}")
                remoteMessenger = binder?.let { Messenger(it) }
                connected.countDown()
            }
            override fun onServiceDisconnected(name: ComponentName?) {
                Log.e(TAG, "onServiceDisconnected: component=$name")
                remoteMessenger = null
            }
            override fun onBindingDied(name: ComponentName?) {
                Log.e(TAG, "onBindingDied: component=$name")
                connected.countDown()
            }
            override fun onNullBinding(name: ComponentName?) {
                Log.e(TAG, "onNullBinding: component=$name")
                connected.countDown()
            }
        }

        val bound = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        Log.i(TAG, "bindService returned=$bound; $diagnostics")
        assertTrue("Could not bind to PythonExecutionService; $diagnostics", bound)
        try {
            assertTrue(
                "Python service connection timed out; $diagnostics",
                connected.await(20, TimeUnit.SECONDS) && remoteMessenger != null
            )
            val request = Message.obtain(null, messageWhat).apply {
                if (source != null) data = Bundle().apply {
                    putString(PythonExecutionService.KEY_CODE, source)
                    if (stdinText != null) putString(PythonExecutionService.KEY_STDIN, stdinText)
                }
                replyTo = replyMessenger
            }
            remoteMessenger?.send(request)
                ?: throw AssertionError("Python service returned no Binder messenger")
            assertTrue("Python execution timed out", resultReady.await(45, TimeUnit.SECONDS))
            return result.get() ?: throw AssertionError("Python service returned an empty result")
        } finally {
            context.unbindService(connection)
        }
    }

    companion object {
        private const val TAG = "ByteGhostRuntimeTest"
    }
}
