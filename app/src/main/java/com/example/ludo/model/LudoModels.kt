package com.example.ludo.model

import androidx.compose.ui.graphics.Color

enum class PlayerColor(
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val darkColor: Color,
    val startTrackIndex: Int,
    val homeEntryTrackIndex: Int // Index on the 52-cell main track right before entering home path
) {
    RED(
        displayName = "Red",
        primaryColor = Color(0xFFE53935),
        secondaryColor = Color(0xFFFF8A80),
        darkColor = Color(0xFFB71C1C),
        startTrackIndex = 0,
        homeEntryTrackIndex = 50
    ),
    GREEN(
        displayName = "Green",
        primaryColor = Color(0xFF43A047),
        secondaryColor = Color(0xFFA5D6A7),
        darkColor = Color(0xFF1B5E20),
        startTrackIndex = 13,
        homeEntryTrackIndex = 11
    ),
    YELLOW(
        displayName = "Yellow",
        primaryColor = Color(0xFFFDD835),
        secondaryColor = Color(0xFFFFF59D),
        darkColor = Color(0xFFF57F17),
        startTrackIndex = 26,
        homeEntryTrackIndex = 24
    ),
    BLUE(
        displayName = "Blue",
        primaryColor = Color(0xFF1E88E5),
        secondaryColor = Color(0xFF90CAF9),
        darkColor = Color(0xFF0D47A1),
        startTrackIndex = 39,
        homeEntryTrackIndex = 37
    );

    val next: PlayerColor
        get() = when (this) {
            RED -> GREEN
            GREEN -> YELLOW
            YELLOW -> BLUE
            BLUE -> RED
        }
}

enum class TokenState {
    IN_BASE,
    ON_TRACK,
    IN_HOME_PATH,
    HOME
}

/**
 * Step count definition for a token:
 * 0: In base
 * 1: Just moved to starting track square
 * 1..51: Outer circular track (51 steps on track)
 * 52..56: Colored home path (5 cells)
 * 57: Final home center (victory cell)
 */
data class Token(
    val id: Int, // 0..3
    val color: PlayerColor,
    val state: TokenState = TokenState.IN_BASE,
    val stepCount: Int = 0 // 0 = in base, 1 = at start square, 57 = reached home
) {
    val isHome: Boolean get() = state == TokenState.HOME
    val isInBase: Boolean get() = state == TokenState.IN_BASE

    /**
     * Main track index (0..51) if the token is ON_TRACK.
     */
    val trackIndex: Int
        get() {
            if (state != TokenState.ON_TRACK || stepCount < 1 || stepCount > 51) return -1
            return (color.startTrackIndex + (stepCount - 1)) % 52
        }

    /**
     * Home path cell index (0..4) if IN_HOME_PATH.
     */
    val homePathIndex: Int
        get() {
            if (state != TokenState.IN_HOME_PATH) return -1
            return stepCount - 52
        }
}

enum class AiDifficulty(val label: String, val description: String) {
    EASY("Easy", "Casual, random legal moves"),
    MEDIUM("Medium", "Balanced progression and captures"),
    HARD("Hard", "Tactical defense and safe square usage"),
    EXPERT("Expert", "Master-level heuristic evaluation")
}

data class Player(
    val id: Int,
    val name: String,
    val color: PlayerColor,
    val isAi: Boolean,
    val difficulty: AiDifficulty = AiDifficulty.MEDIUM,
    val tokens: List<Token> = List(4) { Token(id = it, color = color) },
    val tokenSkin: TokenSkin = TokenSkin.CLASSIC,
    val powerUps: List<PowerUp> = emptyList(),
    val shieldedTokenIds: Set<Int> = emptySet(),
    val boostPending: Boolean = false,
    val luckySixPending: Boolean = false
) {
    val tokensHomeCount: Int get() = tokens.count { it.isHome }
    val hasWon: Boolean get() = tokensHomeCount == 4
    val team: Team get() = Teams.teamFor(color)
}

enum class GamePhase {
    ROLL_DICE,
    SELECT_TOKEN,
    MOVING_TOKEN,
    TURN_SKIPPED,
    GAME_OVER
}

enum class GameMode(val title: String, val subtitle: String) {
    VS_COMPUTER("Play vs Computer", "Challenge intelligent AI players"),
    PASS_AND_PLAY("Pass & Play", "Local multiplayer with 2–4 players on one phone"),
    COMPUTER_ONLY("Watch AI Battle", "Automated AI match with speed control"),
    QUICK_GAME("Quick Match", "Jump straight into a fast-paced game"),
    TEAM_MODE("Team Mode", "2v2 alliance — Red+Yellow vs Green+Blue"),
    ARCADE("Arcade", "Power-ups, timer pressure & special dice")
}

data class GameRules(
    val extraTurnOnSix: Boolean = true,
    val extraTurnOnCapture: Boolean = true,
    val extraTurnOnHome: Boolean = true,
    val threeConsecutiveSixesForfeit: Boolean = true,
    val safeSquaresEnabled: Boolean = true,
    val turnTimerEnabled: Boolean = false,
    val turnTimerSeconds: Int = 30,
    val powerUpsEnabled: Boolean = false,
    val teamModeEnabled: Boolean = false
)

enum class BoardTheme(val displayName: String) {
    CLASSIC("Classic"),
    WOODEN("Wooden"),
    NEON("Neon Glow"),
    ROYAL("Royal Marble"),
    NATURE("Nature Oasis"),
    MIDNIGHT("Midnight"),
    CANDY("Candy Pop")
}

/** Token visual skin — cosmetic only. */
enum class TokenSkin(val displayName: String, val emoji: String) {
    CLASSIC("Classic", "●"),
    CROWN("Crown", "👑"),
    STAR("Star", "⭐"),
    GEM("Gem", "💎"),
    FLAME("Flame", "🔥"),
    ICE("Ice", "❄️"),
    DRAGON("Dragon", "🐉"),
    ROBOT("Robot", "🤖")
}

/** Arcade / special power-ups (Ludo King style). */
enum class PowerUpType(val displayName: String, val description: String, val emoji: String) {
    DOUBLE_DICE("Double Dice", "Roll two dice and pick the best result", "🎲"),
    SHIELD("Shield", "Protect one token from capture for 1 turn", "🛡️"),
    BOOST("Boost", "Move +2 extra steps on your next move", "🚀"),
    SWAP("Swap", "Swap positions with an opponent token", "🔄"),
    FREEZE("Freeze", "Skip the next opponent's turn", "❄️"),
    LUCKY_SIX("Lucky Six", "Guaranteed 6 on your next roll", "✨")
}

data class PowerUp(
    val type: PowerUpType,
    val charges: Int = 1
)

/** Team alliances for 2v2 mode. */
enum class TeamId { TEAM_A, TEAM_B }

data class Team(
    val id: TeamId,
    val name: String,
    val colors: List<PlayerColor>
) {
    fun contains(color: PlayerColor) = colors.contains(color)
}

object Teams {
    val TEAM_A = Team(TeamId.TEAM_A, "Red & Yellow", listOf(PlayerColor.RED, PlayerColor.YELLOW))
    val TEAM_B = Team(TeamId.TEAM_B, "Green & Blue", listOf(PlayerColor.GREEN, PlayerColor.BLUE))
    fun teamFor(color: PlayerColor): Team =
        if (TEAM_A.contains(color)) TEAM_A else TEAM_B
}
