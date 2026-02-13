package com.aigame.heartquest.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aigame.heartquest.ui.theme.*

@Composable
fun MainMenuScreen(
    onStartGame: () -> Unit,
    onContinueGame: () -> Unit,
    onSettings: () -> Unit,
    hasSavedGame: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "menu")

    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartbeat"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MidnightBlue,
                        DeepPurple,
                        Color(0xFF3D1F5C)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Heart icon
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = HeartRed,
                modifier = Modifier
                    .size(80.dp)
                    .scale(heartScale)
            )

            Spacer(Modifier.height(16.dp))

            // Title
            Text(
                text = "Heart Quest",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = SoftWhite,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Win Adrian's Heart",
                fontSize = 16.sp,
                color = WarmPink.copy(alpha = glowAlpha + 0.3f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Light
            )

            Spacer(Modifier.height(60.dp))

            // Start / New Game button
            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepRose
                )
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("New Game", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            if (hasSavedGame) {
                Spacer(Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onContinueGame,
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = WarmPink
                    )
                ) {
                    Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Settings button
            TextButton(
                onClick = onSettings,
                modifier = Modifier.fillMaxWidth(0.75f)
            ) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = null,
                    tint = SoftWhite.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Settings",
                    color = SoftWhite.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(40.dp))

            Text(
                text = "Powered by Claude AI",
                fontSize = 11.sp,
                color = SoftWhite.copy(alpha = 0.3f)
            )
        }
    }
}

private val EaseInOutCubic = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
private val EaseInOutSine = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)
