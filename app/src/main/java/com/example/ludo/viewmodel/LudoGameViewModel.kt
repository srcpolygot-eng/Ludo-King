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
import com.example.ludo.model.PowerUp
import com.example.ludo.model.PowerUpType
import com.example.ludo.model.Team
import com.example.ludo.model.Teams
import com.example.ludo.model.Token
import com.example.ludo.model.TokenSkin
import com.example.ludo.model.TokenState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

data class AnimatingTokenState(
    val tokenId: Int,
    val color: PlayerColor,
    val currentCoord: Pair<Float, Float>,
    val hopElevation: Float = 0f,
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
    val fastForwardSpeed: Int = 1,
    val reducedAnimations: Boolean = false,
    val animatingToken: AnimatingTokenState? = null,
    val capturedReturningTokens: List<AnimatingTokenState> = emptyList(),
    val statusMessage: String = "Roll the dice to begin!",
    val moveHistory: List<String> = emptyList(),
    val winner: Player? = null,
    val turnsCount: Int = 0,
    val totalCapturesInMatch: Int = 0,
    val turnTimeRemaining: Int = 0,
    val isTimerRunning: Boolean = false,
    val selectedTokenSkin: TokenSkin = TokenSkin.CLASSIC,
    val availablePowerUps: List<PowerUp> = emptyList(),
    val lastPowerUpUsed: PowerUpType? = null,
    val isTeamMode: Boolean = false,
    val winningTeam: Team? = null
)

class LudoGameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LudoRepository.getInstance(application)
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var aiTurnJob: Job? = null
    private var tokenMoveJob: Job? = null
    private var timerJob: Job? = null

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
        customPlayerNames: List<String> = emptyList(),
        tokenSkin: TokenSkin = TokenSkin.CLASSIC
    ) {
        aiTurnJob?.cancel()
        tokenMoveJob?.cancel()
        timerJob?.cancel()
        isActionInProgress = false

        val isTeam = mode == GameMode.TEAM_MODE
        val isArcade = mode == GameMode.ARCADE
        val effectiveRules = when {
            isTeam -> rules.copy(teamModeEnabled = true)
            isArcade -> rules.copy(
                powerUpsEnabled = true,
                turnTimerEnabled = true,
                turnTimerSeconds = rules.turnTimerSeconds.coerceIn(15, 60)
            )
            else -> rules
        }

        val colors = listOf(PlayerColor.RED, PlayerColor.GREEN, PlayerColor.YELLOW, PlayerColor.BLUE)
        val selectedColors = when {
            isTeam -> colors
            playerCount == 2 -> listOf(PlayerColor.RED, PlayerColor.YELLOW)
            playerCount == 3 -> listOf(PlayerColor.RED, PlayerColor.GREEN, PlayerColor.YELLOW)
            else -> colors
        }

        val starterPowerUps = if (isArcade) {
            listOf(
                PowerUp(PowerUpType.DOUBLE_DICE, 1),
                PowerUp(PowerUpType.SHIELD, 1),
                PowerUp(PowerUpType.BOOST, 1)
            )
        } else emptyList()

        val playersList = selectedColors.mapIndexed { index, color ->
            val name = when {
                customPlayerNames.size > index && customPlayerNames[index].isNotBlank() ->
                    customPlayerNames[index]
                mode == GameMode.PASS_AND_PLAY ->
                    "Player ${index + 1} (${color.displayName})"
                mode == GameMode.COMPUTER_ONLY ->
                    "Bot ${color.displayName}"
                mode == GameMode.TEAM_MODE -> {
                    val teamName = Teams.teamFor(color).name
                    if (index == 0 || index == 2) humanPlayerName.ifBlank { "You" } + " (${color.displayName})"
                    else "Bot ${color.displayName} [$teamName]"
                }
                index == 0 ->
                    humanPlayerName.ifBlank { "You (${color.displayName})" }
                else ->
                    "Bot ${color.displayName}"
            }

            val isAi = when (mode) {
                GameMode.COMPUTER_ONLY -> true
                GameMode.PASS_AND_PLAY -> false
                GameMode.TEAM_MODE -> index != 0 && index != 2
                GameMode.VS_COMPUTER, GameMode.QUICK_GAME, GameMode.ARCADE -> index != 0
            }

            Player(
                id = index,
                name = name,
                color = color,
                isAi = isAi,
                difficulty = aiDifficulty,
                tokenSkin = tokenSkin,
                powerUps = if (!isAi && isArcade) starterPowerUps else emptyList()
            )
        }

        val startMsg = when {
            isTeam -> "Team Mode! ${Teams.TEAM_A.name} vs ${Teams.TEAM_B.name}"
            isArcade -> "Arcade Mode — power-ups & timer armed!"
            else -> "${playersList.first().name}'s turn! Roll the dice."
        }

        _uiState.value = GameUiState(
            gameMode = mode,
            players = playersList,
            activePlayerIndex = 0,
            phase = GamePhase.ROLL_DICE,
            diceValue = 1,
            consecutiveSixes = 0,
            rules = effectiveRules,
            theme = theme,
            statusMessage = startMsg,
            moveHistory = listOf("Game started in ${mode.title} mode."),
            selectedTokenSkin = tokenSkin,
            availablePowerUps = if (isArcade) starterPowerUps else emptyList(),
            isTeamMode = isTeam,
            turnTimeRemaining = if (effectiveRules.turnTimerEnabled) effectiveRules.turnTimerSeconds else 0,
            isTimerRunning = false
        )

        if (effectiveRules.turnTimerEnabled) startTurnTimer()
        checkAndTriggerAi()
    }

    private fun startTurnTimer() {
        timerJob?.cancel()
        val state = _uiState.value
        if (!state.rules.turnTimerEnabled || state.phase == GamePhase.GAME_OVER) return

        val seconds = state.rules.turnTimerSeconds
        _uiState.update { it.copy(turnTimeRemaining = seconds, isTimerRunning = true) }

        timerJob = viewModelScope.launch {
            var remaining = seconds
            while (isActive && remaining > 0) {
                delay(1000L)
                if (_uiState.value.isPaused) continue
                remaining--
                _uiState.update { it.copy(turnTimeRemaining = remaining) }
            }
            if (isActive && remaining <= 0 && _uiState.value.phase != GamePhase.GAME_OVER) {
                SoundManager.playNoMove()
                _uiState.update {
                    it.copy(
                        isTimerRunning = false,
                        phase = GamePhase.TURN_SKIPPED,
                        statusMessage = "Time's up! Turn forfeited.",
                        moveHistory = listOf("⏱ Turn timed out.") + it.moveHistory
                    )
                }
                delay(700L)
                isActionInProgress = false
                advanceToNextPlayer()
            }
        }
    }

    private fun stopTurnTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isTimerRunning = false) }
    }

    fun setTokenSkin(skin: TokenSkin) {
        _uiState.update { state ->
            state.copy(
                selectedTokenSkin = skin,
                players = state.players.map { p ->
                    if (!p.isAi) p.copy(tokenSkin = skin) else p
                }
            )
        }
    }

    fun usePowerUp(type: PowerUpType) {
        if (isActionInProgress) return
        val state = _uiState.value
        if (!state.rules.powerUpsEnabled) return
        val active = state.players.getOrNull(state.activePlayerIndex) ?: return
        if (active.isAi) return

        val powerUp = active.powerUps.find { it.type == type && it.charges > 0 } ?: return
        val updatedPowerUps = active.powerUps.map {
            if (it.type == type) it.copy(charges = it.charges - 1) else it
        }.filter { it.charges > 0 }

        var updatedPlayer = active.copy(powerUps = updatedPowerUps)
        var status = "${active.name} used ${type.displayName}!"

        when (type) {
            PowerUpType.LUCKY_SIX -> {
                updatedPlayer = updatedPlayer.copy(luckySixPending = true)
                status = "${active.name} activated Lucky Six!"
            }
            PowerUpType.BOOST -> {
                updatedPlayer = updatedPlayer.copy(boostPending = true)
                status = "${active.name} activated Boost (+2)!"
            }
            PowerUpType.SHIELD -> {
                val onTrack = active.tokens.filter { it.state == TokenState.ON_TRACK || it.state == TokenState.IN_HOME_PATH }
                if (onTrack.isNotEmpty()) {
                    updatedPlayer = updatedPlayer.copy(shieldedTokenIds = setOf(onTrack.first().id))
                    status = "${active.name} shielded a token!"
                }
            }
            PowerUpType.DOUBLE_DICE -> status = "${active.name} will roll two dice next!"
            PowerUpType.FREEZE -> status = "${active.name} froze the next opponent!"
            PowerUpType.SWAP -> status = "${active.name} ready to swap tokens!"
        }

        val newPlayers = state.players.map { if (it.id == active.id) updatedPlayer else it }
        _uiState.update {
            it.copy(
                players = newPlayers,
                availablePowerUps = updatedPowerUps,
                lastPowerUpUsed = type,
                statusMessage = status,
                moveHistory = listOf("${type.emoji} $status") + it.moveHistory
            )
        }
        SoundManager.playSixBonus()
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

            val roll = when {
                activePlayer.luckySixPending -> 6
                state.lastPowerUpUsed == PowerUpType.DOUBLE_DICE ->
                    maxOf(Random.nextInt(1, 7), Random.nextInt(1, 7))
                else -> Random.nextInt(1, 7)
            }
            val newSixes = if (roll == 6) state.consecutiveSixes + 1 else 0

            if (activePlayer.luckySixPending) {
                val cleared = state.players.map {
                    if (it.id == activePlayer.id) it.copy(luckySixPending = false) else it
                }
                _uiState.update { it.copy(players = cleared, lastPowerUpUsed = null) }
            }

            if (roll == 6 && !activePlayer.isAi) {
                repository.recordSixRolled()
            }

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
                SoundManager.playNoMove()
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
                if (roll == 6) SoundManager.playSixBonus()

                val shouldAutoMove = legal.size == 1 && !activePlayer.isAi

                _uiState.update {
                    it.copy(
                        diceValue = roll,
                        isDiceRolling = false,
                        consecutiveSixes = newSixes,
                        legalTokens = if (shouldAutoMove) emptyList() else legal,
                        phase = if (shouldAutoMove) GamePhase.MOVING_TOKEN else GamePhase.SELECT_TOKEN,
                        statusMessage = when {
                            shouldAutoMove -> "${activePlayer.name} rolled $roll — moving token…"
                            else -> "${activePlayer.name} rolled $roll! Pick a token to move."
                        }
                    )
                }

                if (shouldAutoMove) {
                    executeSelectedTokenMove(legal.first())
                } else {
                    isActionInProgress = false
                    if (activePlayer.isAi) scheduleAiMoveSelection()
                }
            }
        }
    }

    fun onTokenClicked(token: Token) {
        if (isActionInProgress) return
        val state = _uiState.value
        if (state.phase != GamePhase.SELECT_TOKEN || state.isPaused) return
        val activePlayer = state.players[state.activePlayerIndex]
        if (activePlayer.isAi) return

        if (!LudoGameEngine.canMoveToken(token, state.diceValue)) return

        isActionInProgress = true
        _uiState.update {
            it.copy(phase = GamePhase.MOVING_TOKEN, legalTokens = emptyList())
        }
        executeSelectedTokenMove(token)
    }

    private fun executeSelectedTokenMove(token: Token) {
        val state = _uiState.value
        val activePlayer = state.players[state.activePlayerIndex]
        val baseRoll = state.diceValue.coerceIn(1, 6)
        val tokenSnapshot = token.copy()
        val boosted = if (activePlayer.boostPending) baseRoll + 2 else baseRoll
        val roll = if (activePlayer.boostPending && LudoGameEngine.canMoveToken(tokenSnapshot, boosted)) {
            boosted
        } else baseRoll

        if (activePlayer.boostPending) {
            val cleared = state.players.map {
                if (it.id == activePlayer.id) it.copy(boostPending = false) else it
            }
            _uiState.update { it.copy(players = cleared) }
        }

        if (!LudoGameEngine.canMoveToken(tokenSnapshot, baseRoll)) {
            isActionInProgress = false
            _uiState.update {
                it.copy(
                    phase = GamePhase.SELECT_TOKEN,
                    legalTokens = LudoGameEngine.getLegalTokens(activePlayer, baseRoll),
                    statusMessage = "Invalid move — pick a highlighted token."
                )
            }
            return
        }

        tokenMoveJob?.cancel()
        tokenMoveJob = viewModelScope.launch {
            if (!state.reducedAnimations) {
                if (tokenSnapshot.state == TokenState.IN_BASE) {
                    val startCoord = LudoBoardCoordinates.getBaseSlotCenter(tokenSnapshot.color, tokenSnapshot.id)
                    val targetCoord = LudoBoardCoordinates.getTrackCellCenter(tokenSnapshot.color.startTrackIndex)
                    val frames = 5
                    for (f in 1..frames) {
                        val t = f.toFloat() / frames
                        val currX = startCoord.first + (targetCoord.first - startCoord.first) * t
                        val currY = startCoord.second + (targetCoord.second - startCoord.second) * t
                        val hop = kotlin.math.sin(t * Math.PI).toFloat() * 0.45f
                        _uiState.update {
                            it.copy(
                                animatingToken = AnimatingTokenState(
                                    tokenId = tokenSnapshot.id,
                                    color = tokenSnapshot.color,
                                    currentCoord = Pair(currX, currY),
                                    hopElevation = hop,
                                    scaleMultiplier = 1.25f
                                )
                            )
                        }
                        delay(40L / state.fastForwardSpeed)
                    }
                    SoundManager.playTokenMove()
                } else {
                    val startStep = tokenSnapshot.stepCount
                    val endStep = (startStep + roll).coerceAtMost(LudoGameEngine.MAX_STEPS)
                    for (step in (startStep + 1)..endStep) {
                        val prevCoord = getStepExactCenter(tokenSnapshot.color, tokenSnapshot.id, step - 1)
                        val nextCoord = getStepExactCenter(tokenSnapshot.color, tokenSnapshot.id, step)
                        val subFrames = 3
                        for (sf in 1..subFrames) {
                            val t = sf.toFloat() / subFrames
                            val currX = prevCoord.first + (nextCoord.first - prevCoord.first) * t
                            val currY = prevCoord.second + (nextCoord.second - prevCoord.second) * t
                            val hop = kotlin.math.sin(t * Math.PI).toFloat() * 0.35f
                            _uiState.update {
                                it.copy(
                                    animatingToken = AnimatingTokenState(
                                        tokenId = tokenSnapshot.id,
                                        color = tokenSnapshot.color,
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

            val result = LudoGameEngine.executeMove(
                activePlayer = activePlayer,
                tokenToMove = tokenSnapshot,
                diceRoll = roll,
                allPlayers = state.players,
                rules = state.rules
            )

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

            val newPlayers = state.players.map { player ->
                if (player.id == activePlayer.id) {
                    val updatedTokens = player.tokens.map {
                        if (it.id == tokenSnapshot.id) result.movedToken else it
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
                if (!activePlayer.isAi) repository.recordTokensCaptured(result.capturedTokens.size)
            }

            if (result.reachedHome) {
                SoundManager.playReachedHome()
                historyLog = listOf("${activePlayer.name} brought a token HOME!") + historyLog
                if (!activePlayer.isAi) repository.recordTokenReachedHome()
            }

            if (updatedActivePlayer.hasWon) {
                isActionInProgress = false
                handleGameOver(updatedActivePlayer, newPlayers, totalCaptures)
                return@launch
            }

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
        stopTurnTimer()
        val state = _uiState.value
        val nextIdx = LudoGameEngine.getNextPlayerIndex(state.activePlayerIndex, state.players)
        val nextPlayer = state.players[nextIdx]
        val teamHint = if (state.isTeamMode) " [${nextPlayer.team.name}]" else ""

        _uiState.update {
            it.copy(
                activePlayerIndex = nextIdx,
                phase = GamePhase.ROLL_DICE,
                consecutiveSixes = 0,
                legalTokens = emptyList(),
                lastPowerUpUsed = null,
                statusMessage = "${nextPlayer.name}'s turn$teamHint. Roll the dice!"
            )
        }

        if (state.rules.turnTimerEnabled) startTurnTimer()
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
                    it.copy(phase = GamePhase.MOVING_TOKEN, legalTokens = emptyList())
                }
                executeSelectedTokenMove(selected)
            } else {
                advanceToNextPlayer()
            }
        }
    }

    private fun handleGameOver(winner: Player, players: List<Player>, totalCaptures: Int) {
        stopTurnTimer()
        SoundManager.playVictory()
        val state = _uiState.value
        val winningTeam = if (state.isTeamMode) {
            val team = winner.team
            val teammates = players.filter { team.contains(it.color) }
            if (teammates.all { it.hasWon } || teammates.any { it.hasWon }) team else null
        } else null

        val winMsg = when {
            winningTeam != null -> "🏆 Team ${winningTeam.name} WINS!"
            else -> "🎉 ${winner.name} WINS THE GAME! 🎉"
        }

        _uiState.update {
            it.copy(
                players = players,
                winner = winner,
                winningTeam = winningTeam,
                phase = GamePhase.GAME_OVER,
                statusMessage = winMsg,
                moveHistory = listOf(winMsg) + it.moveHistory
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
            step in 52..56 -> LudoBoardCoordinates.getHomePathCenter(color, step - 52)
            else -> LudoBoardCoordinates.getHomeTriangleSlotCenter(color, tokenId)
        }
    }

    fun togglePause() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
        if (!_uiState.value.isPaused) checkAndTriggerAi()
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
