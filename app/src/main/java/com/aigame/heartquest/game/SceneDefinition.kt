package com.aigame.heartquest.game

data class SceneDefinition(
    val backgroundColor: Long,
    val floorColor: Long,
    val objects: List<SceneObject>,
    val playerSpawn: Vec2,
    val npcSpawn: Vec2,
    val weatherEffect: WeatherEffect = WeatherEffect.NONE,
    val actions: List<PlayerAction>
)

enum class WeatherEffect {
    NONE, RAIN, FIREFLIES, SPARKLES
}

object SceneDefinitions {

    private val universalActions = listOf(
        PlayerAction("smile", "Smile", "\uD83D\uDE0A"),
        PlayerAction("compliment", "Compliment", "\uD83D\uDC96"),
        PlayerAction("joke", "Tell a Joke", "\uD83D\uDE04")
    )

    fun getScene(missionIndex: Int): SceneDefinition = when (missionIndex) {
        0 -> cafeScene()
        1 -> galleryScene()
        2 -> parkScene()
        3 -> kitchenScene()
        4 -> stargazingScene()
        else -> cafeScene()
    }

    private fun cafeScene() = SceneDefinition(
        backgroundColor = 0xFF2D1B0E,
        floorColor = 0xFF8B6914,
        objects = listOf(
            SceneObject("table1", "Table", Vec2(0.3f, 0.3f), 0.10f, 0.10f, 0xFF6B4226, isRound = true),
            SceneObject("table2", "Table", Vec2(0.7f, 0.25f), 0.10f, 0.10f, 0xFF6B4226, isRound = true),
            SceneObject("table3", "Table", Vec2(0.5f, 0.55f), 0.10f, 0.10f, 0xFF6B4226, isRound = true),
            SceneObject("counter", "Counter", Vec2(0.5f, 0.08f), 0.60f, 0.06f, 0xFF4A3728),
            SceneObject("plant1", "Plant", Vec2(0.1f, 0.1f), 0.06f, 0.06f, 0xFF2E7D32, isRound = true),
            SceneObject("plant2", "Plant", Vec2(0.9f, 0.1f), 0.06f, 0.06f, 0xFF2E7D32, isRound = true),
            SceneObject("railing", "Railing", Vec2(0.5f, 0.95f), 0.90f, 0.02f, 0xFF795548)
        ),
        playerSpawn = Vec2(0.5f, 0.80f),
        npcSpawn = Vec2(0.7f, 0.30f),
        actions = universalActions + listOf(
            PlayerAction("sit", "Sit With Him", "\uD83E\uDE91"),
            PlayerAction("order", "Order Drinks", "\u2615"),
            PlayerAction("ask_book", "Ask About Book", "\uD83D\uDCD6"),
            PlayerAction("share_story", "Share a Story", "\uD83D\uDCAC")
        )
    )

    private fun galleryScene() = SceneDefinition(
        backgroundColor = 0xFF1A1A2E,
        floorColor = 0xFF555566,
        objects = listOf(
            SceneObject("painting1", "Painting", Vec2(0.15f, 0.05f), 0.12f, 0.08f, 0xFFE91E63),
            SceneObject("painting2", "Painting", Vec2(0.45f, 0.05f), 0.14f, 0.08f, 0xFF2196F3),
            SceneObject("painting3", "Painting", Vec2(0.78f, 0.05f), 0.12f, 0.08f, 0xFFFF9800),
            SceneObject("painting4", "Painting", Vec2(0.05f, 0.40f), 0.04f, 0.14f, 0xFF9C27B0),
            SceneObject("bench1", "Bench", Vec2(0.35f, 0.45f), 0.12f, 0.04f, 0xFF5D4037),
            SceneObject("bench2", "Bench", Vec2(0.65f, 0.65f), 0.12f, 0.04f, 0xFF5D4037),
            SceneObject("sculpture", "Sculpture", Vec2(0.85f, 0.35f), 0.06f, 0.06f, 0xFFBDBDBD, isRound = true)
        ),
        playerSpawn = Vec2(0.25f, 0.80f),
        npcSpawn = Vec2(0.65f, 0.30f),
        weatherEffect = WeatherEffect.SPARKLES,
        actions = universalActions + listOf(
            PlayerAction("discuss_art", "Discuss Painting", "\uD83C\uDFA8"),
            PlayerAction("share_opinion", "Share Opinion", "\uD83E\uDD14"),
            PlayerAction("suggest_next", "Next Exhibit", "\uD83D\uDC49"),
            PlayerAction("photo", "Take Photo", "\uD83D\uDCF8")
        )
    )

    private fun parkScene() = SceneDefinition(
        backgroundColor = 0xFF1B3A1B,
        floorColor = 0xFF4CAF50,
        objects = listOf(
            SceneObject("tree1", "Oak Tree", Vec2(0.15f, 0.20f), 0.14f, 0.14f, 0xFF2E7D32, isRound = true),
            SceneObject("tree2", "Tree", Vec2(0.80f, 0.15f), 0.12f, 0.12f, 0xFF388E3C, isRound = true),
            SceneObject("tree3", "Tree", Vec2(0.10f, 0.65f), 0.10f, 0.10f, 0xFF43A047, isRound = true),
            SceneObject("bench", "Park Bench", Vec2(0.55f, 0.40f), 0.14f, 0.04f, 0xFF5D4037),
            SceneObject("path", "Path", Vec2(0.50f, 0.70f), 0.20f, 0.50f, 0xFFBCAAA4),
            SceneObject("puddle1", "Puddle", Vec2(0.35f, 0.60f), 0.07f, 0.05f, 0xFF42A5F5, isRound = true),
            SceneObject("puddle2", "Puddle", Vec2(0.60f, 0.75f), 0.06f, 0.04f, 0xFF42A5F5, isRound = true)
        ),
        playerSpawn = Vec2(0.30f, 0.85f),
        npcSpawn = Vec2(0.55f, 0.35f),
        weatherEffect = WeatherEffect.RAIN,
        actions = universalActions + listOf(
            PlayerAction("umbrella", "Share Umbrella", "\u2602\uFE0F"),
            PlayerAction("splash", "Splash Puddle", "\uD83D\uDCA6"),
            PlayerAction("shelter", "Find Shelter", "\uD83C\uDFE0"),
            PlayerAction("dance_rain", "Dance in Rain", "\uD83D\uDC83")
        )
    )

    private fun kitchenScene() = SceneDefinition(
        backgroundColor = 0xFF2D1B0E,
        floorColor = 0xFFD7CCC8,
        objects = listOf(
            SceneObject("counter", "Counter", Vec2(0.50f, 0.12f), 0.70f, 0.08f, 0xFF5D4037),
            SceneObject("stove", "Stove", Vec2(0.80f, 0.12f), 0.10f, 0.08f, 0xFF37474F),
            SceneObject("sink", "Sink", Vec2(0.20f, 0.12f), 0.08f, 0.06f, 0xFF78909C),
            SceneObject("table", "Table", Vec2(0.50f, 0.60f), 0.22f, 0.14f, 0xFF6D4C41),
            SceneObject("ingredients", "Ingredients", Vec2(0.45f, 0.10f), 0.06f, 0.04f, 0xFFFF8A65),
            SceneObject("pot", "Pot", Vec2(0.80f, 0.10f), 0.04f, 0.04f, 0xFF546E7A, isRound = true)
        ),
        playerSpawn = Vec2(0.35f, 0.80f),
        npcSpawn = Vec2(0.55f, 0.20f),
        actions = universalActions + listOf(
            PlayerAction("chop", "Help Chop", "\uD83D\uDD2A"),
            PlayerAction("taste", "Taste Sauce", "\uD83E\uDD64"),
            PlayerAction("flour", "Toss Flour", "\uD83C\uDF2C\uFE0F"),
            PlayerAction("set_table", "Set Table", "\uD83C\uDF7D\uFE0F")
        )
    )

    private fun stargazingScene() = SceneDefinition(
        backgroundColor = 0xFF0A0A2E,
        floorColor = 0xFF1B3A1B,
        objects = listOf(
            SceneObject("blanket", "Blanket", Vec2(0.50f, 0.45f), 0.24f, 0.16f, 0xFFAD1457),
            SceneObject("telescope", "Telescope", Vec2(0.72f, 0.35f), 0.05f, 0.08f, 0xFF78909C),
            SceneObject("tree1", "Tree", Vec2(0.08f, 0.20f), 0.12f, 0.12f, 0xFF1B5E20, isRound = true),
            SceneObject("tree2", "Tree", Vec2(0.92f, 0.50f), 0.10f, 0.10f, 0xFF1B5E20, isRound = true),
            SceneObject("basket", "Picnic Basket", Vec2(0.38f, 0.50f), 0.05f, 0.04f, 0xFF6D4C41)
        ),
        playerSpawn = Vec2(0.40f, 0.75f),
        npcSpawn = Vec2(0.55f, 0.42f),
        weatherEffect = WeatherEffect.FIREFLIES,
        actions = universalActions + listOf(
            PlayerAction("constellation", "Point at Stars", "\u2B50"),
            PlayerAction("move_closer", "Scoot Closer", "\uD83E\uDD7A"),
            PlayerAction("share_memory", "Share Memory", "\uD83D\uDCAD"),
            PlayerAction("hold_hand", "Take His Hand", "\uD83E\uDD1D")
        )
    )
}
