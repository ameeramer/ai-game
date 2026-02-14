package com.aigame.heartquest.game

/**
 * Handles game loop updates: player movement, NPC behavior, collision, and interaction detection.
 */
class GameEngine(private val state: GameState) {

    companion object {
        const val PLAYER_SPEED = 0.25f        // units per second (normalized coords)
        const val NPC_SPEED = 0.12f            // NPC moves slower than player
        const val INTERACTION_RANGE = 0.12f    // distance to trigger action availability
        const val CHARACTER_RADIUS = 0.025f    // collision radius
        const val OBJECT_PADDING = 0.02f       // padding around scene objects
        const val SPEECH_BUBBLE_DURATION = 4.0f // seconds a speech bubble stays visible
        const val ACTION_TEXT_DURATION = 2.0f   // seconds player action text shows
    }

    /** Update game state for one frame. deltaTime in seconds. */
    fun update(deltaTime: Float) {
        updatePlayerMovement(deltaTime)
        updateNpcBehavior(deltaTime)
        updateSpeechBubbles(deltaTime)
        updateInteractionState()
        updateRaindrops(deltaTime)
    }

    private fun updatePlayerMovement(deltaTime: Float) {
        val input = state.joystickInput.value
        if (input.length < 0.01f) {
            state.playerMoving.value = false
            return
        }

        state.playerMoving.value = true
        state.playerDirection.value = Direction.fromVelocity(input.x, input.y)

        val movement = input.normalized() * (PLAYER_SPEED * deltaTime)
        val newPos = state.playerPosition.value + movement

        // Clamp to world bounds
        val clampedX = newPos.x.coerceIn(CHARACTER_RADIUS, 1f - CHARACTER_RADIUS)
        val clampedY = newPos.y.coerceIn(CHARACTER_RADIUS, 1f - CHARACTER_RADIUS)
        val clampedPos = Vec2(clampedX, clampedY)

        // Check collision with scene objects
        if (!collidesWithObjects(clampedPos)) {
            state.playerPosition.value = clampedPos
        }
    }

    private fun updateNpcBehavior(deltaTime: Float) {
        val behavior = state.npcBehavior.value
        val npcPos = state.npcPosition.value
        val playerPos = state.playerPosition.value

        when (behavior) {
            NpcBehavior.APPROACHING_PLAYER -> {
                val dir = (playerPos - npcPos).normalized()
                val dist = npcPos.distanceTo(playerPos)
                if (dist > INTERACTION_RANGE * 0.7f) {
                    val newPos = npcPos + dir * (NPC_SPEED * deltaTime)
                    state.npcPosition.value = newPos
                    state.npcDirection.value = Direction.fromVelocity(dir.x, dir.y)
                } else {
                    state.npcBehavior.value = NpcBehavior.IDLE
                    state.npcDirection.value = Direction.fromVelocity(
                        playerPos.x - npcPos.x, playerPos.y - npcPos.y
                    )
                }
            }
            NpcBehavior.STEPPING_BACK -> {
                val dir = (npcPos - playerPos).normalized()
                val newPos = npcPos + dir * (NPC_SPEED * deltaTime)
                val clampedX = newPos.x.coerceIn(CHARACTER_RADIUS, 1f - CHARACTER_RADIUS)
                val clampedY = newPos.y.coerceIn(CHARACTER_RADIUS, 1f - CHARACTER_RADIUS)
                state.npcPosition.value = Vec2(clampedX, clampedY)

                // Step back briefly then go idle
                state.npcStepBackTimer.value -= deltaTime
                if (state.npcStepBackTimer.value <= 0f) {
                    state.npcBehavior.value = NpcBehavior.IDLE
                }
            }
            NpcBehavior.WALKING -> {
                val target = state.npcWalkTarget.value
                val dir = (target - npcPos).normalized()
                val dist = npcPos.distanceTo(target)
                if (dist > 0.02f) {
                    val newPos = npcPos + dir * (NPC_SPEED * deltaTime)
                    state.npcPosition.value = newPos
                    state.npcDirection.value = Direction.fromVelocity(dir.x, dir.y)
                } else {
                    state.npcPosition.value = target
                    state.npcBehavior.value = NpcBehavior.IDLE
                }
            }
            NpcBehavior.EMOTING -> {
                state.npcEmoteTimer.value -= deltaTime
                if (state.npcEmoteTimer.value <= 0f) {
                    state.npcBehavior.value = NpcBehavior.IDLE
                }
            }
            NpcBehavior.TURNED_AWAY -> {
                // Face away from player
                val awayDir = (npcPos - playerPos).normalized()
                state.npcDirection.value = Direction.fromVelocity(awayDir.x, awayDir.y)
            }
            NpcBehavior.IDLE -> {
                // Face player if close enough
                val dist = npcPos.distanceTo(playerPos)
                if (dist < INTERACTION_RANGE * 1.5f) {
                    val dir = playerPos - npcPos
                    state.npcDirection.value = Direction.fromVelocity(dir.x, dir.y)
                }
            }
        }
    }

    private fun updateSpeechBubbles(deltaTime: Float) {
        // NPC speech bubble timer
        if (state.npcSpeechText.value.isNotEmpty()) {
            state.npcSpeechTimer.value -= deltaTime
            if (state.npcSpeechTimer.value <= 0f) {
                state.npcSpeechText.value = ""
            }
        }

        // Player action text timer
        if (state.playerActionText.value.isNotEmpty()) {
            state.playerActionTimer.value -= deltaTime
            if (state.playerActionTimer.value <= 0f) {
                state.playerActionText.value = ""
            }
        }
    }

    private fun updateInteractionState() {
        val dist = state.playerPosition.value.distanceTo(state.npcPosition.value)
        state.isNearNpc.value = dist < INTERACTION_RANGE
    }

    private fun updateRaindrops(deltaTime: Float) {
        if (state.currentScene.value?.weatherEffect != WeatherEffect.RAIN &&
            state.currentScene.value?.weatherEffect != WeatherEffect.FIREFLIES &&
            state.currentScene.value?.weatherEffect != WeatherEffect.SPARKLES
        ) return

        // Update particle timer for animation
        state.particleTimer.value += deltaTime
    }

    private fun collidesWithObjects(pos: Vec2): Boolean {
        val scene = state.currentScene.value ?: return false
        for (obj in scene.objects) {
            val halfW = obj.width / 2f + OBJECT_PADDING
            val halfH = obj.height / 2f + OBJECT_PADDING
            if (pos.x > obj.position.x - halfW && pos.x < obj.position.x + halfW &&
                pos.y > obj.position.y - halfH && pos.y < obj.position.y + halfH
            ) {
                return true
            }
        }
        return false
    }

    /** Start NPC approach toward player. */
    fun triggerNpcApproach() {
        state.npcBehavior.value = NpcBehavior.APPROACHING_PLAYER
    }

    /** Start NPC stepping back. */
    fun triggerNpcStepBack() {
        state.npcBehavior.value = NpcBehavior.STEPPING_BACK
        state.npcStepBackTimer.value = 0.5f
    }

    /** Make NPC turn away. */
    fun triggerNpcTurnAway() {
        state.npcBehavior.value = NpcBehavior.TURNED_AWAY
    }

    /** Make NPC emote (brief animation). */
    fun triggerNpcEmote() {
        state.npcBehavior.value = NpcBehavior.EMOTING
        state.npcEmoteTimer.value = 1.0f
    }

    /** Show NPC speech bubble. */
    fun showNpcSpeech(text: String) {
        state.npcSpeechText.value = text
        state.npcSpeechTimer.value = SPEECH_BUBBLE_DURATION +
            (text.length / 30f).coerceAtMost(3f) // longer text stays longer
    }

    /** Show player action text above character. */
    fun showPlayerAction(text: String) {
        state.playerActionText.value = text
        state.playerActionTimer.value = ACTION_TEXT_DURATION
    }

    /** Apply NPC action from AI response. */
    fun applyNpcAction(action: String) {
        when (action.lowercase().trim()) {
            "approach_player" -> triggerNpcApproach()
            "step_back" -> triggerNpcStepBack()
            "turn_away" -> triggerNpcTurnAway()
            "emote" -> triggerNpcEmote()
            "face_player" -> {
                state.npcBehavior.value = NpcBehavior.IDLE
            }
            else -> {
                // Default: face player
                state.npcBehavior.value = NpcBehavior.IDLE
            }
        }
    }
}
