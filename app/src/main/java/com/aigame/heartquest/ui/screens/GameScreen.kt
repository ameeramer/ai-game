package com.aigame.heartquest.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.aigame.heartquest.game.ChatMessage
import com.aigame.heartquest.game.GameState
import com.aigame.heartquest.game.NpcAnimation
import com.aigame.heartquest.ui.components.AffectionBar
import com.aigame.heartquest.ui.components.ChatBubble
import com.aigame.heartquest.ui.components.GameGLSurfaceView
import com.aigame.heartquest.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameState: GameState,
    onSendMessage: (String) -> Unit,
    onCompleteMission: () -> Unit,
    onBack: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val mission = gameState.currentMission

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(gameState.chatHistory.size) {
        if (gameState.chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(gameState.chatHistory.size - 1)
        }
    }

    val canCompleteMission = gameState.interactionCount.value >= mission.completionThreshold

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlue)
    ) {
        // Top bar with mission info
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
                            maxLines = 1
                        )
                    }

                    // Mood indicator
                    NpcMoodBadge(mood = gameState.npcMood.value)
                }

                Spacer(Modifier.height(6.dp))
                AffectionBar(affection = gameState.affectionLevel.value)
            }
        }

        // 3D Scene viewport (top portion)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFF0A0A20))
        ) {
            AndroidView(
                factory = { context ->
                    GameGLSurfaceView(context).apply {
                        setNpcAnimation(gameState.npcAnimationState.value)
                    }
                },
                update = { view ->
                    view.setNpcAnimation(gameState.npcAnimationState.value)
                    // Adjust scene ambience based on mission
                    val ambience = when (gameState.currentMissionIndex.value) {
                        0 -> 0.8f  // Sunset cafe
                        1 -> 0.6f  // Art gallery
                        2 -> 0.4f  // Rainy day
                        3 -> 0.7f  // Apartment
                        4 -> 0.2f  // Stargazing
                        else -> 0.5f
                    }
                    view.setSceneAmbience(ambience)
                },
                modifier = Modifier.fillMaxSize()
            )

            // Gradient overlay at bottom of 3D scene
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(30.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, MidnightBlue)
                        )
                    )
            )
        }

        // Chat messages area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(gameState.chatHistory.toList()) { message ->
                ChatBubble(message = message)
            }

            // Loading indicator
            if (gameState.isLoading.value) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, top = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = WarmPink,
                                strokeWidth = 2.dp
                            )
                            Text(
                                "Adrian is typing...",
                                color = SoftWhite.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Complete mission button (when threshold reached)
        AnimatedVisibility(
            visible = canCompleteMission && !gameState.isLoading.value,
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

        // Input area
        Surface(
            color = Color(0xFF1E1E42),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            "Type your action or dialogue...",
                            color = SoftWhite.copy(alpha = 0.3f),
                            fontSize = 14.sp
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SoftWhite,
                        unfocusedTextColor = SoftWhite,
                        cursorColor = WarmPink,
                        focusedBorderColor = WarmPink,
                        unfocusedBorderColor = Twilight,
                        focusedContainerColor = Color(0x11FFFFFF),
                        unfocusedContainerColor = Color(0x11FFFFFF),
                    ),
                    singleLine = false,
                    maxLines = 3,
                    enabled = !gameState.isLoading.value
                )

                Spacer(Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = {
                        if (inputText.isNotBlank() && !gameState.isLoading.value) {
                            onSendMessage(inputText.trim())
                            inputText = ""
                        }
                    },
                    containerColor = if (inputText.isNotBlank() && !gameState.isLoading.value)
                        DeepRose else Twilight,
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = SoftWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NpcMoodBadge(mood: String) {
    val (emoji, color) = when (mood) {
        "happy" -> "😊" to SuccessGreen
        "flirty" -> "😏" to WarmPink
        "annoyed" -> "😒" to Color(0xFFFF9800)
        "shy" -> "😳" to SoftBlush
        "laughing" -> "😂" to GoldenGlow
        else -> "😐" to Color(0xFF888888)
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
