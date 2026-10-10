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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.audio.SoundManager
import com.example.ludo.data.MatchRecordEntity
import com.example.ludo.viewmodel.StatsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    statsViewModel: StatsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by statsViewModel.stats.collectAsState()
    val recentMatches by statsViewModel.recentMatches.collectAsState()
    var showResetConfirm by remember { mutableStateOf(false) }

    val matchesPlayed = stats?.matchesPlayed ?: 0
    val matchesWon = stats?.matchesWon ?: 0
    val matchesLost = stats?.matchesLost ?: 0
    val winRate = if (matchesPlayed > 0) (matchesWon * 100 / matchesPlayed) else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Game Statistics",
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
                        modifier = Modifier.testTag("stats_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showResetConfirm = true },
                        modifier = Modifier.testTag("stats_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Reset",
                            tint = Color.Gray
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
            // Summary Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Matches",
                        value = "$matchesPlayed",
                        color = Color(0xFF1E88E5),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Victories",
                        value = "$matchesWon",
                        color = Color(0xFF43A047),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Win Rate",
                        value = "$winRate%",
                        color = Color(0xFFFFB300),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Streak & In-Game Actions
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D2E)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Gameplay Highlights",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            DetailItem(title = "Tokens Captured", value = "${stats?.tokensCaptured ?: 0}")
                            DetailItem(title = "Tokens Home", value = "${stats?.tokensHome ?: 0}")
                            DetailItem(title = "Total 6s Rolled", value = "${stats?.totalSixesRolled ?: 0}")
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            DetailItem(title = "Current Streak", value = "${stats?.currentStreak ?: 0}")
                            DetailItem(title = "Best Streak", value = "${stats?.bestStreak ?: 0}")
                            DetailItem(title = "Losses", value = "$matchesLost")
                        }
                    }
                }
            }

            // Wins by AI Difficulty
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D2E)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Wins by AI Difficulty",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        DifficultyWinRow("Easy AI", stats?.easyWins ?: 0, Color(0xFF81C784))
                        DifficultyWinRow("Medium AI", stats?.mediumWins ?: 0, Color(0xFF64B5F6))
                        DifficultyWinRow("Hard AI", stats?.hardWins ?: 0, Color(0xFFFFB74D))
                        DifficultyWinRow("Expert AI", stats?.expertWins ?: 0, Color(0xFFE57373))
                    }
                }
            }

            // Recent Matches Header
            item {
                Text(
                    text = "Recent Matches",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (recentMatches.isEmpty()) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    ) {
                        Text(
                            text = "No matches recorded yet. Play a game to see records!",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                        )
                    }
                }
            } else {
                items(recentMatches) { match ->
                    MatchRecordItem(match)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset All Statistics?") },
            text = { Text("This will permanently clear your match records and reset win counts.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        statsViewModel.resetData()
                        showResetConfirm = false
                    }
                ) { Text("Reset", color = Color(0xFFE53935)) }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D2E)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            )
        }
    }
}

@Composable
private fun DetailItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 11.sp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}

@Composable
private fun DifficultyWinRow(label: String, wins: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray))
        }
        Text(
            "$wins wins",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}

@Composable
private fun MatchRecordItem(match: MatchRecordEntity) {
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
    val dateString = remember(match.timestamp) { dateFormat.format(Date(match.timestamp)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D2E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (match.isHumanWinner) "VICTORY" else "DEFEAT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (match.isHumanWinner) Color(0xFF43A047) else Color(0xFFE53935)
                        )
                    )
                    Text(
                        text = " • ${match.mode}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                    )
                }
                Text(
                    text = "Winner: ${match.winnerName} (${match.winnerColor})",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
                Text(
                    text = "$dateString • ${match.turnsCount} turns • ${match.tokensCaptured} captures",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                )
            }
        }
    }
}
