package com.aigame.heartquest.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aigame.heartquest.game.MissionAnalysis
import com.aigame.heartquest.game.MissionManager
import com.aigame.heartquest.ui.components.AffectionBar
import com.aigame.heartquest.ui.theme.*

@Composable
fun MissionCompleteScreen(
    missionTitle: String,
    analysis: MissionAnalysis?,
    affectionLevel: Int,
    currentMissionIndex: Int,
    onNextMission: () -> Unit,
    onMainMenu: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "complete")
    val starScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star"
    )

    val isLastMission = MissionManager.isLastMission(currentMissionIndex)
    val isGameWon = isLastMission && affectionLevel >= 75

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DeepPurple,
                        MidnightBlue,
                        Color(0xFF1A0A2E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(28.dp)
        ) {
            // Star / Heart icon
            Icon(
                imageVector = if (isGameWon) Icons.Filled.Favorite else Icons.Filled.Star,
                contentDescription = null,
                tint = if (isGameWon) HeartRed else GoldenGlow,
                modifier = Modifier
                    .size(64.dp)
                    .scale(starScale)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = if (isGameWon) "You Won Adrian's Heart!" else "Mission Complete!",
                fontSize = if (isGameWon) 28.sp else 26.sp,
                fontWeight = FontWeight.Bold,
                color = if (isGameWon) WarmPink else GoldenGlow,
                textAlign = TextAlign.Center
            )

            Text(
                text = missionTitle,
                fontSize = 15.sp,
                color = SoftWhite.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Affection bar
            AffectionBar(
                affection = affectionLevel,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // AI Analysis Card
            if (analysis != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2055)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Adrian's Thoughts",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldenGlow
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = analysis.summary,
                            fontSize = 14.sp,
                            color = SoftWhite,
                            lineHeight = 20.sp
                        )

                        Spacer(Modifier.height(14.dp))

                        // Affection change indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = if (analysis.affectionChange >= 0) HeartRed else Color(0xFF888888),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (analysis.affectionChange >= 0)
                                    "+${analysis.affectionChange} Affection"
                                else "${analysis.affectionChange} Affection",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (analysis.affectionChange >= 0) SuccessGreen else HeartRed
                            )

                            Spacer(Modifier.width(12.dp))

                            Text(
                                text = "Mood: ${analysis.npcMood.replaceFirstChar { it.uppercase() }}",
                                fontSize = 13.sp,
                                color = SoftWhite.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Advice
                        Text(
                            text = "\"${analysis.advice}\"",
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = WarmPink.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                CircularProgressIndicator(
                    color = WarmPink,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Adrian is reflecting on your time together...",
                    fontSize = 13.sp,
                    color = SoftWhite.copy(alpha = 0.5f),
                    fontStyle = FontStyle.Italic
                )
            }

            Spacer(Modifier.height(32.dp))

            // Next mission or Main menu buttons
            if (!isLastMission) {
                Button(
                    onClick = onNextMission,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepRose),
                    enabled = analysis != null
                ) {
                    Text("Next Mission", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                }

                Spacer(Modifier.height(12.dp))
            }

            OutlinedButton(
                onClick = onMainMenu,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftWhite.copy(alpha = 0.7f))
            ) {
                Icon(Icons.Filled.Home, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("Main Menu", fontSize = 15.sp)
            }
        }
    }
}
