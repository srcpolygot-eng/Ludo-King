package com.example.ludo.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object SoundManager {
    private const val SAMPLE_RATE = 22050
    private val scope = CoroutineScope(Dispatchers.Default)

    var isSoundEnabled: Boolean = true
    var volume: Float = 0.8f

    fun playDiceRoll() {
        if (!isSoundEnabled || volume <= 0f) return
        scope.launch {
            // Generate rattle rattle clatter effect
            val durationSec = 0.35f
            val numSamples = (durationSec * SAMPLE_RATE).toInt()
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                // 4 distinct dice clatter bounces
                val envelope = ((1f - t / durationSec) * (0.5f + 0.5f * sin(2.0 * PI * 28.0 * t).toFloat()))
                val noise = ((Math.random() * 2 - 1).toFloat() * 0.4f)
                val tone = sin(2.0 * PI * (440.0 + 80.0 * sin(2.0 * PI * 18.0 * t)) * t).toFloat() * 0.6f
                val sample = ((noise + tone) * envelope * volume * 0.7f).coerceIn(-1f, 1f)
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    fun playTokenMove() {
        if (!isSoundEnabled || volume <= 0f) return
        scope.launch {
            // Crisp wooden marble tap
            val durationSec = 0.08f
            val numSamples = (durationSec * SAMPLE_RATE).toInt()
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val envelope = exp(-t * 45.0).toFloat()
                val freq = 720.0 + exp(-t * 60.0) * 400.0
                val sample = (sin(2.0 * PI * freq * t).toFloat() * envelope * volume * 0.8f).coerceIn(-1f, 1f)
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    fun playCapture() {
        if (!isSoundEnabled || volume <= 0f) return
        scope.launch {
            // Impact strike whoosh
            val durationSec = 0.28f
            val numSamples = (durationSec * SAMPLE_RATE).toInt()
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val envelope = exp(-t * 12.0).toFloat()
                val freq = (380.0 - t * 750.0).coerceAtLeast(80.0)
                val noise = ((Math.random() * 2 - 1).toFloat() * 0.35f * exp(-t * 25.0).toFloat())
                val tone = sin(2.0 * PI * freq * t).toFloat() * 0.75f
                val sample = ((tone + noise) * envelope * volume * 0.9f).coerceIn(-1f, 1f)
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    fun playReachedHome() {
        if (!isSoundEnabled || volume <= 0f) return
        scope.launch {
            // Ascending celebratory chime
            val durationSec = 0.45f
            val numSamples = (durationSec * SAMPLE_RATE).toInt()
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val freq = when {
                    t < 0.15f -> 523.25 // C5
                    t < 0.30f -> 659.25 // E5
                    else -> 783.99      // G5
                }
                val localT = t % 0.15f
                val env = exp(-localT * 14.0).toFloat()
                val sample = (sin(2.0 * PI * freq * t).toFloat() * env * volume * 0.8f).coerceIn(-1f, 1f)
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    fun playVictory() {
        if (!isSoundEnabled || volume <= 0f) return
        scope.launch {
            // Grand victory fanfare
            val durationSec = 1.0f
            val numSamples = (durationSec * SAMPLE_RATE).toInt()
            val buffer = ShortArray(numSamples)

            val notes = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
            val noteDuration = 0.25f

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val noteIdx = (t / noteDuration).toInt().coerceIn(0, notes.lastIndex)
                val freq = notes[noteIdx]
                val localT = t - (noteIdx * noteDuration)
                val env = exp(-localT * 6.0).toFloat()
                val harmonic = sin(2.0 * PI * freq * 2.0 * t).toFloat() * 0.25f
                val sample = ((sin(2.0 * PI * freq * t).toFloat() + harmonic) * env * volume * 0.85f).coerceIn(-1f, 1f)
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    fun playButtonClick() {
        if (!isSoundEnabled || volume <= 0f) return
        scope.launch {
            val durationSec = 0.04f
            val numSamples = (durationSec * SAMPLE_RATE).toInt()
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val env = exp(-t * 90.0).toFloat()
                val sample = (sin(2.0 * PI * 1100.0 * t).toFloat() * env * volume * 0.5f).coerceIn(-1f, 1f)
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    private fun playPcmBuffer(buffer: ShortArray) {
        try {
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
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            // Clean up after playback
            scope.launch {
                kotlinx.coroutines.delay((buffer.size * 1000L / SAMPLE_RATE) + 100L)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // Audio fallback gracefully handled
        }
    }
}
