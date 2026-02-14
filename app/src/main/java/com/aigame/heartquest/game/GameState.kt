package com.aigame.heartquest.game

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

data class Mission(
    val id: Int,
    val title: String,
    val description: String,
    val scenario: String,
    val targetAffectionGain: Int,
    val completionThreshold: Int = 3
)

data class MissionAnalysis(
    val summary: String,
    val affectionChange: Int,
    val npcMood: String,
    val advice: String
)

data class InteractionRecord(
    val actionId: String,
    val actionLabel: String,
    val npcDialogue: String,
    val npcMood: String,
    val timestamp: Long = System.currentTimeMillis()
)

class GameState {
    // --- Mission State ---
    val currentMissionIndex = mutableStateOf(0)
    val affectionLevel = mutableStateOf(0)
    val interactionCount = mutableStateOf(0)
    val isLoading = mutableStateOf(false)
    val npcMood = mutableStateOf("neutral")
    val missionAnalysis = mutableStateOf<MissionAnalysis?>(null)
    val isMissionComplete = mutableStateOf(false)
    val gameStarted = mutableStateOf(false)
    val interactionHistory = mutableStateListOf<InteractionRecord>()

    // --- 2D World State ---
    val playerPosition = mutableStateOf(Vec2(0.5f, 0.8f))
    val playerDirection = mutableStateOf(Direction.UP)
    val playerMoving = mutableStateOf(false)

    val npcPosition = mutableStateOf(Vec2(0.5f, 0.3f))
    val npcDirection = mutableStateOf(Direction.DOWN)
    val npcBehavior = mutableStateOf(NpcBehavior.IDLE)
    val npcWalkTarget = mutableStateOf(Vec2.ZERO)
    val npcStepBackTimer = mutableFloatStateOf(0f)
    val npcEmoteTimer = mutableFloatStateOf(0f)

    // --- Speech / Action Display ---
    val npcSpeechText = mutableStateOf("")
    val npcSpeechTimer = mutableFloatStateOf(0f)
    val playerActionText = mutableStateOf("")
    val playerActionTimer = mutableFloatStateOf(0f)

    // --- Interaction ---
    val isNearNpc = mutableStateOf(false)
    val joystickInput = mutableStateOf(Vec2.ZERO)

    // --- Scene ---
    val currentScene = mutableStateOf<SceneDefinition?>(null)

    // --- Particles ---
    val particleTimer = mutableFloatStateOf(0f)

    // --- Affection change display ---
    val lastAffectionDelta = mutableStateOf(0)
    val affectionDeltaTimer = mutableFloatStateOf(0f)

    val currentMission: Mission
        get() = MissionManager.missions.getOrElse(currentMissionIndex.value) {
            MissionManager.missions.last()
        }

    fun reset() {
        currentMissionIndex.value = 0
        affectionLevel.value = 0
        interactionCount.value = 0
        isLoading.value = false
        npcMood.value = "neutral"
        missionAnalysis.value = null
        isMissionComplete.value = false
        interactionHistory.clear()
        npcSpeechText.value = ""
        playerActionText.value = ""
        npcBehavior.value = NpcBehavior.IDLE
        lastAffectionDelta.value = 0
        particleTimer.floatValue = 0f
    }

    fun startMission() {
        interactionCount.value = 0
        isLoading.value = false
        missionAnalysis.value = null
        isMissionComplete.value = false
        interactionHistory.clear()
        npcBehavior.value = NpcBehavior.IDLE
        npcSpeechText.value = ""
        playerActionText.value = ""
        lastAffectionDelta.value = 0
        particleTimer.floatValue = 0f
        gameStarted.value = true

        val scene = SceneDefinitions.getScene(currentMissionIndex.value)
        currentScene.value = scene
        playerPosition.value = scene.playerSpawn
        npcPosition.value = scene.npcSpawn
        playerDirection.value = Direction.UP
        npcDirection.value = Direction.DOWN
        joystickInput.value = Vec2.ZERO
    }

    fun advanceToNextMission() {
        if (currentMissionIndex.value < MissionManager.missions.size - 1) {
            currentMissionIndex.value++
            startMission()
        }
    }

    fun recordInteraction(actionId: String, actionLabel: String, npcDialogue: String, npcMood: String) {
        interactionHistory.add(
            InteractionRecord(
                actionId = actionId,
                actionLabel = actionLabel,
                npcDialogue = npcDialogue,
                npcMood = npcMood
            )
        )
        interactionCount.value++
    }

    fun showAffectionDelta(delta: Int) {
        lastAffectionDelta.value = delta
        affectionDeltaTimer.floatValue = 2.0f
    }
}
