package com.example

import com.example.ludo.engine.BonusReason
import com.example.ludo.engine.LudoAiEngine
import com.example.ludo.engine.LudoBoardCoordinates
import com.example.ludo.engine.LudoGameEngine
import com.example.ludo.model.AiDifficulty
import com.example.ludo.model.GameRules
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.model.Token
import com.example.ludo.model.TokenState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LudoGameEngineTest {

    @Test
    fun testBaseTokenCanOnlyMoveOnSix() {
        val token = Token(id = 0, color = PlayerColor.RED, state = TokenState.IN_BASE, stepCount = 0)

        for (roll in 1..5) {
            assertFalse("Token in base must not move on roll $roll", LudoGameEngine.canMoveToken(token, roll))
        }
        assertTrue("Token in base must move on roll 6", LudoGameEngine.canMoveToken(token, 6))

        val moved = LudoGameEngine.previewMove(token, 6)
        assertEquals(TokenState.ON_TRACK, moved.state)
        assertEquals(1, moved.stepCount)
        assertEquals(0, moved.trackIndex) // Red's startTrackIndex is 0
    }

    @Test
    fun testTrackNavigationAndHomePath() {
        val redToken = Token(id = 0, color = PlayerColor.RED, state = TokenState.ON_TRACK, stepCount = 50)
        // Red track index for step 50 is (0 + 49) % 52 = 49
        assertEquals(49, redToken.trackIndex)

        // Rolling 1 lands on step 51 (still on track, trackIndex 50)
        val onTrackFinal = LudoGameEngine.previewMove(redToken, 1)
        assertEquals(TokenState.ON_TRACK, onTrackFinal.state)
        assertEquals(51, onTrackFinal.stepCount)
        assertEquals(50, onTrackFinal.trackIndex)

        // Rolling 2 from step 50 lands on step 52 (enters home path!)
        val inHomePath = LudoGameEngine.previewMove(redToken, 2)
        assertEquals(TokenState.IN_HOME_PATH, inHomePath.state)
        assertEquals(52, inHomePath.stepCount)
        assertEquals(0, inHomePath.homePathIndex) // First home path cell
    }

    @Test
    fun testExactRollRequiredForHomeCenter() {
        val tokenAt55 = Token(id = 0, color = PlayerColor.RED, state = TokenState.IN_HOME_PATH, stepCount = 55)

        // Step 57 is home center, so distance is 2
        assertTrue("Rolling 2 should be legal to reach home", LudoGameEngine.canMoveToken(tokenAt55, 2))
        val finished = LudoGameEngine.previewMove(tokenAt55, 2)
        assertEquals(TokenState.HOME, finished.state)
        assertEquals(57, finished.stepCount)

        // Rolling 1 is legal (advances to 56)
        assertTrue("Rolling 1 should be legal", LudoGameEngine.canMoveToken(tokenAt55, 1))

        // Rolling > 2 must be illegal (overshoot)
        assertFalse("Rolling 3 should overshoot and be illegal", LudoGameEngine.canMoveToken(tokenAt55, 3))
        assertFalse("Rolling 4 should overshoot and be illegal", LudoGameEngine.canMoveToken(tokenAt55, 4))
        assertFalse("Rolling 6 should overshoot and be illegal", LudoGameEngine.canMoveToken(tokenAt55, 6))
    }

    @Test
    fun testCaptureSendsOpponentToBase() {
        val redPlayer = Player(
            id = 0,
            name = "Red",
            color = PlayerColor.RED,
            isAi = false,
            tokens = listOf(Token(id = 0, color = PlayerColor.RED, state = TokenState.ON_TRACK, stepCount = 2)) // trackIndex = 1
        )

        // Green starts at 13. Place Green token at trackIndex = 4 (which is stepCount = (4 - 13 + 52) % 52 + 1 = 44)
        val greenToken = Token(id = 0, color = PlayerColor.GREEN, state = TokenState.ON_TRACK, stepCount = 44)
        assertEquals(4, greenToken.trackIndex) // Unsafe track square

        val greenPlayer = Player(
            id = 1,
            name = "Green",
            color = PlayerColor.GREEN,
            isAi = false,
            tokens = listOf(greenToken)
        )

        val rules = GameRules(extraTurnOnCapture = true)

        // Red token is at stepCount 2 (trackIndex 1). Red rolls 3 -> lands on stepCount 5 (trackIndex 4!)
        val moveResult = LudoGameEngine.executeMove(
            activePlayer = redPlayer,
            tokenToMove = redPlayer.tokens.first(),
            diceRoll = 3,
            allPlayers = listOf(redPlayer, greenPlayer),
            rules = rules
        )

        assertEquals(1, moveResult.capturedTokens.size)
        val captured = moveResult.capturedTokens.first()
        assertEquals(PlayerColor.GREEN, captured.color)
        assertEquals(TokenState.IN_BASE, captured.state)
        assertEquals(0, captured.stepCount)

        assertTrue(moveResult.getsBonusTurn)
        assertEquals(BonusReason.CAPTURED_OPPONENT, moveResult.bonusReason)
    }

    @Test
    fun testSafeSquaresPreventCaptures() {
        // Track index 0 is RED start and a SAFE SQUARE
        val redPlayer = Player(
            id = 0,
            name = "Red",
            color = PlayerColor.RED,
            isAi = false,
            tokens = listOf(Token(id = 0, color = PlayerColor.RED, state = TokenState.ON_TRACK, stepCount = 1)) // trackIndex = 0
        )

        // Blue token on stepCount 14 (39 + 13 = 52 -> trackIndex 0)
        val blueToken = Token(id = 0, color = PlayerColor.BLUE, state = TokenState.ON_TRACK, stepCount = 14)
        assertEquals(0, blueToken.trackIndex)

        val bluePlayer = Player(
            id = 3,
            name = "Blue",
            color = PlayerColor.BLUE,
            isAi = false,
            tokens = listOf(blueToken)
        )

        val rules = GameRules(safeSquaresEnabled = true)

        // Another token lands on track index 0
        val tokenToMove = Token(id = 1, color = PlayerColor.RED, state = TokenState.IN_BASE, stepCount = 0)
        val result = LudoGameEngine.executeMove(
            activePlayer = redPlayer,
            tokenToMove = tokenToMove,
            diceRoll = 6,
            allPlayers = listOf(redPlayer, bluePlayer),
            rules = rules
        )

        // No captures should happen because track index 0 is safe
        assertEquals(0, result.capturedTokens.size)
    }

    @Test
    fun testAiSelectsLegalMoves() {
        val aiPlayer = Player(
            id = 1,
            name = "AI Bot",
            color = PlayerColor.GREEN,
            isAi = true,
            difficulty = AiDifficulty.EXPERT,
            tokens = listOf(
                Token(id = 0, color = PlayerColor.GREEN, state = TokenState.IN_BASE, stepCount = 0),
                Token(id = 1, color = PlayerColor.GREEN, state = TokenState.ON_TRACK, stepCount = 15),
                Token(id = 2, color = PlayerColor.GREEN, state = TokenState.HOME, stepCount = 57),
                Token(id = 3, color = PlayerColor.GREEN, state = TokenState.IN_BASE, stepCount = 0)
            )
        )

        for (diff in AiDifficulty.entries) {
            val selectedToken = LudoAiEngine.selectTokenToMove(
                aiPlayer = aiPlayer.copy(difficulty = diff),
                diceRoll = 4,
                allPlayers = listOf(aiPlayer),
                rules = GameRules(),
                difficulty = diff
            )

            assertNotNull("AI should select a token on roll 4", selectedToken)
            assertEquals("Only token 1 is legal on roll 4", 1, selectedToken!!.id)
        }
    }

    @Test
    fun testVictoryDetectionWhenAllTokensHome() {
        val winnerPlayer = Player(
            id = 0,
            name = "Champion",
            color = PlayerColor.RED,
            isAi = false,
            tokens = List(4) { Token(id = it, color = PlayerColor.RED, state = TokenState.HOME, stepCount = 57) }
        )

        assertTrue("Player should have won when all 4 tokens are home", winnerPlayer.hasWon)
        assertEquals(4, winnerPlayer.tokensHomeCount)
    }
}
