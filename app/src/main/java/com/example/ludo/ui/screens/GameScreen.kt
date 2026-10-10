package com.example.ludo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ludo.audio.SoundManager
import com.example.ludo.model.BoardTheme
import com.example.ludo.model.GamePhase
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.model.Token
import com.example.ludo.ui.components.AnimatedDiceView
import com.example.ludo.ui.components.ConfettiEffect
import com.example.ludo.ui.components.LudoBoardView
import com.example.ludo.ui.components.PlayerBadgeView
import com.example.ludo.ui.theme.LudoThemePalettes
import com.example.ludo.viewmodel.LudoGameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: LudoGameViewModel,
    onExitToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val palette = LudoThemePalettes.getPalette(uiState.theme)

    var showHistoryDialog by remember { mutableStateOf(false) }
    var showRestartConfirm by remember { mutableStateOf(false) }
    var showExitConfirm by remember { mutableStateOf(false) }

    val activePlayer = uiState.players.getOrElse(uiState.activePlayerIndex) {
        Player(id = 0, name = "Player", color = PlayerColor.RED, isAi = false)
    }
    val activeColor = palette.getPlayerColor(activePlayer.color)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(activeColor)
                        )
                        Text(
                            text = "${activePlayer.name}'s Turn",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            SoundManager.playButtonClick()
                            viewModel.togglePause()
                        },
                        modifier = Modifier.testTag("game_pause_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Speed Toggle
                    IconButton(
                        onClick = {
                            SoundManager.playButtonClick()
                            val nextSpeed = when (uiState.fastForwardSpeed) {
                                1 -> 2
                                2 -> 4
                                else -> 1
                            }
                            viewModel.setFastForwardSpeed(nextSpeed)
                        },
                        modifier = Modifier.testTag("game_speed_button")
                    ) {
                        Text(
                            text = "${uiState.fastForwardSpeed}x",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.fastForwardSpeed > 1) Color(0xFFFFD54F) else Color.White
                            )
                        )
                    }

                    // Sound Toggle
                    IconButton(
                        onClick = { viewModel.toggleSound() },
                        modifier = Modifier.testTag("game_sound_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Sound",
                            tint = Color.White
                        )
                    }

                    // Move History
                    IconButton(
                        onClick = {
                            SoundManager.playButtonClick()
                            showHistoryDialog = true
                        },
                        modifier = Modifier.testTag("game_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "History",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F111A))
            )
        },
        containerColor = Color(0xFF0F111A)
    ) { innerPadding ->
        val redPlayer = uiState.players.find { it.color == PlayerColor.RED }
        val greenPlayer = uiState.players.find { it.color == PlayerColor.GREEN }
        val yellowPlayer = uiState.players.find { it.color == PlayerColor.YELLOW }
        val bluePlayer = uiState.players.find { it.color == PlayerColor.BLUE }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Corner Players (Red on Top-Left, Green on Top-Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (redPlayer != null) {
                    PlayerBadgeView(
                        player = redPlayer,
                        isActive = uiState.players.getOrNull(uiState.activePlayerIndex)?.id == redPlayer.id,
                        palette = palette,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (greenPlayer != null) {
                    PlayerBadgeView(
                        player = greenPlayer,
                        isActive = uiState.players.getOrNull(uiState.activePlayerIndex)?.id == greenPlayer.id,
                        palette = palette,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Interactive Ludo Board
            LudoBoardView(
                players = uiState.players,
                activePlayer = activePlayer,
                legalTokens = uiState.legalTokens,
                theme = uiState.theme,
                animatingToken = uiState.animatingToken,
                capturedReturningTokens = uiState.capturedReturningTokens,
                onTokenClicked = { token ->
                    viewModel.onTokenClicked(token)
                },
                modifier = Modifier.padding(horizontal = 2.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Bottom Corner Players (Blue on Bottom-Left, Yellow on Bottom-Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (bluePlayer != null) {
                    PlayerBadgeView(
                        player = bluePlayer,
                        isActive = uiState.players.getOrNull(uiState.activePlayerIndex)?.id == bluePlayer.id,
                        palette = palette,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (yellowPlayer != null) {
                    PlayerBadgeView(
                        player = yellowPlayer,
                        isActive = uiState.players.getOrNull(uiState.activePlayerIndex)?.id == yellowPlayer.id,
                        palette = palette,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom Action Tray (Dice, Turn info, Roll CTA)
            BottomActionTray(
                activePlayer = activePlayer,
                phase = uiState.phase,
                diceValue = uiState.diceValue,
                isRolling = uiState.isDiceRolling,
                consecutiveSixes = uiState.consecutiveSixes,
                statusMessage = uiState.statusMessage,
                palette = palette,
                activeColor = activeColor,
                onRollClicked = { viewModel.rollDice() }
            )

            Spacer(modifier = Modifier.height(6.dp))
        }
    }

    // Pause Dialog
    if (uiState.isPaused) {
        PauseMenuDialog(
            theme = uiState.theme,
            volume = uiState.soundVolume,
            isSoundEnabled = uiState.isSoundEnabled,
            reducedAnimations = uiState.reducedAnimations,
            onResume = { viewModel.togglePause() },
            onRestart = { showRestartConfirm = true },
            onExit = { showExitConfirm = true },
            onThemeChange = { viewModel.setTheme(it) },
            onVolumeChange = { viewModel.setVolume(it) },
            onToggleReducedAnim = { viewModel.toggleReducedAnimations() }
        )
    }

    // Move History Dialog
    if (showHistoryDialog) {
        HistoryDialog(
            history = uiState.moveHistory,
            onDismiss = { showHistoryDialog = false }
        )
    }

    // Restart Confirmation Dialog
    if (showRestartConfirm) {
        AlertDialog(
            onDismissRequest = { showRestartConfirm = false },
            title = { Text("Restart Match?") },
            text = { Text("Are you sure you want to restart this game from the beginning?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestartConfirm = false
                        viewModel.togglePause()
                        viewModel.startNewGame(
                            mode = uiState.gameMode,
                            playerCount = uiState.players.size,
                            humanPlayerName = uiState.players.firstOrNull()?.name ?: "Player",
                            aiDifficulty = activePlayer.difficulty,
                            theme = uiState.theme,
                            rules = uiState.rules,
                            customPlayerNames = uiState.players.map { it.name }
                        )
                    }
                ) { Text("Restart", color = Color(0xFFE53935)) }
            },
            dismissButton = {
                TextButton(onClick = { showRestartConfirm = false }) { Text("Cancel") }
            }
        )
    }

    // Exit Confirmation Dialog
    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text("Exit to Main Menu?") },
            text = { Text("Current match progress will be lost. Return to menu?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitConfirm = false
                        onExitToHome()
                    }
                ) { Text("Exit", color = Color(0xFFE53935)) }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) { Text("Cancel") }
            }
        )
    }

    // Game Over Victory Dialog
    if (uiState.phase == GamePhase.GAME_OVER && uiState.winner != null) {
        VictoryDialog(
            winner = uiState.winner!!,
            turns = uiState.turnsCount,
            captures = uiState.totalCapturesInMatch,
            palette = palette,
            onPlayAgain = {
                viewModel.startNewGame(
                    mode = uiState.gameMode,
                    playerCount = uiState.players.size,
                    humanPlayerName = uiState.players.firstOrNull()?.name ?: "Player",
                    aiDifficulty = activePlayer.difficulty,
                    theme = uiState.theme,
                    rules = uiState.rules,
                    customPlayerNames = uiState.players.map { it.name }
                )
            },
            onMainMenu = { onExitToHome() }
        )
    }
}

@Composable
private fun BottomActionTray(
    activePlayer: Player,
    phase: GamePhase,
    diceValue: Int,
    isRolling: Boolean,
    consecutiveSixes: Int,
    statusMessage: String,
    palette: com.example.ludo.ui.theme.LudoBoardPalette,
    activeColor: Color,
    onRollClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D2E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Status and instructions
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 13.sp
                        ),
                        maxLines = 2
                    )
                }

                if (phase == GamePhase.SELECT_TOKEN && !activePlayer.isAi) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFFD700).copy(alpha = 0.22f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "👉 Tap a highlighted pawn to move",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                        )
                    }
                }

                if (consecutiveSixes > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Sixes Streak:",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray)
                        )
                        for (i in 0 until consecutiveSixes) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFB300))
                            ) {
                                Text(
                                    text = "6",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right: Animated Dice
            val isRollTurn = (phase == GamePhase.ROLL_DICE && !activePlayer.isAi && !isRolling)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedDiceView(
                    diceValue = diceValue,
                    isRolling = isRolling,
                    isRollEnabled = isRollTurn,
                    palette = palette,
                    accentColor = activeColor,
                    onRollClicked = onRollClicked
                )
                if (isRollTurn) {
                    Text(
                        text = "TAP TO ROLL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = activeColor
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PauseMenuDialog(
    theme: BoardTheme,
    volume: Float,
    isSoundEnabled: Boolean,
    reducedAnimations: Boolean,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit,
    onThemeChange: (BoardTheme) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleReducedAnim: () -> Unit
) {
    Dialog(onDismissRequest = { onResume() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D2E))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Game Paused",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                // Resume CTA
                Button(
                    onClick = {
                        SoundManager.playButtonClick()
                        onResume()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("pause_resume_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Resume Match", fontWeight = FontWeight.Bold)
                }

                // Volume slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Sound Volume",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                        )
                        Text(
                            text = "${(volume * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFFD54F))
                        )
                    }
                    Slider(
                        value = volume,
                        onValueChange = { onVolumeChange(it) },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFD54F),
                            activeTrackColor = Color(0xFFFFD54F)
                        )
                    )
                }

                // Theme Quick Select
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Board Theme",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BoardTheme.entries.forEach { t ->
                            val isSel = t == theme
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF2E3458) else Color(0xFF141724))
                                    .border(
                                        1.dp,
                                        if (isSel) Color(0xFFFFD54F) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        SoundManager.playButtonClick()
                                        onThemeChange(t)
                                    }
                                    .padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = t.displayName.take(5),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = if (isSel) Color(0xFFFFD54F) else Color.Gray
                                    )
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            SoundManager.playButtonClick()
                            onRestart()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("pause_restart_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.LightGray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restart", color = Color.LightGray)
                    }

                    OutlinedButton(
                        onClick = {
                            SoundManager.playButtonClick()
                            onExit()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("pause_exit_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = Color(0xFFE53935))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Exit", color = Color(0xFFE53935))
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryDialog(
    history: List<String>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D2E))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Match Move History",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(history) { log ->
                        Text(
                            text = "• $log",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E3458))
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun VictoryDialog(
    winner: Player,
    turns: Int,
    captures: Int,
    palette: com.example.ludo.ui.theme.LudoBoardPalette,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
        ) {
            ConfettiEffect(modifier = Modifier.matchParentSize())

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141724).copy(alpha = 0.94f)),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Trophy",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Text(
                        text = "VICTORY!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700),
                            letterSpacing = 1.5.sp
                        )
                    )

                    Text(
                        text = "${winner.name} has brought all 4 tokens home and won the match!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    )

                    // Match Stats Mini Grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1A1D2E))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Turns", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                            Text("$turns", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Captures", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                            Text("$captures", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Color", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                            Text(winner.color.displayName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = palette.getPlayerColor(winner.color)))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            SoundManager.playButtonClick()
                            onPlayAgain()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("victory_play_again_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                    ) {
                        Text("Play Again", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            SoundManager.playButtonClick()
                            onMainMenu()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("victory_main_menu_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Back to Main Menu", color = Color.White)
                    }
                }
            }
        }
    }
}
