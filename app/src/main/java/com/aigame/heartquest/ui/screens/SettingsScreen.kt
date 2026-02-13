package com.aigame.heartquest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aigame.heartquest.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentApiKey: String,
    currentPlayerName: String,
    onSave: (apiKey: String, playerName: String) -> Unit,
    onBack: () -> Unit
) {
    var apiKey by remember { mutableStateOf(currentApiKey) }
    var playerName by remember { mutableStateOf(currentPlayerName) }
    var showApiKey by remember { mutableStateOf(false) }
    var showSaved by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MidnightBlue, DeepPurple)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Top bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SoftWhite
                    )
                }
                Text(
                    text = "Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftWhite
                )
            }

            Spacer(Modifier.height(32.dp))

            // API Key Section
            Text(
                text = "Claude API Key",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = WarmPink,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Text(
                text = "Required to power Adrian's AI. Get your key from console.anthropic.com",
                fontSize = 12.sp,
                color = SoftWhite.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("sk-ant-...", color = SoftWhite.copy(alpha = 0.3f))
                },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Key,
                        contentDescription = null,
                        tint = WarmPink.copy(alpha = 0.6f)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { showApiKey = !showApiKey }) {
                        Icon(
                            if (showApiKey) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Toggle visibility",
                            tint = SoftWhite.copy(alpha = 0.5f)
                        )
                    }
                },
                visualTransformation = if (showApiKey) VisualTransformation.None
                    else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SoftWhite,
                    unfocusedTextColor = SoftWhite,
                    cursorColor = WarmPink,
                    focusedBorderColor = WarmPink,
                    unfocusedBorderColor = Twilight,
                    focusedContainerColor = Color(0x22FFFFFF),
                    unfocusedContainerColor = Color(0x11FFFFFF),
                )
            )

            Spacer(Modifier.height(28.dp))

            // Player Name Section
            Text(
                text = "Your Name",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = WarmPink,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            OutlinedTextField(
                value = playerName,
                onValueChange = { playerName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Enter your name", color = SoftWhite.copy(alpha = 0.3f))
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SoftWhite,
                    unfocusedTextColor = SoftWhite,
                    cursorColor = WarmPink,
                    focusedBorderColor = WarmPink,
                    unfocusedBorderColor = Twilight,
                    focusedContainerColor = Color(0x22FFFFFF),
                    unfocusedContainerColor = Color(0x11FFFFFF),
                )
            )

            Spacer(Modifier.height(36.dp))

            // Save button
            Button(
                onClick = {
                    onSave(apiKey.trim(), playerName.trim())
                    showSaved = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepRose
                ),
                enabled = apiKey.isNotBlank()
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save Settings", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            if (showSaved) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Settings saved!",
                    color = SuccessGreen,
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
