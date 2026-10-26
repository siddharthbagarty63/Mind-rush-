package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager {
    var isSoundEnabled: Boolean = true
    private val scope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 22050

    fun playTap() {
        if (!isSoundEnabled) return
        scope.launch {
            generateTone(freq = 880f, durationMs = 25, shape = ToneShape.SINE)
        }
    }

    fun playCorrect() {
        if (!isSoundEnabled) return
        scope.launch {
            generateTwoTone(freq1 = 784f, freq2 = 1175f, durationMs = 80)
        }
    }

    fun playWrong() {
        if (!isSoundEnabled) return
        scope.launch {
            generateSweep(startFreq = 220f, endFreq = 110f, durationMs = 120, shape = ToneShape.SQUARE)
        }
    }

    fun playGameStart() {
        if (!isSoundEnabled) return
        scope.launch {
            generateSweep(startFreq = 440f, endFreq = 1100f, durationMs = 150, shape = ToneShape.SINE)
        }
    }

    fun playVictory() {
        if (!isSoundEnabled) return
        scope.launch {
            val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f)
            for (f in notes) {
                generateTone(freq = f, durationMs = 60, shape = ToneShape.SINE)
            }
        }
    }

    fun playXpReward() {
        if (!isSoundEnabled) return
        scope.launch {
            generateSweep(startFreq = 987f, endFreq = 1318f, durationMs = 50, shape = ToneShape.SINE)
        }
    }

    fun playLevelUp() {
        if (!isSoundEnabled) return
        scope.launch {
            val notes = floatArrayOf(440f, 554f, 659f, 880f, 1108f)
            for (f in notes) {
                generateTone(freq = f, durationMs = 70, shape = ToneShape.SINE)
            }
        }
    }

    fun playAchievement() {
        if (!isSoundEnabled) return
        scope.launch {
            val notes = floatArrayOf(659f, 784f, 988f, 1318f)
            for (f in notes) {
                generateTone(freq = f, durationMs = 80, shape = ToneShape.SINE)
            }
        }
    }

    private enum class ToneShape { SINE, SQUARE }

    private fun generateTone(freq: Float, durationMs: Int, shape: ToneShape = ToneShape.SINE) {
        try {
            val numSamples = (durationMs * sampleRate) / 1000
            val buffer = ShortArray(numSamples)
            val dTheta = (2.0 * Math.PI * freq) / sampleRate

            for (i in 0 until numSamples) {
                val envelope = 1.0 - (i.toDouble() / numSamples) // decay
                val wave = when (shape) {
                    ToneShape.SINE -> sin(i * dTheta)
                    ToneShape.SQUARE -> if (sin(i * dTheta) >= 0) 0.6 else -0.6
                }
                buffer[i] = (wave * envelope * Short.MAX_VALUE * 0.4).toInt().toShort()
            }
            playPcm(buffer)
        } catch (_: Exception) {}
    }

    private fun generateTwoTone(freq1: Float, freq2: Float, durationMs: Int) {
        try {
            val halfSamples = (durationMs * sampleRate) / 2000
            val totalSamples = halfSamples * 2
            val buffer = ShortArray(totalSamples)
            val dTheta1 = (2.0 * Math.PI * freq1) / sampleRate
            val dTheta2 = (2.0 * Math.PI * freq2) / sampleRate

            for (i in 0 until halfSamples) {
                val env = 1.0 - (i.toDouble() / (halfSamples * 1.5))
                buffer[i] = (sin(i * dTheta1) * env * Short.MAX_VALUE * 0.4).toInt().toShort()
            }
            for (i in halfSamples until totalSamples) {
                val offset = i - halfSamples
                val env = 1.0 - (offset.toDouble() / halfSamples)
                buffer[i] = (sin(offset * dTheta2) * env * Short.MAX_VALUE * 0.45).toInt().toShort()
            }
            playPcm(buffer)
        } catch (_: Exception) {}
    }

    private fun generateSweep(startFreq: Float, endFreq: Float, durationMs: Int, shape: ToneShape) {
        try {
            val numSamples = (durationMs * sampleRate) / 1000
            val buffer = ShortArray(numSamples)
            var currentPhase = 0.0

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                val dTheta = (2.0 * Math.PI * currentFreq) / sampleRate
                currentPhase += dTheta

                val envelope = 1.0 - progress
                val wave = when (shape) {
                    ToneShape.SINE -> sin(currentPhase)
                    ToneShape.SQUARE -> if (sin(currentPhase) >= 0) 0.5 else -0.5
                }
                buffer[i] = (wave * envelope * Short.MAX_VALUE * 0.4).toInt().toShort()
            }
            playPcm(buffer)
        } catch (_: Exception) {}
    }

    private fun playPcm(buffer: ShortArray) {
        val audioTrack = AudioTrack.Builder()
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
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()
        scope.launch {
            try {
                kotlinx.coroutines.delay((buffer.size * 1000L) / sampleRate + 50)
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }
}
