package com.neonull.protocol.runtime

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Bundle
import android.os.Message
import android.os.Messenger
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

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
        workerThread = HandlerThread("ByteGhost-Python-Runtime").apply { start() }
        messenger = Messenger(Handler(workerThread.looper) { message ->
            handleExecution(message)
            true
        })
    }

    override fun onBind(intent: Intent?): IBinder = messenger.binder

    private fun handleExecution(message: Message) {
        val replyTo = message.replyTo ?: return
        val code = message.data.getString(KEY_CODE).orEmpty()
        val response = try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(applicationContext))
            }
            Python.getInstance().getModule("byteghost_runtime")
                .callAttr("execute", code).toString()
        } catch (error: Throwable) {
            val safeMessage = (error.message ?: "Runtime initialization failed")
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
            """{"ok":false,"errorType":"${error.javaClass.simpleName}","error":"$safeMessage"}"""
        }

        try {
            val reply = Message.obtain(null, MSG_RESULT).apply {
                data = Bundle().apply { putString(KEY_RESULT, response) }
            }
            replyTo.send(reply)
        } catch (_: Exception) {
            // Requesting component may already have been destroyed.
        }
    }

    override fun onDestroy() {
        workerThread.quitSafely()
        super.onDestroy()
    }

    companion object {
        const val MSG_EXECUTE = 1
        const val MSG_RESULT = 2
        const val KEY_CODE = "code"
        const val KEY_RESULT = "result"
    }
}
