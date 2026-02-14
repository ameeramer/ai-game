package com.aigame.heartquest.ai

import com.aigame.heartquest.game.GameState
import com.aigame.heartquest.game.MissionAnalysis
import com.aigame.heartquest.game.MissionManager
import com.aigame.heartquest.game.PlayerAction
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

    suspend fun getNpcReaction(
        apiKey: String,
        gameState: GameState,
        playerAction: PlayerAction
    ): NpcReaction = withContext(Dispatchers.IO) {
        val mission = gameState.currentMission
        val systemPrompt = MissionManager.buildNpcActionPrompt(
            mission = mission,
            affectionLevel = gameState.affectionLevel.value,
            mood = gameState.npcMood.value,
            playerAction = playerAction,
            recentHistory = gameState.interactionHistory.toList()
        )

        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", "The player performs the action: \"${playerAction.label}\". Respond as Adrian.")
            })
        }

        val response = callClaudeApi(apiKey, systemPrompt, messages)
        parseNpcReaction(response)
    }

    suspend fun analyzeMission(
        apiKey: String,
        gameState: GameState
    ): MissionAnalysis = withContext(Dispatchers.IO) {
        val analysisPrompt = MissionManager.buildAnalysisPrompt(
            mission = gameState.currentMission,
            interactions = gameState.interactionHistory.toList(),
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
            val errorJson = try { JSONObject(responseBody) } catch (_: Exception) { null }
            val errorMessage = errorJson?.optJSONObject("error")?.optString("message")
                ?: "API error: ${response.code}"
            throw Exception(errorMessage)
        }

        val json = JSONObject(responseBody)
        val content = json.getJSONArray("content")
        return content.getJSONObject(0).getString("text")
    }

    private fun parseNpcReaction(raw: String): NpcReaction {
        return try {
            val cleaned = raw.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            val json = JSONObject(cleaned)
            NpcReaction(
                dialogue = json.getString("dialogue"),
                mood = json.optString("mood", "neutral"),
                affectionDelta = json.optInt("affection_delta", 0),
                npcAction = json.optString("npc_action", "idle")
            )
        } catch (_: Exception) {
            NpcReaction(
                dialogue = raw.take(200).trim(),
                mood = "neutral",
                affectionDelta = 0,
                npcAction = "idle"
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
        } catch (_: Exception) {
            MissionAnalysis(
                summary = "The mission has concluded.",
                affectionChange = 0,
                npcMood = "neutral",
                advice = "Try to be more engaging next time."
            )
        }
    }
}

data class NpcReaction(
    val dialogue: String,
    val mood: String,
    val affectionDelta: Int,
    val npcAction: String
)
