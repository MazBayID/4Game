package com.neonull.protocol.runtime

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Bundle
import android.os.Message
import android.os.Messenger
import android.os.SystemClock
import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import org.json.JSONObject

/**
 * CPython proof of concept in a dedicated Android app process.
 * The process shares the host app UID and permissions. It is NOT a security
 * sandbox for arbitrary untrusted Python submissions.
 */
class PythonExecutionService : Service() {
    private lateinit var workerThread: HandlerThread
    private lateinit var messenger: Messenger

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate package=$packageName pid=${android.os.Process.myPid()} uid=${android.os.Process.myUid()}")
        workerThread = HandlerThread("ByteGhost-Python-Runtime").apply { start() }
        messenger = Messenger(Handler(workerThread.looper) { message ->
            when (message.what) {
                MSG_PING -> handlePing(message)
                MSG_EXECUTE -> handleExecution(message)
                else -> Log.w(TAG, "Ignoring unknown message type=${message.what}")
            }
            true
        })
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.i(TAG, "onBind action=${intent?.action}")
        return messenger.binder
    }

    /** Binder-only health check. This path must never initialize Chaquopy. */
    private fun handlePing(message: Message) {
        Log.i(TAG, "Received Binder ping pid=${android.os.Process.myPid()} thread=${Thread.currentThread().name}")
        val replyTo = message.replyTo ?: return
        try {
            val reply = Message.obtain(null, MSG_PING_RESULT).apply {
                data = Bundle().apply { putString(KEY_RESULT, "BINDER_OK") }
            }
            replyTo.send(reply)
            Log.i(TAG, "Binder ping replied without starting Python")
        } catch (error: Exception) {
            Log.w(TAG, "Unable to deliver Binder ping response", error)
        }
    }

    private fun handleExecution(message: Message) {
        val requestStartedAt = SystemClock.elapsedRealtime()
        val replyTo = message.replyTo ?: run {
            Log.w(TAG, "Execution request has no replyTo")
            return
        }
        val code = message.data.getString(KEY_CODE).orEmpty()
        val stdinText = message.data.getString(KEY_STDIN).orEmpty()
        Log.i(
            TAG,
            "Received execution request sourceChars=${code.length} stdinChars=${stdinText.length} " +
                "pid=${android.os.Process.myPid()} thread=${Thread.currentThread().name}"
        )
        val inputError = when {
            code.length > MAX_SOURCE_CHARS -> "SourceTooLarge"
            stdinText.length > MAX_STDIN_CHARS -> "InputTooLarge"
            else -> null
        }
        val response = if (inputError != null) {
            Log.w(TAG, "Rejected execution request reason=$inputError")
            JSONObject()
                .put("ok", false)
                .put("stdout", "")
                .put("stderr", "")
                .put("stdoutTruncated", false)
                .put("stderrTruncated", false)
                .put("errorType", inputError)
                .put("traceback", JSONObject.NULL)
                .toString()
        } else try {
            if (!Python.isStarted()) {
                val startupStartedAt = SystemClock.elapsedRealtime()
                Log.i(TAG, "Chaquopy startup begin pid=${android.os.Process.myPid()}")
                val platform = AndroidPlatform(applicationContext).apply {
                    // Surface native startup diagnostics (e.g. loader/import messages) in Logcat.
                    redirectStdioToLogcat()
                }
                Python.start(platform)
                Log.i(
                    TAG,
                    "Chaquopy startup complete elapsedMs=${SystemClock.elapsedRealtime() - startupStartedAt}"
                )
            } else {
                Log.i(TAG, "Chaquopy already started")
            }

            val moduleStartedAt = SystemClock.elapsedRealtime()
            val module = Python.getInstance().getModule("byteghost_runtime")
            Log.i(
                TAG,
                "Python module loaded elapsedMs=${SystemClock.elapsedRealtime() - moduleStartedAt}"
            )

            val executionStartedAt = SystemClock.elapsedRealtime()
            val result = module.callAttr("execute", code, stdinText).toString()
            Log.i(
                TAG,
                "Python execution complete elapsedMs=${SystemClock.elapsedRealtime() - executionStartedAt} " +
                    "totalElapsedMs=${SystemClock.elapsedRealtime() - requestStartedAt}"
            )
            result
        } catch (error: Throwable) {
            Log.e(
                TAG,
                "Python runtime stage failed afterMs=${SystemClock.elapsedRealtime() - requestStartedAt} " +
                    "pid=${android.os.Process.myPid()}",
                error
            )
            JSONObject()
                .put("ok", false)
                .put("errorType", error.javaClass.simpleName)
                .put("error", error.message ?: "Runtime initialization failed")
                .toString()
        }

        try {
            val reply = Message.obtain(null, MSG_RESULT).apply {
                data = Bundle().apply { putString(KEY_RESULT, response) }
            }
            replyTo.send(reply)
            Log.i(
                TAG,
                "Execution reply sent totalElapsedMs=${SystemClock.elapsedRealtime() - requestStartedAt}"
            )
        } catch (error: Exception) {
            Log.w(TAG, "Unable to deliver Python result; requester may be gone", error)
        }
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy")
        workerThread.quitSafely()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "ByteGhostPythonService"
        const val MSG_EXECUTE = 1
        const val MSG_RESULT = 2
        const val MSG_PING = 3
        const val MSG_PING_RESULT = 4
        const val KEY_CODE = "code"
        const val KEY_STDIN = "stdin"
        const val KEY_RESULT = "result"
        private const val MAX_SOURCE_CHARS = 128 * 1024
        private const val MAX_STDIN_CHARS = 64 * 1024
    }
}
