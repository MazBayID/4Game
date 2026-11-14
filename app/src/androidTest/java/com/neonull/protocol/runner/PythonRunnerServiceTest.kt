package com.neonull.protocol.runner

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import java.io.File
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
    fun embeddedRunnerLivesInDedicatedProcessInsideByteGhostPackage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val service = context.packageManager.getServiceInfo(
            ComponentName(context, PythonRunnerService::class.java), 0
        )
        assertEquals("com.neonull.protocol", service.applicationInfo.packageName)
        assertEquals("com.neonull.protocol:execution", service.processName)
        assertFalse("Runner service must not be exported", service.exported)
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            android.content.pm.PackageManager.GET_PERMISSIONS
        )
        val requested = packageInfo.requestedPermissions?.toList().orEmpty()
        assertFalse("ByteGhost Runner must not request INTERNET", requested.contains("android.permission.INTERNET"))
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
    fun gradesAllSevenMissionsAndRejectsIncorrectSolutions() {
        val valid = linkedMapOf(
            1 to """agent = "Null-Agent"
clearance = 1
print(agent)
""",
            2 to """threat = 25
if threat >= 50:
    print("DENIED")
else:
    print("ALLOW")
""",
            3 to """shards = ["K1", "K2"]
shards.append("K3")
vault = {"code": 77}
print(len(shards))
""",
            4 to """packets = 0
for packet in range(5):
    packets += 1
print(packets)
""",
            5 to """def craft_token(seed):
    return seed * 3 + 9
print(craft_token(7))
""",
            6 to """try:
    with open("corrupt.log", "r") as log_file:
        print(log_file.read())
except FileNotFoundError:
    print("SAFE_EXTRACT")
""",
            7 to """class OverlordOverride:
    def __init__(self, key):
        self.key = key
    def shutdown(self):
        return "ROOT_UNLOCKED"
core = OverlordOverride("NULL-2142")
print(core.shutdown())
"""
        )
        valid.forEach { (missionId, source) ->
            val result = JSONObject(requestGrade(missionId, source))
            assertTrue("Mission $missionId CPython execution failed: $result", result.getBoolean("ok"))
            assertTrue("Mission $missionId should pass: $result", result.getBoolean("passed"))
        }

        val invalid = valid.mapValues { (_, source) -> source.replace("clearance = 1", "clearance = 0") }
        val wrongMissionOne = JSONObject(requestGrade(1, invalid.getValue(1)))
        assertTrue(wrongMissionOne.toString(), wrongMissionOne.getBoolean("ok"))
        assertFalse("Incorrect mission solution must not pass", wrongMissionOne.getBoolean("passed"))

        val overwrittenAgent = JSONObject(requestGrade(
            1,
            valid.getValue(1) + "\nagent = \"spoof\""
        ))
        assertFalse("Final variable state must be graded, not only earlier assignments", overwrittenAgent.getBoolean("passed"))

        val overwrittenBranch = JSONObject(requestGrade(
            2,
            valid.getValue(2).replace(
                "    print(\"ALLOW\")",
                "    print(\"ALLOW\")\n    print(\"BLOCKED\")"
            )
        ))
        assertFalse("The actual final branch output must be validated", overwrittenBranch.getBoolean("passed"))

        val clearedShards = JSONObject(requestGrade(
            3,
            valid.getValue(3) + "\nshards.clear()"
        ))
        assertFalse("Final collection contents must be validated", clearedShards.getBoolean("passed"))

        val overwrittenPackets = JSONObject(requestGrade(
            4,
            valid.getValue(4) + "\npackets = 99"
        ))
        assertFalse("Final loop variable must be validated", overwrittenPackets.getBoolean("passed"))

        val syntax = JSONObject(requestGrade(1, "if True print('broken')"))
        assertFalse(syntax.getBoolean("ok"))
        assertFalse(syntax.getBoolean("passed"))
        assertEquals("SyntaxError", syntax.getString("errorType"))
    }

    private fun requestGrade(missionId: Int, source: String): String {
        val connection = bind(InstrumentationRegistry.getInstrumentation().targetContext)
        return try {
            connection.send(source, "", MSG_RESULT, "grade", missionId)
        } finally {
            connection.close()
        }
    }

    @Test
    fun executesRepresentativePythonLanguageCorpusAndFreshNamespaces() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val source = """import math
numbers = [value * value for value in range(4)]
lookup = {str(value): value for value in numbers}
def double(value):
    return value * 2
class Device:
    def __init__(self, name):
        self.name = name
    def boot(self):
        return f"{self.name}:{double(5)}"
class HostileMeta(type):
    def __getattribute__(cls, name):
        if name == "__name__":
            raise RuntimeError("blocked")
        return super().__getattribute__(name)
class Hostile(metaclass=HostileMeta):
    pass
hostile = Hostile()
try:
    raise ValueError("handled")
except ValueError as error:
    caught = str(error)
with open("runner-check.txt", "w") as output_file:
    output_file.write("workspace-ok")
with open("runner-check.txt", "r") as input_file:
    file_value = input_file.read()
print(numbers)
print(lookup["9"])
print(Device("CPython").boot())
print(caught)
print(file_value)
print(round(math.pi, 2))
"""
        val result = JSONObject(request(source))
        assertTrue(result.toString(), result.getBoolean("ok"))
        assertEquals("[0, 1, 4, 9]\n9\nCPython:10\nhandled\nworkspace-ok\n3.14\n", result.getString("stdout"))
        val workspaceFile = File(context.cacheDir, "byteghost-session/runner-check.txt")
        assertFalse("Temporary workspace must be removed after execution", workspaceFile.exists())
        val slots = result.getJSONArray("ramSlots")
        assertTrue((0 until slots.length()).any {
            slots.getJSONObject(it).optString("name") == "hostile" &&
                slots.getJSONObject(it).optString("type") == "Hostile"
        })
        val fresh = JSONObject(request("print('numbers' in globals())"))
        assertTrue(fresh.toString(), fresh.getBoolean("ok"))
        assertEquals("False\n", fresh.getString("stdout"))
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
        fun sendWithoutWaiting(source: String, onReply: () -> Unit = {}) {
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

        fun send(source: String, stdin: String, expectedWhat: Int, mode: String = "execute", missionId: Int = -1): String {
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
                    putString(PythonRunnerService.KEY_MODE, mode)
                    if (missionId >= 0) putInt(PythonRunnerService.KEY_MISSION_ID, missionId)
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
