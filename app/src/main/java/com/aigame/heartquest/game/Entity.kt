package com.aigame.heartquest.game

import kotlin.math.abs
import kotlin.math.sqrt

data class Vec2(val x: Float, val y: Float) {
    operator fun plus(other: Vec2) = Vec2(x + other.x, y + other.y)
    operator fun minus(other: Vec2) = Vec2(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vec2(x * scalar, y * scalar)

    fun distanceTo(other: Vec2): Float {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt(dx * dx + dy * dy)
    }

    fun normalized(): Vec2 {
        val len = length
        return if (len > 0.001f) Vec2(x / len, y / len) else Vec2(0f, 0f)
    }

    val length: Float get() = sqrt(x * x + y * y)

    companion object {
        val ZERO = Vec2(0f, 0f)
    }
}

enum class Direction {
    UP, DOWN, LEFT, RIGHT;

    companion object {
        fun fromVelocity(vx: Float, vy: Float): Direction {
            return if (abs(vx) > abs(vy)) {
                if (vx > 0) RIGHT else LEFT
            } else {
                if (vy > 0) DOWN else UP
            }
        }
    }
}

enum class NpcBehavior {
    IDLE,
    WALKING,
    APPROACHING_PLAYER,
    STEPPING_BACK,
    TURNED_AWAY,
    EMOTING
}

data class SceneObject(
    val id: String,
    val label: String,
    val position: Vec2,
    val width: Float,
    val height: Float,
    val color: Long,
    val isRound: Boolean = false
)

data class PlayerAction(
    val id: String,
    val label: String,
    val emoji: String,
    val requiresProximity: Boolean = true
)
