package com.neonull.protocol.runner

import android.content.ComponentName
import android.content.Context
import android.content.Intent
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
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class PythonRunnerServiceTest {
    @Test
    fun executesPythonAndBoundsOutput() {
        val result = JSONObject(request("print('RUNNER_OK')"))
        assertTrue(result.toString(), result.getBoolean("ok"))
        assertEquals("RUNNER_OK\n", result.getString("stdout"))
        val large = JSONObject(request("print('X' * 70000)"))
        assertTrue(large.getBoolean("ok"))
        assertEquals(64 * 1024, large.getString("stdout").length)
        assertTrue(large.getBoolean("stdoutTruncated"))
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
                connection.send("while True: pass", MSG_RESULT)
            } catch (_: Exception) {
                // Process death may close the Binder before a reply is delivered.
            } finally {
                connection.close()
            }
            assertTrue("Watchdog did not terminate the runner process", disconnected.await(15, TimeUnit.SECONDS))
        } finally {
            context.unbindService(observer)
        }

        val recovered = JSONObject(request("print('RUNNER_RECOVERED')"))
        assertTrue(recovered.toString(), recovered.getBoolean("ok"))
        assertEquals("RUNNER_RECOVERED\n", recovered.getString("stdout"))
    }

    private fun request(source: String): String {
        val connection = bind(InstrumentationRegistry.getInstrumentation().targetContext)
        return try { connection.send(source, MSG_RESULT) } finally { connection.close() }
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
        fun send(source: String, expectedWhat: Int): String {
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
                    putString(PythonRunnerService.KEY_STDIN, "")
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
