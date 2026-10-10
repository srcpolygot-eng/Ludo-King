package com.example.ludo.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.ludo.engine.GridCoord
import com.example.ludo.engine.LudoBoardCoordinates
import com.example.ludo.model.BoardTheme
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor
import com.example.ludo.model.Token
import com.example.ludo.model.TokenState
import com.example.ludo.ui.theme.LudoBoardPalette
import com.example.ludo.ui.theme.LudoThemePalettes
import com.example.ludo.viewmodel.AnimatingTokenState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@Composable
fun LudoBoardView(
    players: List<Player>,
    activePlayer: Player,
    legalTokens: List<Token>,
    theme: BoardTheme,
    animatingToken: AnimatingTokenState? = null,
    capturedReturningTokens: List<AnimatingTokenState> = emptyList(),
    onTokenClicked: (Token) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LudoThemePalettes.getPalette(theme)

    val infiniteTransition = rememberInfiniteTransition(label = "boardPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tokenPulse"
    )

    val haloRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "haloRotation"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .testTag("ludo_board_canvas")
    ) {
        val boardPixelSize = constraints.maxWidth.toFloat()
        val cellSize = boardPixelSize / 15f

        val drawnTokenTargets = mutableListOf<Pair<Token, Offset>>()

        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(legalTokens, activePlayer, boardPixelSize) {
                    detectTapGestures { tapOffset ->
                        if (legalTokens.isEmpty() || activePlayer.isAi) return@detectTapGestures

                        var closestToken: Token? = null
                        var minDistance = cellSize * 1.1f

                        for ((token, pos) in drawnTokenTargets) {
                            if (legalTokens.any { it.id == token.id && it.color == token.color }) {
                                val dist = hypot(tapOffset.x - pos.x, tapOffset.y - pos.y)
                                if (dist < minDistance) {
                                    minDistance = dist
                                    closestToken = token
                                }
                            }
                        }

                        closestToken?.let { onTokenClicked(it) }
                    }
                }
        ) {
            // 1. Board Background
            drawRoundRect(
                color = palette.boardBackground,
                topLeft = Offset(0f, 0f),
                size = Size(boardPixelSize, boardPixelSize),
                cornerRadius = CornerRadius(cellSize * 0.4f, cellSize * 0.4f)
            )

            // 2. Corner Base Boxes (6x6 cells each)
            drawBaseQuadrant(PlayerColor.RED, 0f, 0f, cellSize, palette)
            drawBaseQuadrant(PlayerColor.GREEN, 9f, 0f, cellSize, palette)
            drawBaseQuadrant(PlayerColor.YELLOW, 9f, 9f, cellSize, palette)
            drawBaseQuadrant(PlayerColor.BLUE, 0f, 9f, cellSize, palette)

            // 3. Track Cells (52 cells)
            for (index in 0 until 52) {
                val coord = LudoBoardCoordinates.TRACK_CELLS[index]
                drawTrackCell(coord, index, cellSize, palette)
            }

            // 4. Directional Path Arrows on Track
            drawTrackDirectionArrows(cellSize, palette)

            // 5. Colored Home Paths
            drawHomePaths(cellSize, palette)

            // 6. Center Home Area
            drawCenterHome(cellSize, palette)

            // 7. Outer Board Border
            drawRoundRect(
                color = palette.boardBorder,
                topLeft = Offset(0f, 0f),
                size = Size(boardPixelSize, boardPixelSize),
                cornerRadius = CornerRadius(cellSize * 0.4f, cellSize * 0.4f),
                style = Stroke(width = cellSize * 0.08f)
            )

            // 8. Static & Idle Tokens
            drawnTokenTargets.clear()
            drawBoardTokens(
                players = players,
                activePlayer = activePlayer,
                legalTokens = legalTokens,
                animatingToken = animatingToken,
                capturedReturningTokens = capturedReturningTokens,
                cellSize = cellSize,
                palette = palette,
                pulseScale = pulseScale,
                haloRotation = haloRotation,
                targetList = drawnTokenTargets
            )

            // 9. Floating / Moving Tokens (Top layer with elevation shadow)
            if (animatingToken != null) {
                drawMovingPawn(
                    animatingToken = animatingToken,
                    cellSize = cellSize,
                    palette = palette
                )
            }

            for (cap in capturedReturningTokens) {
                drawMovingPawn(
                    animatingToken = cap,
                    cellSize = cellSize,
                    palette = palette
                )
            }
        }
    }
}

private fun DrawScope.drawBaseQuadrant(
    color: PlayerColor,
    colOffset: Float,
    rowOffset: Float,
    cellSize: Float,
    palette: LudoBoardPalette
) {
    val baseColor = palette.getPlayerColor(color)
    val startX = colOffset * cellSize
    val startY = rowOffset * cellSize
    val baseSize = 6f * cellSize

    // Outer base with gradient
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(baseColor.copy(alpha = 0.95f), baseColor),
            center = Offset(startX + baseSize / 2f, startY + baseSize / 2f),
            radius = baseSize * 0.8f
        ),
        topLeft = Offset(startX, startY),
        size = Size(baseSize, baseSize),
        cornerRadius = CornerRadius(cellSize * 0.35f, cellSize * 0.35f)
    )
    drawRoundRect(
        color = palette.boardBorder.copy(alpha = 0.6f),
        topLeft = Offset(startX, startY),
        size = Size(baseSize, baseSize),
        cornerRadius = CornerRadius(cellSize * 0.35f, cellSize * 0.35f),
        style = Stroke(width = cellSize * 0.05f)
    )

    // Inner White Pediment (4x4 cells)
    val pocketX = (colOffset + 1f) * cellSize
    val pocketY = (rowOffset + 1f) * cellSize
    val pocketSize = 4f * cellSize

    drawRoundRect(
        color = palette.baseInsideBg,
        topLeft = Offset(pocketX, pocketY),
        size = Size(pocketSize, pocketSize),
        cornerRadius = CornerRadius(cellSize * 0.3f, cellSize * 0.3f)
    )
    drawRoundRect(
        color = baseColor.copy(alpha = 0.4f),
        topLeft = Offset(pocketX, pocketY),
        size = Size(pocketSize, pocketSize),
        cornerRadius = CornerRadius(cellSize * 0.3f, cellSize * 0.3f),
        style = Stroke(width = cellSize * 0.04f)
    )

    // 4 Concentric Token Slots
    for (i in 0 until 4) {
        val (c, r) = LudoBoardCoordinates.getBaseSlotCenter(color, i)
        val cx = c * cellSize
        val cy = r * cellSize
        val radius = cellSize * 0.62f

        // Outer recessed ring
        drawCircle(
            color = baseColor.copy(alpha = 0.15f),
            center = Offset(cx, cy),
            radius = radius
        )
        drawCircle(
            color = baseColor.copy(alpha = 0.75f),
            center = Offset(cx, cy),
            radius = radius,
            style = Stroke(width = cellSize * 0.045f)
        )
        // Inner anchor dot
        drawCircle(
            color = baseColor.copy(alpha = 0.35f),
            center = Offset(cx, cy),
            radius = radius * 0.45f
        )
    }
}

private fun DrawScope.drawTrackCell(
    coord: GridCoord,
    trackIndex: Int,
    cellSize: Float,
    palette: LudoBoardPalette
) {
    val x = coord.col * cellSize
    val y = coord.row * cellSize
    val size = Size(cellSize, cellSize)

    val isStartCell = trackIndex in LudoBoardCoordinates.START_TRACK_INDICES
    val cellColor = when (trackIndex) {
        0 -> palette.redColor
        13 -> palette.greenColor
        26 -> palette.yellowColor
        39 -> palette.blueColor
        else -> palette.trackCellBg
    }

    drawRect(color = cellColor, topLeft = Offset(x, y), size = size)
    drawRect(
        color = palette.trackCellBorder,
        topLeft = Offset(x, y),
        size = size,
        style = Stroke(width = cellSize * 0.035f)
    )

    // Star Safe Squares
    if (trackIndex in LudoBoardCoordinates.SAFE_TRACK_INDICES && !isStartCell) {
        // Safe square circular emblem
        drawCircle(
            color = palette.starSafeColor.copy(alpha = 0.22f),
            center = Offset(x + cellSize / 2f, y + cellSize / 2f),
            radius = cellSize * 0.38f
        )
        drawGoldenStar(
            center = Offset(x + cellSize / 2f, y + cellSize / 2f),
            radius = cellSize * 0.32f,
            color = palette.starSafeColor
        )
    } else if (isStartCell) {
        // Start square center emblem
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            center = Offset(x + cellSize / 2f, y + cellSize / 2f),
            radius = cellSize * 0.24f
        )
        drawGoldenStar(
            center = Offset(x + cellSize / 2f, y + cellSize / 2f),
            radius = cellSize * 0.16f,
            color = cellColor
        )
    }
}

private fun DrawScope.drawTrackDirectionArrows(cellSize: Float, palette: LudoBoardPalette) {
    fun drawChevron(x: Float, y: Float, dirAngle: Float, color: Color) {
        val center = Offset(x, y)
        val path = Path()
        val s = cellSize * 0.22f
        path.moveTo(-s, -s * 0.6f)
        path.lineTo(0f, 0f)
        path.lineTo(-s, s * 0.6f)

        // Rotate and draw
        val rad = (dirAngle * PI / 180f).toFloat()
        val cosA = cos(rad)
        val sinA = sin(rad)

        val rotatedPath = Path().apply {
            moveTo(center.x + (-s * cosA - (-s * 0.6f) * sinA), center.y + (-s * sinA + (-s * 0.6f) * cosA))
            lineTo(center.x, center.y)
            lineTo(center.x + (-s * cosA - (s * 0.6f) * sinA), center.y + (-s * sinA + (s * 0.6f) * cosA))
        }
        drawPath(rotatedPath, color = color, style = Stroke(width = cellSize * 0.06f))
    }

    // Red arm moving right: cell (2, 6)
    drawChevron(2.5f * cellSize, 6.5f * cellSize, 0f, Color.White.copy(alpha = 0.8f))
    // Turning up into top arm: cell (5, 6)
    drawChevron(5.5f * cellSize, 6.5f * cellSize, -90f, palette.trackCellBorder.copy(alpha = 0.7f))
    // Green arm moving down: cell (8, 2)
    drawChevron(8.5f * cellSize, 2.5f * cellSize, 90f, Color.White.copy(alpha = 0.8f))
    // Turning right into right arm: cell (8, 5)
    drawChevron(8.5f * cellSize, 5.5f * cellSize, 0f, palette.trackCellBorder.copy(alpha = 0.7f))
    // Yellow arm moving left: cell (12, 8)
    drawChevron(12.5f * cellSize, 8.5f * cellSize, 180f, Color.White.copy(alpha = 0.8f))
    // Turning down into bottom arm: cell (9, 8)
    drawChevron(9.5f * cellSize, 8.5f * cellSize, 90f, palette.trackCellBorder.copy(alpha = 0.7f))
    // Blue arm moving up: cell (6, 12)
    drawChevron(6.5f * cellSize, 12.5f * cellSize, -90f, Color.White.copy(alpha = 0.8f))
    // Turning left into left arm: cell (6, 9)
    drawChevron(6.5f * cellSize, 9.5f * cellSize, 180f, palette.trackCellBorder.copy(alpha = 0.7f))
}

private fun DrawScope.drawHomePaths(cellSize: Float, palette: LudoBoardPalette) {
    fun drawPathRow(path: List<GridCoord>, color: Color, angle: Float) {
        for (coord in path) {
            val x = coord.col * cellSize
            val y = coord.row * cellSize
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(color, color.copy(alpha = 0.85f)),
                    start = Offset(x, y),
                    end = Offset(x + cellSize, y + cellSize)
                ),
                topLeft = Offset(x, y),
                size = Size(cellSize, cellSize)
            )
            drawRect(
                color = palette.trackCellBorder,
                topLeft = Offset(x, y),
                size = Size(cellSize, cellSize),
                style = Stroke(width = cellSize * 0.035f)
            )
            // Centered subtle arrow
            drawCircle(
                color = Color.White.copy(alpha = 0.65f),
                center = Offset(x + cellSize / 2f, y + cellSize / 2f),
                radius = cellSize * 0.14f
            )
        }
    }

    drawPathRow(LudoBoardCoordinates.RED_HOME_PATH, palette.redColor, 0f)
    drawPathRow(LudoBoardCoordinates.GREEN_HOME_PATH, palette.greenColor, 90f)
    drawPathRow(LudoBoardCoordinates.YELLOW_HOME_PATH, palette.yellowColor, 180f)
    drawPathRow(LudoBoardCoordinates.BLUE_HOME_PATH, palette.blueColor, 270f)
}

private fun DrawScope.drawCenterHome(cellSize: Float, palette: LudoBoardPalette) {
    val centerLeft = 6f * cellSize
    val centerTop = 6f * cellSize
    val centerSize = 3f * cellSize
    val cx = 7.5f * cellSize
    val cy = 7.5f * cellSize

    drawRect(
        color = palette.boardBackground,
        topLeft = Offset(centerLeft, centerTop),
        size = Size(centerSize, centerSize)
    )

    // Red Triangle (Left)
    val redPath = Path().apply {
        moveTo(centerLeft, centerTop)
        lineTo(cx, cy)
        lineTo(centerLeft, centerTop + centerSize)
        close()
    }
    drawPath(redPath, color = palette.redColor)

    // Green Triangle (Top)
    val greenPath = Path().apply {
        moveTo(centerLeft, centerTop)
        lineTo(centerLeft + centerSize, centerTop)
        lineTo(cx, cy)
        close()
    }
    drawPath(greenPath, color = palette.greenColor)

    // Yellow Triangle (Right)
    val yellowPath = Path().apply {
        moveTo(centerLeft + centerSize, centerTop)
        lineTo(centerLeft + centerSize, centerTop + centerSize)
        lineTo(cx, cy)
        close()
    }
    drawPath(yellowPath, color = palette.yellowColor)

    // Blue Triangle (Bottom)
    val bluePath = Path().apply {
        moveTo(centerLeft, centerTop + centerSize)
        lineTo(cx, cy)
        lineTo(centerLeft + centerSize, centerTop + centerSize)
        close()
    }
    drawPath(bluePath, color = palette.blueColor)

    // Golden Central Medallion
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFD700), Color(0xFFFFA000)),
            center = Offset(cx, cy),
            radius = cellSize * 0.45f
        ),
        center = Offset(cx, cy),
        radius = cellSize * 0.42f
    )
    drawCircle(
        color = Color.White,
        center = Offset(cx, cy),
        radius = cellSize * 0.42f,
        style = Stroke(width = cellSize * 0.04f)
    )
    drawGoldenStar(
        center = Offset(cx, cy),
        radius = cellSize * 0.22f,
        color = Color.White
    )

    // Center boundary
    drawRect(
        color = palette.boardBorder,
        topLeft = Offset(centerLeft, centerTop),
        size = Size(centerSize, centerSize),
        style = Stroke(width = cellSize * 0.045f)
    )
}

private fun DrawScope.drawGoldenStar(center: Offset, radius: Float, color: Color) {
    val path = Path()
    val innerRadius = radius * 0.46f
    val numPoints = 5
    for (i in 0 until numPoints * 2) {
        val r = if (i % 2 == 0) radius else innerRadius
        val angle = (i * PI / numPoints - PI / 2).toFloat()
        val px = center.x + r * cos(angle)
        val py = center.y + r * sin(angle)
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path, color = color, style = Fill)
}

private fun DrawScope.drawBoardTokens(
    players: List<Player>,
    activePlayer: Player,
    legalTokens: List<Token>,
    animatingToken: AnimatingTokenState?,
    capturedReturningTokens: List<AnimatingTokenState>,
    cellSize: Float,
    palette: LudoBoardPalette,
    pulseScale: Float,
    haloRotation: Float,
    targetList: MutableList<Pair<Token, Offset>>
) {
    val activeTrackTokens = mutableMapOf<Int, MutableList<Token>>()

    // Collect non-animating track tokens to group overlaps
    for (p in players) {
        for (t in p.tokens) {
            val isCurrentlyMoving = (animatingToken?.let { it.tokenId == t.id && it.color == t.color } == true)
            val isCurrentlyReturning = capturedReturningTokens.any { it.tokenId == t.id && it.color == t.color }
            if (t.state == TokenState.ON_TRACK && !isCurrentlyMoving && !isCurrentlyReturning) {
                val list = activeTrackTokens.getOrPut(t.trackIndex) { mutableListOf() }
                list.add(t)
            }
        }
    }

    for (p in players) {
        for (token in p.tokens) {
            val isMoving = (animatingToken?.let { it.tokenId == token.id && it.color == token.color } == true)
            val isReturning = capturedReturningTokens.any { it.tokenId == token.id && it.color == token.color }

            // Moving tokens are drawn in the top layer
            if (isMoving || isReturning) continue

            val isLegal = legalTokens.any { it.id == token.id && it.color == token.color }

            val centerCoord = LudoBoardCoordinates.getTokenCenterCoord(token)
            var (baseX, baseY) = Pair(centerCoord.first * cellSize, centerCoord.second * cellSize)

            // Overlap handling on outer track
            val tokensOnCell = if (token.state == TokenState.ON_TRACK) {
                activeTrackTokens[token.trackIndex] ?: emptyList()
            } else emptyList()

            var scaleFactor = 1.0f
            if (tokensOnCell.size > 1) {
                val idx = tokensOnCell.indexOfFirst { it.id == token.id && it.color == token.color }
                scaleFactor = 0.78f
                val delta = cellSize * 0.17f
                when (idx % 4) {
                    0 -> { baseX -= delta; baseY -= delta }
                    1 -> { baseX += delta; baseY += delta }
                    2 -> { baseX += delta; baseY -= delta }
                    else -> { baseX -= delta; baseY += delta }
                }
            }

            val offset = Offset(baseX, baseY)
            targetList.add(Pair(token, offset))

            draw3DPawn(
                center = offset,
                color = palette.getPlayerColor(token.color),
                cellSize = cellSize,
                isLegal = isLegal,
                isHome = token.isHome,
                scaleFactor = scaleFactor,
                pulseScale = if (isLegal) pulseScale else 1f,
                haloRotation = haloRotation
            )
        }
    }
}

private fun DrawScope.drawMovingPawn(
    animatingToken: AnimatingTokenState,
    cellSize: Float,
    palette: LudoBoardPalette
) {
    val (col, row) = animatingToken.currentCoord
    val baseX = col * cellSize
    val baseY = row * cellSize
    val elevationPixels = animatingToken.hopElevation * cellSize

    // Elevated drop shadow on ground
    drawCircle(
        color = Color.Black.copy(alpha = 0.28f),
        center = Offset(baseX + cellSize * 0.05f, baseY + cellSize * 0.08f),
        radius = cellSize * 0.35f * animatingToken.scaleMultiplier
    )

    // Elevated floating pawn
    draw3DPawn(
        center = Offset(baseX, baseY - elevationPixels),
        color = palette.getPlayerColor(animatingToken.color),
        cellSize = cellSize,
        isLegal = false,
        isHome = false,
        scaleFactor = animatingToken.scaleMultiplier,
        pulseScale = 1f,
        haloRotation = 0f
    )
}

private fun DrawScope.draw3DPawn(
    center: Offset,
    color: Color,
    cellSize: Float,
    isLegal: Boolean,
    isHome: Boolean,
    scaleFactor: Float,
    pulseScale: Float,
    haloRotation: Float
) {
    val radius = cellSize * (if (isHome) 0.32f else 0.40f) * scaleFactor * pulseScale

    // Pulsing Beacon & Arrow for Legal Movable Tokens
    if (isLegal) {
        // Rotating golden aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFEA00).copy(alpha = 0.7f), Color(0xFFFF9800).copy(alpha = 0.2f), Color.Transparent),
                center = center,
                radius = radius * 1.8f
            ),
            center = center,
            radius = radius * 1.75f
        )
        drawCircle(
            color = Color.White,
            center = center,
            radius = radius * 1.35f,
            style = Stroke(width = cellSize * 0.07f)
        )
        // Indicator arrow pointing down onto the pawn
        val arrowY = center.y - radius * 1.6f
        val arrowPath = Path().apply {
            moveTo(center.x - cellSize * 0.12f, arrowY - cellSize * 0.12f)
            lineTo(center.x + cellSize * 0.12f, arrowY - cellSize * 0.12f)
            lineTo(center.x, arrowY)
            close()
        }
        drawPath(arrowPath, color = Color(0xFFFFD700))
        drawPath(arrowPath, color = Color.White, style = Stroke(width = cellSize * 0.02f))
    }

    // Pawn Base Shadow
    drawCircle(
        color = Color.Black.copy(alpha = 0.38f),
        center = Offset(center.x + cellSize * 0.03f, center.y + cellSize * 0.05f),
        radius = radius
    )

    // Tier 1: Outer Base Rim
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, color.copy(alpha = 0.85f), Color.Black.copy(alpha = 0.4f)),
            center = Offset(center.x, center.y + radius * 0.2f),
            radius = radius
        ),
        center = center,
        radius = radius
    )

    // Tier 2: Mid Collar
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.5f), color, color.copy(alpha = 0.7f)),
            center = Offset(center.x - radius * 0.25f, center.y - radius * 0.25f),
            radius = radius * 0.8f
        ),
        center = center,
        radius = radius * 0.75f
    )

    // Tier 3: Pawn Dome Head with Specular Highlight
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.95f), color),
            center = Offset(center.x - radius * 0.28f, center.y - radius * 0.30f),
            radius = radius * 0.6f
        ),
        center = Offset(center.x, center.y - radius * 0.1f),
        radius = radius * 0.52f
    )

    // Center Gold Crown / Pip
    if (isHome) {
        drawGoldenStar(
            center = Offset(center.x, center.y - radius * 0.1f),
            radius = radius * 0.30f,
            color = Color(0xFFFFD700)
        )
    } else {
        drawCircle(
            color = Color.White.copy(alpha = 0.95f),
            center = Offset(center.x, center.y - radius * 0.1f),
            radius = radius * 0.18f
        )
    }
}
