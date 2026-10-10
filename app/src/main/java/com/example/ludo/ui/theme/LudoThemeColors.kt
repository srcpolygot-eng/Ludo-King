package com.example.ludo.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.ludo.model.BoardTheme
import com.example.ludo.model.PlayerColor

data class LudoBoardPalette(
    val theme: BoardTheme,
    val boardBackground: Color,
    val boardBorder: Color,
    val trackCellBg: Color,
    val trackCellBorder: Color,
    val starSafeColor: Color,
    val redColor: Color,
    val greenColor: Color,
    val yellowColor: Color,
    val blueColor: Color,
    val baseInsideBg: Color,
    val diceBg: Color,
    val dicePip: Color
) {
    fun getPlayerColor(color: PlayerColor): Color = when (color) {
        PlayerColor.RED -> redColor
        PlayerColor.GREEN -> greenColor
        PlayerColor.YELLOW -> yellowColor
        PlayerColor.BLUE -> blueColor
    }
}

object LudoThemePalettes {
    val CLASSIC = LudoBoardPalette(
        theme = BoardTheme.CLASSIC,
        boardBackground = Color(0xFFF9F9FB),
        boardBorder = Color(0xFF263238),
        trackCellBg = Color(0xFFFFFFFF),
        trackCellBorder = Color(0xFFCFD8DC),
        starSafeColor = Color(0xFFFFB300),
        redColor = Color(0xFFE53935),
        greenColor = Color(0xFF43A047),
        yellowColor = Color(0xFFFDD835),
        blueColor = Color(0xFF1E88E5),
        baseInsideBg = Color(0xFFFFFFFF),
        diceBg = Color(0xFFFFFFFF),
        dicePip = Color(0xFF212121)
    )

    val WOODEN = LudoBoardPalette(
        theme = BoardTheme.WOODEN,
        boardBackground = Color(0xFF5D4037),
        boardBorder = Color(0xFF3E2723),
        trackCellBg = Color(0xFFD7CCC8),
        trackCellBorder = Color(0xFF8D6E63),
        starSafeColor = Color(0xFFFFD54F),
        redColor = Color(0xFFC62828),
        greenColor = Color(0xFF2E7D32),
        yellowColor = Color(0xFFF9A825),
        blueColor = Color(0xFF1565C0),
        baseInsideBg = Color(0xFFEFEBE9),
        diceBg = Color(0xFFFFE0B2),
        dicePip = Color(0xFF4E342E)
    )

    val NEON = LudoBoardPalette(
        theme = BoardTheme.NEON,
        boardBackground = Color(0xFF0F111A),
        boardBorder = Color(0xFF00E5FF),
        trackCellBg = Color(0xFF1A1D2E),
        trackCellBorder = Color(0xFF2D3250),
        starSafeColor = Color(0xFFFF007F),
        redColor = Color(0xFFFF1744),
        greenColor = Color(0xFF00E676),
        yellowColor = Color(0xFFFFEA00),
        blueColor = Color(0xFF00B0FF),
        baseInsideBg = Color(0xFF141724),
        diceBg = Color(0xFF1A1D2E),
        dicePip = Color(0xFF00E5FF)
    )

    val ROYAL = LudoBoardPalette(
        theme = BoardTheme.ROYAL,
        boardBackground = Color(0xFF0D1B2A),
        boardBorder = Color(0xFFFFD700),
        trackCellBg = Color(0xFF1B263B),
        trackCellBorder = Color(0xFF415A77),
        starSafeColor = Color(0xFFFFD700),
        redColor = Color(0xFFD90429),
        greenColor = Color(0xFF007F5F),
        yellowColor = Color(0xFFFFB703),
        blueColor = Color(0xFF219EBC),
        baseInsideBg = Color(0xFF0F1C2E),
        diceBg = Color(0xFFE0E1DD),
        dicePip = Color(0xFF0D1B2A)
    )

    val NATURE = LudoBoardPalette(
        theme = BoardTheme.NATURE,
        boardBackground = Color(0xFFF4F1DE),
        boardBorder = Color(0xFF3D405B),
        trackCellBg = Color(0xFFFFFFFF),
        trackCellBorder = Color(0xFFD8D4C8),
        starSafeColor = Color(0xFFE07A5F),
        redColor = Color(0xFFE07A5F),
        greenColor = Color(0xFF81B29A),
        yellowColor = Color(0xFFF2CC8F),
        blueColor = Color(0xFF3D405B),
        baseInsideBg = Color(0xFFFAF9F6),
        diceBg = Color(0xFFFAF9F6),
        dicePip = Color(0xFF3D405B)
    )

    val MIDNIGHT = LudoBoardPalette(
        theme = BoardTheme.MIDNIGHT,
        boardBackground = Color(0xFF0A0E17),
        boardBorder = Color(0xFF7C4DFF),
        trackCellBg = Color(0xFF121826),
        trackCellBorder = Color(0xFF1F2A40),
        starSafeColor = Color(0xFFB388FF),
        redColor = Color(0xFFFF5252),
        greenColor = Color(0xFF69F0AE),
        yellowColor = Color(0xFFFFD740),
        blueColor = Color(0xFF40C4FF),
        baseInsideBg = Color(0xFF0D121C),
        diceBg = Color(0xFF1A2233),
        dicePip = Color(0xFFB388FF)
    )

    val CANDY = LudoBoardPalette(
        theme = BoardTheme.CANDY,
        boardBackground = Color(0xFFFFF0F5),
        boardBorder = Color(0xFFFF69B4),
        trackCellBg = Color(0xFFFFFFFF),
        trackCellBorder = Color(0xFFFFC0CB),
        starSafeColor = Color(0xFFFF1493),
        redColor = Color(0xFFFF1744),
        greenColor = Color(0xFF00E676),
        yellowColor = Color(0xFFFFEA00),
        blueColor = Color(0xFF2979FF),
        baseInsideBg = Color(0xFFFFF5F8),
        diceBg = Color(0xFFFFFFFF),
        dicePip = Color(0xFFFF69B4)
    )

    fun getPalette(theme: BoardTheme): LudoBoardPalette = when (theme) {
        BoardTheme.CLASSIC -> CLASSIC
        BoardTheme.WOODEN -> WOODEN
        BoardTheme.NEON -> NEON
        BoardTheme.ROYAL -> ROYAL
        BoardTheme.NATURE -> NATURE
        BoardTheme.MIDNIGHT -> MIDNIGHT
        BoardTheme.CANDY -> CANDY
    }
}
