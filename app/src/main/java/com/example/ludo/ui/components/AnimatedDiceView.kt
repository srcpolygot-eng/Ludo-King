package com.example.ludo.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ludo.ui.theme.LudoBoardPalette
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun AnimatedDiceView(
    diceValue: Int,
    isRolling: Boolean,
    isRollEnabled: Boolean,
    palette: LudoBoardPalette,
    accentColor: Color,
    onRollClicked: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 68.dp
) {
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    var displayedValue by remember { mutableIntStateOf(diceValue) }

    LaunchedEffect(diceValue) {
        if (!isRolling) {
            displayedValue = diceValue
        }
    }

    LaunchedEffect(isRolling) {
        if (isRolling) {
            // Rapid tumble animation
            val rollJob = launch {
                while (true) {
                    displayedValue = Random.nextInt(1, 7)
                    delay(55)
                }
            }
            rotation.animateTo(
                targetValue = rotation.value + 360f,
                animationSpec = tween(durationMillis = 450, easing = LinearEasing)
            )
            scale.animateTo(
                targetValue = 1.18f,
                animationSpec = tween(150, easing = FastOutSlowInEasing)
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(150, easing = FastOutSlowInEasing)
            )
            rollJob.cancel()
            displayedValue = diceValue
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "diceGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val borderModifier = if (isRollEnabled && !isRolling) {
        Modifier.border(
            width = 3.dp,
            color = accentColor.copy(alpha = glowAlpha),
            shape = RoundedCornerShape(16.dp)
        )
    } else Modifier

    var lastClickTimestamp by remember { androidx.compose.runtime.mutableLongStateOf(0L) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .scale(scale.value)
            .rotate(rotation.value)
            .shadow(elevation = if (isRollEnabled) 8.dp else 3.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .then(borderModifier)
            .clickable(
                enabled = isRollEnabled && !isRolling,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            ) {
                val now = System.currentTimeMillis()
                if (now - lastClickTimestamp > 500L) {
                    lastClickTimestamp = now
                    onRollClicked()
                }
            }
            .testTag("animated_dice_button")
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .padding(2.dp)
        ) {
            val w = this.size.width
            val h = this.size.height

            // Dice background with soft gradient
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(palette.diceBg, palette.diceBg.copy(alpha = 0.9f)),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                ),
                topLeft = Offset(0f, 0f),
                size = Size(w, h),
                cornerRadius = CornerRadius(w * 0.22f, h * 0.22f)
            )

            // Outer bevel line
            drawRoundRect(
                color = palette.boardBorder.copy(alpha = 0.25f),
                topLeft = Offset(0f, 0f),
                size = Size(w, h),
                cornerRadius = CornerRadius(w * 0.22f, h * 0.22f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.04f)
            )

            // Pips (Dots)
            drawDicePips(
                value = displayedValue.coerceIn(1, 6),
                width = w,
                height = h,
                pipColor = if (displayedValue == 6) accentColor else palette.dicePip
            )
        }
    }
}

private fun DrawScope.drawDicePips(value: Int, width: Float, height: Float, pipColor: Color) {
    val pipRadius = width * 0.09f
    val left = width * 0.26f
    val center = width * 0.50f
    val right = width * 0.74f

    val top = height * 0.26f
    val middle = height * 0.50f
    val bottom = height * 0.74f

    fun drawPip(x: Float, y: Float) {
        // Subtle pip inner shadow
        drawCircle(
            color = Color.Black.copy(alpha = 0.2f),
            center = Offset(x, y + pipRadius * 0.15f),
            radius = pipRadius
        )
        drawCircle(
            color = pipColor,
            center = Offset(x, y),
            radius = pipRadius
        )
        // Pip specular highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            center = Offset(x - pipRadius * 0.25f, y - pipRadius * 0.25f),
            radius = pipRadius * 0.35f
        )
    }

    when (value) {
        1 -> drawPip(center, middle)
        2 -> {
            drawPip(left, top)
            drawPip(right, bottom)
        }
        3 -> {
            drawPip(left, top)
            drawPip(center, middle)
            drawPip(right, bottom)
        }
        4 -> {
            drawPip(left, top)
            drawPip(right, top)
            drawPip(left, bottom)
            drawPip(right, bottom)
        }
        5 -> {
            drawPip(left, top)
            drawPip(right, top)
            drawPip(center, middle)
            drawPip(left, bottom)
            drawPip(right, bottom)
        }
        6 -> {
            drawPip(left, top)
            drawPip(right, top)
            drawPip(left, middle)
            drawPip(right, middle)
            drawPip(left, bottom)
            drawPip(right, bottom)
        }
    }
}
