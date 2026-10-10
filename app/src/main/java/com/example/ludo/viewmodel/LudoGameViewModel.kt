package com.example.ludo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ludo.audio.SoundManager
import com.example.ludo.data.LudoRepository
import com.example.ludo.engine.BonusReason
import com.example.ludo.engine.LudoAiEngine
import com.example.ludo.engine.LudoBoardCoordinates
import com.example.ludo.engine.LudoGameEngine
import com.example.ludo.model.AiDifficulty
import com.example.ludo.model.BoardTheme
import com.example.ludo.model.GameMode
import com.example.ludo.model.GamePhase
import com.example.ludo.model.GameRules
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.model.Token
import com.example.ludo.model.TokenState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class AnimatingTokenState(
    val tokenId: Int,
    val color: PlayerColor,
    val currentCoord: Pair<Float, Float>, // (col, row)
    val hopElevation: Float = 0f, // vertical hop offset in grid units
    val scaleMultiplier: Float = 1.2f
)

data class GameUiState(
    val gameMode: GameMode = GameMode.VS_COMPUTER,
    val players: List<Player> = emptyList(),
    val activePlayerIndex: Int = 0,
    val phase: GamePhase = GamePhase.ROLL_DICE,
    val diceValue: Int = 1,
    val isDiceRolling: Boolean = false,
    val consecutiveSixes: Int = 0,
    val legalTokens: List<Token> = emptyList(),
    val rules: GameRules = GameRules(),
    val theme: BoardTheme = BoardTheme.CLASSIC,
    val isPaused: Boolean = false,
    val isSoundEnabled: Boolean = true,
    val soundVolume: Float = 0.8f,
    val fastForwardSpeed: Int = 1, // 1x, 2x, 4x for Computer-Only or fast play
    val reducedAnimations: Boolean = false,
    val animatingToken: AnimatingTokenState? = null,
    val capturedReturningTokens: List<AnimatingTokenState> = emptyList(),
    val statusMessage: String = "Roll the dice to begin!",
    val moveHistory: List<String> = emptyList(),
    val winner: Player? = null,
    val turnsCount: Int = 0,
    val totalCapturesInMatch: Int = 0
)

class LudoGameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LudoRepository.getInstance(application)
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var aiTurnJob: Job? = null
    private var tokenMoveJob: Job? = null

    // Guard against rapid duplicate taps
    @Volatile
    private var isActionInProgress: Boolean = false

    init {
        viewModelScope.launch {
            repository.initializeDefaults()
        }
    }

    fun startNewGame(
        mode: GameMode,
        playerCount: Int = 4,
        humanPlayerName: String = "Player 1",
        aiDifficulty: AiDifficulty = AiDifficulty.MEDIUM,
        theme: BoardTheme = BoardTheme.CLASSIC,
        rules: GameRules = GameRules(),
        customPlayerNames: List<String> = emptyList()
    ) {
        aiTurnJob?.cancel()
        tokenMoveJob?.cancel()
        isActionInProgress = false

        val colors = listOf(PlayerColor.RED, PlayerColor.GREEN, PlayerColor.YELLOW, PlayerColor.BLUE)
        val selectedColors = when (playerCount) {
            2 -> listOf(PlayerColor.RED, PlayerColor.YELLOW) // Opposite sides for 2 players
            3 -> listOf(PlayerColor.RED, PlayerColor.GREEN, PlayerColor.YELLOW)
            else -> colors
        }

        val playersList = selectedColors.mapIndexed { index, color ->
            val name = when {
                customPlayerNames.size > index && customPlayerNames[index].isNotBlank() ->
                    customPlayerNames[index]
                mode == GameMode.PASS_AND_PLAY ->
                    "Player ${index + 1} (${color.displayName})"
                mode == GameMode.COMPUTER_ONLY ->
                    "Bot ${color.displayName}"
                index == 0 ->
                    humanPlayerName.ifBlank { "You (${color.displayName})" }
                else ->
                    "Bot ${color.displayName}"
            }

            val isAi = when (mode) {
                GameMode.COMPUTER_ONLY -> true
                GameMode.PASS_AND_PLAY -> false
                GameMode.VS_COMPUTER, GameMode.QUICK_GAME -> index != 0
            }

            Player(
                id = index,
                name = name,
                color = color,
                isAi = isAi,
                difficulty = aiDifficulty
            )
        }

        _uiState.value = GameUiState(
            gameMode = mode,
            players = playersList,
            activePlayerIndex = 0,
            phase = GamePhase.ROLL_DICE,
            diceValue = 1,
            consecutiveSixes = 0,
            rules = rules,
            theme = theme,
            statusMessage = "${playersList.first().name}'s turn! Roll the dice.",
            moveHistory = listOf("Game started in ${mode.title} mode.")
        )

        checkAndTriggerAi()
    }

    fun rollDice() {
        if (isActionInProgress) return
        val state = _uiState.value
        if (state.phase != GamePhase.ROLL_DICE || state.isDiceRolling || state.isPaused) return

        isActionInProgress = true
        val activePlayer = state.players[state.activePlayerIndex]

        SoundManager.playDiceRoll()
        _uiState.update { it.copy(isDiceRolling = true) }

        viewModelScope.launch {
            val rollDuration = if (state.reducedAnimations) 120L else (450L / state.fastForwardSpeed)
            delay(rollDuration)

            val roll = Random.nextInt(1, 7)
            val newSixes = if (roll == 6) state.consecutiveSixes + 1 else 0

            // Record six in repository if human
            if (roll == 6 && !activePlayer.isAi) {
                repository.recordSixRolled()
            }

            // Check 3 consecutive sixes rule
            if (newSixes == 3 && state.rules.threeConsecutiveSixesForfeit) {
                _uiState.update {
                    it.copy(
                        diceValue = 6,
                        isDiceRolling = false,
                        consecutiveSixes = 0,
                        phase = GamePhase.TURN_SKIPPED,
                        statusMessage = "3 Sixes in a row! Turn forfeited.",
                        moveHistory = listOf("${activePlayer.name} rolled 3 sixes! Turn lost.") + it.moveHistory
                    )
                }
                delay(900L / state.fastForwardSpeed)
                isActionInProgress = false
                advanceToNextPlayer()
                return@launch
            }

            val legal = LudoGameEngine.getLegalTokens(activePlayer, roll)

            if (legal.isEmpty()) {
                // No legal moves
                _uiState.update {
                    it.copy(
                        diceValue = roll,
                        isDiceRolling = false,
                        consecutiveSixes = newSixes,
                        legalTokens = emptyList(),
                        phase = GamePhase.TURN_SKIPPED,
                        statusMessage = "${activePlayer.name} rolled $roll. No moves!",
                        moveHistory = listOf("${activePlayer.name} rolled $roll. No moves.") + it.moveHistory
                    )
                }
                delay(800L / state.fastForwardSpeed)
                isActionInProgress = false
                advanceToNextPlayer()
            } else {
                // Move selection required
                _uiState.update {
                    it.copy(
                        diceValue = roll,
                        isDiceRolling = false,
                        consecutiveSixes = newSixes,
                        legalTokens = legal,
                        phase = GamePhase.SELECT_TOKEN,
                        statusMessage = "${activePlayer.name} rolled $roll! Pick a token to move."
                    )
                }

                isActionInProgress = false

                // If active player is AI, schedule move
                if (activePlayer.isAi) {
                    scheduleAiMoveSelection()
                }
            }
        }
    }

    fun onTokenClicked(token: Token) {
        if (isActionInProgress) return
        val state = _uiState.value
        if (state.phase != GamePhase.SELECT_TOKEN || state.isPaused) return
        val activePlayer = state.players[state.activePlayerIndex]
        if (activePlayer.isAi) return // Handled by AI engine

        if (!LudoGameEngine.canMoveToken(token, state.diceValue)) return

        // Immediately lock action and advance phase
        isActionInProgress = true
        _uiState.update {
            it.copy(
                phase = GamePhase.MOVING_TOKEN,
                legalTokens = emptyList()
            )
        }

        executeSelectedTokenMove(token)
    }

    private fun executeSelectedTokenMove(token: Token) {
        val state = _uiState.value
        val activePlayer = state.players[state.activePlayerIndex]
        val roll = state.diceValue

        tokenMoveJob?.cancel()
        tokenMoveJob = viewModelScope.launch {
            // Animate step by step
            if (!state.reducedAnimations) {
                if (token.state == TokenState.IN_BASE) {
                    // Smooth hop from base slot to start track square
                    val startCoord = LudoBoardCoordinates.getBaseSlotCenter(token.color, token.id)
                    val targetCoord = LudoBoardCoordinates.getTrackCellCenter(token.color.startTrackIndex)

                    val frames = 4
                    for (f in 1..frames) {
                        val t = f.toFloat() / frames
                        val currX = startCoord.first + (targetCoord.first - startCoord.first) * t
                        val currY = startCoord.second + (targetCoord.second - startCoord.second) * t
                        val hop = kotlin.math.sin(t * Math.PI).toFloat() * 0.45f
                        _uiState.update {
                            it.copy(
                                animatingToken = AnimatingTokenState(
                                    tokenId = token.id,
                                    color = token.color,
                                    currentCoord = Pair(currX, currY),
                                    hopElevation = hop,
                                    scaleMultiplier = 1.25f
                                )
                            )
                        }
                        delay(35L / state.fastForwardSpeed)
                    }
                    SoundManager.playTokenMove()
                } else {
                    // Step-by-step physical pawn hop along track / home path
                    val startStep = token.stepCount
                    val endStep = startStep + roll
                    for (step in (startStep + 1)..endStep) {
                        val prevCoord = getStepExactCenter(token.color, token.id, step - 1)
                        val nextCoord = getStepExactCenter(token.color, token.id, step)

                        val subFrames = 3
                        for (sf in 1..subFrames) {
                            val t = sf.toFloat() / subFrames
                            val currX = prevCoord.first + (nextCoord.first - prevCoord.first) * t
                            val currY = prevCoord.second + (nextCoord.second - prevCoord.second) * t
                            val hop = kotlin.math.sin(t * Math.PI).toFloat() * 0.35f
                            _uiState.update {
                                it.copy(
                                    animatingToken = AnimatingTokenState(
                                        tokenId = token.id,
                                        color = token.color,
                                        currentCoord = Pair(currX, currY),
                                        hopElevation = hop,
                                        scaleMultiplier = 1.2f
                                    )
                                )
                            }
                            delay(28L / state.fastForwardSpeed)
                        }
                        SoundManager.playTokenMove()
                    }
                }
            } else {
                SoundManager.playTokenMove()
            }

            _uiState.update { it.copy(animatingToken = null) }

            // Resolve game engine logic
            val result = LudoGameEngine.executeMove(
                activePlayer = activePlayer,
                tokenToMove = token,
                diceRoll = roll,
                allPlayers = state.players,
                rules = state.rules
            )

            // Animate captured tokens returning to base if any
            if (!state.reducedAnimations && result.capturedTokens.isNotEmpty()) {
                SoundManager.playCapture()
                val landingTrack = result.movedToken.trackIndex
                val captureCellCoord = LudoBoardCoordinates.getTrackCellCenter(landingTrack)

                val frames = 5
                for (f in 1..frames) {
                    val t = f.toFloat() / frames
                    val returningList = result.capturedTokens.map { cap ->
                        val baseSlotCoord = LudoBoardCoordinates.getBaseSlotCenter(cap.color, cap.id)
                        val currX = captureCellCoord.first + (baseSlotCoord.first - captureCellCoord.first) * t
                        val currY = captureCellCoord.second + (baseSlotCoord.second - captureCellCoord.second) * t
                        val hop = kotlin.math.sin(t * Math.PI).toFloat() * 0.5f
                        AnimatingTokenState(
                            tokenId = cap.id,
                            color = cap.color,
                            currentCoord = Pair(currX, currY),
                            hopElevation = hop,
                            scaleMultiplier = 1.15f
                        )
                    }
                    _uiState.update { it.copy(capturedReturningTokens = returningList) }
                    delay(30L / state.fastForwardSpeed)
                }
                _uiState.update { it.copy(capturedReturningTokens = emptyList()) }
            }

            // Update players with moved and captured tokens
            val newPlayers = state.players.map { player ->
                if (player.id == activePlayer.id) {
                    val updatedTokens = player.tokens.map {
                        if (it.id == token.id) result.movedToken else it
                    }
                    player.copy(tokens = updatedTokens)
                } else {
                    val updatedTokens = player.tokens.map { oppToken ->
                        val captured = result.capturedTokens.find { it.id == oppToken.id && it.color == oppToken.color }
                        captured ?: oppToken
                    }
                    player.copy(tokens = updatedTokens)
                }
            }

            val updatedActivePlayer = newPlayers[state.activePlayerIndex]
            var totalCaptures = state.totalCapturesInMatch
            var historyLog = state.moveHistory

            if (result.capturedTokens.isNotEmpty()) {
                if (state.reducedAnimations) SoundManager.playCapture()
                totalCaptures += result.capturedTokens.size
                val oppColor = result.capturedTokens.first().color.displayName
                historyLog = listOf("${activePlayer.name} captured $oppColor's token!") + historyLog
                if (!activePlayer.isAi) {
                    repository.recordTokensCaptured(result.capturedTokens.size)
                }
            }

            if (result.reachedHome) {
                SoundManager.playReachedHome()
                historyLog = listOf("${activePlayer.name} brought a token HOME!") + historyLog
                if (!activePlayer.isAi) {
                    repository.recordTokenReachedHome()
                }
            }

            // Check Winner
            if (updatedActivePlayer.hasWon) {
                isActionInProgress = false
                handleGameOver(updatedActivePlayer, newPlayers, totalCaptures)
                return@launch
            }

            // Check Bonus Turn
            if (result.getsBonusTurn) {
                val bonusMsg = when (result.bonusReason) {
                    BonusReason.ROLLED_SIX -> "Rolled a 6! Roll again."
                    BonusReason.CAPTURED_OPPONENT -> "Captured opponent! Bonus roll!"
                    BonusReason.REACHED_HOME -> "Token Home! Bonus roll!"
                    null -> "Bonus turn!"
                }

                _uiState.update {
                    it.copy(
                        players = newPlayers,
                        phase = GamePhase.ROLL_DICE,
                        statusMessage = "${activePlayer.name}: $bonusMsg",
                        totalCapturesInMatch = totalCaptures,
                        moveHistory = listOf("${activePlayer.name} earned a bonus roll.") + historyLog,
                        turnsCount = it.turnsCount + 1
                    )
                }

                isActionInProgress = false
                checkAndTriggerAi()
            } else {
                _uiState.update {
                    it.copy(
                        players = newPlayers,
                        totalCapturesInMatch = totalCaptures,
                        moveHistory = historyLog,
                        turnsCount = it.turnsCount + 1
                    )
                }
                isActionInProgress = false
                advanceToNextPlayer()
            }
        }
    }

    private fun advanceToNextPlayer() {
        val state = _uiState.value
        val nextIdx = LudoGameEngine.getNextPlayerIndex(state.activePlayerIndex, state.players)
        val nextPlayer = state.players[nextIdx]

        _uiState.update {
            it.copy(
                activePlayerIndex = nextIdx,
                phase = GamePhase.ROLL_DICE,
                consecutiveSixes = 0,
                legalTokens = emptyList(),
                statusMessage = "${nextPlayer.name}'s turn. Roll the dice!"
            )
        }

        checkAndTriggerAi()
    }

    private fun checkAndTriggerAi() {
        aiTurnJob?.cancel()
        val state = _uiState.value
        if (state.phase == GamePhase.GAME_OVER || state.isPaused) return

        val activePlayer = state.players[state.activePlayerIndex]
        if (activePlayer.isAi) {
            aiTurnJob = viewModelScope.launch {
                delay(650L / state.fastForwardSpeed)
                rollDice()
            }
        }
    }

    private fun scheduleAiMoveSelection() {
        val state = _uiState.value
        val activePlayer = state.players[state.activePlayerIndex]
        if (!activePlayer.isAi) return

        viewModelScope.launch {
            delay(550L / state.fastForwardSpeed)
            val selected = LudoAiEngine.selectTokenToMove(
                aiPlayer = activePlayer,
                diceRoll = state.diceValue,
                allPlayers = state.players,
                rules = state.rules,
                difficulty = activePlayer.difficulty
            )

            if (selected != null) {
                isActionInProgress = true
                _uiState.update {
                    it.copy(
                        phase = GamePhase.MOVING_TOKEN,
                        legalTokens = emptyList()
                    )
                }
                executeSelectedTokenMove(selected)
            } else {
                advanceToNextPlayer()
            }
        }
    }

    private fun handleGameOver(winner: Player, players: List<Player>, totalCaptures: Int) {
        SoundManager.playVictory()
        val state = _uiState.value

        _uiState.update {
            it.copy(
                players = players,
                winner = winner,
                phase = GamePhase.GAME_OVER,
                statusMessage = "🎉 ${winner.name} WINS THE GAME! 🎉",
                moveHistory = listOf("🏆 ${winner.name} won the match!") + it.moveHistory
            )
        }

        val humanPlayer = players.find { !it.isAi }
        val isHumanWinner = humanPlayer != null && humanPlayer.id == winner.id
        val aiDiff = players.find { it.isAi }?.difficulty ?: AiDifficulty.MEDIUM

        viewModelScope.launch {
            repository.recordMatchFinished(
                mode = state.gameMode.name,
                winnerName = winner.name,
                winnerColor = winner.color.displayName,
                isHumanWinner = isHumanWinner,
                playerCount = players.size,
                aiDifficulty = aiDiff,
                turnsCount = state.turnsCount + 1,
                tokensCaptured = totalCaptures
            )
        }
    }

    private fun getStepExactCenter(color: PlayerColor, tokenId: Int, step: Int): Pair<Float, Float> {
        return when {
            step <= 0 -> LudoBoardCoordinates.getBaseSlotCenter(color, tokenId)
            step in 1..51 -> {
                val trackIdx = (color.startTrackIndex + (step - 1)) % 52
                LudoBoardCoordinates.getTrackCellCenter(trackIdx)
            }
            step in 52..56 -> {
                LudoBoardCoordinates.getHomePathCenter(color, step - 52)
            }
            else -> {
                LudoBoardCoordinates.getHomeTriangleSlotCenter(color, tokenId)
            }
        }
    }

    fun togglePause() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
        if (!_uiState.value.isPaused) {
            checkAndTriggerAi()
        }
    }

    fun setFastForwardSpeed(speed: Int) {
        _uiState.update { it.copy(fastForwardSpeed = speed.coerceIn(1, 4)) }
    }

    fun setTheme(theme: BoardTheme) {
        _uiState.update { it.copy(theme = theme) }
    }

    fun toggleSound() {
        val newSound = !_uiState.value.isSoundEnabled
        SoundManager.isSoundEnabled = newSound
        _uiState.update { it.copy(isSoundEnabled = newSound) }
    }

    fun setVolume(volume: Float) {
        SoundManager.volume = volume.coerceIn(0f, 1f)
        _uiState.update { it.copy(soundVolume = volume) }
    }

    fun toggleReducedAnimations() {
        _uiState.update { it.copy(reducedAnimations = !it.reducedAnimations) }
    }
}
