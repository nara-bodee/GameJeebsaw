# DEV BIRD - Game Logic & State Management Documentation

## Overview
DEV BIRD is responsible for all game logic, state management, and game flow orchestration. This document outlines the architecture, key classes, and implementation details.

---

## Architecture Overview

### Key Components

```
GameEngine (server/core/GameEngine.java)
├── GameState - Maintains complete game state snapshot
├── TurnManager - Manages turn/day progression
├── StoryLoader - Loads event data from JSON
└── Event Processing - Applies choice effects and game logic
```

### Game Flow

```
Player Connection (HELLO)
  ↓
All 3 Players Connected → START_GAME
  ↓
TURN_START (Broadcast whose turn)
  ↓
CHOICE_SUBMIT → Apply Effects → Advance Turn
  ↓
Check Day/Game End
  ↓
GAME_OVER or Continue to next turn
```

### Reconnection Flow

```
Player Disconnects
  ↓
PLAYER_DISCONNECTED (60 sec grace)
  ↓
Player Sends RECONNECT {token}
  ↓
STATE_SYNC (Send full game state)
  ↓
PLAYER_RECONNECTED (Notify others)
```

---

## Game State (GameState.java)

The GameState class maintains a complete snapshot of the game that can be serialized for transmission to reconnecting players.

### State Variables

```java
public int dayIndex = 1;           // 1..7 (game lasts 7 days)
public int turnIndex = 0;          // 0..2 (3 turns per day per player)
public String currentEventId = "D1_E1";  // Current event ID
public int timerRemainingSec = 15; // Time remaining for current turn
public List<Player> players;       // 3 players with stats
```

### Player Stats (Love, Charm, Money)

Each Player has three stat types:
- **love** - Main affection score (determines ending)
- **charm** - Can unlock special dialogue/choices
- **money** - Currency for special choices

### Game Progression

**Days**: Game spans 7 days (Day 1 to Day 7)
**Turns**: Each day has 3 turns (Turn 0, 1, 2 = one turn per player)
**Events**: Each day has at least one event with 3-4 choices

### Winner Calculation

Winner is determined by **highest love score** at game end.

```java
String winner = players.stream()
    .max(Comparator.comparingInt(p -> p.love))
    .map(p -> p.playerId)
    .orElse("P1");
```

---

## Game Logic (GameEngine.java)

### Game Initialization

When 3 players connect:
1. Create GameState with initial values
2. Initialize 3 players (P1, P2, P3)
3. Broadcast TURN_START for player P1
4. Send initial STATE_UPDATE

```
onStartGame()
  → Initialize players
  → Set day=1, turn=0, event="D1_E1"
  → broadcastTurnStart()
  → broadcastState()
```

### Choice Processing with Story Effects

The choice system integrates with StoryLoader to apply effects from story.json:

```
CHOICE_SUBMIT {eventId, choiceId}
  ↓
1. Find Choice in Event
2. Check if current player's turn
3. Apply Effect: love ± X, charm ± X, money ± X
4. Advance turn
5. Check day advancement
6. Broadcast updates
```

**Example Choice Effect**:
```json
{
  "choiceId": "C1_1",
  "text": "Help her pay",
  "requirements": {"charmMin": null, "moneyMin": null},
  "effect": {"love": 20, "charm": 5, "money": 0},
  "resultText": "She thanks you! [+20 love]"
}
```

### Turn Management

TurnManager handles turn progression:

```
Current player: players[turnIndex % 3]

After choice:
  turnIndex = (turnIndex + 1) % 3
  if turnIndex == 0:  // New day
    dayIndex++
    currentEventId = "D" + dayIndex + "_E1"
```

### Disconnection & Reconnection

#### Disconnection (onDisconnect)
```
Player disconnects
  → Mark p.connected = false
  → Broadcast PLAYER_DISCONNECTED
  → Start 60-second grace timer
  → If still disconnected after grace: skip their turns
```

#### Reconnection (onReconnect)
```
Player reconnects with valid token
  → Mark p.connected = true
  → Send STATE_SYNC with full game state
  → Broadcast PLAYER_RECONNECTED
  → Send updated state to all
```

#### Timeout Handling
If player disconnects on their turn:
- Currently: turn is skipped (simple approach)
- No AI replacement to avoid complexity
- Player can still reconnect within 60 seconds to resume

---

## Story System (StoryLoader.java)

StoryLoader loads event data from `resources/story.json`.

### Event Structure

```java
public class Event {
    public int day;              // Day number (1-7)
    public String eventId;       // D1_E1, D2_E1, etc.
    public String background;    // Image path
    public String heroineSprite; // Character sprite
    public List<String> dialogue;   // Event text
    public List<Choice> choices;    // Available choices
}
```

### Choice Structure

```java
public class Choice {
    public String choiceId;           // C1_1, C1_2, etc.
    public String text;               // Display text
    public Requirement require;       // Requirements to show choice
    public Effect effect;             // Stats change
    public String resultText;         // Response text
}
```

### Story Data Format (story.json)

```json
{
  "days": [
    {
      "day": 1,
      "eventId": "D1_E1",
      "background": "...",
      "heroineSprite": "...",
      "dialogue": ["...", "..."],
      "choices": [
        {
          "choiceId": "C1_1",
          "text": "Help her",
          "require": {"charmMin": null, "moneyMin": null},
          "effect": {"love": 20, "charm": 0, "money": 0},
          "resultText": "..."
        }
      ]
    }
  ]
}
```

---

## NetworkPackets

### PacketType Constants

```java
// Handshake
HELLO              // {name}
RECONNECT          // {token}
STATE_SYNC         // {fullState}
REJECT             // {reason}

// Game Flow
START_GAME         // No payload
TURN_START         // {currentTurnPlayerId, timerSec}
CHOICE_SUBMIT      // {eventId, choiceId}
STATE_UPDATE       // {fullState}
TIME_OUT           // {playerId}
GAME_OVER          // {winnerPlayerId}

// Presence
PLAYER_DISCONNECTED // {playerId, graceSec}
PLAYER_RECONNECTED  // {playerId}
```

### Full State Schema (for STATE_SYNC/STATE_UPDATE)

```json
{
  "dayIndex": 1,
  "turnIndex": 0,
  "currentEventId": "D1_E1",
  "timerRemainingSec": 15,
  "players": [
    {
      "playerId": "P1",
      "name": "Player 1",
      "love": 20,
      "charm": 5,
      "money": 100,
      "connected": true
    }
  ]
}
```

---

## Implementation Guide for DEV BIRD

### 1. Game Initialization

```java
// In GameEngine.onStartGame()
public void onStartGame(GameServer server) {
    state.players.clear();
    playerOrder.clear();
    
    for (int i = 1; i <= 3; i++) {
        String pid = "P" + i;
        playerOrder.add(pid);
        Player p = new Player(pid, "Player " + pid);
        state.players.add(p);
    }
    
    state.dayIndex = 1;
    state.turnIndex = 0;
    state.currentEventId = "D1_E1";
    
    broadcastTurnStart(server);
    broadcastState(server);
}
```

### 2. Choice Processing with Effects

```java
// In GameEngine.handleChoice()
private void handleChoice(ClientHandler from, DataPacket packet, GameServer server) {
    // 1. Verify it's this player's turn
    String current = turnManager.currentPlayerId(playerOrder);
    if (!from.getPlayerId().equals(current)) return;
    
    // 2. Load event and choice
    String eventId = (String) packet.payload.get("eventId");
    String choiceId = (String) packet.payload.get("choiceId");
    Event event = eventCache.get(eventId);
    Choice choice = findChoice(event, choiceId);
    
    // 3. Apply effects
    Player currentPlayer = state.getPlayer(from.getPlayerId());
    if (currentPlayer != null && choice.effect != null) {
        currentPlayer.love += choice.effect.love;
        currentPlayer.charm += choice.effect.charm;
        currentPlayer.money += choice.effect.money;
    }
    
    // 4. Advance game
    turnManager.nextTurn(playerOrder);
    state.turnIndex = turnManager.getTurnIndex();
    
    if (state.turnIndex == 0) {  // New day
        state.dayIndex++;
        if (state.dayIndex > 7) {
            endGame(server);
            return;
        }
        state.currentEventId = "D" + state.dayIndex + "_E1";
    }
    
    broadcastTurnStart(server);
    broadcastState(server);
}
```

### 3. Story Loader Integration

```java
// In GameEngine constructor
public GameEngine() {
    try {
        List<Event> events = storyLoader.loadEvents();
        for (Event e : events) {
            eventCache.put(e.eventId, e);
        }
    } catch (Exception e) {
        System.err.println("Failed to load story: " + e.getMessage());
    }
}
```

### 4. State Synchronization for Reconnection

```java
// In GameEngine.onReconnect()
public void onReconnect(ClientHandler handler, GameServer server) {
    String playerId = handler.getPlayerId();
    Player p = state.getPlayer(playerId);
    if (p != null) p.connected = true;
    
    disconnectedAt.remove(playerId);
    
    // Send full state to reconnected client
    handler.send(DataPacket.of(PacketType.STATE_SYNC)
            .with("fullState", state.toMap()));
    
    // Notify others
    ServerBroadcaster.broadcast(server.getRoom(),
            DataPacket.of(PacketType.PLAYER_RECONNECTED)
                    .with("playerId", playerId));
    
    broadcastState(server);
}
```

---

## Game End Conditions

Game ends when:
1. **All 7 days completed** - Winner determined by highest love score
2. **Player timeout** (not implemented in basic version)

### Winner Determination

```java
private String calculateWinner() {
    return state.players.stream()
            .max(Comparator.comparingInt(p -> p.love))
            .map(p -> p.playerId)
            .orElse("P1");
}
```

---

## Testing Checklist

- [ ] Game initializes with 3 players
- [ ] Correct player gets turn each round
- [ ] Day progression works (3 turns = 1 day)
- [ ] Choice effects apply correctly
- [ ] State snapshot is complete and accurate
- [ ] Disconnection triggers grace period
- [ ] Reconnection restores game state
- [ ] Game ends on day 8
- [ ] Winner calculated correctly

---

## Future Enhancements

1. **Load from JSON**: Currently uses fallback generator if JSON fails
   - Ensure `story.json` is in resources directory
   - Validate JSON structure before deployment

2. **Event Branching**: Different events based on stats
   - Extend story.json with conditional logic
   - Update StoryLoader to handle branches

3. **AI Timeout**: If needed, implement AI decision for disconnected players
   - Would require strategy/difficulty settings
   - Keep simple to avoid bugs

4. **Timer Implementation**: Add countdown timer
   - Use `timerRemainingSec` field
   - Implement server-side timer thread
   - Auto-timeout after duration

---

## Files Modified/Created by DEV BIRD

**Created:**
- `src/shared/network/PacketType.java` - Packet type constants
- `src/shared/network/DataPacket.java` - Packet structure
- `src/shared/network/PacketCodec.java` - JSON serialization
- `src/shared/models/Event.java` - Event model
- `src/shared/models/Choice.java` - Choice model
- `src/shared/models/Effect.java` - Stats change model
- `src/shared/models/Requirement.java` - Choice requirement model
- `src/shared/models/Player.java` - Player model
- `src/server/core/GameEngine.java` - Main game logic
- `src/server/core/GameState.java` - State management
- `src/server/core/TurnManager.java` - Turn progression
- `src/server/data/StoryLoader.java` - Story loading
- `resources/story.json` - Story data

**Updated:**
- GameEngine integrates with story system
- State properly tracked for all scenarios

---

## Collaboration Notes

- **DEV TIME**: Uses GameEngine.onPacket() and manages connections
- **DEV BIRD**: Implements game logic, choice effects, state management
- **DEV TAO**: Receives STATE_SYNC/STATE_UPDATE, updates UI accordingly
- **DEV MAX**: Uses state snapshots for client validation
- **DEV TIW**: Tests all game flow scenarios and reconnection cases

---

*Last Updated: 2024*
*DEV BIRD Initialization Complete*
