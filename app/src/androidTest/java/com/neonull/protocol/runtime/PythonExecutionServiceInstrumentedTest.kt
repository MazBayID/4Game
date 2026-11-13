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

/**
 * Android-device smoke tests for the Chaquopy + Binder PoC.
 *
 * These tests validate runtime startup and transport only. They do not establish
 * that executing arbitrary player code is secure.
 */
@RunWith(AndroidJUnit4::class)
class PythonExecutionServiceInstrumentedTest {

    @Test
    fun executesPythonAndReturnsStdoutOverBinder() {
        val result = executeThroughService("print('BYTEGHOST_RUNTIME_OK')")
        val json = JSONObject(result)

        assertTrue("Python execution should succeed: $result", json.getBoolean("ok"))
        assertEquals("BYTEGHOST_RUNTIME_OK\n", json.getString("stdout"))
        assertEquals("", json.getString("stderr"))
    }

    @Test
    fun returnsPythonExceptionOverBinderWithoutCrashingService() {
        val result = executeThroughService(
            "raise ValueError('BYTEGHOST_EXPECTED_ERROR')"
        )
        val json = JSONObject(result)

        assertEquals(false, json.getBoolean("ok"))
        assertEquals("ValueError", json.getString("errorType"))
        assertTrue(json.getString("traceback").contains("BYTEGHOST_EXPECTED_ERROR"))

        val followUp = JSONObject(executeThroughService("print('SERVICE_STILL_ALIVE')"))
        assertTrue("Service should accept a request after a Python exception", followUp.getBoolean("ok"))
        assertEquals("SERVICE_STILL_ALIVE\n", followUp.getString("stdout"))
    }

    private fun executeThroughService(source: String): String {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val connected = CountDownLatch(1)
        val resultReady = CountDownLatch(1)
        val result = AtomicReference<String>()

        val replyMessenger = Messenger(Handler(Looper.getMainLooper()) { message ->
            if (message.what == PythonExecutionService.MSG_RESULT) {
                result.set(
                    message.data.getString(PythonExecutionService.KEY_RESULT).orEmpty()
                )
                resultReady.countDown()
            }
            true
        })

        var remoteMessenger: Messenger? = null
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: android.os.IBinder?) {
                remoteMessenger = binder?.let { Messenger(it) }
                connected.countDown()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                remoteMessenger = null
            }
        }

        val bound = context.bindService(
            Intent(context, PythonExecutionService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
        assertTrue("Could not bind to PythonExecutionService", bound)

        try {
            assertTrue("Python service connection timed out", connected.await(15, TimeUnit.SECONDS))
            val request = Message.obtain(null, PythonExecutionService.MSG_EXECUTE).apply {
                data = Bundle().apply {
                    putString(PythonExecutionService.KEY_CODE, source)
                }
                replyTo = replyMessenger
            }
            remoteMessenger?.send(request)
                ?: throw AssertionError("Python service returned no Binder messenger")

            assertTrue("Python execution timed out", resultReady.await(30, TimeUnit.SECONDS))
            return result.get() ?: throw AssertionError("Python service returned an empty result")
        } finally {
            context.unbindService(connection)
        }
    }
}
