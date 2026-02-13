package com.aigame.heartquest.game

object MissionManager {

    val missions = listOf(
        Mission(
            id = 1,
            title = "First Encounter",
            description = "You spot Adrian at a cozy rooftop cafe. Make a good first impression.",
            scenario = "The evening sun casts a golden glow over the rooftop cafe. " +
                "You notice Adrian sitting alone at a corner table, reading a book with a coffee beside him. " +
                "His dark hair catches the light as he looks up, noticing you. " +
                "This is your chance to make a first impression.",
            targetAffectionGain = 15,
            completionThreshold = 4
        ),
        Mission(
            id = 2,
            title = "The Art Gallery",
            description = "Adrian invited you to an art gallery opening. Show your genuine side.",
            scenario = "A sleek modern art gallery buzzes with soft conversation. " +
                "Adrian stands before a large abstract painting, his eyes tracing the brushstrokes. " +
                "He's wearing a fitted navy blazer and smiles warmly when he sees you arrive. " +
                "\"I'm glad you came,\" he says.",
            targetAffectionGain = 20,
            completionThreshold = 5
        ),
        Mission(
            id = 3,
            title = "Rainy Day Walk",
            description = "A surprise rainstorm catches you both. Turn it into a romantic moment.",
            scenario = "What started as a sunny afternoon walk through the park has turned into a downpour. " +
                "You and Adrian take shelter under a large oak tree, rain drumming all around you. " +
                "He laughs, shaking water from his hair. \"Well, this wasn't in the forecast,\" " +
                "he says, standing close to you under the branches.",
            targetAffectionGain = 25,
            completionThreshold = 5
        ),
        Mission(
            id = 4,
            title = "Cooking Together",
            description = "Adrian invited you to his apartment to cook dinner. Get closer.",
            scenario = "Adrian's apartment is warm and inviting, with soft music playing. " +
                "The kitchen counter is spread with fresh ingredients. " +
                "\"I hope you don't mind getting your hands dirty,\" Adrian says with a playful grin, " +
                "handing you an apron. \"I thought we could make pasta from scratch.\"",
            targetAffectionGain = 25,
            completionThreshold = 5
        ),
        Mission(
            id = 5,
            title = "Stargazing Confession",
            description = "A quiet night under the stars. The moment of truth.",
            scenario = "You and Adrian lie on a blanket on a hilltop outside the city. " +
                "The stars spread endlessly above. The only sound is distant crickets and your own breathing. " +
                "Adrian turns to look at you, his expression soft and unguarded. " +
                "\"I've been thinking a lot lately,\" he says quietly. \"About us.\"",
            targetAffectionGain = 30,
            completionThreshold = 6
        )
    )

    fun isLastMission(index: Int): Boolean = index >= missions.size - 1

    fun buildNpcSystemPrompt(mission: Mission, affectionLevel: Int, mood: String): String {
        return """You are Adrian, a handsome, intelligent, and charming man in your late 20s in a dating simulation game.

PERSONALITY:
- You are confident but not arrogant, witty but kind
- You have a warm sense of humor and enjoy intellectual conversations
- You're passionate about art, literature, and cooking
- You can be flirty when the mood is right, but you're not easy to win over
- You value authenticity, kindness, and genuine connection above all
- You dislike dishonesty, superficiality, and overly aggressive behavior

CURRENT STATE:
- Mission: "${mission.title}" - ${mission.description}
- Current affection level: $affectionLevel/100 (affects how warm/open you are)
- Current mood: $mood

BEHAVIOR RULES:
- Respond naturally as Adrian would in this scenario
- Your responses should be 1-3 sentences, conversational and natural
- If affection is low (0-25): be polite but guarded, require effort to open up
- If affection is medium (26-60): be warmer, more playful, show genuine interest
- If affection is high (61-85): be openly flirty, share personal things, show vulnerability
- If affection is very high (86-100): be deeply affectionate, hint at strong feelings
- If the player is rude or inappropriate, react negatively (annoyed, distant)
- If the player is genuinely sweet, react positively (happy, shy, flirty)

RESPONSE FORMAT:
Respond ONLY with a JSON object (no markdown, no code blocks):
{"dialogue": "Your spoken response as Adrian", "mood": "one of: neutral/happy/flirty/annoyed/shy", "affection_delta": number between -10 and 10}

The affection_delta should reflect how Adrian feels about what the player just said/did:
- Negative: rude, creepy, dishonest, or pushy behavior
- Zero: neutral or unremarkable interaction
- Positive: kind, funny, genuine, thoughtful, or charming behavior"""
    }

    fun buildAnalysisPrompt(
        mission: Mission,
        chatHistory: List<ChatMessage>,
        affectionLevel: Int
    ): String {
        val conversationLog = chatHistory.joinToString("\n") { msg ->
            when (msg.sender) {
                "player" -> "Player: ${msg.text}"
                "npc" -> "Adrian: ${msg.text}"
                "narrator" -> "[Scene: ${msg.text}]"
                else -> "[${msg.sender}: ${msg.text}]"
            }
        }

        return """You are the game narrator analyzing a completed mission in a dating simulation.

MISSION: "${mission.title}" - ${mission.description}

CONVERSATION:
$conversationLog

CURRENT AFFECTION LEVEL: $affectionLevel/100

Analyze the player's performance in this mission. Respond ONLY with a JSON object (no markdown, no code blocks):
{
  "summary": "2-3 sentence narrative summary of how the interaction went",
  "affection_change": number (-15 to +${mission.targetAffectionGain}),
  "npc_mood": "Adrian's mood after this mission (happy/neutral/flirty/annoyed/shy)",
  "advice": "One sentence of advice for winning Adrian's heart in future missions"
}

Be fair but not too generous. Reward genuine, thoughtful roleplay. Penalize rudeness or low effort."""
    }
}
