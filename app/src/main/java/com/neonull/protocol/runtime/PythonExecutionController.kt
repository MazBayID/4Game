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

/** Binder client for the CPython process PoC; not wired to the editor yet. */
class PythonExecutionController(
    private val context: Context,
    private val onResult: (String) -> Unit
) {
    private var serviceMessenger: Messenger? = null
    private var isBound = false
    private var pendingCode: String? = null

    private val replyMessenger = Messenger(Handler(Looper.getMainLooper()) { message ->
        if (message.what == PythonExecutionService.MSG_RESULT) {
            onResult(message.data.getString(PythonExecutionService.KEY_RESULT).orEmpty())
        }
        true
    })

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: android.os.IBinder?) {
            serviceMessenger = binder?.let { Messenger(it) }
            sendPendingCode()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceMessenger = null
        }
    }

    fun execute(code: String) {
        pendingCode = code
        if (serviceMessenger != null) {
            sendPendingCode()
        } else if (!isBound) {
            isBound = context.bindService(
                Intent(context, PythonExecutionService::class.java),
                connection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    private fun sendPendingCode() {
        val code = pendingCode ?: return
        val messenger = serviceMessenger ?: return
        pendingCode = null

        try {
            val message = Message.obtain(null, PythonExecutionService.MSG_EXECUTE).apply {
                data = Bundle().apply { putString(PythonExecutionService.KEY_CODE, code) }
                replyTo = replyMessenger
            }
            messenger.send(message)
        } catch (error: RemoteException) {
            val safeMessage = (error.message ?: "Binder communication failed")
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
            onResult("""{"ok":false,"errorType":"RemoteException","error":"$safeMessage"}""")
        }
    }

    fun close() {
        if (isBound) {
            context.unbindService(connection)
        }
        isBound = false
        serviceMessenger = null
        pendingCode = null
    }
}
