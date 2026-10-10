package com.example.ludo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.audio.SoundManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Official Ludo Rules & Guide",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            SoundManager.playButtonClick()
                            onBack()
                        },
                        modifier = Modifier.testTag("rules_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F111A))
            )
        },
        containerColor = Color(0xFF0F111A)
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                RuleSectionCard(
                    title = "Objective of the Game",
                    icon = Icons.Default.Flag,
                    accentColor = Color(0xFF43A047),
                    content = "Each player commands four colored tokens (Red, Green, Yellow, or Blue). The primary objective is to navigate all 4 tokens around the 52-cell outer track and bring them into the central Home triangle before any opponent does."
                )
            }

            item {
                RuleSectionCard(
                    title = "Unlocking from Base",
                    icon = Icons.Default.Casino,
                    accentColor = Color(0xFFE53935),
                    content = "At the start of the match, all four tokens rest in your base quadrant. To deploy a token onto the track's starting square, you must roll a 6 on the die. A roll other than 6 cannot release a token from the base."
                )
            }

            item {
                RuleSectionCard(
                    title = "Clockwise Movement",
                    icon = Icons.Default.SportsEsports,
                    accentColor = Color(0xFF1E88E5),
                    content = "Once on the main track, tokens travel clockwise across 51 squares. After completing the circular circuit, tokens turn inward into their color-coded 5-square home column toward the final center triangle."
                )
            }

            item {
                RuleSectionCard(
                    title = "Captures & Sending to Base",
                    icon = Icons.Default.SportsEsports,
                    accentColor = Color(0xFFFFB300),
                    content = "If your token lands on an opponent's token on a regular track cell, the opponent's token is captured and immediately sent back to its starting base. Capturing also grants you an extra bonus roll!"
                )
            }

            item {
                RuleSectionCard(
                    title = "Safe Squares Protection",
                    icon = Icons.Default.Security,
                    accentColor = Color(0xFF26A69A),
                    content = "Cells with a star emblem and all four colored starting squares are Safe Squares. Tokens resting on safe squares cannot be captured, and tokens of different players can peacefully coexist on these spaces."
                )
            }

            item {
                RuleSectionCard(
                    title = "Exact Roll for Victory",
                    icon = Icons.Default.Flag,
                    accentColor = Color(0xFFAB47BC),
                    content = "To enter the central Home triangle, you must roll the exact number required. For instance, if your token is 2 steps away from Home, rolling a 2 lands it in Home; rolling 3, 4, 5, or 6 cannot be used on that token."
                )
            }

            item {
                RuleSectionCard(
                    title = "Bonus Turns & The 3-Sixes Rule",
                    icon = Icons.Default.Casino,
                    accentColor = Color(0xFFFF7043),
                    content = "You earn an extra turn whenever you:\n1. Roll a 6 on the die.\n2. Capture an opponent's piece.\n3. Bring a token into Home.\n\nHowever, rolling three consecutive sixes forfeits your turn immediately to prevent unfair roll streaks."
                )
            }

            item {
                RuleSectionCard(
                    title = "AI Difficulty Levels",
                    icon = Icons.Default.SmartToy,
                    accentColor = Color(0xFF7E57C2),
                    content = "• Easy: Casual, plays random valid moves.\n• Medium: Focuses on advancing tokens and capturing nearby opponents.\n• Hard: Evaluates immediate threats, uses safe squares, and finishes pieces safely.\n• Expert: Full tactical heuristics including opponent bounties, evasion, danger penalties, and endgame planning."
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun RuleSectionCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D2E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.LightGray.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            )
        }
    }
}
