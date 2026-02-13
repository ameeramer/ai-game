package com.aigame.heartquest.game

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

/**
 * Core game state holding all runtime data for a play session.
 */
data class Mission(
    val id: Int,
    val title: String,
    val description: String,
    val scenario: String,
    val targetAffectionGain: Int,
    val completionThreshold: Int = 3 // minimum interactions to complete
)

data class ChatMessage(
    val sender: String, // "player" or "npc" or "system" or "narrator"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class MissionAnalysis(
    val summary: String,
    val affectionChange: Int,
    val npcMood: String,
    val advice: String
)

class GameState {
    val currentMissionIndex = mutableStateOf(0)
    val affectionLevel = mutableStateOf(0) // 0 to 100
    val chatHistory = mutableStateListOf<ChatMessage>()
    val interactionCount = mutableStateOf(0)
    val isLoading = mutableStateOf(false)
    val npcMood = mutableStateOf("neutral") // neutral, happy, flirty, annoyed, shy
    val missionAnalysis = mutableStateOf<MissionAnalysis?>(null)
    val isMissionComplete = mutableStateOf(false)
    val gameStarted = mutableStateOf(false)
    val npcAnimationState = mutableStateOf(NpcAnimation.IDLE)

    val currentMission: Mission
        get() = MissionManager.missions.getOrElse(currentMissionIndex.value) {
            MissionManager.missions.last()
        }

    fun reset() {
        currentMissionIndex.value = 0
        affectionLevel.value = 0
        chatHistory.clear()
        interactionCount.value = 0
        isLoading.value = false
        npcMood.value = "neutral"
        missionAnalysis.value = null
        isMissionComplete.value = false
        npcAnimationState.value = NpcAnimation.IDLE
    }

    fun startMission() {
        chatHistory.clear()
        interactionCount.value = 0
        isLoading.value = false
        missionAnalysis.value = null
        isMissionComplete.value = false
        npcAnimationState.value = NpcAnimation.IDLE
        gameStarted.value = true

        // Add scene-setting narrator message
        chatHistory.add(
            ChatMessage(
                sender = "narrator",
                text = currentMission.scenario
            )
        )
    }

    fun advanceToNextMission() {
        if (currentMissionIndex.value < MissionManager.missions.size - 1) {
            currentMissionIndex.value++
            startMission()
        }
    }

    fun addPlayerMessage(text: String) {
        chatHistory.add(ChatMessage(sender = "player", text = text))
        interactionCount.value++
    }

    fun addNpcMessage(text: String) {
        chatHistory.add(ChatMessage(sender = "npc", text = text))
    }

    fun addSystemMessage(text: String) {
        chatHistory.add(ChatMessage(sender = "system", text = text))
    }

    fun updateMoodFromString(mood: String) {
        npcMood.value = mood.lowercase().trim()
        npcAnimationState.value = when (npcMood.value) {
            "happy" -> NpcAnimation.HAPPY
            "flirty" -> NpcAnimation.FLIRTY
            "annoyed" -> NpcAnimation.ANNOYED
            "shy" -> NpcAnimation.SHY
            "laughing" -> NpcAnimation.HAPPY
            "blushing" -> NpcAnimation.SHY
            else -> NpcAnimation.IDLE
        }
    }
}

enum class NpcAnimation {
    IDLE, HAPPY, FLIRTY, ANNOYED, SHY, TALKING
}
