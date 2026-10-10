package com.example.ludo.engine

import com.example.ludo.model.GameRules
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.model.Token
import com.example.ludo.model.TokenState

data class MoveResult(
    val movedToken: Token,
    val capturedTokens: List<Token>,
    val reachedHome: Boolean,
    val getsBonusTurn: Boolean,
    val bonusReason: BonusReason?
)

enum class BonusReason {
    ROLLED_SIX,
    CAPTURED_OPPONENT,
    REACHED_HOME
}

object LudoGameEngine {
    const val MAX_STEPS = 57 // Step 1 is start square, 57 is home center

    /**
     * Checks whether a specific token can legally move given the dice roll.
     */
    fun canMoveToken(token: Token, diceRoll: Int): Boolean {
        if (token.state == TokenState.HOME) return false

        if (token.state == TokenState.IN_BASE) {
            return diceRoll == 6
        }

        val targetStep = token.stepCount + diceRoll
        // Must roll exact to land on step 57
        return targetStep <= MAX_STEPS
    }

    /**
     * Returns list of tokens that have legal moves for this player and dice roll.
     */
    fun getLegalTokens(player: Player, diceRoll: Int): List<Token> {
        return player.tokens.filter { canMoveToken(it, diceRoll) }
    }

    /**
     * Computes the simulated next state of a token if moved by diceRoll.
     */
    fun previewMove(token: Token, diceRoll: Int): Token {
        if (!canMoveToken(token, diceRoll)) return token

        if (token.state == TokenState.IN_BASE) {
            return token.copy(
                state = TokenState.ON_TRACK,
                stepCount = 1
            )
        }

        val nextStep = token.stepCount + diceRoll
        val nextState = when {
            nextStep == MAX_STEPS -> TokenState.HOME
            nextStep > 51 -> TokenState.IN_HOME_PATH
            else -> TokenState.ON_TRACK
        }

        return token.copy(
            state = nextState,
            stepCount = nextStep
        )
    }

    /**
     * Executes the move for a token, resolves any captures, and determines if a bonus turn is earned.
     */
    fun executeMove(
        activePlayer: Player,
        tokenToMove: Token,
        diceRoll: Int,
        allPlayers: List<Player>,
        rules: GameRules
    ): MoveResult {
        require(canMoveToken(tokenToMove, diceRoll)) { "Illegal move for token ${tokenToMove.id}" }

        val movedToken = previewMove(tokenToMove, diceRoll)
        val capturedTokens = mutableListOf<Token>()
        var reachedHome = false

        if (movedToken.state == TokenState.HOME && tokenToMove.state != TokenState.HOME) {
            reachedHome = true
        }

        // Check capture on outer track
        if (movedToken.state == TokenState.ON_TRACK) {
            val landingTrackIndex = movedToken.trackIndex
            val isSafeSquare = rules.safeSquaresEnabled &&
                    LudoBoardCoordinates.SAFE_TRACK_INDICES.contains(landingTrackIndex)

            if (!isSafeSquare && landingTrackIndex != -1) {
                // Look for opponent tokens on this square
                for (opponent in allPlayers) {
                    if (opponent.color == activePlayer.color) continue
                    for (oppToken in opponent.tokens) {
                        if (oppToken.state == TokenState.ON_TRACK && oppToken.trackIndex == landingTrackIndex) {
                            capturedTokens.add(
                                oppToken.copy(
                                    state = TokenState.IN_BASE,
                                    stepCount = 0
                                )
                            )
                        }
                    }
                }
            }
        }

        // Determine bonus turn
        var getsBonus = false
        var bonusReason: BonusReason? = null

        if (capturedTokens.isNotEmpty() && rules.extraTurnOnCapture) {
            getsBonus = true
            bonusReason = BonusReason.CAPTURED_OPPONENT
        } else if (reachedHome && rules.extraTurnOnHome) {
            getsBonus = true
            bonusReason = BonusReason.REACHED_HOME
        } else if (diceRoll == 6 && rules.extraTurnOnSix) {
            getsBonus = true
            bonusReason = BonusReason.ROLLED_SIX
        }

        return MoveResult(
            movedToken = movedToken,
            capturedTokens = capturedTokens,
            reachedHome = reachedHome,
            getsBonusTurn = getsBonus,
            bonusReason = bonusReason
        )
    }

    /**
     * Determines the next player index among remaining unfinished players.
     */
    fun getNextPlayerIndex(
        currentIndex: Int,
        players: List<Player>
    ): Int {
        if (players.isEmpty()) return 0
        var next = (currentIndex + 1) % players.size
        var checks = 0
        while (players[next].hasWon && checks < players.size) {
            next = (next + 1) % players.size
            checks++
        }
        return next
    }
}
