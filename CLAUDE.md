# HeartQuest - AI Dating Game

## Project Overview
HeartQuest is a 2D Android dating simulation game where the player controls a character who tries to win the heart of Adrian, an NPC whose dialogue and actions are driven by Claude Opus 4.6 LLM. The game features top-down 2D gameplay with joystick movement, context-sensitive action buttons, and AI-driven NPC behavior.

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + Material 3
- **2D Rendering**: Compose Canvas (procedural drawing)
- **AI Backend**: Anthropic Claude API (claude-opus-4-6)
- **Networking**: OkHttp3
- **Persistence**: Jetpack DataStore Preferences
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34

## Project Structure
```
app/src/main/java/com/aigame/heartquest/
├── MainActivity.kt          # Entry point + Compose navigation
├── GameViewModel.kt          # Main ViewModel orchestrating game logic
├── ai/
│   └── ClaudeAIService.kt    # Anthropic API client for NPC reactions + mission analysis
├── data/
│   └── PreferencesManager.kt # DataStore-based persistence (API key, progress)
├── game/
│   ├── Entity.kt             # Vec2, Direction, NpcBehavior, SceneObject, PlayerAction
│   ├── GameEngine.kt         # Game loop: movement, collision, NPC behavior, timers
│   ├── GameState.kt          # Runtime game state: positions, speech, interactions
│   ├── MissionManager.kt     # Mission definitions + AI prompt engineering
│   └── SceneDefinition.kt    # Scene layouts, objects, and actions per mission
└── ui/
    ├── components/
    │   ├── AffectionBar.kt   # Animated affection progress bar
    │   ├── GameCanvas.kt     # 2D Canvas rendering (characters, objects, effects)
    │   └── VirtualJoystick.kt # Touch joystick for player movement
    ├── screens/
    │   ├── MainMenuScreen.kt      # Title screen with animated heart
    │   ├── GameScreen.kt          # Main gameplay: canvas + actions + joystick
    │   ├── SettingsScreen.kt      # API key + player name configuration
    │   └── MissionCompleteScreen.kt # Post-mission AI analysis display
    └── theme/
        └── Theme.kt           # Material 3 dark theme (rose/purple palette)
```

## Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run lint checks
./gradlew lint

# Clean build
./gradlew clean
```

## Game Architecture

### Gameplay
- **Top-down 2D view**: Player moves around mission scenes using a virtual joystick
- **Action-based interaction**: When near Adrian, context-sensitive action buttons appear (e.g., "Share Umbrella", "Discuss Painting", "Take His Hand")
- **No text input**: Player interacts through predefined actions; the LLM decides NPC reactions
- **NPC autonomy**: Adrian moves, turns, approaches, or steps back based on LLM responses

### Missions
5 missions with progressive intimacy: Cafe meeting -> Art gallery -> Rainy walk -> Cooking together -> Stargazing confession. Each mission has a unique scene layout, objects, weather effects, and action set.

### AI Integration
- **NPC Reactions**: Each player action is sent to Claude with Adrian's personality, current mood, affection level, and interaction history. Claude responds with dialogue, mood, affection delta, and physical action as JSON.
- **NPC Actions**: `approach_player`, `step_back`, `turn_away`, `face_player`, `emote`, `idle`
- **Mission Analysis**: After completing a mission, the interaction history is sent to Claude for narrative analysis, affection scoring, and player advice.

### 2D Rendering
- Compose Canvas with procedural drawing (no external assets)
- Characters drawn from primitives (circles, rectangles, arcs)
- Scene objects per mission (tables, trees, paintings, kitchen items, telescope)
- Weather effects (rain, fireflies, sparkles)
- Speech bubbles above NPC, action text above player
- Mood-reactive particles and color indicators

### Game Engine
- Frame-based update loop using `withFrameMillis`
- Player movement with joystick input and collision detection
- NPC behavior state machine (idle, walking, approaching, stepping back, emoting, turned away)
- Timed speech bubbles and action text
- Interaction range detection

### State Management
- `GameState`: Compose-observable mutable state for positions, speech, mood, interactions
- `GameEngine`: Updates positions, NPC behavior, timers each frame
- `GameViewModel`: Android ViewModel orchestrating AI calls, state updates, persistence
- `PreferencesManager`: DataStore for API key, player name, and save progress

## Key Design Decisions
- API key stored locally in DataStore (user provides their own Anthropic key)
- 2D characters and scenes built from Canvas primitives (no external asset files)
- Action-based gameplay (not text chat) for true game feel
- Only NPC dialogue and behavior are LLM-driven; player controls are deterministic
- Claude's structured JSON responses include physical actions for NPC movement
