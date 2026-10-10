package com.example.ludo.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val xRatio: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val phase: Float
)

@Composable
fun ConfettiEffect(modifier: Modifier = Modifier) {
    val particles = remember {
        val colors = listOf(
            Color(0xFFE53935), Color(0xFF43A047), Color(0xFFFDD835),
            Color(0xFF1E88E5), Color(0xFFFF4081), Color(0xFF00E5FF)
        )
        List(45) {
            ConfettiParticle(
                xRatio = Random.nextFloat(),
                speed = 0.5f + Random.nextFloat() * 0.7f,
                size = 12f + Random.nextFloat() * 16f,
                color = colors.random(),
                phase = Random.nextFloat() * 1000f
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "confetti")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiProgress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        for (p in particles) {
            val y = (((progress * p.speed + (p.phase / 1000f)) % 1f) * height)
            val wobble = sin((progress * 8.0 + p.phase.toDouble())).toFloat() * 20f
            val x = (p.xRatio * width) + wobble
            drawRect(
                color = p.color,
                topLeft = Offset(x, y),
                size = Size(p.size, p.size * 0.6f)
            )
        }
    }
}
