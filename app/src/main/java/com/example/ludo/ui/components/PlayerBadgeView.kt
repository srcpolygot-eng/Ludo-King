package com.example.ludo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.model.Player
import com.example.ludo.model.TokenState
import com.example.ludo.ui.theme.LudoBoardPalette

@Composable
fun PlayerBadgeView(
    player: Player,
    isActive: Boolean,
    palette: LudoBoardPalette,
    modifier: Modifier = Modifier
) {
    val playerColor = palette.getPlayerColor(player.color)

    val infiniteTransition = rememberInfiniteTransition(label = "badgePulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val animatedBg by animateColorAsState(
        targetValue = if (isActive) Color(0xFF1E2338) else Color(0xFF151824),
        animationSpec = tween(300),
        label = "playerBadgeBg"
    )

    val borderModifier = if (isActive) {
        Modifier.border(
            width = 2.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    playerColor.copy(alpha = pulseGlow),
                    Color(0xFFFFD700).copy(alpha = pulseGlow),
                    playerColor.copy(alpha = pulseGlow)
                )
            ),
            shape = RoundedCornerShape(14.dp)
        )
    } else {
        Modifier.border(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.08f),
            shape = RoundedCornerShape(14.dp)
        )
    }

    Card(
        modifier = modifier
            .shadow(if (isActive) 8.dp else 2.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .then(borderModifier),
        colors = CardDefaults.cardColors(containerColor = animatedBg)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Avatar & Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(playerColor.copy(alpha = 0.9f), playerColor),
                                radius = 40f
                            )
                        )
                        .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (player.isAi) Icons.Default.Computer else Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isActive) Color.White else Color.LightGray,
                                fontSize = 13.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (player.hasWon) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Won",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    if (isActive) {
                        Text(
                            text = if (player.isAi) "THINKING..." else "ACTIVE TURN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = playerColor
                            )
                        )
                    } else if (player.isAi) {
                        Text(
                            text = player.difficulty.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Token Progress Dots (Base / Track / Home)
            Column(horizontalAlignment = Alignment.End) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    player.tokens.forEach { token ->
                        val dotColor = when (token.state) {
                            TokenState.HOME -> Color(0xFFFFD700) // Gold
                            TokenState.IN_BASE -> Color.Gray.copy(alpha = 0.35f)
                            else -> playerColor // Active on track or path
                        }
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                                .border(0.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${player.tokensHomeCount}/4 HOME",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        color = Color.LightGray.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}
