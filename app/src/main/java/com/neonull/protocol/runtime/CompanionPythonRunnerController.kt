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
import android.os.RemoteException
import org.json.JSONObject

/**
 * Client for the Python execution service packaged inside ByteGhost.
 */
class CompanionPythonRunnerController(
    private val context: Context,
    private val onResult: (String) -> Unit
) {
    private var remote: Messenger? = null
    private var bound = false
    private var pendingSource: String? = null
    private var pendingStdin: String = ""
    private var pendingMissionId: Int? = null

    private val replies = Messenger(Handler(Looper.getMainLooper()) { message ->
        if (message.what == MSG_RESULT) {
            onResult(message.data.getString(KEY_RESULT).orEmpty())
        }
        true
    })

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: android.os.IBinder?) {
            remote = service?.let(::Messenger)
            sendPending()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            remote = null
            onResult(errorJson("RunnerDisconnected", "Python runner stopped; run the submission again"))
        }

        override fun onBindingDied(name: ComponentName?) {
            remote = null
            if (bound) {
                context.unbindService(this)
                bound = false
            }
            onResult(errorJson("RunnerBindingDied", "Python runner binding died"))
        }

        override fun onNullBinding(name: ComponentName?) {
            remote = null
            if (bound) {
                context.unbindService(this)
                bound = false
            }
            onResult(errorJson("RunnerUnavailable", "Python runner returned an empty binding"))
        }
    }

    fun execute(source: String, stdinText: String = "") =
        submit(source, stdinText, null)

    fun grade(missionId: Int, source: String, stdinText: String = "") =
        submit(source, stdinText, missionId)

    private fun submit(source: String, stdinText: String, missionId: Int?) {
        if (source.length > MAX_SOURCE_CHARS || stdinText.length > MAX_STDIN_CHARS) {
            onResult(errorJson(
                if (source.length > MAX_SOURCE_CHARS) "SourceTooLarge" else "InputTooLarge",
                "Submission exceeds the allowed input size"
            ))
            return
        }
        pendingSource = source
        pendingStdin = stdinText
        pendingMissionId = missionId
        if (remote != null) {
            sendPending()
        } else if (!bound) {
            val intent = Intent(context, com.neonull.protocol.runner.PythonRunnerService::class.java)
            try {
                bound = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            } catch (error: SecurityException) {
                bound = false
                pendingSource = null
                onResult(errorJson("RunnerPermissionDenied", error.message))
                return
            }
            if (!bound) {
                pendingSource = null
                onResult(errorJson("RunnerUnavailable", "The embedded Python execution service could not be started"))
            }
        }
    }

    private fun sendPending() {
        val source = pendingSource ?: return
        val messenger = remote ?: return
        val stdin = pendingStdin
        val missionId = pendingMissionId
        pendingSource = null
        pendingMissionId = null
        try {
            messenger.send(Message.obtain(null, MSG_EXECUTE).apply {
                data = Bundle().apply {
                    putString(KEY_CODE, source)
                    putString(KEY_STDIN, stdin)
                    putString(KEY_MODE, if (missionId == null) MODE_EXECUTE else MODE_GRADE)
                    missionId?.let { putInt(KEY_MISSION_ID, it) }
                }
                replyTo = replies
            })
        } catch (error: RemoteException) {
            onResult(errorJson("RunnerTransportError", error.message))
        }
    }

    fun close() {
        if (bound) context.unbindService(connection)
        bound = false
        remote = null
        pendingSource = null
        pendingStdin = ""
        pendingMissionId = null
    }

    private fun errorJson(type: String, message: String?): String =
        JSONObject()
            .put("ok", false)
            .put("stdout", "")
            .put("stderr", "")
            .put("stdoutTruncated", false)
            .put("stderrTruncated", false)
            .put("errorType", type)
            .put("error", message ?: type)
            .toString()

    companion object {
        private const val MSG_EXECUTE = 1
        private const val MSG_RESULT = 2
        private const val KEY_CODE = "code"
        private const val KEY_STDIN = "stdin"
        private const val KEY_MODE = "mode"
        private const val KEY_MISSION_ID = "missionId"
        private const val KEY_RESULT = "result"
        private const val MODE_EXECUTE = "execute"
        private const val MODE_GRADE = "grade"
        private const val MAX_SOURCE_CHARS = 128 * 1024
        private const val MAX_STDIN_CHARS = 64 * 1024
    }
}
