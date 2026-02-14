package com.aigame.heartquest.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.aigame.heartquest.game.Vec2
import kotlin.math.sqrt

@Composable
fun VirtualJoystick(
    onInput: (Vec2) -> Unit,
    modifier: Modifier = Modifier
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    val baseRadius = 140f / 2f // half of 140dp visual size
    val thumbRadius = 24f

    Canvas(
        modifier = modifier
            .size(140.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val delta = offset - center
                        thumbOffset = clampToCircle(delta, size.width / 2f * 0.7f)
                        val normalized = normalizeJoystick(thumbOffset, size.width / 2f * 0.7f)
                        onInput(Vec2(normalized.x, normalized.y))
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = thumbOffset + dragAmount
                        thumbOffset = clampToCircle(newOffset, size.width / 2f * 0.7f)
                        val normalized = normalizeJoystick(thumbOffset, size.width / 2f * 0.7f)
                        onInput(Vec2(normalized.x, normalized.y))
                    },
                    onDragEnd = {
                        isDragging = false
                        thumbOffset = Offset.Zero
                        onInput(Vec2.ZERO)
                    },
                    onDragCancel = {
                        isDragging = false
                        thumbOffset = Offset.Zero
                        onInput(Vec2.ZERO)
                    }
                )
            }
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.width / 2f

        // Outer ring
        drawCircle(
            color = Color.White.copy(alpha = if (isDragging) 0.15f else 0.08f),
            radius = outerRadius,
            center = center
        )
        drawCircle(
            color = Color.White.copy(alpha = if (isDragging) 0.25f else 0.12f),
            radius = outerRadius,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )

        // Directional guides
        val guideAlpha = if (isDragging) 0.1f else 0.05f
        val guideLen = outerRadius * 0.3f
        drawLine(Color.White.copy(alpha = guideAlpha), Offset(center.x, center.y - outerRadius * 0.4f), Offset(center.x, center.y - outerRadius * 0.4f - guideLen), strokeWidth = 1.5f)
        drawLine(Color.White.copy(alpha = guideAlpha), Offset(center.x, center.y + outerRadius * 0.4f), Offset(center.x, center.y + outerRadius * 0.4f + guideLen), strokeWidth = 1.5f)
        drawLine(Color.White.copy(alpha = guideAlpha), Offset(center.x - outerRadius * 0.4f, center.y), Offset(center.x - outerRadius * 0.4f - guideLen, center.y), strokeWidth = 1.5f)
        drawLine(Color.White.copy(alpha = guideAlpha), Offset(center.x + outerRadius * 0.4f, center.y), Offset(center.x + outerRadius * 0.4f + guideLen, center.y), strokeWidth = 1.5f)

        // Thumb
        val thumbCenter = center + thumbOffset
        drawCircle(
            color = Color.White.copy(alpha = if (isDragging) 0.5f else 0.2f),
            radius = thumbRadius,
            center = thumbCenter
        )
        drawCircle(
            color = Color.White.copy(alpha = if (isDragging) 0.7f else 0.3f),
            radius = thumbRadius,
            center = thumbCenter,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )
    }
}

private fun clampToCircle(offset: Offset, maxRadius: Float): Offset {
    val dist = sqrt(offset.x * offset.x + offset.y * offset.y)
    return if (dist > maxRadius) {
        val scale = maxRadius / dist
        Offset(offset.x * scale, offset.y * scale)
    } else {
        offset
    }
}

private fun normalizeJoystick(offset: Offset, maxRadius: Float): Offset {
    return Offset(
        (offset.x / maxRadius).coerceIn(-1f, 1f),
        (offset.y / maxRadius).coerceIn(-1f, 1f)
    )
}
