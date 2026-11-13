package com.neonull.protocol.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Lightweight procedural Web-Audio-style synth SFX generator using Android AudioTrack.
 * Wrapped in safe background threads & try/catch so emulators or unit tests never crash.
 */
class CyberAudioEngine {
    @Volatile
    var enabled: Boolean = true

    fun playKeyTick() {
        if (!enabled) return
        playToneAsync(freqStart = 880.0, freqEnd = 620.0, durationMs = 28, volume = 0.14)
    }

    fun playDeployBeam() {
        if (!enabled) return
        playSequenceAsync(
            listOf(
                Triple(320.0, 760.0, 95),
                Triple(760.0, 980.0, 70)
            ),
            volume = 0.22
        )
    }

    fun playBreachSuccess() {
        if (!enabled) return
        playSequenceAsync(
            listOf(
                Triple(523.25, 523.25, 90),
                Triple(659.25, 659.25, 90),
                Triple(783.99, 783.99, 100),
                Triple(1046.50, 1046.50, 180)
            ),
            volume = 0.25
        )
    }

    fun playCrimsonAlert() {
        if (!enabled) return
        playSequenceAsync(
            listOf(
                Triple(220.0, 145.0, 130),
                Triple(180.0, 120.0, 160)
            ),
            volume = 0.26
        )
    }

    private fun playToneAsync(freqStart: Double, freqEnd: Double, durationMs: Int, volume: Double) {
        playSequenceAsync(listOf(Triple(freqStart, freqEnd, durationMs)), volume)
    }

    private fun playSequenceAsync(notes: List<Triple<Double, Double, Int>>, volume: Double) {
        Thread {
            try {
                val sampleRate = 22050
                val totalSamples = notes.sumOf { (sampleRate * it.third) / 1000 }
                if (totalSamples <= 0) return@Thread

                val pcm = ShortArray(totalSamples)
                var cursor = 0
                for ((f0, f1, ms) in notes) {
                    val count = (sampleRate * ms) / 1000
                    var phase = 0.0
                    for (i in 0 until count) {
                        val t = i.toDouble() / count.coerceAtLeast(1)
                        val freq = f0 + (f1 - f0) * t
                        phase += 2.0 * PI * freq / sampleRate
                        val env = exp(-3.2 * t)
                        val sample = (sin(phase) * volume * env * Short.MAX_VALUE).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        if (cursor < totalSamples) {
                            pcm[cursor++] = sample.toShort()
                        }
                    }
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcm.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcm, 0, pcm.size)
                track.play()
                val totalMs = notes.sumOf { it.third.toLong() } + 60L
                Thread.sleep(totalMs)
                track.stop()
                track.release()
            } catch (_: Throwable) {
                // Ignore safely in headless/unit-test environments without audio hardware
            }
        }.apply {
            isDaemon = true
            start()
        }
    }
}
