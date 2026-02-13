package com.aigame.heartquest.ai

import com.aigame.heartquest.game.ChatMessage
import com.aigame.heartquest.game.MissionAnalysis
import com.aigame.heartquest.game.MissionManager
import com.aigame.heartquest.game.GameState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ClaudeAIService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiUrl = "https://api.anthropic.com/v1/messages"
    private val model = "claude-opus-4-6"

    suspend fun getNpcResponse(
        apiKey: String,
        gameState: GameState,
        playerMessage: String
    ): NpcResponse = withContext(Dispatchers.IO) {
        val mission = gameState.currentMission
        val systemPrompt = MissionManager.buildNpcSystemPrompt(
            mission = mission,
            affectionLevel = gameState.affectionLevel.value,
            mood = gameState.npcMood.value
        )

        val messages = buildMessageHistory(gameState.chatHistory, playerMessage)

        val response = callClaudeApi(apiKey, systemPrompt, messages)
        parseNpcResponse(response)
    }

    suspend fun analyzeMission(
        apiKey: String,
        gameState: GameState
    ): MissionAnalysis = withContext(Dispatchers.IO) {
        val analysisPrompt = MissionManager.buildAnalysisPrompt(
            mission = gameState.currentMission,
            chatHistory = gameState.chatHistory,
            affectionLevel = gameState.affectionLevel.value
        )

        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", analysisPrompt)
            })
        }

        val response = callClaudeApi(apiKey, "You are a game narrator and analyst.", messages)
        parseAnalysisResponse(response)
    }

    private fun buildMessageHistory(
        chatHistory: List<ChatMessage>,
        currentMessage: String
    ): JSONArray {
        val messages = JSONArray()

        // Include recent conversation history (last 10 messages for context)
        val recentHistory = chatHistory.takeLast(10)
        for (msg in recentHistory) {
            when (msg.sender) {
                "player" -> messages.put(JSONObject().apply {
                    put("role", "user")
                    put("content", msg.text)
                })
                "npc" -> messages.put(JSONObject().apply {
                    put("role", "assistant")
                    put("content", msg.text)
                })
                "narrator" -> messages.put(JSONObject().apply {
                    put("role", "user")
                    put("content", "[Scene description: ${msg.text}]")
                })
            }
        }

        // Add current player message
        messages.put(JSONObject().apply {
            put("role", "user")
            put("content", currentMessage)
        })

        return messages
    }

    private fun callClaudeApi(
        apiKey: String,
        systemPrompt: String,
        messages: JSONArray
    ): String {
        val requestBody = JSONObject().apply {
            put("model", model)
            put("max_tokens", 512)
            put("system", systemPrompt)
            put("messages", messages)
        }

        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response from API")

        if (!response.isSuccessful) {
            val errorJson = try { JSONObject(responseBody) } catch (e: Exception) { null }
            val errorMessage = errorJson?.optJSONObject("error")?.optString("message")
                ?: "API error: ${response.code}"
            throw Exception(errorMessage)
        }

        val json = JSONObject(responseBody)
        val content = json.getJSONArray("content")
        return content.getJSONObject(0).getString("text")
    }

    private fun parseNpcResponse(raw: String): NpcResponse {
        return try {
            // Strip any markdown code block markers if present
            val cleaned = raw.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            val json = JSONObject(cleaned)
            NpcResponse(
                dialogue = json.getString("dialogue"),
                mood = json.optString("mood", "neutral"),
                affectionDelta = json.optInt("affection_delta", 0)
            )
        } catch (e: Exception) {
            // Fallback: treat raw response as dialogue
            NpcResponse(
                dialogue = raw.trim(),
                mood = "neutral",
                affectionDelta = 0
            )
        }
    }

    private fun parseAnalysisResponse(raw: String): MissionAnalysis {
        return try {
            val cleaned = raw.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            val json = JSONObject(cleaned)
            MissionAnalysis(
                summary = json.getString("summary"),
                affectionChange = json.optInt("affection_change", 0),
                npcMood = json.optString("npc_mood", "neutral"),
                advice = json.optString("advice", "Keep being yourself.")
            )
        } catch (e: Exception) {
            MissionAnalysis(
                summary = "The mission has concluded.",
                affectionChange = 0,
                npcMood = "neutral",
                advice = "Try to be more engaging next time."
            )
        }
    }
}

data class NpcResponse(
    val dialogue: String,
    val mood: String,
    val affectionDelta: Int
)
