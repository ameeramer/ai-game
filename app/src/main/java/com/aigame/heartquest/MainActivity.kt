package com.aigame.heartquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aigame.heartquest.ui.screens.*
import com.aigame.heartquest.ui.theme.HeartQuestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HeartQuestTheme {
                HeartQuestApp()
            }
        }
    }
}

enum class Screen {
    MAIN_MENU,
    GAME,
    SETTINGS,
    MISSION_COMPLETE
}

@Composable
fun HeartQuestApp(
    viewModel: GameViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(Screen.MAIN_MENU) }
    val apiKey by viewModel.apiKey.collectAsState()
    val playerName by viewModel.playerName.collectAsState()
    val hasSavedGame by viewModel.hasSavedGame.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show errors as snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Watch for mission completion
    LaunchedEffect(viewModel.gameState.isMissionComplete.value) {
        if (viewModel.gameState.isMissionComplete.value) {
            currentScreen = Screen.MISSION_COMPLETE
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn(initialAlpha = 0.3f) togetherWith fadeOut(targetAlpha = 0.3f)
        },
        modifier = Modifier.fillMaxSize(),
        label = "screenTransition"
    ) { screen ->
        when (screen) {
            Screen.MAIN_MENU -> {
                MainMenuScreen(
                    onStartGame = {
                        if (apiKey.isBlank()) {
                            currentScreen = Screen.SETTINGS
                        } else {
                            viewModel.startNewGame()
                            currentScreen = Screen.GAME
                        }
                    },
                    onContinueGame = {
                        if (apiKey.isBlank()) {
                            currentScreen = Screen.SETTINGS
                        } else {
                            viewModel.continueGame()
                            currentScreen = Screen.GAME
                        }
                    },
                    onSettings = {
                        currentScreen = Screen.SETTINGS
                    },
                    hasSavedGame = hasSavedGame
                )
            }

            Screen.SETTINGS -> {
                SettingsScreen(
                    currentApiKey = apiKey,
                    currentPlayerName = playerName,
                    onSave = { key, name ->
                        viewModel.saveSettings(key, name)
                    },
                    onBack = {
                        currentScreen = Screen.MAIN_MENU
                    }
                )
            }

            Screen.GAME -> {
                GameScreen(
                    gameState = viewModel.gameState,
                    onSendMessage = { message ->
                        viewModel.sendMessage(message)
                    },
                    onCompleteMission = {
                        viewModel.completeMission()
                    },
                    onBack = {
                        currentScreen = Screen.MAIN_MENU
                    }
                )
            }

            Screen.MISSION_COMPLETE -> {
                MissionCompleteScreen(
                    missionTitle = viewModel.gameState.currentMission.title,
                    analysis = viewModel.gameState.missionAnalysis.value,
                    affectionLevel = viewModel.gameState.affectionLevel.value,
                    currentMissionIndex = viewModel.gameState.currentMissionIndex.value,
                    onNextMission = {
                        viewModel.advanceToNextMission()
                        currentScreen = Screen.GAME
                    },
                    onMainMenu = {
                        currentScreen = Screen.MAIN_MENU
                    }
                )
            }
        }
    }
}
