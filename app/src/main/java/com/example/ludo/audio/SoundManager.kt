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
            // Realistic multi-bounce dice clatter with decaying impacts
            val durationSec = 0.42f
            val numSamples = (durationSec * SAMPLE_RATE).toInt()
            val buffer = ShortArray(numSamples)
            val bounceTimes = floatArrayOf(0.0f, 0.07f, 0.15f, 0.24f, 0.33f)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                var sample = 0f
                for (bt in bounceTimes) {
                    val local = t - bt
                    if (local in 0f..0.08f) {
                        val env = exp(-local * 55.0).toFloat()
                        val noise = ((Math.random() * 2 - 1).toFloat() * 0.55f)
                        val click = sin(2.0 * PI * (900.0 + local * 400.0) * local).toFloat() * 0.45f
                        sample += (noise + click) * env
                    }
                }
                // Soft continuous rattle underlay
                val underlay = ((Math.random() * 2 - 1).toFloat() * 0.12f) *
                    ((1f - t / durationSec).coerceAtLeast(0f))
                sample = ((sample + underlay) * volume * 0.75f).coerceIn(-1f, 1f)
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

    /** Soft thud when a player has no legal moves. */
    fun playNoMove() {
        if (!isSoundEnabled || volume <= 0f) return
        scope.launch {
            val durationSec = 0.22f
            val numSamples = (durationSec * SAMPLE_RATE).toInt()
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val envelope = exp(-t * 18.0).toFloat()
                val freq = 180.0 - t * 60.0
                val sample = (sin(2.0 * PI * freq * t).toFloat() * envelope * volume * 0.55f).coerceIn(-1f, 1f)
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    /** Bright chime when a 6 is rolled (bonus turn cue). */
    fun playSixBonus() {
        if (!isSoundEnabled || volume <= 0f) return
        scope.launch {
            val durationSec = 0.32f
            val numSamples = (durationSec * SAMPLE_RATE).toInt()
            val buffer = ShortArray(numSamples)
            val notes = listOf(659.25, 830.61, 987.77) // E5, G#5, B5

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val noteIdx = (t / 0.1f).toInt().coerceIn(0, notes.lastIndex)
                val freq = notes[noteIdx]
                val localT = t - noteIdx * 0.1f
                val env = exp(-localT * 16.0).toFloat()
                val sample = (sin(2.0 * PI * freq * t).toFloat() * env * volume * 0.7f).coerceIn(-1f, 1f)
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
