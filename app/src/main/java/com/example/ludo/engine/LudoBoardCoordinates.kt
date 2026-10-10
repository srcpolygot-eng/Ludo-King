package com.example.ludo.engine

import com.example.ludo.model.PlayerColor
import com.example.ludo.model.Token
import com.example.ludo.model.TokenState

data class GridCoord(val col: Int, val row: Int)

object LudoBoardCoordinates {
    const val GRID_SIZE = 15

    // 52-cell main track in clockwise order starting from Red's start square (1, 6)
    val TRACK_CELLS: List<GridCoord> = listOf(
        // Left arm top row: moving right
        GridCoord(1, 6),  // 0: RED START
        GridCoord(2, 6),  // 1
        GridCoord(3, 6),  // 2
        GridCoord(4, 6),  // 3
        GridCoord(5, 6),  // 4

        // Top arm left column: moving up
        GridCoord(6, 5),  // 5
        GridCoord(6, 4),  // 6
        GridCoord(6, 3),  // 7
        GridCoord(6, 2),  // 8: STAR SAFE SQUARE
        GridCoord(6, 1),  // 9
        GridCoord(6, 0),  // 10

        // Top arm top cap
        GridCoord(7, 0),  // 11
        GridCoord(8, 0),  // 12

        // Top arm right column: moving down
        GridCoord(8, 1),  // 13: GREEN START
        GridCoord(8, 2),  // 14
        GridCoord(8, 3),  // 15
        GridCoord(8, 4),  // 16
        GridCoord(8, 5),  // 17

        // Right arm top row: moving right
        GridCoord(9, 6),  // 18
        GridCoord(10, 6), // 19
        GridCoord(11, 6), // 20
        GridCoord(12, 6), // 21: STAR SAFE SQUARE
        GridCoord(13, 6), // 22
        GridCoord(14, 6), // 23

        // Right arm right cap
        GridCoord(14, 7), // 24
        GridCoord(14, 8), // 25

        // Right arm bottom row: moving left
        GridCoord(13, 8), // 26: YELLOW START
        GridCoord(12, 8), // 27
        GridCoord(11, 8), // 28
        GridCoord(10, 8), // 29
        GridCoord(9, 8),  // 30

        // Bottom arm right column: moving down
        GridCoord(8, 9),  // 31
        GridCoord(8, 10), // 32
        GridCoord(8, 11), // 33
        GridCoord(8, 12), // 34: STAR SAFE SQUARE
        GridCoord(8, 13), // 35
        GridCoord(8, 14), // 36

        // Bottom arm bottom cap
        GridCoord(7, 14), // 37
        GridCoord(6, 14), // 38

        // Bottom arm left column: moving up
        GridCoord(6, 13), // 39: BLUE START
        GridCoord(6, 12), // 40
        GridCoord(6, 11), // 41
        GridCoord(6, 10), // 42
        GridCoord(6, 9),  // 43

        // Left arm bottom row: moving left
        GridCoord(5, 8),  // 44
        GridCoord(4, 8),  // 45
        GridCoord(3, 8),  // 46
        GridCoord(2, 8),  // 47: STAR SAFE SQUARE
        GridCoord(1, 8),  // 48
        GridCoord(0, 8),  // 49

        // Left arm left cap
        GridCoord(0, 7),  // 50: RED HOME ENTRANCE CELL
        GridCoord(0, 6)   // 51
    )

    // Safe squares indices on the main track (4 starting squares + 4 star squares)
    val SAFE_TRACK_INDICES: Set<Int> = setOf(0, 8, 13, 21, 26, 34, 39, 47)

    // The 4 starting track indices
    val START_TRACK_INDICES: Set<Int> = setOf(0, 13, 26, 39)

    // Home path cells for each player (5 cells leading towards center triangle)
    val RED_HOME_PATH: List<GridCoord> = listOf(
        GridCoord(1, 7), GridCoord(2, 7), GridCoord(3, 7), GridCoord(4, 7), GridCoord(5, 7)
    )
    val GREEN_HOME_PATH: List<GridCoord> = listOf(
        GridCoord(7, 1), GridCoord(7, 2), GridCoord(7, 3), GridCoord(7, 4), GridCoord(7, 5)
    )
    val YELLOW_HOME_PATH: List<GridCoord> = listOf(
        GridCoord(13, 7), GridCoord(12, 7), GridCoord(11, 7), GridCoord(10, 7), GridCoord(9, 7)
    )
    val BLUE_HOME_PATH: List<GridCoord> = listOf(
        GridCoord(7, 13), GridCoord(7, 12), GridCoord(7, 11), GridCoord(7, 10), GridCoord(7, 9)
    )

    fun getHomePath(color: PlayerColor): List<GridCoord> = when (color) {
        PlayerColor.RED -> RED_HOME_PATH
        PlayerColor.GREEN -> GREEN_HOME_PATH
        PlayerColor.YELLOW -> YELLOW_HOME_PATH
        PlayerColor.BLUE -> BLUE_HOME_PATH
    }

    // Exact center coordinates for the 4 base slots (in grid fractional units)
    fun getBaseSlotCenter(color: PlayerColor, tokenIndex: Int): Pair<Float, Float> {
        val (baseColStart, baseRowStart) = when (color) {
            PlayerColor.RED -> Pair(0f, 0f)
            PlayerColor.GREEN -> Pair(9f, 0f)
            PlayerColor.YELLOW -> Pair(9f, 9f)
            PlayerColor.BLUE -> Pair(0f, 9f)
        }
        val (subCol, subRow) = when (tokenIndex % 4) {
            0 -> Pair(2.0f, 2.0f)
            1 -> Pair(4.0f, 2.0f)
            2 -> Pair(2.0f, 4.0f)
            else -> Pair(4.0f, 4.0f)
        }
        return Pair(baseColStart + subCol, baseRowStart + subRow)
    }

    // Exact center coordinates for track cells
    fun getTrackCellCenter(trackIndex: Int): Pair<Float, Float> {
        val safeIndex = trackIndex.coerceIn(0, 51)
        val cell = TRACK_CELLS[safeIndex]
        return Pair(cell.col + 0.5f, cell.row + 0.5f)
    }

    // Exact center coordinates for home path cells
    fun getHomePathCenter(color: PlayerColor, pathIndex: Int): Pair<Float, Float> {
        val safeIdx = pathIndex.coerceIn(0, 4)
        val path = getHomePath(color)
        val cell = path[safeIdx]
        return Pair(cell.col + 0.5f, cell.row + 0.5f)
    }

    // Exact center coordinates for finished tokens inside the central colored triangle
    fun getHomeTriangleSlotCenter(color: PlayerColor, tokenIndex: Int): Pair<Float, Float> {
        val idx = tokenIndex % 4
        return when (color) {
            PlayerColor.RED -> when (idx) {
                0 -> Pair(6.6f, 7.0f)
                1 -> Pair(6.6f, 8.0f)
                2 -> Pair(7.0f, 7.3f)
                else -> Pair(7.0f, 7.7f)
            }
            PlayerColor.GREEN -> when (idx) {
                0 -> Pair(7.0f, 6.6f)
                1 -> Pair(8.0f, 6.6f)
                2 -> Pair(7.3f, 7.0f)
                else -> Pair(7.7f, 7.0f)
            }
            PlayerColor.YELLOW -> when (idx) {
                0 -> Pair(8.4f, 7.0f)
                1 -> Pair(8.4f, 8.0f)
                2 -> Pair(8.0f, 7.3f)
                else -> Pair(8.0f, 7.7f)
            }
            PlayerColor.BLUE -> when (idx) {
                0 -> Pair(7.0f, 8.4f)
                1 -> Pair(8.0f, 8.4f)
                2 -> Pair(7.3f, 8.0f)
                else -> Pair(7.7f, 8.0f)
            }
        }
    }

    // Universal single source of truth for token center coordinates
    fun getTokenCenterCoord(token: Token): Pair<Float, Float> {
        return when (token.state) {
            TokenState.IN_BASE -> getBaseSlotCenter(token.color, token.id)
            TokenState.ON_TRACK -> getTrackCellCenter(token.trackIndex)
            TokenState.IN_HOME_PATH -> getHomePathCenter(token.color, token.homePathIndex)
            TokenState.HOME -> getHomeTriangleSlotCenter(token.color, token.id)
        }
    }

    // Backwards-compatible aliases
    fun getBaseTokenCoord(color: PlayerColor, tokenIndex: Int): Pair<Float, Float> =
        getBaseSlotCenter(color, tokenIndex)

    fun getHomeTriangleCenter(color: PlayerColor, tokenIndex: Int): Pair<Float, Float> =
        getHomeTriangleSlotCenter(color, tokenIndex)
}
