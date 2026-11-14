package com.neonull.protocol.runner

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Process
import android.os.SystemClock
import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import org.json.JSONObject
import java.io.File
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicBoolean

/** In-package Python execution service hosted in a dedicated Android process. */
class PythonRunnerService : Service() {
    private lateinit var worker: HandlerThread
    private lateinit var messenger: Messenger
    private lateinit var workerHandler: Handler
    private val executing = AtomicBoolean(false)
    private val watchdog = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        deleteRecursively(File(cacheDir, "byteghost-session"))
        worker = HandlerThread("ByteGhost-Runner").apply { start() }
        workerHandler = Handler(worker.looper)
        messenger = Messenger(Handler(Looper.getMainLooper()) { message ->
            when (message.what) {
                MSG_PING -> reply(message, MSG_PING_RESULT, "BINDER_OK")
                MSG_EXECUTE -> {
                    if (executing.compareAndSet(false, true)) {
                        val request = Message.obtain(message)
                        val posted = workerHandler.post {
                            try {
                                execute(request)
                            } finally {
                                request.recycle()
                                executing.set(false)
                            }
                        }
                        if (!posted) {
                            request.recycle()
                            executing.set(false)
                            message.replyTo?.let {
                                sendJson(it, errorResult("RunnerUnavailable", "Execution worker is not available"))
                            }
                        }
                    } else {
                        message.replyTo?.let {
                            sendJson(it, errorResult("RunnerBusy", "Only one execution is allowed at a time"))
                        }
                    }
                }
                else -> Log.w(TAG, "Ignoring unknown message " + message.what)
            }
            true
        })
        Log.i(TAG, "Runner ready uid=" + Process.myUid() + " pid=" + Process.myPid())
    }

    override fun onBind(intent: Intent?): IBinder = messenger.binder

    private fun execute(message: Message) {
        val replyTo = message.replyTo ?: return
        val source = message.data.getString(KEY_CODE).orEmpty()
        val stdin = message.data.getString(KEY_STDIN).orEmpty()
        if (source.length > MAX_SOURCE_CHARS || stdin.length > MAX_STDIN_CHARS) {
            sendJson(replyTo, errorResult(if (source.length > MAX_SOURCE_CHARS) "SourceTooLarge" else "InputTooLarge"))
            return
        }

        val workspace = File(cacheDir, "byteghost-session")
        val startedAt = SystemClock.elapsedRealtime()
        try {
            deleteRecursively(workspace)
            if (!workspace.mkdirs() && !workspace.isDirectory) {
                throw IllegalStateException("Unable to create temporary workspace")
            }
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(applicationContext))
            }
            val mode = message.data.getString(KEY_MODE) ?: MODE_EXECUTE
            val expired = Runnable {
                Log.e(TAG, "Execution watchdog expired; killing runner process")
                Process.killProcess(Process.myPid())
            }
            watchdog.postDelayed(expired, EXECUTION_TIMEOUT_MS)
            val result = try {
                val python = Python.getInstance()
                when (mode) {
                    MODE_EXECUTE -> python.getModule("byteghost_runner")
                        .callAttr("execute", source, stdin, workspace.absolutePath).toString()
                    MODE_GRADE -> {
                        val missionId = message.data.getInt(KEY_MISSION_ID, -1)
                        python.getModule("byteghost_grader")
                            .callAttr("grade", missionId, source, stdin, workspace.absolutePath).toString()
                    }
                    else -> errorResult("UnknownOperation", "Unsupported runner operation: $mode")
                }
            } finally {
                watchdog.removeCallbacks(expired)
            }
            deleteRecursively(workspace)
            Log.i(TAG, "Execution completed elapsedMs=" + (SystemClock.elapsedRealtime() - startedAt))
            sendJson(replyTo, result)
        } catch (error: Throwable) {
            deleteRecursively(workspace)
            Log.e(TAG, "Runner execution failed", error)
            sendJson(replyTo, errorResult("RunnerError", error.message))
        }
    }

    private fun reply(request: Message, what: Int, value: String) {
        request.replyTo?.let { sendReply(it, what, value) }
    }

    private fun sendJson(replyTo: Messenger, json: String) = sendReply(replyTo, MSG_RESULT, json)

    private fun sendReply(replyTo: Messenger, what: Int, value: String) {
        runCatching {
            replyTo.send(Message.obtain(null, what).apply {
                data = Bundle().apply { putString(KEY_RESULT, value) }
            })
        }.onFailure { Log.w(TAG, "Requester is no longer available", it) }
    }

    private fun errorResult(type: String, message: String? = null): String =
        JSONObject()
            .put("ok", false)
            .put("stdout", "")
            .put("stderr", "")
            .put("stdoutTruncated", false)
            .put("stderrTruncated", false)
            .put("errorType", type)
            .put("error", message ?: type)
            .put("traceback", JSONObject.NULL)
            .toString()

    private fun deleteRecursively(file: File) {
        val path = file.toPath()
        if (Files.isSymbolicLink(path)) {
            Files.deleteIfExists(path)
            return
        }
        if (file.isDirectory) file.listFiles()?.forEach(::deleteRecursively)
        Files.deleteIfExists(path)
    }

    override fun onDestroy() {
        worker.quitSafely()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "ByteGhostRunner"
        const val MSG_EXECUTE = 1
        const val MSG_RESULT = 2
        const val MSG_PING = 3
        const val MSG_PING_RESULT = 4
        const val KEY_CODE = "code"
        const val KEY_STDIN = "stdin"
        const val KEY_MODE = "mode"
        const val KEY_MISSION_ID = "missionId"
        const val KEY_RESULT = "result"
        const val MODE_EXECUTE = "execute"
        const val MODE_GRADE = "grade"
        private const val MAX_SOURCE_CHARS = 128 * 1024
        private const val MAX_STDIN_CHARS = 64 * 1024
        private const val EXECUTION_TIMEOUT_MS = 5_000L
    }
}
