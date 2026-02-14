package com.aigame.heartquest.game

object MissionManager {

    val missions = listOf(
        Mission(
            id = 1,
            title = "First Encounter",
            description = "You spot Adrian at a cozy rooftop cafe. Make a good first impression.",
            scenario = "The evening sun casts a golden glow over the rooftop cafe. " +
                "Adrian sits at a corner table, reading a book with a coffee beside him. " +
                "His dark hair catches the light as he looks up, noticing you.",
            targetAffectionGain = 15,
            completionThreshold = 4
        ),
        Mission(
            id = 2,
            title = "The Art Gallery",
            description = "Adrian invited you to an art gallery opening. Show your genuine side.",
            scenario = "A sleek modern art gallery buzzes with soft conversation. " +
                "Adrian stands before a large abstract painting, wearing a fitted navy blazer. " +
                "He smiles warmly when he sees you arrive.",
            targetAffectionGain = 20,
            completionThreshold = 5
        ),
        Mission(
            id = 3,
            title = "Rainy Day Walk",
            description = "A surprise rainstorm catches you both. Turn it into a romantic moment.",
            scenario = "A sunny afternoon walk has turned into a downpour. " +
                "You and Adrian take shelter under a large oak tree, rain drumming all around. " +
                "He laughs, shaking water from his hair.",
            targetAffectionGain = 25,
            completionThreshold = 5
        ),
        Mission(
            id = 4,
            title = "Cooking Together",
            description = "Adrian invited you to his apartment to cook dinner. Get closer.",
            scenario = "Adrian's apartment is warm and inviting, with soft music playing. " +
                "The kitchen counter is spread with fresh ingredients. " +
                "He hands you an apron with a playful grin.",
            targetAffectionGain = 25,
            completionThreshold = 5
        ),
        Mission(
            id = 5,
            title = "Stargazing Confession",
            description = "A quiet night under the stars. The moment of truth.",
            scenario = "You and Adrian lie on a blanket on a hilltop outside the city. " +
                "The stars spread endlessly above. " +
                "Adrian turns to look at you, his expression soft and unguarded.",
            targetAffectionGain = 30,
            completionThreshold = 6
        )
    )

    fun isLastMission(index: Int): Boolean = index >= missions.size - 1

    fun buildNpcActionPrompt(
        mission: Mission,
        affectionLevel: Int,
        mood: String,
        playerAction: PlayerAction,
        recentHistory: List<InteractionRecord>
    ): String {
        val historyText = if (recentHistory.isNotEmpty()) {
            recentHistory.takeLast(8).joinToString("\n") { record ->
                "- Player: ${record.actionLabel} -> Adrian (${record.npcMood}): \"${record.npcDialogue}\""
            }
        } else {
            "(First interaction)"
        }

        return """You are Adrian, a handsome, intelligent, and charming man in your late 20s in a dating simulation game.

PERSONALITY:
- Confident but not arrogant, witty but kind
- Warm sense of humor, enjoys intellectual conversations
- Passionate about art, literature, and cooking
- Flirty when the mood is right, but not easy to win over
- Values authenticity, kindness, and genuine connection
- Dislikes dishonesty, superficiality, and overly aggressive behavior

CURRENT STATE:
- Mission: "${mission.title}" - ${mission.description}
- Scene: ${mission.scenario}
- Current affection level: $affectionLevel/100
- Current mood: $mood

RECENT INTERACTIONS:
$historyText

THE PLAYER JUST PERFORMED THIS ACTION: "${playerAction.label}"

BEHAVIOR RULES:
- Respond naturally as Adrian would to this specific action
- Your dialogue should be 1-2 sentences, natural and in-character
- If affection is low (0-25): be polite but guarded
- If affection is medium (26-60): be warmer, more playful
- If affection is high (61-85): be openly flirty, show vulnerability
- If affection is very high (86-100): be deeply affectionate
- Consider the context: a "${playerAction.label}" action might be charming or awkward depending on timing
- If the same action is repeated too often, react with mild amusement or suggest variety

RESPONSE FORMAT:
Respond ONLY with a JSON object (no markdown, no code blocks):
{"dialogue": "Your spoken response as Adrian", "mood": "one of: neutral/happy/flirty/annoyed/shy", "affection_delta": number between -10 and 10, "npc_action": "one of: idle/approach_player/step_back/turn_away/face_player/emote"}

npc_action determines Adrian's physical reaction:
- idle: stay in place calmly
- approach_player: walk closer to the player (positive reaction)
- step_back: back away slightly (uncomfortable)
- turn_away: turn around (embarrassed or annoyed)
- face_player: turn to look at the player directly
- emote: express emotion physically (laugh, blush, gesture)"""
    }

    fun buildAnalysisPrompt(
        mission: Mission,
        interactions: List<InteractionRecord>,
        affectionLevel: Int
    ): String {
        val interactionLog = interactions.joinToString("\n") { record ->
            "- Player: ${record.actionLabel} -> Adrian (${record.npcMood}): \"${record.npcDialogue}\""
        }

        return """You are the game narrator analyzing a completed mission in a dating simulation.

MISSION: "${mission.title}" - ${mission.description}

INTERACTIONS:
$interactionLog

CURRENT AFFECTION LEVEL: $affectionLevel/100

Analyze the player's performance. Respond ONLY with a JSON object (no markdown, no code blocks):
{
  "summary": "2-3 sentence narrative summary of how the interaction went",
  "affection_change": number (-15 to +${mission.targetAffectionGain}),
  "npc_mood": "Adrian's mood after this mission (happy/neutral/flirty/annoyed/shy)",
  "advice": "One sentence of advice for winning Adrian's heart in future missions"
}

Be fair but not too generous. Reward genuine, varied interactions. Penalize repetitive or poorly-timed actions."""
    }
}
