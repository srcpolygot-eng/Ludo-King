package com.example.ludo.engine

import com.example.ludo.model.AiDifficulty
import com.example.ludo.model.GameRules
import com.example.ludo.model.Player
import com.example.ludo.model.Token
import com.example.ludo.model.TokenState
import kotlin.random.Random

object LudoAiEngine {

    /**
     * Chooses the best token to move for the given AI player and dice roll.
     */
    fun selectTokenToMove(
        aiPlayer: Player,
        diceRoll: Int,
        allPlayers: List<Player>,
        rules: GameRules,
        difficulty: AiDifficulty = aiPlayer.difficulty
    ): Token? {
        val legalTokens = LudoGameEngine.getLegalTokens(aiPlayer, diceRoll)
        if (legalTokens.isEmpty()) return null
        if (legalTokens.size == 1) return legalTokens.first()

        return when (difficulty) {
            AiDifficulty.EASY -> chooseEasyMove(legalTokens)
            AiDifficulty.MEDIUM -> chooseMediumMove(aiPlayer, legalTokens, diceRoll, allPlayers, rules)
            AiDifficulty.HARD -> chooseHardMove(aiPlayer, legalTokens, diceRoll, allPlayers, rules)
            AiDifficulty.EXPERT -> chooseExpertMove(aiPlayer, legalTokens, diceRoll, allPlayers, rules)
        }
    }

    private fun chooseEasyMove(legalTokens: List<Token>): Token {
        // Mostly random, slight preference to exit base if 6
        val baseToken = legalTokens.find { it.isInBase }
        if (baseToken != null && Random.nextFloat() < 0.6f) {
            return baseToken
        }
        return legalTokens.random()
    }

    private fun chooseMediumMove(
        player: Player,
        legalTokens: List<Token>,
        diceRoll: Int,
        allPlayers: List<Player>,
        rules: GameRules
    ): Token {
        // Priority 1: Finishing moves (entering home)
        for (token in legalTokens) {
            val moved = LudoGameEngine.previewMove(token, diceRoll)
            if (moved.state == TokenState.HOME) return token
        }

        // Priority 2: Captures
        for (token in legalTokens) {
            val moved = LudoGameEngine.previewMove(token, diceRoll)
            if (moved.state == TokenState.ON_TRACK) {
                if (canCapture(moved, player, allPlayers, rules)) {
                    return token
                }
            }
        }

        // Priority 3: Exit base on 6
        val baseToken = legalTokens.find { it.isInBase }
        if (baseToken != null) {
            return baseToken
        }

        // Priority 4: Furthest token
        return legalTokens.maxByOrNull { it.stepCount } ?: legalTokens.first()
    }

    private fun chooseHardMove(
        player: Player,
        legalTokens: List<Token>,
        diceRoll: Int,
        allPlayers: List<Player>,
        rules: GameRules
    ): Token {
        var bestToken = legalTokens.first()
        var bestScore = Double.NEGATIVE_INFINITY

        for (token in legalTokens) {
            val moved = LudoGameEngine.previewMove(token, diceRoll)
            var score = 0.0

            // Finishing move
            if (moved.state == TokenState.HOME) score += 1000.0

            // Enter home path
            if (moved.state == TokenState.IN_HOME_PATH && token.state != TokenState.IN_HOME_PATH) {
                score += 400.0
            }

            // Capture
            if (moved.state == TokenState.ON_TRACK && canCapture(moved, player, allPlayers, rules)) {
                score += 800.0
            }

            // Landing on a safe square
            if (moved.state == TokenState.ON_TRACK && rules.safeSquaresEnabled &&
                LudoBoardCoordinates.SAFE_TRACK_INDICES.contains(moved.trackIndex)
            ) {
                score += 300.0
            }

            // Escaping current danger
            if (token.state == TokenState.ON_TRACK && isThreatened(token, allPlayers)) {
                score += 350.0
            }

            // Unlocking from base
            if (token.isInBase) {
                score += 250.0
            }

            // Progress score
            score += moved.stepCount * 5.0

            // Slight randomness to avoid deterministic repetitive plays
            score += Random.nextDouble(0.0, 10.0)

            if (score > bestScore) {
                bestScore = score
                bestToken = token
            }
        }

        return bestToken
    }

    private fun chooseExpertMove(
        player: Player,
        legalTokens: List<Token>,
        diceRoll: Int,
        allPlayers: List<Player>,
        rules: GameRules
    ): Token {
        var bestToken = legalTokens.first()
        var bestScore = Double.NEGATIVE_INFINITY

        for (token in legalTokens) {
            val moved = LudoGameEngine.previewMove(token, diceRoll)
            var score = 0.0

            // 1. Victory / Reaching Home
            if (moved.state == TokenState.HOME) {
                score += 2000.0
            }

            // 2. Entering safe home column
            if (moved.state == TokenState.IN_HOME_PATH && token.state != TokenState.IN_HOME_PATH) {
                score += 650.0
            }

            // 3. Capture evaluation with bounty based on victim's progress
            if (moved.state == TokenState.ON_TRACK && canCapture(moved, player, allPlayers, rules)) {
                val bounty = getCaptureBounty(moved, player, allPlayers)
                score += 1200.0 + bounty * 12.0
            }

            // 4. Safe square bonus
            if (moved.state == TokenState.ON_TRACK && rules.safeSquaresEnabled &&
                LudoBoardCoordinates.SAFE_TRACK_INDICES.contains(moved.trackIndex)
            ) {
                score += 450.0
            }

            // 5. Evasion: Was current token under threat?
            if (token.state == TokenState.ON_TRACK && isThreatened(token, allPlayers)) {
                score += 550.0
            }

            // 6. Danger penalty: Does target square put this token at risk of being captured next turn?
            if (moved.state == TokenState.ON_TRACK && (!rules.safeSquaresEnabled || !LudoBoardCoordinates.SAFE_TRACK_INDICES.contains(moved.trackIndex))) {
                if (isThreatened(moved, allPlayers)) {
                    score -= 500.0
                }
            }

            // 7. Base unlock strategy
            if (token.isInBase) {
                val activeTokens = player.tokens.count { it.state == TokenState.ON_TRACK }
                score += if (activeTokens == 0) 600.0 else 300.0
            }

            // 8. Progress heuristic
            score += (moved.stepCount * 4.0)

            // Small tie-breaker
            score += Random.nextDouble(0.0, 5.0)

            if (score > bestScore) {
                bestScore = score
                bestToken = token
            }
        }

        return bestToken
    }

    private fun canCapture(
        movedToken: Token,
        player: Player,
        allPlayers: List<Player>,
        rules: GameRules
    ): Boolean {
        if (rules.safeSquaresEnabled && LudoBoardCoordinates.SAFE_TRACK_INDICES.contains(movedToken.trackIndex)) {
            return false
        }
        val targetTrack = movedToken.trackIndex
        if (targetTrack == -1) return false

        return allPlayers.any { opponent ->
            opponent.color != player.color && opponent.tokens.any {
                it.state == TokenState.ON_TRACK && it.trackIndex == targetTrack
            }
        }
    }

    private fun getCaptureBounty(
        movedToken: Token,
        player: Player,
        allPlayers: List<Player>
    ): Int {
        val targetTrack = movedToken.trackIndex
        var maxProgress = 0
        for (opponent in allPlayers) {
            if (opponent.color == player.color) continue
            for (token in opponent.tokens) {
                if (token.state == TokenState.ON_TRACK && token.trackIndex == targetTrack) {
                    if (token.stepCount > maxProgress) {
                        maxProgress = token.stepCount
                    }
                }
            }
        }
        return maxProgress
    }

    private fun isThreatened(token: Token, allPlayers: List<Player>): Boolean {
        val myTrack = token.trackIndex
        if (myTrack == -1) return false
        if (LudoBoardCoordinates.SAFE_TRACK_INDICES.contains(myTrack)) return false

        for (opponent in allPlayers) {
            if (opponent.color == token.color) continue
            for (oppToken in opponent.tokens) {
                if (oppToken.state == TokenState.ON_TRACK) {
                    val oppTrack = oppToken.trackIndex
                    val distance = (myTrack - oppTrack + 52) % 52
                    // If opponent is 1..6 squares behind us
                    if (distance in 1..6) {
                        return true
                    }
                }
            }
        }
        return false
    }
}
