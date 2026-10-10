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
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(activeColor))
                        Text(text = "${activePlayer.name}'s Turn", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { SoundManager.playButtonClick(); viewModel.togglePause() }, modifier = Modifier.testTag("game_pause_button")) {
                        Icon(imageVector = if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = "Pause", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        SoundManager.playButtonClick()
                        val nextSpeed = when (uiState.fastForwardSpeed) { 1 -> 2; 2 -> 4; else -> 1 }
                        viewModel.setFastForwardSpeed(nextSpeed)
                    }, modifier = Modifier.testTag("game_speed_button")) {
                        Text(text = "${uiState.fastForwardSpeed}x", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = if (uiState.fastForwardSpeed > 1) Color(0xFFFFD54F) else Color.White))
                    }
                    IconButton(onClick = { viewModel.toggleSound() }, modifier = Modifier.testTag("game_sound_button")) {
                        Icon(imageVector = if (uiState.isSoundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute, contentDescription = "Sound", tint = Color.White)
                    }
                    IconButton(onClick = { SoundManager.playButtonClick(); showHistoryDialog = true }, modifier = Modifier.testTag("game_history_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = "History", tint = Color.White)
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
            modifier = modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (redPlayer != null) {
                    val isRedActive = uiState.players.getOrNull(uiState.activePlayerIndex)?.id == redPlayer.id
                    PlayerBadgeView(player = redPlayer, isActive = isRedActive, palette = palette, modifier = Modifier.weight(1f), diceValue = uiState.diceValue, isDiceRolling = uiState.isDiceRolling, showDice = isRedActive)
                } else Spacer(modifier = Modifier.weight(1f))
                if (greenPlayer != null) {
                    val isGreenActive = uiState.players.getOrNull(uiState.activePlayerIndex)?.id == greenPlayer.id
                    PlayerBadgeView(player = greenPlayer, isActive = isGreenActive, palette = palette, modifier = Modifier.weight(1f), diceValue = uiState.diceValue, isDiceRolling = uiState.isDiceRolling, showDice = isGreenActive)
                } else Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(4.dp))
            LudoBoardView(players = uiState.players, activePlayer = activePlayer, legalTokens = uiState.legalTokens, theme = uiState.theme, animatingToken = uiState.animatingToken, capturedReturningTokens = uiState.capturedReturningTokens, onTokenClicked = { viewModel.onTokenClicked(it) }, modifier = Modifier.padding(horizontal = 2.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (bluePlayer != null) {
                    val isBlueActive = uiState.players.getOrNull(uiState.activePlayerIndex)?.id == bluePlayer.id
                    PlayerBadgeView(player = bluePlayer, isActive = isBlueActive, palette = palette, modifier = Modifier.weight(1f), diceValue = uiState.diceValue, isDiceRolling = uiState.isDiceRolling, showDice = isBlueActive)
                } else Spacer(modifier = Modifier.weight(1f))
                if (yellowPlayer != null) {
                    val isYellowActive = uiState.players.getOrNull(uiState.activePlayerIndex)?.id == yellowPlayer.id
                    PlayerBadgeView(player = yellowPlayer, isActive = isYellowActive, palette = palette, modifier = Modifier.weight(1f), diceValue = uiState.diceValue, isDiceRolling = uiState.isDiceRolling, showDice = isYellowActive)
                } else Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(6.dp))
            BottomActionTray(activePlayer = activePlayer, phase = uiState.phase, diceValue = uiState.diceValue, isRolling = uiState.isDiceRolling, consecutiveSixes = uiState.consecutiveSixes, statusMessage = uiState.statusMessage, palette = palette, activeColor = activeColor, onRollClicked = { viewModel.rollDice() })
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
    if (uiState.isPaused) {
        PauseMenuDialog(theme = uiState.theme, volume = uiState.soundVolume, isSoundEnabled = uiState.isSoundEnabled, reducedAnimations = uiState.reducedAnimations, onResume = { viewModel.togglePause() }, onRestart = { showRestartConfirm = true }, onExit = { showExitConfirm = true }, onThemeChange = { viewModel.setTheme(it) }, onVolumeChange = { viewModel.setVolume(it) }, onToggleReducedAnim = { viewModel.toggleReducedAnimations() })
    }
    if (showHistoryDialog) HistoryDialog(history = uiState.moveHistory, onDismiss = { showHistoryDialog = false })
    if (showRestartConfirm) {
        AlertDialog(onDismissRequest = { showRestartConfirm = false }, title = { Text("Restart Match?") }, text = { Text("Are you sure you want to restart this game from the beginning?") }, confirmButton = {
            TextButton(onClick = {
                showRestartConfirm = false; viewModel.togglePause()
                viewModel.startNewGame(mode = uiState.gameMode, playerCount = uiState.players.size, humanPlayerName = uiState.players.firstOrNull()?.name ?: "Player", aiDifficulty = activePlayer.difficulty, theme = uiState.theme, rules = uiState.rules, customPlayerNames = uiState.players.map { it.name })
            }) { Text("Restart", color = Color(0xFFE53935)) }
        }, dismissButton = { TextButton(onClick = { showRestartConfirm = false }) { Text("Cancel") } })
    }
    if (showExitConfirm) {
        AlertDialog(onDismissRequest = { showExitConfirm = false }, title = { Text("Exit to Main Menu?") }, text = { Text("Current match progress will be lost. Return to menu?") }, confirmButton = {
            TextButton(onClick = { showExitConfirm = false; onExitToHome() }) { Text("Exit", color = Color(0xFFE53935)) }
        }, dismissButton = { TextButton(onClick = { showExitConfirm = false }) { Text("Cancel") } })
    }
    if (uiState.phase == GamePhase.GAME_OVER && uiState.winner != null) {
        VictoryDialog(winner = uiState.winner!!, turns = uiState.turnsCount, captures = uiState.totalCapturesInMatch, palette = palette, onPlayAgain = {
            viewModel.startNewGame(mode = uiState.gameMode, playerCount = uiState.players.size, humanPlayerName = uiState.players.firstOrNull()?.name ?: "Player", aiDifficulty = activePlayer.difficulty, theme = uiState.theme, rules = uiState.rules, customPlayerNames = uiState.players.map { it.name })
        }, onMainMenu = { onExitToHome() })
    }
}
