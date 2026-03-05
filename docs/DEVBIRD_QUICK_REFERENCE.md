# DEV BIRD Quick Reference Guide

## What DEV BIRD Provides

### 1. Complete Game State Management
- **GameState.java**: Maintains complete game snapshot with:
  - Day/turn progression (1-7 days, 3 turns each)
  - Player stats (love, charm, money)
  - Current event ID tracking
  - Connection status for all players

### 2. Game Logic Engine
- **GameEngine.java**: Orchestrates all game logic:
  - Initializes game when 3 players connect
  - Processes choices and applies story effects
  - Manages turn progression and day advancement
  - Handles player disconnection/reconnection
  - Detects game end condition and calculates winner

### 3. Turn Management
- **TurnManager.java**: Tracks game progression
  - Current turn (0-2) within a day
  - Player order rotation
  - Day advancement detection

### 4. Story System
- **StoryLoader.java**: Loads game events from JSON
  - Parses `resources/story.json`
  - Provides fallback story if JSON unavailable
  - Caches all events for quick access

### 5. Network Protocol
- **PacketType.java**: Defines all message types
- **DataPacket.java**: Message structure
- **PacketCodec.java**: JSON serialization

### 6. Game Models
- **Player.java**: Stats (love, charm, money) + connection status
- **Event.java**: Day event with dialogue and choices
- **Choice.java**: Choice with requirements and effects
- **Effect.java**: Stats changes from choices
- **Requirement.java**: Unlock conditions for choices

---

## How to Use GameEngine

### In Your Main Server Code

```java
// Create game engine (automatically loads story)
GameEngine engine = new GameEngine();

// When 3 players join
engine.onStartGame(gameServer);

// When receiving choice from player
engine.onPacket(clientHandler, packet, gameServer);

// When player disconnects
engine.onDisconnect(playerId, gameServer);

// When player reconnects with token
engine.onReconnect(clientHandler, gameServer);
```

### Getting Current Game State

```java
GameState state = engine.getState();

// Access current info
int day = state.dayIndex;           // 1-7
int turn = state.turnIndex;         // 0-2
String eventId = state.currentEventId;
List<Player> players = state.players;

// Get player
Player p = state.getPlayer("P1");

// Get event info
Event event = engine.getEvent(eventId);
```

### State Snapshot for Transmission

```java
// Create JSON-serializable map of full state
Map<String, Object> snapshot = state.toMap();

// Send to client
DataPacket packet = DataPacket.of(PacketType.STATE_SYNC)
    .with("fullState", snapshot);
```

---

## Game Flow Details

### Day/Turn Progression

```
Start: dayIndex = 1, turnIndex = 0, P1's turn

After P1 chooses: turnIndex = 1, P2's turn
After P2 chooses: turnIndex = 2, P3's turn
After P3 chooses: turnIndex = 0, dayIndex = 2, P1's turn next
...
After day 7, turn 2: dayIndex = 8 → GAME_OVER
```

### Winner Calculation

Player with **highest love score** wins at game end.

### Choice Requirements

Choices can have requirement requirements:
```json
"require": {"charmMin": 20, "moneyMin": 50}
```

If player doesn't meet requirements, choice may be locked or unavailable.

---

## Story.json Format

Location: `resources/story.json`

Structure:
```json
{
  "days": [
    {
      "day": 1,
      "eventId": "D1_E1",
      "background": "path/to/bg.png",
      "heroineSprite": "path/to/sprite.png",
      "dialogue": ["Line 1", "Line 2"],
      "choices": [
        {
          "choiceId": "C1_1",
          "text": "Choice text",
          "require": {"charmMin": null, "moneyMin": null},
          "effect": {"love": 20, "charm": 5, "money": 0},
          "resultText": "Response from heroine"
        }
      ]
    }
  ]
}
```

---

## Key Classes Status

| Class | Location | Status | Notes |
|-------|----------|--------|-------|
| GameEngine | server/core/ | ✅ Complete | Main logic engine |
| GameState | server/core/ | ✅ Complete | State snapshot |
| TurnManager | server/core/ | ✅ Complete | Turn progression |
| StoryLoader | server/data/ | ✅ Complete | JSON loading |
| Event | shared/models/ | ✅ Complete | Event data model |
| Choice | shared/models/ | ✅ Complete | Choice data model |
| Effect | shared/models/ | ✅ Complete | Effect data model |
| Requirement | shared/models/ | ✅ Complete | Requirement model |
| Player | shared/models/ | ✅ Complete | Player model |
| PacketType | shared/network/ | ✅ Complete | Message types |
| DataPacket | shared/network/ | ✅ Complete | Message structure |
| PacketCodec | shared/network/ | ✅ Complete | Serialization |

---

## Packet Flow for Key Scenarios

### Scenario 1: Normal Choice
```
Client: CHOICE_SUBMIT {eventId: "D1_E1", choiceId: "C1_1"}
  ↓
Engine: onPacket()
  - Verify it's player's turn
  - Load choice from story
  - Apply effect to player stats
  - Advance turn
  - Check day/game end
  ↓
Server: Broadcast TURN_START (next player)
Server: Broadcast STATE_UPDATE (new state)
```

### Scenario 2: Disconnection
```
Client: Socket closes
  ↓
Engine: onDisconnect(playerId)
  - Mark p.connected = false
  - Start 60-sec grace timer
  ↓
Server: Broadcast PLAYER_DISCONNECTED
  - UI shows "Waiting for player..."
```

### Scenario 3: Reconnection
```
Client: RECONNECT {token: "tkn_xxxxx"}
  ↓
Engine: onReconnect()
  - Mark p.connected = true
  - Prepare full state
  ↓
Server: Send STATE_SYNC {fullState: {...}}
Server: Broadcast PLAYER_RECONNECTED
```

---

## Important Notes

1. **Current Turn Validation**
   - Choices are only processed from the player whose turn it is
   - Engine rejects choices from other players

2. **Game End**
   - Game ends when `dayIndex > 7`
   - No special end-game screen in this implementation
   - Winner calculated by highest love score

3. **Reconnection Grace Period**
   - 60 seconds to reconnect after disconnect
   - If player doesn't reconnect: their turns are skipped
   - Token mapping persists on server

4. **Story Data**
   - Must be valid JSON format
   - All 7 days (D1_E1 through D7_E1) should have choices
   - Effects are additive to player stats

5. **No AI Implementation**
   - Disconnected players' turns are simply skipped
   - No AI replacement for simplicity
   - Reduces bugs and complexity

---

## Testing Game Logic

### Basic Flow Test
```
1. Start 3 clients
2. All send HELLO
3. Verify START_GAME received
4. Player P1 sends CHOICE_SUBMIT
5. Verify TURN_START for P2
6. Verify STATE_UPDATE received with updated stats
7. Continue for 21 turns (7 days × 3 turns)
8. Verify GAME_OVER with winner
```

### Disconnection Test
```
1. Game in progress
2. Disconnect P2's client
3. Verify PLAYER_DISCONNECTED broadcast
4. Verify timer shows 60 seconds
5. Reconnect within 60 sec with token
6. Verify STATE_SYNC received
7. Verify game continues normally
```

### State Consistency Test
```
1. Send STATE_SYNC to new client
2. Verify all players/stats/day/turn are correct
3. Continue playing
4. Verify state stays consistent across all clients
```

---

## Dependencies (Gradle/Maven)

Add to build file:
```gradle
dependencies {
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

---

## How DEV BIRD Integrates with Other Roles

### Connection to DEV TIME (Network)
- DEV TIME: Manages TCP sockets and ClientHandler
- DEV BIRD: GameEngine receives packets from ClientHandler
- DEV TIME broadcasts packets to all clients

### Connection to DEV TAO (UI)
- DEV BIRD: Sends STATE_UPDATE and STATE_SYNC packets
- DEV TAO: Parses fullState and updates UI
- DEV TAO: Sends CHOICE_SUBMIT when player selects choice

### Connection to DEV MAX (Client Storage)
- DEV BIRD: Token created in ServerBroadcaster
- DEV MAX: Stores token locally in TokenStore
- DEV MAX: Retrieves token for reconnection

### Connection to DEV TIW (Testing)
- DEV BIRD: Provides complete game state for testing
- DEV TIW: Validates state consistency
- DEV TIW: Tests all reconnection scenarios

---

## What Happens When...

| Event | GameEngine Response |
|-------|-------------------|
| Player connects (HELLO) | ServerBroadcaster registers, returns token |
| 3 players connected | onStartGame() initializes game |
| Player submits choice | handleChoice() applies effects, advances turn |
| Day ends (3 turns done) | dayIndex++, new event loaded |
| Day 7 turn 2 finished | endGame(), winner calculated |
| Player disconnects | onDisconnect(), marks disconnected, starts grace |
| Player reconnects | onReconnect(), sends STATE_SYNC |
| Grace period expires | Disconnected player stays disconnected, turns skipped |

---

## FAQ

**Q: How are players ordered?**
A: Order is P1, P2, P3 based on connection order. Turns cycle: 0→1→2→0

**Q: What if a choice needs money but player has none?**
A: Choice effect applies negative money - stats can go negative in this simple version

**Q: Can multiple choices have the same effect?**
A: Yes, multiple choices can lead to same stat change. Story design is flexible.

**Q: What's the max/min stat values?**
A: No limits in current implementation. Stats can be negative.

---

*DEV BIRD Implementation v1.0*
*Complete Game Logic Ready for Integration*
