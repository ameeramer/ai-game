package com.aigame.heartquest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aigame.heartquest.ai.ClaudeAIService
import com.aigame.heartquest.data.PreferencesManager
import com.aigame.heartquest.game.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val gameState = GameState()
    val preferencesManager = PreferencesManager(application)
    private val aiService = ClaudeAIService()

    private val _apiKey = MutableStateFlow("")
    val apiKey = _apiKey.asStateFlow()

    private val _playerName = MutableStateFlow("You")
    val playerName = _playerName.asStateFlow()

    private val _hasSavedGame = MutableStateFlow(false)
    val hasSavedGame = _hasSavedGame.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            _apiKey.value = preferencesManager.apiKey.first()
            _playerName.value = preferencesManager.playerName.first()
            val savedMission = preferencesManager.savedMission.first()
            val savedAffection = preferencesManager.savedAffection.first()
            _hasSavedGame.value = savedMission > 0 || savedAffection > 0
        }
    }

    fun saveSettings(apiKey: String, playerName: String) {
        viewModelScope.launch {
            _apiKey.value = apiKey
            _playerName.value = playerName
            preferencesManager.saveApiKey(apiKey)
            preferencesManager.savePlayerName(playerName)
        }
    }

    fun startNewGame() {
        gameState.reset()
        gameState.startMission()
        viewModelScope.launch {
            preferencesManager.clearProgress()
        }
    }

    fun continueGame() {
        viewModelScope.launch {
            val savedMission = preferencesManager.savedMission.first()
            val savedAffection = preferencesManager.savedAffection.first()
            gameState.currentMissionIndex.value = savedMission
            gameState.affectionLevel.value = savedAffection
            gameState.startMission()
        }
    }

    fun sendMessage(text: String) {
        if (_apiKey.value.isBlank()) {
            _errorMessage.value = "Please set your Claude API key in Settings first."
            return
        }

        gameState.addPlayerMessage(text)
        gameState.isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val response = aiService.getNpcResponse(
                    apiKey = _apiKey.value,
                    gameState = gameState,
                    playerMessage = text
                )

                gameState.addNpcMessage(response.dialogue)
                gameState.updateMoodFromString(response.mood)

                // Update affection level (clamped 0-100)
                val newAffection = (gameState.affectionLevel.value + response.affectionDelta)
                    .coerceIn(0, 100)
                gameState.affectionLevel.value = newAffection

                // Show affection change feedback
                if (response.affectionDelta != 0) {
                    val sign = if (response.affectionDelta > 0) "+" else ""
                    gameState.addSystemMessage("${sign}${response.affectionDelta} Affection")
                }

            } catch (e: Exception) {
                gameState.addSystemMessage("Error: ${e.message ?: "Failed to get response"}")
                _errorMessage.value = e.message
            } finally {
                gameState.isLoading.value = false
            }
        }
    }

    fun completeMission() {
        gameState.isLoading.value = true
        gameState.isMissionComplete.value = true

        viewModelScope.launch {
            try {
                val analysis = aiService.analyzeMission(
                    apiKey = _apiKey.value,
                    gameState = gameState
                )

                // Apply the analysis affection change
                val newAffection = (gameState.affectionLevel.value + analysis.affectionChange)
                    .coerceIn(0, 100)
                gameState.affectionLevel.value = newAffection
                gameState.updateMoodFromString(analysis.npcMood)
                gameState.missionAnalysis.value = analysis

                // Save progress
                preferencesManager.saveProgress(
                    affection = newAffection,
                    missionIndex = gameState.currentMissionIndex.value
                )
                _hasSavedGame.value = true

            } catch (e: Exception) {
                gameState.missionAnalysis.value = com.aigame.heartquest.game.MissionAnalysis(
                    summary = "The mission has concluded. (Analysis unavailable: ${e.message})",
                    affectionChange = 0,
                    npcMood = gameState.npcMood.value,
                    advice = "Keep trying your best!"
                )
            } finally {
                gameState.isLoading.value = false
            }
        }
    }

    fun advanceToNextMission() {
        gameState.advanceToNextMission()
        viewModelScope.launch {
            preferencesManager.saveProgress(
                affection = gameState.affectionLevel.value,
                missionIndex = gameState.currentMissionIndex.value
            )
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
