package com.aigame.heartquest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aigame.heartquest.game.ChatMessage
import com.aigame.heartquest.ui.theme.*

@Composable
fun ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    when (message.sender) {
        "narrator" -> NarratorBubble(message.text, modifier)
        "player" -> PlayerBubble(message.text, modifier)
        "npc" -> NpcBubble(message.text, modifier)
        "system" -> SystemBubble(message.text, modifier)
    }
}

@Composable
private fun NarratorBubble(text: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = SoftWhite.copy(alpha = 0.7f),
            fontSize = 13.sp,
            fontStyle = FontStyle.Italic,
            lineHeight = 18.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x33FFFFFF))
                .padding(12.dp)
        )
    }
}

@Composable
private fun PlayerBubble(text: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 48.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "You",
                color = SoftWhite.copy(alpha = 0.5f),
                fontSize = 11.sp,
                modifier = Modifier.padding(end = 8.dp, bottom = 2.dp)
            )
            Text(
                text = text,
                color = SoftWhite,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp))
                    .background(DeepRose.copy(alpha = 0.8f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun NpcBubble(text: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 48.dp, top = 4.dp, bottom = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(
                text = "Adrian",
                color = GoldenGlow.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
            Text(
                text = text,
                color = SoftWhite,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                    .background(DeepPurple.copy(alpha = 0.9f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun SystemBubble(text: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = GoldenGlow.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontStyle = FontStyle.Italic,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x22FFD700))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
