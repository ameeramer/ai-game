package com.aigame.heartquest.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.aigame.heartquest.game.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameCanvas(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Read all state values to trigger recomposition
    val scene = gameState.currentScene.value
    val playerPos = gameState.playerPosition.value
    val playerDir = gameState.playerDirection.value
    val playerMoving = gameState.playerMoving.value
    val npcPos = gameState.npcPosition.value
    val npcDir = gameState.npcDirection.value
    val npcBehavior = gameState.npcBehavior.value
    val npcMood = gameState.npcMood.value
    val npcSpeech = gameState.npcSpeechText.value
    val npcSpeechTimer = gameState.npcSpeechTimer.floatValue
    val playerAction = gameState.playerActionText.value
    val playerActionTimer = gameState.playerActionTimer.floatValue
    val isNear = gameState.isNearNpc.value
    val particleTime = gameState.particleTimer.floatValue
    val isLoading = gameState.isLoading.value
    val lastDelta = gameState.lastAffectionDelta.value
    val deltaTimer = gameState.affectionDeltaTimer.floatValue

    Canvas(modifier = modifier) {
        if (scene == null) return@Canvas

        val w = size.width
        val h = size.height

        // Draw background
        drawRect(Color(scene.backgroundColor))

        // Draw floor
        drawFloor(scene, w, h)

        // Draw weather effects behind characters
        if (scene.weatherEffect != WeatherEffect.NONE) {
            drawWeatherBehind(scene.weatherEffect, w, h, particleTime)
        }

        // Draw scene objects
        for (obj in scene.objects) {
            drawSceneObject(obj, w, h)
        }

        // Draw interaction range indicator
        if (isNear) {
            val cx = npcPos.x * w
            val cy = npcPos.y * h
            drawCircle(
                color = Color(0x22FFD700),
                radius = GameEngine.INTERACTION_RANGE * w,
                center = Offset(cx, cy)
            )
        }

        // Draw characters (player behind if lower on screen, NPC behind if lower)
        if (playerPos.y < npcPos.y) {
            drawNpc(npcPos, npcDir, npcBehavior, npcMood, w, h, particleTime)
            drawPlayer(playerPos, playerDir, playerMoving, w, h, particleTime)
        } else {
            drawPlayer(playerPos, playerDir, playerMoving, w, h, particleTime)
            drawNpc(npcPos, npcDir, npcBehavior, npcMood, w, h, particleTime)
        }

        // Draw weather effects in front
        if (scene.weatherEffect == WeatherEffect.RAIN) {
            drawRain(w, h, particleTime)
        }

        // Draw player action text
        if (playerAction.isNotEmpty() && playerActionTimer > 0f) {
            val alpha = (playerActionTimer / GameEngine.ACTION_TEXT_DURATION).coerceIn(0f, 1f)
            drawActionText(textMeasurer, playerAction, playerPos, w, h, alpha)
        }

        // Draw NPC speech bubble
        if (npcSpeech.isNotEmpty() && npcSpeechTimer > 0f) {
            val alpha = (npcSpeechTimer / 1f).coerceIn(0f, 1f)
            drawSpeechBubble(textMeasurer, npcSpeech, npcPos, w, h, alpha)
        }

        // Draw loading indicator over NPC
        if (isLoading) {
            drawLoadingDots(npcPos, w, h, particleTime)
        }

        // Draw affection change popup
        if (lastDelta != 0 && deltaTimer > 0f) {
            val alpha = (deltaTimer / 1f).coerceIn(0f, 1f)
            drawAffectionDelta(textMeasurer, lastDelta, npcPos, w, h, alpha)
        }
    }
}

private fun DrawScope.drawFloor(scene: SceneDefinition, w: Float, h: Float) {
    drawRect(
        color = Color(scene.floorColor),
        topLeft = Offset(0f, 0f),
        size = Size(w, h)
    )

    // Subtle grid pattern
    val gridColor = Color.White.copy(alpha = 0.04f)
    val gridSpacing = w / 12f
    var x = gridSpacing
    while (x < w) {
        drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
        x += gridSpacing
    }
    var y = gridSpacing
    while (y < h) {
        drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
        y += gridSpacing
    }
}

private fun DrawScope.drawSceneObject(obj: SceneObject, w: Float, h: Float) {
    val cx = obj.position.x * w
    val cy = obj.position.y * h
    val objW = obj.width * w
    val objH = obj.height * h

    // Shadow
    if (obj.isRound) {
        drawOval(
            color = Color.Black.copy(alpha = 0.2f),
            topLeft = Offset(cx - objW / 2f + 2f, cy - objH / 2f + 2f),
            size = Size(objW, objH)
        )
    }

    if (obj.isRound) {
        drawOval(
            color = Color(obj.color),
            topLeft = Offset(cx - objW / 2f, cy - objH / 2f),
            size = Size(objW, objH)
        )
        // Highlight
        drawOval(
            color = Color.White.copy(alpha = 0.1f),
            topLeft = Offset(cx - objW / 4f, cy - objH / 3f),
            size = Size(objW / 2.5f, objH / 3f)
        )
    } else {
        drawRoundRect(
            color = Color(obj.color),
            topLeft = Offset(cx - objW / 2f, cy - objH / 2f),
            size = Size(objW, objH),
            cornerRadius = CornerRadius(4f, 4f)
        )
        // Top edge highlight
        drawRoundRect(
            color = Color.White.copy(alpha = 0.08f),
            topLeft = Offset(cx - objW / 2f, cy - objH / 2f),
            size = Size(objW, objH / 4f),
            cornerRadius = CornerRadius(4f, 4f)
        )
    }
}

private fun DrawScope.drawPlayer(
    pos: Vec2, dir: Direction, moving: Boolean, w: Float, h: Float, time: Float
) {
    val cx = pos.x * w
    val cy = pos.y * h
    val radius = GameEngine.CHARACTER_RADIUS * w

    // Walk bob
    val bob = if (moving) sin(time * 12f) * 2f else 0f

    // Shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.25f),
        topLeft = Offset(cx - radius * 0.8f, cy + radius * 0.6f),
        size = Size(radius * 1.6f, radius * 0.5f)
    )

    // Body
    drawRoundRect(
        color = Color(0xFF42A5F5), // Blue outfit
        topLeft = Offset(cx - radius * 0.6f, cy - radius * 0.3f + bob),
        size = Size(radius * 1.2f, radius * 1.2f),
        cornerRadius = CornerRadius(radius * 0.3f, radius * 0.3f)
    )

    // Head
    drawCircle(
        color = Color(0xFFFFCC80), // Skin
        radius = radius * 0.55f,
        center = Offset(cx, cy - radius * 0.6f + bob)
    )

    // Hair
    drawArc(
        color = Color(0xFF5D4037),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(cx - radius * 0.55f, cy - radius * 1.15f + bob),
        size = Size(radius * 1.1f, radius * 0.7f)
    )

    // Eyes based on direction
    drawCharacterEyes(cx, cy - radius * 0.6f + bob, radius * 0.55f, dir, Color(0xFF3E2723))

    // "You" label
    drawCircle(
        color = Color(0xFF1565C0),
        radius = radius * 0.25f,
        center = Offset(cx, cy - radius * 1.3f + bob)
    )
}

private fun DrawScope.drawNpc(
    pos: Vec2, dir: Direction, behavior: NpcBehavior, mood: String,
    w: Float, h: Float, time: Float
) {
    val cx = pos.x * w
    val cy = pos.y * h
    val radius = GameEngine.CHARACTER_RADIUS * w

    // Emote bounce
    val emoteOffset = if (behavior == NpcBehavior.EMOTING) {
        sin(time * 10f) * 4f
    } else 0f

    // Walk bob
    val walkBob = if (behavior == NpcBehavior.WALKING ||
        behavior == NpcBehavior.APPROACHING_PLAYER ||
        behavior == NpcBehavior.STEPPING_BACK
    ) {
        sin(time * 10f) * 2f
    } else 0f

    val bob = emoteOffset + walkBob

    // Shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.25f),
        topLeft = Offset(cx - radius * 0.8f, cy + radius * 0.6f),
        size = Size(radius * 1.6f, radius * 0.5f)
    )

    // Body - navy blazer
    drawRoundRect(
        color = Color(0xFF1A237E),
        topLeft = Offset(cx - radius * 0.65f, cy - radius * 0.3f + bob),
        size = Size(radius * 1.3f, radius * 1.3f),
        cornerRadius = CornerRadius(radius * 0.3f, radius * 0.3f)
    )

    // Shirt collar detail
    drawRoundRect(
        color = Color(0xFFF5F5F5),
        topLeft = Offset(cx - radius * 0.2f, cy - radius * 0.3f + bob),
        size = Size(radius * 0.4f, radius * 0.4f),
        cornerRadius = CornerRadius(radius * 0.1f, radius * 0.1f)
    )

    // Head
    drawCircle(
        color = Color(0xFFFFCC80), // Skin
        radius = radius * 0.6f,
        center = Offset(cx, cy - radius * 0.65f + bob)
    )

    // Hair - styled dark
    drawArc(
        color = Color(0xFF3E2723),
        startAngle = 160f,
        sweepAngle = 220f,
        useCenter = true,
        topLeft = Offset(cx - radius * 0.65f, cy - radius * 1.3f + bob),
        size = Size(radius * 1.3f, radius * 0.85f)
    )

    // Eyes
    val eyeDir = if (behavior == NpcBehavior.TURNED_AWAY) {
        when (dir) {
            Direction.UP -> Direction.UP
            Direction.DOWN -> Direction.DOWN
            Direction.LEFT -> Direction.LEFT
            Direction.RIGHT -> Direction.RIGHT
        }
    } else dir
    drawCharacterEyes(cx, cy - radius * 0.65f + bob, radius * 0.6f, eyeDir, Color(0xFF1B5E20))

    // Mood indicator above head
    val moodColor = when (mood) {
        "happy" -> Color(0xFF4CAF50)
        "flirty" -> Color(0xFFFF6B9D)
        "annoyed" -> Color(0xFFFF9800)
        "shy" -> Color(0xFFFFC3D6)
        else -> Color(0xFF888888)
    }
    drawCircle(
        color = moodColor,
        radius = radius * 0.2f,
        center = Offset(cx, cy - radius * 1.5f + bob)
    )

    // Mood particles
    if (mood == "happy" || mood == "flirty") {
        for (i in 0..2) {
            val angle = time * 2f + i * (2f * PI.toFloat() / 3f)
            val px = cx + cos(angle) * radius * 1.2f
            val py = cy - radius * 1.5f + sin(angle) * radius * 0.3f + bob
            drawCircle(
                color = moodColor.copy(alpha = 0.5f),
                radius = 3f,
                center = Offset(px, py)
            )
        }
    }

    // "Adrian" label
    drawRoundRect(
        color = Color(0xFF880E4F),
        topLeft = Offset(cx - radius * 0.8f, cy - radius * 1.85f + bob),
        size = Size(radius * 1.6f, radius * 0.3f),
        cornerRadius = CornerRadius(radius * 0.1f, radius * 0.1f)
    )
}

private fun DrawScope.drawCharacterEyes(
    cx: Float, cy: Float, headRadius: Float, dir: Direction, eyeColor: Color
) {
    val eyeSize = headRadius * 0.12f
    val eyeOffset = headRadius * 0.25f

    when (dir) {
        Direction.DOWN, Direction.UP -> {
            val yOffset = if (dir == Direction.DOWN) headRadius * 0.05f else -headRadius * 0.1f
            // Only draw eyes if facing down (toward camera) or to sides
            if (dir == Direction.DOWN) {
                drawCircle(eyeColor, eyeSize, Offset(cx - eyeOffset, cy + yOffset))
                drawCircle(eyeColor, eyeSize, Offset(cx + eyeOffset, cy + yOffset))
            }
            // If UP, we see the back of the head - no eyes
        }
        Direction.LEFT -> {
            drawCircle(eyeColor, eyeSize, Offset(cx - headRadius * 0.3f, cy - headRadius * 0.05f))
        }
        Direction.RIGHT -> {
            drawCircle(eyeColor, eyeSize, Offset(cx + headRadius * 0.3f, cy - headRadius * 0.05f))
        }
    }
}

private fun DrawScope.drawSpeechBubble(
    textMeasurer: TextMeasurer, text: String, charPos: Vec2,
    w: Float, h: Float, alpha: Float
) {
    val cx = charPos.x * w
    val cy = charPos.y * h
    val radius = GameEngine.CHARACTER_RADIUS * w

    val maxBubbleWidth = w * 0.6f
    val padding = 12f

    val style = TextStyle(
        color = Color(0xFF1A1A2E).copy(alpha = alpha),
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    )
    val measured = textMeasurer.measure(
        text = text,
        style = style,
        maxLines = 4,
        overflow = TextOverflow.Ellipsis,
        constraints = androidx.compose.ui.unit.Constraints(
            maxWidth = (maxBubbleWidth - padding * 2).toInt()
        )
    )

    val bubbleWidth = measured.size.width + padding * 2
    val bubbleHeight = measured.size.height + padding * 2
    val bubbleX = (cx - bubbleWidth / 2f).coerceIn(4f, w - bubbleWidth - 4f)
    val bubbleY = cy - radius * 2.2f - bubbleHeight

    // Bubble background
    drawRoundRect(
        color = Color.White.copy(alpha = alpha * 0.95f),
        topLeft = Offset(bubbleX, bubbleY),
        size = Size(bubbleWidth, bubbleHeight),
        cornerRadius = CornerRadius(12f, 12f)
    )

    // Bubble tail
    val tailPath = Path().apply {
        moveTo(cx - 6f, bubbleY + bubbleHeight)
        lineTo(cx, bubbleY + bubbleHeight + 8f)
        lineTo(cx + 6f, bubbleY + bubbleHeight)
        close()
    }
    drawPath(tailPath, Color.White.copy(alpha = alpha * 0.95f))

    // Text
    drawText(
        textLayoutResult = measured,
        topLeft = Offset(bubbleX + padding, bubbleY + padding)
    )
}

private fun DrawScope.drawActionText(
    textMeasurer: TextMeasurer, text: String, charPos: Vec2,
    w: Float, h: Float, alpha: Float
) {
    val cx = charPos.x * w
    val cy = charPos.y * h
    val radius = GameEngine.CHARACTER_RADIUS * w

    val style = TextStyle(
        color = Color(0xFFFFD700).copy(alpha = alpha),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
    )
    val displayText = "* $text *"
    val measured = textMeasurer.measure(displayText, style)

    val textX = cx - measured.size.width / 2f
    val textY = cy - radius * 2f - measured.size.height

    // Background
    drawRoundRect(
        color = Color(0xFF1A1A2E).copy(alpha = alpha * 0.7f),
        topLeft = Offset(textX - 6f, textY - 4f),
        size = Size(measured.size.width + 12f, measured.size.height + 8f),
        cornerRadius = CornerRadius(8f, 8f)
    )

    drawText(measured, topLeft = Offset(textX, textY))
}

private fun DrawScope.drawLoadingDots(charPos: Vec2, w: Float, h: Float, time: Float) {
    val cx = charPos.x * w
    val cy = charPos.y * h
    val radius = GameEngine.CHARACTER_RADIUS * w

    for (i in 0..2) {
        val dotAlpha = ((sin(time * 4f + i * 0.8f) + 1f) / 2f).coerceIn(0.2f, 0.9f)
        drawCircle(
            color = Color.White.copy(alpha = dotAlpha),
            radius = 4f,
            center = Offset(cx - 12f + i * 12f, cy - radius * 2.3f)
        )
    }
}

private fun DrawScope.drawAffectionDelta(
    textMeasurer: TextMeasurer, delta: Int, charPos: Vec2,
    w: Float, h: Float, alpha: Float
) {
    val cx = charPos.x * w
    val cy = charPos.y * h
    val radius = GameEngine.CHARACTER_RADIUS * w

    val text = if (delta > 0) "+$delta \u2764" else "$delta \u2764"
    val color = if (delta > 0) Color(0xFFFF1744) else Color(0xFF888888)
    val style = TextStyle(
        color = color.copy(alpha = alpha),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    val measured = textMeasurer.measure(text, style)
    val floatUp = (1f - alpha) * 30f

    drawText(
        measured,
        topLeft = Offset(
            cx - measured.size.width / 2f + radius * 1.5f,
            cy - radius * 1.5f - floatUp
        )
    )
}

private fun DrawScope.drawWeatherBehind(
    effect: WeatherEffect, w: Float, h: Float, time: Float
) {
    when (effect) {
        WeatherEffect.FIREFLIES -> {
            for (i in 0..15) {
                val seed = i * 137.5f
                val fx = ((seed * 0.7f + time * 15f * (0.5f + (i % 3) * 0.2f)) % w)
                val fy = ((seed * 1.3f + sin(time + seed) * 30f) % h)
                val flicker = ((sin(time * 3f + seed) + 1f) / 2f).coerceIn(0.1f, 0.8f)
                drawCircle(
                    color = Color(0xFFFFEB3B).copy(alpha = flicker),
                    radius = 3f + sin(time + seed) * 1.5f,
                    center = Offset(fx, fy)
                )
                // Glow
                drawCircle(
                    color = Color(0xFFFFEB3B).copy(alpha = flicker * 0.2f),
                    radius = 8f,
                    center = Offset(fx, fy)
                )
            }
        }
        WeatherEffect.SPARKLES -> {
            for (i in 0..10) {
                val seed = i * 97.3f
                val sx = ((seed * 1.1f + time * 8f) % w)
                val sy = ((seed * 0.9f + time * 5f) % h)
                val sparkle = ((sin(time * 5f + seed) + 1f) / 2f).coerceIn(0f, 0.6f)
                drawCircle(
                    color = Color.White.copy(alpha = sparkle),
                    radius = 2f,
                    center = Offset(sx, sy)
                )
            }
        }
        else -> {}
    }
}

private fun DrawScope.drawRain(w: Float, h: Float, time: Float) {
    val rainColor = Color(0xFF64B5F6).copy(alpha = 0.4f)
    for (i in 0..40) {
        val seed = i * 73.7f
        val rx = (seed * 1.3f) % w
        val ry = ((seed * 0.7f + time * 600f) % (h + 40f)) - 20f
        drawLine(
            color = rainColor,
            start = Offset(rx, ry),
            end = Offset(rx - 2f, ry + 12f),
            strokeWidth = 1.5f
        )
    }
}
