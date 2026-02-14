package com.aigame.heartquest.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aigame.heartquest.game.GameEngine
import com.aigame.heartquest.game.GameState
import com.aigame.heartquest.game.PlayerAction
import com.aigame.heartquest.ui.components.AffectionBar
import com.aigame.heartquest.ui.components.GameCanvas
import com.aigame.heartquest.ui.components.VirtualJoystick
import com.aigame.heartquest.ui.theme.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withFrameMillis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameState: GameState,
    gameEngine: GameEngine,
    onPlayerAction: (PlayerAction) -> Unit,
    onCompleteMission: () -> Unit,
    onBack: () -> Unit
) {
    val mission = gameState.currentMission
    val scene = gameState.currentScene.value
    val isNear = gameState.isNearNpc.value
    val isLoading = gameState.isLoading.value
    val canComplete = gameState.interactionCount.value >= mission.completionThreshold

    // Game loop
    LaunchedEffect(Unit) {
        var lastFrameTime = 0L
        while (isActive) {
            withFrameMillis { frameTime ->
                val delta = if (lastFrameTime == 0L) 0f else (frameTime - lastFrameTime) / 1000f
                lastFrameTime = frameTime
                gameEngine.update(delta.coerceAtMost(0.1f))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlue)
    ) {
        // Top bar
        Surface(
            color = Color(0xFF1E1E42),
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SoftWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mission ${mission.id}: ${mission.title}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldenGlow
                        )
                        Text(
                            text = mission.description,
                            fontSize = 11.sp,
                            color = SoftWhite.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    NpcMoodBadge(mood = gameState.npcMood.value)
                }

                Spacer(Modifier.height(6.dp))
                AffectionBar(affection = gameState.affectionLevel.value)
            }
        }

        // 2D Game Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            GameCanvas(
                gameState = gameState,
                modifier = Modifier.fillMaxSize()
            )

            // Scenario text at start
            if (gameState.interactionCount.value == 0 && !isLoading) {
                Surface(
                    color = Color(0xCC1A1A2E),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = mission.scenario,
                        color = SoftWhite.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Proximity hint
            AnimatedVisibility(
                visible = isNear && !isLoading && gameState.npcSpeechText.value.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            ) {
                Surface(
                    color = Color(0xAA1A1A2E),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Choose an action below",
                        color = GoldenGlow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Complete mission button
        AnimatedVisibility(
            visible = canComplete && !isLoading,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Button(
                onClick = onCompleteMission,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Complete Mission", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Bottom controls: action buttons + joystick
        Surface(
            color = Color(0xFF1E1E42),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Action buttons (left side)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isNear && !isLoading && scene != null) {
                        val actions = scene.actions
                        val topRow = actions.take((actions.size + 1) / 2)
                        val bottomRow = actions.drop((actions.size + 1) / 2)

                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            topRow.forEach { action ->
                                ActionButton(
                                    action = action,
                                    onClick = { onPlayerAction(action) },
                                    enabled = !isLoading
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            bottomRow.forEach { action ->
                                ActionButton(
                                    action = action,
                                    onClick = { onPlayerAction(action) },
                                    enabled = !isLoading
                                )
                            }
                        }
                    } else if (isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = WarmPink,
                                strokeWidth = 2.dp
                            )
                            Text(
                                "Adrian is thinking...",
                                color = SoftWhite.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Text(
                            text = "Walk to Adrian to interact",
                            color = SoftWhite.copy(alpha = 0.3f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Joystick (right side)
                VirtualJoystick(
                    onInput = { input ->
                        gameState.joystickInput.value = input
                    },
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    action: PlayerAction,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.height(36.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DeepRose.copy(alpha = 0.8f),
            disabledContainerColor = Twilight.copy(alpha = 0.4f)
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
    ) {
        Text(action.emoji, fontSize = 14.sp)
        Spacer(Modifier.width(4.dp))
        Text(
            action.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun NpcMoodBadge(mood: String) {
    val (emoji, color) = when (mood) {
        "happy" -> "\uD83D\uDE0A" to SuccessGreen
        "flirty" -> "\uD83D\uDE0F" to WarmPink
        "annoyed" -> "\uD83D\uDE12" to Color(0xFFFF9800)
        "shy" -> "\uD83D\uDE33" to SoftBlush
        "laughing" -> "\uD83D\uDE02" to GoldenGlow
        else -> "\uD83D\uDE10" to Color(0xFF888888)
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 14.sp)
            Text(
                text = mood.replaceFirstChar { it.uppercase() },
                fontSize = 11.sp,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
