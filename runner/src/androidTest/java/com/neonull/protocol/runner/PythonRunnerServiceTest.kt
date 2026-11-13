package com.neonull.protocol.runner

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Messenger
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class PythonRunnerServiceTest {
    @Test
    fun companionRunnerUsesSeparateUidAndSignaturePermission() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val runnerUid = context.applicationInfo.uid
        val hostUid = context.packageManager.getApplicationInfo("com.neonull.protocol", 0).uid
        assertTrue("Runner must have a separate Android UID", runnerUid != hostUid)
        val service = context.packageManager.getServiceInfo(
            ComponentName(context, PythonRunnerService::class.java), 0
        )
        assertEquals("com.neonull.protocol.permission.RUN_PYTHON", service.permission)
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
        val requested = packageInfo.requestedPermissions?.toList().orEmpty()
        assertFalse("Runner must not request INTERNET", requested.contains("android.permission.INTERNET"))
    }

    @Test
    fun executesPythonAndBoundsOutput() {
        val result = JSONObject(request("print('RUNNER_OK')"))
        assertTrue(result.toString(), result.getBoolean("ok"))
        assertEquals("RUNNER_OK\n", result.getString("stdout"))
        val input = JSONObject(request("name = input()\nprint('Hello', name)", "Zero\n"))
        assertTrue(input.toString(), input.getBoolean("ok"))
        assertEquals("Hello Zero\n", input.getString("stdout"))
        val large = JSONObject(request("print('X' * 70000)"))
        val oversizedSource = JSONObject(request(" ".repeat(128 * 1024 + 1)))
        assertFalse(oversizedSource.getBoolean("ok"))
        assertEquals("SourceTooLarge", oversizedSource.getString("errorType"))
        val oversizedInput = JSONObject(request("print(1)", "X".repeat(64 * 1024 + 1)))
        assertFalse(oversizedInput.getBoolean("ok"))
        assertEquals("InputTooLarge", oversizedInput.getString("errorType"))
        assertTrue(large.getBoolean("ok"))
        assertEquals(64 * 1024, large.getString("stdout").length)
        assertTrue(large.getBoolean("stdoutTruncated"))
    }

    @Test
    fun rejectsConcurrentExecutionInsteadOfQueueingIt() {
        val connection = bind(InstrumentationRegistry.getInstrumentation().targetContext)
        val firstFinished = CountDownLatch(1)
        try {
            connection.sendWithoutWaiting("import time; time.sleep(1); print('FIRST_DONE')") {
                firstFinished.countDown()
            }
            val second = JSONObject(connection.send("print('SHOULD_NOT_RUN')", "", MSG_RESULT))
            assertFalse(second.getBoolean("ok"))
            assertEquals("RunnerBusy", second.getString("errorType"))
            assertTrue("First execution did not finish", firstFinished.await(120, TimeUnit.SECONDS))
        } finally {
            connection.close()
        }
    }

    @Test
    fun watchdogKillsInfiniteLoopAndRunnerCanRestart() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val disconnected = CountDownLatch(1)
        val observer = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: android.os.IBinder?) = Unit
            override fun onServiceDisconnected(name: ComponentName?) { disconnected.countDown() }
        }
        val intent = Intent().setComponent(ComponentName(context, PythonRunnerService::class.java))
        assertTrue(context.bindService(intent, observer, Context.BIND_AUTO_CREATE))
        try {
            val connection = bind(context)
            try {
                connection.sendWithoutWaiting("while True: pass")
                assertTrue("Watchdog did not terminate the runner process", disconnected.await(90, TimeUnit.SECONDS))
            } finally {
                connection.close()
            }
        } finally {
            context.unbindService(observer)
        }

        val recovered = JSONObject(request("print('RUNNER_RECOVERED')"))
        assertTrue(recovered.toString(), recovered.getBoolean("ok"))
        assertEquals("RUNNER_RECOVERED\n", recovered.getString("stdout"))
    }

    private fun request(source: String, stdin: String = ""): String {
        val connection = bind(InstrumentationRegistry.getInstrumentation().targetContext)
        return try { connection.send(source, stdin, MSG_RESULT) } finally { connection.close() }
    }

    private fun bind(context: Context): TestConnection {
        val connected = CountDownLatch(1)
        val remote = AtomicReference<Messenger?>()
        val callback = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: android.os.IBinder?) {
                remote.set(binder?.let(::Messenger))
                connected.countDown()
            }
            override fun onServiceDisconnected(name: ComponentName?) = Unit
        }
        val intent = Intent().setComponent(ComponentName(context, PythonRunnerService::class.java))
        check(context.bindService(intent, callback, Context.BIND_AUTO_CREATE)) { "Unable to bind runner" }
        check(connected.await(30, TimeUnit.SECONDS)) { "Runner bind timed out" }
        return TestConnection(context, callback, remote.get() ?: error("Missing runner Binder"))
    }

    private class TestConnection(
        private val context: Context,
        private val callback: ServiceConnection,
        private val remote: Messenger
    ) {
        fun sendWithoutWaiting(source: String, onReply: () -> Unit) {
            val reply = Messenger(Handler(Looper.getMainLooper()) {
                onReply()
                true
            })
            remote.send(Message.obtain(null, PythonRunnerService.MSG_EXECUTE).apply {
                data = Bundle().apply {
                    putString(PythonRunnerService.KEY_CODE, source)
                    putString(PythonRunnerService.KEY_STDIN, "")
                }
                replyTo = reply
            })
        }

        fun send(source: String, stdin: String, expectedWhat: Int): String {
            val latch = CountDownLatch(1)
            val result = AtomicReference<String>()
            val reply = Messenger(Handler(Looper.getMainLooper()) { message ->
                if (message.what == expectedWhat) {
                    result.set(message.data.getString(PythonRunnerService.KEY_RESULT).orEmpty())
                    latch.countDown()
                }
                true
            })
            remote.send(Message.obtain(null, PythonRunnerService.MSG_EXECUTE).apply {
                data = Bundle().apply {
                    putString(PythonRunnerService.KEY_CODE, source)
                    putString(PythonRunnerService.KEY_STDIN, stdin)
                }
                replyTo = reply
            })
            check(latch.await(120, TimeUnit.SECONDS)) { "Runner response timed out" }
            return result.get() ?: error("Empty runner response")
        }

        fun close() = context.unbindService(callback)
    }

    companion object {
        private const val MSG_RESULT = PythonRunnerService.MSG_RESULT
    }
}
