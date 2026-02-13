# HeartQuest - AI Dating Game

## Project Overview
HeartQuest is a 3D Android dating simulation game where the player tries to win the heart of Adrian, a handsome male NPC powered by Claude Opus 4.6 LLM. The game features mission-based progression, real-time 3D rendering, and AI-driven dialogue and analysis.

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + Material 3
- **3D Rendering**: OpenGL ES 2.0 (custom shaders, low-poly character models)
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
│   └── ClaudeAIService.kt    # Anthropic API client for NPC AI + mission analysis
├── data/
│   └── PreferencesManager.kt # DataStore-based persistence (API key, progress)
├── game/
│   ├── GameState.kt          # Runtime game state, chat history, affection
│   └── MissionManager.kt     # Mission definitions + prompt engineering
├── renderer/
│   ├── GameRenderer.kt       # Main OpenGL ES renderer (scene orchestration)
│   ├── NpcCharacterModel.kt  # 3D character model built from primitives
│   ├── GroundPlane.kt        # Scene floor with gradient lighting
│   ├── SkyGradient.kt        # Animated sky background with stars
│   └── ParticleSystem.kt     # Mood-reactive particle effects
└── ui/
    ├── components/
    │   ├── AffectionBar.kt   # Animated affection progress bar
    │   ├── ChatBubble.kt     # Styled chat bubbles (player/NPC/narrator)
    │   └── GameGLSurfaceView.kt # GLSurfaceView wrapper for Compose
    ├── screens/
    │   ├── MainMenuScreen.kt      # Title screen with animated heart
    │   ├── GameScreen.kt          # Main gameplay: 3D view + chat interface
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

### Missions
5 missions with progressive intimacy: Cafe meeting -> Art gallery -> Rainy walk -> Cooking together -> Stargazing confession. Each mission has a scenario, interaction threshold, and target affection gain.

### AI Integration
- **NPC Dialogue**: Each player message is sent to Claude with a system prompt containing Adrian's personality, current mood, affection level, and behavior rules. Claude responds with dialogue, mood, and affection delta as JSON.
- **Mission Analysis**: After completing a mission, the full conversation is sent to Claude for narrative analysis, affection scoring, and player advice.

### 3D Rendering
- OpenGL ES 2.0 with custom GLSL shaders
- Low-poly character built from geometric primitives (tapered boxes + sphere)
- Mood-reactive animations (idle sway, happy bounce, flirty lean, shy look-down)
- Particle system changes based on NPC mood (hearts, sparkles, ambient glow)
- Dynamic scene ambience per mission (sunset, gallery lighting, rain, starlight)

### State Management
- `GameState`: Compose-observable mutable state for real-time UI updates
- `GameViewModel`: Android ViewModel orchestrating AI calls, state updates, persistence
- `PreferencesManager`: DataStore for API key, player name, and save progress

## Key Design Decisions
- API key stored locally in DataStore (user provides their own Anthropic key)
- 3D models built from code primitives (no external asset files needed for MVP)
- Chat-based interaction overlaid on 3D scene for accessible gameplay
- Claude's structured JSON responses enable programmatic mood/affection tracking
